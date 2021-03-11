package swiss.sib.sparql.playground.repository.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.springframework.beans.factory.InitializingBean;

import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.exception.SparqlTutorialException;
import swiss.sib.sparql.playground.repository.RDF4jRepository;

/**
 * RDF data store for sesame
 *
 * @author Daniel Teixeira http://github.com/ddtxra
 *
 */
@org.springframework.stereotype.Repository
public class RDF4jRepositoryImpl implements RDF4jRepository, InitializingBean {

	private static final Log logger = LogFactory.getLog(RDF4jRepositoryImpl.class);

	// Read documentation: http://rdf4j.org/sesame/2.7/docs/users.docbook?view
	// http://www.cambridgesemantics.com/semantic-university/sparql-by-example

	private Repository repository = null;
	private SailRepository testRepository;
	private RepositoryConnection connection = null;

	@PostConstruct
	public void init() throws Exception {
		String repositoryTypeProperty = "";
		if (System.getProperty("repository.type") != null) {
			repositoryTypeProperty = System.getProperty("repository.type");
		}

		RepositoryType repositoryType = RepositoryType.getRepositoryType(repositoryTypeProperty);

		if (repositoryType != RepositoryType.DEFAULT) {
			logger.info("Found repository type property! Value:" + repositoryType);
		}

		File ttlFile = new File(Application.FOLDER + "/ttl-data");

		if (repositoryType == RepositoryType.DEFAULT) {
			initializeDefaultRepository(ttlFile);

		} else if (repositoryType == RepositoryType.NATIVE) {
			initializeNativeRepository(ttlFile);

		} else if (repositoryType == RepositoryType.MARK_LOGIC) {
			initializeMarklogicRepository();
		}

		// logger.info("Counting number of triplets...");
		// logger.info("Repository contains " + countTriplets() + " triplets");

		testRepository = new SailRepository(new MemoryStore());
		testRepository.init();
	}

	// load data in-memory
	private void initializeDefaultRepository(File ttlFile) throws RDFParseException, RepositoryException, IOException {
		logger.info("Initializing in memory repository");
		repository = new SailRepository(new MemoryStore());

		repository.init();
		logger.info("Loading turtle files from " + ttlFile);
		addTTLFiles(ttlFile, repository.getConnection());
	}

	// file storage
	private void initializeNativeRepository(File ttlFile) throws RDFParseException, RepositoryException, IOException {
		File rdf4jDataFolder = new File(Application.FOLDER + "/rdf4j-db");
		File rdf4jDataValueFile = new File(rdf4jDataFolder.getPath() + "/values.dat");

		logger.info("Initializing native repository in " + rdf4jDataFolder);
		repository = new SailRepository(new NativeStore(rdf4jDataFolder));

		// load data if there if file does not exist
		if (!rdf4jDataValueFile.exists()) {
			repository.init();

			logger.info("No previous sesame repository found in " + rdf4jDataValueFile);
			logger.info("Loading turtle files from " + ttlFile);
			logger.info(
					"Depending on the number of triplets, this may take some time to load the first time, please be patient ....");

			addTTLFiles(ttlFile, repository.getConnection());

		} else { // do not load data, as file already exists... read log info
			repository.init();
			logger.info("RDF4j repository already found in " + rdf4jDataValueFile);
			logger.info("Skipping to load turtle files. Remove " + rdf4jDataFolder
					+ " folder if you want to reload turtle files");
		}
	}

	// ml connection
	private void initializeMarklogicRepository() {
	}

	private void addTTLFiles(final File folder, RepositoryConnection conn)
			throws RDFParseException, RepositoryException, IOException {
		long start = System.currentTimeMillis();

		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				logger.debug("Loading " + fileEntry);
				conn.add(fileEntry, "", RDFFormat.TURTLE, new Resource[] {});
			}
		}

		logger.info("Loading turtle files finished in " + (System.currentTimeMillis() - start) + " ms");
	}

	public void writeTriplesAsTurtle(OutputStream output, Map<String, String> prefixes) {
		try {
			RepositoryResult<Statement> statements = repository.getConnection().getStatements(null, null, null, true);
			Model model = Iterations.addAll(statements, new LinkedHashModel());

			for (String key : prefixes.keySet()) {
				model.setNamespace(key, prefixes.get(key));
			}

			Rio.write(model, output, RDFFormat.TURTLE);

		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);

		} catch (RDFHandlerException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);
		}
	}

	public void clearData() {
		try {
			RepositoryResult<Statement> statements = repository.getConnection().getStatements(null, null, null, true);
			repository.getConnection().remove(statements);

		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);
		}
	}

	public void testLoadTurtleData(String data) {
		try {
			InputStream stream = new ByteArrayInputStream(data.getBytes());
			RepositoryResult<Statement> statements = testRepository.getConnection().getStatements(null, null, null,
					true);
			testRepository.getConnection().remove(statements);
			testRepository.getConnection().add(stream, "", RDFFormat.TURTLE, new Resource[] {});

		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);

		} catch (RDFParseException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);

		} catch (IOException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);
		}
	}

	public void loadTurtleData(String data) {
		try {
			InputStream stream = new ByteArrayInputStream(data.getBytes());
			repository.getConnection().add(stream, "", RDFFormat.TURTLE, new Resource[] {});

		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);

		} catch (RDFParseException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);

		} catch (IOException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);
		}
	}

	public long countTriplets() {
		try {
			Query query = prepareQuery("SELECT (COUNT(*) AS ?no) { ?s ?p ?o  }");
			TupleQueryResult result = ((TupleQuery) query).evaluate();

			long n = Long.valueOf(result.next().getBinding("no").getValue().stringValue());
			result.close();
			return n;

		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);

		} catch (QueryEvaluationException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);
		}
	}

	public boolean isDataLoadAllowed() {
		return repository.getDataDir() == null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		connection = repository.getConnection();
	}

	@Override
	public Query prepareQuery(String sparqlQuery) {
		try {
			return connection.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);

		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);
		}
	}
}

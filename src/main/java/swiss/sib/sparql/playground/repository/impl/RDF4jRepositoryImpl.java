package swiss.sib.sparql.playground.repository.impl;

import java.io.*;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.semantics.rdf4j.MarkLogicRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.*;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.*;
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

		// File ttlFile = new Directory(); (Application.FOLDER + "/ttl-data");
		File ttlFolder = new File(Application.FOLDER + "/ttl-data");
		File rdfFolder = new File(Application.FOLDER + "/rdf-data");

		try {
			if (repositoryType == RepositoryType.DEFAULT) {
				initializeDefaultRepository(ttlFolder, rdfFolder);

			} else if (repositoryType == RepositoryType.NATIVE) {
				initializeNativeRepository(ttlFolder, rdfFolder);

			} else if (repositoryType == RepositoryType.MARK_LOGIC) {
				initializeMarklogicRepository(ttlFolder, rdfFolder);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		logger.info("Counting number of triplets...");
		logger.info("Repository contains " + countTriplets() + " triplets");

		testRepository = new SailRepository(new MemoryStore());
		testRepository.init();
	}

	// load data in-memory
	private void initializeDefaultRepository(File ttlFolder, File rdfFolder)
			throws RDFParseException, RepositoryException, IOException {
		logger.info("Initializing in memory repository");
		repository = new SailRepository(new MemoryStore());
		repository.init();

		RepositoryConnection connection = repository.getConnection();

		logger.info("Loading turtle files from " + ttlFolder);
		addTTLFiles(ttlFolder, connection);

		logger.info("Loading turtle files from " + rdfFolder);
		addRDFFiles(rdfFolder, connection);
	}

	// file storage
	private void initializeNativeRepository(File ttlFolder, File rdfFolder)
			throws RDFParseException, RepositoryException, IOException {
		File rdf4jDataFolder = new File(Application.FOLDER + "/rdf4j-db");
		File rdf4jDataValueFile = new File(rdf4jDataFolder.getPath() + "/values.dat");

		logger.info("Initializing native repository in " + rdf4jDataFolder);
		repository = new SailRepository(new NativeStore(rdf4jDataFolder));

		// load data if there if file does not exist
		if (!rdf4jDataValueFile.exists()) {
			repository.init();

			logger.info("No previous sesame repository found in " + rdf4jDataValueFile);
			RepositoryConnection connection = repository.getConnection();

			logger.info("Loading turtle files from " + ttlFolder);
			addTTLFiles(ttlFolder, connection);

			logger.info("Loading turtle files from " + rdfFolder);
			addRDFFiles(rdfFolder, connection);

		} else { // do not load data, as file already exists... read log info
			repository.init();
			logger.info("RDF4j repository already found in " + rdf4jDataValueFile);
			logger.info("Skipping to load turtle files. Remove " + rdf4jDataFolder
					+ " folder if you want to reload turtle files");
		}
	}

	// ml connection
	private void initializeMarklogicRepository(File ttlFolder, File rdfFolder)
			throws RDFParseException, RepositoryException, IOException {
		logger.info("Initializing MarkLogic repository");

		SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");
		repository = new MarkLogicRepository(Application.MARKLOGIC_ADDRESS, Application.MARKLOGIC_PORT,
				securityContext);
		repository.init();

		long tripletCounter = countTriplets();
		if (tripletCounter > 0) {
			logger.info("Data already present in MarkLogic DB. Number of triples: " + tripletCounter);
			return;
		}

		RepositoryConnection connection = repository.getConnection();

		logger.info("Loading turtle files from " + ttlFolder);
		addTTLFiles(ttlFolder, connection);

		logger.info("Loading turtle files from " + rdfFolder);
		addRDFFiles(rdfFolder, connection);
	}

	private void addTTLFiles(final File folder, RepositoryConnection connection)
			throws RDFParseException, RepositoryException, IOException {
		long start = System.currentTimeMillis();

		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				logger.debug("Loading " + fileEntry);

				connection.add(fileEntry, null, RDFFormat.TURTLE, (Resource) null);
			}
		}

		logger.info("Loading turtle files finished in " + (System.currentTimeMillis() - start) + " ms");
	}

	private void addRDFFiles(final File folder, RepositoryConnection connection)
			throws RDFParseException, RepositoryException, IOException {
		long start = System.currentTimeMillis();

		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				logger.debug("Loading " + fileEntry);

				connection.add(fileEntry, null, RDFFormat.RDFXML, (Resource) null);
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
			if (connection == null) {
				connection = repository.getConnection();
			}

			return connection.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);

		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);
		}
	}
}

package swiss.sib.sparql.playground.repository.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.semantics.rdf4j.MarkLogicRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
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
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import org.springframework.beans.factory.InitializingBean;

import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.exception.SparqlTutorialException;
import swiss.sib.sparql.playground.repository.RDF4jRepository;

/**
 * RDF data store for RDF4J
 *
 * @author of original RDF data store for Sesame: Daniel Teixeira
 *         http://github.com/ddtxra
 *
 */
@org.springframework.stereotype.Repository
public class RDF4jRepositoryImpl implements RDF4jRepository, InitializingBean {

	private static final Log logger = LogFactory.getLog(RDF4jRepositoryImpl.class);

	// Read documentation: https://rdf4j.org/documentation/programming/repository/
	// http://www.cambridgesemantics.com/semantic-university/sparql-by-example

	private Repository repository = null;
	private RepositoryConnection connection = null;

	// #region Initialization
	@PostConstruct
	public void init() throws Exception {
		Boolean loadDataFlag = false;
		RepositoryType repositoryType = Application.getRepositoryType();

		try {
			if (repositoryType == RepositoryType.DEFAULT) {
				loadDataFlag = initializeDefaultRepository();

			} else if (repositoryType == RepositoryType.NATIVE) {
				loadDataFlag = initializeNativeRepository();

			} else if (repositoryType == RepositoryType.MARK_LOGIC) {
				loadDataFlag = initializeMarklogicRepository();
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		// load data into initialized repository
		if (loadDataFlag) {
			loadDataFromFiles();
		}

		logger.info("Counting number of triplets...");
		logger.info("Repository contains " + countTriplets() + " triplets");

		if (Application.getInferencingEnabled()) {
			logger.info("Counting number of inferenced triplets...");
			logger.info("Repository contains " + countInferredTriplets() + " inferenced triplets");
		}

		// initialize test repository
		testRepository = new SailRepository(new MemoryStore());
		testRepository.init();
	}

	// load data in-memory
	private Boolean initializeDefaultRepository() throws RDFParseException, RepositoryException, IOException {
		logger.info("Initializing in memory repository");

		if (Application.getInferencingEnabled()) {
			this.repository = new SailRepository(new SchemaCachingRDFSInferencer(new MemoryStore()));
		} else {
			this.repository = new SailRepository(new MemoryStore());
		}
		this.repository.init();
		this.connection = repository.getConnection();

		return true;
	}

	// file storage
	private Boolean initializeNativeRepository() throws RDFParseException, RepositoryException, IOException {
		File rdf4jDataFolder = new File(Application.getFolder() + "/rdf4j-db");
		File rdf4jDataValueFile = new File(rdf4jDataFolder.getPath() + "/values.dat");
		Boolean loadDataFlag = !rdf4jDataValueFile.exists();

		logger.info("Initializing native repository in " + rdf4jDataFolder);
		if (Application.getInferencingEnabled()) {
			this.repository = new SailRepository(new SchemaCachingRDFSInferencer(new NativeStore(rdf4jDataFolder)));
		} else {
			this.repository = new SailRepository(new NativeStore(rdf4jDataFolder));
		}
		this.repository.init();
		this.connection = repository.getConnection();

		// load data if there if file does not exist
		if (loadDataFlag) {
			logger.info("No previous RDF4J repository found in " + rdf4jDataValueFile + "Loading triples...");
		}
		// do not load data, as file already exists...
		else {
			logger.info("RDF4j repository already found in " + rdf4jDataValueFile);
			logger.info("Skipping to loading data. Remove " + rdf4jDataFolder
					+ " folder if you want to reload turtle files");
		}

		return loadDataFlag;
	}

	// marklogic connection
	private Boolean initializeMarklogicRepository() throws RDFParseException, RepositoryException, IOException {
		String host = Application.getMarklogicHost();
		Integer port = Application.getMarklogicPort();
		SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");

		logger.info("Initializing MarkLogic repository");

		// if (Application.getInferencingEnabled()) {

		// } else {

		// }

		this.repository = new MarkLogicRepository(host, port, securityContext);
		this.repository.init();
		this.connection = repository.getConnection();

		long tripletCounter = countTriplets();
		Boolean loadDataFlag = tripletCounter == 0;

		// load data if there if file does not exist
		if (loadDataFlag) {
			logger.info("No data present in MarkLogic database. Loading triples... ");

		} else {
			logger.info("Data already present in MarkLogic DB. Number of triples: " + tripletCounter);
		}

		return loadDataFlag;
	}

	private void loadDataFromFiles() {
		try {
			loadTTLFiles();
			loadRDFFiles();

		} catch (RDFParseException e) {
			logger.error(e.getMessage(), e);
		} catch (RepositoryException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void loadTTLFiles() throws RDFParseException, RepositoryException, IOException {
		// if (this.connection == null) {
		// logger.warn("Repository connection was null.");
		// this.connection = this.repository.getConnection();
		// }

		long start = System.currentTimeMillis();
		File ttlFolder = new File(Application.getFolder() + "/ttl-data");

		if (!ttlFolder.exists()) {
			logger.warn("Folder for Turtle data was not found. Path: " + ttlFolder);
			if (ttlFolder.mkdirs()) {
				logger.debug("Folder for Turtle data successfully created. Path: " + ttlFolder);
			}
			return;
		}

		logger.info("Loading turtle files from " + ttlFolder);

		for (final File fileEntry : ttlFolder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				logger.debug("Loading " + fileEntry);
				this.connection.add(fileEntry, null, RDFFormat.TURTLE, (Resource) null);
			}
		}

		logger.info("Loading turtle files finished in " + (System.currentTimeMillis() - start) + " ms");
	}

	private void loadRDFFiles() throws RDFParseException, RepositoryException, IOException {
		// if (this.connection == null) {
		// logger.warn("Repository connection was null.");
		// this.connection = this.repository.getConnection();
		// }

		long start = System.currentTimeMillis();
		File rdfFolder = new File(Application.getFolder() + "/rdf-data");

		if (!rdfFolder.exists()) {
			logger.warn("Folder for RDF data was not found. Path: " + rdfFolder);
			if (rdfFolder.mkdirs()) {
				logger.debug("Folder for RDF data successfully created. Path: " + rdfFolder);
			}
			return;
		}

		logger.info("Loading rdf files from " + rdfFolder);

		for (final File fileEntry : rdfFolder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				logger.debug("Loading " + fileEntry);
				this.connection.add(fileEntry, null, RDFFormat.RDFXML, (Resource) null);
			}
		}

		logger.info("Loading turtle files finished in " + (System.currentTimeMillis() - start) + " ms");
	}
	// #endregion Initialization

	@Override
	public Query prepareQuery(String sparqlQuery) {
		// if (this.connection == null) {
		// logger.warn("Repository connection was null.");
		// this.connection = this.repository.getConnection();
		// }

		try {
			return this.connection.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);

		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);
		}
	}

	public boolean hasStatement(Statement statement) {
		return this.connection.hasStatement(statement, true);
	}

	public RepositoryResult<Statement> getStatements(Resource subject, IRI predicate, Value object,
			boolean includeInferred) {
		return this.connection.getStatements(subject, predicate, object, includeInferred);
	}

	public void addStatements(List<Statement> statements) {
		this.connection.add(statements);
	}

	public void removeStatements(List<Statement> statements) {
		this.connection.remove(statements);
	}

	public void removeAllStatements() {
		try {
			RepositoryResult<Statement> statements = this.connection.getStatements(null, null, null, true);
			this.connection.remove(statements);

		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);
		}
	}

	public long countInferredTriplets() {
		long nonInferredTripletsCounter = 0;
		long inferredTripletsCounter = 0;

		try {
			// // no inferencing
			RepositoryResult<Statement> noInfrencingResult = this.connection.getStatements(null, null, null, false);
			while (noInfrencingResult.hasNext()) {
				nonInferredTripletsCounter++;
				noInfrencingResult.next();
			}

			// no inferencing
			RepositoryResult<Statement> InferencingResult = this.connection.getStatements(null, null, null, true);
			while (InferencingResult.hasNext()) {
				inferredTripletsCounter++;
				InferencingResult.next();
			}

			logger.info("Number of triplets without inferencing: " + nonInferredTripletsCounter);
			logger.info("Number of triplets with inferencing: " + inferredTripletsCounter);

		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new SparqlTutorialException(e);
		}

		return inferredTripletsCounter - nonInferredTripletsCounter;
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

	// #region Legacy
	@Override
	public void afterPropertiesSet() throws Exception {
		// empty
	}

	public boolean isDataLoadAllowed() {
		return this.repository.getDataDir() == null;
	}

	public void loadTurtleData(String data) {
		// if (this.connection == null) {
		// logger.warn("Repository connection was null.");
		// this.connection = this.repository.getConnection();
		// }

		try {
			InputStream stream = new ByteArrayInputStream(data.getBytes());
			this.connection.add(stream, "", RDFFormat.TURTLE, new Resource[] {});

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

	public void writeTriplesAsTurtle(OutputStream output, Map<String, String> prefixes) {
		try {
			RepositoryResult<Statement> statements = this.connection.getStatements(null, null, null, true);
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

	private SailRepository testRepository;

	public void testLoadTurtleData(String data) {
		try {
			InputStream stream = new ByteArrayInputStream(data.getBytes());
			RepositoryConnection testConnection = this.testRepository.getConnection();
			RepositoryResult<Statement> statements = testConnection.getStatements(null, null, null, true);
			testConnection.remove(statements);
			testConnection.add(stream, "", RDFFormat.TURTLE, new Resource[] {});

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
	// #endregion
}

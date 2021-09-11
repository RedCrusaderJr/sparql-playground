package swiss.sib.sparql.playground.Performance;

import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.semantics.rdf4j.MarkLogicRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import swiss.sib.sparql.playground.domain.SparqlQuery;
import swiss.sib.sparql.playground.domain.SparqlQueryType;
import swiss.sib.sparql.playground.geosparql.marklogic.SparqlEvaluator;
import swiss.sib.sparql.playground.repository.QueryDictionary;

public class PerformanceTestCommon {
	private static final Log logger = LogFactory.getLog(PerformanceTestCommon.class);

	private static final String NEW_LINE = System.lineSeparator();
	private static final String TEST_FOLDER = "test_folder";
	private static final String MARKLOGIC_HOST = "localhost";
	private static final int MARKLOGIC_PORT = 8111;
	// private static final String MARKLOGIC_DB_NAME = "sparql-playground";

	private Repository repository;
	private RepositoryConnection connection;

	private String testQuerySubfolder;
	private MetricTracer metricTracer;
	private SparqlEvaluator sparqlEvaluator;
	private QueryDictionary queryDictionary;
	private Map<String, Map<String, SparqlQuery>> queryMap;

	public PerformanceTestCommon(MetricTracer metricTracer, String testQuerySubfolder) {
		this.testQuerySubfolder = testQuerySubfolder;
		this.metricTracer = metricTracer;

		this.sparqlEvaluator = SparqlEvaluator.getInstance();
		this.queryDictionary = new QueryDictionary();
		this.queryMap = new HashMap<String, Map<String, SparqlQuery>>();
		initQueryMap();
	}

	public void afterEach() {
		this.connection.close();
		this.repository.shutDown();
	}

	public void defaultRepositoryTest(String name) throws Exception {
		// init
		long initStart = System.currentTimeMillis();
		initializeDefaultRepository();
		double initDuration = ((double) (System.currentTimeMillis() - initStart)) / (double) 1000;
		this.metricTracer.appendInit("defaultRepository [" + name + "] initialization lasted " + initDuration + " ms");
		this.metricTracer.appendInit(NEW_LINE);

		// load data
		long loadStart = System.currentTimeMillis();
		loadDataFromFiles();
		double loadDuration = ((double) (System.currentTimeMillis() - loadStart)) / (double) 1000;
		this.metricTracer.appendLoad("defaultRepository [" + name + "] loading data lasted " + loadDuration + " sec");
		this.metricTracer.appendLoad(NEW_LINE);

		// evaluate query
		String sparqlQuery = getTestQueryString(name);
		long evalStart = System.currentTimeMillis();
		Query query = this.connection.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);
		Object result = evaluateQuery(query);
		double evalDuration = ((double) (System.currentTimeMillis() - evalStart)) / (double) 1000;
		this.metricTracer
				.appendEval("defaultRepository [" + name + "] evaluating query lasted " + evalDuration + " sec");
		this.metricTracer.appendEval(NEW_LINE);

		long counter = countTQRBindingSets((TupleQueryResult) result);
		this.metricTracer.appendCounters("defaultRepository [" + name + "] Binding sets in result: " + counter);
		this.metricTracer.appendCounters(NEW_LINE);
	}

	public void nativeRepositoryTest(String name) throws Exception {
		// init
		long initStart = System.currentTimeMillis();
		File rdf4jDataFolder = new File(TEST_FOLDER + "/rdf4j-db");
		File rdf4jDataValueFile = new File(rdf4jDataFolder.getPath() + "/values.dat");
		Boolean loadDataFlag = !rdf4jDataValueFile.exists();
		initializeNativeRepository(rdf4jDataValueFile);
		double initDuration = ((double) (System.currentTimeMillis() - initStart)) / (double) 1000;
		this.metricTracer.appendInit("nativeRepository [" + name + "] initialization lasted " + initDuration + " sec");
		this.metricTracer.appendInit(NEW_LINE);

		// load data
		if (loadDataFlag) {
			long loadStart = System.currentTimeMillis();
			loadDataFromFiles();
			double loadDuration = ((double) (System.currentTimeMillis() - loadStart)) / (double) 1000;
			this.metricTracer
					.appendLoad("nativeRepository [" + name + "] loading data lasted " + loadDuration + " sec");
			this.metricTracer.appendLoad(NEW_LINE);
		}

		// evaluate query
		String sparqlQuery = getTestQueryString(name);
		long evalStart = System.currentTimeMillis();
		Query query = this.connection.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);
		Object result = evaluateQuery(query);
		double evalDuration = ((double) (System.currentTimeMillis() - evalStart)) / (double) 1000;
		this.metricTracer
				.appendEval("nativeRepository [" + name + "] evaluating query lasted " + evalDuration + " sec");
		this.metricTracer.appendEval(NEW_LINE);

		long counter = countTQRBindingSets((TupleQueryResult) result);
		this.metricTracer.appendCounters("nativeRepository [" + name + "] Binding sets in result: " + counter);
		this.metricTracer.appendCounters(NEW_LINE);
	}

	public void markLogicRepositoryTest(String name) throws Exception {
		// init
		long initStart = System.currentTimeMillis();
		initializeMarkLogicRepository();
		double initDuration = ((double) (System.currentTimeMillis() - initStart)) / (double) 1000;
		this.metricTracer
				.appendInit("markLogicRepository [" + name + "] initialization lasted " + initDuration + " sec");
		this.metricTracer.appendInit(NEW_LINE);

		// load data
		long tripletCounter = countTriplets();
		if (tripletCounter == 0) {
			long loadStart = System.currentTimeMillis();
			loadDataFromFiles();
			Double loadDuration = ((double) (System.currentTimeMillis() - loadStart)) / (double) 1000;
			this.metricTracer
					.appendLoad("markLogicRepository [" + name + "] Loading data lasted " + loadDuration + " sec");
			this.metricTracer.appendLoad(NEW_LINE);
		}

		// evaluate query
		String sparqlQueryStr = getTestQueryString(name);
		long evalStart = System.currentTimeMillis();

		try {
			Query sparqlQuery = this.connection.prepareQuery(QueryLanguage.SPARQL, sparqlQueryStr);
			Object result = evaluateQuery(sparqlQuery);
			double evalDuration = ((double) (System.currentTimeMillis() - evalStart)) / (double) 1000;
			this.metricTracer
					.appendEval("markLogicRepository [" + name + "] evaluating query lasted " + evalDuration + " sec");
			this.metricTracer.appendEval(NEW_LINE);

			long counter = countTQRBindingSets((TupleQueryResult) result);
			this.metricTracer
					.appendCounters("markLogicRepository [" + name + "] Number of binding sets in result: " + counter);
			this.metricTracer.appendCounters(NEW_LINE);

		} catch (QueryEvaluationException e) {
			if (!e.getMessage().contains("Server Message: XDMP-UNDFUN")) {
				throw e;
			}

			Object result = evaluateOnMarklogicSemanticApi(sparqlQueryStr);
			double evalDuration = ((double) (System.currentTimeMillis() - evalStart)) / (double) 1000;
			this.metricTracer
					.appendEval("markLogicRepository [" + name + "] evaluating query lasted " + evalDuration + " sec");
			this.metricTracer.appendEval(NEW_LINE);

			long counter = countTQRBindingSets((TupleQueryResult) result);
			this.metricTracer
					.appendCounters("markLogicRepository [" + name + "] Number of binding sets in result: " + counter);
			this.metricTracer.appendCounters(NEW_LINE);
		}
	}

	private Object evaluateQuery(Query query) throws Exception {
		switch (SparqlQueryType.getQueryType(query)) {
		case TUPLE_QUERY:
			return ((TupleQuery) query).evaluate();

		case GRAPH_QUERY:
			return ((GraphQuery) query).evaluate();

		case BOOLEAN_QUERY:
			return ((BooleanQuery) query).evaluate();

		default:
			throw new Exception("Unsupported query type: " + query.getClass().getName());
		}
	}

	private Object evaluateOnMarklogicSemanticApi(String queryStr) throws Exception {
		long start = System.currentTimeMillis();
		Object result = this.sparqlEvaluator.evaluateQuery(queryStr);
		double duration = ((double) (System.currentTimeMillis() - start)) / (double) 1000;
		this.metricTracer
				.appendMarkLogic("evaluateOnMarklogicSemanticApi -> evaluating query lasted " + duration + " sec");
		this.metricTracer.appendMarkLogic(NEW_LINE);

		return result;
	}

	// #region SET UP
	private void initializeDefaultRepository() {
		this.repository = new SailRepository(new MemoryStore());
		this.repository.init();
		this.connection = repository.getConnection();
	}

	private void initializeNativeRepository(File rdf4jDataValueFile) {
		this.repository = new SailRepository(new NativeStore(rdf4jDataValueFile));
		this.repository.init();
		this.connection = repository.getConnection();
	}

	private void initializeMarkLogicRepository() {
		SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");
		this.repository = new MarkLogicRepository(MARKLOGIC_HOST, MARKLOGIC_PORT, securityContext);
		this.repository.init();
		this.connection = repository.getConnection();
	}

	private void initQueryMap() {
		File sparqlQueriesFolder = new File(TEST_FOLDER + "/queries/sparql");
		if (sparqlQueriesFolder.exists() == false) {
			logger.error("Simulator folder not found. Path: " + sparqlQueriesFolder.getPath());
			return;
		}

		for (File subFolder : sparqlQueriesFolder.listFiles()) {
			Map<String, SparqlQuery> queries = new HashMap<String, SparqlQuery>();

			for (SparqlQuery query : this.queryDictionary.getQueries(subFolder.getPath())) {
				queries.put(query.getTitle().trim(), query);
			}
			this.queryMap.put(subFolder.getName().trim(), queries);
		}
	}

	private void loadDataFromFiles() throws Exception {
		loadTTLFiles();
		loadRDFFiles();
	}

	private void loadTTLFiles() throws Exception {
		File ttlFolder = new File(TEST_FOLDER + "/ttl-data");
		if (!ttlFolder.exists()) {
			logger.warn("Folder for Turtle data was not found. Path: " + ttlFolder);
			if (ttlFolder.mkdirs()) {
				logger.debug("Folder for Turtle data successfully created. Path: " + ttlFolder);
			}
			return;
		}

		logger.info("Loading turtle files from " + ttlFolder);
		long start = System.currentTimeMillis();
		for (final File fileEntry : ttlFolder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				// logger.debug("Loading " + fileEntry);
				this.connection.add(fileEntry, null, RDFFormat.TURTLE, (Resource) null);
			}
		}

		logger.info("Loading turtle files finished in " + (System.currentTimeMillis() - start) + " ms");
	}

	private void loadRDFFiles() throws Exception {
		File rdfFolder = new File(TEST_FOLDER + "/rdf-data");
		if (!rdfFolder.exists()) {
			logger.warn("Folder for RDF data was not found. Path: " + rdfFolder);
			if (rdfFolder.mkdirs()) {
				logger.debug("Folder for RDF data successfully created. Path: " + rdfFolder);
			}
			return;
		}

		logger.info("Loading rdf files from " + rdfFolder);
		long start = System.currentTimeMillis();
		for (final File fileEntry : rdfFolder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				// logger.debug("Loading " + fileEntry);
				this.connection.add(fileEntry, null, RDFFormat.RDFXML, (Resource) null);
			}
		}

		logger.info("Loading turtle files finished in " + (System.currentTimeMillis() - start) + " ms");
	}
	// #endregion SET UP

	// #region Test Queries
	private String getTestQueryString(String queryName) {
		return this.queryMap.get(this.testQuerySubfolder).get(queryName).getSparql();
	}
	// #endregion Test Queries

	// #region Count Methods
	private long countTriplets() {
		String queryStr = "SELECT (COUNT(*) AS ?no) { ?s ?p ?o  }";
		Query query = this.connection.prepareQuery(QueryLanguage.SPARQL, queryStr);
		TupleQueryResult result = ((TupleQuery) query).evaluate();

		long n = Long.valueOf(result.next().getBinding("no").getValue().stringValue());
		result.close();
		return n;
	}

	private long countTQRBindingSets(TupleQueryResult result) {
		long counter = 0;

		while (result.hasNext()) {
			counter++;
			result.next();
		}

		return counter;
	}
	// #endregion Count Methods
}

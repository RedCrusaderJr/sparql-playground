package swiss.sib.sparql.playground.Performance;

import com.gembox.spreadsheet.*;
import com.gembox.spreadsheet.tables.*;

import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.client.eval.EvalResultIterator;
import com.marklogic.semantics.rdf4j.MarkLogicRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.eclipse.rdf4j.query.impl.TupleQueryResultBuilder;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import swiss.sib.sparql.playground.domain.JavaScriptQuery;
import swiss.sib.sparql.playground.domain.SparqlQuery;
import swiss.sib.sparql.playground.domain.SparqlQueryType;
import swiss.sib.sparql.playground.geosparql.GeoSparqlEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.MarklogicGeoSparqlEvaluator;
import swiss.sib.sparql.playground.repository.QueryDictionary;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MarklogicGeoSparqlEvaluator.class })
public class PerformanceTestCommon {
	private static final Log logger = LogFactory.getLog(PerformanceTestCommon.class);

	private static final String NEW_LINE = System.lineSeparator();
	private static final String TEST_FOLDER = "test_folder";
	private static final String MARKLOGIC_HOST = "localhost";
	private static final int MARKLOGIC_PORT = 8111;
	private static final String MARKLOGIC_DB_NAME = "sparql-playground";

	public static final String MARKLOGIC_APPROACH_1 = "approach1";
	public static final String MARKLOGIC_APPROACH_1_ML = "ml-approach1";
	public static final String MARKLOGIC_APPROACH_2 = "approach2";

	private Repository repository;
	private RepositoryConnection connection;

	private String cimxmlFolder;
	private String queryFolder;
	private QueryDictionary queryDictionary;
	private Map<String, Map<String, SparqlQuery>> sparqlQueryMap;
	private Map<String, Map<String, JavaScriptQuery>> javascriptQueryMap;
	private MetricTracer metricTracer;
	private ExcelTracer excelTracer;
	@Autowired
	private GeoSparqlEvaluator geoSparqlEvaluator;

	public PerformanceTestCommon(MetricTracer metricTracer, String queryFolder, String cimxmlFolder,
			String excelFileName) throws Exception {
		this.cimxmlFolder = cimxmlFolder;
		this.queryFolder = queryFolder;
		this.metricTracer = metricTracer;
		this.excelTracer = new ExcelTracer();
		this.excelTracer.startExcelFile(excelFileName);

		this.queryDictionary = new QueryDictionary();
		this.sparqlQueryMap = new HashMap<String, Map<String, SparqlQuery>>();
		this.javascriptQueryMap = new HashMap<String, Map<String, JavaScriptQuery>>();
		initQueryMap();
	}

	public void afterEach() {
		if (this.connection != null) {
			this.connection.close();
		}
		this.connection = null;

		if (this.repository != null) {
			this.repository.shutDown();
		}
		this.repository = null;
	}

	public void deleteAll() {
		this.deleteAllNative();
		this.deleteAllMarkLogic();
	}

	public void startExcelTracer(String sheetName) {
		this.excelTracer.setSheetName(sheetName);
	}

	public void resetRowTracker() {
		this.excelTracer.resetRowTracker();
	}

	public void saveExcel() throws IOException {
		this.excelTracer.saveExcelFile();
	}

	public void nextRow() throws IOException {
		this.excelTracer.nextRow();
	}

	public void trace(String currentTestName) throws IOException {
		logger.info(this.metricTracer.completeTrace(currentTestName) + NEW_LINE);
	}

	public long defaultRepositoryTest(String testName, String tableName, int warmupCount) throws Exception {
		// init
		long initStart = System.currentTimeMillis();
		initializeDefaultRepository();
		long initDuration = System.currentTimeMillis() - initStart;
		this.metricTracer
				.appendInit("defaultRepository [" + testName + "] initialization lasted " + initDuration + " ms");
		this.metricTracer.appendInit(NEW_LINE);
		// this.excelTracer.traceToTable(tableName, columnName, data);

		// load data
		long tripletCounter = countTriplets();
		if (tripletCounter == 0) {
			long loadStart = System.currentTimeMillis();
			loadDataFromFiles();
			long loadDuration = System.currentTimeMillis() - loadStart;
			this.metricTracer
					.appendLoad("defaultRepository [" + testName + "] Loading data lasted " + loadDuration + " ms");
			this.metricTracer.appendLoad(NEW_LINE);
			// this.excelTracer.traceToTable(tableName, columnName,
			// evalDuration);
		}
		metricTracer.appendCommon("defaultRepository contains " + countTriplets() + " triplets" + NEW_LINE);

		String sparqlQuery = getSparqlTestQueryString(testName);
		Query query = this.connection.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);

		// warmup
		for (int i = 0; i < warmupCount; i++) {
			evaluateQuery(query);
		}

		// evaluate query
		long evalStart = System.currentTimeMillis();
		evaluateQuery(query);
		long evalDuration = System.currentTimeMillis() - evalStart;
		this.metricTracer
				.appendEval("defaultRepository [" + testName + "] evaluating query lasted " + evalDuration + " ms");
		this.metricTracer.appendEval(NEW_LINE);

		this.excelTracer.traceToTable(tableName, testName, evalDuration);
		return evalDuration;
	}

	public long nativeRepositoryTest(String testName, String tableName, int warmupCount) throws Exception {
		// init
		long initStart = System.currentTimeMillis();
		File rdf4jDataFolder = new File(TEST_FOLDER + "/rdf4j-db");
		File rdf4jDataValueFile = new File(rdf4jDataFolder.getPath() + "/values.dat");
		Boolean loadDataFlag = !rdf4jDataValueFile.exists();
		initializeNativeRepository(rdf4jDataValueFile);
		long initDuration = System.currentTimeMillis() - initStart;
		this.metricTracer
				.appendInit("nativeRepository [" + testName + "] initialization lasted " + initDuration + " ms");
		this.metricTracer.appendInit(NEW_LINE);
		// this.excelTracer.traceToTable(tableName, columnName,
		// evalDuration);

		// load data
		if (loadDataFlag) {
			long loadStart = System.currentTimeMillis();
			loadDataFromFiles();
			long loadDuration = System.currentTimeMillis() - loadStart;
			this.metricTracer
					.appendLoad("nativeRepository [" + testName + "] loading data lasted " + loadDuration + " ms");
			this.metricTracer.appendLoad(NEW_LINE);
			// this.excelTracer.traceToTable(tableName, columnName,
			// evalDuration);
		}
		metricTracer.appendCommon("Native Repository contains " + countTriplets() + " triplets" + NEW_LINE);

		String sparqlQuery = getSparqlTestQueryString(testName);
		Query query = this.connection.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);

		// warmup
		for (int i = 0; i < warmupCount; i++) {
			evaluateQuery(query);
		}

		// evaluate query
		long evalStart = System.currentTimeMillis();
		evaluateQuery(query);
		long evalDuration = System.currentTimeMillis() - evalStart;
		this.metricTracer
				.appendEval("defaultRepository [" + testName + "] evaluating query lasted " + evalDuration + " ms");
		this.metricTracer.appendEval(NEW_LINE);

		this.excelTracer.traceToTable(tableName, testName, evalDuration);
		return evalDuration;
	}

	public long markLogicRepositoryTest(String testName, String approach, String tableName, int warmupCount)
			throws Exception {

		// evaluate query
		long duration = 0;
		if (approach.equals(MARKLOGIC_APPROACH_1)) {
			duration = marklogicApproach1(testName, tableName, false, warmupCount);

		} else if (approach.equals(MARKLOGIC_APPROACH_2)) {
			duration = marklogicApproach2(testName, tableName, warmupCount);

		} else if (approach.equals(MARKLOGIC_APPROACH_1_ML)) {
			duration = marklogicApproach1(testName, tableName, true, warmupCount);

		} else {
			duration = marklogicApproach1(testName, tableName, false, warmupCount);
		}

		return duration;
	}

	private long marklogicApproach1(String testName, String tableName, Boolean isMLCase, int warmupCount)
			throws Exception {
		// init
		long initStart = System.currentTimeMillis();
		initializeMarkLogicRepository();
		long initDuration = System.currentTimeMillis() - initStart;
		this.metricTracer
				.appendInit("markLogicRepository [" + testName + "] initialization lasted " + initDuration + " ms");
		this.metricTracer.appendInit(NEW_LINE);
		// this.excelTracer.traceToTable(tableName, columnName,
		// evalDuration);

		// load data
		long tripletCounter = countTriplets();
		if (tripletCounter == 0) {
			long loadStart = System.currentTimeMillis();
			loadDataFromFiles();
			long loadDuration = System.currentTimeMillis() - loadStart;
			this.metricTracer
					.appendLoad("markLogicRepository [" + testName + "] Loading data lasted " + loadDuration + " ms");
			this.metricTracer.appendLoad(NEW_LINE);
			// this.excelTracer.traceToTable(tableName, columnName,
			// evalDuration);
		}
		metricTracer.appendCommon("Marklogic Repository contains " + countTriplets() + " triplets" + NEW_LINE);

		String sparqlQueryStr = "";
		if (isMLCase) {
			sparqlQueryStr = getSparqlTestQueryString("ml-" + testName);
		} else {
			sparqlQueryStr = getSparqlTestQueryString(testName);
		}

		long evalStart = System.currentTimeMillis();

		try {
			Query sparqlQuery = this.connection.prepareQuery(QueryLanguage.SPARQL, sparqlQueryStr);

			// warmup
			for (int i = 0; i < warmupCount; i++) {
				evaluateQuery(sparqlQuery);
			}

			// evaluate query
			evaluateQuery(sparqlQuery);
			long evalDuration = System.currentTimeMillis() - evalStart;
			this.metricTracer.appendEval(
					"markLogicRepository [" + testName + "] evaluating query lasted " + evalDuration + " ms");
			this.metricTracer.appendEval(NEW_LINE);

			this.excelTracer.traceToTable(tableName, testName, evalDuration);
			return evalDuration;

		} catch (QueryEvaluationException e) {
			if (!e.getMessage().contains("Server Message: XDMP-UNDFUN")) {
				throw e;
			}

			// warmup
			for (int i = 0; i < warmupCount; i++) {
				evaluateOnMarklogicSemanticApi(sparqlQueryStr);
			}

			evaluateOnMarklogicSemanticApi(sparqlQueryStr);
			long evalDuration = System.currentTimeMillis() - evalStart;
			this.metricTracer.appendEval(
					"markLogicRepository [" + testName + "] evaluating query lasted " + evalDuration + " ms");
			this.metricTracer.appendEval(NEW_LINE);

			this.excelTracer.traceToTable(tableName, testName, evalDuration);
			return evalDuration;
		}
	}

	private long marklogicApproach2(String testName, String tableName, int warmupCount) throws Exception {
		// init
		long initStart = System.currentTimeMillis();
		initializeMarkLogicRepository();
		long initDuration = System.currentTimeMillis() - initStart;
		this.metricTracer
				.appendInit("markLogicRepository [" + testName + "] initialization lasted " + initDuration + " ms");
		this.metricTracer.appendInit(NEW_LINE);
		// this.excelTracer.traceToTable(tableName, testName, evalDuration);

		// load data
		long tripletCounter = countTriplets();
		if (tripletCounter == 0) {
			long loadStart = System.currentTimeMillis();
			loadDataFromFiles();
			long loadDuration = System.currentTimeMillis() - loadStart;
			this.metricTracer
					.appendLoad("markLogicRepository [" + testName + "] Loading data lasted " + loadDuration + " ms");
			this.metricTracer.appendLoad(NEW_LINE);
			// this.excelTracer.traceToTable(tableName, testName, evalDuration);
		}

		// warmup
		String javascriptQueryStr = getJavascriptTestQueryString(testName);
		for (int i = 0; i < warmupCount; i++) {
			evaluateJavascriptQuery(javascriptQueryStr);
		}

		// evaluate query
		long evalStart = System.currentTimeMillis();
		evaluateJavascriptQuery(javascriptQueryStr);
		long evalDuration = System.currentTimeMillis() - evalStart;
		this.metricTracer
				.appendEval("markLogicRepository [" + testName + "] evaluating query lasted " + evalDuration + " ms");
		this.metricTracer.appendEval(NEW_LINE);

		this.excelTracer.traceToTable(tableName, testName, evalDuration);
		return evalDuration;
	}

	private void deleteAllNative() {
		File folder = new File(TEST_FOLDER + "/rdf4j-db");
		deleteDirectory(folder);
		logger.info("deleteAllNative finished");
	}

	boolean deleteDirectory(File directoryToBeDeleted) {
		// logger.debug("Entering deleteDirectory for: " + directoryToBeDeleted);
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		// logger.debug("Exiting deleteDirectory for: " + directoryToBeDeleted);
		return directoryToBeDeleted.delete();
	}

	private void deleteAllMarkLogic() {
		SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");
		DatabaseClient client = DatabaseClientFactory.newClient(MARKLOGIC_HOST, MARKLOGIC_PORT, MARKLOGIC_DB_NAME,
				securityContext);

		String deleteQuery = "for $doc in doc() return xdmp:document-delete(xdmp:node-uri($doc))";
		client.newServerEval().xquery(deleteQuery).eval();
		client.release();
		logger.info("deleteAllMarkLogic finished");
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
		Object result = this.geoSparqlEvaluator.evaluateQuery(queryStr, true);
		long duration = System.currentTimeMillis() - start;
		this.metricTracer
				.appendMarkLogic("evaluateOnMarklogicSemanticApi -> evaluating query lasted " + duration + " ms");
		this.metricTracer.appendMarkLogic(NEW_LINE);

		return result;
	}

	private Object evaluateJavascriptQuery(String queryStr) throws Exception {
		long start = System.currentTimeMillis();

		DatabaseClient client = createDbClient();
		EvalResultIterator iterator = client.newServerEval().javascript(queryStr).eval();
		client.release();
		long durationWithoutHandle = System.currentTimeMillis() - start;
		this.metricTracer.appendMarkLogic(
				"evaluateJavascriptQuery -> evaluating query without handle lasted " + durationWithoutHandle + " ms");
		this.metricTracer.appendMarkLogic(NEW_LINE);

		// Object result = handleEvalResult(iterator);

		long durationWithHandle = System.currentTimeMillis() - start;
		this.metricTracer.appendMarkLogic(
				"evaluateJavascriptQuery -> evaluating query with handle lasted " + durationWithHandle + " ms");
		this.metricTracer.appendMarkLogic(NEW_LINE);

		return iterator;
	}

	// #region JAVA API helpers
	private DatabaseClient createDbClient() {
		SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");
		return DatabaseClientFactory.newClient(MARKLOGIC_HOST, MARKLOGIC_PORT, MARKLOGIC_DB_NAME, securityContext);
	}

	// ponovo razmotriti da li je ovo prepakivanje neophodno - Moze li se do fronta
	// proslediti json... tj premeriti pazljivo koliko sta traje
	private Object handleEvalResult(EvalResultIterator iterator) throws Exception {
		Object result;

		long booleanStart = System.currentTimeMillis();
		result = tryHandleAsBooleanResult(iterator);
		long booleanDuration = System.currentTimeMillis() - booleanStart;
		this.metricTracer
				.appendMarkLogic("handleEvalResult -> handling boolean query result lasted " + booleanDuration + " ms");
		this.metricTracer.appendMarkLogic(NEW_LINE);
		if (result != null) {
			return result;
		}

		long tupleStart = System.currentTimeMillis();
		result = tryHandleAsTupleQueryResult(iterator);
		long tupleDuration = System.currentTimeMillis() - tupleStart;
		this.metricTracer
				.appendMarkLogic("handleEvalResult -> handling tuple query result lasted " + tupleDuration + " ms");
		this.metricTracer.appendMarkLogic(NEW_LINE);
		if (result != null) {
			return result;
		}

		throw new Exception("Unknown result type.");
	}

	private Boolean tryHandleAsBooleanResult(EvalResultIterator iterator) {
		if (iterator.hasNext()) {
			String resultStr = iterator.next().getAs(String.class);

			if ("true".equals(resultStr)) {
				return true;
			}

			if ("false".equals(resultStr)) {
				return false;
			}
		}
		return null;
	}

	private TupleQueryResult tryHandleAsTupleQueryResult(EvalResultIterator iterator) throws JSONException {
		Boolean bindingNamesInitialized = false;
		List<String> bindingNames = new ArrayList<String>();
		ValidatingValueFactory valueFactory = new ValidatingValueFactory();
		TupleQueryResultBuilder builder = new TupleQueryResultBuilder();

		while (iterator.hasNext()) {
			String jsonStr = iterator.next().getAs(String.class);
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(jsonStr);

			} catch (JSONException ex) {
				// try {// new JSONArray(jsonStr);// } catch (JSONException ex)...
				logger.error("Not valid JSON string in tuple query result.", ex);
				return null;
			}

			Iterator<?> keys = jsonObj.keys();
			List<Value> values = new ArrayList<Value>();

			while(keys.hasNext()) {
				String name = (String)keys.next();
				String valueStr = jsonObj.getString(name);

				try {
					values.add(valueFactory.createIRI(valueStr));

				} catch (IllegalArgumentException e) {
					// maybe check if value str is a valid IRI?
					values.add(valueFactory.createLiteral(valueStr));
					// logger.debug("Not a valid IRI but literal.");
				}

				if (!bindingNamesInitialized) {
					bindingNames.add(name);
				}
			}

			if (!bindingNamesInitialized) {
				bindingNamesInitialized = true;
				builder.startQueryResult(bindingNames);
			}
			builder.handleSolution(new ListBindingSet(bindingNames, values));
		}

		// handling the empty result...
		if (!bindingNamesInitialized) {
			builder.startQueryResult(new ArrayList<String>());
		}
		builder.endQueryResult();
		return builder.getQueryResult();
	}
	// #endregion JAVA API helpers

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
		initSparqlQueryMap();
		initJavaScriptQueryMap();
	}

	private void initSparqlQueryMap() {
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
			this.sparqlQueryMap.put(subFolder.getName().trim(), queries);
		}
	}

	private void initJavaScriptQueryMap() {
		File sparqlQueriesFolder = new File(TEST_FOLDER + "/queries/javascript");
		if (sparqlQueriesFolder.exists() == false) {
			logger.error("Simulator folder not found. Path: " + sparqlQueriesFolder.getPath());
			return;
		}

		for (File subFolder : sparqlQueriesFolder.listFiles()) {
			Map<String, JavaScriptQuery> queries = new HashMap<String, JavaScriptQuery>();

			for (JavaScriptQuery query : this.queryDictionary.getJavascriptQueries(subFolder.getPath())) {
				queries.put(query.getTitle().trim(), query);
			}
			this.javascriptQueryMap.put(subFolder.getName().trim(), queries);
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
		File rdfFolder = new File(TEST_FOLDER + "/rdf-data/" + this.cimxmlFolder);
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
	private String getSparqlTestQueryString(String queryName) {
		return this.sparqlQueryMap.get(this.queryFolder).get(queryName).getSparql();
	}

	private String getJavascriptTestQueryString(String queryName) {
		return this.javascriptQueryMap.get(this.queryFolder).get(queryName).getJavaScript();
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
			BindingSet set = result.next();
			for (String name : set.getBindingNames()) {
				Binding binding = set.getBinding(name);
				Value value = binding.getValue();
				metricTracer.appendResults("Name: " + name + "| Value: " + value.stringValue() + NEW_LINE);
			}

			counter++;
		}

		metricTracer.appendResults("Total count: " + counter + NEW_LINE + NEW_LINE);
		return counter;
	}

	// #endregion Count Methods

	public void excel() throws java.io.IOException {
		// If using Professional version, put your serial key below.
		SpreadsheetInfo.setLicense("FREE-LIMITED-KEY");

		ExcelFile workbook = new ExcelFile();
		ExcelWorksheet worksheet = workbook.addWorksheet("Tables");

		// Add some data.
		Object[][] data = { { "Worker", "Hours", "Price" }, { "John Doe", 25, 35.0 }, { "Jane Doe", 27, 35.0 },
				{ "Jack White", 18, 32.0 }, { "George Black", 31, 35.0 } };

		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 3; j++)
				worksheet.getCell(i, j).setValue(data[i][j]);

		// Set column widths.
		worksheet.getColumn(0).setWidth(100, LengthUnit.PIXEL);
		worksheet.getColumn(1).setWidth(70, LengthUnit.PIXEL);
		worksheet.getColumn(2).setWidth(70, LengthUnit.PIXEL);
		worksheet.getColumn(3).setWidth(70, LengthUnit.PIXEL);
		worksheet.getColumn(2).getStyle().setNumberFormat("\"$\"#,##0.00");
		worksheet.getColumn(3).getStyle().setNumberFormat("\"$\"#,##0.00");

		// Create table and enable totals row.
		Table table = worksheet.addTable("Table1", "A1:C5", true);
		table.setHasTotalsRow(true);

		// Add new column.
		TableColumn column = table.addColumn();
		column.setName("Total");

		// Populate column.
		for (ExcelCell cell : column.getDataRange())
			cell.setFormula("=Table1[Hours] * Table1[Price]");

		// Set totals row function for newly added column and calculate it.
		column.setTotalsRowFunction(TotalsRowFunction.SUM);
		column.getRange().calculate();

		// Set table style.
		table.setBuiltInStyle(BuiltInTableStyleName.TABLE_STYLE_MEDIUM_2);

		workbook.save("test_folder/results/TestData.xlsx");
	}
}

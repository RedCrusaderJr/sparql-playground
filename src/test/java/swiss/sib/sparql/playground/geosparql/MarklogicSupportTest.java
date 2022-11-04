package swiss.sib.sparql.playground.geosparql;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.domain.SparqlQueryType;
import swiss.sib.sparql.playground.geosparql.marklogic.SparqlEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator.JavaClientEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator.NodeJsClientEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator.RestClientEvaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class MarklogicSupportTest {
	private static final Log logger = LogFactory.getLog(MarklogicSupportTest.class);

	private static List<String> supportedFunctions;
	private static Map<String, String> geosparqlTestQueries;
	private static Map<String, String> marklogicTestQueries;
	private static Repository inMemoryRepo;
	private static Repository marklogicRepo;

	@BeforeAll
	public static void beforeAll() {
		supportedFunctions = new ArrayList<String>();
		geosparqlTestQueries = new HashMap<String, String>();
		marklogicTestQueries = new HashMap<String, String>();

		String intersectionFunction = "http://www.opengis.net/def/function/geosparql/intersection";
		supportedFunctions.add(intersectionFunction);
		geosparqlTestQueries.put(intersectionFunction,
				createQueryWithGeosparqlBinaryFunctionCall(intersectionFunction));
		marklogicTestQueries.put(intersectionFunction, createJSQueryWithBinaryFunctionCall(intersectionFunction));

		inMemoryRepo = new SailRepository(new MemoryStore());
		inMemoryRepo.init();

		String host = Application.getMarklogicHost();
		Integer port = Application.getMarklogicPort();
		SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");
		marklogicRepo = new com.marklogic.semantics.rdf4j.MarkLogicRepository(host, port, securityContext);
		marklogicRepo.init();
	}

	@AfterAll
	public static void afterAll() {
		supportedFunctions = null;
		geosparqlTestQueries = null;
		marklogicTestQueries = null;
		inMemoryRepo.shutDown();
		marklogicRepo.shutDown();
	}

	@Test
	@Disabled
	public void evaluateQueryTest() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT *").append(System.lineSeparator());
		sb.append("WHERE {").append(System.lineSeparator());
		sb.append("  ?s ?p ?o.").append(System.lineSeparator());
		sb.append("}").append(System.lineSeparator());
		sb.append("LIMIT 10");

		SparqlEvaluator ms = SparqlEvaluator.getInstance();
		Object result = ms.evaluateQuery(sb.toString());
		Assertions.assertEquals(true, result instanceof TupleQueryResult);
		TupleQueryResult tupleQueryResult = (TupleQueryResult) result;

		int count = 1;
		while (tupleQueryResult.hasNext()) {
			count++;
			tupleQueryResult.next();
		}
		tupleQueryResult.close();

		Assertions.assertEquals(10, count);
	}

	@Test
	public void parseHttResponseTest() {
		StringBuilder sb = new StringBuilder();
		sb.append("Content-Type: application/jsonX-Primitive: ");
		sb.append("map{");
		sb.append("\"s\":\"http://iec.ch/TC57/CIM-generic#PerLengthSequenceImpedance.g0ch\",");
		sb.append("\"p\":\"http://www.w3.org/2000/01/rdf-schema#label\",");
		sb.append("\"o\":\"g0ch\"");
		sb.append("}--");

		Pattern bindingsPattern = Pattern.compile("map\\{(?<bindings>.*)\\}");
		Pattern nameValuePattern = Pattern.compile("\"(?<bindingName>.*)\":\"(?<value>.*)\"");

		Matcher bindingsMatcher = bindingsPattern.matcher(sb.toString());
		Assertions.assertTrue(bindingsMatcher.find());

		String[] nameValuePairs = bindingsMatcher.group("bindings").split(",");

		for (String nameValuePair : nameValuePairs) {
			Matcher nameValueMatcher = nameValuePattern.matcher(nameValuePair);
			Assertions.assertTrue(nameValueMatcher.find());
		}
	}

	@Test
	public void evaluateGeospatialOnInMemoryRepoTest() {
		logger.debug(String.format("Test: evaluateGeospatialInMemoryRepoTest"));
		RepositoryConnection connection = inMemoryRepo.getConnection();

		for (String functionUri : supportedFunctions) {
			String sparqlQuery = geosparqlTestQueries.get(functionUri);
			logger.debug(String.format("Sparql query:%s%s", System.lineSeparator(), sparqlQuery));

			Query query = connection.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);
			printoutQuery(query);
		}
		logger.debug(String.format("END of Test: evaluateGeospatialInMemoryRepoTest" + System.lineSeparator()));
	}

	@Test
	public void evaluateGeospatialOnMarklogicRepoTest() throws Exception {
		logger.debug(String.format("Test: evaluateGeospatialMarklogicRepoTest"));
		SparqlEvaluator ms = SparqlEvaluator.getInstance();

		for (String functionUri : supportedFunctions) {
			String sparqlQuery = geosparqlTestQueries.get(functionUri);
			logger.debug(String.format("Sparql query:%s%s", System.lineSeparator(), sparqlQuery));

			Object result = ms.evaluateQuery(sparqlQuery);
			Assertions.assertEquals(true, result instanceof TupleQueryResult);
			printoutTupleQueryResult((TupleQueryResult) result);
		}
		logger.debug(String.format("END of Test: evaluateGeospatialMarklogicRepoTest" + System.lineSeparator()));
	}

	@Test
	public void evaluateGeospatialOnMarklogicConsoleTest() {
		logger.debug(String.format("Test: evaluateGeospatialMarklogicConsoleTest"));

		for (String functionUri : supportedFunctions) {
			String sparqlQuery = geosparqlTestQueries.get(functionUri);
			logger.debug(String.format("Sparql query:%s%s", System.lineSeparator(), sparqlQuery));

			// MANUAL TEST
			logger.debug("fails because of geosparql function call, AS EXPECTED");
		}
		logger.debug(String.format("END of Test: evaluateGeospatialMarklogicConsoleTest" + System.lineSeparator()));
	}

	@Test
	public void evaluateJsGeospatialOnMarklogicRestApiTest() throws IOException {
		logger.debug(String.format("Test: evaluateJsGeospatialMarklogicRestApiTest"));
		RestClientEvaluator restApi = new RestClientEvaluator();

		for (String functionUri : supportedFunctions) {
			String jsQuery = marklogicTestQueries.get(functionUri);
			logger.debug(String.format("JS query:%s%s", System.lineSeparator(), jsQuery));

			TupleQueryResult result = restApi.evaluateJavaScript(jsQuery, false);
			printoutTupleQueryResult(result);
		}
		logger.debug(String.format("END of Test: evaluateJsGeospatialMarklogicRestApiTest" + System.lineSeparator()));
	}

	@Test
	public void evaluateJsGeospatialOnMarklogicJavaApi() throws Exception {
		logger.debug(String.format("Test: evaluateJsGeospatialOnMarklogicJavaApi"));
		JavaClientEvaluator javaApi = new JavaClientEvaluator();

		for (String functionUri : supportedFunctions) {
			String jsQuery = marklogicTestQueries.get(functionUri);
			logger.debug(String.format("JS query:%s%s", System.lineSeparator(), jsQuery));

			Object result = javaApi.evaluateJavaScript(jsQuery, false);
			Assertions.assertEquals(true, result instanceof TupleQueryResult);
			printoutTupleQueryResult((TupleQueryResult) result);
		}
		logger.debug(String.format("END of Test: evaluateJsGeospatialOnMarklogicJavaApi" + System.lineSeparator()));
	}

	@Test
	@Disabled
	public void evaluateJsGeospatialOnMarklogicNodeJsApi() throws Exception {
		logger.debug(String.format("Test: evaluateJsGeospatialOnMarklogicNodeJsApi"));
		NodeJsClientEvaluator nodeJsApi = new NodeJsClientEvaluator();

		for (String functionUri : supportedFunctions) {
			String jsQuery = marklogicTestQueries.get(functionUri);
			logger.debug(String.format("JS query:%s%s", System.lineSeparator(), jsQuery));

			Object result = nodeJsApi.evaluateJavaScript(jsQuery, false);
			Assertions.assertEquals(true, result instanceof TupleQueryResult);
			printoutTupleQueryResult((TupleQueryResult) result);
		}
		logger.debug(String.format("END of Test: evaluateJsGeospatialOnMarklogicNodeJsApi" + System.lineSeparator()));
	}

	@Test
	public void evaluateJsGeospatialOnMarklogicConsoleTest() {
		logger.debug(String.format("Test: evaluateJsGeospatialMarklogicConsoleTest"));

		for (String functionUri : supportedFunctions) {
			String jsQuery = marklogicTestQueries.get(functionUri);
			logger.debug(String.format("JS query:%s%s", System.lineSeparator(), jsQuery));

			// MANUAL TEST
			// SUCCESS - 3 bounded values
		}
		logger.debug(String.format("END of Test: evaluateJsGeospatialMarklogicConsoleTest" + System.lineSeparator()));
	}

	@Test
	public void evaluateJsGeospatialOnMarklogic3rdPartyTest() {
		logger.debug(String.format("Test: evaluateJsGeospatialMarklogic3rdPartyTest"));

		for (String functionUri : supportedFunctions) {
			String jsQuery = marklogicTestQueries.get(functionUri);
			logger.debug(String.format("JS query:%s%s", System.lineSeparator(), jsQuery));

			// MANUAL TEST
			// exapmle: postman, curl etc.
			// POSTMAN: success with 3 bindings
		}
		logger.debug(String.format("END of Test: evaluateJsGeospatialMarklogic3rdPartyTest" + System.lineSeparator()));
	}

	@Test
	public void evaluateJsGeospatialWithSelectSubqueryOnMarklogicJavaApiTest() throws Exception {
		logger.debug(String.format("Test: evaluateJsGeospatialWithSelectSubqueryOnMarklogicJavaApiTest"));
		JavaClientEvaluator javaApi = new JavaClientEvaluator();

		for (String functionUri : supportedFunctions) {
			//String jsQuery = marklogicTestQueries.get(functionUri);
			String jsQuery = createQueryWithGeosparqlBinaryFunctionCallAndSelectSubquery(functionUri);
			logger.debug(String.format("JS query:%s%s", System.lineSeparator(), jsQuery));

			Object result = javaApi.evaluateJavaScript(jsQuery, false);
			Assertions.assertEquals(true, result instanceof TupleQueryResult);
			printoutTupleQueryResult((TupleQueryResult) result);
		}
		logger.debug(String.format("END of Test: evaluateJsGeospatialWithSelectSubqueryOnMarklogicJavaApiTest" + System.lineSeparator()));
	}

	private static String createQueryWithGeosparqlBinaryFunctionCallAndSelectSubquery(String function) {
		String bindStr = String.format("  BIND(<%s>(?wktPoint1, ?wktPoint2) as ?functionResult)", function);

		StringBuilder sb = new StringBuilder();
		sb.append("PREFIX geo:<http://www.opengis.net/ont/geosparql#>").append(System.lineSeparator());
		sb.append("SELECT DISTINCT ?functionResult").append(System.lineSeparator());
		sb.append("WHERE {").append(System.lineSeparator());
		sb.append("  {").append(System.lineSeparator());
		sb.append("    SELECT DISTINCT ?wktPoint1 ?wktPoint2").append(System.lineSeparator());
		sb.append("    WHERE {").append(System.lineSeparator());
		sb.append("      BIND(\"POINT (1 1)\"^^geo:wktLiteral as ?wktPoint1)").append(System.lineSeparator());
		sb.append("      BIND(\"POINT (2 2)\"^^geo:wktLiteral as ?wktPoint2)").append(System.lineSeparator());
		sb.append("    }").append(System.lineSeparator());
		sb.append("  }").append(System.lineSeparator());
		sb.append(bindStr).append(System.lineSeparator());
		sb.append("}").append(System.lineSeparator());
		return sb.toString();
	}

	// MAYBE one day
	// private static String createQueryWithGeosparqlUnaryFunctionCall(String
	// function) {
	// String bindStr = String.format(" BIND(<%s>(?wktPoint1) as ?functionResult)",
	// function);

	// StringBuilder sb = new StringBuilder();
	// sb.append("PREFIX
	// geo:<http://www.opengis.net/ont/geosparql#>").append(System.lineSeparator());
	// sb.append("SELECT ?wktPoint1
	// ?functionResult").append(System.lineSeparator());
	// sb.append("WHERE {").append(System.lineSeparator());
	// sb.append(" BIND(\"POINT (1 1)\"^^geo:wktLiteral as
	// ?wktPoint1)").append(System.lineSeparator());
	// sb.append(bindStr).append(System.lineSeparator());
	// sb.append("}").append(System.lineSeparator());
	// return sb.toString();
	// }

	private static String createQueryWithGeosparqlBinaryFunctionCall(String function) {
		String bindStr = String.format("  BIND(<%s>(?wktPoint1, ?wktPoint2) as ?functionResult)", function);

		StringBuilder sb = new StringBuilder();
		sb.append("PREFIX geo:<http://www.opengis.net/ont/geosparql#>").append(System.lineSeparator());
		sb.append("SELECT ?wktPoint1 ?wktPoint2 ?functionResult").append(System.lineSeparator());
		sb.append("WHERE {").append(System.lineSeparator());
		sb.append("  BIND(\"POINT (1 1)\"^^geo:wktLiteral as ?wktPoint1)").append(System.lineSeparator());
		sb.append("  BIND(\"POINT (2 2)\"^^geo:wktLiteral as ?wktPoint2)").append(System.lineSeparator());
		sb.append(bindStr).append(System.lineSeparator());
		sb.append("}").append(System.lineSeparator());
		return sb.toString();
	}

	// MAYBE one day
	// private static String createJSQueryWithUnaryFunctionCall(String functionUri)
	// {
	// Set<String> supportedFunctions =
	// FunctionMapper.getInstance().getAllSupportedFunctionByUri();
	// if (!supportedFunctions.contains(functionUri)) {
	// throw new IllegalArgumentException();
	// }

	// FunctionDescription function =
	// FunctionMapper.getInstance().getFunctionByUri(functionUri);
	// String functionAbbrv = function.abbreviation;
	// String marklogicFunction = function.marklogicFunction;
	// String functionCallStr = String.format(" BIND(xdmp:apply(?%s, ?wktPoint1) as
	// ?functionResult).",
	// functionAbbrv);
	// String paramsStr = String.format("var params = {%s: %s}", functionAbbrv,
	// marklogicFunction);

	// StringBuilder sb = new StringBuilder();
	// sb.append("declareUpdate();").append(System.lineSeparator());
	// sb.append("var sem =
	// require('/MarkLogic/semantics.xqy');").append(System.lineSeparator());
	// sb.append("var query = `").append(System.lineSeparator());
	// sb.append("PREFIX
	// geo:<http://www.opengis.net/ont/geosparql#>").append(System.lineSeparator());
	// sb.append("PREFIX
	// xdmp:<http://marklogic.com/xdmp#>").append(System.lineSeparator());
	// sb.append("SELECT DISTINCT ?wktPoint1
	// ?functionResult").append(System.lineSeparator());
	// sb.append("WHERE {").append(System.lineSeparator());
	// sb.append(" BIND(\"POINT (1 1)\"^^geo:wktLiteral as
	// ?wktPoint1)").append(System.lineSeparator());
	// sb.append(functionCallStr).append(System.lineSeparator());
	// sb.append("}`").append(System.lineSeparator());
	// sb.append(paramsStr).append(System.lineSeparator());
	// sb.append("var results =
	// sem.sparql(query,params);").append(System.lineSeparator());
	// sb.append("results").append(System.lineSeparator());
	// return sb.toString();
	// }

	private static String createJSQueryWithBinaryFunctionCall(String functionUri) {
		Set<String> supportedFunctions = FunctionMapper.getInstance().getAllSupportedFunctionByUri();
		if (!supportedFunctions.contains(functionUri)) {
			throw new IllegalArgumentException();
		}

		FunctionDescription function = FunctionMapper.getInstance().getFunctionByUri(functionUri);
		String functionAbbrv = function.abbreviation;
		String marklogicFunction = function.marklogicFunction;
		String functionCallStr = String.format("  BIND(xdmp:apply(?%s, ?wktPoint1, ?wktPoint2) as ?functionResult).",
				functionAbbrv);
		String paramsStr = String.format("var params = {%s: %s}", functionAbbrv, marklogicFunction);

		StringBuilder sb = new StringBuilder();
		sb.append("declareUpdate();").append(System.lineSeparator());
		sb.append("var sem = require('/MarkLogic/semantics.xqy');").append(System.lineSeparator());
		sb.append("var query = `").append(System.lineSeparator());
		sb.append("PREFIX geo:<http://www.opengis.net/ont/geosparql#>").append(System.lineSeparator());
		sb.append("PREFIX xdmp:<http://marklogic.com/xdmp#>").append(System.lineSeparator());
		sb.append("SELECT DISTINCT ?wktPoint1 ?wktPoint2 ?functionResult").append(System.lineSeparator());
		sb.append("WHERE {").append(System.lineSeparator());
		sb.append("  BIND(\"POINT (1 1)\"^^geo:wktLiteral as ?wktPoint1)").append(System.lineSeparator());
		sb.append("  BIND(\"POINT (2 2)\"^^geo:wktLiteral as ?wktPoint2)").append(System.lineSeparator());
		sb.append(functionCallStr).append(System.lineSeparator());
		sb.append("}`").append(System.lineSeparator());
		sb.append(paramsStr).append(System.lineSeparator());
		sb.append("var results = sem.sparql(query,params);").append(System.lineSeparator());
		sb.append("results").append(System.lineSeparator());
		return sb.toString();
	}

	private void printoutQuery(Query query) {
		SparqlQueryType queryType = SparqlQueryType.getQueryType(query);
		if (SparqlQueryType.TUPLE_QUERY == queryType) {
			TupleQueryResult result = ((TupleQuery) query).evaluate();
			printoutTupleQueryResult(result);

		} else {
			logger.debug(String.format("[WARN] Unexpected type of query output: %s", queryType));
		}
	}

	private void printoutTupleQueryResult(TupleQueryResult result) {
		List<String> bindingNames = result.getBindingNames();
		Integer bindingNamesSize = bindingNames.size();
		logger.debug(String.format("Binding names count: %d", bindingNamesSize));

		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			for (String bindingName : bindingNames) {
				Binding binding = bindingSet.getBinding(bindingName);
				String name = binding.getName();
				String value = binding.getValue().stringValue();
				logger.debug(String.format("[Binding set] name: %s, value: %s", name, value));
			}
		}
	}
}

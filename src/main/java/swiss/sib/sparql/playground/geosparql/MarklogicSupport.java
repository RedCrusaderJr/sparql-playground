package swiss.sib.sparql.playground.geosparql;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.eclipse.rdf4j.query.impl.TupleQueryResultBuilder;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;
import org.json.JSONException;
import org.json.JSONObject;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.client.eval.EvalResultIterator;

import swiss.sib.sparql.playground.Application;

public class MarklogicSupport {
	private static final Log logger = LogFactory.getLog(MarklogicSupport.class);

	// #region Instance
	private static MarklogicSupport instance;

	public static MarklogicSupport getInstance() {
		if (instance == null) {
			synchronized (MarklogicSupport.class) {
				if (instance == null) {
					instance = new MarklogicSupport();
				}
			}
		}

		return instance;
	}

	private MarklogicSupport() {
	}
	// #endregion Instance

	private final Pattern bindingsPattern = Pattern.compile("map\\{(?<bindings>.*)\\}");
	private final Pattern nameStrValuePattern = Pattern.compile("\"(?<bindingName>.*)\":\"(?<value>.*)\"");
	private final Pattern nameSimpleValuePattern = Pattern.compile("\"(?<bindingName>.*)\":(?<value>.*)");
	private final Pattern xdmpApplyArgsPattern = Pattern
			.compile("<http:\\/\\/marklogic\\.com\\/xdmp#apply>\\((?<args>.*)\\)");
	private final Pattern literalPattern = Pattern
			.compile("(?<literalLong>\"\"\"(?<literalValue>.*)\"\"\"\\^\\^\\<.*\\>)");

	public TupleQueryResult evaluateQuery(String sparqlQuery) throws Exception {
		try {
			String alternatedSparqlQuery = alternateSparqlQuery(sparqlQuery);

			List<String> abbreviations = extractFunctionAbbreviationsFromQuery(alternatedSparqlQuery);
			String importFunctionsStr = createImportFunctionsStr(abbreviations);
			String params = createXdmpApplyParams(abbreviations);

			String jsQuery = createJSQuery(alternatedSparqlQuery, importFunctionsStr, params);
			return evaluateOnJavaApi(jsQuery);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	// #region javascript query helpers
	private String alternateSparqlQuery(String sparqlQuery) throws Exception {
		GeosparqlQueryModelVisitor visitor = new GeosparqlQueryModelVisitor(FunctionMapper.getInstance());

		ParsedQuery parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, sparqlQuery, null);
		parsedQuery.getTupleExpr().visit(visitor);

		SPARQLQueryRenderer renderer = new SPARQLQueryRenderer();
		String renderedStr = renderer.render(parsedQuery);
		String finalQueryStr = renderCorrection(renderedStr);
		return finalQueryStr;
	}

	// replacing: """value"""^^<datatype> from xdmp:apply aguments
	// with: 'value' - js can't cope with """literal""" notation
	private String renderCorrection(String queryStr) {
		Matcher xdmpApplyArgsMatcher = xdmpApplyArgsPattern.matcher(queryStr);
		if (!xdmpApplyArgsMatcher.find()) {
			return queryStr;
		}

		String args = xdmpApplyArgsMatcher.group("args");
		String[] argsArray = args.split(", ");
		for (String arg : argsArray) {
			Matcher literalMatcher = literalPattern.matcher(arg);

			if (!literalMatcher.find()) {
				continue;
			}

			String literalLong = literalMatcher.group("literalLong");
			String literalValue = literalMatcher.group("literalValue");
			queryStr = queryStr.replace(literalLong, "'" + literalValue + "'");
		}

		return queryStr;
	}

	private List<String> extractFunctionAbbreviationsFromQuery(String queryStr) {
		List<String> functionAbrvs = new ArrayList<String>();
		FunctionMapper mapper = FunctionMapper.getInstance();

		for (String functionAbrv : mapper.getAllSupportedFunctionByAbbreviations()) {
			if (!queryStr.contains(functionAbrv)) {
				continue;
			}
			functionAbrvs.add(functionAbrv);
		}
		return functionAbrvs;
	}

	private String createImportFunctionsStr(List<String> functionAbrvs) {
		StringBuilder sb = new StringBuilder();
		FunctionMapper mapper = FunctionMapper.getInstance();
		String newLine = System.getProperty("line.separator");

		for (String abrv : functionAbrvs) {
			FunctionDescription function = mapper.getFunctionByAbbreviation(abrv);
			if (!(function instanceof CustomFunction)) {
				continue;
			}

			String mlName = function.marklogicFunction;
			String modulePath = ((CustomFunction) function).modulePath;
			sb.append("import { ").append(mlName).append(" } from  '").append(modulePath).append("';");
			sb.append(newLine);
		}

		return sb.toString();
	}

	private String createXdmpApplyParams(List<String> functionAbrvs) {
		StringBuilder sb = new StringBuilder();
		FunctionMapper mapper = FunctionMapper.getInstance();
		Boolean isFirst = true;

		for (String abrv : functionAbrvs) {
			String separator = ", ";
			if (isFirst) {
				separator = "";
				isFirst = false;
			}

			sb.append(separator);
			sb.append(abrv).append(": ");
			sb.append(mapper.getFunctionByAbbreviation(abrv).marklogicFunction);
		}

		return sb.toString();
	}

	private String createJSQuery(String sparqlQuery, String importFunctionsStr, String params) {
		StringBuilder sb = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		sb.append("var sem = require('/MarkLogic/semantics.xqy');").append(newLine);
		sb.append(importFunctionsStr).append(newLine);
		sb.append("var query = `" + sparqlQuery + "`;").append(newLine);
		sb.append("var params = {" + params + "}").append(newLine);
		sb.append("var results = sem.sparql(query,params);").append(newLine);
		sb.append("results");

		return sb.toString();
	}
	// #endregion Create Javascript query

	public TupleQueryResult evaluateOnRestApi(String jsQuery) throws IOException {
		try {
			String boundaryStr = "BOUNDARY";
			String params = String.format("javascript=%s", jsQuery);
			byte[] postData = params.getBytes(StandardCharsets.UTF_8);
			int postDataLength = postData.length;

			HttpURLConnection connection = createConnection(jsQuery, postDataLength, boundaryStr);
			String responseData = sendRequest(connection, postData);

			return parseHttpResponse(responseData, boundaryStr);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	// #region rest api helpers
	private HttpURLConnection createConnection(String jsQuery, int postDataLength, String boundaryStr)
			throws IOException {
		String host = Application.getMarklogicHost();
		Integer port = Application.getMarklogicPort();
		URL url = new URL(String.format("http://%s:%d/v1/eval", host, port));

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		connection.setRequestProperty("Accept-Charset", "utf-8");
		connection.setRequestProperty("Accept", String.format("multipart/mixed; boundary=%s", boundaryStr));
		connection.setRequestProperty("Accept-Encoding", String.format("gzip, deflate, br"));
		connection.setRequestProperty("Connection", String.format("keep-alive"));

		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setUseCaches(false);
		return connection;
	}

	private String sendRequest(HttpURLConnection connection, byte[] postData) throws IOException {
		try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
			outputStream.write(postData);
		}

		InputStreamReader inputStreamReader = null;
		int status = connection.getResponseCode();
		if (status >= 300) {
			inputStreamReader = new InputStreamReader(connection.getErrorStream());
		} else {
			inputStreamReader = new InputStreamReader(connection.getInputStream());
		}

		String inputLine;
		StringBuffer responseData = new StringBuffer();
		BufferedReader reader = new BufferedReader(inputStreamReader);
		while ((inputLine = reader.readLine()) != null) {
			responseData.append(inputLine);
		}
		reader.close();
		connection.disconnect();

		return responseData.toString();
	}

	// TODO: can it be optimized?
	private TupleQueryResult parseHttpResponse(String responseData, String boundaryStr) {
		TupleQueryResultBuilder builder = new TupleQueryResultBuilder();

		Boolean bindingNamesInitialized = false;
		List<String> bindingNames = new ArrayList<String>();
		ValidatingValueFactory valueFactory = new ValidatingValueFactory();

		String[] boundedParts = responseData.split(boundaryStr);

		// for each triple in the result (could have many iterations)
		for (String boundedPart : boundedParts) {
			Matcher bindingsMatcher = bindingsPattern.matcher(boundedPart);
			if (!bindingsMatcher.find()) {
				continue;
			}

			List<Value> values = new ArrayList<Value>();
			String[] nameValuePairs = bindingsMatcher.group("bindings").split(",");

			// for each variable in select (shouldn't have many iterations)
			for (String nameValuePair : nameValuePairs) {
				Matcher nameValueMatcher = nameStrValuePattern.matcher(nameValuePair);
				if (!nameValueMatcher.find()) {
					nameValueMatcher = nameSimpleValuePattern.matcher(nameValuePair);

					if (!nameValueMatcher.find()) {
						continue;
					}
				}

				String bindingName = nameValueMatcher.group("bindingName");
				String valueStr = nameValueMatcher.group("value");

				if (!bindingNamesInitialized) {
					bindingNames.add(bindingName);
				}

				try {
					values.add(valueFactory.createIRI(valueStr));

				} catch (IllegalArgumentException e) {
					values.add(valueFactory.createLiteral(valueStr));
				}
			}

			if (!bindingNamesInitialized) {
				bindingNamesInitialized = true;
				builder.startQueryResult(bindingNames);
			}

			builder.handleSolution(new ListBindingSet(bindingNames, values));
		}

		builder.endQueryResult();
		return builder.getQueryResult();
	}
	// #endregion

	public TupleQueryResult evaluateOnJavaApi(String jsQuery) throws JSONException {
		DatabaseClient client = createDbClient();
		EvalResultIterator iterator = client.newServerEval().javascript(jsQuery).eval();
		return handleEvalResult(iterator);
	}

	// #region java api helpers
	private DatabaseClient createDbClient() {
		String host = Application.getMarklogicHost();
		Integer port = Application.getMarklogicPort();
		String dbName = Application.getMarklogicDbName();
		SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");

		return DatabaseClientFactory.newClient(host, port, dbName, securityContext);
	}

	private TupleQueryResult handleEvalResult(EvalResultIterator iterator) throws JSONException {
		Boolean bindingNamesInitialized = false;
		List<String> bindingNames = new ArrayList<String>();
		ValidatingValueFactory valueFactory = new ValidatingValueFactory();
		TupleQueryResultBuilder builder = new TupleQueryResultBuilder();

		while (iterator.hasNext()) {
			String jsonStr = iterator.next().getAs(String.class);
			JSONObject jsonObj = new JSONObject(jsonStr);

			String[] names = JSONObject.getNames(jsonObj);
			List<Value> values = new ArrayList<Value>();

			for (String name : names) {
				String valueStr = jsonObj.getString(name);

				try {
					values.add(valueFactory.createIRI(valueStr));

				} catch (IllegalArgumentException e) {
					values.add(valueFactory.createLiteral(valueStr));
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

		if (!bindingNamesInitialized) {
			builder.startQueryResult(new ArrayList<String>());
		}

		builder.endQueryResult();
		return builder.getQueryResult();
	}
	// #endregion java api helpers
}

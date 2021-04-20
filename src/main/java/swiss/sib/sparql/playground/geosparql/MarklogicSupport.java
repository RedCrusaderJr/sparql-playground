package swiss.sib.sparql.playground.geosparql;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

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

import java.util.regex.Matcher;
import swiss.sib.sparql.playground.Application;

public class MarklogicSupport {
	private static final Log logger = LogFactory.getLog(MarklogicSupport.class);

	public TupleQueryResult evaluateQuery(String sparqlQuery) throws Exception {
		try {
			String alternatedSparqlQuery = alternateSparqlQuery(sparqlQuery);
			String params = createParamsForJsQuery(alternatedSparqlQuery);

			String jsQuery = createJSQuery(alternatedSparqlQuery, params);
			return evaluateOnMarklogicRestApi(jsQuery);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private String alternateSparqlQuery(String sparqlQuery) throws Exception {
		GeosparqlQueryModelVisitor visitor = new GeosparqlQueryModelVisitor(FunctionMapper.getInstance());

		ParsedQuery parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, sparqlQuery, null);
		parsedQuery.getTupleExpr().visit(visitor);

		SPARQLQueryRenderer renderer = new SPARQLQueryRenderer();
		String queryStr = renderer.render(parsedQuery);

		return queryStr;
	}

	private String createParamsForJsQuery(String queryStr) {
		FunctionMapper mapper = FunctionMapper.getInstance();

		Boolean isFirst = true;
		StringBuilder sb = new StringBuilder();

		for (String functionAbrv : mapper.getAllSupportedFunctionByAbbreviations()) {
			if (!queryStr.contains(functionAbrv)) {
				continue;
			}

			String separator = ", ";
			if (isFirst) {
				separator = "";
				isFirst = false;
			}

			sb.append(separator);
			sb.append(functionAbrv).append(": ").append(mapper.getMarklogicFunctionByAbbreviation(functionAbrv));
		}

		return sb.toString();
	}

	private String createJSQuery(String sparqlQuery, String params) {
		StringBuilder sb = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		sb.append("declareUpdate();").append(newLine);
		sb.append("var sem = require('/MarkLogic/semantics.xqy');").append(newLine);
		sb.append("var query = `" + sparqlQuery + "`;").append(newLine);
		sb.append("var params = {" + params + "}").append(newLine);
		sb.append("var results = sem.sparql(query,params);").append(newLine);
		sb.append("results");

		return sb.toString();
	}

	private TupleQueryResult evaluateOnMarklogicRestApi(String jsQuery) throws IOException {
		String boundaryStr = "BOUNDARY";
		String params = String.format("javascript=%s", jsQuery);
		byte[] postData = params.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;

		HttpURLConnection connection = createConnection(jsQuery, postDataLength, boundaryStr);
		String responseData = sendRequest(connection, postData);
		TupleQueryResult result = parseResponse(responseData, boundaryStr);

		return result;
	}

	private HttpURLConnection createConnection(String jsQuery, int postDataLength, String boundaryStr)
			throws IOException {
		String address = Application.getMarklogicAddress();
		Integer port = Application.getMarklogicPort();
		URL url = new URL(String.format("http://%s:%d/v1/eval", address, port));

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		connection.setRequestProperty("Accept-Charset", "utf-8");
		connection.setRequestProperty("Accept", String.format("multipart/mixed; boundary=%s", boundaryStr));

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
	private TupleQueryResult parseResponse(String responseData, String boundaryStr) {
		Boolean bindingNamesInitialized = false;
		HashSet<String> bindingNames = new HashSet<String>();
		List<String> bindingNamesList = new ArrayList<String>();
		List<ListBindingSet> bindingSets = new ArrayList<ListBindingSet>();
		ValidatingValueFactory valueFactory = new ValidatingValueFactory();

		Pattern bindingsPattern = Pattern.compile("map\\{(?<bindings>.*)\\}");
		Pattern nameValuePattern = Pattern.compile("\"(?<bindingName>.*)\":\"(?<value>.*)\"");
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
				Matcher nameValueMatcher = nameValuePattern.matcher(nameValuePair);
				if (!nameValueMatcher.find()) {
					continue;
				}

				String bindingName = nameValueMatcher.group("bindingName");
				String value = nameValueMatcher.group("value");

				if (!bindingNamesInitialized && !bindingNames.contains(bindingName)) {
					bindingNames.add(bindingName);
				}

				try {
					values.add(valueFactory.createIRI(value));

				} catch (IllegalArgumentException e) {
					values.add(valueFactory.createLiteral(value));
				}
			}

			if (!bindingNamesInitialized) {
				bindingNamesInitialized = true;
				bindingNamesList = new ArrayList<String>(bindingNames);
			}

			bindingSets.add(new ListBindingSet(bindingNamesList, values));

		}

		TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
		builder.startQueryResult(bindingNamesList);
		for (ListBindingSet bindingSet : bindingSets) {
			builder.handleSolution(bindingSet);
		}
		builder.endQueryResult();

		return builder.getQueryResult();
	}
}

package swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator;

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
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.eclipse.rdf4j.query.impl.TupleQueryResultBuilder;

import swiss.sib.sparql.playground.Application;

public class RestClientEvaluator implements JavaScriptQueryEvaluator {
	private static final Log logger = LogFactory.getLog(JavaClientEvaluator.class);

	private final Pattern bindingsPattern = Pattern.compile("map\\{(?<bindings>.*)\\}");
	private final Pattern nameStrValuePattern = Pattern.compile("\"(?<bindingName>.*)\":\"(?<value>.*)\"");
	private final Pattern nameSimpleValuePattern = Pattern.compile("\"(?<bindingName>.*)\":(?<value>.*)");

	// utvrditi precizno zasto ne radi...
	public TupleQueryResult evaluateJavaScript(String jsQuery) throws IOException {
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
	// #endregion rest api helpers
	// #endregion REST CLIENT API
}

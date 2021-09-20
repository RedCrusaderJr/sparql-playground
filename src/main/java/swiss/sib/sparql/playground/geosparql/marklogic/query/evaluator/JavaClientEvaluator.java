package swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator;

import java.util.ArrayList;
import java.util.List;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.client.eval.EvalResultIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.eclipse.rdf4j.query.impl.TupleQueryResultBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import swiss.sib.sparql.playground.Application;

public class JavaClientEvaluator implements JavaScriptQueryEvaluator {
	private static final Log logger = LogFactory.getLog(JavaClientEvaluator.class);
	private static final String NEW_LINE = System.lineSeparator();

	public Object evaluateJavaScript(String jsQuery, Boolean returnRaw) throws Exception {
		DatabaseClient client = createDbClient();
		EvalResultIterator iterator = client.newServerEval().javascript(jsQuery).eval();
		client.release();

		Object result = iterator;
		if (!returnRaw) {
			result = handleEvalResult(iterator);
		}

		return result;
	}

	// #region JAVA API helpers
	private DatabaseClient createDbClient() {
		String host = Application.getMarklogicHost();
		Integer port = Application.getMarklogicPort();
		String dbName = Application.getMarklogicDbName();
		SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");

		return DatabaseClientFactory.newClient(host, port, dbName, securityContext);
	}

	// ponovo razmotriti da li je ovo prepakivanje neophodno - Moze li se do fronta
	// proslediti json... tj premeriti pazljivo koliko sta traje
	private Object handleEvalResult(EvalResultIterator iterator) throws Exception {
		Object result;

		long booleanStart = System.currentTimeMillis();
		result = tryHandleAsBooleanResult(iterator);
		double booleanDuration = ((double) (System.currentTimeMillis() - booleanStart)) / (double) 1000;
		logger.info("handleEvalResult -> handling boolean query result lasted " + booleanDuration + " sec" + NEW_LINE);
		if (result != null) {
			return result;
		}

		long tupleStart = System.currentTimeMillis();
		result = tryHandleAsTupleQueryResult(iterator);
		double tupleDuration = ((double) (System.currentTimeMillis() - tupleStart)) / (double) 1000;
		logger.info("handleEvalResult -> handling tuple query result lasted " + tupleDuration + " sec" + NEW_LINE);
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

			String[] names = JSONObject.getNames(jsonObj);
			List<Value> values = new ArrayList<Value>();

			for (String name : names) {
				String valueStr = jsonObj.getString(name);
				try {
					values.add(valueFactory.createIRI(valueStr));

				} catch (IllegalArgumentException e) {
					// maybe check if value str is a valid IRI?
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

		// handling the empty result...
		if (!bindingNamesInitialized) {
			builder.startQueryResult(new ArrayList<String>());
		}
		builder.endQueryResult();
		return builder.getQueryResult();
	}
	// #endregion JAVA API helpers
}

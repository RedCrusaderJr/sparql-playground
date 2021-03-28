package swiss.sib.sparql.playground.geosparql.functions;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FunctionFactory {
	private static final Log logger = LogFactory.getLog(FunctionFactory.class);

	private Map<String, Class<GeosparqlFunction>> functionMap;

	public FunctionFactory() {
		try {
			importFunctions();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public GeosparqlFunction createFunction(String functionName, GeosparqlFunctionParameters parameters)
			throws Exception {
		if (!functionMap.containsKey(functionName)) {
			throw new Exception("Function '" + functionName + "' not found.");
		}

		Class<GeosparqlFunction> type = functionMap.get(functionName);
		Constructor<GeosparqlFunction> constructor = type.getDeclaredConstructor();
		return constructor.newInstance(parameters);
	}

	private void importFunctions() throws ClassNotFoundException {
		String className = "swiss.sib.sparql.playground.geosparql.functions.IntersectFunction";
		functionMap = new HashMap<String, Class<GeosparqlFunction>>();

		@SuppressWarnings("unchecked")
		Class<GeosparqlFunction> value = (Class<GeosparqlFunction>) Class.forName(className);
		functionMap.put("intersect", value);
	}

}

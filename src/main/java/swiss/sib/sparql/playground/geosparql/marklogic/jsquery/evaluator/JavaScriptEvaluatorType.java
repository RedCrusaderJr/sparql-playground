package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator;

public enum JavaScriptEvaluatorType {

	JAVA_API, NODE_JS_API, REST_API;

	public static JavaScriptEvaluatorType parseType(String evaluatorTypeStr) {

		if (evaluatorTypeStr.equals("java")) {
			return JavaScriptEvaluatorType.JAVA_API;

		} else if (evaluatorTypeStr.equals("nodejs")) {
			return JavaScriptEvaluatorType.NODE_JS_API;

		} else if (evaluatorTypeStr.equals("rest")) {
			return JavaScriptEvaluatorType.REST_API;

		} else {
			return JavaScriptEvaluatorType.JAVA_API;
		}
	}
}

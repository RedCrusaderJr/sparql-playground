package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator;

public enum EvaluatorApiType {

	JAVA_API, NODE_JS_API, REST_API;

	public static EvaluatorApiType parseType(String evaluatorTypeStr) {

		if (evaluatorTypeStr.equals("java")) {
			return EvaluatorApiType.JAVA_API;

		} else if (evaluatorTypeStr.equals("nodejs")) {
			return EvaluatorApiType.NODE_JS_API;

		} else if (evaluatorTypeStr.equals("rest")) {
			return EvaluatorApiType.REST_API;

		} else {
			return EvaluatorApiType.JAVA_API;
		}
	}
}

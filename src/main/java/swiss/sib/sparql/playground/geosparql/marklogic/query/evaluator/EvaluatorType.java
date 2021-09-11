package swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator;

public enum EvaluatorType {

	JAVA_API, NODE_JS_API, REST_API;

	public static EvaluatorType parseType(String evaluatorTypeStr) {

		if (evaluatorTypeStr.equals("java")) {
			return EvaluatorType.JAVA_API;

		} else if (evaluatorTypeStr.equals("nodejs")) {
			return EvaluatorType.NODE_JS_API;

		} else if (evaluatorTypeStr.equals("rest")) {
			return EvaluatorType.REST_API;

		} else {
			return EvaluatorType.JAVA_API;
		}
	}
}

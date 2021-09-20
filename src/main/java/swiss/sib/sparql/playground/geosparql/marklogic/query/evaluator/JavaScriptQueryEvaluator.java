package swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator;

public interface JavaScriptQueryEvaluator {
	Object evaluateJavaScript(String jsQuery, Boolean returnRaw) throws Exception;
}

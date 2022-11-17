package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator;

public interface JavaScriptQueryEvaluator {
	
	Object evaluate(String jsQuery, Boolean returnRaw) throws Exception;
}

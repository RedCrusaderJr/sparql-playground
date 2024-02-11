package swiss.sib.sparql.playground.geosparql.marklogic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.geosparql.GeoSparqlEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.JavaScriptQueryCreator;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.JavaScriptQueryCreatorType;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.creator.jena.QueryCreatorJena;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.creator.rdf4j.QueryCreatorRdf4j;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.EvaluatorApiType;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.JavaScriptQueryEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.java.JavaClientEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.nodejs.NodeJsClientEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.rest.RestClientEvaluator;

@Component
public class MarklogicGeoSparqlEvaluator implements GeoSparqlEvaluator, InitializingBean {
	private static final Log logger = LogFactory.getLog(MarklogicGeoSparqlEvaluator.class);

	private JavaScriptQueryEvaluator queryEvaluator;
	private JavaScriptQueryCreator jsQueryCreator;
	
	@Override
	public void afterPropertiesSet() throws Exception {

		EvaluatorApiType evaluatorApiType = Application.getMarklogicEvaluatorApiType();
		if (evaluatorApiType == EvaluatorApiType.JAVA_API) {
			this.queryEvaluator = new JavaClientEvaluator();

		} else if (evaluatorApiType == EvaluatorApiType.REST_API) {
			this.queryEvaluator = new RestClientEvaluator();

		} else if (evaluatorApiType == EvaluatorApiType.NODE_JS_API) {
			this.queryEvaluator = new NodeJsClientEvaluator();
		}

		JavaScriptQueryCreatorType jsQueryCreatorType = Application.getJavaScriptQueryCreatorType();
		if(jsQueryCreatorType == JavaScriptQueryCreatorType.JENA_QUERY_CREATOR) {
			this.jsQueryCreator = new QueryCreatorJena();

		} else if (jsQueryCreatorType == JavaScriptQueryCreatorType.RDF4J_QUERY_CREATOR) {
			this.jsQueryCreator = new QueryCreatorRdf4j();
		}
	}

	public Object evaluateQuery(String sparqlQuery) throws Exception {
		return evaluateQuery(sparqlQuery, false);
	}

	public Object evaluateQuery(String sparqlQuery, Boolean returnRaw) throws Exception {
		try {
			String jsQuery = this.jsQueryCreator.createJavaScriptQuery(sparqlQuery);
			return this.queryEvaluator.evaluate(jsQuery, returnRaw);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
}

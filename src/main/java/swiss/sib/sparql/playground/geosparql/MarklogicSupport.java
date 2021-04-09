package swiss.sib.sparql.playground.geosparql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;

public class MarklogicSupport {
	private static final Log logger = LogFactory.getLog(MarklogicSupport.class);

	public Object evaluateQuery(String sparqlQuery) throws Exception {
		try {
			String alternatedSparqlQuery = alternateSparqlQuery(sparqlQuery);
			String params = createParamsForJsQuery(alternatedSparqlQuery);

			String jsQuery = creteJSQuery(alternatedSparqlQuery, params);

			// TODO: EVALUATE WITH MARKLOGIC
			// make a HTTP request to ML REST api

			return jsQuery;

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
		// TODO: params from query... use double map...
		return "";
	}

	private String creteJSQuery(String sparqlQuery, String params) {
		StringBuilder sb = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		sb.append("declareUpdate();" + newLine);
		sb.append("var sem = require('/MarkLogic/semantics.xqy');" + newLine);
		sb.append("var query = `" + sparqlQuery + "`;" + newLine);
		sb.append("var params = {" + params + "}" + newLine);
		sb.append("var results = sem.sparql(query,params);" + newLine);
		sb.append("results");

		return sb.toString();
	}
}

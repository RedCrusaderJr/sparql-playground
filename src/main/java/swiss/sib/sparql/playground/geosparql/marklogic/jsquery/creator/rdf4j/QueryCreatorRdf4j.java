package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.creator.rdf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;

import swiss.sib.sparql.playground.geosparql.FunctionMapper;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.JavaScriptQueryCreator;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.transformator.rdf4j.CustomFunctionVisitor;

public class QueryCreatorRdf4j extends JavaScriptQueryCreator {

    private CustomFunctionVisitor customFunctionVisitor;
    private SPARQLQueryRenderer renderer;

    public QueryCreatorRdf4j() {
        this.customFunctionVisitor = new CustomFunctionVisitor(FunctionMapper.getInstance());
        this.renderer = new SPARQLQueryRenderer();
    }

    @Override
    protected String transformSparqlQuery(String sparqlQueryStr) {
        try {
            ParsedQuery parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, sparqlQueryStr, null);
            parsedQuery.getTupleExpr().visit(customFunctionVisitor);
		    
            String renderedStr = renderer.render(parsedQuery);
		    return renderCorrection(renderedStr);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        logger.warn("[QueryCreatorRdf4j] Transformation was unsuccessful.");
        return sparqlQueryStr;
	}

    // template: <http://marklogic.com/xdmp#apply>( ... )
	private static final Pattern xdmpApplyArgsPattern = Pattern.compile("<http:\\/\\/marklogic\\.com\\/xdmp#apply>\\((?<args>.*)\\)");
    // template: """..."""^^<...>)
    private static final Pattern literalPattern = Pattern.compile("(?<literalLongValue>\"\"\"(?<literalShortValue>.*)\"\"\"\\^\\^\\<.*\\>)");

    // replacing: """value"""^^<datatype> from xdmp:apply aguments
    // with: 'value' - js can't cope with """literal""" notation
    private static String renderCorrection(String queryStr) {
        Matcher xdmpApplyArgsMatcher = xdmpApplyArgsPattern.matcher(queryStr);
        if (!xdmpApplyArgsMatcher.find()) {
            return queryStr;
        }

        String args = xdmpApplyArgsMatcher.group("args");
        String[] argsArray = args.split(", ");
        for (String arg : argsArray) {
            Matcher literalMatcher = literalPattern.matcher(arg);

            if (!literalMatcher.find()) {
                continue;
            }

            String literalLongValuev = literalMatcher.group("literalLongValue");
            String literalShortValue = literalMatcher.group("literalShortValue");
            queryStr = queryStr.replace(literalLongValuev, "'" + literalShortValue + "'");
        }

        return queryStr;
    }
}

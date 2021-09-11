package swiss.sib.sparql.playground.geosparql.marklogic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;

import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.geosparql.CustomFunction;
import swiss.sib.sparql.playground.geosparql.FunctionDescription;
import swiss.sib.sparql.playground.geosparql.FunctionMapper;
import swiss.sib.sparql.playground.geosparql.GeosparqlQueryModelVisitor;
import swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator.JavaClientEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator.EvaluatorType;
import swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator.JavaScriptQueryEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator.NodeJsClientEvaluator;
import swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator.RestClientEvaluator;

public class SparqlEvaluator {
	private static final Log logger = LogFactory.getLog(SparqlEvaluator.class);
	// #region Instance
	private static SparqlEvaluator instance;

	public static SparqlEvaluator getInstance() {
		if (instance == null) {
			synchronized (SparqlEvaluator.class) {
				if (instance == null) {
					instance = new SparqlEvaluator();
				}
			}
		}

		return instance;
	}

	private SparqlEvaluator() {
		if (Application.getMarklogicEvaluatorType() == EvaluatorType.JAVA_API) {
			this.queryEvaluator = new JavaClientEvaluator();

		} else if (Application.getMarklogicEvaluatorType() == EvaluatorType.REST_API) {
			this.queryEvaluator = new RestClientEvaluator();

		} else if (Application.getMarklogicEvaluatorType() == EvaluatorType.NODE_JS_API) {
			this.queryEvaluator = new NodeJsClientEvaluator();
		}
	}
	// #endregion Instance

	private JavaScriptQueryEvaluator queryEvaluator;

	public Object evaluateQuery(String sparqlQuery) throws Exception {
		try {
			String alternatedSparqlQuery = alternateSparqlQuery(sparqlQuery);

			List<String> abbreviations = extractFunctionAbbreviationsFromQuery(alternatedSparqlQuery);
			String importFunctionsStr = createImportFunctionsStr(abbreviations);
			String params = createXdmpApplyParams(abbreviations);

			String jsQuery = createJavaScriptQuery(alternatedSparqlQuery, importFunctionsStr, params);
			return this.queryEvaluator.evaluateJavaScript(jsQuery);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	// #region javascript query helpers
	private String alternateSparqlQuery(String sparqlQuery) throws Exception {
		GeosparqlQueryModelVisitor visitor = new GeosparqlQueryModelVisitor(FunctionMapper.getInstance());

		ParsedQuery parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, sparqlQuery, null);
		parsedQuery.getTupleExpr().visit(visitor);

		SPARQLQueryRenderer renderer = new SPARQLQueryRenderer();
		String renderedStr = renderer.render(parsedQuery);
		String finalQueryStr = renderCorrection(renderedStr);
		return finalQueryStr;
	}

	// <http://marklogic.com/xdmp#apply>( ... )
	private final Pattern xdmpApplyArgsPattern = Pattern
			.compile("<http:\\/\\/marklogic\\.com\\/xdmp#apply>\\((?<args>.*)\\)");
	// """..."""^^<...>)
	private final Pattern literalPattern = Pattern
			.compile("(?<literalLongValue>\"\"\"(?<literalShortValue>.*)\"\"\"\\^\\^\\<.*\\>)");

	// replacing: """value"""^^<datatype> from xdmp:apply aguments
	// with: 'value' - js can't cope with """literal""" notation
	private String renderCorrection(String queryStr) {
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

	private List<String> extractFunctionAbbreviationsFromQuery(String queryStr) {
		List<String> functionAbrvs = new ArrayList<String>();
		FunctionMapper mapper = FunctionMapper.getInstance();

		for (String functionAbrv : mapper.getAllSupportedFunctionByAbbreviations()) {
			if (!queryStr.contains(functionAbrv)) {
				continue;
			}
			functionAbrvs.add(functionAbrv);
		}
		return functionAbrvs;
	}

	private String createImportFunctionsStr(List<String> functionAbrvs) {
		StringBuilder sb = new StringBuilder();
		FunctionMapper mapper = FunctionMapper.getInstance();
		String newLine = System.getProperty("line.separator");

		for (String abrv : functionAbrvs) {
			FunctionDescription function = mapper.getFunctionByAbbreviation(abrv);
			if (!(function instanceof CustomFunction)) {
				continue;
			}

			String mlName = function.marklogicFunction;
			String modulePath = ((CustomFunction) function).modulePath;
			sb.append("import { ").append(mlName).append(" } from  '").append(modulePath).append("';");
			sb.append(newLine);
		}

		return sb.toString();
	}

	private String createXdmpApplyParams(List<String> functionAbrvs) {
		StringBuilder sb = new StringBuilder();
		FunctionMapper mapper = FunctionMapper.getInstance();
		Boolean isFirst = true;

		for (String abrv : functionAbrvs) {
			String separator = ", ";
			if (isFirst) {
				separator = "";
				isFirst = false;
			}

			sb.append(separator);
			sb.append(abrv).append(": ");
			sb.append(mapper.getFunctionByAbbreviation(abrv).marklogicFunction);
		}

		return sb.toString();
	}

	private String createJavaScriptQuery(String sparqlQuery, String importFunctionsStr, String params) {
		StringBuilder sb = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		sb.append("var sem = require('/MarkLogic/semantics.xqy');").append(newLine);
		sb.append(importFunctionsStr).append(newLine);
		sb.append("var query = `" + sparqlQuery + "`;").append(newLine);
		sb.append("var params = {" + params + "}").append(newLine);
		sb.append("var results = sem.sparql(query,params);").append(newLine);
		sb.append("results");

		return sb.toString();
	}
	// #endregion Create Javascript query
}

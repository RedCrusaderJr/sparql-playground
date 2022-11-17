package swiss.sib.sparql.playground.geosparql.marklogic.jsquery;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import swiss.sib.sparql.playground.geosparql.CustomFunction;
import swiss.sib.sparql.playground.geosparql.FunctionDescription;
import swiss.sib.sparql.playground.geosparql.FunctionMapper;

public abstract class JavaScriptQueryCreator {
    protected static final Log logger = LogFactory.getLog(JavaScriptQueryCreator.class);
	protected final String newLine = System.getProperty("line.separator");

	public String createJavaScriptQuery(String sparqlQuery) throws Exception {

		String transformedSparqlQuery = transformSparqlQuery(sparqlQuery);
		
		List<String> abbreviations = getFunctionAbbreviations(transformedSparqlQuery);
		String importFunctionsStr = createImportFunctionsStr(abbreviations);
		String xdmpApplyParams = createXdmpApplyParams(abbreviations);
		
		StringBuilder sb = new StringBuilder();
		sb.append("var sem = require('/MarkLogic/semantics.xqy');").append(newLine);
		sb.append(importFunctionsStr).append(newLine);
		sb.append("var query = `" + transformedSparqlQuery + "`;").append(newLine);
		sb.append("var params = {" + xdmpApplyParams + "}").append(newLine);
		sb.append("var results = sem.sparql(query,params);").append(newLine);
		sb.append("results");

		return sb.toString();
	}

    protected abstract String transformSparqlQuery(String sparqlQueryStr);

    private List<String> getFunctionAbbreviations(String queryStr) {
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
			sb.append("import ").append(mlName).append(" from  '").append(modulePath).append("';");
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
}

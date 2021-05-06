package swiss.sib.sparql.playground.geosparql;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.*;

public class FunctionMapper {
	private static final Log logger = LogFactory.getLog(FunctionMapper.class);

	// #region Instance
	private static FunctionMapper instance;

	public static FunctionMapper getInstance() {
		if (instance == null) {
			synchronized (FunctionMapper.class) {
				if (instance == null) {
					instance = new FunctionMapper();
				}
			}
		}

		return instance;
	}

	private FunctionMapper() {
		this.functionUriToFunctionDesc = new HashMap<String, FunctionDescription>();
		this.abbreviationToFunctionDesc = new HashMap<String, FunctionDescription>();

		try {
			importFunctions();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	// #endregion Instance

	private Map<String, FunctionDescription> functionUriToFunctionDesc;
	private Map<String, FunctionDescription> abbreviationToFunctionDesc;

	private void importFunctions() {
		FunctionDescription function1 = new FunctionDescription();
		function1.functionUri = "http://www.opengis.net/def/function/geosparql/intersection";
		function1.abbreviation = "intersectionFunction";
		function1.marklogicFunction = "geo.regionIntersects";

		CustomFunction function2 = new CustomFunction();
		function2.functionUri = "http://example.org/custom-function/buffer";
		function2.abbreviation = "bFun";
		function2.marklogicFunction = "bufferFunction";
		function2.modulePath = "/buffer-function.mjs";

		addFunction(function1);
		addFunction(function2);
	}

	private void addFunction(FunctionDescription function) {
		this.functionUriToFunctionDesc.put(function.functionUri, function);
		this.abbreviationToFunctionDesc.put(function.abbreviation, function);
	}

	public Boolean findFunctionByUri(String functionUri) {
		return functionUriToFunctionDesc.containsKey(functionUri);
	}

	public Boolean findFunctionByAbbreviation(String functionAbbreviation) {
		return abbreviationToFunctionDesc.containsKey(functionAbbreviation);
	}

	public FunctionDescription getFunctionByUri(String functionUri) {
		return functionUriToFunctionDesc.getOrDefault(functionUri, null);
	}

	public FunctionDescription getFunctionByAbbreviation(String abbreviation) {
		return abbreviationToFunctionDesc.getOrDefault(abbreviation, null);
	}

	public Set<String> getAllSupportedFunctionByUri() {
		return functionUriToFunctionDesc.keySet();
	}

	public Set<String> getAllSupportedFunctionByAbbreviations() {
		return abbreviationToFunctionDesc.keySet();
	}
}

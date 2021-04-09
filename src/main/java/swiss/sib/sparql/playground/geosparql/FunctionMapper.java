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
		try {
			importFunctions();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	// #endregion Instance

	private Map<String, String> geosparqlUriToFunctionAbbreviationMap;
	private Map<String, String> functionAbbreviationToGeosparqlUriMap;

	private void importFunctions() throws ClassNotFoundException {
		geosparqlUriToFunctionAbbreviationMap = new HashMap<String, String>();
		geosparqlUriToFunctionAbbreviationMap.put("http://www.opengis.net/def/function/geosparql/intersection",
				"intersectionFunction");

		functionAbbreviationToGeosparqlUriMap = new HashMap<String, String>();
		functionAbbreviationToGeosparqlUriMap.put("intersectionFunction",
				"http://www.opengis.net/def/function/geosparql/intersection");
	}

	public Boolean findAbbreviationByUri(String functionUri) {
		return geosparqlUriToFunctionAbbreviationMap.containsKey(functionUri);
	}

	public Boolean findFunctionUriByAbbreviation(String functionAbbreviation) {
		return functionAbbreviationToGeosparqlUriMap.containsKey(functionAbbreviation);
	}

	public String getFunctionAbbreviationByUri(String functionUri) {
		return geosparqlUriToFunctionAbbreviationMap.getOrDefault(functionUri, null);
	}

	public String getFunctionUriByAbbreviation(String abbreviation) {
		return functionAbbreviationToGeosparqlUriMap.getOrDefault(abbreviation, null);
	}

	public Set<String> getAllSupportedFunctionByAbbreviations() {
		return functionAbbreviationToGeosparqlUriMap.keySet();
	}
}

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
	private Map<String, String> geosparqlUriToMarklogicFunctionMap;
	private Map<String, String> functionAbbreviationToMarklogicFunctionMap;

	private void importFunctions() throws ClassNotFoundException {
		geosparqlUriToFunctionAbbreviationMap = new HashMap<String, String>();
		geosparqlUriToFunctionAbbreviationMap.put("http://www.opengis.net/def/function/geosparql/intersection",
				"intersectionFunction");

		functionAbbreviationToGeosparqlUriMap = new HashMap<String, String>();
		functionAbbreviationToGeosparqlUriMap.put("intersectionFunction",
				"http://www.opengis.net/def/function/geosparql/intersection");

		geosparqlUriToMarklogicFunctionMap = new HashMap<String, String>();
		geosparqlUriToMarklogicFunctionMap.put("http://www.opengis.net/def/function/geosparql/intersection",
				"geo.regionIntersects");

		functionAbbreviationToMarklogicFunctionMap = new HashMap<String, String>();
		functionAbbreviationToMarklogicFunctionMap.put("intersectionFunction", "geo.regionIntersects");
	}

	public Boolean findAbbreviationByUri(String functionUri) {
		return geosparqlUriToFunctionAbbreviationMap.containsKey(functionUri);
	}

	public Boolean findFunctionUriByAbbreviation(String functionAbbreviation) {
		return functionAbbreviationToGeosparqlUriMap.containsKey(functionAbbreviation);
	}

	public Boolean findMarklogicFunctionByUri(String functionUri) {
		return geosparqlUriToMarklogicFunctionMap.containsKey(functionUri);
	}

	public Boolean findMarklogicFunctionByAbbreviation(String functionAbbreviation) {
		return functionAbbreviationToMarklogicFunctionMap.containsKey(functionAbbreviation);
	}

	public String getFunctionAbbreviationByUri(String functionUri) {
		return geosparqlUriToFunctionAbbreviationMap.getOrDefault(functionUri, null);
	}

	public String getFunctionUriByAbbreviation(String abbreviation) {
		return functionAbbreviationToGeosparqlUriMap.getOrDefault(abbreviation, null);
	}

	public String getMarklogicFunctionByUri(String functionUri) {
		return geosparqlUriToMarklogicFunctionMap.getOrDefault(functionUri, null);
	}

	public String getMarklogicFunctionByAbbreviation(String functionAbbreviation) {
		return functionAbbreviationToMarklogicFunctionMap.getOrDefault(functionAbbreviation, null);
	}

	public Set<String> getAllSupportedFunctionByUri() {
		return geosparqlUriToFunctionAbbreviationMap.keySet();
	}

	public Set<String> getAllSupportedFunctionByAbbreviations() {
		return functionAbbreviationToGeosparqlUriMap.keySet();
	}
}

package swiss.sib.sparql.playground.geosparql;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
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

	private static final String FUNCTION_DEFINITION_PATH = "src/main/java/swiss/sib/sparql/playground/geosparql/marklogic/FunctionDefinition.xml";

	private void importFunctions() {
		try {
			importFunctionsFromXML();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void importFunctionsFromXML() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(FUNCTION_DEFINITION_PATH);
  
		Element functionDescriptions = extractElement(doc, "FunctionDescriptions");
		NodeList functionDescriptionsChildNodes = functionDescriptions.getChildNodes();
		
		for (int i = 0; i < functionDescriptionsChildNodes.getLength(); i++) {
			Node currentNode = functionDescriptionsChildNodes.item(i);
			
			if (currentNode instanceof Element) {
					addFunction(ParseFunction((Element)currentNode));
			}
		}
		
		Element customFunctions = extractElement(doc, "CustomFunctions");
		NodeList customFunctionsChildNodes = customFunctions.getChildNodes();

		for (int i = 0; i < customFunctionsChildNodes.getLength(); i++) {
			Node currentNode = customFunctionsChildNodes.item(i);

			if (currentNode instanceof Element) {
				addFunction(ParseCustomFunction((Element)currentNode));
			}
		}
	}

	private FunctionDescription ParseFunction(Element functionDescriptionElement) {
		FunctionDescription functionDescription = new FunctionDescription();
		functionDescription.functionUri = extractPropertyValue(functionDescriptionElement, "FunctionUri");
		functionDescription.abbreviation = extractPropertyValue(functionDescriptionElement, "Abbreviation");
		functionDescription.marklogicFunction = extractPropertyValue(functionDescriptionElement, "MarklogicFunction");

		return functionDescription;
	}

	private CustomFunction ParseCustomFunction(Element customFunctionElement) {
		CustomFunction customFunction = new CustomFunction();
		customFunction.functionUri = extractPropertyValue(customFunctionElement, "FunctionUri");
		customFunction.abbreviation = extractPropertyValue(customFunctionElement, "Abbreviation");
		customFunction.marklogicFunction = extractPropertyValue(customFunctionElement, "MarklogicFunction");
		customFunction.modulePath = extractPropertyValue(customFunctionElement, "ModulePath");	

		return customFunction;
	}

	private Element extractElement(Document document, String propertyName) {
		return (Element) document.getElementsByTagName(propertyName).item(0);
	}

	private String extractPropertyValue(Element element, String propertyName) {
		return element.getElementsByTagName(propertyName).item(0).getTextContent();
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

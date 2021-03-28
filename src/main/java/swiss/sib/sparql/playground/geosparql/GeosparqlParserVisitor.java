package swiss.sib.sparql.playground.geosparql;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.query.parser.sparql.AbstractASTVisitor;
import org.eclipse.rdf4j.query.parser.sparql.ast.*;

import swiss.sib.sparql.playground.geosparql.functions.FunctionFactory;
import swiss.sib.sparql.playground.geosparql.functions.GeosparqlFunction;
import swiss.sib.sparql.playground.geosparql.functions.GeosparqlFunctionParameters;

public class GeosparqlParserVisitor extends AbstractASTVisitor {
	private static final Log logger = LogFactory.getLog(GeosparqlParserVisitor.class);

	private FunctionFactory factory;
	private Map<String, String> prefixMap;

	public GeosparqlParserVisitor(FunctionFactory factory, Map<String, String> prefixMap) {
		this.factory = factory;
		this.prefixMap = prefixMap;
	}

	@Override
	public Object visit(ASTFunctionCall node, Object data) throws VisitorException {
		String functionName = extractFunctionName(node);
		GeosparqlFunctionParameters parameters = extractFunctionParameters(node, functionName);

		try {
			GeosparqlFunction function = factory.createFunction(functionName, parameters);
			Object result = function.Execute();
			return result;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private String extractFunctionName(ASTFunctionCall node) {
		try {
			String className = "org.eclipse.rdf4j.query.parser.sparql.ast.ASTQName";

			@SuppressWarnings("unchecked")
			Class<ASTQName> type = (Class<ASTQName>) Class.forName(className);
			ASTQName qName = node.jjtGetChild(type);

			String functioName = qName.getValue();
			String fullFunctionName = functioName;

			String[] splitParts = functioName.split(":");
			if (splitParts.length == 2 && prefixMap.containsKey(splitParts[0])) {
				fullFunctionName = prefixMap.get(splitParts[0]) + splitParts[1];
			}

			return fullFunctionName;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}

	private GeosparqlFunctionParameters extractFunctionParameters(ASTFunctionCall node, String functionName) {
		try {
			String className = "org.eclipse.rdf4j.query.parser.sparql.ast.ASTVar";

			@SuppressWarnings("unchecked")
			Class<ASTVar> type = (Class<ASTVar>) Class.forName(className);
			List<ASTVar> argumetns = node.jjtGetChildren(type); // ASTVar has only a variable name.... and value is
																// determined by the rest of the query....
			return null;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}

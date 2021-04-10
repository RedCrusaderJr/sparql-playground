package swiss.sib.sparql.playground.geosparql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.*;
import org.eclipse.rdf4j.query.parser.sparql.AbstractASTVisitor;
import org.eclipse.rdf4j.query.parser.sparql.ast.*;

public class GeosparqlParserVisitor extends AbstractASTVisitor {
	private static final Log logger = LogFactory.getLog(GeosparqlParserVisitor.class);

	private FunctionMapper functionMapper;
	private Map<String, String> prefixMap;

	public GeosparqlParserVisitor(FunctionMapper functionMapper, Map<String, String> prefixMap) {
		this.functionMapper = functionMapper;
		this.prefixMap = prefixMap;
	}

	@Override
	public Object visit(ASTFunctionCall node, Object data) throws VisitorException {
		ASTQName functionName = extractFunctionName(node);
		List<ASTVar> params = extractFunctionParams(node);

		try {
			ASTFunctionCall applyFunctionCall = new ASTFunctionCall(node.hashCode());
			ASTQName applyFunctionName = new ASTQName(functionName.hashCode());
			applyFunctionName.setValue(getFullUri("xdmp:apply"));

			int index = 0;
			applyFunctionCall.jjtAddChild(applyFunctionName, index++);

			ASTVar functionPointer = new ASTVar(555);
			functionPointer.setName(functionMapper.getFunctionAbbreviationByUri(getFullUri(functionName.getValue())));
			applyFunctionCall.jjtAddChild(functionPointer, index++);

			for (ASTVar param : params) {
				applyFunctionCall.jjtAddChild(param, index++);
			}

			node.jjtReplaceWith(applyFunctionCall);
			// TODO: random ID for function pointer
			// TODO: parent children relations, check in detail

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	private ASTQName extractFunctionName(ASTFunctionCall node) {
		try {
			String className = "org.eclipse.rdf4j.query.parser.sparql.ast.ASTQName";

			@SuppressWarnings("unchecked")
			Class<ASTQName> type = (Class<ASTQName>) Class.forName(className);
			ASTQName qName = node.jjtGetChild(type);
			return qName;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private List<ASTVar> extractFunctionParams(ASTFunctionCall node) {
		try {
			String className = "org.eclipse.rdf4j.query.parser.sparql.ast.ASTVar";
			@SuppressWarnings("unchecked")
			Class<ASTVar> type = (Class<ASTVar>) Class.forName(className);
			List<ASTVar> params = node.jjtGetChildren(type);
			return params;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ArrayList<ASTVar>();
		}
	}

	private String getFullUri(String functioName) {
		String fullFunctionName = functioName;

		String[] splitParts = functioName.split(":");
		if (splitParts.length == 2 && prefixMap.containsKey(splitParts[0])) {
			fullFunctionName = prefixMap.get(splitParts[0]) + splitParts[1];
		}

		return fullFunctionName;
	}
}

// TODO:
// sb.append("var params = { intersectFunction: geo.polygon-intersects }" +
// newLine);
// sb.append("prefix xdmp: <http://marklogic.com/xdmp#>" + newLine);
// sb.append("select distinct ?intersect ?xGeom ?yGeom" + newLine);
// sb.append("where {" + newLine);
// sb.append("?x geo:asWKT ?xGeom." + newLine);
// sb.append("?y geo:asWKT ?yGeom." + newLine);
// sb.append("bind(xdmp:apply(?intersectFunction, ?xGeom, ?yGeom) as
// ?intersect)" + newLine);
// sb.append("}" + newLine);

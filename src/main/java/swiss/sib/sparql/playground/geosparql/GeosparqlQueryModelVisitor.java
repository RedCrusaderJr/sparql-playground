package swiss.sib.sparql.playground.geosparql;

import java.util.*;

import org.apache.commons.logging.*;
import org.eclipse.rdf4j.query.algebra.*;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

public class GeosparqlQueryModelVisitor extends AbstractQueryModelVisitor<Exception> {
	private static final Log logger = LogFactory.getLog(GeosparqlQueryModelVisitor.class);

	private FunctionMapper functionMapper;

	public GeosparqlQueryModelVisitor(FunctionMapper functionMapper) {
		this.functionMapper = functionMapper;
	}

	@Override
	public void meet(FunctionCall node) {
		if (!functionMapper.findAbbreviationByUri(node.getURI())) {
			logger.debug("Function call will not be changed. Function uri: " + node.getURI());
			return;
		}

		String functionName = node.getURI();
		List<ValueExpr> params = node.getArgs();

		FunctionCall applyFunctionCall = new FunctionCall();
		applyFunctionCall.setURI("http://marklogic.com/xdmp#apply");
		applyFunctionCall.setParentNode(node);
		List<ValueExpr> args = new ArrayList<ValueExpr>();

		Var functionPointer = new Var();
		functionPointer.setName(functionMapper.getFunctionAbbreviationByUri(functionName));
		functionPointer.setParentNode(applyFunctionCall);
		args.add(functionPointer);

		for (ValueExpr param : params) {
			param.setParentNode(applyFunctionCall);
			args.add(param);
		}

		applyFunctionCall.setArgs(args);
		node.replaceWith(applyFunctionCall);
	}
}

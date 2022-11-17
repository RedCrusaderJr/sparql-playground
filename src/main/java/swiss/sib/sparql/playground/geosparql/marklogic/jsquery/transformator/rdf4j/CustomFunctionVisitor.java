package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.transformator.rdf4j;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.query.algebra.FunctionCall;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

import swiss.sib.sparql.playground.geosparql.FunctionMapper;

public class CustomFunctionVisitor extends AbstractQueryModelVisitor<Exception> {
	private static final Log logger = LogFactory.getLog(CustomFunctionVisitor.class);

	private FunctionMapper functionMapper;

	public CustomFunctionVisitor(FunctionMapper functionMapper) {
		this.functionMapper = functionMapper;
	}

	@Override
	public void meet(FunctionCall node) throws Exception {
		if (!functionMapper.findFunctionByUri(node.getURI())) {
			// logger.debug("Function call will not be changed. Function uri: " +
			// node.getURI());
			return;
		}
		String functionName = node.getURI();
		logger.debug("Changing a Function call. Function uri: " + node.getURI());

		List<ValueExpr> params = node.getArgs();

		FunctionCall applyFunctionCall = new FunctionCall();
		applyFunctionCall.setURI("http://marklogic.com/xdmp#apply");
		applyFunctionCall.setParentNode(node);
		List<ValueExpr> args = new ArrayList<ValueExpr>();

		Var functionPointer = new Var();
		functionPointer.setName(functionMapper.getFunctionByUri(functionName).abbreviation);
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

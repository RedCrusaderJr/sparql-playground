package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.transformator.jena;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.FunctionLabel;

import swiss.sib.sparql.playground.geosparql.FunctionMapper;

public class CustomFunctionVisitor extends OpVisitorBase  {
	private static final Log logger = LogFactory.getLog(CustomFunctionVisitor.class);

	private FunctionMapper functionMapper;

	public CustomFunctionVisitor(FunctionMapper functionMapper) {
		this.functionMapper = functionMapper;
	}
	
	@Override
	public void visit(OpExtend opExtend) {
		Map<Var, Expr> expressionMap = opExtend.getVarExprList().getExprs();
		
		for (Var key : expressionMap.keySet()) {
			Expr currentExpression = expressionMap.get(key);
			if(!currentExpression.isFunction()) {
				continue;
			}

			ExprFunction functionExpression = (ExprFunction)currentExpression; 
			String functionIri = functionExpression.getFunctionIRI();
			if (!functionMapper.findFunctionByUri(functionIri)) {
				continue;
			}

			logger.debug("Changing a Function call. Function iri: " + functionIri);

			ArrayList<Expr> arguments = new ArrayList<Expr>();
			arguments.add(new ExprVar(Var.alloc(functionMapper.getFunctionByUri(functionIri).abbreviation))); 	//function pointer var
			arguments.addAll(functionExpression.getArgs());														//original vars

			E_Function applyFunction = new E_Function("http://marklogic.com/xdmp#apply", ExprList.create(arguments));

			expressionMap.remove(key);
			expressionMap.put(key, applyFunction);
			break;
		}
	}
}

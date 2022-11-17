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
import swiss.sib.sparql.playground.geosparql.FunctionMapper;

public class CustomFunctionVisitor extends OpVisitorBase  {
	private static final Log logger = LogFactory.getLog(CustomFunctionVisitor.class);

	private FunctionMapper functionMapper;

	public CustomFunctionVisitor(FunctionMapper functionMapper) {
		this.functionMapper = functionMapper;
	}
	
	@Override
	public void visit(OpExtend opExtend) {
		
		for (Expr currentExpression : opExtend.getVarExprList().getExprs().values()) {
			
			ExprFunction functionExpression = currentExpression.getFunction(); 
			if(functionExpression == null){
				continue;
			}

			String functionIri = functionExpression.getFunctionIRI();
			if (!functionMapper.findFunctionByUri(functionIri)) {
				continue;
			}
			logger.debug("Changing a Function call. Function iri: " + functionIri);

			ArrayList<Expr> arguments = new ArrayList<Expr>();
			arguments.add(new ExprVar(Var.alloc(functionMapper.getFunctionByUri(functionIri).abbreviation))); 	//function pointer var
			arguments.addAll(functionExpression.getArgs());														//original vars

			E_Function applyFunction = new E_Function(functionIri, ExprList.create(arguments));

			currentExpression.getFunction();
			//functionExpression. Replace(applyFunction);
		}

		//node.replaceWith(applyFunctionCall);
	}
}

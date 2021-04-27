package swiss.sib.sparql.playground.function;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;

public class SquareRootFunction implements Function {
	public static final String NAMESPACE = "http://example.org/custom-function/";

	@Override
	public String getURI() {
		return NAMESPACE + "sqrt";
	}

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
		if (args.length != 1) {
			throw new ValueExprEvaluationException(
					"square root function requires exactly 1 argument, got " + args.length);
		}

		Value arg = args[0];

		if (!(arg instanceof Literal)) {
			throw new ValueExprEvaluationException("invalid argument (literal expected): " + arg);
		}

		Double doubleValue = Double.parseDouble(arg.stringValue());
		return valueFactory.createLiteral(Math.sqrt(doubleValue));
	}
}

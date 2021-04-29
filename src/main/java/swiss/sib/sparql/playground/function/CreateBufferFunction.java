package swiss.sib.sparql.playground.function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;

public class CreateBufferFunction implements Function {
	public static final String NAMESPACE = "http://example.org/custom-function/";

	@Override
	public String getURI() {
		return NAMESPACE + "buffer";
	}

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
		if (args.length != 2) {
			throw new ValueExprEvaluationException("buffer function requires exactly 2 argument, got " + args.length);
		}

		Matcher wktLinestringMatcher = parseArg0(args[0]);
		Double distanceArg = parseArg1(args[1]);

		BufferCreator bufferCreator = new BufferCreator(wktLinestringMatcher, distanceArg);

		String bufferStr = bufferCreator.create();
		return valueFactory.createLiteral(bufferStr);
	}

	private Matcher parseArg0(Value arg) {
		if (!(arg instanceof Literal)) {
			throw new ValueExprEvaluationException("invalid argument (literal expected): " + arg);
		}

		// LINESTRING (x1 y1, x2 y2)
		Pattern wktLinestringPattern = Pattern.compile("LINESTRING \\((?<x1>.*) (?<y1>.*), (?<x2>.*) (?<y2>.*)\\)");
		Matcher wktLinestringMatcher = wktLinestringPattern.matcher(arg.stringValue());

		if (!wktLinestringMatcher.find()) {
			throw new ValueExprEvaluationException("invalid argument format (wkt LINESTRING expected): " + arg);
		}

		return wktLinestringMatcher;
	}

	private Double parseArg1(Value arg) {
		if (!(arg instanceof Literal)) {
			throw new ValueExprEvaluationException("invalid argument (literal expected): " + arg);
		}

		// total distance between a point (x1, y1) and coresponding polygone points -
		// same for point (x2,y2):
		Double distanceArg = Double.parseDouble(arg.stringValue());
		return distanceArg;
	}
}

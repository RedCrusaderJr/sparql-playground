package swiss.sib.sparql.playground.function;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;

public class ExtendLineFunctiion implements Function {
	public static final String NAMESPACE = "http://example.org/custom-function/";

	private final int prec = 15;
	private final RoundingMode mode = RoundingMode.HALF_EVEN;
	private final MathContext context = new MathContext(prec, mode);

	// LINESTRING (x1 y1, x2 y2)
	private final Pattern wktLinestringPattern = Pattern
			.compile("LINESTRING \\((?<x1>.*) (?<y1>.*), (?<x2>.*) (?<y2>.*)\\)");

	// [m / deg]
	private final BigDecimal latUnit = BigDecimal.valueOf(111200.0).setScale(prec, mode);
	// [m / deg]
	private final BigDecimal lonUnit = BigDecimal.valueOf(78630.0).setScale(prec, mode);

	@Override
	public String getURI() {
		return NAMESPACE + "extendLine";
	}

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
		if (args.length != 2) {
			throw new ValueExprEvaluationException("buffer function requires exactly 2 argument, got " + args.length);
		}

		Matcher wktLinestringMatcher = parseArg0(args[0]);
		Double distanceArg = parseArg1(args[1]);

		String lineStr = extendLine(wktLinestringMatcher, distanceArg);
		return valueFactory.createLiteral(lineStr);
	}

	private Matcher parseArg0(Value arg) {
		if (!(arg instanceof Literal)) {
			throw new ValueExprEvaluationException("invalid argument (literal expected): " + arg);
		}

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

	// extends the line for distanceTotal at each end
	private String extendLine(Matcher wktLinestringMatcher, Double distance) {
		BigDecimal x1 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("x1"))).setScale(prec, mode);
		BigDecimal y1 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("y1"))).setScale(prec, mode);
		BigDecimal x2 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("x2"))).setScale(prec, mode);
		BigDecimal y2 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("y2"))).setScale(prec, mode);
		if (x2.compareTo(x1) == 0 && y2.compareTo(y1) == 0) {
			throw new ValueExprEvaluationException(
					"Invalid LINESTRING definition: x1 equals x2 and y1 equals y2. " + "x1: " + x1.doubleValue()
							+ " y1: " + y1.doubleValue() + "| x2: " + x1.doubleValue() + " y2: " + y1.doubleValue());
		}

		BigDecimal distanceTotal = BigDecimal.valueOf(distance).setScale(prec, mode);

		// slope of acline segment: slope = (y2 - y1) / (x2 - x1)
		BigDecimal slope = y2.subtract(y1, context).divide(x2.subtract(x1, context), context); // x2 and x1 will not be
																								// equal

		// angle of line to x-axis [rad]: angle = arcus tangent(slope)
		BigDecimal angle = BigDecimal.valueOf(Math.atan(slope.doubleValue())).setScale(prec, mode);
		if (angle.doubleValue() == 0 || ((Double) angle.doubleValue()).isNaN()) {
			throw new ValueExprEvaluationException("perpendicular angle: " + angle.doubleValue());
		}

		// X and Y components of total distance [m]:
		// distanceY = distanceTotal * sin(angle)
		BigDecimal angleSin = BigDecimal.valueOf(Math.sin(angle.doubleValue())).setScale(prec, mode);
		BigDecimal distanceY = distanceTotal.multiply(angleSin, context);
		// distanceX = distanceY / slope
		BigDecimal distanceX = distanceY.divide(slope, context); // slope will not be zero if latitudes are not equal

		// TODO: UMT conversion
		// latitude change [deg]
		BigDecimal latDeg = distanceX.divide(latUnit, context); // latUnit will never be zero
		// longitude change [deg]
		BigDecimal lonDeg = distanceY.divide(lonUnit, context); // lonUnit will never be zero

		// p as in polygonX1, polygonY1, ...
		// extended line is represented as polygon, without width
		BigDecimal pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4;
		// x1 = x1 - lonDeg
		// y1 = y1 - latDeg
		pX1 = x1.subtract(lonDeg, context);
		pY1 = y1.subtract(latDeg, context);

		pX2 = x1.add(lonDeg, context);
		pY2 = y1.add(latDeg, context);

		// x3 = x2 + lonDeg
		// y3 = y2 + latDeg
		pX3 = x2.add(lonDeg, context);
		pY3 = y2.add(latDeg, context);

		pX4 = x2.subtract(lonDeg, context);
		pY4 = y2.subtract(latDeg, context);

		return formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4);
	}

	// p as in polygonX1, polygonY1, ...
	private String formatPolygonStr(BigDecimal pX1, BigDecimal pY1, BigDecimal pX2, BigDecimal pY2, BigDecimal pX3,
			BigDecimal pY3, BigDecimal pX4, BigDecimal pY4) {

		return String.format(Locale.US, "POLYGON ((%.15f %.15f, %.15f %.15f, %.15f %.15f, %.15f %.15f, %.15f %.15f))",
				pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4, pX1, pY1);
	}
}

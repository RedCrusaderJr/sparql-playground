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

public class CreateBufferFunction implements Function {
	public static final String NAMESPACE = "http://example.org/custom-function/";

	private final int prec = 15;
	private final RoundingMode mode = RoundingMode.HALF_EVEN;
	private final MathContext context = new MathContext(prec, mode);

	// [m / deg]
	private final BigDecimal latUnit = BigDecimal.valueOf(111200.0).setScale(prec, mode);
	// [m / deg]
	private final BigDecimal lonUnit = BigDecimal.valueOf(78630.0).setScale(prec, mode);

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
		BigDecimal distanceTotal = parseArg1(args[1]);

		BigDecimal x1 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("x1"))).setScale(prec, mode);
		BigDecimal y1 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("y1"))).setScale(prec, mode);
		BigDecimal x2 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("x2"))).setScale(prec, mode);
		BigDecimal y2 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("y2"))).setScale(prec, mode);

		Value result;

		if (x2.equals(x1)) {
			result = equalLatitudesSpecialCase(valueFactory, x1, y1, x2, y2, distanceTotal);

		} else if (y2.equals(y1)) {
			result = equalLongitudesSpecialCase(valueFactory, x1, y1, x2, y2, distanceTotal);

		} else {
			result = basicCase(valueFactory, x1, y1, x2, y2, distanceTotal);
		}

		return result;
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

	private BigDecimal parseArg1(Value arg) {
		if (!(arg instanceof Literal)) {
			throw new ValueExprEvaluationException("invalid argument (literal expected): " + arg);
		}

		// total distance between a point (x1, y1) and coresponding polygone points -
		// same for point (x2,y2):
		Double distanceArg = Double.parseDouble(arg.stringValue());
		BigDecimal distanceTotal = BigDecimal.valueOf(distanceArg).setScale(prec, mode);
		return distanceTotal;
	}

	// CASE x1 = x2
	private Value equalLatitudesSpecialCase(ValueFactory valueFactory, BigDecimal x1, BigDecimal y1, BigDecimal x2,
			BigDecimal y2, BigDecimal distanceTotal) {
		// X and Y components of total distance [m]:
		// distanceX = distanceTotal
		BigDecimal distanceX = distanceTotal;
		// latitude change [deg]: latDeg = distanceX / latUnit
		BigDecimal latDeg = distanceX.divide(latUnit, context); // latUnit will never be zero

		// distanceY = 0
		// longitude change [deg]: lonDeg = distanceY / lonUnit
		BigDecimal lonDeg = BigDecimal.valueOf(0).setScale(prec, mode);

		// POLYGONE POINTS
		String polygonStr;
		polygonStr = createPolygonStr(x1, y1, x2, y2, latDeg, lonDeg, true);
		return valueFactory.createLiteral(polygonStr);
	}

	// CASE y1 = y2
	private Value equalLongitudesSpecialCase(ValueFactory valueFactory, BigDecimal x1, BigDecimal y1, BigDecimal x2,
			BigDecimal y2, BigDecimal distanceTotal) {
		// X and Y components of total distance [m]:
		// distanceX = 0
		// latitude change [deg]: latDeg = distanceX / latUnit
		BigDecimal latDeg = BigDecimal.valueOf(0).setScale(prec, mode);

		// distanceY = distanceTotal
		BigDecimal distanceY = distanceTotal;
		// longitude change [deg]: lonDeg = distanceY / lonUnit
		BigDecimal lonDeg = distanceY.divide(lonUnit, context); // lonUnit will never be zero

		// POLYGONE POINTS
		String polygonStr;
		polygonStr = createPolygonStr(x1, y1, x2, y2, latDeg, lonDeg, true);
		return valueFactory.createLiteral(polygonStr);
	}

	private Value basicCase(ValueFactory valueFactory, BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2,
			BigDecimal distanceTotal) {
		// slope of acline segment: Slope = (y2 - y1) / (x2 - x1)
		BigDecimal slope = y2.subtract(y1, context).divide(x2.subtract(x1, context), context);

		// slope of perpendicular line: pSlope = - 1 / Slope
		BigDecimal pSlope = BigDecimal.valueOf(-1).setScale(prec, mode).divide(slope, context); // slope will not be
																								// zero if
																								// longitudes
																								// are not equal

		// angle of perpendicular line to x-axis [rad]: angle = arcus tangent(pSlope)
		BigDecimal angle = BigDecimal.valueOf(Math.atan(pSlope.doubleValue())).setScale(prec, mode);

		// X and Y components of total distance [m]:
		// distanceY = distanceTotal * sin(angle)
		BigDecimal angleSin = BigDecimal.valueOf(Math.sin(angle.doubleValue())).setScale(prec, mode);
		BigDecimal distanceY = distanceTotal.multiply(angleSin, context);

		// distanceX = distanceY / pSlope
		BigDecimal distanceX = distanceY.divide(pSlope, context);

		// latitude change [deg]
		BigDecimal latDeg = distanceX.divide(latUnit, context); // latUnit will never be zero

		// longitude change [deg]
		BigDecimal lonDeg = distanceY.divide(lonUnit, context); // lonUnit will never be zero

		// POLYGONE POINTS
		String polygonStr;
		if (angle.doubleValue() > 0 && angle.doubleValue() < Math.PI / 2) {
			polygonStr = createPolygonStr(x1, y1, x2, y2, latDeg, lonDeg, true);

		} else if (angle.doubleValue() < 0 && angle.doubleValue() > -Math.PI / 2) {
			polygonStr = createPolygonStr(x1, y1, x2, y2, latDeg, lonDeg, false);

		} else {
			polygonStr = "";
		}

		return valueFactory.createLiteral(polygonStr);
	}

	private String createPolygonStr(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2, BigDecimal latDeg,
			BigDecimal lonDeg, Boolean differentSigns) {
		BigDecimal pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4;

		int exponent = 0;
		if (differentSigns) {
			exponent = 1;
		}
		// multiplier = (-1)^exponent
		BigDecimal multiplicand = BigDecimal.valueOf(Math.pow(-1, exponent)).setScale(prec, mode);
		BigDecimal multipliedLonDeg = lonDeg.multiply(multiplicand, context);

		// pX1 = x1 + latDeg
		// pY1 = y1 + lonDeg*multiplier
		pX1 = x1.add(latDeg, context);
		pY1 = y1.add(multipliedLonDeg, context);

		// pX2 = x2 + latDeg
		// pY2 = y2 + lonDeg*multiplier
		pX2 = x2.add(latDeg, context);
		pY2 = y2.add(multipliedLonDeg, context);

		// pX3 = x2 - latDeg
		// pY3 = y2 - lonDeg*multiplier)
		pX3 = x2.subtract(latDeg, context);
		pY3 = y2.subtract(multipliedLonDeg, context);

		// pX4 = x1 - latDeg
		// pY4 = y1 - lonDeg*multiplier)
		pX4 = x1.subtract(latDeg, context);
		pY4 = y1.subtract(multipliedLonDeg, context);

		String polygonStr = String.format(Locale.US, "POLYGON ((%f %f, %f %f, %f %f, %f %f, %f %f))", pX1, pY1, pX2,
				pY2, pX3, pY3, pX4, pY4, pX1, pY1);
		return polygonStr;
	}
}

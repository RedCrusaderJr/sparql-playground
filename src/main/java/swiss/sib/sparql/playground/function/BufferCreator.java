package swiss.sib.sparql.playground.function;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;

import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;

public class BufferCreator {
	private final int prec = 15;
	private final RoundingMode mode = RoundingMode.HALF_EVEN;
	private final MathContext context = new MathContext(prec, mode);

	// [m / deg]
	private final BigDecimal latUnit = BigDecimal.valueOf(111200.0).setScale(prec, mode);
	// [m / deg]
	private final BigDecimal lonUnit = BigDecimal.valueOf(78630.0).setScale(prec, mode);

	private BigDecimal x1;
	private BigDecimal y1;
	private BigDecimal x2;
	private BigDecimal y2;
	private BigDecimal distanceTotal;

	public BufferCreator(Matcher wktLinestringMatcher, Double distance) {
		this.x1 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("x1"))).setScale(prec, mode);
		this.y1 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("y1"))).setScale(prec, mode);
		this.x2 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("x2"))).setScale(prec, mode);
		this.y2 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("y2"))).setScale(prec, mode);
		if (x2.compareTo(x1) == 0 && y2.compareTo(y1) == 0) {
			throw new ValueExprEvaluationException(
					"Invalid LINESTRING definition: x1 equals x2 and y1 equals y2. " + "x1: " + x1.doubleValue()
							+ " y1: " + y1.doubleValue() + "| x2: " + x1.doubleValue() + " y2: " + y1.doubleValue());
		}

		this.distanceTotal = BigDecimal.valueOf(distance).setScale(prec, mode);
	}

	public String create() {
		String strResult;

		if (x2.compareTo(x1) == 0) {
			strResult = equalLatitudesSpecialCase();

		} else if (y2.compareTo(y1) == 0) {
			strResult = equalLongitudesSpecialCase();

		} else {
			strResult = basicCase();
		}

		return strResult;
	}

	private String formatPolygonStr(BigDecimal pX1, BigDecimal pY1, BigDecimal pX2, BigDecimal pY2, BigDecimal pX3,
			BigDecimal pY3, BigDecimal pX4, BigDecimal pY4) {

		return String.format(Locale.US, "POLYGON ((%.15f %.15f, %.15f %.15f, %.15f %.15f, %.15f %.15f, %.15f %.15f))",
				pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4, pX1, pY1);
	}

	// CASE x1 = x2
	private String equalLatitudesSpecialCase() {
		if (y2.doubleValue() < y1.doubleValue()) {
			BigDecimal temp = y2;
			y2 = y1;
			y1 = temp;
		}

		// latitude change [deg]: latDeg = distanceTotal / latUnit
		BigDecimal latDeg = distanceTotal.divide(latUnit, context); // latUnit will never be zero

		// longitude change [deg]: lonDeg = distanceTotal / lonUnit
		BigDecimal lonDeg = distanceTotal.divide(lonUnit, context); // lonUnit will never be zero

		// POLYGONE POINTS
		BigDecimal pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4;
		// pX1 = x1 + latDeg
		// pY1 = y1 - lonDeg
		pX1 = x1.subtract(latDeg, context);
		pY1 = y1.subtract(lonDeg, context);

		// pX2 = x1 - latDeg
		// pY2 = y1 - lonDeg
		pX2 = x1.add(latDeg, context);
		pY2 = y1.subtract(lonDeg, context);

		// pX3 = x2 + latDeg
		// pY3 = y2 + lonDeg
		pX3 = x2.add(latDeg, context);
		pY3 = y2.add(lonDeg, context);

		// pX4 = x2 - latDeg
		// pY4 = y2 + lonDeg
		pX4 = x2.subtract(latDeg, context);
		pY4 = y2.add(lonDeg, context);

		return formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4);
	}

	// CASE y1 = y2
	private String equalLongitudesSpecialCase() {
		if (x2.doubleValue() < x1.doubleValue()) {
			BigDecimal temp = x2;
			x2 = x1;
			x1 = temp;
		}

		// latitude change [deg]: latDeg = distanceTotal / latUnit
		BigDecimal latDeg = distanceTotal.divide(latUnit, context); // latUnit will never be zero

		// longitude change [deg]: lonDeg = distanceTotal / lonUnit
		BigDecimal lonDeg = distanceTotal.divide(lonUnit, context); // lonUnit will never be zero

		// POLYGONE POINTS
		BigDecimal pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4;
		// pX1 = x1 - latDeg
		// pY1 = y1 + lonDeg
		pX1 = x1.subtract(latDeg, context);
		pY1 = y1.subtract(lonDeg, context);

		// pX2 = x1 - latDeg
		// pY2 = y1 - lonDeg
		pX2 = x1.subtract(latDeg, context);
		pY2 = y1.add(lonDeg, context);

		// pX3 = x2 + latDeg
		// pY3 = y2 + lonDeg
		pX3 = x2.add(latDeg, context);
		pY3 = y2.add(lonDeg, context);

		// pX4 = x2 + latDeg
		// pY4 = y2 - lonDeg
		pX4 = x2.add(latDeg, context);
		pY4 = y2.subtract(lonDeg, context);

		return formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4);
	}

	// CASE x1 != x2 && y1 != y2
	private String basicCase() {
		// slope of acline segment: slope = (y2 - y1) / (x2 - x1)
		BigDecimal slope = y2.subtract(y1, context).divide(x2.subtract(x1, context), context); // x2 and x1 will not be
																								// equal
		Boolean isSharp = slope.compareTo(BigDecimal.valueOf(0).setScale(prec, mode)) > 0;
		// puts coordinates in right order
		correctCoordOrder(isSharp);
		// extends line for distanceTotal length at each end
		extendLine(slope);

		return createPolygonStr(slope);
	}

	private void correctCoordOrder(Boolean isSharp) {
		Boolean correctionNeeded = false;

		if (x1.compareTo(x2) > 0) {
			correctionNeeded = true;
		}

		if (isSharp && y1.compareTo(y2) > 0) {
			correctionNeeded = true;
		}

		if (!isSharp && y1.compareTo(y2) < 0) {
			correctionNeeded = true;
		}

		if (correctionNeeded) {
			BigDecimal temp = x1;
			x1 = x2;
			x2 = temp;

			temp = y1;
			y1 = y2;
			y2 = temp;
		}
	}

	// extends the line for distanceTotal at each end
	private void extendLine(BigDecimal slope) {
		// angle of line to x-axis [rad]: pAngle = arcus tangent(pSlope)
		BigDecimal angle = BigDecimal.valueOf(Math.atan(slope.doubleValue())).setScale(prec, mode);
		if (angle.doubleValue() == 0 || ((Double) angle.doubleValue()).isNaN()) {
			throw new ValueExprEvaluationException("perpendicular angle: " + angle.doubleValue());
		}

		// X and Y components of total distance [m]:
		// distanceY = distanceTotal * sin(angle)
		BigDecimal angleSin = BigDecimal.valueOf(Math.sin(angle.doubleValue())).setScale(prec, mode);
		BigDecimal distanceY = distanceTotal.multiply(angleSin, context);
		// distanceX = distanceY / pSlope
		BigDecimal distanceX = distanceY.divide(slope, context); // slope will not be zero if latitudes are not equal

		// latitude change [deg]
		BigDecimal latDeg = distanceX.divide(latUnit, context); // latUnit will never be zero
		// longitude change [deg]
		BigDecimal lonDeg = distanceY.divide(lonUnit, context); // lonUnit will never be zero

		// Boolean isSharp = slope.compareTo(BigDecimal.valueOf(0).setScale(prec, mode))
		// > 0;
		// int exponent = 0;
		// if (isSharp) {
		// exponent = 1;
		// }
		// multiplier = (-1)^exponent
		// BigDecimal multiplier = BigDecimal.valueOf(Math.pow(1,
		// exponent)).setScale(prec, mode);
		// BigDecimal multipliedLonDeg = lonDeg.multiply(multiplier, context);

		// x1 = x1 - latDeg
		// y1 = y1 - lonDeg
		x1 = x1.subtract(latDeg, context);
		y1 = y1.subtract(lonDeg, context);

		// x2 = x2 + latDeg
		// y2 = y2 + lonDeg
		x2 = x2.add(latDeg, context);
		y2 = y2.add(lonDeg, context);
	}

	private String createPolygonStr(BigDecimal slope) {
		// slope of perpendicular line: pSlope = - 1 / Slope
		BigDecimal pSlope = BigDecimal.valueOf(-1).setScale(prec, mode).divide(slope, context); // slope will not be
																								// zero if longitudes
																								// are not equal

		// angle of perpendicular line to x-axis [rad]: pAngle = arcus tangent(pSlope)
		BigDecimal pAngle = BigDecimal.valueOf(Math.atan(pSlope.doubleValue())).setScale(prec, mode);
		if (pAngle.doubleValue() == 0 || ((Double) pAngle.doubleValue()).isNaN()) {
			throw new ValueExprEvaluationException("perpendicular angle: " + pAngle.doubleValue());
		}

		// X and Y components of total distance [m]:
		// distanceY = distanceTotal * sin(angle)
		BigDecimal angleSin = BigDecimal.valueOf(Math.sin(pAngle.doubleValue())).setScale(prec, mode);
		BigDecimal distanceY = distanceTotal.multiply(angleSin, context);
		// distanceX = distanceY / pSlope
		BigDecimal distanceX = distanceY.divide(pSlope, context); // slope will not be zero if latitudes are not equal

		// latitude change [deg]
		BigDecimal latDeg = distanceX.divide(latUnit, context); // latUnit will never be zero
		// longitude change [deg]
		BigDecimal lonDeg = distanceY.divide(lonUnit, context); // lonUnit will never be zero

		BigDecimal pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4;
		// pX1 = x1 - latDeg
		// pY1 = y1 - lonDeg
		pX1 = x1.subtract(latDeg, context);
		pY1 = y1.subtract(lonDeg, context);

		// pX2 = x1 + latDeg
		// pY2 = y1 + lonDeg
		pX2 = x1.add(latDeg, context);
		pY2 = y1.add(lonDeg, context);

		// pX3 = x2 + latDeg
		// pY3 = y2 + lonDeg
		pX3 = x2.add(latDeg, context);
		pY3 = y2.add(lonDeg, context);

		// pX4 = x2 - latDeg
		// pY4 = y2 - lonDeg
		pX4 = x2.subtract(latDeg, context);
		pY4 = y2.subtract(lonDeg, context);

		return formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4);
	}
}

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
	private final BigDecimal latUnit = BigDecimal.valueOf(111200.0).setScale(prec, mode); // consistant
	// [m / deg]
	private final BigDecimal lonUnit = BigDecimal.valueOf(87620.0).setScale(prec, mode); // California zone

	private BigDecimal x1;
	private BigDecimal y1;
	private BigDecimal x2;
	private BigDecimal y2;
	private BigDecimal distanceTotal;

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

		this.x1 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("x1"))).setScale(prec, mode);
		this.y1 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("y1"))).setScale(prec, mode);
		this.x2 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("x2"))).setScale(prec, mode);
		this.y2 = BigDecimal.valueOf(Double.parseDouble(wktLinestringMatcher.group("y2"))).setScale(prec, mode);
		if (x2.compareTo(x1) == 0 && y2.compareTo(y1) == 0) {
			throw new ValueExprEvaluationException(
					"Invalid LINESTRING definition: x1 equals x2 and y1 equals y2. " + "x1: " + x1.doubleValue()
							+ " y1: " + y1.doubleValue() + "| x2: " + x1.doubleValue() + " y2: " + y1.doubleValue());
		}
		this.distanceTotal = BigDecimal.valueOf(distanceArg).setScale(prec, mode);

		String lineStr = extendLine();
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
	private String extendLine() {
		String strResult;

		if (this.x2.compareTo(this.x1) == 0) {
			strResult = equalLongitudesSpecialCase();

		} else if (this.y2.compareTo(this.y1) == 0) {
			strResult = equalLatitudesSpecialCase();

		} else {
			strResult = basicCase();
		}

		return strResult;
	}

	// CASE x1 = x2
	private String equalLongitudesSpecialCase() {
		// correcting the order
		if (y2.doubleValue() < y1.doubleValue()) {
			BigDecimal temp = y2;
			y2 = y1;
			y1 = temp;
		}

		// TODO: UMT conversion
		// latitude change [deg]: latDeg = distanceTotal / latUnit
		BigDecimal latDeg = distanceTotal.divide(latUnit, context); // latUnit will never be zero

		// y1 = y1 - latDeg
		y1 = y1.subtract(latDeg, context);
		// y2 = y2 + latDeg
		y2 = y2.add(latDeg, context);

		return formatLineStr(x1, y1, x2, y2);
	}

	// CASE y1 = y2
	private String equalLatitudesSpecialCase() {
		// correcting the order
		if (x2.doubleValue() < x1.doubleValue()) {
			BigDecimal temp = x2;
			x2 = x1;
			x1 = temp;
		}

		// TODO: UMT conversion
		// longitude change [deg]: lonDeg = distanceTotal / lonUnit
		BigDecimal lonDeg = distanceTotal.divide(lonUnit, context); // lonUnit will never be zero

		// x1 = x1 - lonDeg
		x1 = x1.subtract(lonDeg, context);
		// x2 = x2 - lonDeg
		x2 = x2.add(lonDeg, context);

		return formatLineStr(x1, y1, x2, y2);
	}

	// CASE x1 != x2 && y1 != y2
	private String basicCase() {
		// slope of acline segment: slope = (y2 - y1) / (x2 - x1)
		BigDecimal slope = y2.subtract(y1, context).divide(x2.subtract(x1, context), context); // x2 and x1 will not be
																								// equal

		// angle of line to x-axis [rad]: angle = arcus tangent(slope)
		BigDecimal angle = BigDecimal.valueOf(Math.atan(slope.doubleValue())).setScale(prec, mode);
		if (angle.doubleValue() == 0 || ((Double) angle.doubleValue()).isNaN()) {
			throw new ValueExprEvaluationException("angle: " + angle.doubleValue());
		}

		Boolean isSharp = slope.compareTo(BigDecimal.valueOf(0).setScale(prec, mode)) > 0;
		// puts coordinates in right order
		correctCoordOrder(isSharp);

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

		if (latDeg.compareTo(BigDecimal.valueOf(0).setScale(prec, mode)) < 0) {
			// latDeg < 0
			latDeg = latDeg.multiply(BigDecimal.valueOf(-1).setScale(prec, mode));
		}

		if (lonDeg.compareTo(BigDecimal.valueOf(0).setScale(prec, mode)) < 0) {
			// lonDeg < 0
			lonDeg = lonDeg.multiply(BigDecimal.valueOf(-1).setScale(prec, mode));
		}

		if (isSharp) {
			// x1 = x1 - lonDeg
			// y1 = y1 - latDeg
			x1 = x1.subtract(lonDeg, context);
			y1 = y1.subtract(latDeg, context);

			// x2 = x2 + lonDeg
			// y2 = y2 + latDeg
			x2 = x2.add(lonDeg, context);
			y2 = y2.add(latDeg, context);

		} else {
			// x1 = x1 - lonDeg
			// y1 = y1 + latDeg
			x1 = x1.subtract(lonDeg, context);
			y1 = y1.add(latDeg, context);

			// x2 = x2 + lonDeg
			// y2 = y2 - latDeg
			x2 = x2.add(lonDeg, context);
			y2 = y2.subtract(latDeg, context);
		}

		return formatLineStr(x1, y1, x2, y2);
	}

	private void correctCoordOrder(Boolean isSharp) {
		Boolean correctionNeeded = false;

		if (isSharp && x1.compareTo(x2) > 0) {
			// P1(2, 2) P2(1, 1) or P1(2, 1) P2(1, 2)
			correctionNeeded = true;
		}

		if (isSharp && y1.compareTo(y2) > 0) {
			// P1(2, 2) P2(1, 1)
			correctionNeeded = true;
		}

		if (!isSharp && y1.compareTo(y2) < 0) {
			// P1(2, 1) P2(1, 2)
			correctionNeeded = true;
		}

		if (correctionNeeded) {
			BigDecimal tempX = x1;
			x1 = x2;
			x2 = tempX;

			BigDecimal tempY = y1;
			y1 = y2;
			y2 = tempY;
		}
	}

	// p as in polygonX1, polygonY1, ...
	private String formatLineStr(BigDecimal lX1, BigDecimal lY1, BigDecimal lX2, BigDecimal lY2) {
		return String.format(Locale.US, "POLYGON ((%.15f %.15f, %.15f %.15f, %.15f %.15f, %.15f %.15f, %.15f %.15f))",
				lX1, lY1, lX1, lY1, lX2, lY2, lX2, lY2, lX1, lY1);
	}
}

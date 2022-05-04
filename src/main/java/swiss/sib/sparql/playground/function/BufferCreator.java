package swiss.sib.sparql.playground.function;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;
import org.geotools.geometry.jts.JTS;
import org.opengis.geometry.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class BufferCreator {
	private static final Log logger = LogFactory.getLog(BufferCreator.class);

	private final int prec = 20;
	private final RoundingMode mode = RoundingMode.HALF_EVEN;
	private final MathContext context = new MathContext(prec, mode);

	//[m / deg]
	private final BigDecimal latUnit = BigDecimal.valueOf(111200.0).setScale(prec, mode); // consistant
	//[m / deg]
	private final BigDecimal lonUnit = BigDecimal.valueOf(87620.0).setScale(prec, mode); // California zone

	private BigDecimal x1;
	private BigDecimal y1;
	private BigDecimal x2;
	private BigDecimal y2;
	private BigDecimal distanceTotal;

	public BufferCreator(Double x1, Double y1, Double x2, Double y2, Double distance) {
		this.x1 = BigDecimal.valueOf(x1).setScale(prec, mode);
		this.y1 = BigDecimal.valueOf(y1).setScale(prec, mode);
		this.x2 = BigDecimal.valueOf(x2).setScale(prec, mode);
		this.y2 = BigDecimal.valueOf(y2).setScale(prec, mode);
		if (x2.compareTo(x1) == 0 && y2.compareTo(y1) == 0) {
			throw new ValueExprEvaluationException("Invalid LINESTRING definition: x1 equals x2 and y1 equals y2. " +
				"x1: " + x1.doubleValue() + " y1: " + y1.doubleValue() + "| x2: " + x1.doubleValue() + " y2: " + y1.doubleValue());
		}

		this.distanceTotal = BigDecimal.valueOf(distance).setScale(prec, mode);
	}

	public String create() {
		String strResult = "POINT EMPTY";

		try {
			if (x2.compareTo(x1) == 0) {
				strResult = equalLongitudesSpecialCase();

			} else if (y2.compareTo(y1) == 0) {
				strResult = equalLatitudesSpecialCase();

			} else {
				strResult = basicCase();
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return strResult;
	}

	private String formatPolygonStr(BigDecimal pX1, BigDecimal pY1, BigDecimal pX2, BigDecimal pY2, BigDecimal pX3,
			BigDecimal pY3, BigDecimal pX4, BigDecimal pY4) {

		return String.format(Locale.US, "POLYGON ((%.15f %.15f, %.15f %.15f, %.15f %.15f, %.15f %.15f, %.15f %.15f))",
				pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4, pX1, pY1);
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
		// longitude change [deg]: lonDeg = distanceTotal / lonUnit
		BigDecimal lonDeg = distanceTotal.divide(lonUnit, context); // lonUnit will never be zero

		// POLYGONE POINTS

		// pX1 = x1 - lonDeg
		// pY1 = y1 - latDeg
		BigDecimal pX1 = x1.subtract(lonDeg, context);
		BigDecimal pY1 = y1.subtract(latDeg, context);

		// pX2 = x1 + lonDeg
		// pY2 = y1 - latDeg
		BigDecimal pX2 = x1.add(lonDeg, context);
		BigDecimal pY2 = y1.subtract(latDeg, context);

		// pX3 = x2 + lonDeg
		// pY3 = y2 + latDeg
		BigDecimal pX3 = x2.add(lonDeg, context);
		BigDecimal pY3 = y2.add(latDeg, context);

		// pX4 = x2 - lonDeg
		// pY4 = y2 + latDeg
		BigDecimal pX4 = x2.subtract(lonDeg, context);
		BigDecimal pY4 = y2.add(latDeg, context);

		return formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4);
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
		// latitude change [deg]: latDeg = distanceTotal / latUnit
		BigDecimal latDeg = distanceTotal.divide(latUnit, context); // latUnit will never be zero
		// longitude change [deg]: lonDeg = distanceTotal / lonUnit
		BigDecimal lonDeg = distanceTotal.divide(lonUnit, context); // lonUnit will never be zero

		// POLYGONE POINTS

		// pX1 = x1 - lonDeg
		// pY1 = y1 - latDeg
		BigDecimal pX1 = x1.subtract(lonDeg, context);
		BigDecimal pY1 = y1.subtract(latDeg, context);

		// pX2 = x1 - lonDeg
		// pY2 = y1 + latDeg
		BigDecimal pX2 = x1.subtract(lonDeg, context);
		BigDecimal pY2 = y1.add(latDeg, context);

		// pX3 = x2 + lonDeg
		// pY3 = y2 + latDeg
		BigDecimal pX3 = x2.add(lonDeg, context);
		BigDecimal pY3 = y2.add(latDeg, context);

		// pX4 = x2 + lonDeg
		// pY4 = y2 - latDeg
		BigDecimal pX4 = x2.add(lonDeg, context);
		BigDecimal pY4 = y2.subtract(latDeg, context);

		return formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4);
	}

	// CASE x1 != x2 && y1 != y2
	private String basicCase() throws Exception {
		// slope of acline segment: slope = (y2 - y1) / (x2 - x1)
		BigDecimal slope = y2.subtract(y1, context).divide(x2.subtract(x1, context), context); // x2 and x1 will not be equal
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
	private void extendLine(BigDecimal slope) throws NoSuchAuthorityCodeException, FactoryException, TransformException {
		// angle of line to x-axis [rad]: angle = arcus tangent(slope)
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

		// TODO: UMT conversion
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:25832");
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, false);
		Geometry targetGeometry = (Geometry) JTS.transform(new Envelope(), transform);
		
		// longitude change [deg]
		BigDecimal lonDeg = distanceX.divide(lonUnit, context); // lonUnit will never be zero
		// latitude change [deg]
		BigDecimal latDeg = distanceY.divide(latUnit, context); // latUnit will never be zero
		
		// x1 = x1 - lonDeg
		// y1 = y1 - latDeg
		x1 = x1.subtract(lonDeg, context);
		y1 = y1.subtract(latDeg, context);

		// x2 = x2 + lonDeg
		// y2 = y2 + latDeg
		x2 = x2.add(lonDeg, context);
		y2 = y2.add(latDeg, context);
	}

	private String createPolygonStr(BigDecimal slope) throws Exception {
		// slope of perpendicular line: pSlope = - 1 / Slope
		BigDecimal pSlope = BigDecimal.valueOf(-1).setScale(prec, mode).divide(slope, context); // slope will not be zero if latitudes are not equal

		// angle of perpendicular line to x-axis [rad]: pAngle = arcus tangent(pSlope)
		BigDecimal pAngle = BigDecimal.valueOf(Math.atan(pSlope.doubleValue())).setScale(prec, mode);
		if (pAngle.doubleValue() == 0 || ((Double) pAngle.doubleValue()).isNaN()) {
			throw new ValueExprEvaluationException("perpendicular angle: " + pAngle.doubleValue());
		}

		// X and Y components of total distance [m]:
		// distanceY = distanceTotal * sin(pAngle)
		BigDecimal angleSin = BigDecimal.valueOf(Math.sin(pAngle.doubleValue())).setScale(prec, mode);
		BigDecimal distanceY = distanceTotal.multiply(angleSin, context);
		// distanceX = distanceY / pSlope
		BigDecimal distanceX = distanceY.divide(pSlope, context); // slope will not be zero if latitudes are not equal

		// TODO: UMT conversion
		// longitude change [deg]
		BigDecimal lonDeg = distanceX.divide(lonUnit, context); // lonUnit will never be zero
		// latitude change [deg]
		BigDecimal latDeg = distanceY.divide(latUnit, context); // latUnit will never be zero
		
		// POLYGONE POINTS
		BigDecimal pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4;

		// pX1 = x1 - lonDeg
		// pY1 = y1 - latDeg
		pX1 = x1.subtract(lonDeg, context);
		pY1 = y1.subtract(latDeg, context);

		// pX1 = x1 + lonDeg
		// pY1 = y1 + latDeg
		pX2 = x1.add(lonDeg, context);
		pY2 = y1.add(latDeg, context);

		// pX1 = x2 + lonDeg
		// pY1 = y2 + latDeg
		pX3 = x2.add(lonDeg, context);
		pY3 = y2.add(latDeg, context);

		// pX1 = x2 - lonDeg
		// pY1 = y2 - latDeg
		pX4 = x2.subtract(lonDeg, context);
		pY4 = y2.subtract(latDeg, context);

		return formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4);
	}
}

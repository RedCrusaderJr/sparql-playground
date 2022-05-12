package swiss.sib.sparql.playground.function.createBuffer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;

public class BufferCreatorHelper {

    // x2 and x1 must not not be equal
    public static BigDecimal calculateSlope(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2, MathContext context) {
        // slope of acline segment: slope = (y2 - y1) / (x2 - x1)
        return y2.subtract(y1, context).divide(x2.subtract(x1, context), context); 
    }

    // slope of perpendicular line: pSlope = - 1 / Slope
    // y2 and y1 must not not be equal
    public static BigDecimal calculatePerpendicularSlope(BigDecimal slope, MathContext context) {
        return BigDecimal.valueOf(-1).setScale(context.getPrecision(), context.getRoundingMode()).divide(slope, context);
    }

    public static BigDecimal calculateVerticalOffset(BigDecimal slope, BigDecimal distanceTotal, MathContext context) {
        int precision = context.getPrecision();
        RoundingMode roundingMode = context.getRoundingMode();

		// angle [rad]: angle = arcus tangent(slope)
		BigDecimal angle = BigDecimal.valueOf(Math.atan(slope.doubleValue())).setScale(precision, roundingMode);
		if (angle.doubleValue() == 0 || ((Double) angle.doubleValue()).isNaN()) {
			throw new ValueExprEvaluationException("angle: " + angle.doubleValue());
		}

		// verticalOffeset = distanceTotal * sin(pAngle)
		BigDecimal angleSin = BigDecimal.valueOf(Math.sin(angle.doubleValue())).setScale(precision, roundingMode);
        return distanceTotal.multiply(angleSin, context);
    }

    // perpendicularSlope must not be zero (latitudes/northings must not equal)
    public static BigDecimal calculateHorizontalOffset(BigDecimal verticalOffeset, BigDecimal slope, MathContext context) {
        // horizontalOffset = verticalOffeset / slope
		return verticalOffeset.divide(slope, context); 
    }
}

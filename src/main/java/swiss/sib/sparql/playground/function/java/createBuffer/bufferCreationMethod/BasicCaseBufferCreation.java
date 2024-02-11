package swiss.sib.sparql.playground.function.java.createBuffer.bufferCreationMethod;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import swiss.sib.sparql.playground.function.java.createBuffer.BufferCreatorHelper;

public class BasicCaseBufferCreation extends BufferCreationMethod {

    public BasicCaseBufferCreation(
        Coordinate utmPoint1, 
        Coordinate utmPoint2, 
        Double distance,   
        GeometryFactory geometryFactory) {
            
        super(utmPoint1, utmPoint2, distance, geometryFactory);
    }

    @Override
    // CASE x1 != x2 && y1 != y2
	public String createBufferPolygonString() throws Exception {
		BigDecimal slope = BufferCreatorHelper.calculateSlope(utmEasting1, utmNorthing1, utmEasting2, utmNorthing2, context);
		
		Boolean isSharp = slope.compareTo(BigDecimal.valueOf(0).setScale(precision, mode)) > 0;
		correctCoordOrder(isSharp);

		extendLine(slope);
		return createPolygonStr(slope);
	}

	// puts coordinates in right order
	private void correctCoordOrder(Boolean isSharp) {
		Boolean correctionNeeded = false;

		int eastingComaprison = utmEasting2.compareTo(utmEasting1);
		int northingComparison = utmNorthing2.compareTo(utmNorthing1);

		if (eastingComaprison < 0) {
			correctionNeeded = true;
		}

		if (isSharp && northingComparison < 0) {
			correctionNeeded = true;
		}

		if (!isSharp && northingComparison > 0) {
			correctionNeeded = true;
		}

		if (correctionNeeded) {
			BigDecimal temp = utmEasting1;
			utmEasting1 = utmEasting2;
			utmEasting2 = temp;

			temp = utmNorthing1;
			utmNorthing1 = utmNorthing2;
			utmNorthing2 = temp;
		}
	}

	// extends the line for distanceTotal at each end
	private void extendLine(BigDecimal slope) throws MismatchedDimensionException, TransformException, FactoryException {
		BigDecimal verticalOffset = BufferCreatorHelper.calculateVerticalOffset(slope, distanceTotal, context);
		BigDecimal horizontalOffset = BufferCreatorHelper.calculateHorizontalOffset(verticalOffset, slope, context);
		
		// utmEasting1 = utmEasting1 - horizontalOffset
		// utmNorthing1 = utmNorthing1 - verticalOffset
		utmEasting1 = utmEasting1.subtract(horizontalOffset, context);
		utmNorthing1 = utmNorthing1.subtract(verticalOffset, context);

		// utmEasting2 = utmEasting2 + horizontalOffset
		// utmNorthing2 = utmNorthing2 + verticalOffset
		utmEasting2 = utmEasting2.add(horizontalOffset, context);
		utmNorthing2 = utmNorthing2.add(verticalOffset, context);
	}

	private String createPolygonStr(BigDecimal slope) throws Exception {
        BigDecimal pSlope = BufferCreatorHelper.calculatePerpendicularSlope(slope, context);
		BigDecimal verticalOffset = BufferCreatorHelper.calculateVerticalOffset(pSlope, distanceTotal, context);
		BigDecimal horizontalOffset = BufferCreatorHelper.calculateHorizontalOffset(verticalOffset, pSlope, context);

		// point1.x = utmEasting1 - horizontalOffset
		// point1.y = utmNorthing1 - verticalOffset
		Coordinate point1 = new Coordinate(utmEasting1.subtract(horizontalOffset, context).doubleValue(), utmNorthing1.subtract(verticalOffset, context).doubleValue());

		// point2.x = utmEasting1 + horizontalOffset
		// point2.y = utmNorthing1 + verticalOffset
		Coordinate point2 = new Coordinate(utmEasting1.add(horizontalOffset, context).doubleValue(), utmNorthing1.add(verticalOffset, context).doubleValue());

		// point3.x = utmEasting2 + horizontalOffset
		// point3.y = utmNorthing2 + verticalOffset
		Coordinate point3 = new Coordinate(utmEasting2.add(horizontalOffset, context).doubleValue(), utmNorthing2.add(verticalOffset, context).doubleValue());

		// point4.x = utmEasting2 - horizontalOffset
		// point4.y = utmNorthing2 - verticalOffset
		Coordinate point4 = new Coordinate(utmEasting2.subtract(horizontalOffset, context).doubleValue(), utmNorthing2.subtract(verticalOffset, context).doubleValue());

		return formatPolygonStr(point1, point2, point3, point4);
	}
}

package swiss.sib.sparql.playground.function.java.createBuffer.bufferCreationMethod;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class EqualEastingsBufferCreation extends BufferCreationMethod {

    public EqualEastingsBufferCreation(
        Coordinate utmPoint1, 
        Coordinate utmPoint2, 
        Double distance,   
        GeometryFactory geometryFactory) {
            
        super(utmPoint1, utmPoint2, distance, geometryFactory);
    }

    @Override
    // CASE x1 = x2
    public String createBufferPolygonString() throws Exception {
        // correcting the order
		if (utmNorthing2.compareTo(utmNorthing1) < 0) {
			BigDecimal temp = utmNorthing2;
			utmNorthing2 = utmNorthing1;
			utmNorthing1 = temp;
		}

		// POLYGONE POINTS

		// point1.x = utmEasting1 - distanceTotal
		// point1.y = utmNorthing1 - distanceTotal
		Coordinate point1 = new Coordinate(utmEasting1.subtract(distanceTotal, context).doubleValue(), utmNorthing1.subtract(distanceTotal, context).doubleValue());

		// point2.x = utmEasting1 + distanceTotal
		// point2.y = utmNorthing1 - distanceTotal
		Coordinate point2 = new Coordinate(utmEasting1.add(distanceTotal, context).doubleValue(), utmNorthing1.subtract(distanceTotal, context).doubleValue());

		// point3.x = utmEasting2 + distanceTotal
		// point3.y = utmNorthing2 + distanceTotal
		Coordinate point3 = new Coordinate(utmEasting2.add(distanceTotal, context).doubleValue(), utmNorthing2.add(distanceTotal, context).doubleValue());

		// point4.x = utmEasting2 - distanceTotal
		// point4.y = utmNorthing2 + distanceTotal
		Coordinate point4 = new Coordinate(utmEasting2.subtract(distanceTotal, context).doubleValue(), utmNorthing2.add(distanceTotal, context).doubleValue());

		return super.formatPolygonStr(point1, point2, point3, point4);
    }
}

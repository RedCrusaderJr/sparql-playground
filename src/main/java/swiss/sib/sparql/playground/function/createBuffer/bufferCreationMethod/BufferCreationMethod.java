package swiss.sib.sparql.playground.function.createBuffer.bufferCreationMethod;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import swiss.sib.sparql.playground.function.CrsTransformator;

public abstract class BufferCreationMethod {
    protected final int precision = 8;
	protected final RoundingMode mode = RoundingMode.HALF_EVEN;
	protected final MathContext context = new MathContext(precision, mode);

    protected GeometryFactory geometryFactory;
	protected CrsTransformator crsTransformator = CrsTransformator.getInstance();
    protected BigDecimal utmEasting1, utmNorthing1, utmEasting2, utmNorthing2, distanceTotal;

    public BufferCreationMethod(
        Coordinate utmPoint1, 
        Coordinate utmPoint2, 
        Double distance,   
        GeometryFactory geometryFactory) {
           
        this.utmEasting1 = BigDecimal.valueOf(utmPoint1.x).setScale(precision, mode);
        this.utmNorthing1 = BigDecimal.valueOf(utmPoint1.y).setScale(precision, mode);
        this.utmEasting2 = BigDecimal.valueOf(utmPoint2.x).setScale(precision, mode);
        this.utmNorthing2 = BigDecimal.valueOf(utmPoint2.y).setScale(precision, mode);
        this.distanceTotal = BigDecimal.valueOf(distance).setScale(precision, mode);
            
        this.geometryFactory = geometryFactory;
    }

    public abstract String createBufferPolygonString() throws Exception;

    protected String formatPolygonStr(Coordinate utmPoint1, Coordinate utmPoint2, Coordinate utmPoint3, Coordinate utmPoint4) throws MismatchedDimensionException, TransformException {
		Coordinate wgsPoint1 = ((Point)crsTransformator.fromUtmZone10NToWgs84(geometryFactory.createPoint(new Coordinate(utmPoint1.x, utmPoint1.y)))).getCoordinate();
		Coordinate wgsPoint2 = ((Point)crsTransformator.fromUtmZone10NToWgs84(geometryFactory.createPoint(new Coordinate(utmPoint2.x, utmPoint2.y)))).getCoordinate();
		Coordinate wgsPoint3 = ((Point)crsTransformator.fromUtmZone10NToWgs84(geometryFactory.createPoint(new Coordinate(utmPoint3.x, utmPoint3.y)))).getCoordinate();
		Coordinate wgsPoint4 = ((Point)crsTransformator.fromUtmZone10NToWgs84(geometryFactory.createPoint(new Coordinate(utmPoint4.x, utmPoint4.y)))).getCoordinate();

		return String.format(Locale.US, "POLYGON ((%.10f %.10f, %.10f %.10f, %.10f %.10f, %.10f %.10f, %.10f %.10f))", 
            wgsPoint1.y, wgsPoint1.x, wgsPoint2.y, wgsPoint2.x, wgsPoint3.y, wgsPoint3.x, wgsPoint4.y, wgsPoint4.x, wgsPoint1.y, wgsPoint1.x);
	}
}

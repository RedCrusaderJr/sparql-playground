package swiss.sib.sparql.playground.function;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class CrsTransformator {
    private MathTransform wgs84ToUtmZone10N;
    private MathTransform utmZone10NToWgs84;

    private static CrsTransformator instance;
    public static CrsTransformator getInstance() {
        if(instance == null) {
            synchronized (CrsTransformator.class) {
                if(instance == null) {
                    instance = new CrsTransformator();
                }
            }
        }

        return instance;
    }

    private CrsTransformator() {
        try {
            CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");
            CoordinateReferenceSystem utmZone10N = CRS.decode("EPSG:26910");

            wgs84ToUtmZone10N = CRS.findMathTransform(wgs84, utmZone10N, false);
            utmZone10NToWgs84 = CRS.findMathTransform(utmZone10N, wgs84, false);

        } catch (FactoryException e) {
            e.printStackTrace();
        }
    }

    public Geometry fromWgs84ToUtmZone10N(Geometry sourceGeometry) throws MismatchedDimensionException, TransformException {    
        return JTS.transform(sourceGeometry, wgs84ToUtmZone10N);
    }

    public Geometry fromUtmZone10NToWgs84(Geometry sourceGeometry) throws MismatchedDimensionException, TransformException {		
		return JTS.transform(sourceGeometry, utmZone10NToWgs84);
    }
}

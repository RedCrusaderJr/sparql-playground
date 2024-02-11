package swiss.sib.sparql.playground.function.java.createBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import swiss.sib.sparql.playground.function.java.CrsTransformator;
import swiss.sib.sparql.playground.function.java.createBuffer.bufferCreationMethod.BasicCaseBufferCreation;
import swiss.sib.sparql.playground.function.java.createBuffer.bufferCreationMethod.BufferCreationMethod;
import swiss.sib.sparql.playground.function.java.createBuffer.bufferCreationMethod.EqualEastingsBufferCreation;
import swiss.sib.sparql.playground.function.java.createBuffer.bufferCreationMethod.EqualNorthingBufferCreation;

public class BufferCreator {
	private static final Log logger = LogFactory.getLog(BufferCreator.class);
	private final CrsTransformator crsTransformator = CrsTransformator.getInstance();

	private BufferCreationMethod bufferCreationMethod;

	//					 Longitude [-180, 180)	 Latitude [-90, 90] 
	public BufferCreator(Double inputLongitude1, Double inputLatitude1, Double inputLongitude2, Double inputLatitude2, Double distance) throws MismatchedDimensionException, TransformException {
		GeometryFactory geometryFactory = new GeometryFactory();
		Coordinate utmPoint1 = crsTransformator.fromWgs84ToUtmZone10N(geometryFactory.createPoint(new Coordinate(inputLatitude1, inputLongitude1))).getCoordinate();
		Coordinate utmPoint2 = crsTransformator.fromWgs84ToUtmZone10N(geometryFactory.createPoint(new Coordinate(inputLatitude2, inputLongitude2))).getCoordinate();
		
		this.bufferCreationMethod = getBufferCreationMethod(utmPoint1, utmPoint2, distance, geometryFactory);
	}

	private BufferCreationMethod getBufferCreationMethod(Coordinate utmPoint1, Coordinate utmPoint2, double distance, GeometryFactory geometryFactory) {
		int eastingComaprison = Double.compare(utmPoint2.x, utmPoint1.x);
		int northingComparison = Double.compare(utmPoint2.y, utmPoint1.y);

		if (eastingComaprison == 0 && northingComparison == 0) {
			throw new ValueExprEvaluationException("Invalid LINESTRING definition: utmEasting1 equals utmEasting2 and utmNorthing1 equals utmNorthing2. " +
				"utmEasting1: " + utmPoint1.x + " " +
				"utmNorthing1: " + utmPoint1.y + " | " +
				"utmEasting1: " + utmPoint2.x + " " + 
				"utmNorthing1: " + utmPoint2.y);
		}

		if (eastingComaprison == 0) {
			return new EqualEastingsBufferCreation(utmPoint1, utmPoint2, distance, geometryFactory);
		}

		if (northingComparison == 0) {
			return new EqualNorthingBufferCreation(utmPoint1, utmPoint2, distance, geometryFactory);
		}

		return new BasicCaseBufferCreation(utmPoint1, utmPoint2, distance, geometryFactory);
	}

	public String create() {
		String strResult = "POINT EMPTY";

		try {
			strResult = this.bufferCreationMethod.createBufferPolygonString();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return strResult;
	}
}


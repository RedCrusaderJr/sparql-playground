/********************************************************************************************
*
*	8.7 Non-topological Query Functions
*
*	This clause defines SPARQL functions for performing non-topological spatial operations. 
* 
* 	Req 19 	
*	Implementations shall support geof:distance, geof:buffer, geof:convexHull,
*	geof:intersection, geof:union, geof:difference,	geof:symDifference, geof:envelope
*	and geof:boundary as SPARQL extension functions, consistent with the definitions of
*	the corresponding functions (distance, buffer, convexHull, intersection, difference,
*	symDifference, envelope and boundary respectively) in Simple Features [ISO 19125-1].
*
*
*	8.7.1 Function: geof:distance
*
*	geof:distance (geom1: ogc:geomLiteral, geom2: ogc:geomLiteral, units: xsd:anyURI): xsd:double
*	
*	Returns the shortest distance in units between any two Points in the two
*	geometric objects as calculated in the spatial reference system of geom1.
*
********************************************************************************************/

import { TurfHelper } from '/turf-helper.mjs';
const TH = new TurfHelper();
const TURF = require('/turf.min.js');

export default function distanceFunction(wktGeom1, wktGeom2, ogcUnitUri) {
	if(!wktGeom1 || !wktGeom2) throw new Error("geof:distance -> One of arguments was null/undefined -> wktGeom1: " + wktGeom1 + ", wktGeom2: " + wktGeom2);

	try {
		if (!ogcUnitUri) ogcUnitUri = TH.OGC_METRE;

		let turfGeom1 = TH.convertWktToTurfGeom(TH.getWktPart(wktGeom1));
		let turfGeom2 = TH.convertWktToTurfGeom(TH.getWktPart(wktGeom2));
		let turfUnit = TH.getTurfUnitMappedOn(ogcUnitUri);
		
		let shortestDistance = calculateShortestDistance(turfGeom1, turfGeom2, { units: turfUnit.GetName() })
		return shortestDistance * turfUnit.GetMultiplicator();
			
	} catch (error) {
		return "[ERROR] " + error;
	}
}

function calculateShortestDistance(turfGeom1, turfGeom2, units) {
	// POINT - POINT
	if (turfGeom1.geometry.type == TH.TURF_POINT && turfGeom2.geometry.type == TH.TURF_POINT) {
		return pointToPointDistance(turfGeom1, turfGeom2, units) 
	}

	// POINT - LINE
	if (turfGeom1.geometry.type == TH.TURF_POINT && turfGeom2.geometry.type == TH.TURF_LINESTRING) { 
		return pointToLineDistance(turfGeom1, turfGeom2, units);
	}
	if (turfGeom1.geometry.type == TH.TURF_LINESTRING && turfGeom2.geometry.type == TH.TURF_POINT) {
		return pointToLineDistance(turfGeom2, turfGeom1, units);
	}

	// POINT - POLYGON
	if (turfGeom1.geometry.type == TH.TURF_POINT && turfGeom2.geometry.type == TH.TURF_POLYGON) {
		return pointToPolygonDistance(turfGeom1, turfGeom2, units);
	}
	if (turfGeom1.geometry.type == TH.TURF_POLYGON && turfGeom2.geometry.type == TH.TURF_POINT) {
		return pointToPolygonDistance(turfGeom2, turfGeom1, units);
	}

	// LINE - LINE
	if (turfGeom1.geometry.type == TH.TURF_LINESTRING && turfGeom2.geometry.type == TH.TURF_LINESTRING) {
		return lineToLineDistance(turfGeom1, turfGeom2, units) 
	}

	// LINE - POLYGON
	if (turfGeom1.geometry.type == TH.TURF_LINESTRING && turfGeom2.geometry.type == TH.TURF_POLYGON) {
		return lineToPolygonDistance(turfGeom1, turfGeom2, units) 
	}
	if (turfGeom1.geometry.type == TH.TURF_POLYGON && turfGeom2.geometry.type == TH.TURF_LINESTRING) {
		return lineToPolygonDistance(turfGeom2, turfGeom1, units) 
	}

	// POLYGON - POLYGON
	if (turfGeom1.geometry.type == TH.TURF_POLYGON && turfGeom2.geometry.type == TH.TURF_POLYGON) {
		return polygonToPolygonDistance(turfGeom1, turfGeom2, units) 
	}

	throw new Error("Unsupported turf geom type combination => turfGeom1 type: " + turfGeom1.geometry.type + ", turfGeom2 type: " + turfGeom2.geometry.type);
}

function pointToPointDistance(point1, point2, units) {
	if (point1.geometry.type != TH.TURF_POINT) throw new Error(WrongGeometryTypeErrorMessage(point1.geometry.type, TH.TURF_POINT));
	if (point2.geometry.type != TH.TURF_POINT) throw new Error(WrongGeometryTypeErrorMessage(point2.geometry.type, TH.TURF_POINT));

	return TURF.distance(point1, point2, units);
}

function pointToLineDistance(point, line, units) {
	if (point.geometry.type != TH.TURF_POINT) throw new Error(WrongGeometryTypeErrorMessage(point.geometry.type, TH.TURF_POINT));
	if (line.geometry.type != TH.TURF_LINESTRING) throw new Error(WrongGeometryTypeErrorMessage(line.geometry.type, TH.TURF_LINESTRING));

	return TURF.pointToLineDistance(point, line, units);	
}

function pointToPolygonDistance(point, polygon, units) {
	if (point.geometry.type != TH.TURF_POINT) throw new Error(WrongGeometryTypeErrorMessage(point.geometry.type, TH.TURF_POINT));
	if (polygon.geometry.type != TH.TURF_POLYGON) throw new Error(WrongGeometryTypeErrorMessage(polygon.geometry.type, TH.TURF_POLYGON));

	let shortestDistance = Number.MAX_VALUE;

	let lines = TH.dividePolygonIntoLines(polygon);
	for (let index in lines) {
		let distance = pointToLineDistance(point, lines[index], units);
		
		if (distance < shortestDistance) {
			shortestDistance = distance;
		}
	}

	return shortestDistance;	
}

function lineToLineDistance(line1, line2, units) {
	if (line1.geometry.type != TH.TURF_LINESTRING) throw new Error(WrongGeometryTypeErrorMessage(line1.geometry.type, TH.TURF_LINESTRING));
	if (line2.geometry.type != TH.TURF_LINESTRING) throw new Error(WrongGeometryTypeErrorMessage(line2.geometry.type, TH.TURF_LINESTRING));

	let shortestDistance = Number.MAX_VALUE;

	let line1Points = TH.divideLineIntoPoints(line1);
	for (let index in line1Points) {
		// MEASURE DISTANCE TO LINE 2
		let distance = pointToLineDistance(line1Points[index], line2, units);
		
		if (distance < shortestDistance) {
			shortestDistance = distance;
		}
	}

	let line2Points = TH.divideLineIntoPoints(line2);
	for (let index in line2Points) {
		// MEASURE DISTANCE TO LINE 1
		let distance = pointToLineDistance(line2Points[index], line1, units);
		
		if (distance < shortestDistance) {
			shortestDistance = distance;
		}
	}

	return shortestDistance;
}

function lineToPolygonDistance(line, polygon, units) {
	if (line.geometry.type != TH.TURF_LINESTRING) throw new Error(WrongGeometryTypeErrorMessage(line.geometry.type, TH.TURF_LINESTRING));
	if (polygon.geometry.type != TH.TURF_POLYGON) throw new Error(WrongGeometryTypeErrorMessage(polygon.geometry.type, TH.TURF_POLYGON));

	let shortestDistance = Number.MAX_VALUE;
	
	let polygonLines = TH.dividePolygonIntoLines(polygon);
	for (let index in polygonLines) {
		// MEASURE DISTANCE TO INPUT LINE
		let distance = lineToLineDistance(polygonLines[index], line, units);
		
		if (distance < shortestDistance) {
			shortestDistance = distance;
		}
	}

	return shortestDistance;
}

function polygonToPolygonDistance(polygon1, polygon2, units) {
	if (polygon1.geometry.type != TH.TURF_POLYGON) throw new Error(WrongGeometryTypeErrorMessage(polygon1.geometry.type, TH.TURF_POLYGON));
	if (polygon2.geometry.type != TH.TURF_POLYGON) throw new Error(WrongGeometryTypeErrorMessage(polygon2.geometry.type, TH.TURF_POLYGON));

	let shortestDistance = Number.MAX_VALUE;

	let polygonLines = TH.dividePolygonIntoLines(polygon1);
	for (let index in polygonLines) {
		// MEASURE DISTANCE TO POLYGON 2
		let distance = lineToPolygonDistance(polygonLines[index], polygon2, units);
		
		if (distance < shortestDistance) {
			shortestDistance = distance;
		}
	}
	
	return shortestDistance;	
}

function WrongGeometryTypeErrorMessage(actualType, expectedType) {
	return "Geometry type: " + actualType + ", Expected: " + expectedType;
}

/********************************************************************************************
*	
*	Turf.js
*
*	distance()
*	Calculates the distance between two points in degrees, radians, miles, or kilometers. 
*	This uses the Haversine formula to account for global curvature.
*
*	Returns
*	number - distance between the two points
*
*
*	pointToLineDistance()
*	Returns the minimum distance between a Point and a LineString , being the distance 
*	from a line the minimum distance between the point and any segment of the LineString.
*
*	Returns
*	number - distance between point and line
*
********************************************************************************************/
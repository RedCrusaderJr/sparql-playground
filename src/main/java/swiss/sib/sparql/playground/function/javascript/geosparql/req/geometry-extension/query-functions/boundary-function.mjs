/**************************************************************************************************************
*
* 	8.7 Non-topological Query Functions
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
*	8.7.9 Function: geof:boundary
*
*	geof:boundary (geom1: ogc:geomLiteral): ogc:geomLiteral
*
*	This function returns the closure of the boundary of geom1. 
*	Calculations are in the spatial reference system of geom1.
*
**************************************************************************************************************/


import { TurfHelper } from '/turf-helper.mjs';
const TH = new TurfHelper();
const TURF = require('/turf.min.js');

export default function boundaryFunction(wktGeom1) {
	if (!wktGeom1) throw new Error("geof:boundary -> One of arguments was null/undefined -> wktGeom1: " + wktGeom1);
	wktGeom1 = "" + wktGeom1;

	try {

		let turfGeom1 = TH.convertWktToTurfGeom(wktGeom1);
	
		const bboxPolygon = TURF.bboxPolygon(TURF.bbox(turfGeom1));
		return TH.convertTurfGeomToWKT(bboxPolygon);
			
	} catch (error) {
		return "[ERROR] " + error;
	}
}

/**************************************************************************************************************
*	
*	Turf.js
*
*	bbox()
*	Takes a set of features, calculates the bbox of all input features, and returns a bounding box.
*
*	Returns
*	BBox - bbox extent in minX, minY, maxX, maxY order
*	
**************************************************************************************************************/
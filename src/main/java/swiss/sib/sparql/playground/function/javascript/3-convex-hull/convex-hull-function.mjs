/********************************************************************************************
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
*	8.7.3 Function: geof:convexHull
*
*	geof:convexHull (geom1: ogc:geomLiteral): ogc:geomLiteral
*
*	This function returns a geometric object that represents all Points in
*	the convex hull of geom1. Calculations are in the spatial reference system of geom1.
*
********************************************************************************************/


import { TurfHelper } from '/turf-helper.mjs';
const TH = new TurfHelper();
const TURF = require('/turf.min.js');

export default function convexHullFunction(wktGeom1) {
	if (!wktGeom1) throw new Error("geof:convexHull -> One of arguments was null/undefined -> wktGeom1: " + wktGeom1);

	try {
		let turfGeom1 = TH.convertWktToTurfGeom(wktGeom1);
	
		const turfconvex = TURF.convex(turfGeom1);
		return TH.convertTurfGeomToWKT(turfconvex);
			
	} catch (error) {
		return "[ERROR] " + error;
	}
}

/********************************************************************************************
*	
*	Turf.js
*
*	convex()
*	Takes a Feature or a FeatureCollection and returns a convex hull Polygon.
*
*	Returns
*	Feature <Polygon> - a convex hull
*
********************************************************************************************/
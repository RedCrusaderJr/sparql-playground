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
*	8.7.7 Function: geof:symDifference
*
*	geof:symDifference (geom1: ogc:geomLiteral, geom2: ogc:geomLiteral): ogc:geomLiteral
*
*	This function returns a geometric object that represents all Points in the set symmetric 
*	difference of geom1 with geom2. Calculations are in the spatial reference system of geom1.
*
**************************************************************************************************************/


import { TurfHelper } from '/turf-helper.mjs';
const TH = new TurfHelper();
const TURF = require('/turf.min.js');

export default function symDifferenceFunction(wktGeom1, wktGeom2) {
	if (!wktGeom1 || !wktGeom2) throw new Error("geof:symDifference -> One of arguments was null/undefined -> wktGeom1: " + wktGeom1 + ", wktGeom2: " + wktGeom2);
	wktGeom1 = "" + wktGeom1;
	wktGeom2 = "" + wktGeom2;

	try {

		return "not implemented";
			
	} catch (error) {
		return "[ERROR] " + error;
	}
}

/***************************************************************************************************************
*	
*	Turf.js
*
*	no function
*	
***************************************************************************************************************/
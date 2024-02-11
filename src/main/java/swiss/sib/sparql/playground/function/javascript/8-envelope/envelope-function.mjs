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
*	8.7.8 Function: geof:envelope
*
*	geof:envelope (geom1: ogc:geomLiteral): ogc:geomLiteral
*
*	This function returns the minimum bounding box of geom1. 
*	Calculations are in the spatial reference system of geom1.
*
********************************************************************************************/


import { TurfHelper } from '/turf-helper.mjs';
const TH = new TurfHelper();
const TURF = require('/turf.min.js');

export default function envelopeFunction(wktGeom1) {
	if (!wktGeom1) throw new Error("geof:envelope -> One of arguments was null/undefined -> wktGeom1: " + wktGeom1);

	try {
        let turfGeom1 = TH.convertWktToTurfGeom(wktGeom1);
        
		// restricted only to polygons
        const turfUnion = TURF.envelope(turfGeom1);
        return turfUnion;

    } catch (error) {
        return "[ERROR] " + error;
    }
}

/********************************************************************************************
*	
*	Turf.js
*
*	envelope()
*	Takes any number of features and returns a rectangular Polygon 
*	that encompasses all vertices.
*
*	Returns
*	Feature <Polygon> - a rectangular Polygon feature that encompasses all vertices
*
********************************************************************************************/
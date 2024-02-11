/********************************************************************************************
*
*       8.7 Non-topological Query Functions
*        
*       This clause defines SPARQL functions for performing non-topological spatial operations. 
* 
* 	Req 19 	
*	Implementations shall support geof:distance, geof:buffer, geof:convexHull,
*	geof:intersection, geof:union, geof:difference,	geof:symDifference, geof:envelope
*	and geof:boundary as SPARQL extension functions, consistent with the definitions of
*	the corresponding functions (distance, buffer, convexHull, intersection, difference,
*	symDifference, envelope and boundary respectively) in Simple Features [ISO 19125-1].
*
*
*       8.7.4 Function: geof:intersection
*
*       geof:intersection (geom1: ogc:geomLiteral, geom2: ogc:geomLiteral): ogc:geomLiteral
*   
*       This function returns a geometric object that represents all Points in the 
*       intersection of geom1 with geom2. Calculations are in the spatial reference system of geom1.
*
********************************************************************************************/


import { TurfHelper } from '/turf-helper.mjs';
const TH = new TurfHelper();
const TURF = require('/turf.min.js');

export default function intersectionFunction(wktGeom1, wktGeom2) {
    if (!wktGeom1 || !wktGeom2) throw new Error("geof:intersection -> One of arguments was null/undefined -> wktGeom1: " + wktGeom1 + ", wktGeom2: " + wktGeom2);

    try {
        let turfGeom1 = TH.convertWktToTurfGeom(wktGeom1);
        let turfGeom2 = TH.convertWktToTurfGeom(wktGeom2);
                
        const turfIntersection = TURF.intersect(turfGeom1, turfGeom2);
        return prepareResult(turfIntersection);

    } catch (error) {
        return "[ERROR] " + error;
    }
}

function prepareResult(intersectionResult) {
    return TH.defaultToEmptySpatialObject(intersectionResult);
}
    
/********************************************************************************************
*	
*	Turf.js
*
*	intersect()
*	Takes two polygon or multi-polygon geometries and finds their polygonal intersection.
*   If they don't intersect, returns null.
*
*   Returns
*   (Feature|null) - returns a feature representing the area they share 
*   (either a Polygon or MultiPolygon ). If they do not share any area, returns null.
*
********************************************************************************************/
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
*	8.7.2 Function: geof:buffer
*
* 	geof:buffer (geom: ogc:geomLiteral, radius: xsd:double,	units: xsd:anyURI): ogc:geomLiteral
*	
*	This function returns a geometric object that represents all Points whose
*	distance from geom1 is less than or equal to the radius measured in units. 
*	Calculations are in the spatial reference system of geom1.
*
**************************************************************************************************************/


import { TurfHelper } from '/turf-helper.mjs';
const TH = new TurfHelper();
const TURF = require('/turf.min.js');

export default function bufferFunction(wktGeom1, radius, ogcUnitUri) {	
	if (!wktGeom1 || !radius) throw new Error("geof:buffer -> One of arguments was null/undefined -> wktGeom1: " + wktGeom1 + ", radius: " + radius);
	wktGeom1 = "" + wktGeom1;

	try {
		if (!ogcUnitUri) ogcUnitUri = TH.OGC_METRE;

		let turfGeom1 = TH.convertWktToTurfGeom(wktGeom1);
		let turfUnit = TH.getTurfUnitMappedOn(ogcUnitUri);

		const turfBuffer = TURF.buffer(turfGeom1, radius / turfUnit.GetMultiplicator(), {units: turfUnit.GetName()});
		return TH.convertTurfGeomToWKT(turfBuffer);
			
	} catch (error) {
		return "[ERROR] " + error;
	}
}

/***************************************************************************************************************
*	
*	Turf.js
*
*	buffer()
*	Calculates a buffer for input features for a given radius.
*	Units supported are miles, kilometers, and degrees.
*
*	Returns
*	(FeatureCollection|Feature <(Polygon|MultiPolygon)>|undefined) - buffered features
*
***************************************************************************************************************/
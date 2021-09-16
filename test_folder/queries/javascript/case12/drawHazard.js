var sem = require('/MarkLogic/semantics.xqy');
import { bufferFunction } from  '/buffer-function.mjs';

var query = `
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
PREFIX custom: <http://www.custom.org/> 

SELECT DISTINCT ?g_wktHazardZone
WHERE {
	#hazard zones
	?hazardType rdfs:subClassOf+ custom:Hazard. 
	?hazardId a ?hazardType;
		custom:Hazard.DangerZone ?g_wktHazardZone.
}
`;

var params = {bFun: bufferFunction, intersectionFunction: geo.regionIntersects}
var results = sem.sparql(query,params);
results
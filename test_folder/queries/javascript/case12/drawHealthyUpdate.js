var sem = require('/MarkLogic/semantics.xqy');
import { bufferFunction } from  '/buffer-function.mjs';

var query = `
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
PREFIX geo: <http://www.opengis.net/ont/geosparql#>  
PREFIX cim: <http://iec.ch/TC57/CIM-generic#> 
PREFIX xdmp: <http://marklogic.com/xdmp#> 
PREFIX custom: <http://www.custom.org/> 

SELECT DISTINCT ?g_wktPolygon ?g_wktLine
WHERE {
    #hazard zones
    ?hazardType rdfs:subClassOf+ custom:Hazard. 
    ?hazardId a ?hazardType;
        custom:Hazard.DangerZone ?g_wktHazardZone.
    
    #buffers
    custom:thereIs custom:a ?g_wktPolygon.

    BIND(xdmp:apply(?intersectionFunction, ?g_wktPolygon, ?g_wktHazardZone) as ?intersection).

    #FILTER(str(?intersection) IN ("POINT EMPTY", "false"))
    FILTER(str(?intersection) = "false")
}
`;

var params = {intersectionFunction: geo.regionIntersects}
var results = sem.sparql(query,params);
results
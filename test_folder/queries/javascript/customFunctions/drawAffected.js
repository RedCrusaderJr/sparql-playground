var sem = require('/MarkLogic/semantics.xqy');
import bufferFunction from  '/create-buffer-function.mjs';

var query = `
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
PREFIX geo: <http://www.opengis.net/ont/geosparql#>  
PREFIX cim: <http://iec.ch/TC57/CIM-generic#> 
PREFIX xdmp: <http://marklogic.com/xdmp#> 
PREFIX custom: <http://www.custom.org/> 

SELECT DISTINCT ?g_wktPolygon ?g_wktLine
WHERE {
    #connectivity nodes with a terminal
    ?tmId cim:Terminal.ConnectivityNode ?cNodeId;
        #conducting equipment of that terminal
        cim:Terminal.ConductingEquipment ?condEqId.							   

    #locations - coordinates
    ?condEqId cim:PowerSystemResource.Location ?locationId.
    ?postionPointId1 rdf:type cim:PositionPoint.
    ?postionPointId2 rdf:type cim:PositionPoint.
    FILTER(str(?postionPointId1) < str(?postionPointId2))

    ?postionPointId1 cim:PositionPoint.Location ?locationId;
        cim:PositionPoint.xPosition ?x1;
        cim:PositionPoint.yPosition ?y1.
    ?postionPointId2 cim:PositionPoint.Location ?locationId;
        cim:PositionPoint.xPosition ?x2;
        cim:PositionPoint.yPosition ?y2.
    FILTER(?x1 != ?x2 || ?y1 != ?y2)

    #hazard zones
    ?hazardType rdfs:subClassOf+ custom:Hazard. 
    ?hazardId a ?hazardType;
        custom:Hazard.DangerZone ?g_wktHazardZone.

    #construct WKTs (draw geometry)
    BIND(concat("LINESTRING (", str(?x1), " ", str(?y1), ", ", str(?x2), " ", str(?y2), ")") as ?lineStr).
    BIND(strdt(?lineStr, geo:wktLiteral) as ?g_wktLine).
    
    BIND(xdmp:apply(?bFun, ?lineStr, '50') as ?polygonStr).
    BIND(strdt(?polygonStr, geo:wktLiteral) as ?g_wktPolygon).
    
    BIND(xdmp:apply(?intersectionFunction, ?g_wktPolygon, ?g_wktHazardZone) as ?intersection).

    FILTER(str(?postionPointId1) < str(?postionPointId2))
    FILTER(?x1 != ?x2 || ?y1 != ?y2)
    #FILTER(str(?intersection) NOT IN ("POINT EMPTY", "false"))
    FILTER(str(?intersection) != "false")
}
`;

var params = {bFun: bufferFunction, intersectionFunction: geo.regionIntersects}
var results = sem.sparql(query,params);
results
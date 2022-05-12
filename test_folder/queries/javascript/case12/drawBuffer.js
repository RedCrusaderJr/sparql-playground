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

    ?postionPointId1 cim:PositionPoint.Location ?locationId;
        cim:PositionPoint.xPosition ?x1;
        cim:PositionPoint.yPosition ?y1.
    ?postionPointId2 cim:PositionPoint.Location ?locationId;
        cim:PositionPoint.xPosition ?x2;
        cim:PositionPoint.yPosition ?y2.
    
    #construct WKTs (draw geometry)
    BIND(concat("LINESTRING (", str(?x1), " ", str(?y1), ", ", str(?x2), " ", str(?y2), ")") as ?lineStr).
    BIND(strdt(?lineStr, geo:wktLiteral) as ?g_wktLine).

    BIND(xdmp:apply(?bufferF, ?lineStr, '50') as ?polygonStr).
    BIND(strdt(?polygonStr, geo:wktLiteral) as ?g_wktPolygon).

    FILTER(str(?postionPointId1) < str(?postionPointId2))
    FILTER(?x1 != ?x2 || ?y1 != ?y2)
}
`;

var params = {bufferF: bufferFunction, intersectionF: geo.regionIntersects}
var results = sem.sparql(query,params);
results
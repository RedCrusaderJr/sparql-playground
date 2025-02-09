var sem = require('/MarkLogic/semantics.xqy');
import bufferFunction from  '/create-buffer-function.mjs';

var query = `
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
PREFIX geo: <http://www.opengis.net/ont/geosparql#> 
PREFIX cim: <http://iec.ch/TC57/CIM-generic#> 

SELECT DISTINCT ?g_wktLine
WHERE {
	#connectivity nodes with a terminal
  	?tmId cim:Terminal.ConnectivityNode ?cNodeId;
  		#conducting equipment of that terminal
    	cim:Terminal.ConductingEquipment ?condEqId.
  
  	#locations with coordinates
  	?condEqId cim:PowerSystemResource.Location ?locationId.  
  	?postionPointId1 cim:PositionPoint.Location ?locationId;
  		cim:PositionPoint.xPosition ?x1;
  		cim:PositionPoint.yPosition ?y1.
  	?postionPointId2 cim:PositionPoint.Location ?locationId;
  		cim:PositionPoint.xPosition ?x2;
  		cim:PositionPoint.yPosition ?y2.	
  
  	#construct WKTs (draw geometry)
  	BIND(concat("LINESTRING (", str(?x1), " ", str(?y1), ", ", str(?x2), " ", str(?y2), ")") as ?lineStr).
  	BIND(strdt(?lineStr, geo:wktLiteral) as ?g_wktLine).
  	
	FILTER(str(?postionPointId1) < str(?postionPointId2)).
    FILTER(?x1 != ?x2 || ?y1 != ?y2).
}
`;

var params = {bFun: bufferFunction, intersectionFunction: geo.regionIntersects}
var results = sem.sparql(query,params);
results
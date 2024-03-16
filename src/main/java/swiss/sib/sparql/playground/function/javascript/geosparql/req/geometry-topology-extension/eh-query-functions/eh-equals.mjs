/**************************************************************************************************************
* 
*   9 Geometry Topology Extension (relation_family, serialization, version)
*
*   This clause establishes the Geometry Topology Extension (relation_family, serialization, version) 
*   parameterized requirements class, with URI /req/geometry-topology-extension, which defines a collection
*   of topological query functions that operate on geometry literals. This class is parameterized to give 
*   implementations flexibility in the topological relation families and geometry serializations that they 
*   choose to support. This requirements class has a single corresponding conformance class Geometry Topology
*   Extension (relation_family, serialization, version), with URI /conf/geometry-topology-extension.
*
*   The Dimensionally Extended Nine Intersection Model (DE-9IM) has been used to define the relation tested by 
*   the query functions introduced in this section. Each query function is associated with a defining 
*   DE-9IM intersection pattern. Possible pattern values are -1 (empty), 0, 1, 2, T (true) = {0, 1, 2},
*   F (false) = {-1}, * (don’t care) = {-1, 0, 1, 2}. In the following descriptions, the notation X/Y is used
*   denote applying a spatial relation to geometry types X and Y (i.e., x relation y where x is of type X and
*   y is of type Y). The symbol P is used for 0-dimensional geometries (e.g. points). The symbol L is used for
*   1-dimensional geometries (e.g. lines), and the symbol A is used for 2-dimensional geometries (e.g. polygons).
*   Consult the Simple Features specification [ISO 19125-1] for a more detailed description of DE-9IM 
*   intersection patterns.
*
*   9.4 Requirements for Egenhofer Relation Family (relation_family=Egenhofer)
*   
*   This clause establishes requirements for the Egenhofer relation family. Consult references [2] and [3] for
*   a more detailed discussion of Egenhofer relations. 
*   
*   Req 23 Implementations shall support geof:ehEquals, geof:ehDisjoint, geof:ehMeet, geof:ehOverlap, 
*   geof:ehCovers, geof:ehCoveredBy, geof:ehInside, geof:ehContains as SPARQL extension functions, consistent
*   with their corresponding DE-9IM intersection patterns, as defined by Simple Features [ISO 19125-1].
*   /req/geometry-topology-extension/eh-query-functions
*
*   Boolean query functions defined for the Egenhofer relation family, along with their associated DE-9IM 
*   intersection patterns, are shown in Table 6 below. Multi-row intersection patterns should be interpreted
*   as a logical OR of each row. Each function accepts two arguments (geom1 and geom2) of the geometry literal 
*   serialization type specified by serialization and version. Each function returns an xsd:boolean value of
*   true if the specified relation exists between geom1 and geom2 and returns false otherwise. In each case,
*   the spatial reference system of geom1 is used for spatial calculations.
*
*   geof:ehEquals(geom1: ogc:geomLiteral, geom2: ogc:geomLiteral): xsd:boolean
*   
*   DE-9IM Intersection Pattern: (TFFFTFFFT)
*
**************************************************************************************************************/


export default function ehEqualsFunction(wktGeom1, wktGeom2) {
	if(!wktGeom1 || !wktGeom2) throw new Error("geof:distance -> One of arguments was null/undefined -> wktGeom1: " + wktGeom1 + ", wktGeom2: " + wktGeom2);
	wktGeom1 = "" + wktGeom1;
	wktGeom2 = "" + wktGeom2;

	try {
			
	} catch (error) {
		return "[ERROR] " + error;
	}
}
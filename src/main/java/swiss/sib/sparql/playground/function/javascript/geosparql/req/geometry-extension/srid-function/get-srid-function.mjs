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
*   The Dimensionally Extended Nine Intersection Model (DE-9IM) has been used to define the relation tested 
*   by the query functions introduced in this section. Each query function is associated with a defining 
*   DE-9IM intersection pattern. Possible pattern values are -1 (empty), 0, 1, 2, T (true) = {0, 1, 2}, 
*   F (false) = {-1}, * (don’t care) = {-1, 0, 1, 2}. In the following descriptions, the notation X/Y is used
*   denote applying a spatial relation to geometry types X and Y (i.e., x relation y where x is of type X and
*   y is of type Y). The symbol P is used for 0-dimensional geometries (e.g. points). The symbol L is used for
*   1-dimensional geometries (e.g. lines), and the symbol A is used for 2-dimensional geometries 
*   (e.g. polygons). Consult the Simple Features specification [ISO 19125-1] for a more detailed description 
*   of DE-9IM intersection patterns.
*
* 	Req 21 Implementations shall support geof:relate as a SPARQL extension function,
*   consistent with the relate operator defined in Simple Features [ISO 19125-1].
*
*	geof:relate (geom1: ogc:geomLiteral, geom2: ogc:geomLiteral, pattern-matrix: xsd:String): xsd:boolean
*
*	Returns true if the spatial relationship between geom1 and geom2 corresponds to one with acceptable 
*   values for the specified pattern-matrix. Otherwise, this function returns false. Pattern-matrix 
*   represents a DE-9IM intersection pattern consisting of T (true) and F (false) values. The spatial
*   reference system for geom1 is used for spatial calculations
*
**************************************************************************************************************/


export default function relateFunction(wktGeom1, wktGeom2, patternMatrix) {
	if(!wktGeom1 || !wktGeom2) throw new Error("geof:distance -> One of arguments was null/undefined -> wktGeom1: " + wktGeom1 + ", wktGeom2: " + wktGeom2);
	wktGeom1 = "" + wktGeom1;
	wktGeom2 = "" + wktGeom2;

	try {
			
	} catch (error) {
		return "[ERROR] " + error;
	}
}
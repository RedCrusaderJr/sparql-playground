const WKT = require('/wellknown.js');
const GEO = require('/MarkLogic/geospatial/geospatial');

export default class MarkLogicGeoHelper {
    constructor() {
        this.wktToMarkLogicGeom = new Map([
            ['Point', GEO.point],
            ['LineString', GEO.linestring],
            ['Polygon', GEO.polygon],
            //['MultiPolygon', ""],
            //['MultiLineString', ""],
            //['MultiPolygon', ""]
        ]);  
    }

    getWktPart(wktGeom) {
        let wktGeomString = "" + wktGeom;
        //               "<wkt string>"^^<datatype uri>
        let wktRegex = /\"(?<wkt>.*)\"\^\^(?<datatype>.*)/;
        let wktMatch = wktGeomString.match(wktRegex);

        if (wktMatch) return wktMatch.groups.wkt

        return wktGeomString;
    }

    convertWktToMarkLogicGeom(wktString) {
        const geojsonGeom = WKT.parse(wktString);
        let markLogicFun = this.wktToMarkLogicGeom.get(geojsonGeom.type); 

        if (markLogicFun) {
            return markLogicFun(geojsonGeom.coordinates);
        }
        
        throw new Error("[convertWktToMarkLogicGeom] Unsupported geojson type: " + geojsonGeom.type);
    }
    
    convertMarkLogicGeomToWKT(markLogicGeom) {
        return WKT.stringify(markLogicGeom);
    }
}
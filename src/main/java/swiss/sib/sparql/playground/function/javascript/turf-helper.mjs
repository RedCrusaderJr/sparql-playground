const WKT = require('/wellknown.js');
const TURF = require('/turf.min.js');

class TurfHelper {
    constructor() {
        this.TURF_POINT = 'Point';
        this.TURF_LINESTRING = 'LineString';
        this.TURF_POLYGON = 'Polygon';

        this.wktToTurfGeom = new Map([
            ['Point', TURF.point],
            ['LineString', TURF.lineString],
            ['Polygon', TURF.polygon],
            ['MultiPolygon', TURF.multiPoint],
            ['MultiLineString', TURF.multiLineString],
            ['MultiPolygon', TURF.multiPolygon]
        ]);  

        this.OGC_DEGREE = 'http://www.opengis.net/def/uom/OGC/1.0/degree';
        this.OGC_RADIAN = 'http://www.opengis.net/def/uom/OGC/1.0/radian';
        this.OGC_METRE = 'http://www.opengis.net/def/uom/OGC/1.0/metre';

        this.ogcUnits = new Map([
            [this.OGC_DEGREE, new OgcUnit(this.OGC_DEGREE, 1)],
            [this.OGC_RADIAN, new OgcUnit(this.OGC_RADIAN, 1)],
            [this.OGC_METRE, new OgcUnit(this.OGC_METRE, 1)]
        ]);

        this.TURF_DEGREES = 'degrees';
        this.TURF_RADIANS = 'radians';
        this.TURF_KILOMETERS = 'kilometers';

        this.trufUnits = new Map([
            [this.TURF_DEGREES, new TurfUnit(this.TURF_DEGREES, 1)],
            [this.TURF_RADIANS, new TurfUnit(this.TURF_RADIANS, 1)],
            [this.TURF_KILOMETERS, new TurfUnit(this.TURF_KILOMETERS, 1000)]
        ]);

        this.turfToOgcUnitMap = new Map([
            [this.TURF_DEGREES, this.OGC_DEGREE],
            [this.TURF_RADIANS, this.OGC_RADIAN],
            [this.TURF_KILOMETERS, this.OGC_METRE]
        ]);

        this.ogcToTurfUnitMap = new Map([
            [this.OGC_DEGREE, this.TURF_DEGREES],
            [this.OGC_RADIAN, this.TURF_RADIANS],
            [this.OGC_METRE, this.TURF_KILOMETERS]
        ]);
    }

    divideLineIntoPoints(lineGeom) {
        if(lineGeom.geometry.type != this.TURF_LINESTRING) {
            throw new Error(WrongGeometryTypeErrorMessage(lineGeom.geometry.type, this.TURF_LINESTRING));
        }
    
        let point1 = TURF.point(lineGeom.geometry.coordinates[0]);
        let point2 = TURF.point(lineGeom.geometry.coordinates[1]);
        return [point1, point2]
    }
    
    dividePolygonIntoLines(polygonGeom) {
        if(polygonGeom.geometry.type != this.TURF_POLYGON) {
            throw new Error(WrongGeometryTypeErrorMessage(lineGeom.geometry.type, this.TURF_POLYGON));
        }
    
        let lines = [];
        let polygonPoints = polygonGeom.geometry.coordinates[0];  // MultiPolygon object has array of polygons at this point
        for (let i = 0; i < polygonPoints.length-1; i++) {
            let currentPoint = polygonPoints[i];
            let nextPoint = polygonPoints[i+1];
    
            let line = TURF.lineString([currentPoint, nextPoint]);
            lines.push(line);
        }
    
        return lines;
    }

    getWktPart(wktGeom) {

        let wktGeomString = "" + wktGeom;

        //               "<wkt string>"^^<datatype uri>
        let wktRegex = /\"(?<wkt>.*)\"\^\^(?<datatype>.*)/;
        let wktMatch = wktGeomString.match(wktRegex);

        if(wktMatch) return wktMatch.groups.wkt

        return wktGeomString;
    }

    convertWktToTurfGeom(wktString) {
        const geojsonGeom = WKT.parse(wktString);
        let turfGeomFun = this.wktToTurfGeom.get(geojsonGeom.type); 

        if (turfGeomFun) {
            return turfGeomFun(geojsonGeom.coordinates);
        }
        
        throw new Error("[convertWktToTurfGeom] Unsupported geojson type: " + geojsonGeom.type);
    }
    
    convertTurfGeomToWKT(turfGeom) {
        return WKT.stringify(turfGeom);
    }

    getTurfUnit(turfUnitName) {
        let turfUnit = this.trufUnits.get("" + turfUnitName);

        if (turfUnit) {
            return turfUnit;
        }
        
        throw new Error("[getTurfUnit] Unsupported Turf unit of measurement: " + turfUnitName);
    }

    getTurfUnitMappedOn(ogcUnitUri) {
        let turfUnitName = this.ogcToTurfUnitMap.get("" + ogcUnitUri);
        
        if (!turfUnitName) {
            throw new Error("[getTurfUnitMappedOn] Unsupported OGC unit of measurement: " + ogcUnitUri + "typeof: " + typeof ogcUnitUri);
        }
        
        return this.trufUnits.get("" + turfUnitName);
    }

    getOgcUnit(ogcUnitUri) {
        let ogcUnit = this.ogcUnits.get("" + ogcUnitUri);

        if (ogcUnit) {
            return ogcUnit;
        }
        
        throw new Error("[getOgcUnit] Unsupported OGC unit of measurement: " + ogcUnitUri);
    }

    getOgcUnitMappedOn(turfUnitName) {
        let ogcUnitUri = this.turfToOgcUnitMap.get("" + turfUnitName);
        
        if (!ogcUnitUri) {
            throw new Error("[getOgcUnitMappedOn] Unsupported Turf unit of measurement: " + turfUnitName);
        }
        
        return this.ogcUnits.get("" + ogcUnitUri);
    }

    defaultToEmptySpatialObject(turfGeom) {
        //  In WKT format 'POINT EMPTY' represents an empty spatial object. 
        return turfGeom != null ? this.convertTurfGeomToWKT(turfGeom) : "POINT EMPTY";            
    }

    defaultToEmptyString(turfGeom) {
        return turfGeom != null ? this.convertTurfGeomToWKT(turfGeom) : "";   
    }
}

class TurfUnit {
    constructor(name, multiplicator) {
        this.name = name;
        this.multiplicator = multiplicator;
    }

    GetName() {
        return this.name;
    }

    GetMultiplicator() {
        return this.multiplicator;
    }
}

class OgcUnit {
    constructor(uri, multiplicator) {
        this.uri = uri;
        this.multiplicator = multiplicator;
    }

    GetUri() {
        return this.uri;
    }

    GetMultiplicator() {
        return this.multiplicator;
    }
}

export { TurfHelper, TurfUnit, OgcUnit }
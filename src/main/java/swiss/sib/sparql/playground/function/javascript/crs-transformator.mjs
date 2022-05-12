const Proj4js = require('/proj4js.js');

export default class CrsTransformator {
    constructor() {        
        try {
            this.wgs84 = new Proj4js.Proj("+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs")
            this.utmZone10N = new Proj4js.Proj("+proj=utm +zone=10 +ellps=GRS80 +datum=NAD83 +units=m +no_defs");
    
        } catch (error) {
            console.log(error);
        }
    }

    fromWgs84ToUtmZone10N(wgsPoint) {
        try {
            return Proj4js.transform(this.wgs84, this.utmZone10N, wgsPoint);

        } catch (error) {
            return "" + error;
        }
    }

    fromUtmZone10NToWgs84(utmPoint) {
        try {
            return Proj4js.transform(this.utmZone10N, this.wgs84, utmPoint);

        } catch (error) {
            return "" + error;
        }
    }
}
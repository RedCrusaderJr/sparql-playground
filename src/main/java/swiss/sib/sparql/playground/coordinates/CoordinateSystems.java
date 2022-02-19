package swiss.sib.sparql.playground.coordinates;

import java.util.HashMap;

enum CoordinatesSourceType {
    CIMXML,
    GEOSPARQL,
    RDF4J,
    MARKLOGIC,
    GEOJSON,
    LEAFLET,
}

enum CoordinatesType {
    LATITUDE_LONGITUDE,
    LONGITUDE_LATITUDE,
}

public class CoordinateSystems {

    //#region Instance
    private static volatile CoordinateSystems instance;
    public static CoordinateSystems cleanGetInstance() {
        if(instance != null) {
            return instance;
        }
        
        synchronized(CoordinateSystems .class) {
            if(instance == null){
                instance = new CoordinateSystems();
            }
        }

        return instance;
    }
    //#endregion Instance


    private HashMap<CoordinatesSourceType, CoordinatesType> sourceToCoordinatesTypeMap;
    public HashMap<CoordinatesSourceType, CoordinatesType> GetSourceToCoordinatesTypeMap() {
        return sourceToCoordinatesTypeMap;
    }


    private CoordinateSystems() {
        this.sourceToCoordinatesTypeMap = new HashMap<CoordinatesSourceType, CoordinatesType>();
        this.sourceToCoordinatesTypeMap.put(CoordinatesSourceType.CIMXML, CoordinatesType.LONGITUDE_LATITUDE);
        this.sourceToCoordinatesTypeMap.put(CoordinatesSourceType.GEOSPARQL, CoordinatesType.LONGITUDE_LATITUDE);
        // this.sourceToCoordinatesTypeMap.put(CoordinatesSourceType.RDF4J, CoordinatesType.LONGITUDE_LATITUDE);
        // this.sourceToCoordinatesTypeMap.put(CoordinatesSourceType.MARKLOGIC, CoordinatesType.LONGITUDE_LATITUDE);
        this.sourceToCoordinatesTypeMap.put(CoordinatesSourceType.GEOJSON, CoordinatesType.LONGITUDE_LATITUDE);
        this.sourceToCoordinatesTypeMap.put(CoordinatesSourceType.LEAFLET, CoordinatesType.LATITUDE_LONGITUDE);
    }

    public Coordinates CreateCoordinatesObject(double coordinate1, double coordinate2, CoordinatesSourceType source) {
        CoordinatesType coordinateType = sourceToCoordinatesTypeMap.get(source);
        Coordinates coordinates = new Coordinates();

        if(coordinateType.equals(CoordinatesType.LATITUDE_LONGITUDE)) {
            coordinates.Latitude = coordinate1;
            coordinates.Longitude = coordinate2;
        }
        else if(coordinateType.equals(CoordinatesType.LONGITUDE_LATITUDE)) {
            coordinates.Longitude = coordinate1;
            coordinates.Latitude =  coordinate2;
        }

        return coordinates;
    }
}

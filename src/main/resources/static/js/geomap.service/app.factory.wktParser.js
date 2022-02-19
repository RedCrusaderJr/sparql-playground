(function (angular, undefined) {
	'use strict';

	/*
	* create geomap service
	*/

	angular.module('geomap.service')
		.factory('wktParser', wktParserFactory);

	//
	// implement wktParser factory
	function wktParserFactory() {
		class WktParser {
			constructor() {
			}

            parseElement(element) {
                //HACK: follows the default ordering of WKT/GeoSPARQL (LONGITUDE-LATITUDE)
                let wktRegex = /.*(?<point>POINT) \((?<pointLongitude>.*) (?<pointLatitude>.*)\).*|.*(?<line>LINESTRING) \((?<lineLongitude1>.*) (?<lineLatitude1>.*), (?<lineLongitude2>.*) (?<lineLatitude2>.*)\).*|.*(?<polygon>POLYGON) \(\((?<polygonCoordinates>.*)\)\).*/
                let wktMatch = element.match(wktRegex);
    
                if (typeof wktMatch.groups.point == 'undefined' &&
                    typeof wktMatch.groups.line == 'undefined' &&
                    typeof wktMatch.groups.polygon == 'undefined') {
                    return;
                }
    
                if(wktMatch.groups.point == "POINT") {
                    return	parsePoint(wktMatch);
                }
                
                if(wktMatch.groups.line == "LINESTRING") {
                    return	parseLineString(wktMatch);
                }
                
                if(wktMatch.groups.polygon == "POLYGON") {
                    return	parsePolygon(wktMatch)
                }
    
                return new Object;;
            }

            extractPointCoordinates(parsedElement) {
                let point = parsedElement.Coordinates.Shapes[0][0];
                return point;
            };
    
            extractLineCoordinates(parsedElement) {
                let lineParsedCoords = parsedElement.Coordinates.Shapes[0];
                let lineCoordinates = [];
                lineParsedCoords.forEach(point => {
                    lineCoordinates.push([point.latitude, point.longitude]);
                });
    
                return lineCoordinates;
            };
    
            extractPolygonCoordinates(parsedElement) {
                let polygonParsedCoords = parsedElement.Coordinates.Shapes[0];
                let polygonCoordinates = [];
                polygonParsedCoords.forEach(point => {
                    polygonCoordinates.push([point.latitude, point.longitude]);
                });
    
                return polygonCoordinates;
            };
		}

        function parsePoint(wktMatch) {
            let parsedElement = new Object;

            parsedElement.Name = wktMatch.groups.point;
            parsedElement.Coordinates = {
                Shapes: [[{
                        latitude: wktMatch.groups.pointLatitude.trim(),
                        longitude: wktMatch.groups.pointLongitude.trim(),
                    }]
                ]
            };

            return parsedElement;
        }

        function parseLineString(wktMatch) {
            let parsedElement = new Object;

            parsedElement.Name = wktMatch.groups.line;
            parsedElement.Coordinates = {
                Shapes: [[{
                        latitude: wktMatch.groups.lineLatitude1.trim(),
                        longitude: wktMatch.groups.lineLongitude1.trim(),
                    }, {
                        latitude: wktMatch.groups.lineLatitude2.trim(),
                        longitude: wktMatch.groups.lineLongitude2.trim(),
                    }]
                ]
            };

            return parsedElement;
        }

        function parsePolygon(wktMatch) {
            let parsedElement = new Object;

            parsedElement.Name = wktMatch.groups.polygon;
            parsedElement.Coordinates = {
                Shapes: []
            };

            let shape = [];
            wktMatch.groups.polygonCoordinates.split(', ').forEach(function(point) {
                let pointParts = point.split(' ');
                let pointLongitude = pointParts[0].trim();  //HACK: order is important -> 
                let pointLatitude = pointParts[1].trim();   //LeafletJS (default LATITUTE-LONGITUDE), WKT/GeoSPARQL (default LONGITUDE-LATITUDE)
                shape.push({ 
                    latitude: pointLatitude,
                    longitude: pointLongitude,
                }); 
            });
            parsedElement.Coordinates.Shapes.push(shape);

            return parsedElement;
        }

		return new WktParser();
	}

})(angular);
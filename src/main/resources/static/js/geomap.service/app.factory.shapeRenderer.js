(function (angular, undefined) {
	'use strict';

	/*
	* extend geomap service
	*/

	angular.module('geomap.service')
		.factory('shapeRenderer', shapeRendererFactory);

	//
	// implement shapeRenderer factory
	shapeRendererFactory.$inject=["geomapManipulation", "wktParser"]
	function shapeRendererFactory(geomapManipulation, wktParser) {
		class ShapeRenderer {
			constructor() {
				this.bulkRenderGroupKeys = ["POINT", "LINESTRING", "POLYGON"]//-HEALTHY", "POLYGON-AFFECTED", "POLYGON-HAZARD"]
				this.bulkRenderMap = new Map();
			}

			startBulkRender() {
				this.bulkRenderMap.clear();
				for(const key of this.bulkRenderGroupKeys) {

					if(key === "POLYGON") {
						this.bulkRenderMap.set(key, new Map());
					}
					else {
						this.bulkRenderMap.set(key, []);
					}
				}
			}

			addElementToBulkRender(rawValue, lineColor, polygoneColor, elementOrigin) {
				if(rawValue == "POINT EMPTY") {
					return;
				}
				
				let parsedElement = wktParser.parseElement(rawValue);
				addParsedElementToBulkRender(parsedElement, this.bulkRenderMap, lineColor, polygoneColor, elementOrigin)
			}

			finishBulkRender() {
				for(const groupKey of this.bulkRenderGroupKeys) {
					let	bulkRenderGroup = this.bulkRenderMap.get(groupKey);
					addMultipleElementsToGeomap(groupKey, bulkRenderGroup);
				}
				this.bulkRenderMap.clear();
			}

			routeChangeStart() {
				return true;
			}
		}

		//
		//PRIVATE FUNCTIONS
		function addParsedElementToBulkRender(parsedElement, bulkRenderMap, lineColor, polygoneColor, elementOrigin) {
			if(!bulkRenderMap.has(parsedElement.Name)) {
				console.error("KEY: '" + parsedElement.Name + "' not present in 'bulkRenderMap'.");
				return;
			}

			if(typeof lineColor == 'undefined') {
				lineColor = { color: 'green' };
			}
			if(typeof polygoneColor == 'undefined') {
				polygoneColor = { color: 'blue' };
			}
			
			//
			//get group from map
			let	bulkRenderGroup = bulkRenderMap.get(parsedElement.Name);
			//
			//change group
			switch(parsedElement.Name) {
				case "POINT":
					let point = geomapManipulation.createPoint(wktParser.extractPointCoordinates(parsedElement));
					bulkRenderGroup.push(point);
					//TODO: think about elementOrigin sub-group

					geomapManipulation.setMapViewLatitude(point.latitude);
                	geomapManipulation.setMapViewLongitude(point.longitude);
					break;

				case "LINESTRING":
					let line = geomapManipulation.createLine(wktParser.extractLineCoordinates(parsedElement), lineColor);
					bulkRenderGroup.push(line);
					//TODO: think about elementOrigin sub-group

					geomapManipulation.setMapViewLatitude(line._latlngs[0].lat);
                    geomapManipulation.setMapViewLongitude(line._latlngs[0].lng);
					break;

				case "POLYGON":
					let polygon = geomapManipulation.createPolygon(wktParser.extractPolygonCoordinates(parsedElement), polygoneColor);
					if(!bulkRenderGroup.has(elementOrigin)) {
						bulkRenderGroup.set(elementOrigin, []);
					}
					let subGroup = bulkRenderGroup.get(elementOrigin);
					subGroup.push(polygon);
					bulkRenderGroup.set(elementOrigin, subGroup);

					geomapManipulation.setMapViewLatitude(polygon._latlngs[0][0].lat);
                    geomapManipulation.setMapViewLongitude(polygon._latlngs[0][0].lng);
					break;

				default:
					break;
			}
			//
			//set group back to map
			bulkRenderMap.set(parsedElement.Name, bulkRenderGroup);
		}

		function addMultipleElementsToGeomap(groupKey, bulkRenderGroup) {
			switch(groupKey) {
				case "POINT":
					geomapManipulation.addMultipleMarkersToGeomap(bulkRenderGroup);
					break;

				case "LINESTRING":
					geomapManipulation.addMultipleLinesToGeomap(bulkRenderGroup);
					break;

				case "POLYGON":
					bulkRenderGroup.forEach(function(subGroup, polygonOrigin) {
						geomapManipulation.addMultiplePolygonsToGeomap(subGroup, polygonOrigin);
					});
					break;

				default:
					break;
			}
		};

		return new ShapeRenderer();
	};

})(angular);

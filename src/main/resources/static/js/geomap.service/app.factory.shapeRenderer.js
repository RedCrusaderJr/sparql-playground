(function (angular, undefined) {
	'use strict';

	/*
	* extend geomap service
	*/

	angular.module('geomap.service')
		.factory('shapeRenderer', shapeRendererFactory);

	//
	// implement shapeRenderer factory
	shapeRendererFactory.$inject=["geomapManipulation"]
	function shapeRendererFactory(geomapManipulation) {
		class ShapeRenderer {
			constructor() {
				this.bulkRenderGroupKeys = ["POINT", "LINESTRING", "POLYGON"]//-HEALTY", "POLYGON-AFFECTED", "POLYGON-HAZARD"]
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

			addElementToBulkRender(element, column, lineColor, polygoneColor, elementOrigin) {
				let unparsedElement = element[column].value;
				if(unparsedElement == "POINT EMPTY") {
					return
				}
				let parsedElement = parseElement(unparsedElement);
				this.addParsedElementToBulkRender(parsedElement, lineColor, polygoneColor, elementOrigin)
			}

			//todo: get rid of it
			addParsedElementToBulkRender(parsedElement, lineColor, polygoneColor, elementOrigin) {
				if(!this.bulkRenderMap.has(parsedElement.Name)) {
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
				let	bulkRenderGroup = this.bulkRenderMap.get(parsedElement.Name);
				//
				//change group
				switch(parsedElement.Name) {
					case "POINT":
						let point = geomapManipulation.createPoint(extractPointCoordinates(parsedElement));
						bulkRenderGroup.push(point);
						//TODO: think about elementOrigin sub-group
						break;

					case "LINESTRING":
						let line = geomapManipulation.createLine(extractLineCoordinates(parsedElement), lineColor);
						bulkRenderGroup.push(line);
						//TODO: think about elementOrigin sub-group
						break;

					case "POLYGON":
						let polygon = geomapManipulation.createPolygon(extractPolygonCoordinates(parsedElement), polygoneColor);
						if(bulkRenderGroup.has(elementOrigin) == false) {
							bulkRenderGroup.set(elementOrigin, []);
						}
						let subGroup = bulkRenderGroup.get(elementOrigin);
						subGroup.push(polygon);
						bulkRenderGroup.set(elementOrigin, subGroup);
						break;

					default:
						break;
				}
				//
				//set group back to map
				this.bulkRenderMap.set(parsedElement.Name, bulkRenderGroup);
			}

			finishBulkRender() {
				for(const groupKey of this.bulkRenderGroupKeys) {
					let	bulkRenderGroup = this.bulkRenderMap.get(groupKey);
					addMultipleElementsToGeomap(groupKey, bulkRenderGroup);
				}
				this.bulkRenderMap.clear();
			}

			//
			//depreciated
			renderSingleElement(element, column) {
				let unparsedElement = element[column].value;
				if(unparsedElement == "POINT EMPTY") {
					return
				}
				let parsedElement = parseElement(unparsedElement);
				addSingleElementToGeomap(parsedElement);
			}

			routeChangeStart() {
				return true;
			}
		}

		//
		//HELPER FUNCTIONS
		function parseElement(unparsedElement) {
			let parsedElement = new Object;
			let splittedElement = unparsedElement.substring(0, unparsedElement.length - 1).split(" (");
			parsedElement.Name = splittedElement[0].trim();
			parsedElement.Coordinates = parseCoordinates(splittedElement[1]);
			return parsedElement;
		};

		function parseCoordinates(unparsedCoordinates) {
			let coordinates = new Object;
			coordinates.Shapes = [];
			if(unparsedCoordinates.charAt(0) ==='('){
				let splittedByComma = unparsedCoordinates.split("), (");
				if(splittedByComma.length > 1){
					splittedByComma.forEach(shape => {
						shape = removeParentheses(shape);
						coordinates.Shapes.push(parseShape(shape));
					});
				}
				else{
					coordinates.Shapes.push(parseShape(unparsedCoordinates));
				}
			}
			else {
				coordinates.Shapes.push(parseShape(unparsedCoordinates));
			}

			return coordinates;
		};

		function parseShape(coordinates) {
			let shape = [];
			coordinates = removeParentheses(coordinates);
			let splittedCoordinates = coordinates.split(', ');
			splittedCoordinates.forEach(pointPair => {
				let splittedPointPair = pointPair.trim().split(' ');
				shape.push({ x: splittedPointPair[1].trim(), y: splittedPointPair[0].trim() });
			});
			return shape;
		};

		function removeParentheses(str) {
			str = str.replaceAll("(", "");
			str = str.replaceAll(")", "");
			return str;
		};

		function extractPointCoordinates(parsedElement) {
			let point = parsedElement.Coordinates.Shapes[0][0];
			geomapManipulation.setMapViewLatitude(point.x);
			geomapManipulation.setMapViewLongitude(point.y);

			return point;
		};

		function extractLineCoordinates(parsedElement) {
			let lineParsedCoords = parsedElement.Coordinates.Shapes[0];
			let lineCoordinates = [];
			lineParsedCoords.forEach(point => {
				lineCoordinates.push([point.x, point.y]);
				geomapManipulation.setMapViewLatitude(point.x);
				geomapManipulation.setMapViewLongitude(point.y);
			});

			return lineCoordinates;
		};

		function extractPolygonCoordinates(parsedElement) {
			let polygonParsedCoords = parsedElement.Coordinates.Shapes[0];
			let polygonCoordinates = [];
			polygonParsedCoords.forEach(point => {
				polygonCoordinates.push([point.x, point.y]);
				geomapManipulation.setMapViewLatitude(point.x);
				geomapManipulation.setMapViewLongitude(point.y);
			});

			return polygonCoordinates;
		};

		function addSingleElementToGeomap(parsedElement) {
			switch(parsedElement.Name) {
				case "POINT":
					let point = geomapManipulation.createPoint(extractPointCoordinates(parsedElement));
					geomapManipulation.addSingleElementToGeomap(point);
					break;

				case "LINESTRING":
					let line = geomapManipulation.createLine(extractLineCoordinates(parsedElement), { color: 'green' });
					geomapManipulation.addSingleElementToGeomap(line);
					break;

				case "POLYGON":
					let polygon = geomapManipulation.createPolygon(extractPolygonCoordinates(parsedElement), { color: 'blue' })
					geomapManipulation.addSingleElementToGeomap(polygon);
					break;

				default:
					break;
			}
		};

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

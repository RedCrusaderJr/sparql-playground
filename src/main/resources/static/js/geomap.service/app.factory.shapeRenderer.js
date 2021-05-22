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
					this.bulkRenderMap.set(key, []);
				}
			}

			addElementToBulkRender(element, column, lineColor, polygoneColor) {
				let parsedElement = parseElement(element, column);
				this.addParsedElementToBulkRender(parsedElement, lineColor, polygoneColor)
			}

			//todo: get rid of it
			addParsedElementToBulkRender(parsedElement, lineColor, polygoneColor, elementType) {
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
						break;
					case "LINESTRING":
						let line = geomapManipulation.createLine(extractLineCoordinates(parsedElement), lineColor);
						bulkRenderGroup.push(line);
						break;
					case "POLYGON":
						let polygon = geomapManipulation.createPolygon(extractPolygonCoordinates(parsedElement), polygoneColor)
						bulkRenderGroup.push(polygon);
						break;

						// case "POLYGON":
						// let polygon = geomapManipulation.createPolygon(extractPolygonCoordinates(parsedElement), polygoneColor)
						// bulkRenderGroup.push(polygon);
						// break;

						// case "POLYGON":
						// let polygon = geomapManipulation.createPolygon(extractPolygonCoordinates(parsedElement), polygoneColor)
						// bulkRenderGroup.push(polygon);
						// break;
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

				//geomapManipulation.exportGeojson();
				this.bulkRenderMap.clear();
			}

			//
			//depreciated
			renderSingleElement(element, column) {
				let parsedElement = parseElement(element, column);
				addSingleElementToGeomap(parsedElement);
			}
		}

		//
		//HELPER FUNCTIONS
		function parseElement(element, column) {
			let splittedElement = element[column].value.substring(0, element[column].value.length - 1).split(" (");

			let parsedElement = new Object;
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
				shape.push({ x: splittedPointPair[0].trim(), y: splittedPointPair[1].trim() });
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
					//geomapManipulation.addMultiplePolygonsToGeomap(bulkRenderGroup, polygonType);
					geomapManipulation.addMultiplePolygonsToGeomap(bulkRenderGroup);
					break;

				default:
					break;
			}
		};

		return new ShapeRenderer();
	};

})(angular);

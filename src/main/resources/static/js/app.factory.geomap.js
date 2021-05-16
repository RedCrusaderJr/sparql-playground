(function (angular, undefined) {
	'use strict';

	/*
	* create geomap service
	*/

	angular.module('geomap.service', [])
		   .factory('shapeRenderer', shapeRendererFactory)
		   .factory('geomapManipulation', geomapManipulationFactory);

	//
	// implement shapeRenderer factory
	shapeRendererFactory.$inject=["$q", "geomapManipulation"]
	function shapeRendererFactory($q, geomapManipulation) {
		class ShapeRenderer {
			constructor() {
				this.bulkRenderGroupKeys = ["POINT", "LINESTRING", "POLYGON"]
				this.bulkRenderMap = new Map();
				//
				// wrap promise to this object
				this.$promise = $q.when(this);
				//
				// manage cancel
				this.canceler = $q.defer();
			}

			startBulkRender() {
				this.bulkRenderMap.clear();
				for(const key of this.bulkRenderGroupKeys) {
					this.bulkRenderMap.set(key, []);
				}
			}

			addElementToBulkRender(element, column) {
				let parsedElement = parseElement(element, column);

				if(!this.bulkRenderMap.has(parsedElement.Name)) {
					console.log("KEY: '" + parsedElement.Name + "' not present in 'bulkRenderMap'.");
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
						let line = geomapManipulation.createLine(extractLineCoordinates(parsedElement), { color: 'green' });
						bulkRenderGroup.push(line);
						break;

					case "POLYGON":
						let polygon = geomapManipulation.createPolygon(extractPolygonCoordinates(parsedElement), { color: 'blue' })
						bulkRenderGroup.push(polygon);
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
					geomapManipulation.addMultipleElementsToGeomap(bulkRenderGroup);
				}

				geomapManipulation.exportGeojson();
				this.bulkRenderMap.clear();
			}

			renderSingleElement(element, column) {
				let parsedElement = parseElement(element, column);
				addElementToGeomap(parsedElement);
			}
		}

		//
		//HELPER FUNCTIONS
		function parseElement(element, column) {
			let parsedElement = new Object;
			let splittedElement = element[column].value.substring(0, element[column].value.length - 1).split(" (");
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

		function addElementToGeomap(parsedElement) {
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

		return new ShapeRenderer();
	};

	//
	// implement geomapManipulation factory
	geomapManipulationFactory.$inject=["$http", "$q", "config"]
	function geomapManipulationFactory($http, $q, config) {
		class GeomapManipulation {
			constructor() {
				this.geomap = {};
				this.drawnItems = {};

				this.mapViewZoom = {};
				this.mapViewLatitude = {};
				this.mapViewLongitude = {};

				//
				//URLs
				this.exportURL = config.sparql.geoExport;
				this.importURL = config.sparql.geoImport;

				//
				// wrap promise to this object
				this.$promise = $q.when(this);
				//
				// manage cancel
				this.canceler = $q.defer();
			}

			setMapViewLatitude(value) {
				this.mapViewLatitude = value;
			}

			setMapViewLongitude(value) {
				this.mapViewLongitude = value;
			}

			getMapInstance(latitude, longitude, zoom) {
				this.geomap = createMap(latitude, longitude, zoom);

				this.drawnItems = new L.FeatureGroup();
				this.geomap.addLayer(this.drawnItems);

				this.mapViewZoom = zoom;
				this.mapViewLatitude = latitude;
				this.mapViewLongitude = longitude;

				return this.geomap;
			}

			setCurrentView() {
				this.geomap.setView([this.mapViewLatitude, this.mapViewLongitude], this.mapViewZoom);
			}

			setView(latitude, longitude, zoom) {
				this.geomap.setView([latitude, longitude], zoom);
			}

			createPoint(pointCoords) {
				return new L.marker([pointCoords.x, pointCoords.y]).bindPopup("POINT ("+ pointCoords.x + " " + pointCoords.y + ")");
			}

			createLine(lineCoords, color) {
				return new L.polyline(lineCoords, color).bindPopup("LINESTRING (" + lineCoords + ")");
			}

			createPolygon(polygonCoords, color) {
				return new L.polygon(polygonCoords, color).bindPopup("POLYGON ((" + polygonCoords + "))");
			}

			addSingleElementToGeomap(element) {
				element.addTo(this.drawnItems);
			}

			addMultipleElementsToGeomap(elementArray) {
				L.featureGroup(elementArray).addTo(this.drawnItems);
			}

			clearDrawnItems() {
				this.drawnItems.clearLayers();
			}

			exportGeojson() {
				var collection = this.drawnItems.toGeoJSON();

				var bounds = this.geomap.getBounds();
				collection.bbox = [[
					bounds.getSouthWest().lng,
					bounds.getSouthWest().lat,
					bounds.getNorthEast().lng,
					bounds.getNorthEast().lat
				]];

				console.log(collection);
				console.log(this.exportURL);

				return $http.post(this.exportURL, JSON.stringify(collection))
				.then(function (response) {
					alert(response.data + " successfull EXPORT");
				}, function (error) {
					alert("error " + error.data.responseText);
				});
			}

			//, { "Accept" : "application/json" }
			importGeojson() {
				let self = this;
				return $http.get(this.importURL)
				.then(function (response) {
					//alert(response.data + " successfull IMPORT");
					if(typeof response.data.features != 'undefined') {
						L.geoJSON(response.data).addTo(self.geomap);
						self.mapViewLatitude = response.data.features[0].geometry.coordinates[1];
						self.mapViewLongitude = response.data.features[0].geometry.coordinates[0];
						self.setCurrentView();
					}

				}, function (error) {
					alert("error " + error.data.responseText);
				});
			}
		}

		//
		//HELPER FUNCTIONS
		function createMap(latitude, longitude, zoom) {
			let geomap = L.map('geomapDiv').setView([latitude, longitude], zoom);

			L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw', {
				maxZoom: 18,
				attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, ' +
				'Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
				id: 'mapbox/streets-v11',
				tileSize: 512,
				zoomOffset: -1
			}).addTo(geomap);
			return geomap;
		}

		return new GeomapManipulation();
	}

})(angular);

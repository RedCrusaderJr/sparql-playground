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
					addMultipleElementsToGeomap(groupKey, bulkRenderGroup);
				}

				geomapManipulation.exportGeojson();
				this.bulkRenderMap.clear();
			}

			renderSingleElement(element, column) {
				let parsedElement = parseElement(element, column);
				addSingleElementToGeomap(parsedElement);
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
					//let point = geomapManipulation.createPoint(extractPointCoordinates(parsedElement));
					geomapManipulation.addMultipleMarkersToGeomap(bulkRenderGroup);
					break;

				case "LINESTRING":
					//let line = geomapManipulation.createLine(extractLineCoordinates(parsedElement), { color: 'green' });
					geomapManipulation.addMultipleLinesToGeomap(bulkRenderGroup);
					break;

				case "POLYGON":
					//let polygon = geomapManipulation.createPolygon(extractPolygonCoordinates(parsedElement), { color: 'blue' })
					geomapManipulation.addMultiplePolygonsToGeomap(bulkRenderGroup);
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
				this.markerGroup = {};
				this.lineGroup = {};
				this.polygonGroup = {};

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

			//#region Get/Set
			getMapInstance(latitude, longitude, zoom) {
				this.geomap = createMap(latitude, longitude, zoom);
				//this.markerGroup = L.markerClusterGroup().addTo(this.geomap);
				this.markerGroup = L.layerGroup().addTo(this.geomap);
				this.lineGroup = L.layerGroup().addTo(this.geomap);
				this.polygonGroup = L.layerGroup().addTo(this.geomap);

				this.mapViewZoom = zoom;
				this.mapViewLatitude = latitude;
				this.mapViewLongitude = longitude;

				return this.geomap;
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

			addSingleLineToGeomap(element) {
				element.addTo(this.lineGroup);
			}

			addSinglePolygonToGeomap(element) {
				element.addTo(this.polygonGroup);
			}

			addMultipleMarkersToGeomap(elementArray) {
				L.featureGroup(elementArray).addTo(this.markerGroup);
			}

			addMultipleLinesToGeomap(elementArray) {
				L.featureGroup(elementArray).addTo(this.lineGroup);
			}

			addMultiplePolygonsToGeomap(elementArray) {
				L.featureGroup(elementArray).addTo(this.polygonGroup);
			}
			//#endregion

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

				let exportMap = new Map();
				let markerCollection = this.markerGroup.toGeoJSON();
				markerCollection.bbox = bbox;
				exportMap.set('POINT', markerCollection);

				let lineCollection = this.lineGroup.toGeoJSON();
				lineCollection.bbox = bbox;
				exportMap.set('LINESTRING', lineCollection);

				let polygonCollection = this.polygonGroup.toGeoJSON();
				polygonCollection.bbox = bbox;
				exportMap.set('POLYGON', polygonCollection);

				exportMap.set("VIEW", [this.mapViewLatitude, this.mapViewLongitude, this.mapViewZoom]);

				console.log(exportMap);
				console.log(this.exportURL);

				return $http.post(this.exportURL, JSON.stringify(exportMap, replacer))
				.then(function (response) {
					//TODO: if anything
					//alert(response.data + " successfull EXPORT");
				}, function (error) {
					alert("error " + error.data.responseText);
				});
			}

			importGeojson() {
				let self = this;
				return $http.get(this.importURL)
				.then(function (response) {
					let geojsonMap = JSON.parse(JSON.stringify(response.data), reviver);
					if(geojsonMap.length == 0) {
						return;
					}

					for(const key of geojsonMap.keys()) {
						let elements = geojsonMap.get(key);

						switch(key) {
							case "POINT":
								L.geoJSON(elements).addTo(self.markerGroup);
								break;

							case "LINESTRING":
								L.geoJSON(elements).addTo(self.lineGroup);
								self.lineGroup.eachLayer(layer => {
									layer.setStyle({
										color: 'green'
									});
								});
								break;

							case "POLYGON":
								L.geoJSON(elements).addTo(self.polygonGroup);
								self.polygonGroup.eachLayer(layer => {
									layer.setStyle({
										color: 'blue'
									});
								});
								break;

							case "VIEW":
								self.mapViewLatitude = elements[0];
								self.mapViewLongitude = elements[1];
								self.mapViewZoom = elements[2];
								self.setCurrentView();
								break;

							default:
								break;
						}
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

		function replacer(key, value) {
			if(value instanceof Map) {
			  return {
				dataType: 'Map',
				value: Array.from(value.entries()), // or with spread: value: [...value]
			  };
			} else {
			  return value;
			}
		  }

		  function reviver(key, value) {
			if(typeof value === 'object' && value !== null) {
			  if (value.dataType === 'Map') {
				return new Map(value.value);
			  }
			}
			return value;
		  }

		return new GeomapManipulation();
	}

})(angular);

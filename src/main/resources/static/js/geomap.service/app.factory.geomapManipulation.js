(function (angular, undefined) {
	'use strict';

	/*
	* create geomap service
	*/

	angular.module('geomap.service', [])
		.factory('geomapManipulation', geomapManipulationFactory);

	//
	// implement geomapManipulation factory
	geomapManipulationFactory.$inject=["$http", "config"]
	function geomapManipulationFactory($http, config) {
		class GeomapManipulation {
			constructor() {
				this.geomap = {};
				this.markerGroup = {};
				this.lineGroup = {};
				this.healtyPolygonGroup = {};
				this.affectedPolygonGroup = {};
				this.hazardPolygonGroup = {};

				this.mapViewZoom = {};
				this.mapViewLatitude = {};
				this.mapViewLongitude = {};

				//
				//URLs
				this.exportURL = config.sparql.geoExport;
				this.importURL = config.sparql.geoImport;
			}

			//#region Get/Set
			getMapInstance(latitude, longitude, zoom) {
				this.geomap = createMap(latitude, longitude, zoom);
				//this.markerGroup = L.markerClusterGroup().addTo(this.geomap);
				this.markerGroup = L.layerGroup().addTo(this.geomap);
				this.lineGroup = L.layerGroup().addTo(this.geomap);
				this.healtyPolygonGroup = L.layerGroup().addTo(this.geomap);
				this.affectedPolygonGroup = L.layerGroup().addTo(this.geomap);
				this.hazardPolygonGroup = L.layerGroup().addTo(this.geomap);

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

			setCurrentView() {
				this.geomap.setView([this.mapViewLatitude, this.mapViewLongitude], this.mapViewZoom);
			}

			setView(latitude, longitude, zoom) {
				this.geomap.setView([latitude, longitude], zoom);
			}
			//#endregion

			//#region Create Element
			createPoint(pointCoords) {
				return new L.marker([pointCoords.x, pointCoords.y]).bindPopup("POINT ("+ pointCoords.x + " " + pointCoords.y + ")");
			}

			createLine(lineCoords, color) {
				return new L.polyline(lineCoords, color).bindPopup("LINESTRING (" + lineCoords + ")");
			}

			createPolygon(polygonCoords, color) {
				return new L.polygon(polygonCoords, color).bindPopup("POLYGON ((" + polygonCoords + "))");
			}
			//#endregion

			//#region Add Elements to Geomap
			addSingleElementToGeomap(element) {
				element.addTo(this.drawnItems);
			}

			addSingleLineToGeomap(element) {
				element.addTo(this.lineGroup);
			}

			addSinglePolygonToGeomap(element, polygonType) {
				if(typeof polygonType == 'undefined') {
					polygonType = "HEALTY";
				}

				switch(polygonType) {
					case "HEALTY":
						element.addTo(this.healtyPolygonGroup);
						break;
					case "AFFECTED":
						element.addTo(this.affectedPolygonGroup);
						break;
					case "HAZARD":
						element.addTo(this.hazardPolygonGroup);
						break;
					default:
						break;
				}
			}

			addMultipleMarkersToGeomap(elementArray) {
				L.featureGroup(elementArray).addTo(this.markerGroup);
			}

			addMultipleLinesToGeomap(elementArray) {
				L.featureGroup(elementArray).addTo(this.lineGroup);
			}

			addMultiplePolygonsToGeomap(elementArray, polygonType) {
				if(typeof polygonType == 'undefined') {
					polygonType = "HEALTY";
				}

				switch(polygonType) {
					case "HEALTY":
						L.featureGroup(elementArray).addTo(this.healtyPolygonGroup);
						break;
					case "AFFECTED":
						L.featureGroup(elementArray).addTo(this.affectedPolygonGroup);
						break;
					case "HAZARD":
						L.featureGroup(elementArray).addTo(this.hazardPolygonGroup);
						break;
					default:
						break;
				}
			}
			//#endregion

			clearDrawnItems() {
				this.markerGroup.clearLayers();
				this.lineGroup.clearLayers();
				this.healtyPolygonGroup.clearLayers();
				this.affectedPolygonGroup.clearLayers();
				this.hazardPolygonGroup.clearLayers();
			}

			//#region Export/Import
			exportGeojson() {
				let bounds = this.geomap.getBounds();
				let bbox = [[
					bounds.getSouthWest().lng,
					bounds.getSouthWest().lat,
					bounds.getNorthEast().lng,
					bounds.getNorthEast().lat
				]];

				let exportMap = new Map();
				//
				//POINTS
				let markerCollection = this.markerGroup.toGeoJSON();
				markerCollection.bbox = bbox;
				exportMap.set('POINT', markerCollection);
				//
				//LINES
				let lineCollection = this.lineGroup.toGeoJSON();
				lineCollection.bbox = bbox;
				exportMap.set('LINESTRING', lineCollection);
				//
				//POLYGONS
				let polygoneMap = new Map();
				let healtyPolygonCollection = this.healtyPolygonGroup.toGeoJSON();
				healtyPolygonCollection.bbox = bbox;
				polygoneMap.set("HEALTY", healtyPolygonCollection);
				let affectedPolygonCollection = this.affectedPolygonGroup.toGeoJSON();
				affectedPolygonCollection.bbox = bbox;
				polygoneMap.set("AFFECTED", affectedPolygonCollection);
				let hazardPolygonCollection = this.hazardPolygonGroup.toGeoJSON();
				hazardPolygonCollection.bbox = bbox;
				polygoneMap.set("HAZARD", hazardPolygonCollection);
				exportMap.set('POLYGON', polygoneMap);

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
					let lineColor = { color: 'green' };

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
									layer.setStyle(lineColor);
								});
								break;
							case "POLYGON":
								renderPolygons(elements, self);
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
					alert("error: " + error.data.responseText);
				});
			}
			//#endregion
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

		function renderPolygons(polygonCollections, self) {
			let healtyPolygoneColor = { color: 'blue' };
			let affectedPolygoneColor = { color: 'red' };
			let hazardPolygoneColor = { color: 'orange' };

			for(const key of polygonCollections.keys()) {
				let polygonCollection = polygonCollections.get(key);

				switch(key) {
					case "HEALTY":
						L.geoJSON(polygonCollection).addTo(self.healtyPolygonGroup);
						self.healtyPolygonGroup.eachLayer(layer => {
							layer.setStyle(healtyPolygoneColor);
						});
						break;
					case "AFFECTED":
						L.geoJSON(polygonCollection).addTo(self.affectedPolygonGroup);
						self.affectedPolygonGroup.eachLayer(layer => {
							layer.setStyle(affectedPolygoneColor);
						});
						break;
					case "HAZARD":
						L.geoJSON(polygonCollection).addTo(self.hazardPolygonGroup);
						self.hazardPolygonGroup.eachLayer(layer => {
							layer.setStyle(hazardPolygoneColor);
						});
						break;
					default:
						break;
				}
			}
		}

		return new GeomapManipulation();
	}

})(angular);

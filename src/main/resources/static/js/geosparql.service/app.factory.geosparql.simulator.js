(function (angular, undefined) {
	'use strict';

	/*
	* create simulator service
	*/

	angular.module('geosparql.simulator.service', [])
		   .factory('geoSparqlSimulator', simulatorFactory);

	//
	// implement simulator factory
	simulatorFactory.$inject=['$http', "config", "stopwatch", "shapeRenderer", "geomapManipulation"]
	function simulatorFactory($http, config, stopwatch, shapeRenderer, geomapManipulation) {
		class Simulator {
			constructor() {
				//
				//URLs
				this.weatherStartNew = config.sparql.weatherStartNew;
				this.weatherNext = config.sparql.weatherNext;
				this.weatherReset = config.sparql.weatherReset;
				this.weatherStop = config.sparql.weatherStop;

				this.simulatorStart = config.sparql.simulatorStart;
				this.simulatorEvaluate = config.sparql.simulatorEvaluate;
				this.simulatorStop = config.sparql.simulatorStop;
			}

			//#region Getters
			getWeatherStartNew() {
				return this.weatherStartNew;
			}

			getWeatherNext() {
				return this.weatherNext;
			}

			getWeatherStop() {
				return this.weatherStop;
			}

			getSimulatorStart() {
				return this.simulatorStart;
			}

			getSimulatorEvaluate() {
				return this.simulatorEvaluate;
			}

			getSimulatorStop() {
				return this.simulatorStop;
			}
			//#endregion

			start(elapseInterval) {
				console.log("'Start' button pressed.");

				let self = this;
				return $http.get(self.weatherStartNew).then(function(response) {
					if(response.data == false) {
						console.error("WeatherService: START NEW simulation FAILED.");
						stopwatch.stop();
						return false;
					}

					console.log("WeatherService: START NEW simulation SUCCESSFUL.");
					$http.get(self.simulatorStart).then(function(response) {
						if(response.data == false) {
							console.error("GeoSparqlSimulator: START simulation FAILED.");
							stopwatch.stop();
							return false;
						}

						console.log("GeoSparqlSimulator: START simulation SUCCESSFUL.");
						stopwatch.start(elapseInterval, elapsedCallback);
						return true;

					}, function (error) {
						console.error("simulatorStart => error: " + error.data.responseText);
						stopwatch.stop();
						return false;
					});

				}, function (error) {
					console.error("weatherStartNew => error: " + error.data.responseText);
					stopwatch.stop();
					return false;
				});
			}

			pause() {
				console.log("'Pause' button pressed.");

				stopwatch.pause();
				return true;
			}

			reset() {
				console.log("'Reset' button pressed.");

				stopwatch.pause();

				let self = this;
				return $http.get(self.weatherReset).then(function(response) {
					if(response.data == false) {
						console.error("WeatherService: RESET simulation FAILED.");
						stopwatch.stop();
						return false;
					}

					console.log("WeatherService: RESET simulation SUCCESSFUL.");
					$http.get(self.simulatorEvaluate).then(function(response) {
						if(response.data == false) {
							console.error("GeoSparqlSimulator: EVALUATE simulation FAILED.");
							stopwatch.stop();
							return false;
						}

						console.log("GeoSparqlSimulator: START simulation SUCCESSFUL.");
						stopwatch.reset();
						return true;

					}, function (error) {
						console.error("simulatorEvaluate => error: " + error.data.responseText);
						stopwatch.stop();
						return false;
					});

				}, function (error) {
					console.error("weatherReset => error: " + error.data.responseText);
					stopwatch.stop();
					return false;
				});
			}

			stop() {
				console.log("'Stop' button pressed.");

				stopwatch.stop();

				let self = this;
				return $http.get(self.weatherStop).then(function(response) {
					if(response.data == false) {
						console.error("WeatherService: STOP simulation FAILED.");
						return false;
					}

					console.log("WeatherService: STOP simulation SUCCESSFUL.");
					$http.get(self.simulatorStop).then(function(response) {
						if(response.data == false) {
							console.error("GeoSparqlSimulator: STOP simulation FAILED.");
							return false;
						}

						console.log("GeoSparqlSimulator: STOP simulation SUCCESSFUL.");
						return true;

					}, function (error) {
						console.error("simulatorStop => error: " + error.data.responseText);
						return false;
					});

				}, function (error) {
					console.error("weatherStop => error: " + error.data.responseText);
					return false;
				});
			}
		}

		var instance = new Simulator();
		var evaluationInProcess = false;

		function elapsedCallback() {
			console.log("elapsedCallback called");
			return $http.get(instance.getWeatherNext()).then(function(response) {
				if(response.data == false) {
					console.error("WeatherService: NEXT iteration FAILED.");
					return;
				}

				if(evaluationInProcess == true) {
					return;
				}

				evaluationInProcess = true;

				console.log("WeatherService: NEXT iteration SUCCESSFUL.");
				$http.get(instance.getSimulatorEvaluate()).then(function(response) {
					if(typeof response.data == 'undefined') {
						console.error("GeoSparqlSimulator: EVALUATE current iteration FAILED.");
						return;
					}

					console.log("GeoSparqlSimulator: EVALUATE current iteration SUCCESSFUL.");
					evaluationInProcess = false;

					processEvaluationData(response.data);

				}, function (error) {
					evaluationInProcess = false;
					console.error("simulatorEvaluate => error: " + error.data.responseText);
				});

			}, function (error) {
				evaluationInProcess = false;
				console.error("weatherNext => error: " + error.data.responseText);
			});
		}

		function processEvaluationData(dataMap) {
			geomapManipulation.clearDrawnItems();
			shapeRenderer.startBulkRender();

			if(typeof dataMap.healty != 'undefined') {
				let lineColor = { color: 'green' };
				let polygonColor = { color: 'blue' };
				prepareBulkRender(dataMap.healty, lineColor, polygonColor, "HEALTY");
			}

			if(typeof dataMap.affected != 'undefined') {
				let lineColor = { color: 'green' };
				let polygonColor = { color: 'red' };
				prepareBulkRender(dataMap.affected, lineColor, polygonColor, "AFFECTED");
			}

			if(typeof dataMap.hazard != 'undefined') {
				let lineColor = { color: 'green' };
				let polygonColor = { color: 'orange' };
				prepareBulkRender(dataMap.hazard, lineColor, polygonColor, "HAZARD");
			}

			shapeRenderer.finishBulkRender();
			geomapManipulation.setCurrentView();
			geomapManipulation.exportGeojson();
		}

		function prepareBulkRender(queryResult, lineColor, polygonColor, elementOrigin) {
			var geoSpatialColumnHeaders = queryResult.names.filter(function (name) {
				var finder = 'g_';
				return eval('/' + finder + '/').test(name);
			});

			if (geoSpatialColumnHeaders.length == 0) {
				return;
			}

			var bindings = queryResult.bindings;

			geoSpatialColumnHeaders.forEach(columnName => {
				let columnNameVar = columnName;
				bindings.forEach(binding => {
					let parsedElement = parseElement(binding[columnNameVar]);
					shapeRenderer.addParsedElementToBulkRender(parsedElement, lineColor, polygonColor, elementOrigin);
				});
			});
		}

		function parseElement(element) {
			let wktRegex = /.*(?<point>POINT) \((?<pointX>.*) (?<pointY>.*)\).*|.*(?<line>LINESTRING) \((?<lineX1>.*) (?<lineY1>.*), (?<lineX2>.*) (?<lineY2>.*)\).*|.*(?<polygon>POLYGON) \(\((?<polygonCoordinates>.*)\)\).*/
			let wktMatch = element.match(wktRegex);

			if (typeof wktMatch.groups.point == 'undefined'
			&& typeof wktMatch.groups.line == 'undefined'
			&& typeof wktMatch.groups.polygon == 'undefined') {
				return;
			}

			let parsedElement = new Object;
			if(wktMatch.groups.point == "POINT") {
				parsedElement.Name = wktMatch.groups.point;
				parsedElement.Coordinates = {
					Shapes: [[{
							x: wktMatch.groups.pointX.trim(),
							y: wktMatch.groups.pointY.trim(),
						}]
					]
				};
			}
			else if(wktMatch.groups.line == "LINESTRING") {
				parsedElement.Name = wktMatch.groups.line;
				parsedElement.Coordinates = {
					Shapes: [[{
							x: wktMatch.groups.lineX1.trim(),
							y: wktMatch.groups.lineY1.trim(),
						}, {
							x: wktMatch.groups.lineX2.trim(),
							y: wktMatch.groups.lineY2.trim(),
						}]
					]
				};
			}
			else if(wktMatch.groups.polygon == "POLYGON") {
				parsedElement.Name = wktMatch.groups.polygon;
				parsedElement.Coordinates = {
					Shapes: []
				};

				let shape = [];
				wktMatch.groups.polygonCoordinates.split(', ').forEach(function(point) {
					let pointParts = point.split(' ');
					shape.push({ x: pointParts[0].trim(), y: pointParts[1].trim()});
				});
				parsedElement.Coordinates.Shapes.push(shape);
			}

			return parsedElement;
		}

		return instance;
	}

})(angular);

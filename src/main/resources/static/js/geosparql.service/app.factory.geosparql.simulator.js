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
				this.weatherStartNewUrl = config.sparql.weatherStartNew;
				this.weatherNextUrl = config.sparql.weatherNext;
				this.weatherResetUrl = config.sparql.weatherReset;
				this.weatherStopUrl = config.sparql.weatherStop;

				this.simulatorStartUrl = config.sparql.simulatorStart;
				this.simulatorEvaluateUrl = config.sparql.simulatorEvaluate;
				this.simulatorStopUrl = config.sparql.simulatorStop;
			}

			//#region URL Getters
			getWeatherStartNewUrl() {
				return this.weatherStartNewUrl;
			}

			getWeatherNextUrl() {
				return this.weatherNextUrl;
			}

			getWeatherStopUrl() {
				return this.weatherStopUrl;
			}

			getSimulatorStartUrl() {
				return this.simulatorStartUrl;
			}

			getSimulatorEvaluateUrl() {
				return this.simulatorEvaluateUrl;
			}

			getSimulatorStopUrl() {
				return this.simulatorStopUrl;
			}
			//#endregion

			start(elapseInterval) {
				console.log("'Start' button pressed.");

				let self = this;
				geomapManipulation.enableViewSetting();
				return $http.get(self.weatherStartNewUrl).then(function(response) {
					if(response.data == false) {
						console.error("WeatherService: START NEW simulation FAILED.");
						stopwatch.stop();
						return false;
					}

					console.log("WeatherService: START NEW simulation SUCCESSFUL.");
					return $http.get(self.simulatorStartUrl).then(function(response) {
						if(response.data == false) {
							console.error("GeoSparqlSimulator: START simulation FAILED.");
							stopwatch.stop();
							return false;
						}

						console.log("GeoSparqlSimulator: START simulation SUCCESSFUL.");
						return callSimulatorEvaluate(self.getSimulatorEvaluateUrl()).then(function() {
							geomapManipulation.disableViewSetting();
							stopwatch.start(elapseInterval, elapsedCallback);
							return true;
						});

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

				let self = this;
				stopwatch.pause();
				geomapManipulation.enableViewSetting();
				return $http.get(self.weatherResetUrl).then(function(response) {
					if(response.data == false) {
						console.error("WeatherService: RESET simulation FAILED.");
						stopwatch.stop();
						return false;
					}

					console.log("WeatherService: RESET simulation SUCCESSFUL.");
					return callSimulatorEvaluate(self.simulatorEvaluateUrl).then(function() {
						geomapManipulation.disableViewSetting();
						stopwatch.reset();
						return true;
					});

				}, function (error) {
					console.error("weatherReset => error: " + error.data.responseText);
					stopwatch.stop();
					return false;
				});
			}

			stop() {
				console.log("'Stop' button pressed.");

				let self = this;
				stopwatch.stop();
				//geomapManipulation.enableViewSetting();
				return $http.get(self.weatherStopUrl).then(function(response) {
					if(response.data == false) {
						console.error("WeatherService: STOP simulation FAILED.");
						return false;
					}

					console.log("WeatherService: STOP simulation SUCCESSFUL.");
					return $http.get(self.simulatorStopUrl).then(function(response) {
						if(response.data == false) {
							console.error("GeoSparqlSimulator: STOP simulation FAILED.");
							return false;
						}

						console.log("GeoSparqlSimulator: STOP simulation SUCCESSFUL.");

						//FOR CLEANUP
						return callSimulatorEvaluate(self.simulatorEvaluateUrl);

					}, function (error) {
						console.error("simulatorStop => error: " + error.data.responseText);
						return false;
					});

				}, function (error) {
					console.error("weatherStop => error: " + error.data.responseText);
					return false;
				});
			}

			routeChangeStart() {
				this.stop();
				return true;
			}
		}

		var instance = new Simulator();
		var evaluationInProcess = false;

		function elapsedCallback() {
			console.log("elapsedCallback called");
			return $http.get(instance.getWeatherNextUrl()).then(function(response) {
				if(response.data == false) {
					console.error("WeatherService: NEXT iteration FAILED.");
					return;
				}

				if(evaluationInProcess == true) {
					return;
				}

				evaluationInProcess = true;

				console.log("WeatherService: NEXT iteration SUCCESSFUL.");
				return callSimulatorEvaluate(instance.getSimulatorEvaluateUrl());

			}, function (error) {
				evaluationInProcess = false;
				console.error("weatherNext => error: " + error.data.responseText);
			});
		}

		function callSimulatorEvaluate(simulatorEvaluateUrl) {
			return $http.get(simulatorEvaluateUrl).then(function(response) {
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

		}

		function processEvaluationData(dataMap) {
			geomapManipulation.clearDrawnItems();
			shapeRenderer.startBulkRender();

			if(typeof dataMap.healthy != 'undefined') {
				let lineColor = { color: 'green' };
				let polygonColor = { color: 'blue' };
				prepareBulkRender(dataMap.healthy, lineColor, polygonColor, "HEALTHY");
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
					shapeRenderer.addElementToBulkRender(binding[columnNameVar], lineColor, polygonColor, elementOrigin);
				});
			});
		}

		return instance;
	}

})(angular);

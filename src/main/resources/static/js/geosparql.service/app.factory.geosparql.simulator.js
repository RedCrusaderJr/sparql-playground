(function (angular, undefined) {
	'use strict';

	/*
	* create simulator service
	*/

	angular.module('geosparql.simulator.service', [])
		   .factory('geoSparqlSimulator', simulatorFactory);

	//
	// implement simulator factory
	simulatorFactory.$inject=['$http', "config", "stopwatch"]
	function simulatorFactory($http, config, stopwatch) {
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

		function elapsedCallback() {
			console.log("elapsedCallback called");
			return $http.get(instance.getWeatherNext()).then(function(response) {
				if(response.data == false) {
					console.error("WeatherService: NEXT iteration FAILED.");
					return;
				}

				console.log("WeatherService: NEXT iteration SUCCESSFUL.");
				$http.get(instance.getSimulatorEvaluate()).then(function(response) {
					if(response.data == false) {
						console.error("GeoSparqlSimulator: EVALUATE current iteration FAILED.");
						return;
					}

					console.log("GeoSparqlSimulator: EVALUATE current iteration SUCCESSFUL.");
					//TODO: render result

				}, function (error) {
					console.error("simulatorEvaluate => error: " + error.data.responseText);
				});

			}, function (error) {
				console.error("weatherNext => error: " + error.data.responseText);
			});
		}

		return instance;
	}

})(angular);

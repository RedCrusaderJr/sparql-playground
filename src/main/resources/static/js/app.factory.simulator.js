(function (angular, undefined) {
	'use strict';

	/*
	* create simulator service
	*/

	angular.module('simulator.service', [])
		   .factory('simulator', simulatorFactory);

	//
	// implement simulator factory
	simulatorFactory.$inject=["$q", '$http', 'shapeRenderer', "config"]
	function simulatorFactory($q, $http, shapeRenderer, config) {
		class Simulator {
			constructor() {
				//
				// wrap promise to this object
				this.$promise = $q.when(this);
				//
				// manage cancel
				this.canceler = $q.defer();

				//
				//URLs
				this.startSimulation = config.sparql.startSimulation;
				this.pauseSimulation = config.sparql.pauseSimulation;
				this.resetSimulation = config.sparql.resetSimulation;
				this.stopSimulation = config.sparql.stopSimulation;
			}

			start() {
				return $http.post(this.startSimulation)
				.then(function (response) {
					alert(response.data + " successful SIMULATION start");
				}, function (error) {
					alert("error " + error.data.responseText);
				});
			}

			pause() {
				return $http.post(this.pauseSimulation)
				.then(function (response) {
					alert(response.data + " successful SIMULATION pause");
				}, function (error) {
					alert("error " + error.data.responseText);
				});
			}

			reset() {
				return $http.post(this.resetSimulation)
				.then(function (response) {
					alert(response.data + " successful SIMULATION reset");
				}, function (error) {
					alert("error " + error.data.responseText);
				});
			}

			stop() {
				return $http.post(this.stopSimulation)
				.then(function (response) {
					alert(response.data + " successful SIMULATION stop");
				}, function (error) {
					alert("error " + error.data.responseText);
				});
			}
		}
		return new Simulator();
	}

})(angular);

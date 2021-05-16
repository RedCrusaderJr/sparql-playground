(function (angular, undefined) {
	'use strict';

	/*
	* create simulator service
	*/

	angular.module('simulator.service', [])
		   .factory('simulator', simulatorFactory);

	//
	// implement simulator factory
	simulatorFactory.$inject=["$q", 'shapeRenderer']
	function simulatorFactory($q, shapeRenderer) {
		class Simulator {
			constructor() {
				//
				// wrap promise to this object
				this.$promise = $q.when(this);
				//
				// manage cancel
				this.canceler = $q.defer();
			}

			start() {

			}

			pause() {

			}

			reset() {

			}

			stop() {

			}
		}
		return new Simulator();
	}

})(angular);

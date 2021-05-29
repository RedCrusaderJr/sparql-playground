(function (angular, undefined) {
	'use strict';

	/*
	* create stopwatch service
	*/

	angular.module('geosparql.simulator.service')
		   .factory('stopwatch', stopwatchFactory);

	//
	// implement stopwatch factory
	function stopwatchFactory() {
		class Stopwatch {
			constructor() {
				this.counter = 0;
				this.interval = null;
				this.elapseInterval = 0;
				this.elapsedCallback = null;
				this.isStopwatchActive = false;
			}

			//#region GETTERS
			getCounter() {
				return this.counter;
			}

			incrementCounter() {
				this.counter++;
			}

			getIsStopwatchActive() {
				return this.isStopwatchActive;
			}

			getElapseInterval() {
				return this.elapseInterval;
			}
			//#endregion

			start(elapseInterval, elapsedCallback) {
				console.log("Stopwatch: START called.");
				this.isStopwatchActive = true;

				if(!this.interval) {
					this.counter = 0;
					this.elapseInterval = elapseInterval;
					this.elapsedCallback = elapsedCallback;
					this.interval = setInterval(countSecondsCallback, 1000);
				}
			}

			pause() {
				console.log("Stopwatch: PAUSE called.");
				this.isStopwatchActive = false;
			}

			reset() {
				console.log("Stopwatch: RESET called.");
				this.counter = 0;
				this.isStopwatchActive = true;
			}

			stop() {
				console.log("Stopwatch: STOP called.");

				this.counter = 0;
				this.isStopwatchActive = false;
				if(this.interval) {
					clearInterval(this.interval);
					this.interval = null;
				}
			}

			routeChangeStart() {
				this.stop();
				return true;
			}
		}

		var instance = new Stopwatch();

		function countSecondsCallback() {
			if(instance.getIsStopwatchActive() == false) {
				return;
			}

			//
			//increment
			instance.incrementCounter();

			//
			//check if it's time to elapse
			let elapseInterval = instance.getElapseInterval();
			if((instance.getCounter() % elapseInterval) == 0) {
				console.log("Calling 'elapsedCallback' after " + elapseInterval + " seconds");
				instance.elapsedCallback();
			}
		}

		return instance;
	}

})(angular);

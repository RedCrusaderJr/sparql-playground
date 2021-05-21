(function (angular, undefined) {
	'use strict';

	//
	//Define the application global configuration
	angular.module('snorql.config', []).factory('config', [
    	function () {

			var BASE_URL = window.location.origin + "/";
			// global application configuration
			var defaultConfig = {
				apiUrl : BASE_URL,
				home:'/',
				sparql : {
					endpoint: BASE_URL + 'sparql',
					examples: BASE_URL + 'queries',
					faqsURL: BASE_URL + 'faqs',
					prefixes: BASE_URL + 'prefixes',
					dataURL: BASE_URL + 'ttl-data',
					geoExport: BASE_URL + 'save-geo-data',
					geoImport: BASE_URL + 'load-geo-data',

					weatherStartNew: BASE_URL + 'weather-service/start-new',
					weatherNext: BASE_URL + 'weather-service/next',
					weatherReset: BASE_URL + 'weather-service/reset',
					weatherStop: BASE_URL + 'weather-service/stop',

					simulatorStart: BASE_URL + 'simulator/start',
					simulatorEvaluate: BASE_URL + 'simulator/evaluate',
					simulatorStop: BASE_URL + 'simulator/stop',
				}
			}
        	return defaultConfig;
    	}
	]);

})(angular);

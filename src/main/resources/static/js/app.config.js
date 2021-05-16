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
					startSimulation: BASE_URL + 'start-simulation',
					pauseSimulation: BASE_URL + 'pause-simulation',
					resetSimulation: BASE_URL + 'reset-simulation',
					stopSimulation: BASE_URL + 'stop-simulation',
					nextSimulation: BASE_URL + 'next-simulation'
				}
			}
        	return defaultConfig;
    	}
	]);

})(angular);

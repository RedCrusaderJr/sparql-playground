(function (angular, undefined) {'use strict';

	/*
	* create snorql service
	*/

	angular.module('snorql.service', [])
		   .factory('snorql', snorqlFactory)
		   .factory('simulator', simulatorFactory)
		   .factory('shapeRenderer', shapeRendererFactory)
		   .factory('geomapManipulation', geomapManipulationFactory);


	//
	// implement snorql factory
	snorqlFactory.$inject=["$http", "$q", "$timeout", "$location", "config", "shapeRenderer", "geomapManipulation"]
	function snorqlFactory($http, $q, $timeout, $location, config, shapeRenderer, geomapManipulation) {

		var defaultSnorql={
			property:'SELECT DISTINCT ?resource ?value\n' +
						'WHERE { ?resource <URI_COMPONENT> ?value }\n' +
						'ORDER BY ?resource ?value',

			clazz :  'SELECT DISTINCT ?instance\n' +
						'WHERE { ?instance a <URI_COMPONENT> }\n' +
						'ORDER BY ?instance',

			describe:'SELECT DISTINCT ?property ?hasValue ?isValueOf\n' +
						'WHERE {\n' +
						'  { <URI_COMPONENT> ?property ?hasValue }\n' +
						'  UNION\n' +
						'  { ?isValueOf ?property <URI_COMPONENT> }\n' +
						'}\n' +
						'ORDER BY (!BOUND(?hasValue)) ?property ?hasValue ?isValueOf',

			description:   'Here is a an example on how to get the first 10 rows of a dataset. \nClick on the examples on the right to continue your journey about learning SPARQL.',


			title:"Extract some data",
			query:'SELECT DISTINCT * WHERE {\n  ?s ?p ?o\n}\nLIMIT 10\n\n\n# When doing such a query it is important to set LIMIT 10.\n# This limit avoids performance issues, if the size of the dataset is unknown.',

			//
			// set your endpoint here
			sparqlEndpoint:config.sparql.endpoint,
			sparqlUrlExamples:config.sparql.examples,
			sparqlUrlPrefixes:config.sparql.prefixes,
			sparqlUrlData:config.sparql.dataURL
		};


		var defaultSparqlParams={
			'default-graph-uri':null,
			'named-graph-uri':null,
			output:'json',
		};

		var defaultAcceptHeaders={
			html:'application/sparql-results+json,*/*',
			json:'application/sparql-results+json,*/*',
			xml:'application/sparql-results+xml,*/*',
			csv:'application/sparql-results+csv,*/*',
			tsv:'application/sparql-results+tsv,*/*'
		};

		//
		// serialize prefixes
		var query_getPrefixes = function(pfxs) {
			var prefixes = '';
			for (var prefix in pfxs) {
				var uri = pfxs[prefix];
				prefixes = prefixes + 'PREFIX ' + prefix + ': <' + uri + '>\n';
			}
			return prefixes;
		};

		class Snorql {
			constructor() {
				// this service depend on two $resources (eg. dao in Java world)
				// this.$dao={queries:$resource('queries.json'), sparqlQuery:$resource('sparql.json')};
				// queries examples
				this.examples = [];
				// examples tags
				this.tags = [];

				//frequently asked questions
				this.faqs = [];

				this.prefixes = "";

				// ttl data
				this.data = "";

				// initial sparql result
				this.result = { head: [], results: [] };

				// initial sparql query
				this.query = defaultSnorql.query;

				// initial selected query id
				this.selectedQueryId = 0;

				// initial selected query title
				this.queryTitle = defaultSnorql.title;

				// initial url for examples
				this.examplesUrl = defaultSnorql.sparqlUrlExamples;

				// initial url for examples
				this.dataURL = defaultSnorql.sparqlUrlData;

				// initial url for prefixes
				this.prefixesUrl = defaultSnorql.sparqlUrlPrefixes;

				this.description = defaultSnorql.description;

				// wrap promise to this object
				this.$promise = $q.when(this);

				// manage cancel
				this.canceler = $q.defer();
			}
			reset() {
				this.canceler.resolve();
				this.result = { head: [], results: [] };
				this.canceler = $q.defer();
			}
			endpoint() {
				return defaultSnorql.sparqlEndpoint;
			}
			//
			// load sparql examples
			loadExamples() {
				var self = this;
				if (this.examples.length) {
					return this;
				}
				this.$promise = this.$promise.then(function () {
					return $http({ method: 'GET', url: self.examplesUrl });
				});

				this.$promise.then(function (config) {
					var index = 0, rawtags = [];
					self.examples = (config.data);
					self.examples.forEach(function (example) {
						example.index = index++;
						if (!example.tags)
							return;
						//
						// considering multiple tags
						example.tags.forEach(function (tag) {
							if (self.tags.indexOf(tag.trim()) == -1) {
								self.tags.push(tag);
							}
						});
					});
				});

				return this;
			}
			//
			// load sparql examples
			loadPrefixes() {
				var self = this;
				if (this.prefixes != "") {
					return this;
				}
				this.$promise = this.$promise.then(function () {
					return $http({ method: 'GET', url: self.prefixesUrl });
				});

				this.$promise.then(function (response) {
					self.prefixes = (response.data);
				});

				return this;
			}
			//
			//load sparql examples
			loadFaqs() {
				var self = this;
				return $http.get(config.sparql.faqsURL).then(function (response) {
					self.faqs = (response.data);
				});
			}
			//
			// load sparql examples
			loadData() {
				var self = this;
				console.log(this.dataURL);
				return $http.get(this.dataURL).then(function (response) {
					self.data = (response.data);
				});

			}
			pushData() {
				var self = this;
				console.log(this.dataURL);
				return $http({
					url: this.dataURL,
					method: 'PUT',
					params: { data: self.data }
				}).then(function (response) {
					alert(response.data + " triples successfully loaded");
				}, function (error) {
					alert("error " + error.data.responseText);
				});
			}
			//
			// manage default snorql state
			updateQuery(params) {
				if (params.class) {
					this.query = defaultSnorql['class'].replace(/URI_COMPONENT/g, params.class);
				}
				else if (params.property) {
					this.query = defaultSnorql['property'].replace(/URI_COMPONENT/g, params.property);
				}
				else if (params.describe) {
					this.query = defaultSnorql['describe'].replace(/URI_COMPONENT/g, params.describe);
				} else {
					this.query = params.query || defaultSnorql.query;
				}
				return this.query;
			}
			// start a sparql query,
			//  http filter define : query* (default), describe, class, property and output=json* (default)
			executeQuery(sparql, filter) {
				var self = this;
				if (!sparql || sparql === '')
					return self;

				this.reset();
				var params = angular.extend(defaultSparqlParams, filter, { query: sparql });

				// setup prefixes
				params.query = query_getPrefixes(this.prefixes) + '\n' + params.query;
				var accept = { 'Accept': defaultAcceptHeaders[params.output] };
				var url = defaultSnorql.sparqlEndpoint;

				/*if(params.output!=='html'){
					self.reset();
					var deferred = $q.defer();
					window.location =url+ '?'+$.param(params);
					this.$promise=deferred.promise;
					$timeout(function(){
					deferred.resolve(this);
					},200)
					return self;
				}*/
				if (params.output !== 'html') {
					var encodedUrl = url + "?output=" + params.output + "&query=" + encodeURIComponent(params.query);
					console.log(encodedUrl);
					window.open(encodedUrl);

					return self;

				} else {
					//
					// html output is done by parsing json
					params.output = 'json';
					this.$promise = $http({ method: 'GET', url: url, params: params, headers: accept, timeout: this.canceler.promise });
					this.$promise.then(function (config) {
						self.result = (config.data);
						console.log(self.result);

						//drawing on map if geospatial data in query
						window.queryShapes.clearLayers();
						var geoSpatialColumnHeaders = self.result.head.vars.filter(function (item) {
							var finder = 'g_';
							return eval('/' + finder + '/').test(item);
						});

						if (geoSpatialColumnHeaders.length > 0) {
							var elements = self.result.results.bindings;
							geoSpatialColumnHeaders.forEach(column => {
								elements.forEach(element => {
									shapeRenderer.renderElement(element, column);
								});
							});

							geomapManipulation.setView(window.mapViewLat, window.mapViewLong, 13);
						}
					});

				}
				return this;
			}
			//
			// access the singleton
			getPrefixes() {
				return this.prefixes;
			}
			/**
			 * SPARQLResultFormatter: Renders a SPARQL/JSON result set into an HTML table.
			 */
			SPARQLResultFormatter() {
				return new (function (result, namespaces) {
					this._json = result;
					this._variables = this._json.head['vars'] || {};
					this._boolean = this._json.boolean;
					if (this._boolean === undefined) {
						this._results = this._json.results['bindings'] || [];
					} else {
						this._variables = ["boolean"];
					}

					this._namespaces = namespaces;

					this.toDOM = function () {
						var table = document.createElement('table');
						table.className = 'queryresults';
						table.appendChild(this._createTableHeader());
						if (this._boolean === undefined) {
							for (var i = 0; i < this._results.length; i++) {
								table.appendChild(this._createTableRow(this._results[i], i));
							}
						} else { //ASK query
							table.appendChild(this._createTableBooleanRow(this._boolean));
						}
						return table;
					};

					// TODO: Refactor; non-standard link makers should be passed into the class by the caller
					this._getLinkMaker = function (varName) {
						//console.log(varName);
						if (varName == 'property') {
							return function (uri) { return '?property=' + encodeURIComponent(uri); };
						} else if (varName == 'class') {
							return function (uri) { return '?class=' + encodeURIComponent(uri); };
						} else {
							return function (uri) { return '?describe=' + encodeURIComponent(uri); };
						}
					};

					this._createTableHeader = function () {
						var tr = document.createElement('tr');
						var hasNamedGraph = false;
						for (var i = 0; i < this._variables.length; i++) {
							var th = document.createElement('th');
							th.appendChild(document.createTextNode(this._variables[i]));
							tr.appendChild(th);
							if (this._variables[i] == 'namedgraph') {
								hasNamedGraph = true;
							}
						}
						if (hasNamedGraph) {
							var th = document.createElement('th');
							th.appendChild(document.createTextNode(' '));
							tr.insertBefore(th, tr.firstChild);
						}
						return tr;
					};

					this._createTableBooleanRow = function (boolean) {
						var tr = document.createElement('tr');
						tr.className = 'odd';
						var namedGraph = null;
						var td = document.createElement('td');
						td.appendChild(this._formatNode({ value: boolean, type: "literal" }, "boolean"));
						tr.appendChild(td);
						return tr;
					};

					this._createTableRow = function (binding, rowNumber) {
						var tr = document.createElement('tr');
						if (rowNumber % 2) {
							tr.className = 'odd';
						} else {
							tr.className = 'even';
						}
						var namedGraph = null;
						for (var i = 0; i < this._variables.length; i++) {
							var varName = this._variables[i];
							td = document.createElement('td');
							td.appendChild(this._formatNode(binding[varName], varName));
							tr.appendChild(td);
							if (this._variables[i] == 'namedgraph') {
								namedGraph = binding[varName];
							}
						}
						if (namedGraph) {
							var link = document.createElement('a');
							link.href = 'javascript:snorql.switchToGraph(\'' + namedGraph.value + '\')';
							link.appendChild(document.createTextNode('Switch'));
							var td = document.createElement('td');
							td.appendChild(link);
							tr.insertBefore(td, tr.firstChild);
						}
						return tr;
					};

					this._formatNode = function (node, varName) {
						if (!node) {
							return this._formatUnbound(node, varName);
						}
						if (node.type == 'uri') {
							return this._formatURI(node, varName);
						}
						if (node.type == 'bnode') {
							return this._formatBlankNode(node, varName);
						}
						if (node.type == 'literal') {
							return this._formatPlainLiteral(node, varName);
						}
						if (node.type == 'typed-literal') {
							return this._formatTypedLiteral(node, varName);
						}
						return document.createTextNode('???');
					};

					this._formatURI = function (node, varName) {
						var span = document.createElement('span');
						span.className = 'uri';
						var a = document.createElement('a');
						a.href = this._getLinkMaker(varName)(node.value);
						a.title = '<' + node.value + '>';
						a.className = 'graph-link';
						var qname = this._toQName(node.value);
						if (qname) {
							a.appendChild(document.createTextNode(qname));
							span.appendChild(a);
							if ((qname.indexOf('entry') == 0) || (qname.indexOf('iso') == 0)) {

								var spacer = document.createTextNode(' --- ');
								span.appendChild(spacer);
								var a2 = document.createElement('a');
								a2.href = node.value.replace("rdf", "db").replace("isoform", "entry");
								a2.title = '< View in neXtProt >';
								a2.className = 'url';
								a2.target = '_blank'; // Opens in new tab
								a2.appendChild(document.createTextNode(' (neXtProt link) '));
								span.appendChild(a2);
							}
						} else {
							// embed image object
							match = node.value.match(/\.(png|gif|jpg)(\?.+)?$/);
							if (match) {
								img = document.createElement('img');
								img.src = node.value;
								img.title = node.value;
								img.className = 'media';

								a.appendChild(img);
								span.appendChild(a);
							} else {
								a.appendChild(document.createTextNode(node.value));
								span.appendChild(document.createTextNode('<'));
								span.appendChild(a);
								span.appendChild(document.createTextNode('>'));

							}

						}
						var match = node.value.match(/^(https?|ftp|mailto|irc|gopher|news):/);
						if (match) {
							span.appendChild(document.createTextNode(' '));
							var externalLink = document.createElement('a');
							externalLink.href = node.value;
							span.appendChild(externalLink);
						}



						return span;
					};

					this._formatPlainLiteral = function (node, varName) {
						var text = '"' + node.value + '"';
						if (node['xml:lang']) {
							text += '@' + node['xml:lang'];
						}
						return document.createTextNode(text);
					};

					this._formatTypedLiteral = function (node, varName) {
						var text = '"' + node.value + '"';
						if (node.datatype) {
							text += '^^' + this._toQNameOrURI(node.datatype);
						}
						if (this._isNumericXSDType(node.datatype)) {
							var span = document.createElement('span');
							span.title = text;
							span.appendChild(document.createTextNode(node.value));
							return span;
						}
						return document.createTextNode(text);
					};

					this._formatBlankNode = function (node, varName) {
						return document.createTextNode('_:' + node.value);
					};

					this._formatUnbound = function (node, varName) {
						var span = document.createElement('span');
						span.className = 'unbound';
						span.title = 'Unbound';
						span.appendChild(document.createTextNode('-'));
						return span;
					};

					this._toQName = function (uri) {
						for (var prefix in this._namespaces) {
							var nsURI = this._namespaces[prefix];
							if (uri.indexOf(nsURI) == 0) {
								return prefix + ':' + uri.substring(nsURI.length);
							}
						}
						return null;
					};

					this._toQNameOrURI = function (uri) {
						var qName = this._toQName(uri);
						return (qName == null) ? '<' + uri + '>' : qName;
					};

					this._isNumericXSDType = function (datatypeURI) {
						for (var i = 0; i < this._numericXSDTypes.length; i++) {
							if (datatypeURI == this._xsdNamespace + this._numericXSDTypes[i]) {
								return true;
							}
						}
						return false;
					};
					this._xsdNamespace = 'http://www.w3.org/2001/XMLSchema#';
					this._numericXSDTypes = ['long', 'decimal', 'float', 'double', 'int',
						'short', 'byte', 'integer', 'nonPositiveInteger', 'negativeInteger',
						'nonNegativeInteger', 'positiveInteger', 'unsignedLong',
						'unsignedInt', 'unsignedShort', 'unsignedByte'];
				})(this.result, this.getPrefixes());
			}
		}

		return new Snorql()
	};

	//
	// implement simulator factory
	simulatorFactory.$inject=["$q", 'shapeRenderer']
	function simulatorFactory($q, shapeRenderer) {
		class Simulator {
			constructor() {
				// wrap promise to this object
				this.$promise = $q.when(this);

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

	//
	// implement shapeRenderer factory
	shapeRendererFactory.$inject=["$q"]
	function shapeRendererFactory($q) {
		class ShapeRenderer {
			constructor() {
				// wrap promise to this object
				this.$promise = $q.when(this);

				// manage cancel
				this.canceler = $q.defer();
			}

			renderElement(element, column) {
				let parsedElement = parseElement(element, column);
				addElementToMap(parsedElement);
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

		function addElementToMap(parsedElement) {
			switch(parsedElement.Name) {
				case "POINT":
				drawPoint(parsedElement);
				break;
				case "LINESTRING":
				drawLine(parsedElement);
				break;
				case "POLYGON":
				drawPolygon(parsedElement);
				break;
				default:
				break;
			}
		};

		function drawPoint(parsedElement) {
			parsedElement.Coordinates.Shapes.forEach(point => {
				let x = point[0].x;
				let y = point[0].y;
				new L.marker([x, y]).addTo(window.queryShapes);
				window.mapViewLat = x;
				window.mapViewLong = y;
			});
		};

		function drawLine(parsedElement) {
			parsedElement.Coordinates.Shapes.forEach(line => {
				let latlongs = [];
				line.forEach(point => {
					let x = point.x;
					let y = point.y;
					latlongs.push([x,y]);
					window.mapViewLat = x;
					window.mapViewLong = y;
				});
				new L.polyline(latlongs, {color: 'red'}).addTo(window.queryShapes);
			});
		};

		function drawPolygon(parsedElement) {
			parsedElement.Coordinates.Shapes.forEach(polygon => {
				let latlongs = [];
				polygon.forEach(point => {
					let x = point.x;
					let y = point.y;
					latlongs.push([x,y]);
					window.mapViewLat = x;
					window.mapViewLong = y;
				});
				new L.polygon(latlongs, {color: 'blue'}).addTo(window.queryShapes);
			});
		};

		return new ShapeRenderer();
	};

	//
	// implement geomapManipulation factory
	geomapManipulationFactory.$inject=["$q"]
	function geomapManipulationFactory($q) {
		class GeomapManipulation {
			constructor() {
				this.geomap = []

				// wrap promise to this object
				this.$promise = $q.when(this);

				// manage cancel
				this.canceler = $q.defer();
			}

			initMap() {
				this.geomap = L.map('geomapDiv').setView([51.505, -0.09], 13);
				L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw', {
					maxZoom: 18,
					attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, ' +
					'Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
					id: 'mapbox/streets-v11',
					tileSize: 512,
					zoomOffset: -1
				}).addTo(this.geomap);
				//
				// map markers
				window.queryShapes = L.layerGroup().addTo(this.geomap);
				window.mapViewLat = 45;
				window.mapViewLong = 19;

				return this.geomap;
			}

			setView(latitude, longitude, zoom) {
				this.geomap.setView([latitude, longitude], zoom);
			}
		}

		return new GeomapManipulation();
	}

})(angular);

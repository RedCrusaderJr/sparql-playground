package swiss.sib.sparql.playground.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.domain.SparqlQuery;
import swiss.sib.sparql.playground.repository.QueryDictionary;
import swiss.sib.sparql.playground.service.SparqlService;

@RestController
public class GeoSparqlSimulatorController implements InitializingBean {
	private static final Log logger = LogFactory.getLog(SparqlController.class);
	private static String SIMULATOR_FOLDER_NAME = "simulator";
	private static String DEFAULT_FOLDER_NAME = "default";
	private static String ASK_FOLDER_NAME = "ask";
	private static String ALTERNATIVE_FOLDER_NAME = "alternative";
	// private static String MARKLOGIC_FOLDER_NAME = "marklogic";

	@Autowired
	private QueryDictionary queryDictionary;
	@Autowired
	private SparqlService sparqlService;
	private Map<String, Map<String, SparqlQuery>> queryMap;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.queryMap = new HashMap<String, Map<String, SparqlQuery>>();
		initQueryMap();
	}

	@RequestMapping(value = "simulator/start", method = RequestMethod.GET)
	public @ResponseBody Boolean start() {
		// this.queryMap.clear();
		// initQueryMap();
		return true;
	}

	@RequestMapping(value = "simulator/evaluate", method = RequestMethod.GET)
	public @ResponseBody String evaluate() throws NameNotFoundException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (this.queryMap.containsKey(DEFAULT_FOLDER_NAME) == false
				|| this.queryMap.containsKey(ASK_FOLDER_NAME) == false
				|| this.queryMap.containsKey(ALTERNATIVE_FOLDER_NAME) == false) {
			throw new NameNotFoundException(
					"Folder structure of '" + SIMULATOR_FOLDER_NAME + "' not valid. It should contain these folders: '"
							+ DEFAULT_FOLDER_NAME + "', '" + ASK_FOLDER_NAME + "', '" + ALTERNATIVE_FOLDER_NAME + "'");
		}

		long start = System.currentTimeMillis();
		for (SparqlQuery defaultSelectQuery : this.queryMap.get(DEFAULT_FOLDER_NAME).values()) {
			String queryTitle = defaultSelectQuery.getTitle().trim();

			if (this.queryMap.get(ASK_FOLDER_NAME).containsKey(queryTitle)) {
				SparqlQuery askQuery = this.queryMap.get(ASK_FOLDER_NAME).get(queryTitle);

				if (evaluateAskQuery(askQuery) == false) {
					// ASK query evaluated FALSE
					if (this.queryMap.get(ALTERNATIVE_FOLDER_NAME).containsKey(queryTitle) == false) {
						// ALTERNATIVE query not present -> skip evaluation
						continue;
					}
					SparqlQuery alternativeSelectQuery = this.queryMap.get(ALTERNATIVE_FOLDER_NAME).get(queryTitle);
					TupleQueryResult result = evaluateSelectQuery(alternativeSelectQuery);
					putToResultMap(resultMap, queryTitle, result);
					continue;
				}
			}
			// ASK query does not exist or evaluated TRUE
			TupleQueryResult result = evaluateSelectQuery(defaultSelectQuery);
			putToResultMap(resultMap, queryTitle, result);
		}
		logger.info("GeoSparql Simulator Evaluation finished in " + (System.currentTimeMillis() - start) + " ms");
		return new JSONObject(resultMap).toString();
	}

	@RequestMapping(value = "simulator/stop", method = RequestMethod.GET)
	public @ResponseBody Boolean stop() {
		// todo: clean up
		return true;
	}

	private void initQueryMap() {
		File simulatorFolder = new File(Application.getFolder() + "/" + SIMULATOR_FOLDER_NAME);
		if (simulatorFolder.exists() == false) {
			logger.error("Simulator folder not found. Path: " + simulatorFolder.getPath());
			return;
		}

		for (File subFolder : simulatorFolder.listFiles()) {
			Map<String, SparqlQuery> queries = new HashMap<String, SparqlQuery>();

			for (SparqlQuery query : this.queryDictionary.getQueries(subFolder.getPath())) {
				queries.put(query.getTitle().trim(), query);
			}
			this.queryMap.put(subFolder.getName().trim(), queries);
		}
	}

	private Boolean evaluateAskQuery(SparqlQuery askQuery) {
		long sparqlEvalStart = System.currentTimeMillis();
		Boolean result = (Boolean) this.sparqlService.evaluateQuery(askQuery.getSparql());

		long sparqlEvalEnd = System.currentTimeMillis() - sparqlEvalStart;
		String title = askQuery.getTitle().trim();
		logger.info("Evaluating ASK query '" + title + "' finished in " + sparqlEvalEnd + " ms");

		return result;
	}

	private TupleQueryResult evaluateSelectQuery(SparqlQuery selectQuery) {
		long sparqlEvalStart = System.currentTimeMillis();

		TupleQueryResult result = (TupleQueryResult) this.sparqlService.evaluateQuery(selectQuery.getSparql());

		long sparqlEvalEnd = System.currentTimeMillis() - sparqlEvalStart;
		String title = selectQuery.getTitle().trim();
		logger.info("Evaluating SELECT query '" + title + "' finished in " + sparqlEvalEnd + " ms");

		return result;
	}

	private void putToResultMap(Map<String, Object> resultMap, String queryTitle, TupleQueryResult result) {
		if (resultMap.containsKey(queryTitle) == true) {
			logger.warn("Entry with query title '" + queryTitle + "' already present in ResultMap");
			return;
		}

		List<JSONObject> bindingSets = new ArrayList<JSONObject>();
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();

			Map<String, Value> keyValuePairs = new HashMap<String, Value>();
			bindingSet.forEach(binding -> {
				keyValuePairs.put(binding.getName(), binding.getValue());
			});

			JSONObject json = new JSONObject(keyValuePairs);
			bindingSets.add(json);
		}

		Map<String, Object> queryResult = new HashMap<String, Object>();
		queryResult.put("names", result.getBindingNames());
		queryResult.put("bindings", bindingSets);

		resultMap.put(queryTitle, queryResult);
	}
}

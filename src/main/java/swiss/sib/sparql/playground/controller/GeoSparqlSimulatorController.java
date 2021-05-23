package swiss.sib.sparql.playground.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryInterruptedException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterFactory;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriterFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.domain.SparqlQuery;
import swiss.sib.sparql.playground.repository.QueryDictionary;
import swiss.sib.sparql.playground.service.SparqlService;

@RestController
public class GeoSparqlSimulatorController {
	private static final Log logger = LogFactory.getLog(SparqlController.class);

	@Autowired
	QueryDictionary queryDictionary;

	@Autowired
	private SparqlService sparqlService;

	@RequestMapping(value = "simulator/start", method = RequestMethod.GET)
	public @ResponseBody Boolean start() {
		// todo: setup
		return true;
	}

	@RequestMapping(value = "simulator/evaluate", method = RequestMethod.GET)
	public @ResponseBody String evaluate() throws IOException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		long start = System.currentTimeMillis();

		List<SparqlQuery> queries = queryDictionary.getQueries(Application.getFolder() + "/simulator");
		for (SparqlQuery query : queries) {
			String title = query.getTitle().trim();

			long sparqlEvalStart = System.currentTimeMillis();
			TupleQueryResult result = (TupleQueryResult) sparqlService.evaluateQuery(query.getSparql());
			logger.info("Evaluating '" + title + "' query finished in " + (System.currentTimeMillis() - sparqlEvalStart)
					+ " ms");

			if (resultMap.containsKey(title) == true) {
				continue;
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
			resultMap.put(title, queryResult);

			queryResult.put("names", result.getBindingNames());
			queryResult.put("bindings", bindingSets);
		}

		logger.info("GeoSparql Simulator Evaluation finished in " + (System.currentTimeMillis() - start) + " ms");
		return new JSONObject(resultMap).toString();
	}

	@RequestMapping(value = "simulator/stop", method = RequestMethod.GET)
	public @ResponseBody Boolean stop() {
		// todo: clean up
		return true;
	}
}

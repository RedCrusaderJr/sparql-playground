package swiss.sib.sparql.playground.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.query.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.controller.SparqlController;
import swiss.sib.sparql.playground.domain.SparqlQueryType;
import swiss.sib.sparql.playground.exception.SparqlTutorialException;
import swiss.sib.sparql.playground.geosparql.GeoSparqlEvaluator;
import swiss.sib.sparql.playground.repository.RDF4jRepository;
import swiss.sib.sparql.playground.repository.impl.RepositoryType;
import swiss.sib.sparql.playground.utils.IOUtils;

/**
 * A SPARQL service that adds prefixes to repository
 *
 * @author Daniel Teixeira http://github.com/ddtxra
 *
 */
@Service
public class SparqlService implements InitializingBean {
	private static final Log logger = LogFactory.getLog(SparqlController.class);

	@Autowired
	private RDF4jRepository repository;
	@Autowired
	private GeoSparqlEvaluator geoSparqlEvaluator;

	private Map<String, String> prefixes = null;
	private String prefixesString;

	public Query getQuery(String queryStr) throws SparqlTutorialException {
		return repository.prepareQuery(queryStr);
	}

	public Object evaluateQuery(String queryStr) {
		try {
			Query query = repository.prepareQuery(queryStr);
			switch (SparqlQueryType.getQueryType(query)) {
				case TUPLE_QUERY:
					return ((TupleQuery) query).evaluate();

				case GRAPH_QUERY:
					return ((GraphQuery) query).evaluate();

				case BOOLEAN_QUERY:
					return ((BooleanQuery) query).evaluate();

				default:
					throw new SparqlTutorialException("Unsupported query type: " + query.getClass().getName());
			}
		}
		catch (QueryInterruptedException e) {
			logger.info("Query interrupted", e);
			throw new SparqlTutorialException("Query evaluation took too long");
		}
		catch (QueryEvaluationException e) {
			if (Application.getRepositoryType() == RepositoryType.MARK_LOGIC && e.getMessage().contains("Server Message: XDMP-UNDFUN")) {
				return evaluateOnMarklogicSemantics(queryStr);

			} else {
				logger.info("Query evaluation error", e);
				throw new SparqlTutorialException("Query evaluation error: " + e.getMessage());
			}
		} 
	}

	private Object evaluateOnMarklogicSemantics(String queryStr) {
		try {
			return geoSparqlEvaluator.evaluateQuery(queryStr);

		} catch (Exception e) {
			throw new SparqlTutorialException(e);
		}
	}

	public Map<String, String> getPrefixes() {
		return prefixes;
	}

	public String getPrefixesString() {
		return prefixesString;
	}

	public void setPrefixesString(String prefixesString) {
		this.prefixesString = prefixesString;
	}

	public void setPrefixes(Map<String, String> prefixes) {
		this.prefixes = prefixes;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.prefixesString = IOUtils.readFile(Application.getFolder() + "/prefixes.ttl", "");

		String prefixes[] = this.prefixesString.split("\n");
		Map<String, String> prefixMap = new TreeMap<String, String>();

		for (String prefix : prefixes) {
			String[] prefixParts = prefix.split(" ");
			String prefixId = prefixParts[1].replaceAll(":", "").trim();
			String prefixUri = prefixParts[2].replaceAll("<", "").replaceAll(">", "").trim();

			prefixMap.put(prefixId, prefixUri);
		}

		this.setPrefixes(prefixMap);
	}

	public void writeData(OutputStream out) {
		if (isDataLoadAllowed()) {
			repository.writeTriplesAsTurtle(out, prefixes);

		} else {

			try {
				out.write(
						"Loading data is not supported for native store (only available for memory store)".getBytes());

			} catch (IOException e) {
				e.printStackTrace();
				throw new SparqlTutorialException(e);
			}
		}
	}

	public long loadData(String data) {
		if (isDataLoadAllowed()) {
			repository.testLoadTurtleData(data); // check if data is ok first (returns exception if not)
			repository.removeAllStatements();
			repository.loadTurtleData(data);
			return repository.countTriplets();

		} else {
			throw new SparqlTutorialException("Loading data is not supported for native store");
		}
	}

	public boolean isDataLoadAllowed() {
		return repository.isDataLoadAllowed();
	}

	public long countNumberOfTriples() {
		return repository.countTriplets();
	}
}

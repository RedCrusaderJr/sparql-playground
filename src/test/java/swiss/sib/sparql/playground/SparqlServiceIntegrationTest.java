package swiss.sib.sparql.playground;

import swiss.sib.sparql.playground.service.SparqlService;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebAppConfiguration
public class SparqlServiceIntegrationTest {

	@Autowired
	private SparqlService sparqlService;

	@Test
	public void testAskQuery() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("ASK {").append(System.lineSeparator());
		sb.append("  FILTER (2>1)").append(System.lineSeparator());
		sb.append("}");
		String query = sb.toString();

		Boolean result = (Boolean) sparqlService.evaluateQuery(query);
		Assert.assertTrue(result);
	}

	@Test
	public void testQueryWithURI() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ?x").append(System.lineSeparator());
		sb.append("WHERE {").append(System.lineSeparator());
		sb.append("  ?x rdf:type <http://example.org/tuto/ontology#Cat>.").append(System.lineSeparator());
		sb.append("}");
		String query = sb.toString();

		TupleQueryResult result = (TupleQueryResult) sparqlService.evaluateQuery(query);
		Assert.assertEquals(2, countResults(result));
	}

	@Test
	public void testQueryWithNamespaces() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(sparqlService.getPrefixesString()).append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("SELECT ?x").append(System.lineSeparator());
		sb.append("WHERE {").append(System.lineSeparator());
		sb.append("  ?x rdf:type tto:Cat.").append(System.lineSeparator());
		sb.append("}");
		String query = sb.toString();

		TupleQueryResult result = (TupleQueryResult) sparqlService.evaluateQuery(query);
		Assert.assertEquals(2, countResults(result));
	}

	// This query stopped working from 2.8.7 upgrade
	// See related issue:
	// https://groups.google.com/forum/#!topic/sesame-users/NpidJt61cCQ

	@Test
	@Ignore
	public void testFederatedQueryWithEBI() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>").append(System.lineSeparator());
		sb.append("PREFIX msi: <http://rdf.ebi.ac.uk/resource/biosamples/msi/>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("SELECT *").append(System.lineSeparator());
		sb.append("WHERE {").append(System.lineSeparator());
		sb.append("  SERVICE <http://www.ebi.ac.uk/rdf/services/biosamples/servlet/query> {");
		sb.append(System.lineSeparator());
		sb.append("    msi:GAE-GEOD-25609 rdf:type ?obj.").append(System.lineSeparator());
		sb.append("  }").append(System.lineSeparator());
		sb.append("}");
		String federatedQuery = sb.toString();

		TupleQueryResult result = sparqlService.executeSelectQuery(federatedQuery);
		Assert.assertEquals(2, countResults(result));
	}

	@Test
	public void testFederatedQueryWithDBPedia() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("PREFIX dbp: <http://dbpedia.org/property/>").append(System.lineSeparator());
		sb.append("PREFIX tto:<http://example.org/tuto/ontology#>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("SELECT *").append(System.lineSeparator());
		sb.append("WHERE {").append(System.lineSeparator());
		sb.append("  SERVICE <http://dbpedia.org/sparql> {").append(System.lineSeparator());
		sb.append("    SELECT ?person ?birthDate ?occupation ?pet").append(System.lineSeparator());
		sb.append("    WHERE {").append(System.lineSeparator());
		sb.append("      VALUES ?birthDate { \"1942-07-13\"^^xsd:date }").append(System.lineSeparator());
		sb.append("      ?person dbp:birthDate ?birthDate.").append(System.lineSeparator());
		sb.append("      ?person dbp:occupation ?occupation.").append(System.lineSeparator());
		sb.append("    }").append(System.lineSeparator());
		sb.append("  }").append(System.lineSeparator());
		sb.append("  OPTIONAL { ?person tto:pet ?pet. }").append(System.lineSeparator());
		sb.append("}");
		String federatedQuery = sb.toString();

		TupleQueryResult result = sparqlService.executeSelectQuery(federatedQuery);
		Assert.assertTrue(countResults(result) > 3);
	}

	@Test
	@Ignore
	public void testFederatedQuery() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(sparqlService.getPrefixesString()).append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("SELECT ?subj ?pred ?obj").append(System.lineSeparator());
		sb.append("WHERE {").append(System.lineSeparator());
		sb.append("  VALUES (?subj ?pred) {").append(System.lineSeparator());
		sb.append("    (dbpedia:Harrison_Ford dbo:birthDate)").append(System.lineSeparator());
		sb.append("    (dbpedia:Harrison_Ford dbp:name)").append(System.lineSeparator());
		sb.append("    (dbpedia:Harrison_Ford dbp:occupation)").append(System.lineSeparator());
		sb.append("  }").append(System.lineSeparator());
		sb.append("  { ?subj ?pred ?obj. }").append(System.lineSeparator());
		sb.append("  UNION {").append(System.lineSeparator());
		sb.append("    SERVICE <http://dbpedia.org/sparql> {").append(System.lineSeparator());
		sb.append("       ?subj ?pred ?obj.").append(System.lineSeparator());
		sb.append("    }").append(System.lineSeparator());
		sb.append("  }").append(System.lineSeparator());
		sb.append("}");
		String federatedQuery = sb.toString();

		TupleQueryResult result = (TupleQueryResult) sparqlService.evaluateQuery(federatedQuery);
		Assert.assertEquals(2, countResults(result));
	}

	@Test
	public void testCountNumberOfTriples() throws Exception {
		Long n = sparqlService.countNumberOfTriples();
		System.out.println(n + " triples");
		Assert.assertTrue(n > 50);
		Assert.assertTrue(n < 100);
	}

	private long countResults(TupleQueryResult results) {
		try {
			long counter = 0;
			while (results.hasNext()) {
				results.next();
				counter++;
			}
			return counter;
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
			return 0;
		}
	}
}

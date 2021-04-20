package swiss.sib.sparql.playground.geosparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Test;

public class MarklogicSupportTest {

	@Test
	public void evaluateQueryTest() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("select *").append(System.lineSeparator());
		sb.append("where {").append(System.lineSeparator());
		sb.append("  ?s ?p ?o.").append(System.lineSeparator());
		sb.append("bind(?s as ?b).").append(System.lineSeparator());
		sb.append("}").append(System.lineSeparator());
		sb.append("limit 10");

		MarklogicSupport ms = new MarklogicSupport();
		TupleQueryResult result = ms.evaluateQuery(sb.toString());

		int count = 0;
		while (result.hasNext()) {
			count++;
			result.next();
		}
		assertEquals(10, count);
	}

	@Test
	public void parseResponseTest() {
		StringBuilder sb = new StringBuilder();
		sb.append("Content-Type: application/jsonX-Primitive: ");
		sb.append("map{");
		sb.append("\"s\":\"http://iec.ch/TC57/CIM-generic#PerLengthSequenceImpedance.g0ch\",");
		sb.append("\"p\":\"http://www.w3.org/2000/01/rdf-schema#label\",");
		sb.append("\"o\":\"g0ch\"");
		sb.append("}--");

		Pattern bindingsPattern = Pattern.compile("map\\{(?<bindings>.*)\\}");
		Pattern nameValuePattern = Pattern.compile("\"(?<bindingName>.*)\":\"(?<value>.*)\"");

		Matcher bindingsMatcher = bindingsPattern.matcher(sb.toString());
		assertTrue(bindingsMatcher.find());

		String[] nameValuePairs = bindingsMatcher.group("bindings").split(",");

		for (String nameValuePair : nameValuePairs) {
			Matcher nameValueMatcher = nameValuePattern.matcher(nameValuePair);
			assertTrue(nameValueMatcher.find());
		}
	}

	@Test
	public void evaluateGeospatialInMemoryRepoTest() {

	}

	@Test
	public void evaluateGeospatialMarklogicRepoTest() {

	}

	@Test
	public void evaluateGeospatialMarklogicConsoleTest() {
		// MANUAL TEST
	}

	@Test
	public void evaluateJsGeospatialMarklogicRestApiTest() {

	}

	@Test
	public void evaluateJsGeospatialMarklogicConsoleTest() {
		// MANUAL TEST
	}

	@Test
	public void evaluateJsGeospatialMarklogic3rdPartyTest() {
		// MANUAL TEST
		// exapmle: postman
	}
}

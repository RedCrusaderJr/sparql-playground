package swiss.sib.sparql.playground.geosparql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;

public class RenderTest {
	private static String lineSeparator = System.lineSeparator();

	@Test
	public void renderBindTest2() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("select ?b").append(lineSeparator);
		sb.append("where {").append(lineSeparator);
		sb.append("  ?s ?p ?o.").append(lineSeparator);
		sb.append("  bind(?s as ?b).").append(lineSeparator);
		sb.append("}");
		String query = sb.toString();

		sb.delete(0, sb.length());
		sb.append("select ?b").append(lineSeparator);
		sb.append("where {").append(lineSeparator);
		sb.append("  ?s ?p ?o.").append(lineSeparator);
		sb.append("  bind(?s as ?b).").append(lineSeparator);
		sb.append("}");
		String expected = sb.toString();

		executeRenderTest(query, expected);
	}

	private void executeRenderTest(String query, String expected) throws Exception {
		ParsedQuery pq = new SPARQLParser().parseQuery(query, null);
		String actual = new SPARQLQueryRenderer().render(pq);

		Assertions.assertEquals(expected, actual);
	}
}

package swiss.sib.sparql.playground.geosparql;

import java.util.Map;
import org.eclipse.rdf4j.query.parser.sparql.AbstractASTVisitor;

public class GeosparqlParserVisitor extends AbstractASTVisitor {

	Map<String, String> prefixMap;

	public GeosparqlParserVisitor(Map<String, String> prefixMap) {
		this.prefixMap = prefixMap;
	}
}

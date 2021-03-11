package swiss.sib.sparql.playground.repository;

import java.io.OutputStream;
import java.util.Map;

import org.eclipse.rdf4j.query.Query;

/**
*	TODO: for discussion - if we were to merge our code with the original project,
* 	maybe we should leave some parts of it - e.g. SesameRepository interface and such,
*	where we would further extend those classes and interfaces...
*/

/**
 * Interface for Sesame Repository
 *
 * @author Daniel Teixeira http://github.com/ddtxra
 *
 */
public interface RDF4jRepository {

	void testLoadTurtleData(String data);

	void loadTurtleData(String data);

	boolean isDataLoadAllowed();

	void clearData();

	void writeTriplesAsTurtle(OutputStream out, Map<String, String> prefixes);

	Query prepareQuery(String sparqlQuery);

	long countTriplets();
}

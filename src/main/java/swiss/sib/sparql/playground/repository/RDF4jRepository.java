package swiss.sib.sparql.playground.repository;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.repository.RepositoryResult;

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

	void addStatements(List<Statement> statements);

	void removeStatements(List<Statement> statements);

	RepositoryResult<Statement> getStatements(Resource subj, IRI pred, Value obj);

	boolean hasStatement(Statement st);
}

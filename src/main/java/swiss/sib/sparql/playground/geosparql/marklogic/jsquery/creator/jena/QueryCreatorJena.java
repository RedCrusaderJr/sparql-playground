package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.creator.jena;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpWalker;

import swiss.sib.sparql.playground.geosparql.FunctionMapper;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.JavaScriptQueryCreator;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.transformator.jena.CustomFunctionVisitor;

public class QueryCreatorJena extends JavaScriptQueryCreator {

    private CustomFunctionVisitor customFunctionVisitor;

    public QueryCreatorJena() {
        this.customFunctionVisitor = new CustomFunctionVisitor(FunctionMapper.getInstance());
    }

	@Override
    protected String transformSparqlQuery(String sparqlQueryStr) {
		try {
            Op opRoot = Algebra.compile(QueryFactory.create(sparqlQueryStr)); // Get the algebra for the query 
            OpWalker.walk(opRoot, customFunctionVisitor);
            
            return OpAsQuery.asQuery(opRoot).serialize();
        }
        catch (Exception e) {
            logger.warn("[QueryCreatorJena] Transformation was unsuccessful.", e);
            return sparqlQueryStr;
        }
	}
}

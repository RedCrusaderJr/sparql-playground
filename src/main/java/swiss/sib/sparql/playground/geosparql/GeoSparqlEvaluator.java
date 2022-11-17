package swiss.sib.sparql.playground.geosparql;

public interface GeoSparqlEvaluator {

    public Object evaluateQuery(String sparqlQuery) throws Exception;

    public Object evaluateQuery(String sparqlQuery, Boolean reurnRaw) throws Exception;
}

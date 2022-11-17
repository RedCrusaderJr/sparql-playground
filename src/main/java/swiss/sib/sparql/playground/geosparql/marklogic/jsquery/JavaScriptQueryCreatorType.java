package swiss.sib.sparql.playground.geosparql.marklogic.jsquery;

public enum JavaScriptQueryCreatorType {

    JENA_QUERY_CREATOR, RDF4J_QUERY_CREATOR;

    public static JavaScriptQueryCreatorType parseType(String evaluatorTypeStr) {

        if (evaluatorTypeStr.equals("jena")) {
            return JavaScriptQueryCreatorType.JENA_QUERY_CREATOR;

        } else if (evaluatorTypeStr.equals("rdf4j")) {
            return JavaScriptQueryCreatorType.RDF4J_QUERY_CREATOR;

        } else {
            return JavaScriptQueryCreatorType.JENA_QUERY_CREATOR;
        }
    }
}

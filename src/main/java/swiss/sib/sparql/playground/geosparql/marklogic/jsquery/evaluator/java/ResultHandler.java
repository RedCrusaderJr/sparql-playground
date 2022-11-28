package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.java;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.eclipse.rdf4j.query.impl.TupleQueryResultBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import com.marklogic.client.eval.EvalResultIterator;

public class ResultHandler {
    private static final Log logger = LogFactory.getLog(JavaClientEvaluator.class);
 
    Object result;
    String resultType;
    Boolean isFirstIteration;
    String currentIterationStr;
    List<String> bindingNames;
    Boolean bindingNamesAreInitialized;
    
    JSONObject jsonObj;
    String[] jsonNames;
    List<Value> jsonValues;
    
    TupleQueryResultBuilder builder;
    ValidatingValueFactory valueFactory;

    public ResultHandler() {
        valueFactory = new ValidatingValueFactory();
        builder = new TupleQueryResultBuilder();
    }
    
	//TODO: ponovo razmotriti da li je ovo prepakivanje neophodno - Moze li se do fronta
	// proslediti json... tj premeriti pazljivo koliko sta traje
	public Object handleEvalResult(EvalResultIterator iterator) throws Exception {
        result = null;
        resultType = "TUPLE_QUERY";
        isFirstIteration = true;
        bindingNamesAreInitialized = false;
        bindingNames = new ArrayList<String>();
        
        iterator.forEach(currentIteration -> {
			currentIterationStr = currentIteration.getAs(String.class);

			//TRY PARSE AS ASK QUERY
            if(isFirstIteration) {
				isFirstIteration = false;
                
                resultType = "ASK_QUERY";
				if("true".equals(currentIterationStr)) {
					result = true; return;
                }
				if("false".equals(currentIterationStr)) {
					result = false;	return;
				}

                resultType = "TUPLE_QUERY";
			}

			//TRY PARSE AS TUPLE QUERY
			try {
				jsonObj = new JSONObject(currentIterationStr);
			} catch (JSONException ex) {
				// try {// new JSONArray(jsonStr);// } catch (JSONException ex)...
    			logger.error("Not valid JSON string in tuple query result.", ex);
				resultType = "ERROR"; return;
			}
			
			//ClassLoader classloader = org.json.JSONObject.class.getClassLoader();
			//System.out.println("Core JSONObject came from " + classloader.getResource("org/json/JSONObject.class").getPath());

			jsonNames = JSONObject.getNames(jsonObj);
			jsonValues = new ArrayList<Value>();

            String currentJsonValueStr;
			for (String jsonName : jsonNames) {
				try {
                    currentJsonValueStr = jsonObj.getString(jsonName);
                } catch (JSONException ex) {
                    logger.error("Not valid JSON string in tuple query result.", ex);
                    resultType = "ERROR"; return;
                }
                
                try {
					jsonValues.add(valueFactory.createIRI(currentJsonValueStr));
				} catch (IllegalArgumentException e) {
					// maybe check if value str is a valid IRI?
					jsonValues.add(valueFactory.createLiteral(currentJsonValueStr));
				}

				if (!bindingNamesAreInitialized) {
					bindingNames.add(jsonName);
				}
			}

			if (!bindingNamesAreInitialized) {
				bindingNamesAreInitialized = true;
				builder.startQueryResult(bindingNames);
			}

			builder.handleSolution(new ListBindingSet(bindingNames, jsonValues));	
		});

		iterator.close();

        return finalizeResult();
	}

	private Object finalizeResult() {
		if(resultType == "ASK_QUERY") {
			return result;
		} 
        
        if(resultType == "TUPLE_QUERY") {
			if (!bindingNamesAreInitialized) {
				//handling the empty result
				builder.startQueryResult(new ArrayList<String>());
			}
	
			builder.endQueryResult();
			return builder.getQueryResult();
		}

		return null;
	}
}

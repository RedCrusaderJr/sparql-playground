package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.java;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.eclipse.rdf4j.query.impl.TupleQueryResultBuilder;
import org.huldra.math.BigInt;
import org.json.JSONException;
import org.json.JSONObject;

import com.marklogic.client.eval.EvalResultIterator;

public class ResultHandler {
    private static final Log logger = LogFactory.getLog(JavaClientEvaluator.class);
 
    Object result;
    String resultType;
    Boolean isFirstIteration;
    Boolean bindingNamesAreInitialized;
    List<String> bindingNames;
    
    TupleQueryResultBuilder builder;
    ValidatingValueFactory valueFactory;

    public ResultHandler() {
        valueFactory = new ValidatingValueFactory();
        builder = new TupleQueryResultBuilder();
    }
    
	public Object handleEvalResult(EvalResultIterator resultIterator) throws Exception {
        result = null;
        resultType = "TUPLE_QUERY";
        isFirstIteration = true;
        bindingNamesAreInitialized = false;
        bindingNames = new ArrayList<String>();
        
		try {
			resultIterator.forEach(currentResultIterator -> {
			
				String resultIterationString = currentResultIterator.getAs(String.class);
	
				//TRY PARSE AS ASK QUERY
				if (isFirstIteration && tryHandleAskQuery(resultIterationString)) {
					resultType = "ASK_QUERY";	
					return;				
				}
	
				//PARSE AS TUPLE QUERY
				resultType = "TUPLE_QUERY";
				if (!tryHandleTupleQuery(resultIterationString)) return;
			});

		} catch (Exception e) {
			resultIterator.close();
			logger.error("[handleEvalResult] Error: " + e.getMessage(), e);
			
			resultType = "ERROR"; 
			result = e.getMessage();
		}
		
		resultIterator.close();
		return finalizeResult();
	}

	private Boolean tryHandleAskQuery(String resultString) {
		isFirstIteration = false;
		
		if ("true".equals(resultString)) {
			result = true; 
		}
		else if ("false".equals(resultString)) {
			result = false;	
		}

		return result instanceof Boolean;
	}

	private Boolean tryHandleTupleQuery(String resultIterationString) {
		JSONObject jsonObj;
		
		try {
			jsonObj = new JSONObject(resultIterationString);
		
		} catch (JSONException ex) {
			logger.error("Not valid JSON string in tuple query result.", ex);
			resultType = "ERROR"; 
			return false;
		}
		
		String[] jsonNames = JSONObject.getNames(jsonObj);
		List<Value> jsonValues = new ArrayList<Value>();

		for (String jsonName : jsonNames) {
			
			if (!bindingNamesAreInitialized) {
				bindingNames.add(jsonName);
			}

			Object currentJsonValue = jsonObj.get(jsonName);

			if (currentJsonValue instanceof String) {	
				IRI iri = tryCreateIRI((String)currentJsonValue);
				if (iri != null) {
					jsonValues.add(iri);
					continue;
				}
			}
			
			jsonValues.add(createLiteral(currentJsonValue));
		}

		if (!bindingNamesAreInitialized) {
			bindingNamesAreInitialized = true;
			builder.startQueryResult(bindingNames);
		}

		builder.handleSolution(new ListBindingSet(bindingNames, jsonValues));
		return true;
	}

	public IRI tryCreateIRI(String iriString) {
		try {
			return valueFactory.createIRI(iriString);

		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public Literal createLiteral(Object jsonValue) {
		
		if (jsonValue instanceof String) return valueFactory.createLiteral((String)jsonValue);
		
		if (jsonValue instanceof Float) return valueFactory.createLiteral((Float)jsonValue);
		if (jsonValue instanceof Double) return valueFactory.createLiteral((Double)jsonValue);
		if (jsonValue instanceof BigDecimal) return valueFactory.createLiteral((BigDecimal)jsonValue);
		
		if (jsonValue instanceof Integer) return valueFactory.createLiteral((Integer)jsonValue);
		if (jsonValue instanceof Long) return valueFactory.createLiteral((Long)jsonValue);
		if (jsonValue instanceof BigInteger) return valueFactory.createLiteral((BigInteger)jsonValue);

		if (jsonValue instanceof Boolean) return valueFactory.createLiteral((Boolean)jsonValue);

		throw new IllegalArgumentException("[error in createLiteral] json value: " + jsonValue.toString() + ", type: " + jsonValue.getClass());
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

package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.java;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.client.eval.EvalResultIterator;
import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.JavaScriptQueryEvaluator;

public class JavaClientEvaluator implements JavaScriptQueryEvaluator {
	ResultHandler resultHandler;

	public JavaClientEvaluator() {
		resultHandler = new ResultHandler();
	}

	public Object evaluate(String jsQuery, Boolean returnRaw) throws Exception {
		EvalResultIterator resultIterator;	
		DatabaseClient client = createDbClient();
		
		try {
			resultIterator = client.newServerEval().javascript(jsQuery).eval();	
		} finally {
			client.release();
		}

		Object result;
		try {
			result = resultIterator;
			if (!returnRaw) {
				result = resultHandler.handleEvalResult(resultIterator);
			}
		} finally {
			resultIterator.close();
		}

		return result;
	}

	private DatabaseClient createDbClient() {
		String host = Application.getMarklogicHost();
		Integer port = Application.getMarklogicPort();
		String dbName = Application.getMarklogicDbName();
		SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");

		return DatabaseClientFactory.newClient(host, port, dbName, securityContext);
	}
}

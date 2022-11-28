package swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.java;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.client.eval.EvalResultIterator;
import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.geosparql.marklogic.jsquery.evaluator.JavaScriptQueryEvaluator;

public class JavaClientEvaluator implements JavaScriptQueryEvaluator {
	private String host;
	private Integer port;
	private String dbName;
	private SecurityContext securityContext;
	private ResultHandler resultHandler;

	public JavaClientEvaluator() {
		this.resultHandler = new ResultHandler();
		populateDatabaseClientParameters();
	}

	public Object evaluate(String jsQuery, Boolean returnRaw) throws Exception {
		DatabaseClient client = DatabaseClientFactory.newClient(host, port, dbName, securityContext);
		
		try {
			EvalResultIterator resultIterator = client.newServerEval().javascript(jsQuery).eval();	
			return returnRaw ? resultIterator : this.resultHandler.handleEvalResult(resultIterator);
		
		} finally {
			client.release();
		}
	}

	private void populateDatabaseClientParameters() {
		this.host = Application.getMarklogicHost();
		this.port = Application.getMarklogicPort();
		this.dbName = Application.getMarklogicDbName();
		this.securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");
	}
}

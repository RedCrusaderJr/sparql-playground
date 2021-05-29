package swiss.sib.sparql.playground.controller;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriterFactory;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterFactory;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLBooleanJSONWriterFactory;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriterFactory;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLBooleanXMLWriterFactory;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriterFactory;
import org.eclipse.rdf4j.query.resultio.text.BooleanTextWriterFactory;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriterFactory;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import swiss.sib.sparql.playground.exception.SparqlTutorialException;
import swiss.sib.sparql.playground.service.SparqlService;

@Controller
public class SparqlQueryController {
	private static final Log logger = LogFactory.getLog(SparqlController.class);

	@Autowired
	private SparqlService sparqlService;

	// Code taken from Sesame (before used to be in SparqlController)
	@RequestMapping(value = "/sparql")
	public void sparqlEndpoint(@RequestParam(value = "query", required = true) String queryStr,
			@RequestParam(value = "output", required = false) String output, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		if (queryStr == null) {
			throw new SparqlTutorialException("Missing parameter: query string is missing.");
		}

		synchronized (this) {
			Object queryResult = null;

			try {
				queryResult = sparqlService.evaluateQuery(queryStr);

			} catch (SparqlTutorialException e) {
				logger.error(e.getMessage(), e);
				throw e;

			} finally {
				finalize(queryResult, output, response);
			}
		}
	}

	private void finalize(Object queryResult, String output, HttpServletResponse response) {
		if (queryResult == null) {
			try {
				logger.error("Query Result is null.");
				response.sendError(500, "Serialization error: Query Result is null.");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			return;
		}

		if (queryResult instanceof TupleQueryResult) {
			finalizeTupleQueryResult((TupleQueryResult) queryResult, output, response);

		} else if (queryResult instanceof GraphQueryResult) {
			throw new NotImplementedException();

		} else if (queryResult instanceof Boolean) {
			finalizeBooleanQueryResult((Boolean) queryResult, output, response);

		} else {
			logger.warn("Unkown type of query result:" + queryResult.getClass());
		}
	}

	private void finalizeTupleQueryResult(TupleQueryResult queryResult, String output, HttpServletResponse response) {
		TupleQueryResultWriterFactory factory;

		if (output != null && output.equalsIgnoreCase("csv")) {
			factory = new SPARQLResultsCSVWriterFactory();
			response.setContentType(MimeTypeUtils.TEXT_PLAIN_VALUE);
			response.setHeader("Content-Disposition", "attachment; filename=" + "result." + output);

		} else if (output != null && output.equalsIgnoreCase("tsv")) {
			factory = new SPARQLResultsTSVWriterFactory();
			response.setContentType(MimeTypeUtils.TEXT_PLAIN_VALUE);

		} else if (output != null && output.equalsIgnoreCase("xml")) {
			factory = new SPARQLResultsXMLWriterFactory();
			response.setContentType(MimeTypeUtils.APPLICATION_XML_VALUE);

		} else {
			factory = new SPARQLResultsJSONWriterFactory();
			response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
		}

		try {
			renderTupleQueryResultInternal(factory, queryResult, response);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void finalizeBooleanQueryResult(Boolean queryResult, String output, HttpServletResponse response) {
		BooleanQueryResultWriterFactory factory;

		if (output != null && output.equalsIgnoreCase("txt")) {
			factory = new BooleanTextWriterFactory();
			response.setContentType(MimeTypeUtils.TEXT_PLAIN_VALUE);

		} else if (output != null && output.equalsIgnoreCase("xml")) {
			factory = new SPARQLBooleanXMLWriterFactory();
			response.setContentType(MimeTypeUtils.APPLICATION_XML_VALUE);

		} else {
			factory = new SPARQLBooleanJSONWriterFactory();
			response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
		}

		try {
			renderBooleanQueryResultInternal(factory, queryResult, response);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void renderTupleQueryResultInternal(TupleQueryResultWriterFactory factory,
			TupleQueryResult tupleQueryResult, HttpServletResponse response) throws IOException {
		response.setStatus(200);
		ServletOutputStream out = response.getOutputStream();

		try {
			TupleQueryResultWriter qrWriter = factory.getWriter(out);
			QueryResults.report(tupleQueryResult, qrWriter);

		} catch (QueryInterruptedException var16) {
			logger.error("Query interrupted", var16);
			response.sendError(503, "Query evaluation took too long");

		} catch (QueryEvaluationException var17) {
			logger.error("Query evaluation error", var17);
			response.sendError(500, "Query evaluation error: " + var17.getMessage());

		} catch (TupleQueryResultHandlerException var18) {
			logger.error("Serialization error", var18);
			response.sendError(500, "Serialization error: " + var18.getMessage());

		} finally {
			out.close();
		}
	}

	private void renderBooleanQueryResultInternal(BooleanQueryResultWriterFactory factory, Boolean booleanQueryResult,
			HttpServletResponse response) throws IOException {
		response.setStatus(200);
		ServletOutputStream out = response.getOutputStream();

		try {
			QueryResultIO.writeBoolean(booleanQueryResult, factory.getBooleanQueryResultFormat(), out);

		} catch (QueryInterruptedException var16) {
			logger.error("Query interrupted", var16);
			response.sendError(503, "Query evaluation took too long");

		} catch (QueryEvaluationException var17) {
			logger.error("Query evaluation error", var17);
			response.sendError(500, "Query evaluation error: " + var17.getMessage());

		} catch (TupleQueryResultHandlerException var18) {
			logger.error("Serialization error", var18);
			response.sendError(500, "Serialization error: " + var18.getMessage());

		} finally {
			out.close();
		}
	}
}

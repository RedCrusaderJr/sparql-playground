package swiss.sib.sparql.playground.controller;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterFactory;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriterFactory;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriterFactory;
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

		if (queryStr != null) {
			synchronized (this) {

				TupleQueryResult queryResult = (TupleQueryResult) sparqlService.evaluateQuery(queryStr);

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

				renderInternal(factory, queryResult, request, response);
			}

		} else {
			throw new SparqlTutorialException("Missing parameter: ");
		}
	}

	private void renderInternal(TupleQueryResultWriterFactory factory, TupleQueryResult tupleQueryResult,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
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
}

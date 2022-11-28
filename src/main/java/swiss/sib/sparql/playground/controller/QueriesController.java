package swiss.sib.sparql.playground.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.domain.SparqlQuery;
import swiss.sib.sparql.playground.repository.QueryDictionary;
import swiss.sib.sparql.playground.repository.impl.RepositoryType;
import swiss.sib.sparql.playground.service.PageService;
import swiss.sib.sparql.playground.utils.IOUtils;

/**
 * Controller used to server the SPARQL queries
 *
 * @author Daniel Teixeira http://github.com/ddtxra
 *
 */
@RestController
public class QueriesController {

	@Autowired
	QueryDictionary queryDictionary;
	@Autowired
	PageService pageService;

	@RequestMapping(value = "/queries", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<SparqlQuery> queries() throws IOException {
		return queryDictionary.getQueries(getQueriesPath());
	}

	@RequestMapping(value = "/queries/{queryId}.{extension}")
	public @ResponseBody byte[] queryImage(@PathVariable("queryId") String queryId,
			@PathVariable("extension") String extension) throws IOException {
		return pageService.getFileOrTry(getQueriesPath() + queryId + "." + extension, null,
				extension);
	}

	private String getQueriesPath() {
		String subforlder = Application.getRepositoryType() == RepositoryType.MARK_LOGIC ? "marklogic/" : "rdf4j/";
		return Application.getFolder() + "/queries/" + subforlder;
	}

	@RequestMapping(value = "/rdfhelp", produces = MediaType.APPLICATION_JSON_VALUE)
	public String rdfhelp() throws IOException {
		return IOUtils.readFile(Application.getFolder() + "/rdfhelp.json", "");
	}
}

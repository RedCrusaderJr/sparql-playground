package swiss.sib.sparql.playground.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import swiss.sib.sparql.playground.domain.JavaScriptQuery;
import swiss.sib.sparql.playground.domain.SparqlQuery;
import swiss.sib.sparql.playground.exception.SparqlTutorialException;
import swiss.sib.sparql.playground.utils.IOUtils;

/**
 * Dictionary for the queries
 *
 * @author Daniel Teixeira http://github.com/ddtxra
 *
 */
@Repository
public class QueryDictionary {

	private Map<String, String> resourcesMap = null;
	private Map<String, String> jsResourcesMap = null;

	protected Map<String, String> getResourcesMap(String folder) {
		// In case of changes, reload them (should be removed in a
		// production environment)
		loadResources(folder);
		return resourcesMap;
	}

	protected Map<String, String> getJSResourcesMap(String folder) {
		// In case of changes, reload them (should be removed in a
		// production environment)
		loadJSResources(folder);
		return jsResourcesMap;
	}

	protected String getResource(String resource) {
		if (resourcesMap.containsKey(resource)) {
			return resourcesMap.get(resource);

		} else {
			throw new SparqlTutorialException(
					"Resource " + resource + " not found on a total of " + resourcesMap.size() + " resources");
		}
	}

	protected String getJsResource(String resource) {
		if (jsResourcesMap.containsKey(resource)) {
			return jsResourcesMap.get(resource);

		} else {
			throw new SparqlTutorialException(
					"Resource " + resource + " not found on a total of " + jsResourcesMap.size() + " resources");
		}
	}

	protected void loadResources(String folder) {
		resourcesMap = new TreeMap<String, String>();
		for (final File fileEntry : new File(folder).listFiles()) {
			if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".rq")) {
				resourcesMap.put(fileEntry.getName().replace(".rq", ""),
						IOUtils.readFile(fileEntry.getAbsolutePath(), null));
			}
		}
	}

	protected void loadJSResources(String folder) {
		jsResourcesMap = new TreeMap<String, String>();
		for (final File fileEntry : new File(folder).listFiles()) {
			if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".js")) {
				jsResourcesMap.put(fileEntry.getName().replace(".js", ""),
						IOUtils.readFile(fileEntry.getAbsolutePath(), null));
			}
		}
	}

	public SparqlQuery getDemoQuery(String queryId) {
		String sparqlRaw = getResource(queryId);
		return buildSparqlQueryFromRawContent(queryId, sparqlRaw);
	}

	@Cacheable("queries")
	public synchronized List<SparqlQuery> getQueries(String folder) {
		Map<String, String> map = getResourcesMap(folder);
		Collection<String> queries = map.keySet();
		List<SparqlQuery> queriesList = new ArrayList<SparqlQuery>();

		for (String queryId : queries) {
			queriesList.add(buildSparqlQueryFromRawContent(queryId, map.get(queryId)));
		}

		// Sort by query id
		Collections.sort(queriesList, new Comparator<SparqlQuery>() {
			public int compare(SparqlQuery q1, SparqlQuery q2) {
				return Long.valueOf(q1.getUserQueryId()).compareTo(Long.valueOf(q2.getUserQueryId()));
			}
		});

		return queriesList;
	}

	public synchronized List<JavaScriptQuery> getJavascriptQueries(String folder) {
		Map<String, String> map = getJSResourcesMap(folder);
		Collection<String> queries = map.keySet();
		List<JavaScriptQuery> queriesList = new ArrayList<JavaScriptQuery>();

		for (String queryId : queries) {
			queriesList.add(buildJavaScriptQueryFromRawContent(queryId, map.get(queryId)));
		}

		return queriesList;
	}

	private JavaScriptQuery buildJavaScriptQueryFromRawContent(String queryId, String rawContent) {
		JavaScriptQuery jsQuery = new JavaScriptQuery();
		jsQuery.setTitle(queryId);
		jsQuery.setJavaScript(rawContent);

		return jsQuery;
	}

	private SparqlQuery buildSparqlQueryFromRawContent(String queryId, String rawContent) {
		SparqlQuery dsq = new SparqlQuery();
		Map<String, String> rawProps = getMetaInfo(rawContent);

		dsq.setSparql(rawProps.get("query"));
		dsq.setImg(rawProps.get("img"));
		dsq.setTitle(rawProps.get("title"));
		dsq.setOwner("nextprot");
		dsq.setBackgroundColor(rawProps.get("color"));
		dsq.setDescription(rawProps.get("comment"));

		try {

			String id = null;
			if (rawProps.get("id") != null) {
				id = rawProps.get("id");

			} else {
				id = queryId;
			}

			dsq.setPublicId(id);
			// removes all non numeric character
			dsq.setUserQueryId(Long.valueOf(id.replaceAll("[^0-9]", "")));

		} catch (Exception e) {
			dsq.setUserQueryId(0);
		}

		if (rawProps.get("tags") != null) {
			Set<String> tags = new HashSet<String>(Arrays.asList(rawProps.get("tags").split(",")));
			dsq.setTags(tags);

		} else {
			dsq.setTags(new HashSet<String>());
		}

		// dsq.set(rawProps.get("count"));
		// dsq.setAcs(rawProps.get("acs"));

		return dsq;
	}

	private String parseAndGlupRawQuery(String rawData, String q, String label, Map<String, String> meta) {
		String p = "[# ]?" + label + ":([^\\n]*)";
		Matcher m = Pattern.compile(p, Pattern.DOTALL | Pattern.MULTILINE).matcher(rawData);
		boolean found = false;

		while (m.find()) {
			found = true;

			if (!meta.containsKey(label)) {
				meta.put(label, m.group(1));

			} else {
				meta.put(label, meta.get(label) + "\n" + m.group(1));
			}
		}
		if (found) {
			return q.replaceAll(p, "");
		}

		return q;
	}

	private Map<String, String> getMetaInfo(String rawData) {
		Map<String, String> meta = new HashMap<String, String>();
		String q = rawData;

		q = parseAndGlupRawQuery(rawData, q, "id", meta);
		q = parseAndGlupRawQuery(rawData, q, "endpoint", meta);
		q = parseAndGlupRawQuery(rawData, q, "tags", meta);
		q = parseAndGlupRawQuery(rawData, q, "acs", meta);
		q = parseAndGlupRawQuery(rawData, q, "count", meta);
		q = parseAndGlupRawQuery(rawData, q, "title", meta);
		q = parseAndGlupRawQuery(rawData, q, "comment", meta);
		q = parseAndGlupRawQuery(rawData, q, "time", meta);
		q = parseAndGlupRawQuery(rawData, q, "img", meta);
		q = parseAndGlupRawQuery(rawData, q, "color", meta);

		meta.put("query", q.trim());

		return meta;
	}

	@Cacheable("faqs")
	public List<SparqlQuery> getFaqs() {
		return this.getQueries("faqs");
	}
}

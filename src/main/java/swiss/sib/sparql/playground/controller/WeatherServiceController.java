package swiss.sib.sparql.playground.controller;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import swiss.sib.sparql.playground.Application;
import swiss.sib.sparql.playground.repository.RDF4jRepository;

@Controller

public class WeatherServiceController {
	private static final Log logger = LogFactory.getLog(SparqlController.class);

	@Autowired
	private RDF4jRepository rdf4j;
	Map<Integer, Model> collection = new HashMap<Integer, Model>();
	int iterator = 0;
	int numOfFiles = 0;

	// @RequestMapping(value = "/sparql") // trebace mi ovako za moj controller, i
	// onda start, next pozivamo sa front-a
	// mvc pattern, na frontu, svaki taj neki segment, upravljace start-om na http i
	// start na mom, i pokrenuce se stoperica, to cemo videti jos
	// mozda samo start i next da vrate da su zavrsili, kad se zavrsi next metoda,
	// on okine neki simulator i svaki taj process data
	// bice dosta query-a, iscrtavanje buffer-a,a kad pozovemo next, onda cemo imati
	// query za isrctavanje
	// gromovi, location - wkt, promenicemo ulazne file-ove, da budu type storm i
	// taj propery location
	// uvucemo te trojke i dodjemo do koordinata i isrctamo, i pokrenemo
	// interception
	// tu kazemo ovaj deo koji je interception-ovan njega posebno isrctamo
	// oni line-ove ciji se poligoni seku, i to pocrvenimo
	// gavru docekamo sa gotovim
	// geo data saver ?
	// import export, povratna vrednost body response-a, 2 get-a, start, stop
	//
	@RequestMapping(value = "weather-service/start-new", method = RequestMethod.GET)
	public @ResponseBody Boolean startNewSimulation() {
		File folder = new File(Application.getFolder() + "/ws-data/");
		int id = 0;
		numOfFiles = folder.list().length;
		while (id < numOfFiles) {
			try {
				File file = new File(Application.getFolder() + "/ws-data/" + "test" + id + ".ttl");
				java.net.URL documentUrl = file.toURI().toURL();
				try (InputStream inputStream = documentUrl.openStream()) {
					Model results = Rio.parse(inputStream, documentUrl.toString(), RDFFormat.TURTLE);
					collection.put(id, results);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return false;
			}

			id++;
		}

		return true;
	}

	@RequestMapping(value = "weather-service/next", method = RequestMethod.GET)
	public @ResponseBody Boolean nextIteration() {
		List<Statement> oldStatements = new ArrayList<Statement>();
		List<Statement> newStatements = new ArrayList<Statement>();
		Model oldModel;
		Model newModel;

		if (iterator == 0) {
			int iteratorOld = numOfFiles - 1;
			oldModel = collection.get(iteratorOld);
		} else {
			oldModel = collection.get(iterator - 1);
		}

		for (Statement statement : oldModel) {
			if (rdf4j.hasStatement(statement)) {
				oldStatements.add(statement);
			}
		}
		if (!oldStatements.isEmpty()) {
			rdf4j.removeStatements(oldStatements);
		}

		newModel = collection.get(iterator);
		for (Statement statement : newModel) {
			newStatements.add(statement);
		}

		rdf4j.addStatements(newStatements);

		if (iterator == numOfFiles - 1) {
			iterator = 0;
		} else {
			iterator++;
		}

		return true;
	}

	@RequestMapping(value = "weather-service/reset", method = RequestMethod.GET)
	public @ResponseBody Boolean resetSimulation() {
		iterator = 0;
		return true;
	}

	@RequestMapping(value = "weather-service/stop", method = RequestMethod.GET)
	public @ResponseBody Boolean stopSimulation() {
		collection.clear();
		return true;
	}
}

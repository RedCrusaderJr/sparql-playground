package swiss.sib.sparql.playground.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import swiss.sib.sparql.playground.repository.impl.RDF4jRepositoryImpl;

@Controller

public class WeatherServiceController {
    public RDF4jRepositoryImpl rdf4j = new RDF4jRepositoryImpl();
    Map<Integer, Model> collection = new HashMap<Integer, Model>();
    int iterator = 0;

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
    @RequestMapping(value = "/start-simulation", method = RequestMethod.POST)
    public void start() throws IOException {

        int id = 0;
        while (id < 4) {
            File file = new File("C:\\Users\\ODBeast\\Desktop\\test" + id + ".ttl");
            java.net.URL documentUrl = file.toURI().toURL();
            InputStream inputStream = documentUrl.openStream();

            Model results = Rio.parse(inputStream, documentUrl.toString(), RDFFormat.TURTLE);
            collection.put(id, results);
            id++;
        }
    }

    @RequestMapping(value = "/pause-simulation", method = RequestMethod.POST)
    public void pause() throws IOException {

    }

    @RequestMapping(value = "/reset-simulation", method = RequestMethod.POST)
    public void reset() throws IOException {

    }

    @RequestMapping(value = "/stop-simulation", method = RequestMethod.POST)
    public void stop() throws IOException {

    }

    @RequestMapping(value = "/next-simulation", method = RequestMethod.POST)
    public void next() {
        List<Statement> oldStatements = new ArrayList<Statement>();
        List<Statement> newStatements = new ArrayList<Statement>();
        Model oldModel;
        Model newModel;

        if (!rdf4j.isEmpty()) {
            if (iterator == 0) {
                int iteratorOld = 3;
                oldModel = collection.get(iteratorOld);
            } else {
                oldModel = collection.get(iterator - 1);
            }

            for (Statement statement : oldModel) {
                oldStatements.add(statement);
            }
            rdf4j.removeStatements(oldStatements);
        }

        newModel = collection.get(iterator);
        for (Statement statement : newModel) {
            newStatements.add(statement);
        }

        rdf4j.addStatements(newStatements);

        if (iterator == 3) {
            iterator = 0;
        } else {
            iterator++;
        }
    }
}

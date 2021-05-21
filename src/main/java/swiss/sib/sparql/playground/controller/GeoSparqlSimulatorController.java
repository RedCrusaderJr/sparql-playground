package swiss.sib.sparql.playground.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeoSparqlSimulatorController {
	@RequestMapping(value = "simulator/start", method = RequestMethod.GET)
	public @ResponseBody Boolean start() {
		// todo: setup
		return true;
	}

	@RequestMapping(value = "simulator/evaluate", method = RequestMethod.GET)
	public @ResponseBody Boolean evaluate() {
		// todo: evaluate on repo
		return true;
	}

	@RequestMapping(value = "simulator/stop", method = RequestMethod.GET)
	public @ResponseBody Boolean stop() {
		// todo: clean up
		return true;
	}
}

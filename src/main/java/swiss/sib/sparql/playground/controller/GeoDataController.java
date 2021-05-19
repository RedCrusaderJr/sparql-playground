package swiss.sib.sparql.playground.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeoDataController {

	private String geojsonData = "";

	@RequestMapping(value = "/save-geo-data", method = RequestMethod.POST)
	public void saveData(@RequestBody String geojsonStr, HttpServletResponse response) throws IOException {
		this.geojsonData = geojsonStr;
	}

	@RequestMapping(value = "/load-geo-data", method = RequestMethod.GET)
	public @ResponseBody String loadData(HttpServletResponse response) {
		return this.geojsonData;
	}
}

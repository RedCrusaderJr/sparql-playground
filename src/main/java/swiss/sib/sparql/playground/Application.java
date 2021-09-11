package swiss.sib.sparql.playground;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

import swiss.sib.sparql.playground.geosparql.marklogic.query.evaluator.EvaluatorType;
import swiss.sib.sparql.playground.repository.impl.RepositoryType;

import java.io.File;

/**
 * A simple Spring Boot application for educative purpose in learning SPARQL
 *
 * @author Daniel Teixeira http://github.com/ddtxra
 *
 */
@SpringBootApplication
public class Application {

	private static final Log logger = LogFactory.getLog(Application.class);

	// argument with index 0
	private static String FOLDER = "default"; // e.g. nextprot, uniprot, geospatial
	// argument with index 1
	private static RepositoryType REPOSITORY_TYPE = RepositoryType.parseType(""); // get default value
	// argument with index 2
	private static Boolean INFERENCING_ENABLED = false;
	// argument with index 3
	private static EvaluatorType MARKLOGIC_EVALUATOR_TYPE = EvaluatorType.parseType(""); // get
																							// default
																							// value
	// argument with index 4
	private static String MARKLOGIC_HOST = "localhost";
	// argument with index 5
	private static int MARKLOGIC_PORT = 8111;
	// argument with index 6
	private static String MARKLOGIC_DB_NAME = "sparql-playground";

	public static String getFolder() {
		return FOLDER;
	}

	public static RepositoryType getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public static Boolean getInferencingEnabled() {
		return INFERENCING_ENABLED;
	}

	public static String getMarklogicHost() {
		return MARKLOGIC_HOST;
	}

	public static int getMarklogicPort() {
		return MARKLOGIC_PORT;
	}

	public static String getMarklogicDbName() {
		return MARKLOGIC_DB_NAME;
	}

	public static EvaluatorType getMarklogicEvaluatorType() {
		return MARKLOGIC_EVALUATOR_TYPE;
	}

	public static void main(String[] args) {
		logger.info("SPARQL Playground\n");

		try {
			setFolder(args);
			setRepositoryType(args);
			setInferencingEnabled(args);
			setMarkLogicSettings(args);

			SpringApplication.run(Application.class, args);

			logHostingAddress();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	// argument with index 0
	private static void setFolder(String[] args) {
		if (args.length == 0) {
			return;
		}

		String folderAux = args[0];

		if (new File(folderAux).exists()) {
			FOLDER = folderAux;

		} else {
			logger.debug(folderAux + " folder not found");
		}

		logger.debug("FOLDER: " + FOLDER);
	}

	// argument with index 1
	private static void setRepositoryType(String[] args) {
		String repositoryTypeStr = "";
		if (args.length >= 2 && args[1] != "") {
			repositoryTypeStr = args[1];
		}

		if (System.getProperty("repository.type") != null) {
			repositoryTypeStr = System.getProperty("repository.type");
		}

		REPOSITORY_TYPE = RepositoryType.parseType(repositoryTypeStr);
		logger.debug("REPOSITORY_TYPE: " + REPOSITORY_TYPE);
	}

	// argument with index 2
	private static void setInferencingEnabled(String[] args) {
		String inferencingEnabledStr = "";
		if (args.length >= 3 && args[2] != "") {
			inferencingEnabledStr = args[2];
		}

		// if format is wrong then it would be left on the default value: false
		if (inferencingEnabledStr.equals("true")) {
			INFERENCING_ENABLED = true;
		}

		logger.debug("INFERENCING_ENABLED: " + INFERENCING_ENABLED);
	}

	// arguments with index 3, 4 and 5 (and optional 6)
	private static void setMarkLogicSettings(String[] args) {
		// HOST AND ADDRESS MUST BE SET IN PAIR
		if (args.length < 6) {
			return;
		}

		if (args[3] != "") {
			MARKLOGIC_EVALUATOR_TYPE = EvaluatorType.parseType(args[3]);
		}

		if (args[4] != "") {
			MARKLOGIC_HOST = args[4];
		}

		if (args[5] != "") {
			String portString = args[5];

			try {
				MARKLOGIC_PORT = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
				logger.error(e.getMessage());
			}
		}

		// OPTIONAL
		if (args.length > 6 && args[6] != "") {
			MARKLOGIC_DB_NAME = args[6];
		}

		logger.debug("MARKLOGIC_EVALUATOR_TYPE: " + MARKLOGIC_EVALUATOR_TYPE);
		logger.debug("MARKLOGIC_ADDRESS: " + MARKLOGIC_HOST);
		logger.debug("MARKLOGIC_PORT: " + MARKLOGIC_PORT);
		logger.debug("MARKLOGIC_DB_NAME: " + MARKLOGIC_DB_NAME);
	}

	private static void logHostingAddress() {
		String port = null;

		if (System.getProperty("server.port") == null) {
			port = "8080";
			logger.info(
					"Taking default port 8080. The value of the port can be changed, by adding the jvm option: -Dserver.port=8090");

		} else {
			port = System.getProperty("server.port");
			logger.info("server.port option found. Taking port " + port);
		}

		String serverUrl = "http://localhost:" + port; // path to your new file
		logger.info("Server started at " + serverUrl);
	}

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("asset", "queries", "faqs", "page", "page-tree");
	}
}

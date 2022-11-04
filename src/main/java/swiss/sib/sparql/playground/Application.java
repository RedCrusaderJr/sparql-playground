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
	private static int APPLICATION_PORT = 8080;
	// argument with index 1
	private static String FOLDER = "default"; // e.g. nextprot, uniprot, geospatial
	// argument with index 2
	private static RepositoryType REPOSITORY_TYPE = RepositoryType.parseType(""); // get default value
	// argument with index 3
	private static Boolean INFERENCING_ENABLED = false;
	// argument with index 4
	private static EvaluatorType MARKLOGIC_EVALUATOR_TYPE = EvaluatorType.parseType(""); // get default value
	// argument with index 5
	private static String MARKLOGIC_HOST = "localhost";
	// argument with index 6
	private static int MARKLOGIC_PORT = 8111;
	// argument with index 7
	private static String MARKLOGIC_DB_NAME = "sparql-playground-db";

	public static int getApplicationPort() {
		return APPLICATION_PORT;
	}

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
			setApplicationPort(args);
			logger.debug("APPLICATION_PORT: " + APPLICATION_PORT);

			setFolder(args);
			logger.debug("FOLDER: " + FOLDER);

			setRepositoryType(args);
			logger.debug("REPOSITORY_TYPE: " + REPOSITORY_TYPE);

			setInferencingEnabled(args);
			logger.debug("INFERENCING_ENABLED: " + INFERENCING_ENABLED);

			setMarkLogicSettings(args);
			logger.debug("MARKLOGIC_EVALUATOR_TYPE: " + MARKLOGIC_EVALUATOR_TYPE);
			logger.debug("MARKLOGIC_ADDRESS: " + MARKLOGIC_HOST);
			logger.debug("MARKLOGIC_PORT: " + MARKLOGIC_PORT);
			logger.debug("MARKLOGIC_DB_NAME: " + MARKLOGIC_DB_NAME);

			SpringApplication.run(Application.class, args);
			logHostingAddress();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	// argument with index 0
	private static void setApplicationPort(String[] args)
	{
		if ((args.length < 1) || (args[0] == "")) {
			return;
		}

		try {
			APPLICATION_PORT = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			logger.error(e.getMessage());
		}
	}

	// argument with index 1
	private static void setFolder(String[] args) {
		if ((args.length < 2) || (args[1] == "")) {
			return;
		}

		String folderStr = args[1];

		if (new File(folderStr).exists()) {
			FOLDER = folderStr;
		} else {
			logger.warn(folderStr + " folder not found. Default value set to: " + FOLDER);
		}
	}

	// argument with index 2
	private static void setRepositoryType(String[] args) {	
		if (System.getProperty("repository.type") != null) {
			REPOSITORY_TYPE = RepositoryType.parseType(System.getProperty("repository.type"));
			return;
		}

		if ((args.length < 3) || (args[2] == "")) {
			return;
		}

		REPOSITORY_TYPE = RepositoryType.parseType(args[2]);
	}

	// argument with index 3
	private static void setInferencingEnabled(String[] args) {
		if ((args.length < 4) || (args[3] == "")) {
			return;
		}

		INFERENCING_ENABLED = args[3].equals("true");
	}

	// arguments with index 4, 5, 6 and 7
	private static void setMarkLogicSettings(String[] args) {
		// HOST AND ADDRESS MUST BE SET IN PAIR
		if ((args.length < 8) || (args[4] == "") || (args[5] == "") || (args[6] == "") || (args[7] == "")) {
			return;
		}

		MARKLOGIC_EVALUATOR_TYPE = EvaluatorType.parseType(args[4]);
		MARKLOGIC_HOST = args[5];

		try {
			MARKLOGIC_PORT = Integer.parseInt(args[6]);
		} catch (NumberFormatException e) {
			logger.error(e.getMessage());
		}

		MARKLOGIC_DB_NAME = args[7];
	}

	private static void logHostingAddress() {
		String portStr = null;

		if (System.getProperty("server.port") != null) {
			portStr = System.getProperty("server.port");
			logger.info("server.port option found. Taking port " + portStr);
			logger.info("The value of the port can be changed, by adding the jvm option: -Dserver.port=8090");
		}
		else  {
			portStr = Integer.toString(APPLICATION_PORT);
			System.setProperty("server.port", portStr);
		} 

		String serverUrl = "http://localhost:" + portStr;
		logger.info("Server started at " + serverUrl);
	}

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("asset", "queries", "faqs", "page", "page-tree");
	}
}

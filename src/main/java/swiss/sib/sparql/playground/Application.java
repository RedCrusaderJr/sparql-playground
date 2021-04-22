package swiss.sib.sparql.playground;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

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
	private static String FOLDER = "default"; // e.g. nextprot or uniprot
	// argument with index 1
	private static RepositoryType REPOSITORY_TYPE = RepositoryType.DEFAULT;
	// argument with index 2
	private static String MARKLOGIC_HOST = "localhost";
	// argument with index 3
	private static int MARKLOGIC_PORT = 8111;
	// argument with index 4
	private static String MARKLOGIC_DB_NAME = "sparql-playground";

	public static String getFolder() {
		return FOLDER;
	}

	public static RepositoryType getRepositoryType() {
		return REPOSITORY_TYPE;
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

	public static void main(String[] args) {
		logger.info("SPARQL Playground\n");

		try {
			setFolder(args);
			setRepositoryType(args);
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

		REPOSITORY_TYPE = RepositoryType.getRepositoryType(repositoryTypeStr);
		logger.debug("REPOSITORY_TYPE: " + REPOSITORY_TYPE);
	}

	// arguments with index 2 and 3
	private static void setMarkLogicSettings(String[] args) {
		// HOST AND ADDRESS MUST BE SET IN PAIR
		if (args.length < 4) {
			return;
		}

		if (args[2] != "") {
			MARKLOGIC_HOST = args[2];
		}

		if (args[3] != "") {
			String portString = args[3];

			try {
				MARKLOGIC_PORT = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
				logger.error(e.getMessage());
			}
		}

		// OPTIONAL
		if (args.length > 4 && args[4] != "") {
			MARKLOGIC_DB_NAME = args[4];
		}

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

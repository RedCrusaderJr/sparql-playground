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

	private static String FOLDER = "default"; // e.g. nextprot or uniprot
	private static RepositoryType REPOSITORY_TYPE = RepositoryType.DEFAULT;
	private static String MARKLOGIC_ADDRESS = "localhost";
	private static int MARKLOGIC_PORT = 8111;

	public static String getFolder() {
		return FOLDER;
	}

	public static RepositoryType getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public static String getMarklogicAddress() {
		return MARKLOGIC_ADDRESS;
	}

	public static int getMarklogicPort() {
		return MARKLOGIC_PORT;
	}

	public static void main(String[] args) {
		logger.info("SPARQL Playground\n");

		try {
			setFolder(args);
			setRepositoryType();
			setMarkLogicSettings(args);

			SpringApplication.run(Application.class, args);

			logHostingAddress();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

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

		logger.debug("FOLDER set to: " + FOLDER);
	}

	private static void setRepositoryType() {
		String repositoryTypeProperty = "marklogic";
		if (System.getProperty("repository.type") != null) {
			repositoryTypeProperty = System.getProperty("repository.type");
		}

		REPOSITORY_TYPE = RepositoryType.getRepositoryType(repositoryTypeProperty);

		if (REPOSITORY_TYPE != RepositoryType.DEFAULT) {
			logger.info("Found repository type property! Value: " + REPOSITORY_TYPE);
		} else {
			logger.info("Repository type was set to a default value: " + REPOSITORY_TYPE);
		}
	}

	private static void setMarkLogicSettings(String[] args) {
		if (args.length < 3) {
			return;
		}

		if (args[1] != "") {
			MARKLOGIC_ADDRESS = args[1];
			logger.debug("MARKLOGIC_ADDRESS set to: " + MARKLOGIC_ADDRESS);
		}

		if (args[2] != "") {
			String portString = args[2];

			try {
				MARKLOGIC_PORT = Integer.parseInt(portString);
				logger.debug("MARKLOGIC_PORT set to: " + MARKLOGIC_PORT);

			} catch (NumberFormatException e) {
				logger.error(e.getMessage());
				logger.debug("MARKLOGIC_PORT has default value of:" + MARKLOGIC_PORT);
			}
		}
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

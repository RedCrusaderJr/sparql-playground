package swiss.sib.sparql.playground.Performance.Functions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import swiss.sib.sparql.playground.function.BufferCreator;

@TestInstance(Lifecycle.PER_CLASS)
public class GeoSPARQLFunctionTest {
	private static final Log logger = LogFactory.getLog(GeoSPARQLFunctionTest.class);
	private static final String NEW_LINE = System.lineSeparator();

	private long sumDurations = 0;
	private long iterationCounter = 0;

	@RepeatedTest(800)
	public void javaFunctionTest() {
		long functionStart = System.currentTimeMillis();

		BufferCreator bufferCreator = new BufferCreator(1.0, 1.0, 0.0, 0.0, 50.0);
		String bufferStr = bufferCreator.create();

		long functionDuration = System.currentTimeMillis() - functionStart;

		sumDurations += functionDuration;
		iterationCounter++;
	}

	@AfterAll
	public void afterAll() {
		logger.info(
				"Average function evaluation: " + (double) sumDurations / (double) iterationCounter + " ms" + NEW_LINE);
		logger.info("Total function evaluation duration: " + sumDurations + " ms" + NEW_LINE);
	}
}

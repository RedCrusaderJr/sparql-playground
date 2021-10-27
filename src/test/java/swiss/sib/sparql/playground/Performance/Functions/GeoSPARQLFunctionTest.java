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

	@Test
	public void javaFunctionTest() {
		int totalIterations = 3400;

		for (int iteration = 0; iteration < totalIterations; iteration++) {
			long functionStart = System.currentTimeMillis();

			BufferCreator bufferCreator = new BufferCreator(1.0, 1.0, 0.0, 0.0, 50.0);
			String bufferStr = bufferCreator.create();

			long functionDuration = System.currentTimeMillis() - functionStart;

			sumDurations += functionDuration;
			iterationCounter++;
		}
	}

	@AfterAll
	public void afterAll() {
		StringBuilder sb = new StringBuilder();
		sb.append(NEW_LINE);
		sb.append("Average function evaluation: " + (double) sumDurations / (double) iterationCounter + " ms");
		sb.append(NEW_LINE);
		sb.append("Total function evaluation duration: " + sumDurations + " ms");
		sb.append(NEW_LINE);
		sb.append("Number of iterations: " + iterationCounter);
		sb.append(NEW_LINE);
		logger.info(sb.toString());
	}
}

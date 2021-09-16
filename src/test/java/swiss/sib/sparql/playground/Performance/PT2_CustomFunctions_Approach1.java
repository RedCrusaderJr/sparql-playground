package swiss.sib.sparql.playground.Performance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class PT2_CustomFunctions_Approach1 {
	private static final Log logger = LogFactory.getLog(PT2_CustomFunctions_Approach1.class);
	private static final String NEW_LINE = System.lineSeparator();
	private static final String QUERY_FOLDER = "noCustomFunctions";
	private static final String CMIXML_FOLDER = "1cimxml";
	private static final String MARKLOGIC_APPROACH = "approach1";

	private MetricTracer metricTracer;
	private PerformanceTestCommon ptc;

	@BeforeAll
	public void beforeAll() {
		this.metricTracer = new MetricTracer();
		this.ptc = new PerformanceTestCommon(metricTracer, QUERY_FOLDER, CMIXML_FOLDER);
	}

	@AfterEach
	public void afterEach() {
		this.ptc.afterEach();
		// this.ptc.deleteAll();
	}

	@AfterAll
	public void afterAll() {
		logger.info("Init metric: " + NEW_LINE + this.metricTracer.traceInit() + NEW_LINE);
		logger.info("Load metric: " + NEW_LINE + this.metricTracer.traceLoad() + NEW_LINE);
		logger.info("Eval metric: " + NEW_LINE + this.metricTracer.traceEval() + NEW_LINE);
		logger.info("Result counters: " + NEW_LINE + this.metricTracer.traceCounters() + NEW_LINE);
		logger.info("MarkLogic metric: " + NEW_LINE + this.metricTracer.traceMarkLogic() + NEW_LINE);
		// logger.info("Results metric: " + NEW_LINE + this.metricTracer.traceResults()
		// + NEW_LINE);
	}

	@Test
	public void test2() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawBufferOpt");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawBufferOpt");
		afterEach();
		long marklogicCount = this.ptc.markLogicRepositoryTest("drawBufferOpt", MARKLOGIC_APPROACH);

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogicCount);
	}

	@Test
	public void test3() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawHealthyOpt");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawHealthyOpt");
		afterEach();
		long marklogicCount = this.ptc.markLogicRepositoryTest("ml-drawHealthyOpt", MARKLOGIC_APPROACH);

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogicCount);
	}
}

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
	private static final Log logger = LogFactory.getLog(PT1_NoCustomFunctions.class);
	private static final String NEW_LINE = System.lineSeparator();
	private static final String SUBFOLDER = "customFunctions";

	private MetricTracer metricTracer;
	private PerformanceTestCommon ptc;

	@BeforeAll
	public void beforeAll() {
		this.metricTracer = new MetricTracer();
		this.ptc = new PerformanceTestCommon(metricTracer, SUBFOLDER);
	}

	@AfterEach
	public void afterEach() {
		this.ptc.afterEach();
	}

	@AfterAll
	public void afterAll() {
		logger.info("Init metric: " + NEW_LINE + this.metricTracer.traceInit() + NEW_LINE);
		logger.info("Load metric: " + NEW_LINE + this.metricTracer.traceLoad() + NEW_LINE);
		logger.info("Eval metric: " + NEW_LINE + this.metricTracer.traceEval() + NEW_LINE);
		logger.info("Result counters: " + NEW_LINE + this.metricTracer.traceCounters() + NEW_LINE);
		logger.info("MarkLogic metric: " + NEW_LINE + this.metricTracer.traceMarkLogic() + NEW_LINE);
	}

	@Test
	public void test2() throws Exception {
		try {
			this.ptc.defaultRepositoryTest("test2");
			afterEach();
			this.ptc.nativeRepositoryTest("test2");
			afterEach();
			this.ptc.markLogicRepositoryTest("test2");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Assertions.assertEquals(true, false);
		}
	}

	@Test
	public void test3() throws Exception {
		try {
			this.ptc.defaultRepositoryTest("test3");
			afterEach();
			this.ptc.nativeRepositoryTest("test3");
			afterEach();
			this.ptc.markLogicRepositoryTest("test3");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Assertions.assertEquals(true, false);
		}
	}
}

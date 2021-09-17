package swiss.sib.sparql.playground.Performance.Case1NoWarmUp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import swiss.sib.sparql.playground.Performance.MetricTracer;
import swiss.sib.sparql.playground.Performance.PerformanceTestCommon;

@TestInstance(Lifecycle.PER_CLASS)
public class Case1NoWarmUp1CIMXML {
	private static final Log logger = LogFactory.getLog(Case1NoWarmUp1CIMXML.class);
	private static final String NEW_LINE = System.lineSeparator();
	private static final String QUERY_FOLDER = "case12";
	private static final String CMIXML_FOLDER = "1cimxml";

	private MetricTracer metricTracer;
	private PerformanceTestCommon ptc;

	@BeforeAll
	public void beforeAll() {
		this.metricTracer = new MetricTracer();
		this.metricTracer.setTraceEnabled(true);
		this.ptc = new PerformanceTestCommon(metricTracer, QUERY_FOLDER, CMIXML_FOLDER);
		this.ptc.deleteAll();
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
		logger.info("Common metric: " + NEW_LINE + this.metricTracer.traceCommon() + NEW_LINE);
	}

	@Test
	public void test1Basic() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("basic");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("basic");
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest("basic", "approach1");
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest("basic", "approach2");

		Assertions.assertEquals(defaultCount, nativeCount);
		Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test2DrawFeeder() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawFeeder");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawFeeder");
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest("drawFeeder", "approach1");
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest("drawFeeder", "approach2");

		Assertions.assertEquals(defaultCount, nativeCount);
		Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test3DrawFeederOpt() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawFeederOpt");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawFeederOpt");
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest("drawFeederOpt", "approach1");
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest("drawFeederOpt", "approach2");

		Assertions.assertEquals(defaultCount, nativeCount);
		Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test4DrawBuffer() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawBuffer");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawBuffer");
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest("drawBuffer", "approach1");
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest("drawBuffer", "approach2");

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test5DrawBuffertOpt() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawBufferOpt");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawBufferOpt");
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest("drawBufferOpt", "approach1");
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest("drawBufferOpt", "approach2");

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test6DrawHealthy() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawHealthy");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawHealthy");
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest("ml-drawHealthy", "approach1");
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest("drawHealthy", "approach2");

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test7DrawHealthyOpt() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawHealthyOpt");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawHealthyOpt");
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest("ml-drawHealthyOpt", "approach1");
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest("drawHealthyOpt", "approach2");

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test8DrawAffected() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawAffected");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawAffected");
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest("ml-drawAffected", "approach1");
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest("drawAffected", "approach2");

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test9DrawAffectedOpt() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawAffectedOpt");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawAffectedOpt");
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest("ml-drawAffectedOpt", "approach1");
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest("drawAffectedOpt", "approach2");

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test10DrawHazard() throws Exception {
		long defaultCount = this.ptc.defaultRepositoryTest("drawHazard");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest("drawHazard");
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest("drawHazard", "approach1");
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest("drawHazard", "approach2");

		Assertions.assertEquals(defaultCount, nativeCount);
		Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}
}

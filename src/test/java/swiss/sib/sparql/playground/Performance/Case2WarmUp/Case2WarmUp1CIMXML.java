package swiss.sib.sparql.playground.Performance.Case2WarmUp;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import swiss.sib.sparql.playground.Performance.ExcelTracer;
import swiss.sib.sparql.playground.Performance.MetricTracer;
import swiss.sib.sparql.playground.Performance.PerformanceTestCommon;

@TestInstance(Lifecycle.PER_CLASS)
public class Case2WarmUp1CIMXML {
	private static final Log logger = LogFactory.getLog(Case2WarmUp1CIMXML.class);
	private static final String NEW_LINE = System.lineSeparator();
	private static final String QUERY_FOLDER = "case12";
	private static final String CMIXML_FOLDER = "1cimxml";
	private static final Long WARM_UP_ITERATIONS = (long) 10;

	private String currentTestName = "";
	private MetricTracer metricTracer;
	private PerformanceTestCommon ptc;

	@BeforeAll
	public void beforeAll() throws Exception {
		this.metricTracer = new MetricTracer();
		this.metricTracer.setTraceEnabled(false);
		this.ptc = new PerformanceTestCommon(metricTracer, QUERY_FOLDER, CMIXML_FOLDER);
		this.ptc.deleteAll();
		this.ptc.startExcelTracer(CMIXML_FOLDER);
	}

	@AfterEach
	public void afterEach() throws IOException {
		this.ptc.afterEach();
		this.ptc.deleteAll();
		this.ptc.resetRowTracker();
	}

	@AfterAll
	public void afterAll() throws IOException {
		this.ptc.trace(currentTestName);
		this.ptc.saveExcel();
	}

	@Test
	public void test1Basic() throws Exception {
		this.currentTestName = "Case2WarmUp1CIMXML-test1Basic";
		// warm up
		for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
			this.ptc.defaultRepositoryTest(ExcelTracer.BASIC, ExcelTracer.DEFAULT);
			afterEach();
			this.ptc.nativeRepositoryTest(ExcelTracer.BASIC, ExcelTracer.NATIVE);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.BASIC, PerformanceTestCommon.MARKLOGIC_APPROACH_1,
					ExcelTracer.MARKLOGIC_RDF4J);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.BASIC, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
					ExcelTracer.APPROACH_2);
		}
		this.metricTracer.setTraceEnabled(true);

		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.BASIC, ExcelTracer.DEFAULT);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.BASIC, ExcelTracer.NATIVE);
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest(ExcelTracer.BASIC,
				PerformanceTestCommon.MARKLOGIC_APPROACH_1, ExcelTracer.MARKLOGIC_RDF4J);
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest(ExcelTracer.BASIC,
				PerformanceTestCommon.MARKLOGIC_APPROACH_2, ExcelTracer.APPROACH_2);

		Assertions.assertEquals(defaultCount, nativeCount);
		Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test2DrawFeeder() throws Exception {
		this.currentTestName = "Case2WarmUp1CIMXML-test2DrawFeeder";
		// warm up
		for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
			this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_FEEDER, ExcelTracer.DEFAULT);
			afterEach();
			this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_FEEDER, ExcelTracer.NATIVE);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER, PerformanceTestCommon.MARKLOGIC_APPROACH_1,
					ExcelTracer.MARKLOGIC_RDF4J);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
					ExcelTracer.APPROACH_2);
		}
		this.metricTracer.setTraceEnabled(true);

		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_FEEDER, ExcelTracer.DEFAULT);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_FEEDER, ExcelTracer.NATIVE);
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER,
				PerformanceTestCommon.MARKLOGIC_APPROACH_1, ExcelTracer.MARKLOGIC_RDF4J);
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER,
				PerformanceTestCommon.MARKLOGIC_APPROACH_2, ExcelTracer.APPROACH_2);

		Assertions.assertEquals(defaultCount, nativeCount);
		Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test3DrawFeederOpt() throws Exception {
		this.currentTestName = "Case2WarmUp1CIMXML-test3DrawFeederOpt";
		// warm up
		for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
			this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT, ExcelTracer.DEFAULT);
			afterEach();
			this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT, ExcelTracer.NATIVE);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_1,
					ExcelTracer.MARKLOGIC_RDF4J);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
					ExcelTracer.APPROACH_2);
		}
		this.metricTracer.setTraceEnabled(true);

		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT, ExcelTracer.DEFAULT);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT, ExcelTracer.NATIVE);
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT,
				PerformanceTestCommon.MARKLOGIC_APPROACH_1, ExcelTracer.MARKLOGIC_RDF4J);
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT,
				PerformanceTestCommon.MARKLOGIC_APPROACH_2, ExcelTracer.APPROACH_2);

		Assertions.assertEquals(defaultCount, nativeCount);
		Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test4DrawBuffer() throws Exception {
		this.currentTestName = "Case2WarmUp1CIMXML-test4DrawBuffer";
		// warm up
		for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
			this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_BUFFER, ExcelTracer.DEFAULT);
			afterEach();
			this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_BUFFER, ExcelTracer.NATIVE);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER, PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML,
					ExcelTracer.APPROACH_1);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
					ExcelTracer.APPROACH_2);
		}
		this.metricTracer.setTraceEnabled(true);

		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_BUFFER, ExcelTracer.DEFAULT);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_BUFFER, ExcelTracer.NATIVE);
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER,
				PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML, ExcelTracer.APPROACH_1);
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER,
				PerformanceTestCommon.MARKLOGIC_APPROACH_2, ExcelTracer.APPROACH_2);

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test5DrawBuffertOpt() throws Exception {
		this.currentTestName = "Case2WarmUp1CIMXML-test5DrawBuffertOpt";
		// warm up
		for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
			this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, ExcelTracer.DEFAULT);
			afterEach();
			this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, ExcelTracer.NATIVE);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML,
					ExcelTracer.APPROACH_1);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
					ExcelTracer.APPROACH_2);
		}
		this.metricTracer.setTraceEnabled(true);

		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, ExcelTracer.DEFAULT);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, ExcelTracer.NATIVE);
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT,
				PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML, ExcelTracer.APPROACH_1);
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT,
				PerformanceTestCommon.MARKLOGIC_APPROACH_2, ExcelTracer.APPROACH_2);

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test6DrawHealthy() throws Exception {
		this.currentTestName = "Case2WarmUp1CIMXML-test6DrawHealthy";
		// warm up
		for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
			this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_HEALTHY, ExcelTracer.DEFAULT);
			afterEach();
			this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_HEALTHY, ExcelTracer.NATIVE);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY, PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML,
					ExcelTracer.APPROACH_1);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
					ExcelTracer.APPROACH_2);
		}
		this.metricTracer.setTraceEnabled(true);

		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_HEALTHY, ExcelTracer.DEFAULT);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_HEALTHY, ExcelTracer.NATIVE);
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY,
				PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML, ExcelTracer.APPROACH_1);
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY,
				PerformanceTestCommon.MARKLOGIC_APPROACH_2, ExcelTracer.APPROACH_2);

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test7DrawHealthyOpt() throws Exception {
		this.currentTestName = "Case2WarmUp1CIMXML-test7DrawHealthyOpt";
		// warm up
		for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
			this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, ExcelTracer.DEFAULT);
			afterEach();
			this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, ExcelTracer.NATIVE);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT,
					PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML, ExcelTracer.APPROACH_1);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
					ExcelTracer.APPROACH_2);
		}
		this.metricTracer.setTraceEnabled(true);

		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, ExcelTracer.DEFAULT);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, ExcelTracer.NATIVE);
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT,
				PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML, ExcelTracer.APPROACH_1);
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT,
				PerformanceTestCommon.MARKLOGIC_APPROACH_2, ExcelTracer.APPROACH_2);

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test8DrawAffected() throws Exception {
		this.currentTestName = "Case2WarmUp1CIMXML-test8DrawAffected";
		// warm up
		for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
			this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_AFFECTED, ExcelTracer.DEFAULT);
			afterEach();
			this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_AFFECTED, ExcelTracer.NATIVE);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED, PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML,
					ExcelTracer.APPROACH_1);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
					ExcelTracer.APPROACH_2);
		}
		this.metricTracer.setTraceEnabled(true);

		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_AFFECTED, ExcelTracer.DEFAULT);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_AFFECTED, ExcelTracer.NATIVE);
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED,
				PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML, ExcelTracer.APPROACH_1);
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED,
				PerformanceTestCommon.MARKLOGIC_APPROACH_2, ExcelTracer.APPROACH_2);

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test9DrawAffectedOpt() throws Exception {
		this.currentTestName = "Case2WarmUp1CIMXML-test9DrawAffectedOpt";
		// warm up
		for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
			this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT, ExcelTracer.DEFAULT);
			afterEach();
			this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT, ExcelTracer.NATIVE);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT,
					PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML, ExcelTracer.APPROACH_1);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
					ExcelTracer.APPROACH_2);
		}
		this.metricTracer.setTraceEnabled(true);

		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT, ExcelTracer.DEFAULT);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT, ExcelTracer.NATIVE);
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT,
				PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML, ExcelTracer.APPROACH_1);
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT,
				PerformanceTestCommon.MARKLOGIC_APPROACH_2, ExcelTracer.APPROACH_2);

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@Test
	public void test10DrawHazard() throws Exception {
		this.currentTestName = "Case2WarmUp1CIMXML-test10DrawHazard";
		// warm up
		for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
			this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_HAZARD, ExcelTracer.DEFAULT);
			afterEach();
			this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_HAZARD, ExcelTracer.NATIVE);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HAZARD, PerformanceTestCommon.MARKLOGIC_APPROACH_1,
					ExcelTracer.MARKLOGIC_RDF4J);
			afterEach();
			this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HAZARD, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
					ExcelTracer.APPROACH_2);
		}
		this.metricTracer.setTraceEnabled(true);

		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_HAZARD, ExcelTracer.DEFAULT);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_HAZARD, ExcelTracer.NATIVE);
		afterEach();
		long marklogic1Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HAZARD,
				PerformanceTestCommon.MARKLOGIC_APPROACH_1, ExcelTracer.MARKLOGIC_RDF4J);
		afterEach();
		long marklogic2Count = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HAZARD,
				PerformanceTestCommon.MARKLOGIC_APPROACH_2, ExcelTracer.APPROACH_2);

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}
}

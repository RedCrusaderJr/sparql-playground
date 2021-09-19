package swiss.sib.sparql.playground.Performance.Case1NoWarmUp;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import swiss.sib.sparql.playground.Performance.ExcelTracer;
import swiss.sib.sparql.playground.Performance.MetricTracer;
import swiss.sib.sparql.playground.Performance.PerformanceTestCommon;

//@Disabled
@TestInstance(Lifecycle.PER_CLASS)
public class Case1NoWarmUp100CIMXML {
	private static final Log logger = LogFactory.getLog(Case1NoWarmUp100CIMXML.class);
	private static final String NEW_LINE = System.lineSeparator();
	private static final String QUERY_FOLDER = "case12";
	private static final String CMIXML_FOLDER = "100cimxml";

	private String currentTestName = "";
	private MetricTracer metricTracer;
	private PerformanceTestCommon ptc;

	@BeforeAll
	public void beforeAll() throws Exception {
		this.metricTracer = new MetricTracer();
		this.metricTracer.setTraceEnabled(true);
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

	@RepeatedTest(10)
	public void test1Basic() throws Exception {
		this.currentTestName = "Case1NoWarmUp100CIMXML-test1Basic";
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

	@RepeatedTest(10)
	public void test2DrawFeeder() throws Exception {
		this.currentTestName = "Case1NoWarmUp100CIMXML-test2DrawFeeder";
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

	@RepeatedTest(10)
	public void test3DrawFeederOpt() throws Exception {
		this.currentTestName = "Case1NoWarmUp100CIMXML-test3DrawFeederOpt";
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

	@RepeatedTest(10)
	public void test4DrawBuffer() throws Exception {
		this.currentTestName = "Case1NoWarmUp100CIMXML-test4DrawBuffer";
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
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}

	@RepeatedTest(10)
	public void test5DrawBuffertOpt() throws Exception {
		this.currentTestName = "Case1NoWarmUp100CIMXML-test5DrawBuffertOpt";
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

	@RepeatedTest(10)
	public void test6DrawHealthy() throws Exception {
		this.currentTestName = "Case1NoWarmUp100CIMXML-test6DrawHealthy";
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

	@RepeatedTest(10)
	public void test7DrawHealthyOpt() throws Exception {
		this.currentTestName = "Case1NoWarmUp100CIMXML-test7DrawHealthyOpt";
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

	@RepeatedTest(10)
	public void test8DrawAffected() throws Exception {
		this.currentTestName = "Case1NoWarmUp100CIMXML-test8DrawAffected";
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

	@RepeatedTest(10)
	public void test9DrawAffectedOpt() throws Exception {
		this.currentTestName = "Case1NoWarmUp100CIMXML-test9DrawAffectedOpt";
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

	@RepeatedTest(10)
	public void test10DrawHazard() throws Exception {
		this.currentTestName = "Case1NoWarmUp100CIMXML-test10DrawHazard";
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
		Assertions.assertEquals(nativeCount, marklogic1Count);
		// Assertions.assertEquals(marklogic1Count, marklogic2Count);
	}
}

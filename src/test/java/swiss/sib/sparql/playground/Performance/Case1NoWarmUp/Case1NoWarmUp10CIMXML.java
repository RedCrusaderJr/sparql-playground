package swiss.sib.sparql.playground.Performance.Case1NoWarmUp;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import swiss.sib.sparql.playground.Performance.ExcelTracer;
import swiss.sib.sparql.playground.Performance.MetricTracer;
import swiss.sib.sparql.playground.Performance.PerformanceTestCommon;

//@Disabled
@TestInstance(Lifecycle.PER_CLASS)
public class Case1NoWarmUp10CIMXML {
	private static final String QUERY_FOLDER = "case12";
	private static final String CMIXML_FOLDER = "10cimxml";
	private static final String CASE_NAME = "CASE_1";

	private String currentTestName = "";
	private MetricTracer metricTracer;
	private PerformanceTestCommon ptc;

	@BeforeAll
	public void beforeAll() throws Exception {
		this.metricTracer = new MetricTracer();
		this.metricTracer.setTraceEnabled(true);
		this.ptc = new PerformanceTestCommon(metricTracer, QUERY_FOLDER, CMIXML_FOLDER, CASE_NAME);
		this.ptc.deleteAll();
		this.ptc.startExcelTracer(CMIXML_FOLDER);
	}

	private void afterEachLocalCase() {
		this.ptc.afterEach();
		this.ptc.deleteAll();
	}

	@AfterEach
	public void afterEachTest() throws IOException {
		afterEachLocalCase();
		this.ptc.nextRow();
		this.ptc.saveExcel();
	}

	@AfterAll
	public void afterAll() throws IOException {
		this.ptc.trace(currentTestName);
		this.ptc.saveExcel();
	}

	@RepeatedTest(10)
	public void test1Basic() throws Exception {
		this.currentTestName = "Case1NoWarmUp10CIMXML-test1Basic";
		this.ptc.defaultRepositoryTest(ExcelTracer.BASIC, ExcelTracer.DEFAULT, 0);
		afterEachLocalCase();
		this.ptc.nativeRepositoryTest(ExcelTracer.BASIC, ExcelTracer.NATIVE, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.BASIC, PerformanceTestCommon.MARKLOGIC_APPROACH_1,
				ExcelTracer.MARKLOGIC_RDF4J, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.BASIC, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
				ExcelTracer.APPROACH_2, 0);
	}

	@RepeatedTest(10)
	public void test2DrawFeeder() throws Exception {
		this.currentTestName = "Case1NoWarmUp10CIMXML-test2DrawFeeder";
		this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_FEEDER, ExcelTracer.DEFAULT, 0);
		afterEachLocalCase();
		this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_FEEDER, ExcelTracer.NATIVE, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER, PerformanceTestCommon.MARKLOGIC_APPROACH_1,
				ExcelTracer.MARKLOGIC_RDF4J, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
				ExcelTracer.APPROACH_2, 0);
	}

	@RepeatedTest(10)
	public void test3DrawFeederOpt() throws Exception {
		this.currentTestName = "Case1NoWarmUp10CIMXML-test3DrawFeederOpt";
		this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT, ExcelTracer.DEFAULT, 0);
		afterEachLocalCase();
		this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT, ExcelTracer.NATIVE, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_1,
				ExcelTracer.MARKLOGIC_RDF4J, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
				ExcelTracer.APPROACH_2, 0);
	}

	@RepeatedTest(10)
	public void test4DrawBuffer() throws Exception {
		this.currentTestName = "Case1NoWarmUp10CIMXML-test4DrawBuffer";
		this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_BUFFER, ExcelTracer.DEFAULT, 0);
		afterEachLocalCase();
		this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_BUFFER, ExcelTracer.NATIVE, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER, PerformanceTestCommon.MARKLOGIC_APPROACH_1,
				ExcelTracer.APPROACH_1, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
				ExcelTracer.APPROACH_2, 0);
	}

	@RepeatedTest(10)
	public void test5DrawBuffertOpt() throws Exception {
		this.currentTestName = "Case1NoWarmUp10CIMXML-test5DrawBuffertOpt";
		this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, ExcelTracer.DEFAULT, 0);
		afterEachLocalCase();
		this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, ExcelTracer.NATIVE, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_1,
				ExcelTracer.APPROACH_1, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
				ExcelTracer.APPROACH_2, 0);
	}

	@RepeatedTest(10)
	public void test6DrawHealthy() throws Exception {
		this.currentTestName = "Case1NoWarmUp10CIMXML-test6DrawHealthy";
		this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_HEALTHY, ExcelTracer.DEFAULT, 0);
		afterEachLocalCase();
		this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_HEALTHY, ExcelTracer.NATIVE, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY, PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML,
				ExcelTracer.APPROACH_1, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
				ExcelTracer.APPROACH_2, 0);
	}

	@RepeatedTest(10)
	public void test7DrawHealthyOpt() throws Exception {
		this.currentTestName = "Case1NoWarmUp10CIMXML-test7DrawHealthyOpt";
		this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, ExcelTracer.DEFAULT, 0);
		afterEachLocalCase();
		this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, ExcelTracer.NATIVE, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML,
				ExcelTracer.APPROACH_1, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
				ExcelTracer.APPROACH_2, 0);
	}

	@RepeatedTest(10)
	public void test8DrawAffected() throws Exception {
		this.currentTestName = "Case1NoWarmUp10CIMXML-test8DrawAffected";
		this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_AFFECTED, ExcelTracer.DEFAULT, 0);
		afterEachLocalCase();
		this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_AFFECTED, ExcelTracer.NATIVE, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED, PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML,
				ExcelTracer.APPROACH_1, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
				ExcelTracer.APPROACH_2, 0);
	}

	@RepeatedTest(10)
	public void test9DrawAffectedOpt() throws Exception {
		this.currentTestName = "Case1NoWarmUp10CIMXML-test9DrawAffectedOpt";
		this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT, ExcelTracer.DEFAULT, 0);
		afterEachLocalCase();
		this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT, ExcelTracer.NATIVE, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_1_ML,
				ExcelTracer.APPROACH_1, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_AFFECTED_OPT, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
				ExcelTracer.APPROACH_2, 0);
	}

	@RepeatedTest(10)
	public void test10DrawHazard() throws Exception {
		this.currentTestName = "Case1NoWarmUp10CIMXML-test10DrawHazard";
		this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_HAZARD, ExcelTracer.DEFAULT, 0);
		afterEachLocalCase();
		this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_HAZARD, ExcelTracer.NATIVE, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HAZARD, PerformanceTestCommon.MARKLOGIC_APPROACH_1,
				ExcelTracer.MARKLOGIC_RDF4J, 0);
		afterEachLocalCase();
		this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HAZARD, PerformanceTestCommon.MARKLOGIC_APPROACH_2,
				ExcelTracer.APPROACH_2, 0);
	}
}

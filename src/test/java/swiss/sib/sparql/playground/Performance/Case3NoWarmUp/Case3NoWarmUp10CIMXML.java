package swiss.sib.sparql.playground.Performance.Case3NoWarmUp;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import swiss.sib.sparql.playground.Performance.MetricTracer;
import swiss.sib.sparql.playground.Performance.PerformanceTestCommon;

public class Case3NoWarmUp10CIMXML {
	private static final String QUERY_FOLDER = "case34";
	private static final String CMIXML_FOLDER = "10cimxml";
	private static final String CASE_NAME = "CASE_3";
	// private static final Long WARM_UP_ITERATIONS = (long) 10;

	private String currentTestName = "";
	private MetricTracer metricTracer;
	private PerformanceTestCommon ptc;

	@BeforeAll
	public void beforeAll() throws Exception {
		this.metricTracer = new MetricTracer();
		this.metricTracer.setTraceEnabled(false);
		this.ptc = new PerformanceTestCommon(metricTracer, QUERY_FOLDER, CMIXML_FOLDER, CASE_NAME);
		this.ptc.deleteAll();
		this.ptc.startExcelTracer(CMIXML_FOLDER);
	}

	@AfterEach
	public void afterEach() throws IOException {
		this.ptc.afterEach();
		this.ptc.deleteAll();
		this.ptc.trace(currentTestName);
		this.ptc.resetRowTracker();
	}
}

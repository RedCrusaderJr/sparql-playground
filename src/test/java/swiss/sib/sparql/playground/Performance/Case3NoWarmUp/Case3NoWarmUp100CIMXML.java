package swiss.sib.sparql.playground.Performance.Case3NoWarmUp;

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

import swiss.sib.sparql.playground.Performance.MetricTracer;
import swiss.sib.sparql.playground.Performance.PerformanceTestCommon;

public class Case3NoWarmUp100CIMXML {
	private static final Log logger = LogFactory.getLog(Case3NoWarmUp100CIMXML.class);
	private static final String NEW_LINE = System.lineSeparator();
	private static final String QUERY_FOLDER = "case34";
	private static final String CMIXML_FOLDER = "100cimxml";
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
		this.ptc.trace(currentTestName);
		this.ptc.resetRowTracker();
	}
}

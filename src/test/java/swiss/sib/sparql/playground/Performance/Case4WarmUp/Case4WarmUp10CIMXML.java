package swiss.sib.sparql.playground.Performance.Case4WarmUp;

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

public class Case4WarmUp10CIMXML {
	private static final Log logger = LogFactory.getLog(Case4WarmUp10CIMXML.class);
	private static final String NEW_LINE = System.lineSeparator();
	private static final String QUERY_FOLDER = "case34";
	private static final String CMIXML_FOLDER = "10cimxml";
	private static final Long WARM_UP_ITERATIONS = (long) 10;

	private MetricTracer metricTracer;
	private PerformanceTestCommon ptc;

	@BeforeAll
	public void beforeAll() {
		this.metricTracer = new MetricTracer();
		this.metricTracer.setTraceEnabled(false);
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
}

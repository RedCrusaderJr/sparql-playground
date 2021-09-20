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

	private String currentTestName = "";
	private MetricTracer metricTracer;
	private PerformanceTestCommon ptc;

	@BeforeAll
	public void beforeAll() throws Exception {
		this.metricTracer = new MetricTracer();
		this.ptc = new PerformanceTestCommon(metricTracer, QUERY_FOLDER, CMIXML_FOLDER, "");
	}

	@AfterEach
	public void afterEach() {
		this.ptc.afterEach();
		// this.ptc.deleteAll();
	}

	@Test
	public void test2() throws Exception {
		this.currentTestName = "PT2_CustomFunctions_Approach1-test2";
		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, "");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, "");
		afterEach();
		long marklogicCount = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_BUFFER_OPT, MARKLOGIC_APPROACH, "");

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogicCount);
	}

	@Test
	public void test3() throws Exception {
		this.currentTestName = "PT2_CustomFunctions_Approach1-test3";
		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, "");
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, "");
		afterEach();
		long marklogicCount = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_HEALTHY_OPT, MARKLOGIC_APPROACH, "");

		Assertions.assertEquals(defaultCount, nativeCount);
		// Assertions.assertEquals(nativeCount, marklogicCount);
	}
}

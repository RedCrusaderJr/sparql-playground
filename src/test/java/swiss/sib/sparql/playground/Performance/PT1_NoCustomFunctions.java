package swiss.sib.sparql.playground.Performance;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@Disabled
public class PT1_NoCustomFunctions {
	private static final Log logger = LogFactory.getLog(PT1_NoCustomFunctions.class);
	private static final String NEW_LINE = System.lineSeparator();
	private static final String QUERY_FOLDER = "noCustomFunctions";
	private static final String CMIXML_FOLDER = "1cimxml";
	private static final String MARKLOGIC_APPROACH = "";

	private String currentTestName = "";
	private MetricTracer metricTracer;
	private PerformanceTestCommon ptc;

	@BeforeAll
	public void beforeAll() throws Exception {
		this.metricTracer = new MetricTracer();
		this.ptc = new PerformanceTestCommon(metricTracer, QUERY_FOLDER, CMIXML_FOLDER, "");
		this.ptc.deleteAll();
	}

	@AfterEach
	public void afterEach() throws IOException {
		this.ptc.afterEach();
		this.ptc.deleteAll();
		this.ptc.trace(currentTestName);
	}

	@Test
	public void testExcel() throws IOException {
		this.ptc.excel();
	}

	@Test
	public void test0() throws Exception {
		this.currentTestName = "PT1_NoCustomFunctions-test0";
		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.BASIC, "", 0);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.BASIC, "", 0);
		afterEach();
		long marklogicCount = this.ptc.markLogicRepositoryTest(ExcelTracer.BASIC, MARKLOGIC_APPROACH, "", 0);

		Assertions.assertEquals(defaultCount, nativeCount);
		Assertions.assertEquals(nativeCount, marklogicCount);
	}

	@Test
	public void test1() throws Exception {
		this.currentTestName = "PT1_NoCustomFunctions-test1";
		long defaultCount = this.ptc.defaultRepositoryTest(ExcelTracer.DRAW_FEEDER, "", 0);
		afterEach();
		long nativeCount = this.ptc.nativeRepositoryTest(ExcelTracer.DRAW_FEEDER, "", 0);
		afterEach();
		long marklogicCount = this.ptc.markLogicRepositoryTest(ExcelTracer.DRAW_FEEDER, MARKLOGIC_APPROACH, "", 0);

		Assertions.assertEquals(defaultCount, nativeCount);
		Assertions.assertEquals(nativeCount, marklogicCount);
	}
}

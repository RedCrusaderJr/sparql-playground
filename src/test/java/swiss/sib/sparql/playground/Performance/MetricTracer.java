package swiss.sib.sparql.playground.Performance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MetricTracer {
	private static final String NEW_LINE = System.lineSeparator();

	private Boolean traceEnabled;

	private StringBuilder sbInit;
	private StringBuilder sbLoad;
	private StringBuilder sbEval;
	private StringBuilder sbCounters;
	private StringBuilder sbMarkLogic;
	private StringBuilder sbResults;
	private StringBuilder sbCommon;

	public MetricTracer() {
		this.sbInit = new StringBuilder();
		this.sbLoad = new StringBuilder();
		this.sbEval = new StringBuilder();
		this.sbCounters = new StringBuilder();
		this.sbMarkLogic = new StringBuilder();
		this.sbResults = new StringBuilder();
		this.sbCommon = new StringBuilder();
	}

	public void setTraceEnabled(Boolean value) {
		this.traceEnabled = value;
	}

	public void appendInit(String traceStr) {
		if (!traceEnabled) {
			return;
		}
		this.sbInit.append(traceStr);
	}

	public void appendLoad(String traceStr) {
		if (!traceEnabled) {
			return;
		}
		this.sbLoad.append(traceStr);
	}

	public void appendEval(String traceStr) {
		if (!traceEnabled) {
			return;
		}
		this.sbEval.append(traceStr);
	}

	public void appendCounters(String traceStr) {
		if (!traceEnabled) {
			return;
		}
		this.sbCounters.append(traceStr);
	}

	public void appendMarkLogic(String traceStr) {
		if (!traceEnabled) {
			return;
		}
		this.sbMarkLogic.append(traceStr);
	}

	public void appendResults(String traceStr) {
		if (!traceEnabled) {
			return;
		}
		this.sbResults.append(traceStr);
	}

	public void appendCommon(String traceStr) {
		if (!traceEnabled) {
			return;
		}
		this.sbCommon.append(traceStr);
	}

	public String completeTrace(String currentTestName) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(NEW_LINE + NEW_LINE + "Complete trace for :" + currentTestName + NEW_LINE);
		sb.append(traceInit(currentTestName));
		sb.append(traceLoad(currentTestName));
		sb.append(traceCounters(currentTestName));
		sb.append(traceMarkLogic(currentTestName));
		// sb.append(traceResults(currentTestName));
		sb.append(traceCommon(currentTestName));

		// traceToFile(sb.toString(), currentTestName);

		return sb.toString();
	}

	public void traceToFile(String traceStr, String currentTestName) throws IOException {
		String fileName = "test_folder/" + currentTestName + ".txt";
		File traceFile = new File(fileName);
		if (!traceFile.exists()) {
			traceFile.createNewFile();
		}

		FileWriter myWriter = new FileWriter(fileName);
		myWriter.write(traceStr);
		myWriter.close();

	}

	public String traceInit(String currentTestName) {
		String trace = "Init metric: " + NEW_LINE + this.sbInit.toString() + NEW_LINE;
		return trace;
	}

	public String traceLoad(String currentTestName) {
		String trace = "Load metric: " + NEW_LINE + this.sbLoad.toString() + NEW_LINE;
		return trace;
	}

	public String traceEval(String currentTestName) {
		String trace = "Eval metric: " + NEW_LINE + this.sbEval.toString() + NEW_LINE;
		return trace;
	}

	public String traceCounters(String currentTestName) {
		String trace = "Counters metric: " + NEW_LINE + this.sbCounters.toString() + NEW_LINE;
		return trace;
	}

	public String traceMarkLogic(String currentTestName) {
		String trace = "MarkLogic metric: " + NEW_LINE + this.sbMarkLogic.toString() + NEW_LINE;
		return trace;
	}

	public String traceResults(String currentTestName) {
		String trace = "Results metric: " + NEW_LINE + this.sbResults.toString() + NEW_LINE;
		return trace;
	}

	public String traceCommon(String currentTestName) {
		String trace = "Common metric: " + NEW_LINE + this.sbCommon.toString() + NEW_LINE;
		return trace;
	}
}

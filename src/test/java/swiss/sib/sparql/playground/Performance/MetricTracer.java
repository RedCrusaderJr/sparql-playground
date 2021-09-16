package swiss.sib.sparql.playground.Performance;

public class MetricTracer {
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

	public String traceInit() {
		return this.sbInit.toString();
	}

	public String traceLoad() {
		return this.sbLoad.toString();
	}

	public String traceEval() {
		return this.sbEval.toString();
	}

	public String traceCounters() {
		return this.sbCounters.toString();
	}

	public String traceMarkLogic() {
		return this.sbMarkLogic.toString();
	}

	public String traceResults() {
		return this.sbResults.toString();
	}

	public String traceCommon() {
		return this.sbCommon.toString();
	}
}

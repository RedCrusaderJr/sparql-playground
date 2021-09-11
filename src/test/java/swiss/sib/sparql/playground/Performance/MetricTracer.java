package swiss.sib.sparql.playground.Performance;

public class MetricTracer {
	private StringBuilder sbInit;
	private StringBuilder sbLoad;
	private StringBuilder sbEval;
	private StringBuilder sbCounters;
	private StringBuilder sbMarkLogic;

	public MetricTracer() {
		this.sbInit = new StringBuilder();
		this.sbLoad = new StringBuilder();
		this.sbEval = new StringBuilder();
		this.sbCounters = new StringBuilder();
		this.sbMarkLogic = new StringBuilder();
	}

	public void appendInit(String traceStr) {
		this.sbInit.append(traceStr);
	}

	public void appendLoad(String traceStr) {
		this.sbLoad.append(traceStr);
	}

	public void appendEval(String traceStr) {
		this.sbEval.append(traceStr);
	}

	public void appendCounters(String traceStr) {
		this.sbCounters.append(traceStr);
	}

	public void appendMarkLogic(String traceStr) {
		this.sbMarkLogic.append(traceStr);
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
}

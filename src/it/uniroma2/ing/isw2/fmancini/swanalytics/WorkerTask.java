package it.uniroma2.ing.isw2.fmancini.swanalytics;

public enum WorkerTask {
	TICKETS("analyzeTickets"),
	DATASET("generateDataset"),
	VERSIONS("analyzeVersions");
	
	private final String taskMethod;
	private WorkerTask (final String taskMethod) {
		this.taskMethod = taskMethod;
	}
	public String getMethod() {
		return this.taskMethod;
	}
}

package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

public enum MetricType {
	NUMREVSIONS("NumRevisions"),
	NAUTH("NAuth"),
	LOCADDED("LOCAdded"),
	AVGLOCADDED("AVGLOCAdded"),
	MAXLOCADDED("MAXLOCAdded"),
	CHURN("Churn"),
	AVGCHURN("AVGChurn"),
	MAXCHURN("MAXChurn");
	
	private final String className;
	private MetricType (final String className) {
		this.className = className;
	}
	
	public String getClassName() {
		return this.className;
	}
	
}

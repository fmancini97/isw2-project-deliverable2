package it.uniroma2.ing.isw2.fmancini.machinelearning;

public enum Sampling {
	NOSAMPLING("No sampling"),
	OVERSAMPLING("Oversampling"),
	UNDERSAMPLING("Undersampling"),
	SMOTE("Smote");
	
	private final String samplingName;
	private Sampling (final String samplingName) {
		this.samplingName = samplingName;
	}
	
	@Override
	public String toString() {
		return this.samplingName;
	}
}

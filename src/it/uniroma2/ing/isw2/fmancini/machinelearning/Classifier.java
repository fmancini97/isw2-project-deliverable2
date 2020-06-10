package it.uniroma2.ing.isw2.fmancini.machinelearning;

public enum Classifier {
	RANDOMFOREST("RandomForest", "weka.classifiers.trees.RandomForest"),
	NAIVEBAYES("NaiveBayes", "weka.classifiers.bayes.NaiveBayes"),
	IBK("IBk", "weka.classifiers.lazy.IBk");
	
	private final String name;
	private final String className;
	
	private Classifier(final String name, final String method) {
		this.name = name;
		this.className = method;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public String getClassName() {
		return this.className;
	}
	
}

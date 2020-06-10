package it.uniroma2.ing.isw2.fmancini.machinelearning;

import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVable;

public class RunResult implements CSVable {
	private Integer numVersions;
	private Classifier classifier;
	private Float precision;
	private Float recall;
	private Float auc;
	private Float kappa;

	public RunResult(Integer numVersions, Classifier classifier, Float precision, Float recall, Float auc,
			Float kappa) {
		super();
		this.numVersions = numVersions;
		this.classifier = classifier;
		this.precision = precision;
		this.recall = recall;
		this.auc = auc;
		this.kappa = kappa;
	}
	
	

	public Integer getNumVersions() {
		return numVersions;
	}



	public Classifier getClassifier() {
		return classifier;
	}



	public Float getPrecision() {
		return precision;
	}



	public Float getRecall() {
		return recall;
	}



	public Float getAuc() {
		return auc;
	}



	public Float getKappa() {
		return kappa;
	}



	@Override
	public String toCSV() {
		return this.numVersions.toString() + "," + this.classifier.toString() + "," + this.precision.toString() + "," + this.recall.toString() + "," + this.auc.toString() + "," + this.kappa.toString();
	}

	@Override
	public String getHeader() {
		return "#TrainingRelease,Classifier,Precision,Recall,AUC,Kappa";
	}
}

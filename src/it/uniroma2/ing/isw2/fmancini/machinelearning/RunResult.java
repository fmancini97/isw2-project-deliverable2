package it.uniroma2.ing.isw2.fmancini.machinelearning;

import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVable;

public class RunResult implements CSVable {
	private String projectName;
	private Integer numVersions;
	private Float trainingPercentage;
	private Float defectiveInTrainingPercentage;
	private Float defectiveInTestingPercentage;
	private Classifier classifier;
	private Sampling sampling;
	private boolean featureSelection;
	private Float truePositive;
	private Float falsePositive;
	private Float trueNegative;
	private Float falseNegative;
	private Float precision;
	private Float recall;
	private Float auc;
	private Float kappa;

	public RunResult(String projectName, Classifier classifier, Sampling sampling, boolean featureSelection) {
		super();
		this.projectName = projectName;
		this.numVersions = 0;
		this.trainingPercentage = (float) 0;
		this.defectiveInTrainingPercentage = (float) 0;
		this.defectiveInTestingPercentage = (float) 0;
		this.classifier = classifier;
		this.sampling = sampling;
		this.featureSelection = featureSelection;
		this.truePositive = (float) 0;
		this.falsePositive = (float) 0;
		this.trueNegative = (float) 0;
		this.falseNegative = (float) 0;
		this.precision = (float) 0;
		this.recall = (float) 0;
		this.auc = (float) 0;
		this.kappa = (float) 0;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Integer getNumVersions() {
		return numVersions;
	}

	public void setNumVersions(Integer numVersions) {
		this.numVersions = numVersions;
	}
	

	public Float getTrainingPercentage() {
		return trainingPercentage;
	}

	public void setTrainingPercentage(Float trainingPercentage) {
		this.trainingPercentage = trainingPercentage;
	}

	public Float getDefectiveInTrainingPercentage() {
		return defectiveInTrainingPercentage;
	}

	public void setDefectiveInTrainingPercentage(Float defectiveInTrainingPercentage) {
		this.defectiveInTrainingPercentage = defectiveInTrainingPercentage;
	}

	public Float getDefectiveInTestingPercentage() {
		return defectiveInTestingPercentage;
	}

	public void setDefectiveInTestingPercentage(Float defectiveInTestingPercentage) {
		this.defectiveInTestingPercentage = defectiveInTestingPercentage;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	
	public boolean isFeatureSelection() {
		return featureSelection;
	}

	public void setFeatureSelection(boolean featureSelection) {
		this.featureSelection = featureSelection;
	}

	public Float getTruePositive() {
		return truePositive;
	}

	public void setTruePositive(Float truePositive) {
		this.truePositive = truePositive;
	}

	public Float getFalsePositive() {
		return falsePositive;
	}

	public void setFalsePositive(Float falsePositive) {
		this.falsePositive = falsePositive;
	}

	public Float getTrueNegative() {
		return trueNegative;
	}

	public void setTrueNegative(Float trueNegative) {
		this.trueNegative = trueNegative;
	}

	public Float getFalseNegative() {
		return falseNegative;
	}

	public void setFalseNegative(Float falseNegative) {
		this.falseNegative = falseNegative;
	}

	public Float getPrecision() {
		return precision;
	}

	public void setPrecision(Float precision) {
		this.precision = precision;
	}

	public Float getRecall() {
		return recall;
	}

	public void setRecall(Float recall) {
		this.recall = recall;
	}

	public Float getAuc() {
		return auc;
	}

	public void setAuc(Float auc) {
		this.auc = auc;
	}

	public Float getKappa() {
		return kappa;
	}

	public void setKappa(Float kappa) {
		this.kappa = kappa;
	}

	@Override
	public String toCSV() {
		String featureSelectionName = (this.featureSelection) ? "Best First" : "No selection";
		
		return this.projectName + "," + this.numVersions.toString() + "," + this.trainingPercentage.toString() + ","
				+ this.defectiveInTrainingPercentage + "," + this.defectiveInTestingPercentage.toString() + ","
				+ this.classifier.toString() + "," + this.sampling + "," + featureSelectionName + ","
				+ this.truePositive.toString() + "," + this.falsePositive.toString() + ","
				+ this.trueNegative.toString() + "," + this.falseNegative.toString() + ","
				+ this.precision.toString() + "," + this.recall.toString() + "," 
				+ this.auc.toString() + "," + this.kappa.toString();
	}

	@Override
	public String getHeader() {
		return "Dataset,#TrainingRelease,%Training,%Defective in training,%Defective in testing,Classifier,Sampling,Feature Selection,TP,FP,TN,FN,Precision,Recall,AUC,Kappa";
	}
}

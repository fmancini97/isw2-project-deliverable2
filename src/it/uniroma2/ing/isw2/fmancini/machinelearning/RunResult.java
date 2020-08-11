package it.uniroma2.ing.isw2.fmancini.machinelearning;

import it.uniroma2.ing.isw2.fmancini.csv.CSVable;

/**
 * Contains the results of a test of a classifier
 * @author fmancini
 *
 */
public class RunResult implements CSVable {
	private String projectName;
	private Integer numVersions;
	private Float trainingPercentage;
	private Float defectiveInTrainingPercentage;
	private Float defectiveInTestingPercentage;
	private Classifier classifier;
	private Sampling sampling;
	private boolean featureSelection;
	private Float truePositives;
	private Float falsePositives;
	private Float trueNegatives;
	private Float falseNegatives;
	private Float truePositiveRate;
	private Float falsePositiveRate;
	private Float trueNegativeRate;
	private Float falseNegativeRate;
	private Float precision;
	private Float recall;
	private Float auc;
	private Float kappa;

	public RunResult(String projectName, Classifier classifier, Sampling sampling, boolean featureSelection) {
		super();
		this.projectName = projectName;
		this.numVersions = 0;
		this.trainingPercentage = 0f;
		this.defectiveInTrainingPercentage = 0f;
		this.defectiveInTestingPercentage = 0f;
		this.classifier = classifier;
		this.sampling = sampling;
		this.featureSelection = featureSelection;
		this.truePositives = 0f;
		this.falsePositives = 0f;
		this.trueNegatives = 0f;
		this.falseNegatives = 0f;
		this.truePositiveRate = 0f;
		this.falsePositiveRate = 0f;
		this.trueNegativeRate = 0f;
		this.falseNegativeRate = 0f;
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

	public Sampling getSampling() {
		return sampling;
	}

	public void setSampling(Sampling sampling) {
		this.sampling = sampling;
	}

	public Float getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(Float truePositives) {
		this.truePositives = truePositives;
	}

	public Float getFalsePositives() {
		return falsePositives;
	}

	public void setFalsePositives(Float falsePositives) {
		this.falsePositives = falsePositives;
	}

	public Float getTrueNegatives() {
		return trueNegatives;
	}

	public void setTrueNegatives(Float trueNegatives) {
		this.trueNegatives = trueNegatives;
	}

	public Float getFalseNegatives() {
		return falseNegatives;
	}

	public void setFalseNegatives(Float falseNegatives) {
		this.falseNegatives = falseNegatives;
	}

	public Float getTruePositiveRate() {
		return truePositiveRate;
	}

	public void setTruePositiveRate(Float truePositiveRate) {
		this.truePositiveRate = truePositiveRate;
	}

	public Float getFalsePositiveRate() {
		return falsePositiveRate;
	}

	public void setFalsePositiveRate(Float falsePositiveRate) {
		this.falsePositiveRate = falsePositiveRate;
	}

	public Float getTrueNegativeRate() {
		return trueNegativeRate;
	}

	public void setTrueNegativeRate(Float trueNegativeRate) {
		this.trueNegativeRate = trueNegativeRate;
	}

	public Float getFalseNegativeRate() {
		return falseNegativeRate;
	}

	public void setFalseNegativeRate(Float falseNegativeRate) {
		this.falseNegativeRate = falseNegativeRate;
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
		Float correctedPrecision = (this.precision.isNaN()) ? 0.0f : this.precision;
		
		return this.projectName + ";" + this.numVersions.toString() + ";" + this.trainingPercentage.toString() + ";"
				+ this.defectiveInTrainingPercentage + ";" + this.defectiveInTestingPercentage.toString() + ";"
				+ this.classifier.toString() + ";" + this.sampling + ";" + featureSelectionName + ";"
				+ this.truePositives.toString() + ";" + this.falsePositives.toString() + ";"
				+ this.trueNegatives.toString() + ";" + this.falseNegatives.toString() + ";"
				+ this.truePositiveRate.toString() + ";" + this.falsePositiveRate.toString() + ";"
				+ this.trueNegativeRate.toString() + ";" + this.falseNegativeRate.toString() + ";"
				+ correctedPrecision.toString().replace(',', '.') + ";" + this.recall.toString() + ";" 
				+ this.auc.toString() + ";" + this.kappa.toString();
	}

	@Override
	public String getHeader() {
		return "Dataset;#TrainingRelease;%Training;%Defective in training;%Defective in testing;Classifier;Sampling;Feature Selection;TP;FP;TN;FN;TPR;FPR;TNR;FNR;Precision;Recall;AUC;Kappa";
	}
}

package it.uniroma2.ing.isw2.fmancini.machinelearning;

import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;


public class ClassifierThread extends Thread {
	private String projectName;
	private Classifier classifier;
	private Sampling sampling;
	private boolean featureSelection;
	private Integer numVersions;
	private Instances trainingSet;
	private Instances testingSet;
	private Integer numDefectiveInTraining;
	private Integer numDefectiveInTesting;
	private Integer datasetSize;
	private Integer classIndex;
	private RunResult result;
	private Exception error;
	
	public ClassifierThread(String projectName, Classifier classifier, Sampling sampling, boolean featureSelection, Integer datasetSize, Integer classIndex) {
		super();
		this.projectName = projectName;
		this.classifier = classifier;
		this.sampling = sampling;
		this.featureSelection = featureSelection;
		this.numVersions = 0;
		this.trainingSet = null;
		this.testingSet = null;
		this.numDefectiveInTesting = 0;
		this.numDefectiveInTraining = 0;
		this.classIndex = classIndex;
		this.datasetSize = datasetSize;
		this.error = null;
	}
	
	@Override
	public void run() {
		try {
			AbstractClassifier abstractClassfier = null; 
			Class<?> classifierClass;
			classifierClass = Class.forName(classifier.getClassName());
			abstractClassfier = (AbstractClassifier) classifierClass.newInstance();
			
			if (this.featureSelection) {
				AttributeSelection filter = new AttributeSelection();
				//create evaluator and search algorithm objects
				CfsSubsetEval eval = new CfsSubsetEval();
				GreedyStepwise search = new GreedyStepwise();
				//set the algorithm to search backward
				search.setSearchBackwards(true);
				//set the filter to use the evaluator and search algorithm
				filter.setEvaluator(eval);
				filter.setSearch(search);
				//specify the dataset
				filter.setInputFormat(this.trainingSet);
				this.trainingSet = Filter.useFilter(this.trainingSet, filter);
				this.testingSet = Filter.useFilter(this.testingSet, filter);
				
			}
			
			int numAttr = this.trainingSet.numAttributes();

			this.trainingSet.setClassIndex(numAttr - 1);
			this.testingSet.setClassIndex(numAttr - 1);
			
			FilteredClassifier fc = null;
			String[] opts = null;
			
			switch (this.sampling) {
				case UNDERSAMPLING:
					fc = new FilteredClassifier();
					fc.setClassifier(abstractClassfier);
					SpreadSubsample  spreadSubsample = new SpreadSubsample();
					opts = new String[]{ "-M", "1.0"};
					spreadSubsample.setOptions(opts);
					fc.setFilter(spreadSubsample);
					abstractClassfier = fc;
					break;
				case SMOTE:
					fc = new FilteredClassifier();
					fc.setClassifier(abstractClassfier);
					SMOTE smote = new SMOTE();
					smote.setInputFormat(this.trainingSet);
					fc.setFilter(smote);
					abstractClassfier = fc;
					break;
				case OVERSAMPLING:
					fc = new FilteredClassifier();
					fc.setClassifier(abstractClassfier);
					// Check what is the majority class
					Integer trainingSize = this.trainingSet.size();
					Double sampleSizePercent =  ((this.numDefectiveInTraining > ((double) trainingSize / 2.0)) ? (double) this.numDefectiveInTraining/trainingSize : (1 - ((double) this.numDefectiveInTraining/trainingSize)));
					sampleSizePercent = sampleSizePercent * 100 * 2;
					Resample resample = new Resample();
					resample.setInputFormat(this.trainingSet);
					opts = new String[]{ "-B", "1.0", "-Z", String.format("%.2f", sampleSizePercent).replace(',', '.')};
					resample.setOptions(opts);
					fc.setFilter(resample);
					abstractClassfier = fc;
					break;
				case NOSAMPLING:
					break;
			}
			

			abstractClassfier.buildClassifier(this.trainingSet);

			Evaluation eval = new Evaluation(this.testingSet);	

			eval.evaluateModel(abstractClassfier, this.testingSet); 
			
			Float truePositive = (float) eval.truePositiveRate(this.classIndex);
			Float falsePositive = (float) eval.falsePositiveRate(this.classIndex);
			Float trueNegative = (float) eval.trueNegativeRate(this.classIndex);
			Float falseNegative = (float) eval.falseNegativeRate(this.classIndex);
			Float precision = (float) eval.precision(this.classIndex);
			Float recall = (float) eval.recall(this.classIndex);
			Float auc = (float) eval.areaUnderROC(this.classIndex);
			Float kappa = (float) eval.kappa();
			
			this.result = new RunResult(this.projectName, this.classifier,this.sampling, this.featureSelection);
			this.result.setNumVersions(this.numVersions);
			this.result.setTrainingPercentage(((float) this.trainingSet.size()/this.datasetSize) * 100);
			this.result.setDefectiveInTrainingPercentage(((float) this.numDefectiveInTraining/this.trainingSet.size()) * 100);
			this.result.setDefectiveInTestingPercentage(((float) this.numDefectiveInTesting/this.testingSet.size()) * 100);
			this.result.setTruePositive(truePositive);
			this.result.setFalsePositive(falsePositive);
			this.result.setTrueNegative(trueNegative);
			this.result.setFalseNegative(falseNegative);
			this.result.setPrecision(precision);
			this.result.setRecall(recall);
			this.result.setAuc(auc);
			this.result.setKappa(kappa);
		} catch (Exception e) {
			this.error = e;
		}
		
	}

	public Integer getNumVersions() {
		return numVersions;
	}

	public void setNumVersions(Integer numVersions) {
		this.numVersions = numVersions;
	}

	public Instances getTrainingSet() {
		return trainingSet;
	}

	public void setTrainingSet(Instances trainingSet) {
		this.trainingSet = trainingSet;
	}

	public Instances getTestingSet() {
		return testingSet;
	}

	public void setTestingSet(Instances testingSet) {
		this.testingSet = testingSet;
	}

	public Integer getNumDefectiveInTraining() {
		return numDefectiveInTraining;
	}

	public void setNumDefectiveInTraining(Integer numDefectiveInTraining) {
		this.numDefectiveInTraining = numDefectiveInTraining;
	}

	public Integer getNumDefectiveInTesting() {
		return numDefectiveInTesting;
	}

	public void setNumDefectiveInTesting(Integer numDefectiveInTesting) {
		this.numDefectiveInTesting = numDefectiveInTesting;
	}

	public RunResult getResult() {
		return result;
	}

	public Exception getError() {
		return error;
	}
	 
	
}
package it.uniroma2.ing.isw2.fmancini.machinelearning;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class ClassifierThread extends Thread {
	private Classifier classifier;
	private Integer numVersions;
	private Instances trainingSet;
	private Instances testingSet;
	private RunResult result;
	private Exception error;
	
	public ClassifierThread(Classifier classifier,Integer numVersions ,Instances trainingSet, Instances testingSet) {
		super();
		this.classifier = classifier;
		this.numVersions = numVersions;
		this.trainingSet = trainingSet;
		this.testingSet = testingSet;
		this.error = null;
	}

	@Override
	public void run() {
		try {
			AbstractClassifier abstractClassfier = null; 
			Class<?> classifierClass;
			classifierClass = Class.forName(classifier.getClassName());
			abstractClassfier = (AbstractClassifier) classifierClass.newInstance();
			

			abstractClassfier.buildClassifier(trainingSet);

			Evaluation eval = new Evaluation(testingSet);	


			eval.evaluateModel(abstractClassfier, testingSet); 
			
			
			Float precision = (float) eval.precision(1);
			Float recall = (float) eval.recall(1);
			Float auc = (float) eval.areaUnderROC(1);
			Float kappa = (float) eval.kappa();
			
			this.result = new RunResult(this.numVersions, this.classifier, precision, recall, auc, kappa);
		} catch (Exception e) {
			this.error = e;
		}
		
	}
	
	public RunResult getResult() {
		return result;
	}

	public Exception getError() {
		return error;
	}
}
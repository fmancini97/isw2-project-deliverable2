package it.uniroma2.ing.isw2.fmancini.machinelearning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.uniroma2.ing.isw2.fmancini.swanalytics.ProjectWorker;
import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVDAO;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class WalkForwardThread extends Thread{
	private File projectDataset;
	private String projectName;
	private Logger logger;


	public WalkForwardThread(String projectName, File projectDataset) {
		super();
		this.projectName = projectName;
		this.projectDataset = projectDataset;
		this.logger = Logger.getLogger(ProjectWorker.class.getSimpleName() + "." + projectName);
	}

	@Override
	public void run() {
		try {
			this.logger.log(Level.INFO, "[{0}] Starting analysis...", this.projectName.toUpperCase());
			CSVDAO walkForwardResult = new CSVDAO("./output/" + this.projectName + "/" + this.projectName + "_walkforward");
			ArffLoader loader = new ArffLoader();
			loader.setSource(this.projectDataset);
			Instances data = loader.getDataSet();//get instances object
			Integer datasetSize = data.size();

			Instances training = new Instances(data, 0);
			Instances testing = new Instances(data, 0);


			Integer version = 1;
			Integer numDefectiveInTraining = 0;
			Integer numDefectiveInTesting = 0;
			List<RunResult> results = new ArrayList<>();
			for(Instance instance: data) {

				if (instance.value(0) == version) {
					training.add(instance);
					if (instance.value(instance.numAttributes() - 1) == 0) {
						numDefectiveInTraining++;
					}
				} else if (instance.value(0) == (version + 1)) {
					testing.add(instance);
					if (instance.value(instance.numAttributes() - 1) == 0) {
						numDefectiveInTesting++;
					}
				} else {
					results.addAll(this.runAnalysis(training, testing, version, datasetSize, numDefectiveInTraining, numDefectiveInTesting));

					training.addAll(testing);
					numDefectiveInTraining += numDefectiveInTesting;
					testing = new Instances(data, 0);
					numDefectiveInTesting = 0;
					testing.add(instance);
					if (instance.value(instance.numAttributes() - 1) == 0) {
						numDefectiveInTesting++;
					}
					version++;
				} 
			}

			results.addAll(this.runAnalysis(training, testing, version, datasetSize, numDefectiveInTraining, numDefectiveInTesting));

			walkForwardResult.open();
			walkForwardResult.saveToCSV(results);
			walkForwardResult.close();

		} catch ( Exception e) {
			this.logger.log(Level.SEVERE, "[{0}] Error while analyzing classifiers: {1}", new Object[] {this.projectName.toUpperCase(),e.getMessage()});
		}
		
		this.logger.log(Level.INFO, "[{0}] Analysis completed", this.projectName.toUpperCase());
	}

	private List<RunResult> runAnalysis(Instances training, Instances testing, Integer numVersions, Integer datasetSize, Integer numDefectiveInTraining, Integer numDefectiveInTesting) {
		List<RunResult> results = new ArrayList<>();
		try {
			//use a simple filter to remove a certain attribute	
			//set up options to remove 1st attribute
			String[] optsTraining = new String[]{ "-R", "1"};
			//create a Remove object (this is the filter class)
			Remove removeTraining = new Remove();
			//set the filter options
			removeTraining.setOptions(optsTraining);
			//pass the dataset to the filter
			removeTraining.setInputFormat(training);
			Instances trainingSet = Filter.useFilter(training, removeTraining);
			
			//use a simple filter to remove a certain attribute	
			//set up options to remove 1st attribute
			String[] optsTesting = new String[]{ "-R", "1"};

			Remove removeTesting = new Remove();
			//set the filter options
			removeTesting.setOptions(optsTesting);
			//pass the dataset to the filter
			removeTesting.setInputFormat(testing);
			Instances testingSet = Filter.useFilter(testing, removeTesting);


			

			List<ClassifierThread> threads = new ArrayList<>();
			for (Sampling sampling: Sampling.values()) {
				for (Classifier classifier: Classifier.values()) {
					this.logger.log(Level.INFO, "[{0}] Walkforward run number {1}: {2}, {3}, No feature selection", new Object[] {this.projectName.toUpperCase(), numVersions, classifier, sampling});
					ClassifierThread classifierRun = new ClassifierThread(this.projectName, classifier, sampling, false, datasetSize);
					classifierRun.setNumVersions(numVersions);
					classifierRun.setTrainingSet(new Instances(trainingSet));
					classifierRun.setTestingSet(new Instances(testingSet));
					classifierRun.setNumDefectiveInTraining(numDefectiveInTraining);
					classifierRun.setNumDefectiveInTesting(numDefectiveInTesting);
					threads.add(classifierRun);
					classifierRun.start();
					
					this.logger.log(Level.INFO, "[{0}] Walkforward run number {1}: {2}, {3}, Best first", new Object[] {this.projectName.toUpperCase(), numVersions, classifier, sampling});
					classifierRun = new ClassifierThread(this.projectName, classifier, sampling, true, datasetSize);
					classifierRun.setNumVersions(numVersions);
					classifierRun.setTrainingSet(new Instances(trainingSet));
					classifierRun.setTestingSet(new Instances(testingSet));
					classifierRun.setNumDefectiveInTraining(numDefectiveInTraining);
					classifierRun.setNumDefectiveInTesting(numDefectiveInTesting);
					threads.add(classifierRun);
					classifierRun.start();

				}
			}

			for (ClassifierThread classifierRun : threads) {
				classifierRun.join();
				Exception error = classifierRun.getError();
				if (error == null) {
					results.add(classifierRun.getResult());
				} else {
					this.logger.log(Level.WARNING, "[{0}] Error while analyzing classifier: {1}", new Object[] {this.projectName.toUpperCase(), error.getMessage()});
				}
			}
		} catch (Exception e) {
			this.logger.log(Level.WARNING, "[{0}] Error while analyzing classifier: {1}", new Object[] {this.projectName.toUpperCase(), e.getMessage()});
		}

		return results;
	}


}

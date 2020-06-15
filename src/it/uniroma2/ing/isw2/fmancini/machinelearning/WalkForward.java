package it.uniroma2.ing.isw2.fmancini.machinelearning;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.uniroma2.ing.isw2.fmancini.swanalytics.ProjectWorker;
import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVDAO;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;


public class WalkForward {

	public static void main(String[] args) {

		Logger logger = Logger.getLogger(ProjectWorker.class.getSimpleName());
		logger.log(Level.INFO, "Starting Walkforward analysis");
		// List all projects available
		File projectsDir = new File("./output");
		File[] projects = projectsDir.listFiles();

		
		for (File project : projects) {
			File projectDataset = new File(project.getAbsoluteFile(), project.getName() + "_dataset.arff");
			if (projectDataset.exists()) {
				WalkForward.analyzeProject(project.getName(), projectDataset);
				
			}
		}  
		logger.info("All analysis are completed!");

	}
	private static void analyzeProject(String projectName, File projectDataset) {
		Logger logger = Logger.getLogger(ProjectWorker.class.getSimpleName());
		try {
			logger.log(Level.INFO, "[{0}] Starting analysis...", projectName.toUpperCase());
			CSVDAO walkForwardResult = new CSVDAO("./output/" + projectName + "/" + projectName + "_walkforward");
			ArffLoader loader = new ArffLoader();
			loader.setSource(projectDataset);
			Instances data = loader.getDataSet();//get instances object
			Integer datasetSize = data.size();

			Instances training = new Instances(data, 0);
			Instances testing = new Instances(data, 0);
			
			
			Integer classIndex = WalkForward.retriveClassIndex(training);

			Integer version = 1;
			Integer numDefectiveInTraining = 0;
			Integer numDefectiveInTesting = 0;
			List<RunResult> results = new ArrayList<>();
			for(Instance instance: data) {

				if (instance.value(0) == version) {
					training.add(instance);
					if (instance.value(instance.numAttributes() - 1) == classIndex) {
						numDefectiveInTraining++;
					}
				} else if (instance.value(0) == (version + 1)) {
					testing.add(instance);
					if (instance.value(instance.numAttributes() - 1) == classIndex) {
						numDefectiveInTesting++;
					}
				} else {
					results.addAll(WalkForward.runAnalysis(projectName, training, testing, version, datasetSize, numDefectiveInTraining, numDefectiveInTesting));

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

			results.addAll(WalkForward.runAnalysis(projectName,training, testing, version, datasetSize, numDefectiveInTraining, numDefectiveInTesting));

			walkForwardResult.open();
			walkForwardResult.saveToCSV(results);
			walkForwardResult.close();

		} catch ( Exception e) {
			logger.log(Level.SEVERE, "[{0}] Error while analyzing classifiers: {1}", new Object[] {projectName.toUpperCase(),e.getMessage()});
		}

		logger.log(Level.INFO, "[{0}] Analysis completed", projectName.toUpperCase());
	}

	private static List<RunResult> runAnalysis(String projectName, Instances training, Instances testing, Integer numVersions, Integer datasetSize, Integer numDefectiveInTraining, Integer numDefectiveInTesting) {
		Logger logger = Logger.getLogger(ProjectWorker.class.getSimpleName());
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
			
			Attribute attribute = testingSet.attribute(testingSet.numAttributes() - 1);
			Enumeration<Object> values = attribute.enumerateValues();
			
			Integer classIndex = values.nextElement().toString().equals("Yes") ? 0 : 1;
			
			List<ClassifierThread> threads = new ArrayList<>();
			for (Sampling sampling: Sampling.values()) {
				for (Classifier classifier: Classifier.values()) {
					logger.log(Level.INFO, "[{0}] Walkforward run number {1}: {2}, {3}, No feature selection", new Object[] {projectName.toUpperCase(), numVersions, classifier, sampling});
					ClassifierThread classifierRun = new ClassifierThread(projectName, classifier, sampling, false, datasetSize, classIndex);
					classifierRun.setNumVersions(numVersions);
					classifierRun.setTrainingSet(new Instances(trainingSet));
					classifierRun.setTestingSet(new Instances(testingSet));
					classifierRun.setNumDefectiveInTraining(numDefectiveInTraining);
					classifierRun.setNumDefectiveInTesting(numDefectiveInTesting);
					threads.add(classifierRun);
					classifierRun.start();

					logger.log(Level.INFO, "[{0}] Walkforward run number {1}: {2}, {3}, Best first", new Object[] {projectName.toUpperCase(), numVersions, classifier, sampling});
					classifierRun = new ClassifierThread(projectName, classifier, sampling, true, datasetSize, classIndex);
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
					logger.log(Level.WARNING, "[{0}] Error while analyzing classifier: {1}", new Object[] {projectName.toUpperCase(), error.getMessage()});
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "[{0}] Error while analyzing classifier: {1}", new Object[] {projectName.toUpperCase(), e.getMessage()});
		}

		return results;
	}
	
	private static Integer retriveClassIndex(Instances instances) {
		Attribute attribute = instances.attribute(instances.numAttributes() - 1);
		Enumeration<Object> values = attribute.enumerateValues();
		
		return values.nextElement().toString().equals("Yes") ? 0 : 1;
	}
}

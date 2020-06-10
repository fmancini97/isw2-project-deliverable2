package it.uniroma2.ing.isw2.fmancini.machinelearning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.uniroma2.ing.isw2.fmancini.swanalytics.ProjectWorker;


public class WalkForward {

	public static void main(String[] args) {
		
		Logger logger = Logger.getLogger(ProjectWorker.class.getSimpleName());
		logger.log(Level.INFO, "Starting Walkforward analysis");
		// List all projects available
		File projectsDir = new File("./output");
		File[] projects = projectsDir.listFiles();

		List<WalkForwardThread> threads = new ArrayList<>();
		for (File project : projects) {
			File projectDataset = new File(project.getAbsoluteFile(), project.getName() + "_dataset.arff");
			if (projectDataset.exists()) {
				WalkForwardThread thread = new WalkForwardThread(project.getName(), projectDataset);
				threads.add(thread);
				thread.start();
			}
		}  
		
		for (WalkForwardThread thread: threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		logger.info("All analysis are completed!");

	}
}

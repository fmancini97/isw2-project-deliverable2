package it.uniroma2.ing.isw2.fmancini.swanalytics;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.IssueType;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SWAnalytics {

	public static void main(String[] args) {
		
		Logger logger = Logger.getLogger(SWAnalytics.class.getSimpleName());
		
		
		JSONParser jsonParser = new JSONParser();
		
		logger.info("Retrieving configuration");
		List<ProjectWorker> workerTasks = new ArrayList<>();

		
		try (FileReader reader = new FileReader("config.json")) {
	            //Read JSON file
			
			JSONArray projectConfs = (JSONArray) jsonParser.parse(reader);
			
			for (Integer i = 0; i < projectConfs.size(); i++) {
				ProjectWorker workerTask = SWAnalytics.genWorker((JSONObject) projectConfs.get(i), logger);
				if (workerTask != null) {
					workerTasks.add(workerTask);
				}
					
			}	 
			
			Integer i = 0;
			Integer j = 0;
			
			while(i < workerTasks.size()) {
				if (j-i < 5 && j < workerTasks.size()) {
					workerTasks.get(j).start();
					j++;
				} else {
					workerTasks.get(i).join();
					i++;
				}
			}	
	          
		} catch (IOException | ParseException e) {
			logger.log(Level.WARNING, "Error while retrieving configuration: {0}", e.getMessage());
			System.exit(1);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		logger.log(Level.INFO, "All the analysis are completed!");
		
		System.exit(0);
	
	}
	
	private static ProjectWorker genWorker(JSONObject projectConf, Logger logger) {
		ProjectWorker workerTask = null;
		String projectName = "";
		String gitReleaseRegex;
		Integer releasesPercentage;
		
		projectName = (String) projectConf.get("project-name");
		if (projectName == null || projectName.equals("")) {
	 		logger.log(Level.WARNING, "Error: Invalid project name or missing tasks"); 
		 	return workerTask;
	 	}
		
		JSONArray jsonTasks = (JSONArray) projectConf.get("tasks");
		
		if (jsonTasks == null) {
	 		logger.log(Level.WARNING, "Error: Missing tasks"); 
	 		return workerTask;

		}
	 	
	 	List<WorkerTask> tasks = new ArrayList<>();
	 	for (Integer j = 0; j < jsonTasks.size(); j++) {
	 		WorkerTask task = WorkerTask.valueOf((String) jsonTasks.get(j));
	 		
	 		if (task == null) {
		 		logger.log(Level.WARNING, "Error: Invalid Task {0}. Available analysis types are: {1}", new Object[] {(String) jsonTasks.get(j), Arrays.toString(WorkerTask.values())}); 

	 		} else {
	 			tasks.add(task);
	 		}
	 		
	 	}
	 	
	 	JSONArray jsonIssues = (JSONArray) projectConf.get("analysis-types");
	 	List<IssueType> issueTypes = new ArrayList<>();

	 	if (jsonIssues != null) {
	 		for (Integer j = 0; j < jsonIssues.size(); j++) {
				IssueType issueType = IssueType.valueOf((String) jsonIssues.get(j));
		 		
		 		if (issueType == null) {
			 		logger.log(Level.WARNING, "Error: Invalid Analysis type {0}. Available analysis types are: {1}", new Object[] {(String) jsonIssues.get(j), Arrays.toString(IssueType.values())}); 

		 		} else {
		 			issueTypes.add(issueType);
		 		}
		 		
		 	}
	 	}
	 	
	 	gitReleaseRegex = getReleaseRegex(projectConf);
	 	releasesPercentage = getReleasesPercentage(projectConf);
	 	
	 	workerTask = new ProjectWorker(projectName);
	 	workerTask.setIssueTypes(issueTypes);
	 	workerTask.setGitReleaseRegex(gitReleaseRegex);
	 	workerTask.setWorkerTasks(tasks);
	 	workerTask.setReleasesPercentage(releasesPercentage);
	 	
	 	return workerTask;
	}
	
	private static String getReleaseRegex(JSONObject projectConf) {
		String gitReleaseRegex = (String) projectConf.get("git-release-regex");
	 	
	 	if (gitReleaseRegex == null) {
	 		gitReleaseRegex = "%s";
	 	}
	 	return gitReleaseRegex;
	 	
	}
	
	private static Integer getReleasesPercentage(JSONObject projectConf) {
		Long releasesPercentage = (Long) projectConf.get("releases-percentage");
	 	
	 	if (releasesPercentage == null) {
	 		releasesPercentage = Long.valueOf(100);
	 	}
	 	return releasesPercentage.intValue();
	}
	
	
}



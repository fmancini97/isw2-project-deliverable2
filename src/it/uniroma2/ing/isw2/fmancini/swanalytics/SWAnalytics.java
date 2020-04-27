package it.uniroma2.ing.isw2.fmancini.swanalytics;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.MetricType;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SWAnalytics {

	public static void main(String[] args) {
		
		Logger logger = Logger.getLogger(SWAnalytics.class.getName());
		String projectName = "";
		IssueType issueType = null; 
		String gitReleasePath = "";
		
		JSONParser jsonParser = new JSONParser();
		JiraAPI jiraAPI;
		
		logger.info("Retrieving configuration");
		
		try (FileReader reader = new FileReader("config.json")) {
	            //Read JSON file
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
	            
		 	projectName = (String) obj.get("project-name");
		 	
		 	if (projectName == null || projectName.equals("")) {
		 		logger.log(Level.WARNING, "Error: Invalid project name"); 
			 	System.exit(1);
		 	}
			 	
		 	issueType = IssueType.valueOf((String) obj.get("analysis-type"));
			 	
		 	if (issueType == null) {
		 		logger.log(Level.WARNING, "Error: Invalid Analysis type. Available analysis types are: {0}", Arrays.toString(IssueType.values())); 
			 	System.exit(1);
		 	}	
		 	
		 	gitReleasePath = (String) obj.get("git-release-path");
		 	
		 	if (gitReleasePath == null) {
		 		gitReleasePath = "%s";
		 	}	
			 	
	          
		} catch (IOException | ParseException e) {
			logger.log(Level.WARNING, "Error while retrieving configuration: {0}", e.getMessage());
			System.exit(1);
		}
		
		
		
		
		ProjectAnalyzer projectAnalyzer = new ProjectAnalyzer(projectName);
		try {
			projectAnalyzer.init();
		} catch (IOException | GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.log(Level.INFO, "Looking for issues of type {0} for the project {1}", new Object[] {issueType, projectName});
		Map<String, Ticket> tickets;
		try {
			tickets = projectAnalyzer.analyzeTickets(issueType);
			logger.info("Generating CSV file");

			
			
			CSVDAO releasesCSV = new CSVDAO("output/" + projectName + "_" + issueType.toString());
			releasesCSV.open();
			
			releasesCSV.saveToCSV(new ArrayList<>(tickets.values()));
			releasesCSV.close();
			
			
			logger.info("CSV file generated successfully");
		} catch (IOException | GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		logger.info("Looking for project releases");

		
		try {
			List<Release> releases =projectAnalyzer.getReleases(gitReleasePath);

			logger.info("Generating CSV file");

			CSVDAO releasesCSV = new CSVDAO("output/" + projectName + "_versions");
			releasesCSV.open();
			
			releasesCSV.saveToCSV(releases);
			releasesCSV.close();

			logger.info("CSV file generated successfully");

		} catch (JSONException | IOException | java.text.ParseException | GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
		logger.info("Analyzing project classes");
		
		
		
		try {
			List<MetricType> metrics = new ArrayList<>();
			metrics.addAll(Arrays.asList(MetricType.values()));
			MeasurmentIterator measurmentIterator = projectAnalyzer.analyzeClasses(metrics);
			
			logger.info("Generating CSV file");

			
			CSVDAO metricCSV = new CSVDAO("output/" + projectName + "_classes");
			metricCSV.open();
			
			for (List<ClassData> classes = measurmentIterator.next(); classes.size() != 0; classes = measurmentIterator.next()) {
				metricCSV.saveToCSV(classes);
			}
			metricCSV.close();
			
			logger.info("CSV file generated successfully");
			
		} catch (JSONException | IOException | java.text.ParseException | GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}
}



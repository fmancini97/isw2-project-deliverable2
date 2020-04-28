package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.errors.GitAPIException;

import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVDAO;
import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVIncorrectNumValues;
import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.MetricType;

public class ProjectWorker extends Thread {
	
	private static String baseDir = "output/";
	private static String savingCSV = "Generating CSV file";
	private static String csvSaved = "CSV file generated successfully";

	
	private String projectName;
	private ProjectAnalyzer projectAnalyzer;
	private Logger logger;
	private List<IssueType> issueTypes;
	public void setIssueTypes(List<IssueType> issueTypes) {
		this.issueTypes = issueTypes;
	}

	public void setWorkerTasks(List<WorkerTask> workerTasks) {
		this.workerTasks = workerTasks;
	}

	public void setGitReleaseRegex(String gitReleaseRegex) {
		this.gitReleaseRegex = gitReleaseRegex;
	}


	private List<WorkerTask> workerTasks;
	private String gitReleaseRegex;
	
	
	
	public ProjectWorker(String projectName) {
		this.projectName = projectName;
		this.projectAnalyzer = new ProjectAnalyzer(projectName, baseDir);
		this.logger = Logger.getLogger(ProjectWorker.class.getSimpleName() + "-" + projectName);
		this.issueTypes = null;
		this.workerTasks = null;
		this.gitReleaseRegex = "%s";
	}
	
	protected void analyzeVersions() {
		this.logger.log(Level.INFO, "Looking for {0} releases", this.projectName);
		
		List<Release> releases = null;
		try {
			releases = projectAnalyzer.getReleases();
		} catch (IOException | ParseException | GitAPIException e) {
			this.logger.log(Level.WARNING, "Error while looking for {0} releases: {1}", new Object[] {this.projectName, e.getMessage()});
			return;
		}
		
		if (releases == null) {
			logger.log(Level.SEVERE, "Error while looking for {0} releases: ProjectAnalyzer.getReleases() returned a null value", this.projectName);
			return;
		}
		
		logger.info(savingCSV);

		try {
			CSVDAO releasesCSV = new CSVDAO(baseDir + projectName.toLowerCase() + "/" + projectName.toLowerCase() + "_versions");
			releasesCSV.open();
			releasesCSV.saveToCSV(releases);
			releasesCSV.close();

		} catch (IOException | CSVIncorrectNumValues e) {
			this.logger.log(Level.WARNING, "Error while saving releases: {0}", e.getMessage());
			return;
		} 
		
		logger.info(csvSaved);
	}
	
	protected void analyzeClasses() {
		
		this.logger.log(Level.INFO, "Analyzing {0} classes", this.projectName);
					
		MeasurmentIterator measurmentIterator = null;
		try {
			List<MetricType> metrics = new ArrayList<>();
			metrics.addAll(Arrays.asList(MetricType.values()));
			measurmentIterator = projectAnalyzer.analyzeClasses(metrics);
		} catch (IOException | ParseException | GitAPIException e) {
			this.logger.log(Level.WARNING, "Error while analyzing {0} classes: {1}", new Object[] {this.projectName, e.getMessage()});
			return;
		}
		
		if (measurmentIterator == null) {
			logger.log(Level.SEVERE, "Error while looking for {0} releases: ProjectAnalyzer.analyzeClasses() returned a null value", this.projectName);
			return;
		}
		
		
		logger.info(savingCSV);

		
		CSVDAO metricCSV = new CSVDAO(baseDir + projectName.toLowerCase() + "/" + projectName.toLowerCase() + "_classes");
		try {
			metricCSV.open();
			for (List<ClassData> classes = measurmentIterator.next(); classes != null; classes = measurmentIterator.next()) {
				metricCSV.saveToCSV(classes);
			}
			metricCSV.close();
			
		} catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | GitAPIException | InterruptedException e) {
			this.logger.log(Level.WARNING, "Error while analyzing {0} classes: {1}", new Object[] {this.projectName, e.getMessage()});
			Thread.currentThread().interrupt();
			return;
		} catch (IOException | CSVIncorrectNumValues e)  {
			this.logger.log(Level.WARNING, "Error while saving classes: {0}", e.getMessage());
			return;
		} 
		
		
		logger.info(csvSaved);
	}
	
	protected void analyzeTickets() {
		for (IssueType issueType : this.issueTypes) {
			logger.log(Level.INFO, "Looking for issues of type {0} for the project {1}", new Object[] {issueType, projectName});
			Map<String, Ticket> tickets;
			
			try {
				tickets = projectAnalyzer.analyzeTickets(issueType);
			} catch (IOException | GitAPIException e) {
				this.logger.log(Level.WARNING, "Error while looking for issues of type {0}: {1}", new Object[] {issueType, e.getMessage()});
			return;
			}
				
			if (tickets == null) {
				logger.log(Level.SEVERE, "Error while looking for {0} releases: ProjectAnalyzer.analyzeTickets() returned a null value", this.projectName);
				return;
			}
				
			logger.info(savingCSV);
				
			try {
				CSVDAO releasesCSV = new CSVDAO(baseDir + projectName.toLowerCase() + "/" + projectName.toLowerCase() + "_" + issueType.toString());

				releasesCSV.open();
				releasesCSV.saveToCSV(new ArrayList<>(tickets.values()));
				releasesCSV.close();
					
			} catch (IOException | CSVIncorrectNumValues e) {
				this.logger.log(Level.WARNING, "Error while saving tickets: {0}", e.getMessage());
				return;

			}
			logger.info(csvSaved);
		}
	}
	

	@Override
	public void run() {
		try {
			projectAnalyzer.init(this.gitReleaseRegex);
		} catch (IOException | GitAPIException e) {
			this.logger.log(Level.WARNING, "Error while initializing analysis: {0}", e.getMessage());

		}
		
		for (WorkerTask workerTask : workerTasks) {
			try {
				Method task = this.getClass().getDeclaredMethod(workerTask.getMethod());
				task.invoke(this, (Object[]) null);			
				
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.log(Level.SEVERE, "Error while executing {0} task: {1}", new Object[] {workerTask.getMethod(), e.getMessage()});

			}
		}
		
		
	}

}

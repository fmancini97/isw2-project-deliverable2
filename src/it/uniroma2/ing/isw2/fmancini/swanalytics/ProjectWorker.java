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
import org.json.JSONException;

import it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis.ClassData;
import it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis.MeasurmentIterator;
import it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis.Release;
import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVDAO;
import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVIncorrectNumValues;
import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVable;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.IssueType;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.Ticket;
import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.MetricType;

public class ProjectWorker extends Thread {
	
	private static String baseDir = "output/";
	private static String savingCSV = "Generating CSV file";
	private static String csvSaved = "CSV file generated successfully";
	private static String csvLogTemplate = "[{0}] {1}";

	
	private String projectName;
	private ProjectAnalyzer projectAnalyzer;
	private Logger logger;
	private List<IssueType> issueTypes;
	private List<WorkerTask> workerTasks;
	private String gitReleaseRegex;
	private Integer releasesPercentage;
	
	
	
	public ProjectWorker(String projectName) {
		this.projectName = projectName;
		this.projectAnalyzer = new ProjectAnalyzer(projectName, baseDir);
		this.logger = Logger.getLogger(ProjectWorker.class.getSimpleName() + "." + projectName);
		this.issueTypes = null;
		this.workerTasks = null;
		this.gitReleaseRegex = "%s";
		this.releasesPercentage = 100;
		
	}
	
	public void setIssueTypes(List<IssueType> issueTypes) {
		this.issueTypes = issueTypes;
	}

	public void setWorkerTasks(List<WorkerTask> workerTasks) {
		this.workerTasks = workerTasks;
	}

	public void setGitReleaseRegex(String gitReleaseRegex) {
		this.gitReleaseRegex = gitReleaseRegex;
	}
	
	public void setReleasesPercentage(Integer releasesPercentage) {
		this.releasesPercentage = releasesPercentage;
	}

	protected void analyzeVersions() {
		this.logger.log(Level.INFO, "[{0}] Looking for {0} releases", this.projectName);
		
		List<Release> releases = null;
		try {
			releases = projectAnalyzer.getReleases();
		} catch (IOException | ParseException | GitAPIException e) {
			this.logger.log(Level.WARNING, "[{0}] Error while looking for {0} releases: {1}", new Object[] {this.projectName, e.getMessage()});
			return;
		}
		
		if (releases == null) {
			logger.log(Level.SEVERE, "[{0}] Error while looking for {0} releases: ProjectAnalyzer.getReleases() returned a null value", this.projectName);
			return;
		}
		
		logger.log(Level.INFO, csvLogTemplate, new Object[] {this.projectName, savingCSV});

		try {
			CSVDAO releasesCSV = new CSVDAO(baseDir + projectName.toLowerCase() + "/" + projectName.toLowerCase() + "_versions");
			releasesCSV.open();
			List<List<? extends CSVable>> data = new ArrayList<>();
			data.add(releases);
			releasesCSV.saveToCSV(data);
			releasesCSV.close();

		} catch (IOException | CSVIncorrectNumValues e) {
			this.logger.log(Level.WARNING, "[{0}] Error while saving releases: {1}", new Object[] {this.projectName, e.getMessage()});
			return;
		} 
		
		logger.log(Level.INFO, csvLogTemplate, new Object[] {this.projectName, csvSaved});
	}
	
	protected void generateDataset() {
		
		this.logger.log(Level.INFO, "[{0}] Generating dataset...", this.projectName);
					
		MeasurmentIterator measurmentIterator = null;
		try {
			List<MetricType> metrics = new ArrayList<>();
			metrics.addAll(Arrays.asList(MetricType.values()));
			measurmentIterator = projectAnalyzer.analyzeClasses(metrics, this.releasesPercentage);
		} catch (IOException | ParseException | GitAPIException e) {
			this.logger.log(Level.WARNING, "[{0}] Error while analyzing {0} classes: {1}", new Object[] {this.projectName, e.getMessage()});
			return;
		}
		
		if (measurmentIterator == null) {
			logger.log(Level.SEVERE, "[{0}] Error while looking for {0} releases: ProjectAnalyzer.analyzeClasses() returned a null value", this.projectName);
			return;
		}
		
		
		logger.log(Level.INFO, csvLogTemplate, new Object[] {this.projectName, savingCSV});

		
		CSVDAO metricCSV = new CSVDAO(baseDir + projectName.toLowerCase() + "/" + projectName.toLowerCase() + "_dataset");
		try {
			metricCSV.open();
			for (List<ClassData> classes = measurmentIterator.next(); classes != null; classes = measurmentIterator.next()) {
				List<List<? extends CSVable>> data = new ArrayList<>();
				data.add(classes);
				metricCSV.saveToCSV(data);
			}
			metricCSV.close();
			
		} catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | GitAPIException | InterruptedException e) {
			this.logger.log(Level.WARNING, "[{0}] Error while analyzing {0} classes: {1}", new Object[] {this.projectName, e.getMessage()});
			Thread.currentThread().interrupt();
			return;
		} catch (IOException | CSVIncorrectNumValues e)  {
			this.logger.log(Level.WARNING, "[{0}] Error while saving classes: {1}", new Object[] {this.projectName, e.getMessage()});
			return;
		} 
		
		
		logger.log(Level.INFO, csvLogTemplate, new Object[] {this.projectName, csvSaved});
	}
	
	protected void analyzeTickets() {
		for (IssueType issueType : this.issueTypes) {
			logger.log(Level.INFO, "[{1}] Looking for issues of type {0} for the project {1}", new Object[] {issueType, projectName});
			Map<String, Ticket> tickets;
			
			try {
				tickets = projectAnalyzer.analyzeTickets(issueType);
			} catch (IOException | GitAPIException | JSONException e) {
				this.logger.log(Level.WARNING, "[{0}] Error while looking for issues of type {1}: {2}", new Object[] {this.projectName, issueType, e.getMessage()});
				return;
			}
				
			if (tickets == null) {
				logger.log(Level.SEVERE, "[{0}] Error while looking for {0} releases: ProjectAnalyzer.analyzeTickets() returned a null value", this.projectName);
				return;
			}
				
			logger.log(Level.INFO, csvLogTemplate, new Object[] {this.projectName, savingCSV});
				
			try {
				CSVDAO releasesCSV = new CSVDAO(baseDir + projectName.toLowerCase() + "/" + projectName.toLowerCase() + "_" + issueType.toString().toLowerCase());

				releasesCSV.open();
				List<List<? extends CSVable>> data = new ArrayList<>();
				data.add(new ArrayList<>(tickets.values()));
				releasesCSV.saveToCSV(data);
				releasesCSV.close();
					
			} catch (IOException | CSVIncorrectNumValues e) {
				this.logger.log(Level.WARNING, "[{0}] Error while saving tickets: {1}", new Object[] {this.projectName, e.getMessage()});
				return;

			}
			logger.log(Level.INFO, csvLogTemplate, new Object[] {this.projectName, csvSaved});
		}
	}
	

	@Override
	public void run() {
		try {
			projectAnalyzer.init(this.gitReleaseRegex);
		} catch (IOException | GitAPIException e) {
			this.logger.log(Level.WARNING, "[{0}] Error while initializing analysis: {1}", new Object[] {this.projectName, e.getMessage()});

		}
		
		for (WorkerTask workerTask : workerTasks) {
			try {
				Method task = this.getClass().getDeclaredMethod(workerTask.getMethod());
				task.invoke(this, (Object[]) null);			
				
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.log(Level.SEVERE, "[{0}] Error while executing {1} task: {2}", new Object[] {this.projectName, workerTask.getMethod(), e.getMessage()});

			}
		}
		
		logger.log(Level.INFO, "[{0}] Analysis completed!", this.projectName);
		
		
	}

}

package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.eclipse.jgit.api.errors.GitAPIException;

import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.MetricType;

public class ProjectAnalyzer {
	private GitAPI git;
	private JiraAPI jira;
	private String projectName;
	private String baseDir;
	private String gitReleaseRegex;
	private List<Release> releases;
	
	
	public ProjectAnalyzer(String projectName, String baseDir) {
		this.projectName = projectName;
		if (!baseDir.substring(baseDir.length() - 1).contains("/")) {
			this.baseDir = baseDir + "/";
		} else {
			this.baseDir = baseDir;
		}
	}
	
	public void init(String gitReleaseRegex) throws IOException, GitAPIException {
		// Check if the directory output exists
		File outputDirectory = new File(this.baseDir);
	    if (!outputDirectory.exists()){
	        outputDirectory.mkdir();
	    }
	    
	    // Check if the project directory exist
	    File projectDirectory = new File(this.baseDir + this.projectName.toLowerCase());
	    if (!projectDirectory.exists()) {
	    	projectDirectory.mkdir();
	    }

		this.git = new GitAPI(this.projectName, this.baseDir);
		this.git.init();
		this.jira = new JiraAPI(this.projectName);
		this.releases = null;
		this.gitReleaseRegex = gitReleaseRegex;
	}
	
	public void init() throws IOException, GitAPIException {
		this.init("%s");
	}

	
	public Map<String,Ticket> analyzeTickets(IssueType issueType) throws IOException, GitAPIException {
		Map<String,Ticket> tickets = null;
		tickets = this.jira.retriveTickets(issueType);
		List<CommitInfo> commits = this.git.getCommits();

		this.findFixedDate(tickets, commits);
		
		return tickets;
	}
	
	private void findFixedDate(Map<String,Ticket> tickets, List<CommitInfo> commits) {
		for (CommitInfo commit : commits) {
			List<String> ticketIds = commit.findTicketIds(this.projectName.toUpperCase() + "-");
			
			for (String ticketId: ticketIds) { 
				Ticket ticket = tickets.get(ticketId);
				if (ticket != null && (ticket.getResolvedDate() == null || ticket.getResolvedDate().compareTo(commit.getDate()) < 0)) {
					ticket.setResolvedDate(commit.getDate());
				}
			}
		}
	}
	
	public List<Release> getReleases() throws IOException, ParseException, GitAPIException{
		if (this.releases == null) {
			this.retiveReleases();
		}	
		return this.releases;
	
	}	
	
	public MeasurmentIterator analyzeClasses(List<MetricType> metrics) throws IOException, ParseException, GitAPIException {
		if (this.releases == null) {
			this.retiveReleases();
		}	
		
		return new MeasurmentIterator(this.releases.iterator(), metrics ,this.git);
	}
	
	private void retiveReleases() throws IOException, ParseException, GitAPIException {
		this.releases = new ArrayList<>();
		SortedSet<ReleaseJira> releasesJira = this.jira.retriveReleases();
		Map<String, ReleaseGit> releasesGit = this.git.getReleases();
		Integer sequentialId = 1;
		for (ReleaseJira releaseJira : releasesJira) {			
			ReleaseGit releaseGit = releasesGit.get(String.format(this.gitReleaseRegex,releaseJira.getName()));
			if (releaseGit != null) {
				releases.add(new Release(sequentialId, releaseJira.getReleaseDate(), releaseJira.getName(), releaseGit.getName(), releaseGit.getReleaseId()));
				sequentialId++;
			}
		}
	}

}

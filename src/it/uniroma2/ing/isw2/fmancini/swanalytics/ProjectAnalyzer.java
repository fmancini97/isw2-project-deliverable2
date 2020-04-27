package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.json.JSONException;

import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.MetricType;

public class ProjectAnalyzer {
	private GitAPI git;
	private JiraAPI jira;
	private String projectName;
	
	
	public ProjectAnalyzer(String projectName) {
		this.projectName = projectName;
	}
	
	public void init() throws IOException, GitAPIException {
		this.git = new GitAPI(this.projectName);
		this.git.init();
		this.jira = new JiraAPI(this.projectName);
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
	
	public void listReleasesDiff(Release start, Release end) {
		List<DiffData> diffs;
		try {
			diffs = this.git.diff(start.getReleaseId(), end.getReleaseId());
			for (DiffData diff : diffs) {
				System.out.println("OldPath: " + diff.getOldPath());
				System.out.println("NewPath: " + diff.getNewPath());
				System.out.println("ChangeType: " + diff.getChangeType().toString());
				System.out.println("AddedLines: " + diff.getAddedLines());
				System.out.println("DeletedLines: " + diff.getDeletedLines());
				System.out.println();
			}
		} catch (GitAPIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public List<Release> getReleases(String gitReleasePath) throws JSONException, IOException, ParseException, GitAPIException{
		List<Release> releases = new ArrayList<>();
		TreeSet<ReleaseJira> releasesJira = this.jira.retriveReleases();
		HashMap<String, ReleaseGit> releasesGit = this.git.getReleases();
		Integer sequentialId = 1;
		for (ReleaseJira releaseJira : releasesJira) {
			ReleaseGit releaseGit = releasesGit.get(String.format(gitReleasePath,releaseJira.getName()));
			if (releaseGit != null) {
				releases.add(new Release(sequentialId, releaseJira.getReleaseDate(), releaseJira.getName(), releaseGit.getName(), releaseGit.getReleaseId()));
				sequentialId++;
			}
		}	
		return releases;
	
	}	
	
	public MeasurmentIterator analyzeClasses(List<MetricType> metrics) throws JSONException, IOException, ParseException, GitAPIException {
		return new MeasurmentIterator(this.getReleases().iterator(), metrics ,this.git);
	}
	
	public List<Release> getReleases() throws JSONException, IOException, ParseException, GitAPIException {
		return this.getReleases("%s");
	}

}

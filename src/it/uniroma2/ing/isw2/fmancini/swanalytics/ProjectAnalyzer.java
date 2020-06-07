package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;

import it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis.Bug;
import it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis.InjectedVersionCalculator;
import it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis.MeasurmentIterator;
import it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis.Release;
import it.uniroma2.ing.isw2.fmancini.swanalytics.git.CommitInfo;
import it.uniroma2.ing.isw2.fmancini.swanalytics.git.DiffData;
import it.uniroma2.ing.isw2.fmancini.swanalytics.git.GitAPI;
import it.uniroma2.ing.isw2.fmancini.swanalytics.git.ReleaseGit;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.IssueType;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.JiraAPI;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.JiraBug;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.ReleaseJira;
import it.uniroma2.ing.isw2.fmancini.swanalytics.jira.Ticket;
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
	
	
	public Map<String,TreeSet<Integer>> getBuggyClasses() throws IOException, ParseException, GitAPIException {
		
		SortedSet<Bug> sortedBugs = this.getBugs();
		
		
		return this.findAffectedClasses(sortedBugs);
	}
	
	private SortedSet<Bug> getBugs() throws IOException, ParseException, GitAPIException {
		
		HashMap<String, Bug> indexeedBugs = new HashMap<>();
		TreeSet<Bug> sortedBugs = new TreeSet<>((o1,o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
		List<CommitInfo> commits = this.git.getCommits();
		
		
		// Find opening version, fixed version and classes
		for (CommitInfo commit: commits) {
			sortedBugs.addAll(this.findBugFixs(commit, indexeedBugs));
		}
		return sortedBugs;
	}
	
	private SortedSet<Bug> findBugFixs(CommitInfo commit, HashMap<String, Bug> indexeedBugs) throws IOException, ParseException, GitAPIException {
		Map<String, JiraBug> jiraBugs = this.jira.getBugs();
		List<String> bugNames = commit.findTicketIds(this.projectName + "-");
		TreeSet<Bug> sortedBugs = new TreeSet<>((o1,o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
		for (String bugName : bugNames) {
			
			JiraBug jiraBug = jiraBugs.get(bugName);
			if (jiraBug != null) {
				
				List<String> classes = this.getAffectedClasses(commit.getParentId(), commit.getId());
				Bug bug = indexeedBugs.get(bugName);
				if (bug == null) {
					Release openingVersion = this.findRelease(jiraBug.getCreated());
					Release fixedVersion = this.findRelease(commit.getDate());
					if (openingVersion == null || fixedVersion == null || openingVersion.getId() > fixedVersion.getId()) continue;
					bug = new Bug(bugName, jiraBug.getCreated(),openingVersion.getId(), fixedVersion.getId());
					indexeedBugs.put(bugName, bug);
					sortedBugs.add(bug);
				}
				bug.appendAffectedClasses(classes);
			}
		}
		
		
		return sortedBugs;
	}
	
	private List<String> getAffectedClasses(ObjectId start, ObjectId end) throws GitAPIException, IOException {
		List<DiffData> diffDatas = this.git.diff(start, end);

		List<String> classes = new ArrayList<>();
		for (DiffData diffData: diffDatas) {
			if (diffData.getNewPath().contains(".java")) {
				classes.add(diffData.getNewPath());
			}
		}
		return classes;
	}
	
	private Map<String,TreeSet<Integer>> findAffectedClasses(SortedSet<Bug> bugs) throws IOException, ParseException {
		Map<String, JiraBug> jiraBugs = this.jira.getBugs();

		
		// Find Affected Version
		InjectedVersionCalculator  affectedVersionCalculator = new InjectedVersionCalculator(bugs.size());
		Map<String, Release> releasesMap = new HashMap<>();
		for(Release release: this.releases) {
			releasesMap.put(release.getJiraName(), release);
		}

		HashMap<String, TreeSet<Integer>> classBugginess = new HashMap<>();
		for (Bug bug: bugs) {
			List<String> affectedVersions = jiraBugs.get(bug.getName()).getVersions();
			Release injectedVersion = this.findInjectededVerison(affectedVersions, releasesMap);
					
			if (injectedVersion == null || injectedVersion.getId() > bug.getOpeningVersion()) {
				bug.setInjectedVersion(affectedVersionCalculator.computeAffectedVersion(bug.getOpeningVersion(), bug.getFixedVersion()));
			} else {
				bug.setInjectedVersion(injectedVersion.getId());
				affectedVersionCalculator.updateProportionValue(injectedVersion.getId(), bug.getOpeningVersion(), bug.getFixedVersion());
			}		
			List<Integer> affectedVersionss = IntStream.rangeClosed(bug.getInjectedVersion(), bug.getFixedVersion()).boxed().collect(Collectors.toList());
			for (String affectedClass: bug.getAffectedClasses()) {
				TreeSet<Integer> classAffectedVefrisons = null;
				if (classBugginess.containsKey(affectedClass)) {
					classAffectedVefrisons = classBugginess.get(affectedClass);
				} else {
					classAffectedVefrisons = new TreeSet<>();
					classBugginess.put(affectedClass, classAffectedVefrisons);
				}
				classAffectedVefrisons.addAll(affectedVersionss);
			}
		}
		
		return classBugginess;
	}
	
	private Release findInjectededVerison(List<String> affectedVersions, Map<String, Release> releases) {
		Release injectedVersion = null;
		for (String affectedVersion: affectedVersions) {
			Release probableInjectedVersion = releases.get(affectedVersion);
			injectedVersion = (probableInjectedVersion != null 
				&& (injectedVersion == null || probableInjectedVersion.getReleaseDate().before(injectedVersion.getReleaseDate()))) 
				? probableInjectedVersion : injectedVersion;
		}
		return injectedVersion;
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
	
	public MeasurmentIterator analyzeClasses(List<MetricType> metrics, Integer releasePercentage) throws IOException, ParseException, GitAPIException {
		if (this.releases == null) {
			this.retiveReleases();
		}	
		Map<String, TreeSet<Integer>> buggyClasses = this.getBuggyClasses();
		
		List<Release> analyzedReleases = new ArrayList<>();
		if (releasePercentage >= 100 || releasePercentage <= 0) {
			analyzedReleases.addAll(this.releases);
		} else {
			Integer numReleases = (int) (this.releases.size() * (releasePercentage/100.0));
			analyzedReleases.addAll(this.releases.subList(0, numReleases));
			
		}
		
		return new MeasurmentIterator(analyzedReleases.iterator(), metrics, buggyClasses, this.git);
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
	
	
	private Release findRelease(Date date) throws IOException, ParseException, GitAPIException {
		if (this.releases == null) {
			this.retiveReleases();
		}	
		if (date == null || date.compareTo(this.releases.get(this.releases.size() - 1).getReleaseDate()) > 0) {
			return null;
		}
		Integer length = this.releases.size();
		Integer minorIndex = 0;
		Integer majorIndex = length;
		while (true) {
			Integer index = (minorIndex + majorIndex) / 2;
			Release lowerRelease = this.releases.get(index);
			if (index + 1 < length) {
				Release upperRelease = this.releases.get(index + 1);
				
				if (date.compareTo(lowerRelease.getReleaseDate()) > 0 && date.compareTo(upperRelease.getReleaseDate()) <= 0) {
					return upperRelease;
				} else if (date.compareTo(upperRelease.getReleaseDate()) > 0) {
					minorIndex = index + 1;
				} else if (index == 0) {
					return lowerRelease;
				} else {
					majorIndex = index;
				}
			} else if (date.compareTo(lowerRelease.getReleaseDate()) >= 0) {
				return lowerRelease;
			} else {
				majorIndex = index;
			}
		}
	}

}

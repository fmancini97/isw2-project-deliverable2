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

/**
 * Performs multiple analyses on the classes of an Apache project 
 * @author fmancini
 *
 */
public class ProjectAnalyzer {
	private GitAPI git;
	private JiraAPI jira;
	private String projectName;
	private String baseDir;
	private String gitReleaseRegex;
	private List<Release> releases;
	
	/**
	 * 
	 * @param projectName: name of the project
	 * @param baseDir: folder where to save the metadata associated with the project
	 */
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

	/**
	 * Analyze the Jira tickets associated with the project
	 * @param issueType: type of ticket issue
	 * @return
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public Map<String,Ticket> analyzeTickets(IssueType issueType) throws IOException, GitAPIException {
		Map<String,Ticket> tickets = null;
		tickets = this.jira.retriveTickets(issueType);
		List<CommitInfo> commits = this.git.getCommits();

		this.findFixedDate(tickets, commits);		
		return tickets;
	}
	
	/**
	 * Search for defective classes and versions in which they were defective
	 * @return Association between classes and versions in which they are defective
	 * @throws IOException
	 * @throws ParseException
	 * @throws GitAPIException
	 */
	public Map<String,TreeSet<Integer>> getBuggyClasses() throws IOException, ParseException, GitAPIException {
		
		SortedSet<Bug> sortedBugs = this.getBugs();
		
		
		return this.findAffectedClasses(sortedBugs);
	}
	
	/**
	 * Search all bugs within the project
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws GitAPIException
	 */
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
	
	/**
	 * Search for defective classes within a commit using the jira ticket identification code in the comment.
	 * Index bugs based on ticket identifier and search for opening version and fixed version of the ticket
	 * @param commit
	 * @param indexeedBugs
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws GitAPIException
	 */
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
	
	/**
	 * Search for classes that have changed between two revisions.
	 * This method is used to find defective classes
	 * @param start
	 * @param end
	 * @return
	 * @throws GitAPIException
	 * @throws IOException
	 */
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
	
	/**
	 * Builds a map where it associates to each class the versions in which it was defective
	 * @param bugs
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private Map<String,TreeSet<Integer>> findAffectedClasses(SortedSet<Bug> bugs) throws IOException, ParseException {
		Map<String, JiraBug> jiraBugs = this.jira.getBugs();

		
		// Find Affected Version using Moving Window technique
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
			List<Integer> affectedVersionss = IntStream.range(bug.getInjectedVersion(), bug.getFixedVersion()).boxed().collect(Collectors.toList());
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
	
	/**
	 * Search for the injected version in a list of versions.
	 * This method is used when tickets provide affected versions
	 * @param affectedVersions
	 * @param releases
	 * @return
	 */
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
	
	/**
	 * Search for Jira ticket fixed dates within commit comments
	 * @param tickets
	 * @param commits
	 */
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
	
	/**
	 * Search the releases of the project.
	 * If the search has already been done, the method returns the cached versions immediately
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws GitAPIException
	 */
	public List<Release> getReleases() throws IOException, ParseException, GitAPIException{
		if (this.releases == null) {
			this.retiveReleases();
		}	
		return this.releases;
	
	}	
	
	/**
	 * Measure classes and analyze their defectiveness
	 * @param metrics: list of metrics to use for measurements
	 * @param releasePercentage: percentage of versions to consider in measurements (snoring)
	 * @return iterator that allows you to analyze classes one version at a time
	 * @throws IOException
	 * @throws ParseException
	 * @throws GitAPIException
	 */
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
	
	/**
	 * Search for project versions
	 * @throws IOException
	 * @throws ParseException
	 * @throws GitAPIException
	 */
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
	
	/**
	 * Search for the version associated with a date.
	 * If it exists, the returned version has as released date a date immediately following the date in input
	 * @param date
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws GitAPIException
	 */
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

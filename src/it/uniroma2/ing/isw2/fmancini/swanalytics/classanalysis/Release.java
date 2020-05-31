package it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVable;

public class Release implements CSVable {
	private Integer id;
	private Date releaseDate;
	private String jiraName;
	private String gitName;
	private ObjectId releaseId;
	
	public Release(Integer id, Date releaseDate, String jiraName, String gitName, ObjectId releaseId) {
		super();
		this.id = id;
		this.releaseDate = releaseDate;
		this.jiraName = jiraName;
		this.gitName = gitName;
		this.releaseId = releaseId;
	}
	
	public String getHeader() {
		return "id,releaseDate,jiraName,gitName,commitSha";
	}

	public Integer getId() {
		return id;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public String getJiraName() {
		return jiraName;
	}

	public String getGitName() {
		return gitName;
	}

	public ObjectId getReleaseId() {
		return releaseId;
	}

	public String getReleaseSha() {
		return this.releaseId.getName();
	}
	
	public String toCSV() {
		return id.toString() + "," + new SimpleDateFormat("yyyy-MM-dd").format(this.releaseDate) + "," + this.jiraName + "," + this.gitName + "," + this.getReleaseSha();
	}
}

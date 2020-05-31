package it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bug {
	private String name;
	private Date createdAt;
	private Integer openingVersion;
	private Integer affectedVersion;
	private Integer fixedVersion;
	private List<String> affectedClasses;
	
	public Bug(String name, Date createdAt,Integer openingVersion, Integer fixedVersion) {
		this.name = name;
		this.createdAt = createdAt;
		this.openingVersion = openingVersion;
		this.fixedVersion = fixedVersion;
		this.affectedClasses = new ArrayList<>();
	}
	
	public Bug(String name, Date createdAt,Integer openingVersion, Integer fixedVersion, Integer affectedVersions,
			List<String> affectedClasses) {
		this(name, createdAt, openingVersion, fixedVersion);
		this.affectedVersion = affectedVersions;
		this.affectedClasses = affectedClasses;
	}

	public String getName() {
		return name;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}

	public Integer getOpeningVersion() {
		return openingVersion;
	}

	public Integer getAffectedVersion() {
		return affectedVersion;
	}

	public Integer getFixedVersion() {
		return fixedVersion;
	}

	public List<String> getAffectedClasses() {
		return affectedClasses;
	}
	
	
	public void setAffectedVersion(Integer affectedVersions) {
		this.affectedVersion = affectedVersions;
	}

	public void appendAffectedClasses(List<String> classes) {
		this.affectedClasses.addAll(classes);
	}
	
}

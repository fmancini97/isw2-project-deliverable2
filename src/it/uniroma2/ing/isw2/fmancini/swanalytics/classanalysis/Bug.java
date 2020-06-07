package it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bug {
	private String name;
	private Date createdAt;
	private Integer injectedVersion;
	private Integer openingVersion;
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
		this.injectedVersion = affectedVersions;
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

	public Integer getInjectedVersion() {
		return injectedVersion;
	}

	public Integer getFixedVersion() {
		return fixedVersion;
	}

	public List<String> getAffectedClasses() {
		return affectedClasses;
	}
	
	
	public void setInjectedVersion(Integer affectedVersions) {
		this.injectedVersion = affectedVersions;
	}

	public void appendAffectedClasses(List<String> classes) {
		this.affectedClasses.addAll(classes);
	}
	
}

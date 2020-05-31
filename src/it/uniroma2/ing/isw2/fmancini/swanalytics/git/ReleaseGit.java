package it.uniroma2.ing.isw2.fmancini.swanalytics.git;

import org.eclipse.jgit.lib.ObjectId;

public class ReleaseGit {
	private String name;
	private ObjectId releaseId;
	
	public ReleaseGit(String name, ObjectId releaseId) {
		super();
		this.name = name;
		this.releaseId =releaseId;
	}

	public String getName() {
		return name;
	}

	public String getSha() {
		return this.releaseId.getName();
	}

	public ObjectId getReleaseId() {
		return releaseId;
	}	
	
	
}

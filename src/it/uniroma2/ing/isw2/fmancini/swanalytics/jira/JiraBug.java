package it.uniroma2.ing.isw2.fmancini.swanalytics.jira;

import java.util.Date;
import java.util.List;

/**
 * Contains data related to a bug fix ticket
 * @author fmancini
 *
 */
public class JiraBug {
	private String name;
	private Date created;
	private List<String> fixVersions;
	private List<String> versions; // Array delle versioni affette
	
	public JiraBug(String name, Date created, List<String> fixVersions, List<String> versions) {
		super();
		this.name = name;
		this.created = created;
		this.fixVersions = fixVersions;
		this.versions = versions;
	}

	public String getName() {
		return name;
	}

	public Date getCreated() {
		return created;
	}

	public List<String> getFixVersions() {
		return fixVersions;
	}

	public List<String> getVersions() {
		return versions;
	}
	
	
}

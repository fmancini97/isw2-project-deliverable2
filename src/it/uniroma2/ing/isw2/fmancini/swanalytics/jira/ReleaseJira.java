package it.uniroma2.ing.isw2.fmancini.swanalytics.jira;

import java.util.Date;

/**
 * Contains data for a version of the project
 * @author fmancini
 *
 */
public class ReleaseJira {
	private Integer id;
	private String name;
	private Date releaseDate;
	
	public ReleaseJira(Integer id, String name, Date releaseDate) {
		this.id = id;
		this.name = name;
		this.releaseDate = releaseDate;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}
	
	
	
}

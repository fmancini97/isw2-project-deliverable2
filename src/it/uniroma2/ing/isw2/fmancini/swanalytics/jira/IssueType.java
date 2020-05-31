package it.uniroma2.ing.isw2.fmancini.swanalytics.jira;

public enum IssueType {
	TICKET("in%20(standardIssueTypes()%2C%20subTaskIssueTypes())"),
	BUGS("%3D%20Bug"),
	NEWFEATURES("%3D\"New%20Feature\"");
	
	private final String restType;
	private IssueType (final String restType) {
		this.restType = restType;
	}
	public String getType() {
		return this.restType;
	}
}

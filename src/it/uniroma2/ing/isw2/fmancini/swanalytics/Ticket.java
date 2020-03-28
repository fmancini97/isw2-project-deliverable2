/**
 * 
 */
package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.util.Date;

/**
 * @author fmancini
 *
 */
public class Ticket {
	 private String projectName;
	 private String ticketID;
	 private IssueType issueType;
	 private Date resolvedDate;
	 
	 public Ticket(String projectName, String ticketID, IssueType issueType) {
		 this.projectName = projectName;
		 this.ticketID = ticketID;
		 this.issueType = issueType;
	 }

	public Date getResolvedDate() {
		return resolvedDate;
	}

	public void setResolvedDate(Date resolvedDate) {
		this.resolvedDate = resolvedDate;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getTicketID() {
		return ticketID;
	}

	public IssueType getIssueType() {
		return issueType;
	}
}

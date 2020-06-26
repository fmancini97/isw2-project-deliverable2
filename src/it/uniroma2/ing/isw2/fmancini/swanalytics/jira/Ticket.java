/**
 * 
 */
package it.uniroma2.ing.isw2.fmancini.swanalytics.jira;

import java.text.SimpleDateFormat;
import java.util.Date;

import it.uniroma2.ing.isw2.fmancini.csv.CSVable;

/**
 * @author fmancini
 *
 */
public class Ticket implements CSVable{
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

	@Override
	public String toCSV() {
		
		
		String date = (this.resolvedDate != null) ? new SimpleDateFormat("yyyy-MM-dd").format(this.resolvedDate) : "";
		return this.ticketID + ";" + date;
	}

	@Override
	public String getHeader() {
		return "Ticket ID;Resolved Date";
	}
}

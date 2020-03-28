package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.util.Date;

public class CommitInfo {
	private String id;
	private Date date;
	private String message;
	
	public CommitInfo(String id, Date date, String message) {
		super();
		this.id = id;
		this.date = date;
		this.message = message;
	}
	
	public String getId() {
		return id;
	}
	public Date getDate() {
		return date;
	}
	public String getComment() {
		return message;
	}
	
	public String findTickeId(String ticketPath) {
		String ticketId = ticketPath;
		
		int index = message.indexOf(ticketPath);
		if (index == -1) {
			return "";
		} else {
			index+=ticketPath.length();
			while (index < message.length()) {
				String num = message.substring(index, index + 1);
				try {
			        Integer.parseInt(num);
			    } catch (NumberFormatException nfe) {
			        return ticketId;
			    }
				
				ticketId = ticketId + num;
				
				index++;
			}
			return ticketId;
		}
	}
	
	
}

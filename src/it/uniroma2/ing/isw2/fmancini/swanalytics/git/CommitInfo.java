package it.uniroma2.ing.isw2.fmancini.swanalytics.git;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

public class CommitInfo {
	private ObjectId id;
	private Date date;
	private String message;
	private ObjectId parentId;
	
	public CommitInfo(ObjectId id, Date date, String message, ObjectId parentId) {
		super();
		this.id = id;
		this.date = date;
		this.message = message;
		this.parentId = parentId;
	}
	
	public ObjectId getId() {
		return id;
	}
	public Date getDate() {
		return date;
	}
	public String getComment() {
		return message;
	}
	
	public ObjectId getParentId() {
		return parentId;
	}

	public List<String> findTicketIds(String ticketPath) {
		List<String> ticketIds= new ArrayList<>();
		
		
		
		int index = message.indexOf(ticketPath);
		while (index != -1) {
			StringBuilder ticketId = new StringBuilder(ticketPath);
			index+=ticketPath.length();
			while (index < message.length()) {
				String num = message.substring(index, index + 1);
				try {
			        Integer.parseInt(num);
			    } catch (NumberFormatException nfe) {
			    	break;
			    }
				
				ticketId.append(num);
				
				index++;
			}
			ticketIds.add(ticketId.toString());
			index = message.indexOf(ticketPath, index);
			
		}
		
		return ticketIds;
	}
	
	
}

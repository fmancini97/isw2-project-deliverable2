package it.uniroma2.ing.isw2.fmancini.swanalytics;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class SWAnalytics {

	public static void main(String[] args) {
		String projectName = "";
		IssueType issueType = null; 
		
		JSONParser jsonParser = new JSONParser();
		JyraAPI jyraAPI;
		
		
		
		try (FileReader reader = new FileReader("config.json")) {
	            //Read JSON file
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
	            
		 	projectName = (String) obj.get("project-name");
			 	
		 	issueType = IssueType.valueOf((String) obj.get("analysis-type"));
			 	
		 	if (issueType == null) {
		 		System.out.println("Error: Invalid Analysis type");
			 	return;
		 	}	
			 	
	          
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
	    } catch (ParseException e) {
	    	e.printStackTrace();
	    }
		
		jyraAPI = new JyraAPI(projectName);
		try {
			jyraAPI.retriveTickets(issueType);
		} catch (IOException e) {
			e.printStackTrace();
	    } 
		
		Map<String,Ticket> tickets = null;
		try {
			 tickets = jyraAPI.retriveTickets(issueType);
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		GitAPI gitAPI = new GitAPI(projectName);
		
		List<CommitInfo> commits = gitAPI.getCommits();
		
		findFixedDate(projectName, tickets, commits);
		
		
		try {
		FileWriter csvWriter = new FileWriter("output/" + projectName.toLowerCase() + ".csv");
		csvWriter.append("Ticket ID");
		csvWriter.append(",");
		csvWriter.append("Date");
		csvWriter.append("\n");
		
		 Iterator it = tickets.entrySet().iterator();
		 while (it.hasNext()) {
		        Map.Entry<String,Ticket> pair = (Map.Entry)it.next();
		        String key = pair.getKey();
		        Ticket ticket = pair.getValue();
		        
		        String date = (ticket.getResolvedDate() != null) ?  new SimpleDateFormat("yyyy-MM-dd").format(ticket.getResolvedDate()) : "";
				csvWriter.append(key + "," + date + "\n");
		 }
	
		
		csvWriter.flush();
		csvWriter.close();
		} catch(IOException e) {
			
			
		}
	}
	
	public static void findFixedDate(String projectName, Map<String,Ticket> tickets, List<CommitInfo> commits) {
		String ticketId;
		for (CommitInfo commit : commits) {
			ticketId = commit.findTickeId(projectName.toUpperCase() + "-");
			
			if (ticketId != "") {
				Ticket ticket = tickets.get(ticketId);
				if (ticket != null && (ticket.getResolvedDate() == null || ticket.getResolvedDate().compareTo(commit.getDate()) < 0)) {
					ticket.setResolvedDate(commit.getDate());
				}
			}
		}
	}
}



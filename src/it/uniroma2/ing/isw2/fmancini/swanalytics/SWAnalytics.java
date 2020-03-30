package it.uniroma2.ing.isw2.fmancini.swanalytics;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SWAnalytics {

	public static void main(String[] args) {
		
		Logger logger = Logger.getLogger(SWAnalytics.class.getName());
		String projectName = "";
		IssueType issueType = null; 
		
		JSONParser jsonParser = new JSONParser();
		JyraAPI jyraAPI;
		
		logger.info("Retrieving configuration");
		
		try (FileReader reader = new FileReader("config.json")) {
	            //Read JSON file
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
	            
		 	projectName = (String) obj.get("project-name");
		 	
		 	if (projectName == null || projectName.equals("")) {
		 		logger.log(Level.WARNING, "Error: Invalid project name"); 
			 	System.exit(1);
		 	}
			 	
		 	issueType = IssueType.valueOf((String) obj.get("analysis-type"));
			 	
		 	if (issueType == null) {
		 		logger.log(Level.WARNING, "Error: Invalid Analysis type. Available analysis types are: {0}", Arrays.toString(IssueType.values())); 
			 	System.exit(1);
		 	}	
			 	
	          
		} catch (IOException | ParseException e) {
			logger.log(Level.WARNING, "Error while retrieving configuration: {0}", e.getMessage());
			System.exit(1);
		}
		
		logger.log(Level.INFO, "Looking for issues of type {0} for the project {1}", new Object[] {issueType, projectName});
		
		jyraAPI = new JyraAPI(projectName);
		
		
		Map<String,Ticket> tickets = null;
		try {
			 tickets = jyraAPI.retriveTickets(issueType);
		} catch (JSONException | IOException e) {
			logger.log(Level.WARNING, "Error while retrieving tickets: {0}", e.getMessage());
			System.exit(1);
		}
		
		if (tickets == null) {
			logger.log(Level.SEVERE, "Error while retrieving tickets: null structure returnd by: JyraAPI.retriveTickets()");
			System.exit(1);
		}
		
		logger.log(Level.INFO, "Looking for commits of the project {0}", projectName);

		
		GitAPI gitAPI = new GitAPI(projectName);
		
		List<CommitInfo> commits = null;
		try {
			commits = gitAPI.getCommits();
		} catch (GitAPIException | IOException e) {
			logger.log(Level.WARNING, "Error while retrieving commits: {0}", e.getMessage());
			System.exit(1);
		}
		
		if (commits == null) {
			logger.log(Level.SEVERE, "Error while retrieving commits: null structure returnd by: GitAPI.getCommits()");
			System.exit(1);
		}
		
		logger.info("Looking for conclusion date of the tickets ");

		
		findFixedDate(projectName, tickets, commits);
		
		logger.info("Generating CSV file");

		try (FileWriter csvWriter = new FileWriter("output/" + projectName.toLowerCase() + ".csv")) {
		csvWriter.append("Ticket ID");
		csvWriter.append(",");
		csvWriter.append("Date");
		csvWriter.append("\n");
		
		 Iterator<Map.Entry<String,Ticket>> it = tickets.entrySet().iterator();
		 while (it.hasNext()) {
		        Map.Entry<String,Ticket> pair = it.next();
		        String key = pair.getKey();
		        Ticket ticket = pair.getValue();
		        
		        String date = (ticket.getResolvedDate() != null) ?  new SimpleDateFormat("yyyy-MM-dd").format(ticket.getResolvedDate()) : "";
				csvWriter.append(key + "," + date + "\n");
		 }
	
		
		csvWriter.flush();
		} catch(IOException e) {
			logger.log(Level.WARNING, "Error while writing CSV file: {0}", e.getMessage());
			System.exit(1);
			
		}
		
		logger.info("CSV file generated successfully");
	}
	
	public static void findFixedDate(String projectName, Map<String,Ticket> tickets, List<CommitInfo> commits) {
		String ticketId;
		for (CommitInfo commit : commits) {
			ticketId = commit.findTickeId(projectName.toUpperCase() + "-");
			
			if (!ticketId.equals("")) {
				Ticket ticket = tickets.get(ticketId);
				if (ticket != null && (ticket.getResolvedDate() == null || ticket.getResolvedDate().compareTo(commit.getDate()) < 0)) {
					ticket.setResolvedDate(commit.getDate());
				}
			}
		}
	}
}



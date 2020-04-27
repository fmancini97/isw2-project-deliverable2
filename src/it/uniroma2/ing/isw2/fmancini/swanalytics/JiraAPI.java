/**
 * 
 */
package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author fmancini
 *
 */
public class JiraAPI {
	private static String basicUrl = "https://issues.apache.org/jira/rest/api/2/";
	private String projectName;
	
	
	public JiraAPI(String projectName) {
		this.projectName = projectName;
	}
	
	
	public Map<String,Ticket> retriveTickets(IssueType issueType) throws IOException {
		Map<String,Ticket> tickets = new HashMap<>();
		
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
	      //Get JSON API for closed bugs w/ AV in the project
		do {
			//Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + 1000;
	        String url = basicUrl + "search?jql=project%20%3D%20"
	               + this.projectName + "%20AND%20issueType%20" + issueType.getType() + "%20AND(%20status%20=%20closed%20OR"
	               + "%20status%20=%20resolved%20)AND%20resolution%20=%20fixed%20&fields=key,resolutiondate,versions,created&startAt="
	               + i.toString() + "&maxResults=" + j.toString();
	        JSONObject json = JSONTools.readJsonFromUrl(url);
	        JSONArray issues = json.getJSONArray("issues");
	        total = json.getInt("total");
	        for (; i < total && i < j; i++) {
	           //Iterate through each bug
	       	 
	        	JSONObject issue = issues.getJSONObject(i%1000);
	        	tickets.put(issue.get("key").toString(), new Ticket(this.projectName, issue.get("key").toString(), issueType));
	        	
	        }  
	        
		} while (i < total);
		
		return tickets;
	      
	}
	
	public TreeSet<ReleaseJira> retriveReleases() throws IOException, JSONException, ParseException{
		
		String url = basicUrl + "project/" + this.projectName + "/version";
		TreeSet<ReleaseJira> releases = new TreeSet<>(new Comparator<ReleaseJira>(){
            //@Override
            public int compare(ReleaseJira o1, ReleaseJira o2) {
                return o1.getReleaseDate().compareTo(o2.getReleaseDate());
            }
         }); 
		
		
		JSONObject json = JSONTools.readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("values");
        
        for (Integer i = 0; i < versions.length(); i++ ) {
            JSONObject version = versions.getJSONObject(i);
        	
            if (version.has("releaseDate") && version.has("name") && version.has("id")) {
            	releases.add(new ReleaseJira(version.getInt("id"), version.getString("name"), new SimpleDateFormat("yyyy-MM-dd").parse(version.getString("releaseDate"))));	
            }
        }
        return releases;
	}
		
		
		
	
	
	
	

}

/**
 * 
 */
package it.uniroma2.ing.isw2.fmancini.swanalytics.jira;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

import it.uniroma2.ing.isw2.fmancini.swanalytics.JSONTools;

/**
 * @author fmancini
 *
 */
public class JiraAPI {
	private static String basicUrl = "https://issues.apache.org/jira/rest/api/2/";
	private String projectName;
	private Map<String, JiraBug> bugs;
	
	
	public JiraAPI(String projectName) {
		this.projectName = projectName;
		this.bugs = null;
	}
	
	
	public Map<String,Ticket> retriveTickets(IssueType issueType) throws IOException {
		Map<String,Ticket> tickets = new HashMap<>();
		
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
	      //Get JSON API for closed bugs w/ AV in the project
		do {
			//https://issues.apache.org/jira/rest/api/2/search?jql=project%20%3D%20OPENJPA%20AND%20issueType%20%3D%20Bug%20AND(%20status%20=%20closed%20OR%20status%20=%20resolved%20)AND%20resolution%20=%20fixed%20&fields=key,resolutiondate,versions,created&startAt=0&maxResults=1000
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
	
	public SortedSet<ReleaseJira> retriveReleases() throws IOException, ParseException{
		
		String url = basicUrl + "project/" + this.projectName + "/version";
		TreeSet<ReleaseJira> releases = new TreeSet<>((o1,o2) -> o1.getReleaseDate().compareTo(o2.getReleaseDate()));
		
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
	
	public Map<String, JiraBug> getBugs() throws IOException, ParseException {
		if (this.bugs != null) {
			return new HashMap<>(this.bugs);
		}
		
		this.bugs = new HashMap<>();
		
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
	      //Get JSON API for closed bugs w/ AV in the project
		do {
			//Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + 1000;
	        String url = basicUrl + "search?jql=project%20%3D%20"
	               + this.projectName + "%20AND%20issueType%20" + IssueType.BUGS.getType() + "%20AND(%20status%20=%20closed%20OR"
	               + "%20status%20=%20resolved%20)AND%20resolution%20=%20fixed%20&fields=key,fixVersions,versions,created&startAt="
	               + i.toString() + "&maxResults=" + j.toString();
	        JSONObject json = JSONTools.readJsonFromUrl(url);
	        JSONArray issues = json.getJSONArray("issues");
	        total = json.getInt("total");
	        for (; i < total && i < j; i++) {
	           //Iterate through each bug
	       	 	
	        	JSONObject issue = issues.getJSONObject(i%1000);
	        	String name = issue.getString("key");
	        	JSONObject fields = issue.getJSONObject("fields");
	        	Date created = new SimpleDateFormat("yyyy-MM-dd").parse(fields.getString("created"));
	        	List<String> fixVersions = this.parseVersions(fields.getJSONArray("fixVersions"));
	        	List<String> versions = this.parseVersions(fields.getJSONArray("versions"));
	        	
	        	this.bugs.put(name, new JiraBug(name, created, fixVersions, versions));
	        }  
	        
		} while (i < total);
		
		return new HashMap<>(this.bugs);
		
	}
	
	private List<String> parseVersions(JSONArray versions) {
		List<String> parsedVersions = new ArrayList<>();
		
		for (Integer i = 0; i < versions.length(); i++) {
			JSONObject version = versions.getJSONObject(i);
			String versionName = version.getString("name");
			parsedVersions.add(versionName);
		}
		
		return parsedVersions;
		
	}
		
		
	
	
	
	

}

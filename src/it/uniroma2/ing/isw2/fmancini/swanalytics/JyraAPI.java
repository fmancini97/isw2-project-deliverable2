/**
 * 
 */
package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author fmancini
 *
 */
public class JyraAPI {
	private static String basicUrl = "https://issues.apache.org/jira/rest/api/2/";
	private String projectName;
	
	
	public JyraAPI(String projectName) {
		this.projectName = projectName;
	}
	
	
	public Map<String,Ticket> retriveTickets(IssueType issueType) throws JSONException, IOException {
		Map<String,Ticket> tickets = new HashMap<String,Ticket>();
		
		Integer j = 0, i = 0, total = 1;
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
		
		
		
	
	
	
	

}

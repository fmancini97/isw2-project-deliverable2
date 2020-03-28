/**
 * 
 */
package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author fmancini
 *
 */
public class GitAPI {

	private static String gitUrl = "https://github.com/apache/";
	
	private String projectName;
	
	public GitAPI(String projectName) {
		this.projectName = projectName.toLowerCase();
	}
	
	List<CommitInfo> getCommits() {
		
		List<CommitInfo> commits = new ArrayList<CommitInfo>();
		Git git = null;
		try {
			
			if (!Files.exists(Paths.get("output/" + projectName))) {
				git = Git.cloneRepository()
						.setURI(gitUrl + projectName + ".git")
						.setDirectory(new File("output/" + projectName))
						.call();
			} else {
				git = Git.open(new File( "output/" + projectName + "/.git"));
			}
			
		} catch (GitAPIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Repository repository = git.getRepository();
        
        Iterable<RevCommit> commitsLog = null;
		try {
			commitsLog = git.log().call();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
       
        for (RevCommit commit : commitsLog) {
        	
        	commits.add(new CommitInfo(commit.getName(), new Date(commit.getCommitTime() * 1000L), commit.getFullMessage()));
        }
        
        
            
        return commits;
	}
	
	
}

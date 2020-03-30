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
import org.eclipse.jgit.revwalk.RevCommit;

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
	
	List<CommitInfo> getCommits() throws GitAPIException, IOException {
		
		List<CommitInfo> commits = new ArrayList<>();
		Git git = this.getRepository();
        
        Iterable<RevCommit> commitsLog = null;
		
		commitsLog = git.log().call();
		
		
       
        for (RevCommit commit : commitsLog) {
        	
        	commits.add(new CommitInfo(commit.getName(), new Date(commit.getCommitTime() * 1000L), commit.getFullMessage()));
        }
                    
        return commits;
	}
	
	private Git getRepository() throws IOException, GitAPIException {
		String projectDir = "output/" + projectName.toLowerCase();
		
		if (!Files.exists(Paths.get(projectDir))) {
			return Git.cloneRepository()
					.setURI(gitUrl + projectName + ".git")
					.setDirectory(new File(projectDir))
					.call();
		} else {
			try (Git git = Git.open(new File( projectDir + "/.git"))){
				git.pull().call();
				return git;
			}
		}	
	}
	
}

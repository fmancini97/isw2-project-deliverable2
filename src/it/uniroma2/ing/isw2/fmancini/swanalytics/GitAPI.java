/**
 * 
 */
package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

/**
 * @author fmancini
 *
 */
public class GitAPI {

	private static String gitUrl = "https://github.com/apache/";
	
	private String projectName;
	
	private Git git;
	
	public GitAPI(String projectName) {
		this.projectName = projectName.toLowerCase();
	}
	
	public void init() throws IOException, GitAPIException {
		this.git = this.getRepository();
	}
 	
	
	List<CommitInfo> getCommits() throws GitAPIException, IOException {
		
		List<CommitInfo> commits = new ArrayList<>();  
        Iterable<RevCommit> commitsLog = null;
		
		commitsLog = git.log().call();
		
       
        for (RevCommit commit : commitsLog) {
        	//System.out.println(commit.getName());
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
				git.checkout().setName("master").call();
				git.pull().call();
				
				return git;
			}
		}	
	}
	
	public HashMap<String, ReleaseGit> getReleases() throws GitAPIException {
		HashMap<String, ReleaseGit> releases = new HashMap<>();
		List<Ref> tags = git.tagList().call();
		
		RevWalk walk = new RevWalk(this.git.getRepository());
		for (Ref refTag : tags) {
			try {
				RevCommit commit = walk.parseCommit(refTag.getObjectId());
				RevTag revTag = walk.parseTag(refTag.getObjectId());
				releases.put(revTag.getTagName(), new ReleaseGit(revTag.getTagName(), commit.getId()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		walk.close();
		
		return releases;
	}
	
	public List<DiffData> diff(ObjectId startCommit, ObjectId endCommit) throws MissingObjectException, IncorrectObjectTypeException, GitAPIException, IOException {
		
		DiffCommand command = git.diff()
				.setNewTree(this.retriveTreeParser(endCommit))
				.setShowNameAndStatusOnly(true);
		
		command = (startCommit != null) ? command.setOldTree(this.retriveTreeParser(startCommit)) : command;
		
		List<DiffEntry> diffEntries = command.call();
		List<DiffData> diff = new ArrayList<>();
		for (DiffEntry diffEntry : diffEntries) {
			diff.add(this.parseDiffEntry(diffEntry));
		}
		return diff;
	}
	
	public List<DiffData> diff(ObjectId endCommit) throws MissingObjectException, IncorrectObjectTypeException, GitAPIException, IOException {
		return this.diff(null, endCommit);
	}
	
	public List<RevCommit> listCommits(ObjectId startRelease, ObjectId endRelease) throws NoHeadException, MissingObjectException, IncorrectObjectTypeException, GitAPIException {
		LogCommand logCommand = this.git.log();
		logCommand = (startRelease != null) ? logCommand.addRange(startRelease, endRelease) : logCommand.add(endRelease);
		
		Iterable<RevCommit> commitPath = logCommand.call();
		List<RevCommit> commits = new ArrayList<>();
		for (RevCommit commit : commitPath) {
			commits.add(0, commit);
		}
		return commits;
	}
	
	public List<RevCommit> listCommits(ObjectId endRelease) throws NoHeadException, MissingObjectException, IncorrectObjectTypeException, GitAPIException {
		return this.listCommits(null, endRelease);
	}

	
	public void printDiff(DiffEntry diff) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		
		DiffFormatter formatter = new DiffFormatter(byteStream);
        formatter.setRepository(this.git.getRepository());
        formatter.format(diff);
        
        System.out.println(byteStream.toString());
	}
	
	private CanonicalTreeParser retriveTreeParser(ObjectId releaseId) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		RevWalk walk = new RevWalk(this.git.getRepository());
		RevCommit commit = walk.parseCommit(releaseId);
		ObjectId treeId = commit.getTree().getId();
		ObjectReader reader = git.getRepository().newObjectReader();
		return new CanonicalTreeParser( null, reader, treeId);
	}
	
	public List<String> listFiles(String commitHash) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException {
		git.checkout().setName(commitHash).call();
		return this.lsFiles("output/" + this.projectName + "/");
		
	}
	
	private List<String> lsFiles(String startDir) {
        List<String> fileNames = new ArrayList<>();
		File dir = new File(startDir);
        File[] files = dir.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                // Check if the file is a directory
                if (file.isDirectory()) {
                    // We will not print the directory name, just use it as a new
                    // starting point to list files from
                	
                	List<String> relativeFiles = this.lsFiles(file.getAbsolutePath());
                    for (String relativeFile : relativeFiles) {
                    	if (relativeFile.indexOf(".java") != -1) 
                    		fileNames.add(file.getName() + "/" + relativeFile);
                    }
                } else {
                    // We can use .length() to get the file size
                	if (file.getName().indexOf(".java") != -1) 
                		fileNames.add(file.getName());
                }
            }
        }
        return fileNames;
    }
	
	
	private DiffData parseDiffEntry(DiffEntry diff) throws IOException {
		
		String newPath = diff.getNewPath();
		String oldPath = diff.getOldPath();
		ChangeType changeType = diff.getChangeType();
		
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		
		DiffFormatter formatter = new DiffFormatter(byteStream);
        formatter.setRepository(this.git.getRepository());
        formatter.format(diff);
        String log = byteStream.toString();
        
        Integer addedLines = 0;
        Integer deletedLines = 0;
        Integer index;
        
        // Find '@@' in log status 
        while (log.length() >= 2 && !log.substring(0,2).equals("@@")) {
        	index = log.indexOf('\n');
        	if (index == -1) {
        		log = "";
        	} else {
        		log = log.substring(index + 1);
        	}
        }
        
        index = log.indexOf('\n');
    	if (index == -1) {
    		log = "";
    	} else {
    		log = log.substring(index + 1);
    	}
        
    	while (log.length() >= 1) {
    		switch(log.charAt(0)) {
    			case '+':
    				addedLines++;
    				break;
    		 	case '-':
    		 		deletedLines++;
    		 		break;
    		 	default:
    		 		
    		 }
    		 index = log.indexOf('\n');
         	 if (index == -1) {
         		 log = "";
         	 } else {
         		 log = log.substring(index + 1);
         	 } 	 
    	}
    	
    	
        return new DiffData(oldPath, newPath, changeType, addedLines, deletedLines);   
	}
}

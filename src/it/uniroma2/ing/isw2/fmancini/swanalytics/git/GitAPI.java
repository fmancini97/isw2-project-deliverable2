/**
 * 
 */
package it.uniroma2.ing.isw2.fmancini.swanalytics.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis.Release;

/**
 * @author fmancini
 *
 */
public class GitAPI {

	private static String gitUrl = "https://github.com/apache/";
	
	private String projectName;
	
	private Git git;
	
	private String repoDir;
	
	private String baseDir;
	
	private List<CommitInfo> commits;
	
	
	public GitAPI(String projectName, String baseDir) {
		this.projectName = projectName.toLowerCase();
		if (!baseDir.substring(baseDir.length() - 1).contains("/")) {
			this.baseDir = baseDir + "/";
		} else {
			this.baseDir = baseDir;
		}
	}
	
	public void init() throws IOException, GitAPIException {
		this.repoDir = this.baseDir + projectName.toLowerCase() + "/" + projectName + "_repo";
		
		if (!Files.exists(Paths.get(repoDir))) {
			this.git = Git.cloneRepository()
					.setURI(gitUrl + projectName + ".git")
					.setDirectory(new File(repoDir))
					.call();
		} else {
			try (Git gitRepo = Git.open(new File( repoDir + "/.git"))){
				gitRepo.checkout().setName("master").call();
				gitRepo.pull().call();
				
				this.git = gitRepo;
			}
		}
	}
	
	public String getRepoDir() {
		return repoDir;
	}

	public List<CommitInfo> getCommits() throws GitAPIException {
		if (this.commits != null) {
			return this.commits;
		}
		
		this.commits = new ArrayList<>();  
        Iterable<RevCommit> commitsLog = null;
        
		this.git.checkout().setName("master").call();
		commitsLog = git.log().call();
       
        for (RevCommit commit : commitsLog) {
        	ObjectId parentId = (commit.getParentCount() != 0) ? commit.getParent(0).getId() : null;
        	this.commits.add(new CommitInfo(commit.getId(), new Date(commit.getCommitTime() * 1000L), commit.getFullMessage(), parentId));
        }
                    
        return this.commits;
	}
	
	public Map<String, ReleaseGit> getReleases() throws GitAPIException, IOException {
		HashMap<String, ReleaseGit> releases = new HashMap<>();
		List<Ref> tags = git.tagList().call();
		
		RevWalk walk = new RevWalk(this.git.getRepository());
		for (Ref refTag : tags) {
				String releaseName = refTag.getName().substring("refs/tags/".length());
				RevCommit commit = walk.parseCommit(refTag.getObjectId());
				commit.getParent(0);
				releases.put(releaseName, new ReleaseGit(releaseName, commit.getId()));
		}
			
		walk.close();
		
		return releases;
	}
	
	public List<DiffData> diff(ObjectId startCommit, ObjectId endCommit) throws GitAPIException, IOException {
		
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
	
	public List<DiffData> diff(ObjectId endCommit) throws GitAPIException, IOException {
		return this.diff(null, endCommit);
	}
	
	public List<RevCommit> listCommits(ObjectId startRelease, ObjectId endRelease) throws MissingObjectException, IncorrectObjectTypeException, GitAPIException {
		LogCommand logCommand = this.git.log();
		logCommand = (startRelease != null) ? logCommand.addRange(startRelease, endRelease) : logCommand.add(endRelease);
		
		Iterable<RevCommit> commitPath = logCommand.call();
		List<RevCommit> releaseCommits = new ArrayList<>();
		for (RevCommit commit : commitPath) {
			releaseCommits.add(0, commit);
		}
		return releaseCommits;
	}
	
	public List<RevCommit> listCommits(ObjectId endRelease) throws MissingObjectException, IncorrectObjectTypeException, GitAPIException {
		return this.listCommits(null, endRelease);
	}
	
	private CanonicalTreeParser retriveTreeParser(ObjectId releaseId) throws IOException {
		RevWalk walk = new RevWalk(this.git.getRepository());
		RevCommit commit = walk.parseCommit(releaseId);
		ObjectId treeId = commit.getTree().getId();
		ObjectReader reader = git.getRepository().newObjectReader();
		walk.close();
		return new CanonicalTreeParser( null, reader, treeId);
	}
	
	public List<String> listFiles(String identifier) throws IOException {
		List<String> files = new ArrayList<>();
		
		try (RevWalk revWalk = new RevWalk(this.git.getRepository()); TreeWalk treeWalk = new TreeWalk(this.git.getRepository())) {
			ObjectId commitId = ObjectId.fromString(identifier);
			RevCommit commit = revWalk.parseCommit(commitId);
			ObjectId treeId = commit.getTree();

			treeWalk.reset(treeId);
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				String path = treeWalk.getPathString();
			    if (path.contains(".java")) {
			    	files.add(path);
			    }
			    
			}
			revWalk.dispose();
		}
		return files;	
	}
	
	public InputStream getFile(Release release, String filePath) throws IOException {
		try (RevWalk revWalk = new RevWalk(this.git.getRepository()); TreeWalk treeWalk = new TreeWalk(this.git.getRepository())) {
			ObjectId commitId = ObjectId.fromString(release.getReleaseSha());

            RevCommit commit = revWalk.parseCommit(commitId);
            // and using commit's tree find the path
            RevTree tree = commit.getTree();

            // now try to find a specific file
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filePath));
            if (!treeWalk.next()) {
            	throw new IllegalStateException("Did not find expected file " + filePath);
            }

            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = this.git.getRepository().open(objectId);

            revWalk.dispose();
            return loader.openStream();
        }
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
    	formatter.close();
    	
        return new DiffData(oldPath, newPath, changeType, addedLines, deletedLines);   
	}
}

package it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;


import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.RevisionMetric;
import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.RevisionMetricFactory;
import it.uniroma2.ing.isw2.fmancini.swanalytics.git.DiffData;
import it.uniroma2.ing.isw2.fmancini.swanalytics.git.GitAPI;
import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.MetricType;

public class MeasurmentIterator {
	private Iterator<Release> releases;
	private Release previousRelease;
	private Release actualRelease;
	private Map<String,TreeSet<Integer>> buggyClasses;
	private GitAPI git;
	private List<MetricType> metricTypes;
	private TreeMap<String,ClassData> temporaryClasses;
	private TreeMap<String, ClassData> actualReleaseClasses;

	

	public MeasurmentIterator(Iterator<Release> releases, List<MetricType> metricTypes, Map<String,TreeSet<Integer>> buggyClasses, GitAPI git) {
		this.releases = releases;
		this.previousRelease = null;
		this.actualRelease = null;
		this.metricTypes = metricTypes;
		this.buggyClasses = buggyClasses;
		this.git = git;
		this.temporaryClasses = null;
		this.actualReleaseClasses = new TreeMap<>();
	}
	
	public List<ClassData> next() throws GitAPIException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InterruptedException{
		List<ClassData> classDatas = null;
		
		if (!releases.hasNext()) {
			return classDatas;
		}
		
		this.previousRelease = this.actualRelease;
		this.actualRelease = releases.next();
		
		this.updateActualReleaseClasses();
		this.updateTemporaryClasses();
			
		ObjectId previousReleaseId = (this.previousRelease != null) ? this.previousRelease.getReleaseId() : null;
		List<RevCommit> commits = this.git.listCommits(previousReleaseId, this.actualRelease.getReleaseId());
		
		for (RevCommit commit : commits) {
			List<DiffData> diffs = (commit.getParentCount() != 0) ? this.git.diff(commit.getParent(0).getId(), commit.getId()) : this.git.diff(commit.getId());
			
			for (DiffData diff : diffs) {
				this.processDiff(commit, diff);	
			}
		}
		
		this.analyzeFiles();
		
		classDatas = new ArrayList<>(this.actualReleaseClasses.values());
		return classDatas;	
	}
	
	private void analyzeFiles() throws IOException, InterruptedException {
		List<FileAnalysisThread> fileAnalysisThreads = new ArrayList<>();
		
		for (ClassData classData : this.actualReleaseClasses.values()) {
			if (this.buggyClasses.containsKey(classData.getName())) {
				TreeSet<Integer> affectedVersions = this.buggyClasses.get(classData.getName());
				if (affectedVersions.contains(this.actualRelease.getId())) 
					classData.setBugginess(true);
			}
						
			FileAnalysisThread fileAnalysisThread = new FileAnalysisThread(classData, this.git.getFile(this.actualRelease, classData.getName()));
			fileAnalysisThread.start();
			fileAnalysisThreads.add(fileAnalysisThread);
		}
		
		for (FileAnalysisThread fileAnalysisThread : fileAnalysisThreads ) {
			fileAnalysisThread.join();
			if (fileAnalysisThread.getError() != null) {
				throw fileAnalysisThread.getError();
			}
		}
	}
	
	private void updateActualReleaseClasses() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		// Listing classes
		this.temporaryClasses = this.actualReleaseClasses;
		this.actualReleaseClasses = new TreeMap<>();
		List<String> classNames = this.git.listFiles(this.actualRelease.getReleaseSha());
			// Creating classData for the new actual version
			for (String className : classNames) {
				List<RevisionMetric> metrics = new ArrayList<>();
				for (MetricType metricType : this.metricTypes) {
					metrics.add(RevisionMetricFactory.getSingletonInstance().createMetric(metricType));
				}
				// Search if the class already exists in the previous version
				if (this.temporaryClasses.containsKey(className)) {
				// The class has not been moved
					this.temporaryClasses.remove(className);	
				} 
				this.actualReleaseClasses.put(className, new ClassData(className, this.actualRelease, metrics));
			}
	}
	
	private void updateTemporaryClasses() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// Keep track of the previous version classes which are not in the new releases: 
				// maybe they could be only moved in a revision of the new release
		for (ClassData previousVersionClass : this.temporaryClasses.values()) {
			List<RevisionMetric> metrics = new ArrayList<>();
			for (MetricType metricType : this.metricTypes) {
				metrics.add(RevisionMetricFactory.getSingletonInstance().createMetric(metricType));
			}
			this.temporaryClasses.put(previousVersionClass.getName(), new ClassData(previousVersionClass.getName(), this.actualRelease, metrics));
		}
	}
	
	private void processDiff(RevCommit commit, DiffData diff) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!diff.getNewPath().contains(".java") && !diff.getOldPath().contains(".java")) {
			// The file is not a java class
			return;
		}
		
		ClassData classData = null;
		switch (diff.getChangeType()) {
		case ADD:
			List<RevisionMetric> metrics = new ArrayList<>();
			for (MetricType metricType : this.metricTypes) {
				metrics.add(RevisionMetricFactory.getSingletonInstance().createMetric(metricType));
			}
			
			classData = new ClassData(diff.getNewPath(), this.actualRelease, metrics);
			classData.updateRevisionMeasurments(commit, diff);

			this.insertClassData(classData);

			break;
			
		case COPY:
			classData = this.findClass(diff.getOldPath());
			if (classData == null) {
				break;
			}
			classData = new ClassData(classData);
			classData.setName(diff.getNewPath());
			classData.updateRevisionMeasurments(commit, diff);
			this.insertClassData(classData);
			
			break;
		case MODIFY:
			classData = this.findClass(diff.getOldPath());
			if (classData == null) {
				break;
			}
			classData.updateRevisionMeasurments(commit, diff);
			break;
			
		case RENAME:
			
			classData = this.findClass(diff.getOldPath());
			if (classData == null) {
				break;
			}
			classData.setName(diff.getNewPath());
			classData.updateRevisionMeasurments(commit, diff);
			this.temporaryClasses.remove(diff.getOldPath());
			
			this.insertClassData(classData);
			break;
		case DELETE:
			this.temporaryClasses.remove(diff.getOldPath());
			break;
			
		default:
		}
	}
	
	private ClassData findClass(String className) {
		ClassData classData = null;
		if (this.actualReleaseClasses.containsKey(className)) {
			classData = this.actualReleaseClasses.get(className);
		} else if (this.temporaryClasses.containsKey(className)){
			classData = this.temporaryClasses.get(className);
		}
		
		return classData;
	
	}
	
	private void insertClassData(ClassData classData) {
		if (this.actualReleaseClasses.containsKey(classData.getName())) {
			this.actualReleaseClasses.put(classData.getName(), classData);
		} else {
			this.temporaryClasses.put(classData.getName(), classData);
		}
	}
	
	protected class FileAnalysisThread extends Thread{
		ClassData classData;
		InputStream fileData;
		IOException error;
		
		public FileAnalysisThread(ClassData classData, InputStream fileData) {
			this.classData = classData;
			this.fileData = fileData;
			this.error = null;
		}
		
		public IOException getError() {
			return this.error;
		}
		
		
		@Override
		public void run() {
			try {
				this.classData.computeFileMeasurment(this.fileData);
			} catch (IOException e) {
				this.error = e;
			}
			
		}
		
	}
	
}

package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;


import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.Metric;
import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.MetricFactory;
import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.MetricType;

public class MeasurmentIterator {
	private Iterator<Release> releases;
	private Release previousRelease;
	private Release actualRelease;
	private GitAPI git;
	private List<MetricType> metricTypes;
	private TreeMap<String,ClassData> temporaryClasses;
	private TreeMap<String, ClassData> actualReleaseClasses;

	

	public MeasurmentIterator(Iterator<Release> releases, List<MetricType> metricTypes,GitAPI git) {
		this.releases = releases;
		this.previousRelease = null;
		this.actualRelease = null;
		this.metricTypes = metricTypes;
		this.git = git;
		this.temporaryClasses = null;
		this.actualReleaseClasses = new TreeMap<>();
	}
	
	public List<ClassData> next() throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException, IOException{
		List<ClassData> classDatas = new ArrayList<>();
		
		if (!releases.hasNext()) {
			return classDatas;
		}
		
		this.previousRelease = this.actualRelease;
		this.actualRelease = releases.next();
		
		this.temporaryClasses = this.actualReleaseClasses;
		this.actualReleaseClasses = new TreeMap<>();
		
		// Listing classes
		List<String> classNames = this.git.listFiles(this.actualRelease.getReleaseSha());
		
		TreeMap<String,ClassData> previousReleaseClasses = this.actualReleaseClasses;
		this.actualReleaseClasses = new TreeMap<>();
		this.temporaryClasses = new TreeMap<>();
		
		// Creating classData for the new actual version
		for (String className : classNames) {
			List<Metric> metrics = new ArrayList<>();
			for (MetricType metricType : this.metricTypes) {
				metrics.add(MetricFactory.getSingletonInstance().createMetric(metricType));
			}
			
			
			// Search if the class already exists in the previous version
			if (previousReleaseClasses.containsKey(className)) {
				// The class has not been moved
				previousReleaseClasses.remove(className);	
			} 
			
			this.actualReleaseClasses.put(className, new ClassData(className, this.actualRelease, metrics));

		}
		
		// Keep track of the previous version classes which are not in the new releases: 
		// maybe they could be only moved in a revision of the new release
		for (ClassData previousVersionClass : previousReleaseClasses.values()) {
			List<Metric> metrics = new ArrayList<>();
			for (MetricType metricType : this.metricTypes) {
				metrics.add(MetricFactory.getSingletonInstance().createMetric(metricType));
			}
			this.temporaryClasses.put(previousVersionClass.getName(), new ClassData(previousVersionClass.getName(), this.actualRelease, metrics));
		}
		
		ObjectId previousReleaseId = (this.previousRelease != null) ? this.previousRelease.getReleaseId() : null;
		
		
		List<RevCommit> commits = this.git.listCommits(previousReleaseId, this.actualRelease.getReleaseId());
		
		ObjectId previousCommit = previousReleaseId;
		
		for (RevCommit commit : commits) {
			List<DiffData> diffs = this.git.diff(previousCommit, commit.getId()); 
			
			for (DiffData diff : diffs) {
				if (!diff.getNewPath().contains(".java") && !diff.getOldPath().contains(".java")) {
					// The file is not a java class
					continue;
				}
				
				
				switch (diff.getChangeType()) {
				case ADD:
					ClassData classData = null;
					if (this.actualReleaseClasses.containsKey(diff.getNewPath())) {
						classData = this.actualReleaseClasses.get(diff.getNewPath());
					} else {
						List<Metric> metrics = new ArrayList<>();
						for (MetricType metricType : this.metricTypes) {
							metrics.add(MetricFactory.getSingletonInstance().createMetric(metricType));
						}
						classData = new ClassData(diff.getNewPath(), this.actualRelease, metrics);
						this.temporaryClasses.put(diff.getNewPath(), classData);
					}
					
					classData.updateMeasurments(commit, diff);
					break;
					
				case COPY:
					if (this.actualReleaseClasses.containsKey(diff.getOldPath())) {
						classData = this.actualReleaseClasses.get(diff.getOldPath());
					} else if (this.temporaryClasses.containsKey(diff.getOldPath())) {
						classData = this.temporaryClasses.get(diff.getOldPath());
					} else {
						break;
					}
					
					classData = new ClassData(classData);
					classData.setName(diff.getNewPath());
					
					if (this.actualReleaseClasses.containsKey(diff.getNewPath())) {
						this.actualReleaseClasses.put(diff.getNewPath(), classData);
					} else {
						this.temporaryClasses.put(diff.getNewPath(), classData);
					}
					
					break;
				case MODIFY:
					if (this.actualReleaseClasses.containsKey(diff.getOldPath())) {
						classData = this.actualReleaseClasses.get(diff.getOldPath());
					} else if (this.temporaryClasses.containsKey(diff.getOldPath())) {
						classData = this.temporaryClasses.get(diff.getOldPath());
					} else {
						break;
					}
					
					classData.updateMeasurments(commit, diff);
					break;
					
				case RENAME:
					
					if (this.actualReleaseClasses.containsKey(diff.getOldPath())) {
						classData = this.actualReleaseClasses.get(diff.getOldPath());
					} else if (this.temporaryClasses.containsKey(diff.getOldPath())) {
						classData = this.temporaryClasses.get(diff.getOldPath());
						this.temporaryClasses.remove(diff.getOldPath());
					} else {
						break;
					}
					
					classData.setName(diff.getNewPath());
					
					if (this.actualReleaseClasses.containsKey(diff.getNewPath())) {
						this.actualReleaseClasses.put(diff.getNewPath(), classData);
					} else {
						this.temporaryClasses.put(diff.getNewPath(), classData);
					}
					
				default:
				}
				
			}
			previousCommit = commit;
		}
			
		classDatas.addAll(this.actualReleaseClasses.values());
		return classDatas;	
	}
}

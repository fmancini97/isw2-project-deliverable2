package it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVable;
import it.uniroma2.ing.isw2.fmancini.swanalytics.git.DiffData;
import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.LOC;
import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.RevisionMetric;

public class ClassData implements CSVable {
	private String name;
	private Release release;
	private List<RevisionMetric> revisionMetrics;
	private LOC size;
	private boolean isBuggy;
	
	
	public ClassData(String name, Release release, List<RevisionMetric> metrics) {
		super();
		this.name = name;
		this.release = release;
		this.revisionMetrics = metrics;
		this.size = new LOC();
		this.isBuggy = false;
	}
	
	public ClassData(ClassData source) {
		this.name = source.name;
		this.release = source.release;
		this.revisionMetrics = new ArrayList<>();
		for (RevisionMetric metric : source.revisionMetrics) {
			this.revisionMetrics.add(metric.duplicate());
		}
		this.size = new LOC(source.size);
	}
	
	public void updateRevisionMeasurments(RevCommit commit, DiffData diff) {
		for (RevisionMetric metric : revisionMetrics) {
			metric.updateMeasurment(commit, diff);
		}
	}
	
	public void computeFileMeasurment(InputStream file) throws IOException {
		this.size.measure(file);
		
	}

	public String getName() {
		return name;
	}
	

	public void setName(String name) {
		this.name = name;
	}

	public Release getRelease() {
		return release;
	}

	public List<RevisionMetric> getMetrics() {
		return revisionMetrics;
	}
	
	public void setBugginess(boolean isBuggy) {
		this.isBuggy = isBuggy;
	}

	@Override
	public String toCSV() {
		StringBuilder metricValues = new StringBuilder();
		
		for (RevisionMetric metric : this.revisionMetrics) {
			metricValues.append(';');
			metricValues.append(metric.toCSV());
		}
		String bugginess = (this.isBuggy) ? "Yes" : "No";
		return this.release.getId() + ";" + this.name + ";" + this.size.toCSV() + metricValues.toString() + ";" + bugginess; 
	}

	@Override
	public String getHeader() {
		StringBuilder metricNames = new StringBuilder();
		for (RevisionMetric metric : this.revisionMetrics) {
			metricNames.append(';');
			metricNames.append(metric.getHeader());
		}
		
		return "Version" + ";Name;" + this.size.getHeader() + metricNames.toString() + ";Buggy";
	}
}

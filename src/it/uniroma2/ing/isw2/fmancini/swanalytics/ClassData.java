package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.csv.CSVable;
import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.LOC;
import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.RevisionMetric;

public class ClassData implements CSVable {
	private String name;
	private Release release;
	private List<RevisionMetric> revisionMetrics;
	private LOC size;
	
	
	public ClassData(String name, Release release, List<RevisionMetric> metrics) {
		super();
		this.name = name;
		this.release = release;
		this.revisionMetrics = metrics;
		this.size = new LOC();
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
	
	public void computeFileMeasurment(String baseDir) throws IOException {
		if (!baseDir.substring(baseDir.length() - 1).equals("/")) {
			baseDir = baseDir + "/";
		}
		this.size.measure(baseDir + this.name);
		
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

	@Override
	public String toCSV() {
		StringBuilder metricValues = new StringBuilder();
		
		for (RevisionMetric metric : this.revisionMetrics) {
			metricValues.append(',');
			metricValues.append(metric.toCSV());
		}
		return this.release.toCSV() + "," + this.name + "," + this.size.toCSV() + metricValues.toString(); 
	}

	@Override
	public String getHeader() {
		StringBuilder metricNames = new StringBuilder();
		for (RevisionMetric metric : this.revisionMetrics) {
			metricNames.append(',');
			metricNames.append(metric.getHeader());
		}
		
		return this.release.getHeader() + ",Name," + this.size.getHeader() + metricNames.toString();
	}
}

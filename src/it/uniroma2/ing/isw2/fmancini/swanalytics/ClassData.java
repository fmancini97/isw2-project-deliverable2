package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.metrics.Metric;

public class ClassData implements CSVable {
	private String name;
	private Release release;
	private List<Metric> metrics;
	
	
	public ClassData(String name, Release release, List<Metric> metrics) {
		super();
		this.name = name;
		this.release = release;
		this.metrics = metrics;
	}
	
	public ClassData(ClassData source) {
		this.name = source.name;
		this.release = source.release;
		this.metrics = new ArrayList<>();
		for (Metric metric : source.metrics) {
			this.metrics.add(metric.duplicate());
		}
	}
	
	
	
	public void updateMeasurments(RevCommit commit, DiffData diff) {
		for (Metric metric : metrics) {
			metric.updateMeasurment(commit, diff);
		}
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

	public List<Metric> getMetrics() {
		return metrics;
	}

	@Override
	public String toCSV() {
		String metricValues = "";
		for (Metric metric : this.metrics) {
			metricValues = metricValues + metric.toCSV() + ",";
		}
		
		metricValues = metricValues.substring(0, metricValues.length() - 1);
		
		return this.release.toCSV() + "," + this.name + "," + metricValues; 
	}

	@Override
	public String getHeader() {
		String metricNames = "";
		for (Metric metric : this.metrics) {
			metricNames = metricNames + metric.getHeader() + ",";
		}
		
		metricNames = metricNames.substring(0, metricNames.length() - 1);
		
		return this.release.getHeader() + ",Name," + metricNames;
	}
}

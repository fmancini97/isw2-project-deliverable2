package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.CSVable;
import it.uniroma2.ing.isw2.fmancini.swanalytics.DiffData;

public abstract class Metric implements CSVable  {
	private Integer measurment;
	
	protected Metric () {
		this.measurment = 0;
	}
	
	protected Metric (Metric metric) {
		this.measurment = metric.measurment;
	}
	
	protected void setMeasurment(Integer measurment) {
		this.measurment = measurment;
	}
	
	public Integer getMeasurment() {
		return this.measurment;
	}
	
	public abstract void updateMeasurment(RevCommit commit, DiffData diff);
	public abstract Metric duplicate();
	
	@Override
	public String toCSV() {
		return String.valueOf(this.measurment);
	}
	
	@Override
	public String getHeader() {
		return this.getClass().getSimpleName();
	}
	
	
}

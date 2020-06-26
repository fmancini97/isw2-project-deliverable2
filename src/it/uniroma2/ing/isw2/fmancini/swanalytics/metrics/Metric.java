package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import it.uniroma2.ing.isw2.fmancini.csv.CSVable;

public abstract class Metric implements CSVable {

	private Integer measurment;
	
	protected Metric() {
		this.measurment = 0;
	}
	
	protected Metric(Metric source) {
		this.measurment = source.measurment;
	}
	
	protected void setMeasurment(Integer measurment) {
		this.measurment = measurment;
	}
	
	public Integer getMeasurment() {
		return this.measurment;
	}
	

	@Override
	public String toCSV() {
		return String.valueOf(this.measurment);
	}
	
	@Override
	public String getHeader() {
		return this.getClass().getSimpleName();
	}

}

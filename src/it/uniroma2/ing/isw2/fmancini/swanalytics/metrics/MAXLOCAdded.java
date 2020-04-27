package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.DiffData;

public class MAXLOCAdded extends Metric {

	public MAXLOCAdded() {
		super();
	}
	
	private MAXLOCAdded(MAXLOCAdded source) {
		super(source);
	}
	
	@Override
	public void updateMeasurment(RevCommit commit, DiffData diff) {
		Integer measurment = super.getMeasurment();
		super.setMeasurment((measurment >= diff.getAddedLines())? measurment : diff.getAddedLines());

	}

	@Override
	public Metric duplicate() {
		return new MAXLOCAdded(this);
	}
	
}

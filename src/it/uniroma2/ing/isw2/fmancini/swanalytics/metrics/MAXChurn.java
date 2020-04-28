package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.DiffData;

public class MAXChurn extends RevisionMetric {

	
	public MAXChurn() {
		super();
	}
	
	private MAXChurn(MAXChurn source) {
		super(source);
	}
	
	@Override
	public void updateMeasurment(RevCommit commit, DiffData diff) {
		Integer measurment = super.getMeasurment();
		super.setMeasurment((measurment >= diff.getAddedLines() - diff.getDeletedLines()) ? measurment : diff.getAddedLines() - diff.getDeletedLines());
	}

	@Override
	public RevisionMetric duplicate() {
		return new MAXChurn(this);
	}


}

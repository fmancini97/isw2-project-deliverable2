package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.DiffData;

public class Churn extends Metric {
	
	public Churn() {
		super();
	}
	
	private Churn (Churn source) {
		super(source);
	}
	
	
	@Override
	public void updateMeasurment(RevCommit commit, DiffData diff) {
		super.setMeasurment(super.getMeasurment() + diff.getAddedLines() - diff.getDeletedLines());
	}


	@Override
	public Metric duplicate() {
		return new Churn(this);
	}
	


}

package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.DiffData;

public class NumRevisions extends Metric {

	public NumRevisions() {
		super();
	}
	
	private NumRevisions(NumRevisions source) {
		super(source);
	}

	@Override
	public void updateMeasurment(RevCommit commit, DiffData diff) {
		super.setMeasurment(super.getMeasurment() + 1);
		
	}

	@Override
	public Metric duplicate() {
		return new NumRevisions(this);
	}
	
}

package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.DiffData;

public class LOCAdded extends Metric {

	
	public LOCAdded() {
		super();
	}
	
	private LOCAdded(LOCAdded source) {
		super(source);
	}
	
	@Override
	public void updateMeasurment(RevCommit commit, DiffData diff) {
		super.setMeasurment(super.getMeasurment() + diff.getAddedLines());
	}

	@Override
	public Metric duplicate() {
		return new LOCAdded(this);
	}

}

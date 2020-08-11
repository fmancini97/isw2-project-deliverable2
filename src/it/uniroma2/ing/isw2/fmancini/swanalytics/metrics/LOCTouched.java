package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.git.DiffData;

/**
 * Measures the number of touched code lines of a class
 * @author fmancini
 *
 */
public class LOCTouched extends RevisionMetric {

	public LOCTouched() {
		super();
	}
	
	private LOCTouched(LOCTouched source) {
		super(source);
	}
	
	
	@Override
	public void updateMeasurment(RevCommit commit, DiffData diff) {
		super.setMeasurment(super.getMeasurment() + diff.getAddedLines() + diff.getDeletedLines());

	}

	@Override
	public RevisionMetric duplicate() {
		return new LOCTouched(this);
	}

}

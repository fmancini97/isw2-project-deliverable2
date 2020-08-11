package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.git.DiffData;

/**
 * Measures the number of revisions of a class
 * @author fmancini
 *
 */
public class NumRevisions extends RevisionMetric {

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
	public RevisionMetric duplicate() {
		return new NumRevisions(this);
	}
	
}

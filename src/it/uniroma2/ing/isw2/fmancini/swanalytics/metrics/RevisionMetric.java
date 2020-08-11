package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.git.DiffData;

/**
 * Abstract class of a metric that measures classes by their revisions
 * @author fmancini
 *
 */
public abstract class RevisionMetric extends Metric  {
	
	protected RevisionMetric () {
		super();
	}
	
	protected RevisionMetric (RevisionMetric metric) {
		super(metric);
	}
	
	public abstract void updateMeasurment(RevCommit commit, DiffData diff);
	public abstract RevisionMetric duplicate();
	
}

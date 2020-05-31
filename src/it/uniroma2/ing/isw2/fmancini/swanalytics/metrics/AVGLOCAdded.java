package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.git.DiffData;

public class AVGLOCAdded extends RevisionMetric{
	
	private NumRevisions numRevisions;
	private LOCAdded locAdded;
	
	public AVGLOCAdded() {
		super();
		this.numRevisions = new NumRevisions();
		this.locAdded = new LOCAdded();
	}
	
	private AVGLOCAdded(AVGLOCAdded source) {
		super(source);
		this.numRevisions = (NumRevisions) source.numRevisions.duplicate();
		this.locAdded = (LOCAdded) source.locAdded.duplicate();
	}

	@Override
	public void updateMeasurment(RevCommit commit, DiffData diff) {
		this.numRevisions.updateMeasurment(commit, diff);
		this.locAdded.updateMeasurment(commit, diff);
		super.setMeasurment(this.locAdded.getMeasurment()/this.numRevisions.getMeasurment());
	}

	@Override
	public RevisionMetric duplicate() {
		return new AVGLOCAdded(this);
	}
	
	
	
	

}

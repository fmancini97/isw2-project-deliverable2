package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.DiffData;

public class AVGChurn extends Metric {
	
	private NumRevisions numRevisions;
	private Churn churn;
	
	public AVGChurn() {
	
		this.numRevisions = new NumRevisions();
		this.churn = new Churn();
	}
	
	private AVGChurn(AVGChurn source) {
		super(source);
		this.numRevisions = (NumRevisions) source.numRevisions.duplicate();
		this.churn = (Churn) source.churn.duplicate();
	}

	@Override
	public void updateMeasurment(RevCommit commit, DiffData diff) {
		this.numRevisions.updateMeasurment(commit, diff);
		this.churn.updateMeasurment(commit, diff);
		super.setMeasurment(Math.round(this.churn.getMeasurment()/this.numRevisions.getMeasurment()));
	}

	@Override
	public Metric duplicate() {
		return new AVGChurn(this);
	}
	
	
}

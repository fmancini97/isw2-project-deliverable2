package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import java.util.HashSet;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.ing.isw2.fmancini.swanalytics.git.DiffData;


public class NAuth extends RevisionMetric {
	HashSet<String> auths;
	
	public NAuth() {
		super();
		this.auths = new HashSet<>();
	}
	
	private NAuth(NAuth source) {
		super(source);
		this.auths = new HashSet<>(this.auths);
	}

	@Override
	public void updateMeasurment(RevCommit commit, DiffData diff) {
		String authMail = commit.getAuthorIdent().getEmailAddress();
		if (!this.auths.contains(authMail)) {
			super.setMeasurment(super.getMeasurment() + 1);
			auths.add(authMail);
		}
		
	}

	@Override
	public RevisionMetric duplicate() {
		return new NAuth(this);
	}	
	
}

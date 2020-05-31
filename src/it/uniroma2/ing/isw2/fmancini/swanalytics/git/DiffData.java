package it.uniroma2.ing.isw2.fmancini.swanalytics.git;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;

public class DiffData {
	private String oldPath;
	private String newPath;
	private ChangeType changeType;
	private Integer addedLines;
	private Integer deletedLines;
	public DiffData(String oldPath, String newPath, ChangeType changeType, Integer addedLines, Integer deletedLines) {
		super();
		this.oldPath = oldPath;
		this.newPath = newPath;
		this.changeType = changeType;
		this.addedLines = addedLines;
		this.deletedLines = deletedLines;
	}
	public String getOldPath() {
		return oldPath;
	}
	public String getNewPath() {
		return newPath;
	}
	public ChangeType getChangeType() {
		return changeType;
	}
	public Integer getAddedLines() {
		return addedLines;
	}
	public Integer getDeletedLines() {
		return deletedLines;
	}
	
	
	
}

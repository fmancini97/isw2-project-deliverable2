package it.uniroma2.ing.isw2.fmancini.swanalytics.csv;

public class CSVIncorrectNumValues extends Exception {
	public CSVIncorrectNumValues(Integer numValues, Integer actualNumValues) {
		super("Incorrect number of inserted values. Expected: " + numValues + " Actual: " + actualNumValues);
	}
}

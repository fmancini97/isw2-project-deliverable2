package it.uniroma2.ing.isw2.fmancini.csv;

public class CSVIncorrectNumValues extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CSVIncorrectNumValues(Integer numValues, Integer actualNumValues) {
		super("Incorrect number of inserted values. Expected: " + numValues + " Actual: " + actualNumValues);
	}
}

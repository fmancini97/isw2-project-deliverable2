package it.uniroma2.ing.isw2.fmancini.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * It takes care of saving classes that implement the CSVable interface in csv files
 * @author fmancini
 */
public class CSVDAO {
	private String csvName;
	private FileWriter csvWriter;
	private boolean hasHeader;	
	
	public CSVDAO(String csvName) {
		
		if (csvName.length() < 4 || csvName.substring(csvName.length() - 4).compareTo(".csv") != 0) {
			this.csvName = csvName + ".csv";
		} else {
			this.csvName = csvName;
		}
		
		this.hasHeader = false; 
		
	}
	
	public void open() throws IOException {
		this.csvWriter = new FileWriter(this.csvName);
	}
	
	public void close() throws IOException {
		this.csvWriter.close();
	}
	
	public void saveToCSV(List<List<? extends CSVable>> data) throws IOException, CSVIncorrectNumValues {
		Integer length=0;
		
		// Check iterator sizes
		for (List<? extends CSVable> value : data) {
			if (length == 0) {
				length = value.size();
			} else if (length != value.size()) {
				throw new CSVIncorrectNumValues(value.size(), length);
			}
		}
		
		if (!this.hasHeader) {
			this.appendHeader(data);
			this.hasHeader = true;
		}
		
		for (Integer i = 0; i < length; i++) {
			boolean isfirst = true;
			csvWriter.append('\n');
			for (List<? extends CSVable> value : data) {
				if (isfirst) {
					isfirst = false;
				}	else {
					csvWriter.append(',');
				}
				csvWriter.append(value.get(i).toCSV());	
			}
		}
		csvWriter.flush();
	}
	
	private void appendHeader(List<List<? extends CSVable>> data) throws IOException {
		boolean isfirst = true;
		for (List<? extends CSVable> value : data) {
			if (isfirst) {
				isfirst = false;
			}	else {
				csvWriter.append(',');
			}
			csvWriter.append(value.get(0).getHeader());	
		}
	}
}

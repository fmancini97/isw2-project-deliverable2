package it.uniroma2.ing.isw2.fmancini.swanalytics;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CSVDAO {
	private String csvName;
	private FileWriter csvWriter;
	private boolean hasHeader;
	private Integer length;
	
	
	public CSVDAO(String csvName) {
		
		if (csvName.length() < 4 || csvName.substring(csvName.length() - 4).compareTo(".csv") != 0) {
			this.csvName = csvName + ".csv";
		} else {
			this.csvName = csvName;
		}
		
		this.hasHeader = false; 
		this.length=0;
		
	}
	
	
	public void open() throws IOException {
		this.csvWriter = new FileWriter(this.csvName);
	}
	
	public void close() throws IOException {
		this.csvWriter.close();
	}
	
	public void saveToCSV(List<? extends CSVable>... data) throws IOException {
		
		// Check iterator sizes
		
		if (!this.hasHeader) {
			this.appendHeader(data);
			this.hasHeader = true;
		}
		
		boolean isfirst = true;
		for (Integer i = 0; i < this.length; i++) {
			isfirst = true;
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
	
	private void appendHeader(List<? extends CSVable>... data) throws IOException {
		boolean isfirst = true;
		for (List<? extends CSVable> value : data) {
			if (this.length == 0) {
				this.length = value.size();
			} else if (length != value.size()) {
				// TODO throware eccezione
				throw new Error("Lunghezze non vanno bene");
			}
			if (isfirst) {
				isfirst = false;
			}	else {
				csvWriter.append(',');
			}
			csvWriter.append(value.get(0).getHeader());	
		}
	}
}

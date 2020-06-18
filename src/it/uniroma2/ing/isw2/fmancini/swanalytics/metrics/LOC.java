package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class LOC extends Metric{
	
	public LOC () {
		super();
	}
	
	public LOC(LOC source) {
		super(source);
	}
	
	public void measure(InputStream file) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file))) {
			Stream<String> stream = br.lines();
			stream = stream.map(String::trim);
			
			for (Object currentLineObject: stream.toArray()) {
				String currentLine = (String) currentLineObject;
				if (!(currentLine.isEmpty() || currentLine.equals(" "))) {
		        	this.setMeasurment(this.getMeasurment()+ 1);
				}
            }
	    } 
	}

}

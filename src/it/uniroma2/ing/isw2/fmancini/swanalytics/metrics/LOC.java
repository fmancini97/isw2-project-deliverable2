package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LOC extends Metric{
	
	public LOC () {
		super();
	}
	
	public LOC(LOC source) {
		super(source);
	}
	
	public void measure(InputStream file) throws IOException {
		try (InputStream is = new BufferedInputStream(file)) {
	        byte[] c = new byte[1024];

	        int readChars = is.read(c);
	        if (readChars == -1) {
	            // bail out if nothing to read
	            return;
	        }

	        // make it easy for the optimizer to tune this loop
	        while (readChars == 1024) {
	            for (int i=0; i<1024; i++) {
	                if (c[i] == '\n') {
	                    super.setMeasurment(super.getMeasurment() + 1);
	                }
	            }
	            readChars = is.read(c);
	        }

	        // count remaining characters
	        while (readChars != -1) {
	            for (int i=0; i<readChars; ++i) {
	                if (c[i] == '\n') {
	                	super.setMeasurment(super.getMeasurment() + 1);
	                }
	            }
	            readChars = is.read(c);
	        }

	        super.setMeasurment((super.getMeasurment() == 0) ? 1 : super.getMeasurment());
	    } 
	}

}

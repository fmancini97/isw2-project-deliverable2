package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

/**
 * Factory that generates the classes to perform the measurements
 * @author fmancini
 *
 */
public class RevisionMetricFactory {
	
	
	private static class MetricContainer{ 
		public static final RevisionMetricFactory sigletonInstance = new RevisionMetricFactory();
	}
		 	
	protected RevisionMetricFactory() {
	}

	


	public static final RevisionMetricFactory getSingletonInstance() {		
		return MetricContainer.sigletonInstance;
	}
	
	
	public RevisionMetric createMetric(MetricType metric) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		RevisionMetric metricInstance = null; 
		Class<?> metricClass;
		
		
		metricClass = Class.forName(this.getClass().getPackage().getName() + "." + metric.getClassName());
		metricInstance = (RevisionMetric) metricClass.newInstance();
		
		return metricInstance;
		
	}
	
}

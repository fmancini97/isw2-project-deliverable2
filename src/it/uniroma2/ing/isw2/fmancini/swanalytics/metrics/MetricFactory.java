package it.uniroma2.ing.isw2.fmancini.swanalytics.metrics;

public class MetricFactory {
	
	
	private static class MetricContainer{ 
		public static final MetricFactory sigletonInstance = new MetricFactory();
	}
		 	
	protected MetricFactory() {
	}

	


	public static final MetricFactory getSingletonInstance() {		
		return MetricContainer.sigletonInstance;
	}
	
	
	public Metric createMetric(MetricType metric) {
		Metric metricInstance = null; 
		Class<?> metricClass;
		
		try {
			metricClass = Class.forName(this.getClass().getPackage().getName() + "." + metric.getClassName());
			metricInstance = (Metric) metricClass.newInstance();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return metricInstance;
		
	}
	
}

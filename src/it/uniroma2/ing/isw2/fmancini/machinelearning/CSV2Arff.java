package it.uniroma2.ing.isw2.fmancini.machinelearning;

//import required classes
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import java.io.File;

/**
 * Converts datasets from csv format to arff format
 * @author fmancini
 *
 */
public class CSV2Arff {
  
  public static void main(String[] args) throws Exception {
	  
	// List all projects available
	File projectsDir = new File("./output");
	File[] projects = projectsDir.listFiles();
	
	for (File project : projects) {
		File projectClasses = new File(project.getAbsoluteFile(), project.getName() + "_dataset.csv");
		if (projectClasses.exists()) {
			CSVLoader loader = new CSVLoader();
			loader.setFieldSeparator(";");
		    loader.setSource(projectClasses);
		    Instances data = loader.getDataSet();//get instances object

		    data.deleteAttributeAt(1);
		    
		    // save ARFF
		    ArffSaver saver = new ArffSaver();
		    saver.setInstances(data);//set the dataset we want to convert
		    //and save as ARFF
		    saver.setFile(new File(project.getAbsoluteFile(), project.getName() + "_dataset.arff"));
		    saver.writeBatch();
		}
	}  
    
  }
} 

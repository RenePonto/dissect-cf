package hu.mta.sztaki.lpds.cloud.simulator.iaas.vmconsolidation;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

	/**
	 * @author Rene Ponto
	 * 
	 * This class manages the consolidation algorithms, which means do several test cases with different
	 * values for the constants of the consolidators. The results are going to be saved inside a seperate
	 * file. There are also methods to set the needed values for consolidation of some algorithms.
	 */
@SuppressWarnings("serial")
public class ConsolidationController {
	
	Properties props;		// the properties-file, contains the constants of the pso-, abc- and ga-consolidator
	
	/**
	 * Sets all default values (which are the origin ones) and reads the properties-file.
	 * The file is saved in .xml in the root of the simulator.
	 * @throws IOException 
	 */
	public ConsolidationController() throws IOException {
		
		props = new Properties();
		File file = new File("consolidationProperties.xml");
		FileInputStream fileInput = new FileInputStream(file);
		props.loadFromXML(fileInput);
		fileInput.close();
		
		// the default values
		
		setPsoProperties("50", "50", "2", "2");
		setAbcProperties("10", "50", "5");
		setGaProperties("10", "50", "10");
	}
	
	/**
	 * We define some values for test cases, then doing those tests and save the results. In order to
	 * do that, the properties have to be changed.
	 */
	public void runTests() {
		this.initializeTest("Case 1");
		
		try {
			saveResults(1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.initializeTest("Case 2");
		
		try {
			saveResults(2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.initializeTest("Case 3");
		
		try {
			saveResults(3);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ...
	}
	
	/**
	 * Save results inside a csv file.
	 * @param i
	 * 			Needed for defining test cases.
	 * @throws FileNotFoundException
	 */
	private void saveResults(int i) throws FileNotFoundException {
		
		File file = new File("consolidationResults"+i+".csv");
		//TODO
	}
	
	/**
	 * Here we define which values are taken for the tests. 
	 */
	private void initializeTest(String string) {
		//example 1 : default values
		if(string == "Case 1") {
			
		}		
		//example 2 : doubled the population, halved the iterations of the algortihms
		if(string == "Case 2") {
			this.setPsoProperties("100", "25", "2", "2");
			this.setAbcProperties("20", "25", "5");
			this.setGaProperties("20", "25", "20");
		}
		
		//example 3 : halved the population, doubled the iterations
		if(string == "Case 3") {
			this.setPsoProperties("25", "100", "2", "2");
			this.setAbcProperties("5", "100", "5");
			this.setGaProperties("5", "100", "5");
		}		
	}
	
	/**
	 * Setter for the constant values of the pso algorithm.
	 * @param swarmSize
	 * 			This value defines the amount of particles.
	 * @param iterations
	 * 			This value defines the number of iterations.
	 * @param c1
	 * 			This value defines the first learning factor.
	 * @param c2
	 * 			This value defines the second learning factor.
	 */
	private void setPsoProperties(String swarmSize, String iterations, String c1, String c2) {
		props.setProperty("psoSwarmSize", swarmSize);
		props.setProperty("psoNrIterations", iterations);
		props.setProperty("psoC1", c1);
		props.setProperty("psoC2", c2);
	}
	
	/**
	 * Setter for the constant values of the abc algorithm.
	 * @param populationSize
	 * 			This value defines the amount of individuals in the population.
	 * @param iterations
	 * 			This value defines the number of iterations.
	 * @param limitTrials
	 * 			This value defines the maximum number of trials for improvement before a solution is abandoned.
	 */
	private void setAbcProperties(String populationSize, String iterations, String limitTrials) {
		props.setProperty("abcPopulationSize", populationSize);
		props.setProperty("abcNrIterations", iterations);
		props.setProperty("abcLimitTrials", limitTrials);
	}
	
	/**
	 * Setter for the constant values of the ga algorithm.
	 * @param populationSize
	 * 			This value defines the amount of individuals in the population.
	 * @param iterations
	 * 			This value defines the number of iterations.
	 * @param crossovers
	 * 			This value defines the number of recombinations to perform in each generation.
	 */
	private void setGaProperties(String populationSize, String iterations, String crossovers) {
		props.setProperty("gaPopulationSize", populationSize);
		props.setProperty("gaNrIterations", iterations);
		props.setProperty("gaNrCrossovers", crossovers);
	}

}
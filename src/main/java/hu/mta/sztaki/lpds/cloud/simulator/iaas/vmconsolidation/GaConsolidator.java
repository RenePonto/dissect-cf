package hu.mta.sztaki.lpds.cloud.simulator.iaas.vmconsolidation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;

/**
 * VM consolidator using a genetic algorithm.
 * 
 * This is a fairly basic GA. Ideas for further improvement:
 * - Use a local search procedure to improve individuals
 * - Improve performance by caching fitness values (or their components)
 * - Make fitness more sophisticated, e.g. by incorporating the skewness of 
 * PMs' load
 * - Make fitness more sophisticated, e.g. by allowing a large decrease in the 
 * number of migrations to compensate for a small increase in the number of 
 * active PMs.
 * - Every time and again, introduce new random individuals into the population 
 * to increase diversity
 * - Use more sophisticated termination criteria
 * 
 * @author Zoltan Mann
 */
public class GaConsolidator extends ModelBasedConsolidator {
	
	/** The properties file for setting the constant values */
	Properties props;
	/** For generating random numbers */
	private Random random;
	/** Number of individuals in the population */
	private int populationSize;
	/** Terminate the GA after this many generations */
	private int nrIterations;
	/** Number of recombinations to perform in each generation */
	private int nrCrossovers;


	/** Population for the GA, consisting of solutions=individuals */
	private Vector<Solution> population;

	/**
	 * Creates GaConsolidator with empty population.
	 */
	public GaConsolidator(IaaSService toConsolidate, double upperThreshold, double lowerThreshold, long consFreq) {
		super(toConsolidate, upperThreshold, lowerThreshold, consFreq);
		random=new Random();
		population=new Vector<>();
	}

	/**
	 * Initializes the population with populationSize random solutions.
	 */
	private void initializePopulation() {
		for(int i=0;i<populationSize;i++) {
			Solution s=new Solution(bins);
			s.fillRandomly();
			population.add(s);
		}
	}

	/**
	 * Take two random individuals of the population and let them recombinate
	 * to create a new individual. If the new individual is better than one of
	 * its parents, then it replaces that parent in the population, otherwise
	 * it is discarded.
	 */
	private void crossover() {
		int i1=random.nextInt(population.size());
		int i2=random.nextInt(population.size());
		Solution s1=population.get(i1);
		Solution s2=population.get(i2);
		Solution s3=s1.recombinate(s2);
		Fitness f1=s1.evaluate();
		Fitness f2=s2.evaluate();
		Fitness f3=s3.evaluate();
		if(f3.isBetterThan(f1))
			population.set(i1,s3);
		else if(f3.isBetterThan(f2))
			population.set(i2,s3);
	}

	/**
	 * At the end of the GA, update the model of the consolidator to reflect the
	 * mapping corresponding to the best found solution.
	 */
	private void implementBestSolution() {
		//Determine "best" solution (i.e. a solution, compared to which there is no better one)
		Solution bestSol=population.get(0);
		Fitness bestFitness=bestSol.evaluate();
		for(int i=1;i<populationSize;i++) {
			Fitness fitness=population.get(i).evaluate();
			if(fitness.isBetterThan(bestFitness)) {
				bestSol=population.get(i);
				bestFitness=fitness;
			}
		}
		//Implement solution in the model
		bestSol.implement();
		adaptPmStates();
	}
	
	/**
	 * Reads the properties file and sets the constant values for consolidation.
	 * @throws InvalidPropertiesFormatException
	 * @throws IOException
	 */
	private void setValues() throws InvalidPropertiesFormatException, IOException {
		
		props = new Properties();
		File file = new File("consolidationProperties.xml");
		FileInputStream fileInput = new FileInputStream(file);
		props.loadFromXML(fileInput);
		fileInput.close();
		
		this.populationSize = Integer.parseInt(props.getProperty("gaPopulationSize"));
		this.nrIterations = Integer.parseInt(props.getProperty("gaNrIterations"));
		this.nrCrossovers = Integer.parseInt(props.getProperty("gaNrCrossovers"));
	}

	/**
	 * Perform the genetic algorithm to optimize the mapping of VMs to PMs.
	 */
	@Override
	protected void optimize() {
		try {
			setValues();
		} catch (InvalidPropertiesFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initializePopulation();
		//Logger.getGlobal().info("Population after initialization: "+populationToString());
		for(int iter=0;iter<nrIterations;iter++) {
			//From each individual in the population, create an offspring using
			//mutation. If the child is better than its parent, it replaces it
			//in the population, otherwise it is discarded.
			for(int i=0;i<populationSize;i++) {
				Solution parent=population.get(i);
				Solution child=parent.mutate();
				if(child.evaluate().isBetterThan(parent.evaluate()))
					population.set(i,child);
			}
			//Perform the given number of crossovers.
			for(int i=0;i<nrCrossovers;i++) {
				crossover();
			}
			//Logger.getGlobal().info("Population after iteration "+iter+": "+populationToString());
		}
		implementBestSolution();
	}

	/**
	 * String representation of the whole population (for debugging purposes).
	 */
	public String populationToString() {
		String result="";
		boolean first=true;
		for(int i=0;i<populationSize;i++) {
			if(!first)
				result=result+" ";
			result=result+population.get(i).toString();
			first=false;
		}
		return result;
	}
}

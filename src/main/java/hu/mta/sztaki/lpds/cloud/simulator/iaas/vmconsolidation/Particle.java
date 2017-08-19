package hu.mta.sztaki.lpds.cloud.simulator.iaas.vmconsolidation;

import java.util.LinkedHashSet;
import java.util.Set;

/**
	 * @author Rene Ponto
	 *
	 * The idea for particles is to have a list of double values where all hostPMs in Order of their hosted VMs are.
	 * Therefore the id of the specific PM is used.
	 */

public class Particle {
	
	private double fitnessValue;	// used to evaluate the quality of the solution
	private ArithmeticVector velocity;		// the actual velocity : in which direction shall the solution go?
	private ArithmeticVector location;		// the actual location : possible solution
	
	private double personalBest;
	private ArithmeticVector personalBestLocation;
		
	/**
	 * Empty constructor becouse every value of a Particle is set using the setter-methods.
	 */
	public Particle() {
		personalBestLocation = new ArithmeticVector();
		personalBest = -1;
	}

	/**
	 * Compute the number of PMs that should be on, given our mapping. This
	 * is a component of the fitness.
	 */
	private int getNrActivePms() {		
		//clears the list so there is no pm more than once there
		ArithmeticVector count = location;
		Set<Double> setItems = new LinkedHashSet<Double>(location);
		count.clear();
		count.addAll(setItems);
		
		return count.size();
	}
	
	/**
	 * Used to get the actual fitnessValue of this particle
	 * @param location The actual result of this particle.
	 * @return new fitnessValue
	 */
	public double evaluateFitnessFunction() {
		double result = getNrActivePms();				
		return result;
	}
	
	public double getPBest() {
		return this.personalBest;
	}
	
	public void setPBest(double value) {
		this.personalBest = value;
	}
	
	public ArithmeticVector getPBestLocation() {
		return this.personalBestLocation;
	}
	
	public void setPBestLocation(ArithmeticVector loc) {		
		this.personalBestLocation = loc;
	}
	
	public ArithmeticVector getVelocity() {
		return velocity;
	}

	public void setVelocity(ArithmeticVector velocity) {
		this.velocity = velocity;
	}

	public ArithmeticVector getLocation() {
		return location;
	}

	public void setLocation(ArithmeticVector location) {
		this.location = location;
	}

	public double getFitnessValue() {
		fitnessValue = evaluateFitnessFunction();
		return fitnessValue;
	}
	
	public String toString() {
		String erg = "Location: " + this.getLocation() + System.getProperty("line.separator") 
			+ "Velocity: " + this.getVelocity() + System.getProperty("line.separator") 
			+ "FitnessValue: " + this.getFitnessValue() + ", PersonalBestFitness: " 
			+ this.getPBest() + System.getProperty("line.separator")
			+ "PersonalBestLocation: " + this.getPBestLocation() + System.getProperty("line.separator");
		
		return erg;
	}
}
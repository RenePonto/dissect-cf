package hu.mta.sztaki.lpds.cloud.simulator.iaas.vmconsolidation;

import java.util.ArrayList;

/**
	 * @author Rene Ponto
	 *
	 * This class is used to create an Object for the location and the velocity of
	 * particles of the PSO-Consolidator. Despite of that there a operations like add an ArithmeticVector, 
	 * subtract an ArithmeticVector and multiply it with a constant.
	 */
@SuppressWarnings("serial")
public class ArithmeticVector extends ArrayList<Double>{

	double highestID;
	
	/**
	 * Empty constructor, becouse all values of this class are created randomly at the beginning of the PSO
	 * and get added to this.
	 */
	public ArithmeticVector() {
		
	}
	
	/**
	 * The toString-method, used for debugging.
	 */
	public String toString() {
		String erg = "Size: " + this.size() + ", " + this.toList();
		return erg;
	}
	
	/**
	 * Creates an artificial list of the Value of this class, used inside the toString-method.
	 * @return
	 */
	private String toList() {
		String erg = "[";
		for(int i = 0; i < this.size(); i++) {
			erg = erg + this.get(i) + ", ";
		}
		if (erg != null && erg.length() > 2) {
	        erg = erg.substring(0, erg.length() - 2);
	    }
		erg = erg + "]";
		return erg;
	}
	
	/**
	 * Method to add another ArithmeticVector to this one. There is a defined border
	 * so there can no PM be used which is not in the IaaS.
	 * @param second The second ArithmeticVector, the velocity.
	 * @return The solution of this operation as a new ArithmeticVector.
	 */
	public ArithmeticVector addUp(ArithmeticVector second) {
		
		highestID = getHighest();		// the defined border
		
		ArithmeticVector erg = new ArithmeticVector();
		for(int i = 0; i < this.size(); i++) {
			if(this.get(i) + second.get(i) > highestID)		
				erg.add(highestID);
			else {
				if(this.get(i) + second.get(i) < 0)
					erg.add(0.0);	// if the actual value is 0.0 and the actual value of the velcity is smaller than 0.0,
									// 0.0 is set becouse there is no lower ID than 0.
				else
					erg.add(this.get(i) + second.get(i));
			}				
		}
		return erg;
	}
	
	/**
	 * Method to subtract another ArithmeticVector of this one. There is a defined border
	 * for not using a PM with an ID lower than 0, becouse such a PM does not exist.
	 * @param second The second ArithmeticVector, the velocity.
	 * @return The solution of this operation as a new ArithmeticVector.
	 */
	public ArithmeticVector subtract(ArithmeticVector second) {
		ArithmeticVector erg = new ArithmeticVector();
		for(int i = 0; i < this.size(); i++) {
			if(this.get(i) - second.get(i) < 0)
				erg.add(0.0);	// if the value would be lower than 0, 0 is set becouse there is no lower id than 0.
			else
				erg.add(this.get(i) - second.get(i));
			
		}
		return erg;
	}
	
	/**
	 * Method to multiply every value of this class with a constant.
	 * @param constant The double Value to multiply with.
	 * @return The solution of this operation as a new ArithmeticVector.
	 */
	public ArithmeticVector multiply(double constant) {
		ArithmeticVector erg = new ArithmeticVector();
		for(int i = 0; i < this.size(); i++) {
			erg.add(this.get(i) * constant);
		}
		return erg;		
	}
	
	/**
	 * Method for getting the PM with the highest ID.
	 * @return The highest ID of all PMs.
	 */
	private double getHighest() {
		double highest = 0;
		for(int i = 0; i < this.size(); i++) {
			if(highest < this.get(i))
				highest = this.get(i);
		}
		
		return highest;
	}
}
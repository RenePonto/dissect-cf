package hu.mta.sztaki.lpds.cloud.simulator.iaas.vmconsolidation;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;

/**
 * @author Rene Ponto
 *
 * This class manages VM consolidation with a particle swarm optimization algorithm. For that, particles
 * are used to find the best solution (minimized fitness function) for consolidation. At the moment the 
 * amount of currently active PMs, the number of migrations and the amount of overAllocated PMs are used 
 * for the fitness.
 */

public class PsoConsolidator extends SolutionBasedConsolidator {

	/** defines the amount of particles */
	private int swarmSize;		
	
	/** defines the amount of iterations */
	private int nrIterations;	
	
	/** the problem dimension, gets defined according to the amounts of VMs */
	private int dimension;	
	
	/** learning factor one */
	private int c1;		
	
	/** learning factor two */
	private int c2;				
	
	/** used to get a new velocity for each particle */
	private final double w = 0.6;
	
	/** For generating random numbers */
	private Random generator;

	/** counter for the graph actions */
	public int count = 1;	
	
	/** counter for creating particles */
	private int particleCounter = 1; 

	/** the swarm with all created particles */
	private Vector<Particle> swarm = new Vector<Particle>();

	// the best values so far
	private Fitness globalBest;
	private ArithmeticVector globalBestLocation;

	/**
	 * The constructor uses its superclass-constructor to create an abstract model and work on the 
	 * modelled PMs and VMs. After finding the solution everything will be done inside the simulator.
	 * 
	 * @param toConsolidate
	 * 			The iaas service with the machines to consolidate.	
	 * @param consFreq
	 * 			This value determines, how often the consolidation should run.
	 */
	public PsoConsolidator(IaaSService toConsolidate, long consFreq) {
		super(toConsolidate, consFreq);		
	}
	
	/**
	 * Reads the properties file and sets the constant values for consolidation.
	 */
	@Override
	protected void processProps() {
	
		this.swarmSize = Integer.parseInt(props.getProperty("psoSwarmSize"));
		this.nrIterations = Integer.parseInt(props.getProperty("psoNrIterations"));
		this.c1 = Integer.parseInt(props.getProperty("psoC1"));
		this.c2 = Integer.parseInt(props.getProperty("psoC2"));
		this.generator = new Random(Long.parseLong(props.getProperty("seed")));
		
		determineCreations(swarmSize);
	}
	
	/**
	 * The toString-method, used for debugging.
	 */
	public String toString() {
		String erg = "Amount of VMs: " + dimension + ", GlobalBest: " + this.globalBest + ", GlobalBestLocation: " + this.globalBestLocation;		
		return erg;
	}
	
	@Override
	protected void determineCreations(int numberOfCreations) {
		if(numberOfCreations < 3) {
			Logger.getGlobal().warning("Inappropriate size for the swarm/population.");
			return;
		}			
		if(numberOfCreations == 3) {
			randomCreations = 1;
			unchangedCreations = 1;
			firstFitCreations = 1;
		}
		else {
			unchangedCreations = 1;
			Double randoms = numberOfCreations * 0.25;
			firstFitCreations = randoms.intValue();
			randomCreations = numberOfCreations - unchangedCreations - firstFitCreations;
		}
	}
	
	/**
	 * Creates a new Particle and adds it to the swarm. It is intended that its 
	 * location and velocity do not change and represents the situation before
	 * starting consolidating.
	 */
	private void createUnchangedParticle() {
		Particle p = new Particle(items, bins, particleCounter);		
		ArithmeticVector loc = new ArithmeticVector();
		// do not change the actual location of VMs on PMs
		for(ModelVM vm : items) {
			loc.add((double)vm.gethostPM().getNumber() - 1);
		}
		ArithmeticVector vel = new ArithmeticVector();
		// this location shall not change, so we set 0 as velocity
		for(int i = 0; i < dimension; i++) {
			vel.add(0.0);
		}
		p.setLocation(loc);
		p.setVelocity(vel);
		p.setPBestLocation(loc);		
		swarm.add(p);
		
		// Logger.getGlobal().info("Added Particle: " + i + ", " + p.toString());
	}
	
	/**
	 * Creates a new Particle with a random location and velocity and adds it to the 
	 * swarm. 
	 */
	private void createParticleRandomly() {
		Particle p = new Particle(items, bins, particleCounter);		
		ArithmeticVector loc = new ArithmeticVector();
		for(int j = 0; j < this.dimension; j++) {			
			// create a new double to add random PMs to the list of the actual particle
			double a = generator.nextInt(bins.size());			
			loc.add(a); 	// add the random PM
		}				
		ArithmeticVector vel = new ArithmeticVector();
		for(int j = 0; j < this.dimension; j++) {			
			double a;			
			// here we make a random chance of getting a lower id or a higher id
			if(generator.nextBoolean()) {
				a = + 1;
			}				
			else {
				a = - 1;
			}
			vel.add(a); 	// add the random velocity
		}		
		p.setLocation(loc);
		p.setVelocity(vel);
		p.setPBestLocation(loc);		
		swarm.add(p);
		
		// Logger.getGlobal().info("Added Particle: " + i + ", " + p.toString());
	}
	
	/**
	 * Creates a new Particle generated by FirstFit and adds it to the 
	 * swarm.
	 */
	private void createParticleFirstFit() {
		Particle p = new Particle(items, bins, particleCounter);		
		ArithmeticVector loc = new ArithmeticVector();
		// do the consolidation with FirstFit
		for(ModelVM vm : items) {
			
			ModelPM host = null;
			for(int i = 0; i < getBins().size(); i++) {
				if(getBins().get(i).isMigrationPossible(vm)) {
					host = getBins().get(i);
					continue;
				}
			}
			if(host != null) {
				loc.add((double)host.getNumber() - 1);
			}
			else {
				Logger.getGlobal().warning("No PM in FirstFit particle creation found as host!");
				loc.add((double) 0);
			}		
		}
		ArithmeticVector vel = new ArithmeticVector();
		// this location shall not change, so we set 0 as velocity
		for(int i = 0; i < dimension; i++) {
			vel.add(0.0);
		}
		p.setLocation(loc);
		p.setVelocity(vel);
		p.setPBestLocation(loc);		
		swarm.add(p);
		
		// Logger.getGlobal().info("Added Particle: " + i + ", " + p.toString());		
	}

	/**
	 * Method to create a swarm and the defined amount of particles. Each Particle gets a 
	 * random location and velocity except one, which represents the situation before 
	 * starting consolidating.
	 */
	private void initializeSwarm() {
		swarm.clear();
		
		for(int i = 0; i < randomCreations; i++) {
			createParticleRandomly();
			particleCounter++;	// increase counter
		}
		for(int i = 0; i < firstFitCreations; i++) {
			createParticleFirstFit();
			particleCounter++;
		}
		for(int i = 0; i < unchangedCreations; i++) {
			createUnchangedParticle();
			particleCounter++;
		}
		
	}
	
	/**
	 * Perform the particle swarm optimization algorithm to optimize the mapping of VMs to PMs.
	 */
	@Override
	protected void optimize() {
		// get the dimension by getting the amount of VMs on the actual PMs
		this.dimension = items.size();
		initializeSwarm();
		
		int t = 0;		// counter for the iterations
		
		while(t < this.nrIterations) {
			// step 1 - update pBest
			for(Particle p : swarm) {
				
				//Logger.getGlobal().info("Before setting best values, Particle " + p.getNumber() + ", " + p.toString());
				
				p.evaluateFitnessFunction();	
				
				p.setPBest(p.getFitnessValue());
				p.setPBestLocation(p.getLocation());
				
				//the aim is to minimize the function
				if(p.getFitnessValue().isBetterThan(p.getPBest())) {
					p.setPBest(p.getFitnessValue());
					p.setPBestLocation(p.getLocation());
				}
				//Logger.getGlobal().info("Iteration " + t + ", Particle " + p.getNumber() + ", " + p.toString());
			}
			
			// step 2 - update gBest
			int bestParticleIndex = getMinPos();	// get the position of the minimum fitness value			
			//Logger.getGlobal().info("bestParticleIndex: " + bestParticleIndex + " in Iteration " + t);
			
			// set the new global best fitness / location
			if(t == 0 || swarm.get(bestParticleIndex).getFitnessValue().isBetterThan(globalBest)) {
				globalBest = swarm.get(bestParticleIndex).getFitnessValue();
				globalBestLocation = swarm.get(bestParticleIndex).getLocation();		
			}
			
			//Logger.getGlobal().info("GlobalBest: " + globalBest + ", GlobalBestLocation: " + globalBestLocation);
			
			for(int i = 0; i < swarmSize; i++) {
				Particle p = swarm.get(i);
				
				if(t == 0) {										
					ArithmeticVector newLoc = p.getLocation().addUp(p.getVelocity());				
					p.setLocation(newLoc);					
				}
				else {
					double r1 = generator.nextDouble();
					double r2 = generator.nextDouble();
					
					// step 3 - update velocity
					
					/**
					 * Comment on the function to update the velocity:
					 * 
					 * At first the actual Velocity of the Particle has to be multiplied with the user-supplied coefficient w,
					 * which is called the inertiaComponent. After this the cognitiveComponent has to be added to the 
					 * inertiaComponent, which inherits the multiplication of the coefficients c1 and r1, multiplied with the 
					 * result of the personalBestLocation of the Particle minus the actual location of it. Afterwards the 
					 * socialComponent also has to be added to the parts before. It inherits the multiplication of the
					 * coefficients c2 and r2, multiplied with the result of the subtraction of the globalBestLocation with the
					 * actual location of the Particle.
					 */
					
					ArithmeticVector inertiaComponent = p.getVelocity().multiply(w);
					ArithmeticVector cognitiveComponent = (p.getPBestLocation().subtract(p.getLocation()).multiply(c1 * r1));
					ArithmeticVector socialComponent = (globalBestLocation.subtract(p.getLocation()).multiply(c2 * r2));
					ArithmeticVector newVel = inertiaComponent.addUp(cognitiveComponent).addUp(socialComponent);
					
					p.setVelocity(newVel);
					
					//Logger.getGlobal().info("Particle: " + p.getNumber() + ", new Velocity: " + newVel);
					
					// step 4 - update location

					ArithmeticVector newLoc = p.getLocation().addUp(p.getVelocity());				
					p.setLocation(newLoc);
					
					//Logger.getGlobal().info("Particle: " + p.getNumber() + ", new Location: " + newLoc);					
					//Logger.getGlobal().info("Iteration " + t + ", Updated Particle " + p.getNumber() + System.getProperty("line.separator") + p.toString());
				}
				
			}
			
			//Logger.getGlobal().info("In iteration " + t + ", GlobalBest: " + globalBest + ", GlobalBestLocation: " + globalBestLocation);
			
			// increase counter for the iterations
			t++;
		}				
		implementSolution();
	}	
	
	/**
	 * In this method the hostPM of each VM is compared to the solution of the algorithm, the globalBestLocation.
	 * If the hosts are the same, no migration is needed, if not, we migrate the VM to the new host.
	 */
	private void implementSolution() {
		//Implement solution in the model
		for(int i = 0; i < dimension; i++) {
			
			ModelPM oldPm = items.get(i).gethostPM();
			ModelPM newPm = bins.get(globalBestLocation.get(i).intValue());		
			if(newPm != oldPm)
				oldPm.migrateVM(items.get(i), newPm);
		}
		adaptPmStates();
	}
	
	/**
	 * Method to find the position of the particle with the smallest fitness value of all Particles.
	 * @return The position where the value is in the vector.
	 */
	private int getMinPos() {		
		Fitness minValue = swarm.get(0).getFitnessValue();
		int pos = 0;
		
		for(int i = 0; i < swarm.size(); i++) {
			if(swarm.get(i).getFitnessValue().isBetterThan(minValue)) {
				minValue = swarm.get(i).getFitnessValue();
				pos = i;
			}
		}		
		return pos;
	}
	
	/**
	 * @author Rene Ponto
	 *
	 * The idea for particles is to have a list of double values where all hostPMs in order of their hosted VMs are.
	 * Therefore the id of the specific PM is used. On the given list mathematical operations can be done, like addition
	 * and substraction. For that list an ArithmeticVector is used.
	 */

	public class Particle {
	
		private int number;
	
		private Fitness fitnessValue;			// used to evaluate the quality of the solution
		private ArithmeticVector velocity;		// the actual velocity : in which direction shall the solution go?
		private ArithmeticVector location;		// the actual location : possible solution
	
		List<ModelVM> items;	// contains all VMs of the abstract model
		List<ModelPM> bins;		// contains all PMs of the abstract model
	
		private Fitness personalBest;	// the personal best Fitness so far
		private ArithmeticVector personalBestLocation;	// the personal best location so far
		
		/**
		 * Creates a new Particle and sets the bins and items lists with the values out of the model.
	 	* @param items
	 	* @param bins
	 	*/
		public Particle(List<ModelVM> items, List<ModelPM> bins, int number) {
			this.items = items;
			this.bins = bins;
			this.number = number;
		}
	
		/**
		 * Computes the total of PM which are overallocated, aggregated over all PMs and all
		 * resource types. This can be used as a component of the fitness.
		 */
		double getTotalOverAllocation() {
			//Logger.getGlobal().info(this.toString());
		
			double result = 0;
			//Now we determine the allocation of every PM
			Map<Integer,ResourceVector> allocations = new HashMap<>();
			for(ModelPM pm : bins) {
				allocations.put(pm.getNumber(), new ResourceVector(0,0,0));
			}
			for(int i = 0; i < items.size(); i++) {
				ModelPM pm = bins.get(location.get(i).intValue());
				allocations.get(pm.getNumber()).add(items.get(i).getResources());
			}
			//For each PM, see if it is overallocated; if yes, increase the result accordingly.
			for(ModelPM pm : bins) {
				ResourceVector allocation = allocations.get(pm.getNumber());
				ConstantConstraints cap = pm.getTotalResources();
				if(allocation.getTotalProcessingPower() > cap.getTotalProcessingPower()*pm.getUpperThreshold())
					result += allocation.getTotalProcessingPower() / (cap.getTotalProcessingPower()*pm.getUpperThreshold());
				if(allocation.getRequiredMemory() > cap.getRequiredMemory()*pm.getUpperThreshold())
					result += allocation.getRequiredMemory() / (cap.getRequiredMemory()*pm.getUpperThreshold());
			}
			return result;
		}

		/**
		 * Compute the number of PMs that should be on, given our mapping. This
		 * is a component of the fitness.
		 */
		private int getNrActivePms() {	
			//clears the list so there is no pm more than once there
			Set<Double> setItems = new LinkedHashSet<Double>(location);
			return setItems.size();
		}
	
		/**
		 * Compute the number of migrations needed, given our mapping. This
		 * can be used as a component of the fitness.
		 */
		private int getNrMigrations() {
			//Logger.getGlobal().info(this.toString());
			int result = 0;
			//See for each VM whether it must be migrated.
			for(int i = 0; i < items.size(); i++) {
				ModelPM oldPm = items.get(i).gethostPM();
				ModelPM newPm = bins.get(location.get(i).intValue());		
				if(newPm != oldPm)
					result++;
			}
			return result;
		}
	
		/**
		 * Used to get the actual fitnessValue of this particle.
		 * 
		 * @param location The actual result of this particle.
		 * @return new fitnessValue
		 */
		public void evaluateFitnessFunction() {
			roundValues();
			Fitness result = new Fitness();
		
			result.totalOverAllocated = getTotalOverAllocation();			
			result.nrActivePms = getNrActivePms();
			result.nrMigrations = getNrMigrations();
		
			fitnessValue = result;
		}
	
		public int getNumber() {
			return number;
		}
	
		public Fitness getPBest() {
			return this.personalBest;
		}
	
		public void setPBest(Fitness fitness) {
			this.personalBest = fitness;
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

		public Fitness getFitnessValue() {
			//fitnessValue = evaluateFitnessFunction();
			return fitnessValue;
		}
	
		private void roundValues() {
			for(int i = 0; i < location.size(); i++) {
			
				double value = location.get(i).intValue();
				location.set(i, value);
			}
		}
	
		/**
		 * The toString-method, used for debugging.
		 */
		public String toString() {
			String erg = "Location: " + this.getLocation() + System.getProperty("line.separator") 
				+ "Velocity: " + this.getVelocity() + System.getProperty("line.separator") 
				+ "FitnessValue: " + this.getFitnessValue() + ", PersonalBestFitness: " 
				+ this.getPBest() + System.getProperty("line.separator")
				+ "PersonalBestLocation: " + this.getPBestLocation() + System.getProperty("line.separator");
		
			return erg;
		}
	}
	
}
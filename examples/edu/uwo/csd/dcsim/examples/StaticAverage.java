package edu.uwo.csd.dcsim.examples;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import edu.uwo.csd.dcsim.*;
import edu.uwo.csd.dcsim.core.*;
import edu.uwo.csd.dcsim.vm.*;

public class StaticAverage extends DCSimulationTask {

	private static Logger logger = Logger.getLogger(StaticAverage.class);
	
	public static void main(String args[]) {
		
		Simulation.initializeLogging();

		Collection<DCSimulationTask> completedTasks;
		SimulationExecutor<DCSimulationTask> executor = new SimulationExecutor<DCSimulationTask>();
		
		executor.addTask(new StaticAverage("staticavg-1", 1088501048448116498l));
//		executor.addTask(new StaticAverage("staticavg-2", 3081198553457496232l));
//		executor.addTask(new StaticAverage("staticavg-3", -2485691440833440205l));
//		executor.addTask(new StaticAverage("staticavg-4", 2074739686644571611l));
//		executor.addTask(new StaticAverage("staticavg-5", -1519296228623429147l));
		
		completedTasks = executor.execute();
		
		for(SimulationTask task : completedTasks) {
			logger.info(task.getName());
			ExampleHelper.printMetrics(task.getResults());
		}
		
	}
	
	public StaticAverage(String name, long seed) {
		super(name, 864000000);
		this.setRandomSeed(seed);
		this.setMetricRecordStart(86400000);
	}

	@Override
	public void setup(DataCentreSimulation simulation) {
		DataCentre dc = ExampleHelper.createDataCentre(simulation);
		simulation.addDatacentre(dc);
		
		ArrayList<VMAllocationRequest> vmList = ExampleHelper.createVmList(simulation, true);
		
		ExampleHelper.placeVms(vmList, dc);
	}
	
}

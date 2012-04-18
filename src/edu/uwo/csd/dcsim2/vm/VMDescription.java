package edu.uwo.csd.dcsim2.vm;

import edu.uwo.csd.dcsim2.application.*;

public class VMDescription {

	private int cores;
	private int coreCapacity;
	private int memory;	
	private int bandwidth;
	private long storage;
	private ApplicationFactory applicationFactory;
	
	public VMDescription(int cores, int coreCapacity, int memory, int bandwidth, long storage, ApplicationFactory applicationFactory) {
		this.cores = cores;
		this.coreCapacity = coreCapacity;
		this.memory = memory;
		this.bandwidth = bandwidth;
		this.storage = storage;
		this.applicationFactory = applicationFactory;
	}
	
	public VM createVM() {
		return new VM(this, applicationFactory.createApplication());
	}
	
	public int getCpu() {
		return cores * coreCapacity;
	}
	
	public int getCores() {
		return cores;
	}
	
	public int getCoreCapacity() {
		return coreCapacity;
	}
	
	public int getMemory() {
		return memory;
	}
	
	public int getBandwidth() {
		return bandwidth;
	}
	
	public long getStorage() {
		return storage;
	}
	
	public ApplicationFactory getApplicationFactory() {
		return applicationFactory;
	}
	
}

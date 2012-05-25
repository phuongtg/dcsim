package edu.uwo.csd.dcsim.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import edu.uwo.csd.dcsim.DataCentre;
import edu.uwo.csd.dcsim.core.Simulation;
import edu.uwo.csd.dcsim.host.Host;
import edu.uwo.csd.dcsim.management.action.MigrationAction;
import edu.uwo.csd.dcsim.management.stub.HostStub;
import edu.uwo.csd.dcsim.management.stub.HostStubCpuInUseComparator;
import edu.uwo.csd.dcsim.management.stub.HostStubVmCountComparator;
import edu.uwo.csd.dcsim.management.stub.VmStub;
import edu.uwo.csd.dcsim.management.stub.VmStubCpuInUseComparator;

public class VMConsolidationPolicySimple extends VMConsolidationPolicy {

	double lowerThreshold;
	double upperThreshold;
	
	public VMConsolidationPolicySimple(Simulation simulation, DataCentre dc, long interval, double lowerThreshold, double upperThreshold) {
		super(simulation, dc, interval);
		this.lowerThreshold = lowerThreshold;
		this.upperThreshold = upperThreshold;
	}

	@Override
	public void execute() {
	
		ArrayList<Host> hostList = dc.getHosts();
		
		//Categorize hosts
		ArrayList<HostStub> empty = new ArrayList<HostStub>();
		ArrayList<HostStub> underUtilized = new ArrayList<HostStub>();
		ArrayList<HostStub> partiallyUtilized = new ArrayList<HostStub>();
		ArrayList<HostStub> stressed = new ArrayList<HostStub>();
		
		for (Host host : hostList) {
			double cpuUtilization = host.getCpuManager().getCpuUtilization();
			if (host.getVMAllocations().size() == 0) {
				empty.add(new HostStub(host));
			} else if (cpuUtilization < lowerThreshold) {
				underUtilized.add(new HostStub(host));
			} else if (cpuUtilization > upperThreshold) {
				stressed.add(new HostStub(host));
			} else {
				partiallyUtilized.add(new HostStub(host));
			}
		}
		
		//sort underutilized
		Collections.sort(underUtilized, new HostStubVmCountComparator(new HostStubCpuInUseComparator()));
		
		ArrayList<HostStub> sources = underUtilized;
		
		ArrayList<HostStub> targets = new ArrayList<HostStub>(partiallyUtilized.size() + underUtilized.size());
		
		targets.addAll(partiallyUtilized);
		targets.addAll(underUtilized);
		
		targets = orderTargetHosts(targets);
		
		HashSet<HostStub> usedSources = new HashSet<HostStub>();
		HashSet<HostStub> usedTargets = new HashSet<HostStub>();
		ArrayList<MigrationAction> migrations = new ArrayList<MigrationAction>();
		
		//iterate through source hosts
		int migsAllowed;
		for (HostStub source : sources) {
			if (!usedTargets.contains(source)) { //make sure the source hasn't been used as a target
			
				migsAllowed = 2;
				ArrayList<VmStub> vmList = orderSourceVms(source.getVms());
				
				for (VmStub vm : vmList) {
					
					for (HostStub target : targets) {
						 if (source != target &&
								 !usedSources.contains(target) &&	//make sure the target hasn't been used as a source
								 target.hasCapacity(vm) &&														//target has capacity
								 ((target.getCpuInUse(vm)) / target.getTotalCpu()) <= upperThreshold &&	//target will still not be stressed
								 target.getHost().isCapable(vm.getVM().getVMDescription())) {				//target is capable
							 
							 source.migrate(vm, target);
							 migrations.add(new MigrationAction(source, target, vm));
							 
							 --migsAllowed;
							 usedTargets.add(target);
							 usedSources.add(source);
							 break;
						 }
					}
					if (migsAllowed == 0) break;
				}
				
				targets = orderTargetHosts(targets);
			}
		}
		
		//trigger migrations
		for (MigrationAction migration : migrations) {
			migration.execute(simulation, this);
		}
	}
	
	protected ArrayList<VmStub> orderSourceVms(ArrayList<VmStub> sourceVms) {
		ArrayList<VmStub> sources = new ArrayList<VmStub>(sourceVms);
		Collections.sort(sources, new VmStubCpuInUseComparator());
		return sources;
	}
	
	protected ArrayList<HostStub> orderTargetHosts(ArrayList<HostStub> targets) {
		
		Collections.sort(targets, new HostStubCpuInUseComparator());
		Collections.reverse(targets);
		
		return targets;
	}

}
package edu.uwo.csd.dcsim.management;

import java.util.ArrayList;
import java.util.Collections;

import edu.uwo.csd.dcsim.core.Simulation;
import edu.uwo.csd.dcsim.host.Host;
import edu.uwo.csd.dcsim.host.comparator.*;
import edu.uwo.csd.dcsim.vm.VMAllocationRequest;

public class VMPlacementPolicyFixedCount extends VMPlacementPolicy {

	int vmsPerHost;
	
	public VMPlacementPolicyFixedCount(Simulation simulation, int vmsPerHost) {
		super(simulation);
		this.vmsPerHost = vmsPerHost;
	}

	@Override
	public boolean submitVM(VMAllocationRequest vmAllocationRequest) {

		ArrayList<Host> sortedHosts = sortHostList();
		Host target = findTargetHost(vmAllocationRequest, sortedHosts);

		if (target != null)
			return submitVM(vmAllocationRequest, target);

		return false;			
	}

	@Override
	public boolean submitVMs(ArrayList<VMAllocationRequest> vmAllocationRequests) {

		for (VMAllocationRequest request : vmAllocationRequests) {
			if (!submitVM(request))
				return false;
		}
		
		return true;
	}

	private ArrayList<Host> sortHostList() {
		ArrayList<Host> sorted = new ArrayList<Host>();
		
		sorted.addAll(datacentre.getHosts());
		Collections.sort(sorted, new HostVMCountComparator());
		Collections.reverse(sorted); //switch to decreasing order
		return sorted;
	}
	
	private Host findTargetHost(VMAllocationRequest vmAllocationRequest, ArrayList<Host> sortedHosts) {

		for (Host host : sortedHosts) {
			if (host.getVMAllocations().size() < vmsPerHost && host.hasCapacity(vmAllocationRequest) && host.isCapable(vmAllocationRequest.getVMDescription())) {
				return host;
			}
		}
		
		return null;
	}

}

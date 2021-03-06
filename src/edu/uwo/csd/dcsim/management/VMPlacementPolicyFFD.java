package edu.uwo.csd.dcsim.management;

import java.util.ArrayList;
import java.util.Collections;

import edu.uwo.csd.dcsim.core.Simulation;
import edu.uwo.csd.dcsim.host.*;
import edu.uwo.csd.dcsim.host.comparator.*;
import edu.uwo.csd.dcsim.vm.*;

/**
 * Implements a First Fit Decreasing algorithm, placing VMs on the Host with the most resources
 * allocated while still having room to add the VM. 
 * 
 * @author Michael Tighe
 *
 */
public class VMPlacementPolicyFFD extends VMPlacementPolicy {

	public VMPlacementPolicyFFD(Simulation simulation) {
		super(simulation);
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
			if (!submitVM(request)) {
				return false;
			}
		}
		
		return true;
	}
	
	private ArrayList<Host> sortHostList() {
		ArrayList<Host> sorted = new ArrayList<Host>();
		
		sorted.addAll(datacentre.getHosts());
		
		/* 
		 * order by cpu allocated in decreasing order to choose hosts that are
		 * already hosting the most CPU load
		 */
		Collections.sort(sorted, new HostCpuAllocationComparator());
		Collections.reverse(sorted);
		
		return sorted;
	}
	
	private Host findTargetHost(VMAllocationRequest vmAllocationRequest, ArrayList<Host> sortedHosts) {

		for (Host host : sortedHosts) {
			/* check allocations individually in order to override the CPU Manager's hasCapacity method to
			 * for the CPU Manager to NOT oversubscribe the initial vm placement
			 */
			if (host.isCapable(vmAllocationRequest.getVMDescription())
					&& host.getMemoryManager().hasCapacity(vmAllocationRequest)
					&& host.getBandwidthManager().hasCapacity(vmAllocationRequest)
					&& host.getStorageManager().hasCapacity(vmAllocationRequest)
					&& host.getCpuManager().getAvailableAllocation() >= vmAllocationRequest.getCpu()) {
				return host;
			}
		}
		
		return null;
	}


}

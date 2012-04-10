package edu.uwo.csd.dcsim2.management;

import java.util.ArrayList;

import edu.uwo.csd.dcsim2.host.*;
import edu.uwo.csd.dcsim2.vm.*;

public class HostStub {

	public enum State {ON, SUSPENDED, OFF}
	
	private Host host;
	private double vmmCpuInUse;
	private double vmmCpuAlloc;
	private double emptyVMAllocCpu = 0;
	
	private ArrayList<VmStub> vms = new ArrayList<VmStub>();
	private ArrayList<VmStub> incomingVMs = new ArrayList<VmStub>();
	
	private int incomingMigrationCount = 0;
	private int outgoingMigrationCount = 0;
	
	public static ArrayList<HostStub> createMockHostList(ArrayList<Host> hosts) {
		ArrayList<HostStub> mockHosts = new ArrayList<HostStub>();
		
		for (Host host : hosts)
			mockHosts.add(new HostStub(host));
		
		return mockHosts;
	}
	
	public HostStub(Host host) {
		this.host = host;
		
		vmmCpuInUse = host.getPrivDomainAllocation().getVm().getResourcesInUse().getCpu();
		vmmCpuAlloc = host.getPrivDomainAllocation().getCpu();
		
		incomingMigrationCount = host.getMigratingIn().size();
		outgoingMigrationCount = host.getMigratingOut().size();
		
		for (VMAllocation vmAllocation : host.getVMAllocations()) {
			if (vmAllocation.getVm() != null) {
				if (!host.getMigratingOut().contains(vmAllocation))
					vms.add(new VmStub(vmAllocation.getVm()));
			} else {
				emptyVMAllocCpu += vmAllocation.getCpu();
			}
		}

	}
	
	public Host getHost() {
		return host;
	}
	
	public State getState() {
		if (host.getState() == Host.HostState.ON || host.getState() == Host.HostState.POWERING_ON) {
			return State.ON;
		} else if (host.getState() == Host.HostState.SUSPENDED || host.getState() == Host.HostState.SUSPENDING) {
			return State.SUSPENDED;
		} else {
			return State.OFF; 
		}
	}
	
	public ArrayList<VmStub> getVms() {
		return vms;
	}
	
	public double getCpuInUse() {
		double cpuInUse = vmmCpuInUse;
		for (VmStub vm : vms) {
			cpuInUse += vm.getCpuInUse();
		}
		return cpuInUse;
	}
	
	public double getCpuAllocated() {
		double cpuAlloc = vmmCpuAlloc + emptyVMAllocCpu;
		for (VmStub vm : vms) {
			cpuAlloc += vm.getCpuAlloc();
		}
		return cpuAlloc;
	}
	
	public double getTotalCpu() {
		return host.getCpuManager().getTotalCpu();
	}
	
	public double getUnusedCpu() {
		return getTotalCpu() - getCpuInUse();
	}
	
	public double getUnallocatedCpu() {
		return getTotalCpu() - getCpuAllocated();
	}
	
	public double getCpuUtilization() {
		return getCpuInUse() / getTotalCpu();
	}
	
	public int getIncomingMigrationCount() {
		return incomingMigrationCount;
	}
	
	public int getOutgoingMigrationCount() {
		return outgoingMigrationCount;
	}
	
	public void setIncomingMigrationCount(int incomingMigrationCount) {
		this.incomingMigrationCount = incomingMigrationCount;
	}
	
	public void setOutgoingMigrationCount(int outgoingMigrationCount) {
		this.outgoingMigrationCount = outgoingMigrationCount;
	}
	
	public ArrayList<VmStub> getIncomingVMs() {
		return incomingVMs;
	}
	
	public void migrate(VmStub vm, HostStub target) {
		++outgoingMigrationCount;
		
		target.setIncomingMigrationCount(target.getIncomingMigrationCount() + 1);
		vms.remove(vm);
		target.getVms().add(vm);
		target.getIncomingVMs().add(vm);
	}
	
	/**
	 * Checks with the real Host's CPUManager for capacity to add the VM, along with any other VMs previously
	 * indicated as migrating in. It DOES NOT take into account freeing resources from migrating out VMs, for
	 * a purpose: If migrations are triggered simultaneously, the load of a migrating out VM will still be present
	 * when incoming VMs begin migration. This could be changed provided there is a mechanism for specifying a
	 * sequence of migrations.
	 * @param vm
	 * @return
	 */
	public boolean hasCapacity(VmStub vm) {
		//assemble a list of all VMs to be migrated
		ArrayList<VMAllocationRequest> vmAllocationRequests = new ArrayList<VMAllocationRequest>();
		
		for (VmStub incomingVM : incomingVMs) {
			vmAllocationRequests.add(new VMAllocationRequest(incomingVM.getVM().getVMAllocation()));
		}
		
		//add new VM to check
		vmAllocationRequests.add(new VMAllocationRequest(vm.getVM().getVMAllocation()));
		
		return host.hasCapacity(vmAllocationRequests);
	}
	
	public double getCpuInUse(VmStub vm) {
		return getCpuInUse() + vm.getCpuInUse();
	}
	
	public double getCpuAllocated(VmStub vm) {
		return getCpuAllocated() + vm.getCpuAlloc();
	}
	
}
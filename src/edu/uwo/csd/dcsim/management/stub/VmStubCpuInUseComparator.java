package edu.uwo.csd.dcsim.management.stub;

import java.util.Comparator;


public class VmStubCpuInUseComparator implements Comparator<VmStub> {

	@Override
	public int compare(VmStub o1, VmStub o2) {
		return (int)(Math.round(o1.getCpuInUse()) -  Math.round(o2.getCpuInUse()));
	}

}

package edu.uwo.csd.dcsim.application;

import java.util.ArrayList;

import edu.uwo.csd.dcsim.application.workload.Workload;
import edu.uwo.csd.dcsim.common.ObjectBuilder;
import edu.uwo.csd.dcsim.vm.VMDescription;

/**
 * ServiceBuilder provides a simpler interface for building a multi-tiered Service. It automatically handles linking the workload and tiers
 * together in the order in which they are added. While this is a more convenient method of building a Service, it is not mandatory; Service
 * can still be instantiated directly.
 * 
 * @author Michael Tighe
 *
 */
public class ServiceBuilder implements ObjectBuilder<Service> {

	private Workload workload;
	private ArrayList<Service.ServiceTier> tiers =  new ArrayList<Service.ServiceTier>();
	
	/**
	 * Add the Workload that will feed this Service
	 * @param value
	 * @return
	 */
	public ServiceBuilder workload(Workload value) {
		this.workload = value;
		return this;
	}
	
	/**
	 * Add a new tier to the Service
	 * @param applicationTier
	 * @param vmDescription
	 * @param minSize
	 * @param maxSize
	 * @return
	 */
	public ServiceBuilder tier(ApplicationTier applicationTier, VMDescription vmDescription, int minSize, int maxSize) {
		Service.ServiceTier tier = new Service.ServiceTier(applicationTier, vmDescription, minSize, maxSize);
		tiers.add(tier);
		return this;
	}
	
	@Override
	public Service build() {
		
		if (workload == null)
			throw new IllegalStateException("Cannot build a Service with no Workload");
		if (tiers.size() == 0)
			throw new IllegalStateException("Cannot build a Service with no tiers");
		
		Service service = new Service();
		service.setWorkload(workload);
		
		workload.setWorkTarget(tiers.get(0).getApplicationTier());
		for (int i = 1; i < tiers.size(); ++i) {
			tiers.get(i - 1).getApplicationTier().setWorkTarget(tiers.get(i).getApplicationTier());
		}
		tiers.get(tiers.size() - 1).getApplicationTier().setWorkTarget(workload);
		
		for (Service.ServiceTier tier : tiers)
			service.addServiceTier(tier);
		
		
		return service;
	}

}

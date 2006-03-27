
package org.tigr.microarray.mev.persistence;

import java.beans.*;

import org.tigr.microarray.mev.cluster.gui.impl.sota.SOTATreeData;


public class SOTATreeDataPersistenceDelegate extends PersistenceDelegate {
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		SOTATreeData std = (SOTATreeData) oldInstance;
		return new Expression((SOTATreeData) oldInstance, oldInstance.getClass(), "new",
						new Object[]{std.nodeHeights, std.leftChild, std.rightChild,
				std.nodePopulation, new Boolean(std.absolute), new Integer(std.function),
				new Float(std.factor), std.clusterPopulation, std.clusterDiversity, 
				std.cluster, std.centroidMatrix});  
		}
	    	
}












































 
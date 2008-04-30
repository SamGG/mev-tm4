/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMExperimentClusterViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.svm;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class SVMExperimentClusterViewer extends ExperimentClusterViewer {
    
    
    /**
     * Constructs a <code>SVMExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public SVMExperimentClusterViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * @inheritDoc
     */
    public SVMExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    		super(e, clusters, genesOrder, drawAnnotations, offset);
    }
    
}



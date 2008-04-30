/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CASTCentroidViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.cast;



import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;


public class CASTCentroidViewer extends CentroidViewer {    
    
    /**
     * Construct a <code>CASTCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public CASTCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * This constructor is used by XMLEncoder/Decoder to store and retreive a 
     * CentroidViewer object to/from and xml file.  This constructor must 
     * always exist, with its current method signature, for purposes of 
     * backwards-compatability in loading old save-files from MeV versions 
     * of v3.2 and later.  
     * 
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     * @param id
     */
    public CASTCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }

}

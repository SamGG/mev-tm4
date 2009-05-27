/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GSHExperimentViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gsh;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class GSHExperimentViewer extends ExperimentViewer {
    
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public GSHExperimentViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * State-saving constructor for MeV v4.4.
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     */
    public GSHExperimentViewer(Experiment experiment, ClusterWrapper clusters, ClusterWrapper samplesOrder, Boolean drawAnnotations) {
    	this(experiment, clusters.getClusters(), samplesOrder.getClusters()[0], drawAnnotations.booleanValue());
    }
    /**
     * State-saving constructor used to load saved analysis files from MeV v4.0-4.3.
     * 
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     * @param header
     * @param insets
     * @deprecated
     */
    public GSHExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations) {
    	super(e, clusters, samplesOrder, drawAnnotations, new ExperimentHeader(e, clusters), new Insets(0, 10, 0,0));
    }
 
}

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NMFGUI.java,v $
 * $Revision: 1.10 $
 * $Date: 2009-08-24 17:27:40 $
 * $Author: dschlauch $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.nmf;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.cluster.gui.impl.pca.PCInfoViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  dschlauch
 * @version
 */
public class NMFGUI implements IClusterGUI, IScriptGUI {
    
    protected Algorithm algorithm;
    protected IFramework framework;
    protected Progress progressBar;
    protected Experiment experiment;
    protected Experiment connectivityMatrix[];
	FloatMatrix[][] W;
	FloatMatrix[][] H;
	float[][] costs;
    protected int[][][] clusters;
    protected FloatMatrix[] means;
    protected FloatMatrix[] variances;
	private float cophen[];
    
    
    protected String[] auxTitles;
    protected Object[][] auxData;
    
    
    
    protected IData data;
    
    int rvalue, maxrvalue, numRuns, maxIters;
    boolean divergence, doSamples, storeClusters, multiClusters;
    
    /** Creates new NMFGUI */
    public NMFGUI() {
    }
    
    /**
     * This method should return a tree with calculation results or
     * null, if analysis start was canceled.
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *       which is used to obtain an initial analysis data and parameters.
     * @throws AlgorithmException if calculation was failed.
     * @throws AbortException if calculation was canceled.
     * @see IFramework
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        this.framework = framework;
        this.experiment = framework.getData().getExperiment();        
        this.data = framework.getData();
        
        NMFDialog initNMF = new NMFDialog((JFrame)framework.getFrame());

        if (initNMF.showModal()==JOptionPane.CANCEL_OPTION) 
        	return null;
        
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("NMF");
            algorithm.addAlgorithmListener(listener);

            this.progressBar = new Progress(framework.getFrame(), "Running NMF Analysis", listener);
            this.progressBar.show();
            
            AlgorithmData data = new AlgorithmData();

            rvalue = initNMF.getRValue();
            maxrvalue = initNMF.getMaxRValue();
            numRuns = initNMF.getNumRuns();
            maxIters = initNMF.getMaxIterations();
            divergence = initNMF.getDivergence();
            doSamples = initNMF.isDoSamples();
            storeClusters = initNMF.isStoreClusters();
            multiClusters = initNMF.isMultiClusters();
            
            int numFactorRuns = 1 + maxrvalue - rvalue;
            System.out.println("num factor runs " + numFactorRuns);
            connectivityMatrix = new Experiment[numFactorRuns];
        	W = new FloatMatrix[numFactorRuns][];
        	H = new FloatMatrix[numFactorRuns][];
        	costs = new float[numFactorRuns][];
            clusters = new int[numFactorRuns][][] ;
            means = new FloatMatrix[numFactorRuns];
            variances = new FloatMatrix[numFactorRuns];
            cophen = new float[numFactorRuns];
            
            data.addMatrix("experiment", experiment.getMatrix());
            data.addParam("r-value", String.valueOf(rvalue));
            data.addParam("max- r-value", String.valueOf(maxrvalue));
            data.addParam("runs", String.valueOf(numRuns));
            data.addParam("iterations", String.valueOf(maxIters));
            data.addParam("divergence", String.valueOf(divergence));
            data.addParam("doSamples", String.valueOf(doSamples));
            data.addParam("multiClusters", String.valueOf(multiClusters));
            
            long start = System.currentTimeMillis();
            System.out.println("stating executing");

            Cluster[] result_cluster = new Cluster[1+maxrvalue-rvalue];
            for (int factorIndex=0; factorIndex<=(maxrvalue-rvalue); factorIndex++){
                data.addParam("r-value", String.valueOf(rvalue+factorIndex));
	            AlgorithmData result = algorithm.execute(data);
	            
	            System.out.println("finished executing");
	            
	            // getting the results
	            result_cluster[factorIndex] = result.getCluster("cluster");
	            clusters[factorIndex] = result.getIntMatrix("clusters");
	            means[factorIndex] = result.getMatrix("clusters_means");
	            variances[factorIndex] = result.getMatrix("clusters_variances");
	
	            cophen[factorIndex] = result.getParams().getFloat("cophen");
	            if (storeClusters)
	            	storeClusters(clusters[factorIndex]);
	            
	            connectivityMatrix[factorIndex]= new Experiment(result.getMatrix("connectivity-matrix"), experiment.getColumns());
	
	        	W[factorIndex] = new FloatMatrix[numRuns];
	        	H[factorIndex] = new FloatMatrix[numRuns];
	        	for (int i=0; i<numRuns; i++)
	        		W[factorIndex][i] = result.getMatrix("W"+i);
	        	for (int i=0; i<numRuns; i++)
	        		H[factorIndex][i] = result.getMatrix("H"+i);
	            costs[factorIndex] = result.getMatrix("costs").transpose().A[0];
            }
            GeneralInfo info = new GeneralInfo();
            
            long time = System.currentTimeMillis() - start;
            info.time = time;

            return createResultTree(result_cluster, info);
            
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progressBar != null) {
                progressBar.dispose();
            }
        }
    }
    
    
    private void storeClusters(int[][]clusters) {
		for (int i=0; i<clusters.length; i++)
			framework.storeClusterWithoutDialog(clusters[i], "Algorithm", "NMF - Cluster "+ (i+1), "Cluster "+Integer.toString((i+1)),"NMF - Cluster "+ (i+1), null, ClusterRepository.EXPERIMENT_CLUSTER);
	}

	public AlgorithmData getScriptParameters(IFramework framework) {
        
        this.experiment = framework.getData().getExperiment();
        
        NMFDialog NMFDialog = new NMFDialog((JFrame)framework.getFrame());
        
        if (NMFDialog.showModal()==JOptionPane.CANCEL_OPTION) 
        	return null;
        
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        AlgorithmData data = new AlgorithmData();
        
        
        // alg name
        data.addParam("name", "NMF");
        
        // alg type
        data.addParam("alg-type", "cluster-genes");
        
        // output class
        data.addParam("output-class", "partition-output");
        
        //output nodes
        String [] outputNodes = new String[2];
        outputNodes[0] = "Significant Genes";
        outputNodes[1] = "Non-significant Genes";
        
        data.addStringArray("output-nodes", outputNodes);
        
        
        return data;
    }
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        
        Listener listener = new Listener();
        this.experiment = experiment;
        this.data = framework.getData();    
        return null;
//        try {
//            algData.addMatrix("experiment", experiment.getMatrix());
//            algorithm = framework.getAlgorithmFactory().getAlgorithm("NMF");
//            algorithm.addAlgorithmListener(listener);
//
//            this.progressBar = new Progress(framework.getFrame(), "Running NMF Analysis", listener);
//            this.progressBar.show();
//            
//            long start = System.currentTimeMillis();
//            AlgorithmData result = algorithm.execute(algData);
//            long time = System.currentTimeMillis() - start;
//            
//            // getting the results
//            Cluster result_cluster = result.getCluster("cluster");
//            NodeList nodeList = result_cluster.getNodeList();
//            //AlgorithmParameters resultMap = result.getParams();
//            int k = 2; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
//                       
//            this.clusters = new int[k][];
//            for (int i=0; i<k; i++) {
//                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
//            }
//            this.means = result.getMatrix("clusters_means");
//            this.variances = result.getMatrix("clusters_variances");
//            
//            
//            AlgorithmParameters params = algData.getParams();
//            
//            GeneralInfo info = new GeneralInfo();
//            info.time = time;
//            //ADD MORE INFO PARAMETERS HERE
//            info.alpha = params.getFloat("alpha");
////            info.correctionMethod = getSigMethod(params.getInt("correction-method"));
//            info.usePerms = params.getBoolean("usePerms");
//            info.numPerms = params.getInt("numPerms");
//            info.function = framework.getDistanceMenu().getFunctionName(params.getInt("distance-function"));
//            info.hcl = params.getBoolean("hierarchical-tree");
//            info.hcl_genes = params.getBoolean("calculate-genes");
//            info.hcl_samples = params.getBoolean("calculate-experiments");
//            if(info.hcl)
//                info.hcl_method = params.getInt("method-linkage") ;
//            
//            Vector<String> titlesVector = new Vector<String>();
//            
//            auxTitles = new String[titlesVector.size()];
//            for (int i = 0; i < auxTitles.length; i++) {
//                auxTitles[i] = (String)(titlesVector.get(i));
//            }
//            
//            auxData = new Object[experiment.getNumberOfGenes()][auxTitles.length];
//            
//            return createResultTree(result_cluster, info);
//            
//        } finally {
//            if (algorithm != null) {
//                algorithm.removeAlgorithmListener(listener);
//            }
//            if (progressBar != null) {
//                progressBar.dispose();
//            }
//        }
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(Cluster[] result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("NMF");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    protected void addResultNodes(DefaultMutableTreeNode root, Cluster[] result_cluster, GeneralInfo info) {
    	DefaultMutableTreeNode[] factorRoot = new DefaultMutableTreeNode[result_cluster.length];
    	for (int i=0; i<result_cluster.length; i++){
    		factorRoot[i] = new DefaultMutableTreeNode(""+rvalue+i+" Cluster NMF");
    		factorRoot[i].add(new DefaultMutableTreeNode("Cophenetic Correlation = " + (cophen[i])));
	        addConsensusMatrix(factorRoot[i], result_cluster[i], info, i);
	        addExpressionImages(factorRoot[i], i);
	        addCentroidViews(factorRoot[i], i);
	//        addTableViews(root, i);
	//        addClusterInfo(root, i);
	        addWHFactors(factorRoot[i], i);
	        root.add(factorRoot[i]);
    	}
        addGeneralInfo(root, info);
    }
    
    private void addWHFactors(DefaultMutableTreeNode root, int factorIndex) {
        DefaultMutableTreeNode WNode = new DefaultMutableTreeNode("Metagenes (W)");
        DefaultMutableTreeNode HNode = new DefaultMutableTreeNode("Metagenes (H)");
        NMFFactorViewer wfv = new NMFFactorViewer(W[factorIndex]);
        for (int i=0; i<W.length; i++) {
        	WNode.add(new DefaultMutableTreeNode(new LeafInfo("W Factor "+(i+1) + ", cost = "+costs[i], wfv, new Integer(i))));
        }
        NMFFactorViewer hfv = new NMFFactorViewer(H[factorIndex]);
        for (int i=0; i<H.length; i++) {
        	HNode.add(new DefaultMutableTreeNode(new LeafInfo("H Factor "+(i+1) + ", cost = "+costs[i], hfv, new Integer(i))));
        }
        root.add(WNode);
        root.add(HNode);
		
	}

	protected void addTableViews(DefaultMutableTreeNode root, int factorIndex) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
        IViewer tabViewer = new ClusterTableViewer(this.experiment, this.clusters[factorIndex], this.data, this.auxTitles, this.auxData);
        for (int i=0; i<this.clusters.length; i++) {
            if (i < this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", tabViewer, new Integer(i))));
            } else if (i == this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", tabViewer, new Integer(i))));
            }
        }
        root.add(node);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    protected void addExpressionImages(DefaultMutableTreeNode root, int factorIndex) {
    	for (int i=0; i<clusters.length; i++){
    		for (int j=0; j<clusters[i].length; j++)
    			System.out.print(clusters[i][j]+"\t");
    		System.out.println();
    	}
    	System.out.println("here3");
    	
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new NMFExperimentClusterViewer(this.experiment, clusters[factorIndex]);
        for (int i=0; i<this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+ (i+1), expViewer, new Integer(i))));
        }
        root.add(node);
    }
    
    /**
     * Adds nodes to display hierarchical trees.
     */
    protected void addConsensusMatrix(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info, int factorIndex) {
        NodeList nodeList = result_cluster.getNodeList();
        root.add(new DefaultMutableTreeNode(new LeafInfo("Consensus Matrix with HCL ", createHCLViewer(nodeList.getNode(0), info, factorIndex))));
    }
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    protected IViewer createHCLViewer(Node clusterNode, GeneralInfo info, int factorIndex) {
        HCLTreeData genes_result = info.hcl_genes ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = info.hcl_samples ? getResult(clusterNode, info.hcl_genes ? 4 : 0) : null;
        return new NMFHCLViewer(this.connectivityMatrix[factorIndex], createDefaultFeatures(connectivityMatrix[factorIndex]), genes_result, samples_result);
    }
    protected int[] createDefaultFeatures(Experiment experiment) {
        int[] features = new int[experiment.getNumberOfGenes()];
        for (int i=0; i<features.length; i++) {
            features[i] = i;
        }
        return features;
    }
    /**
     * Returns a hcl tree data from the specified cluster node.
     */
    protected HCLTreeData getResult(Node clusterNode, int pos) {
        HCLTreeData data = new HCLTreeData();
        NodeValueList valueList = clusterNode.getValues();
        data.child_1_array = (int[])valueList.getNodeValue(pos).value;
        data.child_2_array = (int[])valueList.getNodeValue(pos+1).value;
        data.node_order = (int[])valueList.getNodeValue(pos+2).value;
        data.height = (float[])valueList.getNodeValue(pos+3).value;
        return data;
    }
    
    /**
     * Adds node with cluster information.
     */
    protected void addClusterInfo(DefaultMutableTreeNode root, int factorIndex) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results (#,%)", new NMFInfoViewer(this.clusters[factorIndex], this.experiment.getNumberOfGenes()))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    protected void addCentroidViews(DefaultMutableTreeNode root, int factorIndex) {
    	System.out.println("Adding centroids");
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        NMFExperimentCentroidViewer centroidViewer = new NMFExperimentCentroidViewer(this.experiment, clusters[factorIndex]);
        centroidViewer.setMeans(this.means[factorIndex].A);
        centroidViewer.setVariances(this.variances[factorIndex].A);
        for (int i=0; i<this.clusters.length; i++) {
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+ (i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+ (i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
       }
//        root.add(centroidNode);
        root.add(expressionNode);
        
//        NMFCentroidsViewer centroidsViewer = new NMFCentroidsViewer(this.experiment, clusters, geneTimeMeans, geneTimeSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues);
//
//        centroidsViewer.setMeans(this.means.A);
//        centroidsViewer.setVariances(this.variances.A);
//        
//        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
//        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
    }
    
    
    /**
     * Adds node with general information.
     */
    protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Cluster by " + (doSamples ? "Samples": "Genes")));
        node.add(new DefaultMutableTreeNode("Number of clusters: "+String.valueOf(rvalue)));
        node.add(new DefaultMutableTreeNode("Number of runs: " + String.valueOf(numRuns)));
        node.add(new DefaultMutableTreeNode("Maximum iterations: "+String.valueOf(maxIters)));
        node.add(new DefaultMutableTreeNode("Cost measurement: " + (divergence ? "Divergence" : "Euclidean distance")));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time-1)+" ms"));
        root.add(node);
    }
    
    
    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    protected class Listener extends DialogListener implements AlgorithmListener {
    	//EH added so AMP could extend this class
        protected Listener(){super();}
        public void valueChanged(AlgorithmEvent event) {
            switch (event.getId()) {
                case AlgorithmEvent.SET_UNITS:
                    progressBar.setUnits(event.getIntValue());
                    progressBar.setDescription(event.getDescription());
                    break;
                case AlgorithmEvent.PROGRESS_VALUE:
                    progressBar.setValue(event.getIntValue());
                    progressBar.setDescription(event.getDescription());
                    break;
                case AlgorithmEvent.MONITOR_VALUE:
                    int value = event.getIntValue();
                    break;
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
                progressBar.dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            progressBar.dispose();
        }
    }
    
    protected class GeneralInfo {

        public int clusters;
        public String correctionMethod;
        public float alpha;
        public long time;
        public String function;
        
        protected boolean hcl, usePerms;
        protected int hcl_method, numPerms;
        protected boolean hcl_genes;
        protected boolean hcl_samples = true;
    	//EH constructor added so AMP could extend
        protected GeneralInfo(){
    		super();
    	}        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
    
}
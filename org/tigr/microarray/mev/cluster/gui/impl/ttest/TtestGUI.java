/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: TtestGUI.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.ttest;

import java.util.Vector;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.util.FloatMatrix;
import org.tigr.util.ConfMap;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Monitor;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;

/**
 *
 * @author  nbhagaba
 * @version
 */
public class TtestGUI implements IClusterGUI {
    
    private Algorithm algorithm;
    private Progress progress;
    //private Monitor monitor;
    
    private Experiment experiment;
    private int[][] clusters;
    private FloatMatrix means;
    private FloatMatrix variances;
    
    //private Vector sigTValues, nonSigTValues, sigPValues, nonSigPValues, additionalHeaders, additionalSigOutput, additionalNonSigOutput;
    private Vector tValues, pValues, dfValues, meansA, meansB, sdA, sdB;
    private IData data;
    Vector exptNamesVector;
    int[] groupAssignments;
    boolean isPermutations;
    boolean[] isSig;
    double[] diffMeansBA, negLog10PValues;
    
    public TtestGUI() {
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
        //int k = 2;
        
        this.experiment = framework.getData().getExperiment();
        exptNamesVector = new Vector();
        this.data = framework.getData();
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(i));
        }
        
        TtestInitDialog ttDialog = new TtestInitDialog((JFrame) framework.getFrame(), true, exptNamesVector);
        ttDialog.setVisible(true);
        
        if (!ttDialog.isOkPressed()) return null;
        
        double alpha = 0.01d;
        try {
            alpha = ttDialog.getAlphaValue();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Invalid alpha value!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        groupAssignments = ttDialog.getGroupAssignments();
        int significanceMethod = ttDialog.getSignificanceMethod();
        boolean isHierarchicalTree = ttDialog.isDrawTrees();
        boolean isPermut = ttDialog.isPermut();
        isPermutations = isPermut;
        int numCombs = ttDialog.getUserNumCombs();
        boolean useAllCombs = ttDialog.useAllCombs();
        
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame());
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperience();
            hcl_genes = hcl_dialog.isClusterGenes();
        }
        Listener listener = new Listener();
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("TTEST");
            algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            //this.monitor = new Monitor(framework.getFrame(), "Reallocations", 25, 100, 210.0/genes);
            //this.monitor.setStepXFactor((int)Math.floor(245/iterations));
            //this.monitor.update(genes);
            //this.monitor.show();
            
            this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix());
            data.addParam("distance-factor", String.valueOf(1.0f));
            IDistanceMenu menu = framework.getDistanceMenu();
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            
            int function = menu.getDistanceFunction();
            if (function == Algorithm.DEFAULT) {
                function = Algorithm.EUCLIDEAN;
            }
            
            data.addParam("distance-function", String.valueOf(function));
            data.addIntArray("group-assignments", groupAssignments);
            data.addParam("alpha", String.valueOf(alpha));
            data.addParam("significance-method", String.valueOf(significanceMethod));
            data.addParam("is-permut", String.valueOf(isPermut));
            data.addParam("num-combs", String.valueOf(numCombs));
            data.addParam("use-all-combs", String.valueOf(useAllCombs));
            
            // hcl parameters
            if (isHierarchicalTree) {
                data.addParam("hierarchical-tree", String.valueOf(true));
                data.addParam("method-linkage", String.valueOf(hcl_method));
                data.addParam("calculate-genes", String.valueOf(hcl_genes));
                data.addParam("calculate-experiments", String.valueOf(hcl_samples));
            }
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            AlgorithmParameters resultMap = result.getParams();
            int k = 2; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            FloatMatrix sigPValuesMatrix = result.getMatrix("sigPValues");
            FloatMatrix sigTValuesMatrix = result.getMatrix("sigTValues");
            FloatMatrix nonSigPValuesMatrix = result.getMatrix("nonSigPValues");
            FloatMatrix nonSigTValuesMatrix = result.getMatrix("nonSigTValues");
            FloatMatrix pValuesMatrix = result.getMatrix("pValues");
            FloatMatrix tValuesMatrix = result.getMatrix("tValues");
            FloatMatrix dfMatrix = result.getMatrix("dfValues");
            FloatMatrix meansAMatrix = result.getMatrix("meansAMatrix");
            FloatMatrix meansBMatrix = result.getMatrix("meansBMatrix");
            FloatMatrix sdAMatrix = result.getMatrix("sdAMatrix");
            FloatMatrix sdBMatrix = result.getMatrix("sdBMatrix");
            FloatMatrix isSigMatrix = result.getMatrix("isSigMatrix");
            
            pValues = new Vector();
            tValues = new Vector();
            dfValues = new Vector();
            meansA = new Vector();
            meansB = new Vector();
            sdA = new Vector();
            sdB = new Vector();
            
            for (int i = 0; i < pValuesMatrix.getRowDimension(); i++) {
                pValues.add(new Float(pValuesMatrix.A[i][0]));
            }
            
            for (int i = 0; i < tValuesMatrix.getRowDimension(); i++) {
                tValues.add(new Float(tValuesMatrix.A[i][0]));
            }
            
            for (int i = 0; i < dfMatrix.getRowDimension(); i++) {
                dfValues.add(new Float(dfMatrix.A[i][0]));
            }
            
            for (int i = 0; i < meansAMatrix.getRowDimension(); i++) {
                meansA.add(new Float(meansAMatrix.A[i][0]));
                meansB.add(new Float(meansBMatrix.A[i][0]));
                sdA.add(new Float(sdAMatrix.A[i][0]));
                sdB.add(new Float(sdBMatrix.A[i][0]));
            }
            
            isSig = new boolean[isSigMatrix.getRowDimension()];
            
            for (int i = 0; i < isSig.length; i++) {
                if (isSigMatrix.A[i][0] == 1.0f) {
                    isSig[i] = true;
                } else {
                    isSig[i] = false;
                }
            }
            
            diffMeansBA = new double[isSigMatrix.getRowDimension()];
            for (int i = 0; i < diffMeansBA.length; i++) {
                diffMeansBA[i] = (double)(meansBMatrix.A[i][0]) - (double)(meansAMatrix.A[i][0]);
            }
            
            negLog10PValues = new double[isSigMatrix.getRowDimension()];
            
            double log10BaseE = Math.log(10);
            
            for (int i = 0; i < negLog10PValues.length; i++) {
                double currentP = (double)(pValuesMatrix.A[i][0]);
                negLog10PValues[i] = (-1)*((Math.log(currentP))/log10BaseE);
                //System.out.println("i = " + i + ", currentP = " + currentP + ", negLog10P = " + negLog10PValues[i]);
            }
            
            //sigTValues = new Vector();
            //sigPValues = new Vector();
            //nonSigTValues = new Vector();
            //nonSigTValues = new Vector();
            
            //additionalHeaders = new Vector();
            //additionalHeaders.add("Absolute t Value");
            //additionalHeaders.add("p Value");
            
            //additionalSigOutput = new Vector();
            //additionalNonSigOutput = new Vector();
            
            /*
            for (int i = 0; i < sigPValuesMatrix.getRowDimension(); i++) {
                sigPValues.add(new Float(sigPValuesMatrix.A[i][0]));
            }
             
            for (int i = 0; i < nonSigPValuesMatrix.getRowDimension(); i++) {
                nonSigPValues.add(new Float(nonSigPValuesMatrix.A[i][0]));
            }
             
            for (int i = 0; i < sigTValuesMatrix.getRowDimension(); i++) {
                sigTValues.add(new Float(sigTValuesMatrix.A[i][0]));
            }
             
            for (int i = 0; i < nonSigTValuesMatrix.getRowDimension(); i++) {
                nonSigTValues.add(new Float(nonSigTValuesMatrix.A[i][0]));
            }
             
            additionalSigOutput.add(sigTValues);
            additionalSigOutput.add(sigPValues);
            additionalNonSigOutput.add(nonSigTValues);
            additionalNonSigOutput.add(nonSigPValues);
             */
            
            GeneralInfo info = new GeneralInfo();
            
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = alpha;
            info.sigMethod = getSigMethod(significanceMethod);
            info.pValueBasedOn = getPValueBasedOn(isPermut);
            if (isPermut) {
                info.useAllCombs = useAllCombs;
                info.numCombs = numCombs;
            }
            info.function = menu.getFunctionName(function);
            info.hcl = isHierarchicalTree;
            info.hcl_genes = hcl_genes;
            info.hcl_samples = hcl_samples;
            info.hcl_method = hcl_method;
            return createResultTree(result_cluster, info);
            
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progress != null) {
                progress.dispose();
            }
            /*
            if (monitor != null) {
                monitor.dispose();
            }
             */
        }
        
        //return null; //FOR NOW
    }
    
    private String getPValueBasedOn(boolean isPerm) {
        String str = "";
        if (isPerm) {
            str = "permutation";
        } else {
            str = "t-distribution";
        }
        
        return str;
    }
    
    private String getSigMethod(int sigMethod) {
        String methodName = "";
        
        if (sigMethod == TtestInitDialog.JUST_ALPHA) {
            methodName = "Just alpha";
        } else if (sigMethod == TtestInitDialog.STD_BONFERRONI) {
            methodName = "Standard Bonferroni correction";
        } else if (sigMethod == TtestInitDialog.ADJ_BONFERRONI) {
            methodName = "Adjusted Bonferroni correction";
        }
        
        return methodName;
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    private DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("T Tests");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    private void addResultNodes(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        addExpressionImages(root);
        addHierarchicalTrees(root, result_cluster, info);
        addCentroidViews(root);
        addClusterInfo(root);
        addTStatsViews(root);
        addVolcanoPlot(root);
        addGeneralInfo(root, info);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    private void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new TtestExperimentViewer(this.experiment, this.clusters, meansA, meansB, sdA, sdB, pValues, tValues, dfValues);
        for (int i=0; i<this.clusters.length; i++) {
            if (i < this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", expViewer, new Integer(i))));
            } else if (i == this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", expViewer, new Integer(i))));
                
            }
        }
        root.add(node);
    }
    
    /**
     * Adds nodes to display hierarchical trees.
     */
    private void addHierarchicalTrees(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        if (!info.hcl) {
            return;
        }
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Hierarchical Trees");
        NodeList nodeList = result_cluster.getNodeList();
        for (int i=0; i<nodeList.getSize(); i++) {
            if (i < nodeList.getSize() - 1 ) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
            } else if (i == nodeList.getSize() - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
            }
        }
        root.add(node);
    }
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    private IViewer createHCLViewer(Node clusterNode, GeneralInfo info) {
        HCLTreeData genes_result = info.hcl_genes ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = info.hcl_samples ? getResult(clusterNode, info.hcl_genes ? 4 : 0) : null;
        return new HCLViewer(this.experiment, clusterNode.getFeaturesIndexes(), genes_result, samples_result);
    }
    
    /**
     * Returns a hcl tree data from the specified cluster node.
     */
    private HCLTreeData getResult(Node clusterNode, int pos) {
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
    private void addClusterInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results (#,%)", new TtestInfoViewer(this.clusters, this.experiment.getNumberOfGenes()))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        TtestCentroidViewer centroidViewer = new TtestCentroidViewer(this.experiment, clusters, meansA, meansB, sdA, sdB, pValues, tValues, dfValues);
        centroidViewer.setMeans(this.means.A);
        centroidViewer.setVariances(this.variances.A);
        for (int i=0; i<this.clusters.length; i++) {
            
            if (i == 0) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            } else if (i == 1) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                
            }
        }
        
        TtestCentroidsViewer centroidsViewer = new TtestCentroidsViewer(this.experiment, clusters, meansA, meansB, sdA, sdB, pValues, tValues, dfValues);
        centroidsViewer.setMeans(this.means.A);
        centroidsViewer.setVariances(this.variances.A);
        
        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    private void addTStatsViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode tStatsNode = new DefaultMutableTreeNode("Gene Statistics");
        IViewer tSigViewer = new TStatsTableViewer(this.experiment, this.clusters, this.data, meansA, meansB, sdA, sdB, pValues, tValues, dfValues, true);
        IViewer tNonSigViewer = new TStatsTableViewer(this.experiment, this.clusters, this.data, meansA, meansB, sdA, sdB, pValues, tValues, dfValues, false);
        
        tStatsNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes", tSigViewer)));
        tStatsNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes", tNonSigViewer)));
        
        root.add(tStatsNode);
    }
    
    private void addVolcanoPlot(DefaultMutableTreeNode root) {
        //DefaultMutableTreeNode vNode = new DefaultMutableTreeNode("Volcano plot");
        IViewer volcanoPlotViewer = new TTestVolcanoPlotViewer(diffMeansBA, negLog10PValues, isSig);
        root.add(new DefaultMutableTreeNode(new LeafInfo("Volcano Plot", volcanoPlotViewer)));
    }
    
    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(getGroupAssignmentInfo());
        node.add(new DefaultMutableTreeNode("Alpha (overall threshold p-value): "+info.alpha));
        node.add(new DefaultMutableTreeNode("P-values based on: "+info.pValueBasedOn));
        if (isPermutations) {
            node.add(new DefaultMutableTreeNode("All permutations used: " + info.useAllCombs));
            node.add(new DefaultMutableTreeNode("Number of permutations per gene: " + info.numCombs));
        }
        node.add(new DefaultMutableTreeNode("Significance determined by: "+info.sigMethod));
        node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
    }
    
    
    private DefaultMutableTreeNode getGroupAssignmentInfo() {
        DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode("Group assigments ");
        DefaultMutableTreeNode groupA = new DefaultMutableTreeNode("Group A ");
        DefaultMutableTreeNode groupB = new DefaultMutableTreeNode("Group B ");
        DefaultMutableTreeNode neitherGroup = new DefaultMutableTreeNode("Neither group ");
        
        int neitherGroupCounter = 0;
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
                groupA.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
            } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
                groupB.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
            } else {
                neitherGroup.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
                neitherGroupCounter++;
            }
        }
        
        groupAssignmentInfo.add(groupA);
        groupAssignmentInfo.add(groupB);
        if (neitherGroupCounter > 0) {
            groupAssignmentInfo.add(neitherGroup);
        }
        
        return groupAssignmentInfo;
    }
    
    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    private class Listener extends DialogListener implements AlgorithmListener {
        
        public void valueChanged(AlgorithmEvent event) {
            switch (event.getId()) {
                case AlgorithmEvent.SET_UNITS:
                    progress.setUnits(event.getIntValue());
                    progress.setDescription(event.getDescription());
                    break;
                case AlgorithmEvent.PROGRESS_VALUE:
                    progress.setValue(event.getIntValue());
                    progress.setDescription(event.getDescription());
                    break;
                case AlgorithmEvent.MONITOR_VALUE:
                    int value = event.getIntValue();
                    if (value == -1) {
                        //monitor.dispose();
                    } else {
                        //monitor.update(value);
                    }
                    break;
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
                progress.dispose();
                //monitor.dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            progress.dispose();
            //monitor.dispose();
        }
    }
    
    private class GeneralInfo {
        public int clusters;
        public String sigMethod;
        public String pValueBasedOn;
        public double alpha;
        public int numCombs;
        public boolean useAllCombs;
        //public boolean converged;
        //public int iterations;
        //public int userNumClusters;
        public long time;
        public String function;
        //public int numReps;
        //public double thresholdPercent;
        
        private boolean hcl;
        private int hcl_method;
        private boolean hcl_genes;
        private boolean hcl_samples;
        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
}
/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * ScriptRunner.java
 *
 * Created on March 15, 2004, 3:27 PM
 */

package org.tigr.microarray.mev.script.util;

import java.io.File;
import java.awt.Frame;
import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.ResultTree;

import org.tigr.microarray.mev.action.ActionManager;
import org.tigr.microarray.mev.action.AnalysisAction;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;

import org.tigr.microarray.mev.script.Script;
import org.tigr.microarray.mev.script.scriptGUI.ScriptCentroidViewer;
import org.tigr.microarray.mev.script.scriptGUI.ScriptExperimentViewer;

//for testing
import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCGUI;

/**
 *
 * @author  braisted
 */
public class ScriptRunner {
    
    private Script script;
    private ScriptTree scriptTree;
    private Frame parentFrame;
    private ActionManager actionManager;
    private IFramework framework;
    private Hashtable classHash;
    private AlgorithmSet [] algSets;
    /**
     *  Three output modes: internal (0), file (1), external (2)
     *  see <code>ScriptConstants</code> document for constant names.
     */
    private int mode;
    
    /** Creates a new instance of ScriptRunner */
    public ScriptRunner(Script script, ActionManager actionManager, IFramework framework) {
        this.script = script;
        scriptTree = script.getScriptTree();
        this.actionManager = actionManager;
        this.framework = framework;
        mode = ScriptConstants.SCRIPT_OUTPUT_MODE_INTERNAL_OUTPUT;
        parentFrame = framework.getFrame();
        classHash = getClassNames();
    }
    
    public void setOutputMode(int outputMode) {
        mode = outputMode;
    }
    
    public void execute(int outputMode) {
        mode = outputMode;
        Thread thread = new Thread(new Runner());
        thread.start();
    }
    
    public void execute() {
        Thread thread = new Thread(new Runner());
        thread.start();
    }
    
    public DefaultMutableTreeNode execute(AlgorithmSet set) {
        Experiment experiment = set.getExperiment();
        int algCount = set.getAlgorithmCount();
        File outputFile;
        AlgorithmNode algNode;
        AlgorithmData data;
        String algName, algType;
        
        DefaultMutableTreeNode currNode = null, outputNode = null;
        
        System.out.println("Execute AlgSet");
        
        if(mode == ScriptConstants.SCRIPT_OUTPUT_MODE_FILE_OUTPUT) {
            JFileChooser chooser = new JFileChooser(System.getProperty("user.dir")+System.getProperty("file.separator")+"Data");
            if(chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
                outputFile = chooser.getSelectedFile();
            } else {
                return null;
            }
        }
        
        if(algCount > 0) {
            //create a node for the alg set and a data info node;            
        }
        
        for(int i = 0; i < algCount; i++) {
            algNode = set.getAlgorithmNodeAt(i);
            data = algNode.getAlgorithmData();
            algName = algNode.getAlgorithmName();
            algType = algNode.getAlgorithmType();
           // int actionIndex = getActionIndex(algName);
            
            System.out.println("Execute algorithm Name = "+algNode.getAlgorithmName());
            
           // Action action = actionManager.getAction(actionManager.ANALYSIS_ACTION+String.valueOf(actionIndex));
           if(algType.equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER)) {
            
            String className = (String)(this.classHash.get(algName));
            
            System.out.println("Run script "+className);
            
            
            try {
                Class clazz = Class.forName(className);
                IClusterGUI gui = (IClusterGUI)clazz.newInstance();
                currNode = ((KMCGUI)gui).executeScript(framework, data, experiment);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parentFrame, "Can't execute script "+algName+ " algorithm", "Script Parameter Error", JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();
            }
            
            if(currNode != null) {
                if(outputNode == null) {
                    outputNode = new DefaultMutableTreeNode("Results");
                }
                outputNode.add(currNode);
            }
           } else if(algType.equals(ScriptConstants.ALGORITHM_TYPE_ADJUSTMENT)){ 
           //Handle adjustments here
               data.addParam("name", algName);

               ScriptDataTransformer adjuster = new ScriptDataTransformer(experiment);
               Experiment resultExperiment = adjuster.transformData(data);
               
               //Associate result experiment and indices with the output node's result set if it exists.               
               int [][] clusters = new int[1][];
               clusters[0] = getDefaultGeneIndices(resultExperiment.getNumberOfGenes());
               attachResultToChildAlgorithmSets(algNode, resultExperiment, clusters);
               
                if(outputNode == null) {
                    outputNode = new DefaultMutableTreeNode("Results");
                }
               
                DefaultMutableTreeNode resultNode = getViewerNodes(resultExperiment);
                resultNode.setUserObject("Data Adjustment: "+algName);
                outputNode.add(resultNode);
           }
        }
        return outputNode;
    }
    
    private class Runner implements Runnable {
        
        public void run() {
            algSets = scriptTree.getAlgorithmSets();
            DefaultMutableTreeNode currNode, setNode, dataNode, resultNode, scriptNode;
            AlgorithmSet set;
            Experiment experiment;
            boolean haveResult = false;
            DataNode inputNode;
            AlgorithmNode inputAlgNode;
            
            DefaultMutableTreeNode scriptResultNode = new DefaultMutableTreeNode("Script Result");
            
            if(algSets.length > 0) {
                                
            }
            
            for(int i = 0; i < algSets.length; i++) {
                set = algSets[i];
                if(set.getAlgorithmCount() > 0) {
                    
                    resultNode = execute(set);
                    
                    if(resultNode != null) {
                        experiment = set.getExperiment();
                        
                        haveResult = true;
                        setNode = new DefaultMutableTreeNode("Algorithm Set");
                        dataNode = new DefaultMutableTreeNode("Input Data");
                        
                        inputNode = set.getDataNode();
                        inputAlgNode = (AlgorithmNode)(inputNode.getParent());
                        
                        if(inputAlgNode != null) {
                            currNode = new DefaultMutableTreeNode("Algorithm Source: "+ inputAlgNode.getAlgorithmName() +
                            " ["+inputAlgNode.getDataNodeRef()+","+inputAlgNode.getID()+"] ");
                            dataNode.add(currNode);
                        }

                        currNode = new DefaultMutableTreeNode("Input Data Node: "+inputNode.toString());
                        dataNode.add(currNode);
                        currNode = new DefaultMutableTreeNode("Number of Experiments: "+experiment.getNumberOfSamples());
                        dataNode.add(currNode);
                        currNode = new DefaultMutableTreeNode("Number of Genes: "+experiment.getNumberOfGenes());
                        dataNode.add(currNode);
                                                        
                        currNode = getViewerNodes(set.getExperiment());                        
                        dataNode.add(currNode);                        

                        setNode.add(dataNode);
                        setNode.add(resultNode);
                        
                        scriptResultNode.add(setNode);
                    }
                }                
            }
            
            ResultTree tree = framework.getResultTree();
            framework.addNode(tree.getAnalysisNode(), scriptResultNode);
            tree.scrollPathToVisible(new TreePath(((DefaultTreeModel)(tree.getModel())).getPathToRoot(scriptResultNode)));
        }
        
    }
    
    public DefaultMutableTreeNode getViewerNodes(Experiment experiment) {
        DefaultMutableTreeNode viewerNode = new DefaultMutableTreeNode("Input Data Viewers");
        int [][] cluster = new int [1][];
        
        //since it's a single set, contains all indices in experiment.
        cluster[0] = getDefaultGeneIndices(experiment.getNumberOfGenes());

        //Will need to deal with var. exp nums perhaps use exp cluster viewers
                
        DefaultMutableTreeNode currNode = new DefaultMutableTreeNode(new LeafInfo("Expression Image", new ScriptExperimentViewer(experiment, cluster)));
        viewerNode.add(currNode);
        
        FloatMatrix matrix = experiment.getMatrix();
        FloatMatrix means = getMeans(matrix, cluster);
        FloatMatrix vars = getVariances(matrix, means, cluster);  
        
        ScriptCentroidViewer viewer = new ScriptCentroidViewer(experiment, cluster);
        viewer.setMeans(means.A);
        viewer.setVariances(vars.A);
        currNode = new DefaultMutableTreeNode(new LeafInfo("Centroid Graph", viewer, new CentroidUserObject(0,CentroidUserObject.VARIANCES_MODE)));
        viewerNode.add(currNode);
        currNode = new DefaultMutableTreeNode(new LeafInfo("Expression Graph", viewer, new CentroidUserObject(0,CentroidUserObject.VALUES_MODE)));
        viewerNode.add(currNode);

        return viewerNode;
    }
    
    public Hashtable getClassNames() {
        Hashtable hash = new Hashtable();
        int algCnt = 0;
        String algName, className;

        AnalysisAction action;
        
        while ((action = (AnalysisAction)(actionManager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(algCnt))))!=null){
           
            //Name or Short Description??
            
            algName = (String)(action.getValue(Action.NAME));            
            className = (String)(action.getValue(ActionManager.PARAMETER));

            hash.put(algName, className);
            algCnt++;                
        }       
        return hash;
    }
    
    
        /**
     *  Calculates means for the clusters
     */
    private FloatMatrix getMeans(FloatMatrix data, int [][] clusters){
        FloatMatrix means = new FloatMatrix(clusters.length, data.getColumnDimension());
        for(int i = 0; i < clusters.length; i++){
            means.A[i] = getMeans(data, clusters[i]);
        }
        return means;
    }
    
    
    /**
     *  Returns a set of means for an element
     */
    private float [] getMeans(FloatMatrix data, int [] indices){
        int nSamples = data.getColumnDimension();
        float [] means = new float[nSamples];
        float sum = 0;
        float n = 0;
        float value;
        for(int i = 0; i < nSamples; i++){
            n = 0;
            sum = 0;
            for(int j = 0; j < indices.length; j++){
                value = data.get(indices[j],i);
                if(!Float.isNaN(value)){
                    sum += value;
                    n++;
                }
            }
            if(n > 0)
                means[i] = sum/n;
            else
                means[i] = Float.NaN;
        }
        return means;
    }
    
    /**
     * Returns a matrix of standard deviations grouped by cluster and element
     */
    private FloatMatrix getVariances(FloatMatrix data, FloatMatrix means, int [][] clusters){
        int nSamples = data.getColumnDimension();
        FloatMatrix variances = new FloatMatrix(clusters.length, nSamples);
        for(int i = 0; i < clusters.length; i++){
            variances.A[i] = getVariances(data, means, clusters[i], i);
        }
        return variances;
    }
    
    private float [] getVariances(FloatMatrix data, FloatMatrix means, int [] indices, int clusterIndex){
        int nSamples = data.getColumnDimension();
        float [] variances = new float[nSamples];
        float sse = 0;
        float mean;
        float value;
        int n = 0;
        for(int i = 0; i < nSamples; i++){
            mean = means.get(clusterIndex, i);
            n = 0;
            sse = 0;
            for(int j = 0; j < indices.length; j++){
                value = data.get(indices[j], i);
                if(!Float.isNaN(value)){
                    sse += (float)Math.pow((value - mean),2);
                    n++;
                }
            }
            if(n > 1)
                variances[i] = (float)Math.sqrt(sse/(n-1));
            else
                variances[i] = 0.0f;
        }
        return variances;
    }
    
    private int [] getDefaultGeneIndices(int length) {
            int [] indices = new int[length];
        for(int i = 0; i < indices.length; i++)
            indices[i] = i;
            
            return indices;
    }
    
   private void attachResultToChildAlgorithmSets(AlgorithmNode algNode, Experiment experiment, int [][] clusters) {
        //get data ouput nodes
       int outputCount = algNode.getChildCount();
       DataNode dataNode;
       for(int i = 0; i < outputCount; i++) {
            dataNode = ((DataNode)algNode.getChildAt(i));
            for( int j = 0; j < algSets.length; j++) {
                if(dataNode == algSets[j].getDataNode()) {
                    //if it's not multicluster ouput then append the propper experiment
                    if(!dataNode.getDataOutputClass().equals(ScriptConstants.OUTPUT_DATA_CLASS_MULTICLUSTER_OUTPUT)) {
                        if( i < clusters.length ) {                         
                            setExperiment(algSets[j], experiment, clusters[i]);                         
                        }
                    }
                    
                    //if it IS multicluster output then the next algorithm must be for
                    //cluster selection.  This algorithm will require clusters[][] for selection process                    
                    else {
                        setExperimentAndClusters(algSets[i], experiment, clusters);
                    }
                    
                }
            }
       }
   }
 
   private void setExperiment(AlgorithmSet algSet, Experiment experiment, int [] indices) {
        ScriptDataTransformer transformer = new ScriptDataTransformer(experiment);
        Experiment trimmedExperiment = transformer.getTrimmedExperiment(indices);
        algSet.setExperiment(trimmedExperiment);
   }
   
   
   private void setExperimentAndClusters(AlgorithmSet algSet, Experiment experiment, int [][] clusters) {
       
   }
    
    
}

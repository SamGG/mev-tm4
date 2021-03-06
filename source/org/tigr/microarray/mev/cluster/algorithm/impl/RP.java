/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RP.java,v $
 * $Revision: 1.8 $
 * $Date: 2008-8-28 17:27:39 $
 * $Author: dschlauch $
 * $State: Exp $
 */


package org.tigr.microarray.mev.cluster.algorithm.impl;


import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.gui.impl.rp.RPInitBox;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class RP extends AbstractAlgorithm{
    public static final int FALSE_NUM = 12;
    public static final int FALSE_PROP = 13;  
    
    private int progress;
    private int totalProgressStep2;
    private FloatMatrix expMatrix;
    private FloatMatrix experimentData;
    private FloatMatrix dataGroupA;
    private FloatMatrix dataGroupB;
    private int[] groupAAss, groupBAss;
    private float alpha;
    private boolean stop = false;
    private int[] inGroupAssignments;
    private int[][] sigGenesArrays;

    double[] rankProductArrayDown;
    double[] rankProductArrayUp;
    private int numGenes, numExps;
    private float falseProp;
    private boolean  drawSigTreesOnly;
    private int numPerms, falseNum;
    private int correctionMethod;    
    private int hcl_function;
    private int upDown;
    private int testDesign;
    private boolean hcl_absolute;
    private boolean hcl_genes_ordered;  
    private boolean hcl_samples_ordered; 
    private boolean doUp,doDown; 
	private float[] qValsDown;
	private float[] qValsUp;
	private float[] pValsDown;
	private float[] pValsUp;
    
    private AlgorithmEvent event;
    /**
     * This method should interrupt the calculation.
     */
    public void abort() {
        stop = true;        
    }
    /**
     * This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
    	AlgorithmParameters map = data.getParams();
        testDesign = map.getInt("classes",0);
    	expMatrix = data.getMatrix("experiment");
    	if (testDesign==RPInitBox.ONE_CLASS||testDesign==RPInitBox.TWO_CLASS){
	    	inGroupAssignments = data.getIntArray("group_assignments");
	    	int q =0;
	    	for (int i=0; i<inGroupAssignments.length; i++){
	    		if (inGroupAssignments[i]!=0)
	    			q++;
	    	}
	    	int[] groupAssignments=new int[q];
	    	q=0;
	    	for (int i=0; i<inGroupAssignments.length; i++){
	    		if (inGroupAssignments[i]!=0){
	    			groupAssignments[q]=i;
	    			q++;
	    		}
	    	}
	    	experimentData = expMatrix.getMatrix(0, expMatrix.getRowDimension()-1, groupAssignments);
    	}
    	
        numPerms = map.getInt("numPerms", 0);
        upDown = map.getInt("UpOrDown",0);
        doUp=true;
        doDown=true;
        if (upDown==1)
        	doDown=false;
        if (upDown==2)
        	doUp=false;
        
        
        if (testDesign ==RPInitBox.PAIRED){
        	experimentData = expMatrix.getMatrix(0, expMatrix.getRowDimension()-1, data.getIntArray("groupB_paired_assignments")).
    			minus(expMatrix.getMatrix(0, expMatrix.getRowDimension()-1, data.getIntArray("groupA_paired_assignments")));
        }
        
        if (testDesign ==RPInitBox.TWO_CLASS){
        	int r=0;
        	for (int i=0; i<inGroupAssignments.length; i++){
        		if (inGroupAssignments[i]==1)
        			r++;
        	}
        	groupAAss=new int[r];
        	r=0;
        	for (int i=0; i<inGroupAssignments.length; i++){
        		if (inGroupAssignments[i]==1){
        			groupAAss[r]=i;
        			r++;
        		}
        	}
        	dataGroupA = expMatrix.getMatrix(0, expMatrix.getRowDimension()-1, groupAAss);
        	
        	int s=0;
        	for (int i=0; i<inGroupAssignments.length; i++){
        		if (inGroupAssignments[i]==2)
        			s++;
        	}
        	groupBAss=new int[s];
        	s=0;
        	for (int i=0; i<inGroupAssignments.length; i++){
        		if (inGroupAssignments[i]==2){
        			groupBAss[s]=i;
        			s++;
        		}
        	}
        	dataGroupB = expMatrix.getMatrix(0, expMatrix.getRowDimension()-1, groupBAss);
        }
        correctionMethod = map.getInt("correction-method");
        if (correctionMethod == RPInitBox.FALSE_NUM)
        	falseNum = map.getInt("falseNum");
        if (correctionMethod == RPInitBox.FALSE_PROP)
        	falseProp = map.getFloat("falseProp");
        hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
        hcl_absolute = map.getBoolean("hcl-distance-absolute", false);     
        hcl_genes_ordered = map.getBoolean("hcl-genes-ordered", false);  
        hcl_samples_ordered = map.getBoolean("hcl-samples-ordered", false);     
        numGenes= experimentData.getRowDimension();
        rankProductArrayDown = new double[numGenes];
        rankProductArrayUp = new double[numGenes];
        numExps = expMatrix.getColumnDimension();
        alpha = map.getFloat("alpha");
    	boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
            if (hierarchical_tree) {
                drawSigTreesOnly = map.getBoolean("draw-sig-trees-only");
            }        
    	int method_linkage = map.getInt("method-linkage", 0);
    	boolean calculate_genes = map.getBoolean("calculate-genes", false);
    	boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
    	
    	progress=0;
    	event = null;
    	event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Permuting Random Data...");
    	// set progress limit
    	fireValueChanged(event);
    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
    	event.setIntValue(0);
    	fireValueChanged(event);
    	
    	if (testDesign==RPInitBox.ONE_CLASS||testDesign==RPInitBox.PAIRED)
    		runOneClassAlg();
    	else
    		runTwoClassAlg();
    	for (int i=0; i<3; i++){
    		if (sigGenesArrays[i]==null)
    			sigGenesArrays[i] = new int[0];
    	}
    	AlgorithmData result = new AlgorithmData();
    	FloatMatrix means =getMeans(sigGenesArrays);        
    	
    	FloatMatrix variances = getVariances(sigGenesArrays, means); 
    	Cluster result_cluster = new Cluster();
    	NodeList nodeList = result_cluster.getNodeList();
    	int[] features;        
    	for (int i=0; i<sigGenesArrays.length; i++) {
    	    if (stop) {
    	    	throw new AbortException();
    	    }
    	    features = sigGenesArrays[i];
    	    Node node = new Node(features);
    	    nodeList.addNode(node);
    	    if (hierarchical_tree) {
                if (drawSigTreesOnly) {
                    if ((i == 0)||(i == 1)) {
                        node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
                        event.setIntValue(i+1);
                        fireValueChanged(event);                       
                    }                    
                } else {                
                    node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
                    event.setIntValue(i+1);
                    fireValueChanged(event);
                }                
    	    }
    	}

    	if (testDesign==RPInitBox.TWO_CLASS)
            result.addMatrix("fold_change", getFoldChange());
    	result.addIntMatrix("sigGenesArrays", sigGenesArrays);
    	result.addCluster("cluster", result_cluster);
    	result.addParam("number-of-clusters", "1"); 
    	result.addMatrix("clusters_means", means);
    	result.addMatrix("clusters_variances", variances); 
        result.addMatrix("geneTimeMeansMatrix", getAllGeneTimeMeans());
        result.addMatrix("geneTimeSDsMatrix", getAllGeneTimeSDs());   
        if (doDown){
        	result.addMatrix("pValuesDown", getPValuesDown());
        	result.addMatrix("qValuesDown", getQValuesDown());
        	result.addMatrix("RPValsDown", getRPValsDown());
        }
        if (doUp){
        	result.addMatrix("pValuesUp", getPValuesUp());
        	result.addMatrix("qValuesUp", getQValuesUp());
        	result.addMatrix("RPValsUp", getRPValsUp());
        }
    	return result;   
    }
    

    private FloatMatrix getSubExperiment(FloatMatrix experiment, int[] features) {
    	FloatMatrix subExperiment = new FloatMatrix(features.length, experiment.getColumnDimension());
    	for (int i=0; i<features.length; i++) {
    	    subExperiment.A[i] = experiment.A[features[i]];
    	}
    	return subExperiment;
    }
    
    /**
     * Checking the result of hcl algorithm calculation.
     * @throws AlgorithmException, if the result is incorrect.
     */
    private void validate(AlgorithmData result) throws AlgorithmException {
		if (result.getIntArray("child-1-array") == null) {
		    throw new AlgorithmException("parameter 'child-1-array' is null");
		}
		if (result.getIntArray("child-2-array") == null) {
		    throw new AlgorithmException("parameter 'child-2-array' is null");
		}
		if (result.getIntArray("node-order") == null) {
		    throw new AlgorithmException("parameter 'node-order' is null");
		}
		if (result.getMatrix("height") == null) {
		    throw new AlgorithmException("parameter 'height' is null");
		}
    }
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
    	NodeValueList nodeList = new NodeValueList();
    	AlgorithmData data = new AlgorithmData();
    	FloatMatrix experiment = getSubExperiment(expMatrix, features);
    	data.addMatrix("experiment", experiment);
            data.addParam("hcl-distance-function", String.valueOf(this.hcl_function));
            data.addParam("hcl-distance-absolute", String.valueOf(this.hcl_absolute));
    	data.addParam("method-linkage", String.valueOf(method));
    	HCL hcl = new HCL();
    	AlgorithmData result;
    	if (genes) {
    	    data.addParam("calculate-genes", String.valueOf(true));
    	    data.addParam("optimize-gene-ordering", String.valueOf(hcl_genes_ordered));
    	    result = hcl.execute(data);
    	    validate(result);
    	    addNodeValues(nodeList, result);
    	}
    	if (experiments) {
    	    data.addParam("calculate-genes", String.valueOf(false));
    	    data.addParam("optimize-sample-ordering", String.valueOf(hcl_samples_ordered));
    	    result = hcl.execute(data);
    	    validate(result);
    	    addNodeValues(nodeList, result);
    	}
    	return nodeList;
    }
    
    private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
    	target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
    	target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
    	target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
    	target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
        }
    private FloatMatrix getMeans(int[][] clusters) {
    	if (upDown==3){
	    	FloatMatrix means = new FloatMatrix(clusters.length, numExps);
	    	FloatMatrix mean;
	    	for (int i=0; i<clusters.length; i++) {
	    	    mean = getMean(clusters[i]);
	    	    means.A[i] = mean.A[0];
	    	}
	    	return means;
    	}
    	if (upDown==2){
        	FloatMatrix means = new FloatMatrix(2, numExps);
        	FloatMatrix mean;
        	
        	mean = getMean(clusters[0]);
        	means.A[0] = mean.A[0];
        	mean = getMean(clusters[2]);
        	means.A[1] = mean.A[0];
        	return means;
        }
    	if (upDown==1){
        	FloatMatrix means = new FloatMatrix(2, numExps);
        	FloatMatrix mean;
        	
        	mean = getMean(clusters[1]);
        	means.A[0] = mean.A[0];
        	mean = getMean(clusters[2]);
        	means.A[1] = mean.A[0];
        	return means;
        }
    	return null;
    }
    
    private FloatMatrix getMean(int[] cluster) {
    	FloatMatrix mean = new FloatMatrix(1, numExps);
    	float currentMean;
    	int n = cluster.length;
    	int denom = 0;
    	float value;
    	for (int i=0; i<numExps; i++) {
    	    currentMean = 0f;
    	    denom = 0;
    	    for (int j=0; j<n; j++) {
    		value = expMatrix.get(((Integer) cluster[j]).intValue(), i);
    		if (!Float.isNaN(value)) {
    		    currentMean += value;
    		    denom++;
    		}
    	    }
    	    mean.set(0, i, currentMean/(float)denom);
    	}

    	return mean;
    }
    
    private FloatMatrix getVariances(int[][] clustersinit, FloatMatrix means) {
    	int[][]clusters=clustersinit;
    	if (upDown==2){
    		clusters = new int[2][];
    		clusters[0] = clustersinit[0];
    		clusters[1] = clustersinit[2];
    	}
    	if (upDown==1){
    		clusters = new int[2][];
    		clusters[0] = clustersinit[1];
    		clusters[1] = clustersinit[2];    		
    	}
    	final int rows = means.getRowDimension();
    	final int columns = means.getColumnDimension();
    	FloatMatrix variances = new FloatMatrix(rows, columns);
    	for (int row=0; row<rows; row++) {
    	    for (int column=0; column<columns; column++) {
    		variances.set(row, column, getSampleVariance(clusters[row], column, means.get(row, column)));
    	    }
    	}
    	return variances;
    }
    int validN;
    private float getSampleVariance(int[] cluster, int column, float mean) {
    	return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
    	
    }  
    private float getSampleNormalizedSum(int[] cluster, int column, float mean) {
    	final int size = cluster.length;
    	float sum = 0f;
    	float value;
    	validN = 0;
    	for (int i=0; i<size; i++) {
    	    value = expMatrix.get(((Integer) cluster[i]).intValue(), column);
    	    if (!Float.isNaN(value)) {
    		sum += Math.pow(value-mean, 2);
    		validN++;
    	    }
    	}
    	return sum;
    }
    
    private float[] getGeneGroupMeans(int gene) {     
    	float[] geneValues = new float[numExps];
        for (int i = 0; i < numExps; i++) {
	    geneValues[i] = expMatrix.A[gene][i];
        } 
        
        float[][] geneValuesByGroups = new float[1][];
        
        for (int i = 0; i < 1; i++) {
            geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
        } 
        
        float[] geneGroupMeans = new float[1];
        for (int i = 0; i < 1; i++) {
            geneGroupMeans[i] = getMean(geneValuesByGroups[i]);
        }
        
        return geneGroupMeans;
    }
    
    private float[] getGeneGroupSDs(int gene) {
    	float[] geneValues = new float[numExps];        
        for (int i = 0; i < numExps; i++) {
        	geneValues[i] = expMatrix.A[gene][i];
        }   
        
        float[][] geneValuesByGroups = new float[1][];
        
        for (int i = 0; i < 1; i++) {
            geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
        }  
        
        float[] geneGroupSDs = new float[1];
        for (int i = 0; i < 1; i++) {
            geneGroupSDs[i] = getStdDev(geneValuesByGroups[i]);
        }
        return geneGroupSDs;        
    }
    
    private float[] getGeneValuesForGroup(float[] geneValues, int group) {
        Vector<Float> groupValuesVector = new Vector<Float>();
        
        for (int i = 0; i < geneValues.length; i++) {
            groupValuesVector.add(new Float(geneValues[i]));
        }
        
        float[] groupGeneValues = new float[groupValuesVector.size()];
        
        for (int i = 0; i < groupValuesVector.size(); i++) {
            groupGeneValues[i] = ((Float)(groupValuesVector.get(i))).floatValue();
        }
        
        return groupGeneValues;
    }
    
    private FloatMatrix getAllGeneTimeMeans() {
        FloatMatrix means = new FloatMatrix(numGenes, 1);
        for (int i = 0; i < means.getRowDimension(); i++) {
            means.A[i] = getGeneGroupMeans(i);
        }
        return means;
    }
    
    private FloatMatrix getAllGeneTimeSDs() {
        FloatMatrix sds = new FloatMatrix(numGenes, 1);
        for (int i = 0; i < sds.getRowDimension(); i++) {
            sds.A[i] = getGeneGroupSDs(i);
        }
        return sds;        
    }
    
    private FloatMatrix getPValuesDown(){
    	
    	FloatMatrix pvals = new FloatMatrix(numGenes, 1);
    	for (int i=0; i<pvals.getRowDimension(); i++){
    		pvals.A[i][0] =pValsDown[i]; 
    	}
    	return pvals;
    }
    
    private FloatMatrix getQValuesDown(){
    	FloatMatrix pvals = new FloatMatrix(numGenes, 1);
    	for (int i=0; i<pvals.getRowDimension(); i++){
    		pvals.A[i][0] =qValsDown[i]; 
    	}
    	return pvals;
    }
    
	private FloatMatrix getRPValsUp(){
	    	
    	FloatMatrix rpvals = new FloatMatrix(numGenes, 1);
    	for (int i=0; i<rpvals.getRowDimension(); i++){
    		Double db = new Double(rankProductArrayUp[i]);
    		rpvals.A[i][0] =db.floatValue(); 
    	}
    	return rpvals;
    }
	private FloatMatrix getRPValsDown(){
		
		FloatMatrix rpvals = new FloatMatrix(numGenes, 1);
		for (int i=0; i<rpvals.getRowDimension(); i++){
			Double db = new Double(rankProductArrayDown[i]);
    		rpvals.A[i][0] =db.floatValue();
		}
		return rpvals;
	}
    private FloatMatrix getPValuesUp(){
    	
    	FloatMatrix pvals = new FloatMatrix(numGenes, 1);
    	for (int i=0; i<pvals.getRowDimension(); i++){
    		pvals.A[i][0] =pValsUp[i]; 
    	}
    	return pvals;
    }
    
    private FloatMatrix getQValuesUp(){
    	FloatMatrix pvals = new FloatMatrix(numGenes, 1);
    	for (int i=0; i<pvals.getRowDimension(); i++){
    		pvals.A[i][0] =qValsUp[i]; 
    	}
    	return pvals;
    }
    
    private float getMean(float[] group) {
    	float sum = 0;
    	int n = 0;
    	
    	for (int i = 0; i < group.length; i++) {
    	    if (!Float.isNaN(group[i])) {
    		sum = sum + group[i];
    		n++;
    	    }
    	}
    	if (n == 0) {
            return Float.NaN;
        }
    	float mean =  sum / (float)n;
        if (Float.isInfinite(mean)) {
            return Float.NaN;
        }
            
    	return mean;
    }
    
    private float getStdDev(float[] group) {
		float mean = getMean(group);
		int n = 0;
		float sumSquares = 0;
		for (int i = 0; i < group.length; i++) {
		    if (!Float.isNaN(group[i])) {
				sumSquares = (float)(sumSquares + Math.pow((group[i] - mean), 2));
				n++;
		    }
		}
        if (n < 2) {
            return Float.NaN;
        }
		float var = sumSquares / (float)(n - 1);
		if (Float.isInfinite(var)) {
            return Float.NaN;
        }
		return (float)(Math.sqrt((double)var));
    }    


	public void runOneClassAlg(){
		double[][] adata = new double[experimentData.getColumnDimension()][experimentData.getRowDimension()];
		for (int i=0; i<experimentData.getColumnDimension(); i++){
			for (int j=0; j<experimentData.getRowDimension(); j++){
				adata[i][j] = experimentData.get(j, i);
			}
		}
		double[] rankArray = new double[adata[0].length];
		for (int i=0; i<rankArray.length; i++){
			rankArray[i]=i;
		}
	    for (int i=0; i< (adata.length); i++){
	    	ExperimentUtil.sort2(adata[i], rankArray);
	    	
	    	for (int j=0; j<adata[i].length; j++){
	    		adata[i][j]=j+1.0;
	    	}
	    	ExperimentUtil.sort2(rankArray, adata[i]);
	    }
	    rankProductArrayDown = new double[rankArray.length];
	    rankProductArrayUp = new double[rankArray.length];
	    for (int i = 0; i<rankProductArrayDown.length; i++){
	    	double rankProductDown = 1;
	    	double rankProductUp = 1;
	    	if (doDown){
		    	for (int j = 0; j<adata.length; j++){
		    		rankProductDown = rankProductDown*adata[j][i];
		    	}
		    	rankProductArrayDown[i] = (Math.pow(rankProductDown, 1/(double)adata.length));
		    	rankProductDown = 1;
	    	}
	    	if (doUp){
	    		for (int j = 0; j<adata.length; j++){
		    		rankProductUp = rankProductUp*(adata[j].length+1 - adata[j][i]);
		    	}
		    	rankProductArrayUp[i] = (Math.pow(rankProductUp, 1/(double)adata.length));
		    	rankProductUp = 1;
	    	}
	    }
	    
	    
	    
	    
	    
	    //begin permutation-based estimation
	    double[][]rankProductMatrix = new double[numPerms][adata[0].length];
	    for (int permutations=0; permutations<numPerms; permutations++){
	    	if (stop)
	    		break;
	    	
	    	updateProgressBar();
		    int[][] intRanksMatrix = new int[adata.length][];
		    int[][] permutedRanksMatrix = new int[adata.length][];
		    for (int i=0; i<adata.length; i++){
			    	intRanksMatrix[i] = new int[adata[i].length];
			    	permutedRanksMatrix[i] = new int[adata[i].length];
		    	for (int j=0; j<adata[i].length; j++){
		    		
			    	intRanksMatrix[i][j] = (int)adata[i][j]-1;
			    }
		    }
		    for (int i=0; i<adata.length; i++){
		    	permutedRanksMatrix[i] = getPermutedValues(adata[i].length, intRanksMatrix[i]);
		    }
		    
		    
		    for (int i = 0; i<rankProductMatrix[permutations].length; i++){
		    	double permutedRankProduct = 1;
		    	for (int j = 0; j<adata.length; j++){
		    		permutedRankProduct = permutedRankProduct*(permutedRanksMatrix[j][i]+1);
		    	}
		    	rankProductMatrix[permutations][i] = (Math.pow(permutedRankProduct, 1/(double)adata.length));
		    	permutedRankProduct = 1;
		    }
		    Arrays.sort(rankProductMatrix[permutations]);
	    }

	    event.setDescription("Finding q-values");
	    progress=0;
	    
	    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Finding q-values...");
    	// set progress limit
    	fireValueChanged(event);
    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
    	event.setIntValue(0);
    	fireValueChanged(event);
	    
	    double[] expectedPDown = new double[adata[0].length];
	    double[] expectedPUp = new double[adata[0].length];
	    int totalC=0;
	    totalProgressStep2 = expectedPDown.length;
	    
	   
	    if (doDown){
		    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Finding Down Regulated q-values...");
	    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	    	event.setIntValue(0);
		    for (int h=0; h<expectedPDown.length;h++){
		    	totalC=0;
		    	updateProgressBar2();
			    for (int i=0; i<rankProductMatrix.length; i++){
			    		int c;
			    		c=-Arrays.binarySearch(rankProductMatrix[i], rankProductArrayDown[h])-1;
			    		if (c<0)
				    		c=Arrays.binarySearch(rankProductMatrix[i], rankProductArrayDown[h]);
			    		totalC=totalC+c;			    					    	
			    }
			    expectedPDown[h]=(double)totalC/numPerms;
		    }
	    }
	    if (doUp){
		    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Finding Up Regulated q-values...");
	    	progress=0;
	    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	    	event.setIntValue(0);
	    	for (int h=0; h<expectedPUp.length;h++){
		    	totalC=0;
		    	updateProgressBar2();
			    for (int i=0; i<rankProductMatrix.length; i++){
			    	int c = -Arrays.binarySearch(rankProductMatrix[i], rankProductArrayUp[h])-1;
		    		if (c<0)
			    		c=Arrays.binarySearch(rankProductMatrix[i], rankProductArrayUp[h]);
		    		totalC=totalC+c;
			    }
			    expectedPUp[h]=(double)totalC/numPerms;
		    }
	    }
	    sigGenesArrays = new int[3][];
	    if (doDown){
		    double[] qValuesDown = new double[expectedPDown.length];
		    int[] rankg = new int[expectedPDown.length];

		    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Determining Down Regulated False Discovery Rate...");
	    	progress=0;
	    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	    	event.setIntValue(0);
	    	double[] sortedPDown = new double[expectedPDown.length];
	    	for (int i=0; i<expectedPDown.length; i++){
	    		sortedPDown[i]=expectedPDown[i];
	    	}
	    	Arrays.sort(sortedPDown);
		    for(int i=0; i<qValuesDown.length;i++){
		    	if (stop)
		    		break;
		    	updateProgressBar2();
		    	int rankTotal = Arrays.binarySearch(sortedPDown, expectedPDown[i])+1;
		    	
		    	rankg[i]=rankTotal;
		    }
		    for(int i=0; i<qValuesDown.length;i++){
		    	qValuesDown[i] = expectedPDown[i]/rankg[i];
		    }

	 		qValsDown =new float[qValuesDown.length];
	 		for (int i=0; i<qValsDown.length; i++){
	 			qValsDown[i]=(float)qValuesDown[i];
	 		}

	 		pValsDown=new float[qValuesDown.length];
	 		for (int i=0; i<pValsDown.length; i++){
	 			pValsDown[i]=(float)expectedPDown[i]/this.numGenes;
	 		}

		    int sigGenesDownCounter=0;
		    if (correctionMethod == RPInitBox.JUST_ALPHA){
	
			    for (int i=0; i<rankg.length; i++){
			    	if(pValsDown[i]<=alpha)
			    		sigGenesDownCounter++;
			    }
		    }

		    if (correctionMethod == RPInitBox.FALSE_PROP){
			    for (int i=0; i<rankg.length; i++){
			    	if(qValsDown[i]<=falseProp)
			    		sigGenesDownCounter++;
			    }
		    }

		    if (correctionMethod == RPInitBox.FALSE_NUM){
			    for (int i=0; i<rankg.length; i++){
			    	if(expectedPDown[i]<=falseNum)
			    		sigGenesDownCounter++;
			    }
		    }

		    sigGenesArrays[0] = new int[sigGenesDownCounter];
	 		int counters=0;
	
		    if (correctionMethod == RPInitBox.JUST_ALPHA){
		    	for (int i=0; i<rankg.length; i++){
			    	if(pValsDown[i]<=alpha){
			    		sigGenesArrays[0][counters] = i;
			    		counters++;
			    	}
			    }
		    }

		    if (correctionMethod == RPInitBox.FALSE_PROP){
				for (int i=0; i<rankg.length; i++){
			    	if(qValuesDown[i]<=falseProp){
			    		sigGenesArrays[0][counters] = i;
			    		counters++;
			    	}
			    }
		    }

		    if (correctionMethod == RPInitBox.FALSE_NUM){
		    	for (int i=0; i<rankg.length; i++){
			    	if(expectedPDown[i]<=falseNum){
			    		sigGenesArrays[0][counters] = i;
			    		counters++;
			    	}
			    }
		    }
	    }
	    if (doUp){
	    	 double[] qValuesUp = new double[expectedPUp.length];
		    int[] rankg = new int[expectedPUp.length];
		    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Determining Up Regulated False Discovery Rate...");
	    	progress=0;
	    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	    	event.setIntValue(0);
	    	
	    	double[] sortedPUp = new double[expectedPUp.length];
	    	for (int i=0; i<expectedPUp.length; i++){
	    		sortedPUp[i]=expectedPUp[i];
	    	}
	    	Arrays.sort(sortedPUp);
		    for(int i=0; i<qValuesUp.length;i++){
		    	if (stop)
		    		break;
		    	updateProgressBar2();
		    	int rankTotal = Arrays.binarySearch(sortedPUp, expectedPUp[i])+1;
		    	
		    	rankg[i]=rankTotal;
		    }
		    
	    	
		    for(int i=0; i<qValuesUp.length;i++){
		    	qValuesUp[i] = expectedPUp[i]/rankg[i];
		    }
	
	 		qValsUp =new float[qValuesUp.length];
	 		for (int i=0; i<qValsUp.length; i++){
	 			qValsUp[i]=(float)qValuesUp[i];
	 		}
	 		pValsUp=new float[qValuesUp.length];
	 		for (int i=0; i<pValsUp.length; i++){
	 			pValsUp[i]=(float)expectedPUp[i]/this.numGenes;
	 		}
		    int sigGenesUpCounter=0;
		    if (correctionMethod == RPInitBox.JUST_ALPHA){
	
			    for (int i=0; i<rankg.length; i++){
			    	if(pValsUp[i]<=alpha)
			    		sigGenesUpCounter++;
			    }
		    }
		    if (correctionMethod == RPInitBox.FALSE_PROP){
			    for (int i=0; i<rankg.length; i++){
			    	if(qValsUp[i]<=falseProp)
			    		sigGenesUpCounter++;
			    }
		    }
		    if (correctionMethod == RPInitBox.FALSE_NUM){
			    for (int i=0; i<rankg.length; i++){
			    	if(expectedPUp[i]<=falseNum)
			    		sigGenesUpCounter++;
			    }
		    }


		    sigGenesArrays[1] = new int[sigGenesUpCounter];
	 		int counters=0;
	
		    if (correctionMethod == RPInitBox.JUST_ALPHA){
		    	for (int i=0; i<rankg.length; i++){
			    	if(pValsUp[i]<=alpha){
			    		sigGenesArrays[1][counters] = i;
			    		counters++;
			    	}
			    }
		    }
		    if (correctionMethod == RPInitBox.FALSE_PROP){
				for (int i=0; i<rankg.length; i++){
			    	if(qValuesUp[i]<=falseProp){
			    		sigGenesArrays[1][counters] = i;
			    		counters++;
			    	}
			    }
		    }
		    if (correctionMethod == RPInitBox.FALSE_NUM){
		    	for (int i=0; i<rankg.length; i++){
			    	if(expectedPUp[i]<=falseNum){
			    		sigGenesArrays[1][counters] = i;
			    		counters++;
			    	}
			    }
		    }
	    }
	    //Get the remaining non-significant genes
	    ArrayList<Integer> insigGenes = new ArrayList<Integer>();
	    for (int i=0; i<rankArray.length; i++){
	    	boolean cont = false;
	    	if (doDown){
		    	for (int j=0; j<sigGenesArrays[0].length; j++){
		    		if (i==sigGenesArrays[0][j])
		    			cont=true;
		    	}
	    	}
	    	if (doUp){
		    	for (int j=0; j<sigGenesArrays[1].length; j++){
		    		if (i==sigGenesArrays[1][j])
		    			cont=true;
		    	}
	    	}
	    	if (cont)
	    		continue;
	    	insigGenes.add(i);
	    	
	    }
	    
	    sigGenesArrays[2] = new int[insigGenes.size()];
	    for (int i=0; i<sigGenesArrays[2].length; i++){
	    	sigGenesArrays[2][i]=insigGenes.get(i);
	    }
	}
	
	public void runTwoClassAlg(){
		int numOfPairs = dataGroupA.getColumnDimension()*dataGroupB.getColumnDimension();
		int numGroupA = dataGroupA.getColumnDimension();
		int numGroupB = dataGroupB.getColumnDimension();
		double[][] adata = new double[numOfPairs][numGenes];
		int col=0;
		for (int i=0; i<numGroupA; i++){
			for (int j=0; j<numGroupB; j++){
				for (int k=0; k<numGenes; k++){
					adata[col][k] = dataGroupB.get(k, j)-dataGroupA.get(k, i);
				}
				col++;
			}
		}
		double[] rankArray = new double[numGenes];
		for (int i=0; i<rankArray.length; i++){
			rankArray[i]=i;
		}
	    for (int i=0; i< (adata.length); i++){
	    	ExperimentUtil.sort2(adata[i], rankArray);
	    	
	    	for (int j=0; j<adata[i].length; j++){
	    		adata[i][j]=j+1.0;
	    	}
	    	ExperimentUtil.sort2(rankArray, adata[i]);
	    }
	    for (int i = 0; i<numGenes; i++){
	    	double[] rankProductDownArrayTemp = new double[numGroupA];
			for (int num=0; num<numGroupA; num++){
				rankProductDownArrayTemp[num]=1;
			}
	    	if (doDown){
	    		int sampleNum=0;
	    		for (int a=0; a<numGroupA; a++){
	    			for (int b=0; b<numGroupB; b++){
	    				rankProductDownArrayTemp[a] = rankProductDownArrayTemp[a]*adata[sampleNum][i]/numGenes;
	    				sampleNum++;
	    			}
	    			rankProductDownArrayTemp[a] = Math.pow(rankProductDownArrayTemp[a], 1f/(float)numGroupB);
		    	}
	    		double rankProductDownTemp = 1;
	    		for (int c=0; c<rankProductDownArrayTemp.length; c++){
	    			rankProductDownTemp=rankProductDownArrayTemp[c]*rankProductDownTemp;
	    		}
		    	rankProductArrayDown[i] = numGenes*Math.pow(rankProductDownTemp, 1f/(float)rankProductDownArrayTemp.length);
	    	}

//	    	double rankProductUp = 1;
	    	double[] rankProductUpArrayTemp = new double[numGroupA];
			for (int num=0; num<numGroupA; num++){
				rankProductUpArrayTemp[num]=1;
			}
	    	if (doUp){

	    		int sampleNum=0;
	    		for (int a=0; a<numGroupA; a++){
	    			for (int b=0; b<numGroupB; b++){
	    				rankProductUpArrayTemp[a] = rankProductUpArrayTemp[a]*(adata[sampleNum].length+1 - adata[sampleNum][i])/numGenes;
	    				sampleNum++;
	    			}
	    			rankProductUpArrayTemp[a] = Math.pow(rankProductUpArrayTemp[a], 1f/(float)numGroupB);
		    	}
	    		double rankProductUpTemp = 1;
	    		for (int c=0; c<rankProductUpArrayTemp.length; c++){
	    			rankProductUpTemp=rankProductUpArrayTemp[c]*rankProductUpTemp;
	    		}
		    	rankProductArrayUp[i] = numGenes*Math.pow(rankProductUpTemp, 1f/(float)rankProductUpArrayTemp.length);
	    		
//	    		for (int j = 0; j<adata.length; j++){
//		    		rankProductUp = rankProductUp*(adata[j].length+1 - adata[j][i])/numGenes;
//		    	}
//		    	rankProductArrayUp[i] = numGenes*Math.pow(rankProductUp, 1f/(float)adata.length);
//		    	rankProductUp = 1;
	    	}
	    }
	    
	    
	    //begin permutation-based estimation

	    //////////*************************
	    double[] trynewarray= new double[numGenes*numPerms];
	    int ind = 0;
	    for (int perm = 0; perm < numPerms; perm++) {
            if (stop) {
                break;
            }
	    	double[][] randPairs = new double[numOfPairs][numGenes];
	    	float[][] groupARand= new float[dataGroupA.getColumnDimension()][numGenes];
	    	float[][] groupBRand= new float[dataGroupB.getColumnDimension()][numGenes];
	    	updateProgressBar();
            
            FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix);
            groupARand = permutedMatrix.getMatrix(0, permutedMatrix.getRowDimension()-1, groupAAss).A;
            groupBRand = permutedMatrix.getMatrix(0, permutedMatrix.getRowDimension()-1, groupBAss).A;

		    int x=0;
		    for (int i=0; i<groupARand[0].length; i++){
			    for (int j=0; j<groupBRand[0].length; j++){
			    	for (int g=0; g<numGenes; g++){
			    		randPairs[x][g]=groupBRand[g][j]-groupARand[g][i];
			    	}
			    	x++;
			    }
		    }

			double[] order = new double[numGenes];
			for (int i=0; i<order.length; i++){
				order[i]=i;
			}
		    for (int i=0; i< numOfPairs; i++){
		    	ExperimentUtil.sort2(randPairs[i], order);
		    	
		    	for (int j=0; j<numGenes; j++){
		    		randPairs[i][j]=j+1.0;
		    	}
		    	ExperimentUtil.sort2(order, randPairs[i]);
		    }

		    for (int i = 0; i<numGenes; i++){
		    	

		    	double[] rankProductDownArrayTemp = new double[numGroupA];
				for (int num=0; num<numGroupA; num++){
					rankProductDownArrayTemp[num]=1;
				}
	    		int sampleNum=0;
	    		for (int a=0; a<numGroupA; a++){
	    			for (int b=0; b<numGroupB; b++){
	    				rankProductDownArrayTemp[a] = rankProductDownArrayTemp[a]*randPairs[sampleNum][i]/numGenes;
	    				sampleNum++;
	    			}
	    			rankProductDownArrayTemp[a] = Math.pow(rankProductDownArrayTemp[a], 1f/(float)numGroupB);
		    	}
	    		double rankProductDownTemp = 1;
	    		for (int c=0; c<rankProductDownArrayTemp.length; c++){
	    			rankProductDownTemp=rankProductDownArrayTemp[c]*rankProductDownTemp;
	    		}
	    		trynewarray[ind] = numGenes*Math.pow(rankProductDownTemp, 1f/(float)rankProductDownArrayTemp.length);
		    	ind++;
		    	
		    	
		    	
//		    	double permutedRankProduct = 1;
//		    	for (int j = 0; j<numOfPairs; j++){
//		    		permutedRankProduct = permutedRankProduct*(randPairs[j][i]+1)/numGenes;
//		    	}
//		    	trynewarray[ind]= numGenes*Math.pow(permutedRankProduct, 1f/(float)adata.length);
//		    	ind++;
//		    	permutedRankProduct = 1;
		    }
        }
	    Arrays.sort(trynewarray);
	    
	    event.setDescription("Finding q-values");
	    progress=0;
	    
	    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Finding q-values...");
    	// set progress limit
    	fireValueChanged(event);
    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
    	event.setIntValue(0);
    	fireValueChanged(event);
	    
	    double[] expectedPDown = new double[numGenes];
	    double[] expectedPUp = new double[numGenes];
	    totalProgressStep2 = expectedPDown.length;
	    
	    if (doDown){
		    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Finding Down Regulated q-values...");
	    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	    	event.setIntValue(0);
	    	for (int h=0; h<numGenes;h++){
		    	updateProgressBar2();
	    		int c;
	    		c=-Arrays.binarySearch(trynewarray, rankProductArrayDown[h])-1;
	    		if (c<0)
		    		c=Arrays.binarySearch(trynewarray, rankProductArrayDown[h]);
			    expectedPDown[h]=(double)c/numPerms;
		    }
	    }
	    if (doUp){
		    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Finding Up Regulated q-values...");
	    	progress=0;
	    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	    	event.setIntValue(0);
	    	for (int h=0; h<numGenes;h++){
		    	updateProgressBar2();
		    	int c;
	    		c=-Arrays.binarySearch(trynewarray, rankProductArrayUp[h])-1;
	    		if (c<0)
		    		c=Arrays.binarySearch(trynewarray, rankProductArrayUp[h]);
			    expectedPUp[h]=(double)c/numPerms;
		    }
	    }
	    
	    sigGenesArrays = new int[3][];
	    if (doDown){
		    double[] qValuesDown = new double[numGenes];
		    int[] rankg = new int[numGenes];

		    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Determining Down Regulated False Discovery Rate...");
	    	progress=0;
	    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	    	event.setIntValue(0);
	    	double[] sortedPDown = new double[numGenes];
	    	for (int i=0; i<numGenes; i++){
	    		sortedPDown[i]=expectedPDown[i];
	    	}
	    	Arrays.sort(sortedPDown);
		    for(int i=0; i<numGenes;i++){
		    	if (stop)
		    		break;
		    	updateProgressBar2();
		    	rankg[i] = -Arrays.binarySearch(sortedPDown, expectedPDown[i]+.000001)-1;
		    	if (rankg[i]<0)
		    		rankg[i]=Arrays.binarySearch(sortedPDown, expectedPDown[i]+.000001);
		    	rankg[i]++;
		    	
		    }
		    for(int i=0; i<numGenes;i++){
		    	qValuesDown[i] = expectedPDown[i]/rankg[i];
		    }

	 		qValsDown =new float[numGenes];
	 		for (int i=0; i<qValsDown.length; i++){
	 			qValsDown[i]=(float)qValuesDown[i];
	 		}

	 		pValsDown=new float[numGenes];
	 		for (int i=0; i<pValsDown.length; i++){
	 			pValsDown[i]=(float)expectedPDown[i]/numGenes;
	 		}

		    int sigGenesDownCounter=0;
		    if (correctionMethod == RPInitBox.JUST_ALPHA){
	
			    for (int i=0; i<rankg.length; i++){
			    	if(pValsDown[i]<=alpha)
			    		sigGenesDownCounter++;
			    }
		    }

		    if (correctionMethod == RPInitBox.FALSE_PROP){
			    for (int i=0; i<rankg.length; i++){
			    	if(qValsDown[i]<=falseProp)
			    		sigGenesDownCounter++;
			    }
		    }

		    if (correctionMethod == RPInitBox.FALSE_NUM){
			    for (int i=0; i<rankg.length; i++){
			    	if(expectedPDown[i]<=falseNum)
			    		sigGenesDownCounter++;
			    }
		    }
		    
		    sigGenesArrays[0] = new int[sigGenesDownCounter];
	 		int counters=0;
	
		    if (correctionMethod == RPInitBox.JUST_ALPHA){
		    	for (int i=0; i<rankg.length; i++){
			    	if(pValsDown[i]<=alpha){
			    		sigGenesArrays[0][counters] = i;
			    		counters++;
			    	}
			    }
		    }

		    if (correctionMethod == RPInitBox.FALSE_PROP){
				for (int i=0; i<rankg.length; i++){
			    	if(qValuesDown[i]<=falseProp){
			    		sigGenesArrays[0][counters] = i;
			    		counters++;
			    	}
			    }
		    }

		    if (correctionMethod == RPInitBox.FALSE_NUM){
		    	for (int i=0; i<rankg.length; i++){
			    	if(expectedPDown[i]<=falseNum){
			    		sigGenesArrays[0][counters] = i;
			    		counters++;
			    	}
			    }
		    }
	    }
	    if (doUp){
	    	double[] qValuesUp = new double[expectedPUp.length];
		    int[] rankg = new int[expectedPUp.length];
		    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Determining Up Regulated False Discovery Rate...");
	    	progress=0;
	    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	    	event.setIntValue(0);
	    	
	    	double[] sortedPUp = new double[expectedPUp.length];
	    	for (int i=0; i<expectedPUp.length; i++){
	    		sortedPUp[i]=expectedPUp[i];
	    	}
	    	Arrays.sort(sortedPUp);
		    for(int i=0; i<qValuesUp.length;i++){
		    	if (stop)
		    		break;
		    	updateProgressBar2();
		    	rankg[i] = -Arrays.binarySearch(sortedPUp, expectedPUp[i]-.000001)-1;
		    	if (rankg[i]<0)
		    		rankg[i]=Arrays.binarySearch(sortedPUp, expectedPUp[i]-.000001);
		    	rankg[i]++;
		    }
		    
		    for(int i=0; i<qValuesUp.length;i++){
		    	qValuesUp[i] = expectedPUp[i]/rankg[i];
		    }
	
	 		qValsUp =new float[qValuesUp.length];
	 		for (int i=0; i<qValsUp.length; i++){
	 			qValsUp[i]=(float)qValuesUp[i];
	 		}
	 		pValsUp=new float[qValuesUp.length];
	 		for (int i=0; i<pValsUp.length; i++){
	 			pValsUp[i]=(float)expectedPUp[i]/numGenes;
	 		}
		    int sigGenesUpCounter=0;
		    if (correctionMethod == RPInitBox.JUST_ALPHA){
			    for (int i=0; i<rankg.length; i++){
			    	if(pValsUp[i]<=alpha)
			    		sigGenesUpCounter++;
			    }
		    }
		    if (correctionMethod == RPInitBox.FALSE_PROP){
			    for (int i=0; i<rankg.length; i++){
			    	if(qValsUp[i]<=falseProp)
			    		sigGenesUpCounter++;
			    }
		    }
		    if (correctionMethod == RPInitBox.FALSE_NUM){
			    for (int i=0; i<rankg.length; i++){
			    	if(expectedPUp[i]<=falseNum)
			    		sigGenesUpCounter++;
			    }
		    }


		    sigGenesArrays[1] = new int[sigGenesUpCounter];
	 		int counters=0;
	
		    if (correctionMethod == RPInitBox.JUST_ALPHA){
		    	for (int i=0; i<rankg.length; i++){
			    	if(pValsUp[i]<=alpha){
			    		sigGenesArrays[1][counters] = i;
			    		counters++;
			    	}
			    }
		    }
		    if (correctionMethod == RPInitBox.FALSE_PROP){
				for (int i=0; i<rankg.length; i++){
			    	if(qValsUp[i]<=falseProp){
			    		sigGenesArrays[1][counters] = i;
			    		counters++;
			    	}
			    }
		    }
		    if (correctionMethod == RPInitBox.FALSE_NUM){
		    	for (int i=0; i<rankg.length; i++){
			    	if(expectedPUp[i]<=falseNum){
			    		sigGenesArrays[1][counters] = i;
			    		counters++;
			    	}
			    }
		    }
	    }
	    //Get the remaining non-significant genes
	    ArrayList<Integer> insigGenes = new ArrayList<Integer>();
	    for (int i=0; i<rankArray.length; i++){
	    	boolean cont = false;
	    	if (doDown){
		    	for (int j=0; j<sigGenesArrays[0].length; j++){
		    		if (i==sigGenesArrays[0][j])
		    			cont=true;
		    	}
	    	}
	    	if (doUp){
		    	for (int j=0; j<sigGenesArrays[1].length; j++){
		    		if (i==sigGenesArrays[1][j])
		    			cont=true;
		    	}
	    	}
	    	if (cont)
	    		continue;
	    	insigGenes.add(i);
	    	
	    }
	    
	    sigGenesArrays[2] = new int[insigGenes.size()];
	    for (int i=0; i<sigGenesArrays[2].length; i++){
	    	sigGenesArrays[2][i]=insigGenes.get(i);
	    }
	}
    protected FloatMatrix getPermutedMatrix(FloatMatrix inputMatrix) {
    	int[] validArray = new int[numGenes];
        int[] permGenes;
        for (int j = 0; j < validArray.length; j++) {
            validArray[j] = j;
        }
        FloatMatrix permutedMatrix = new FloatMatrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
        for (int i = 0; i < inputMatrix.getColumnDimension(); i++) {
            
            permGenes = getPermutedValues(numGenes, validArray); //returns an int array of size "numExps", with the valid values permuted
            
            for (int j = 0; j < inputMatrix.getRowDimension(); j++) {
                permutedMatrix.A[j][i] = inputMatrix.A[permGenes[j]][i];
            }
        }
        return permutedMatrix;
    } 
	protected static int[] getPermutedValues(int arrayLength, int[] validArray) {//returns an integer array of length "arrayLength", with the valid values (the currently included experiments) permuted
        int[] permutedValues = new int[arrayLength];
        for (int i = 0; i < permutedValues.length; i++) {
            permutedValues[i] = i;
        }
       
        int[] permutedValidArray = new int[validArray.length];
        for (int i = 0; i < validArray.length; i++) {
            permutedValidArray[i] = validArray[i];
        }
        
        for (int i = permutedValidArray.length; i > 1; i--) {
            Random generator2 =new Random();
            int randVal = generator2.nextInt(i - 1);
            int temp = permutedValidArray[randVal];
            permutedValidArray[randVal] = permutedValidArray[i - 1];
            permutedValidArray[i - 1] = temp;
        }  
        
        for (int i = 0; i < validArray.length; i++) {
            permutedValues[validArray[i]] = permutedValidArray[i];
        }
        return permutedValues;
    }
	
	private void updateProgressBar(){
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue((100*progress)/(numPerms));
    	fireValueChanged(event);
	}
	private void updateProgressBar2(){
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue((100*progress)/(totalProgressStep2));
    	fireValueChanged(event);
	}
    private FloatMatrix getFoldChange() {
    	FloatMatrix fm = new FloatMatrix(numGenes, 1);
	    for (int g=0; g<numGenes; g++){
	    	float[] groupA = new float[dataGroupA.getColumnDimension()];
	    	float[] groupB = new float[dataGroupB.getColumnDimension()];
	    	for (int i=0; i<groupA.length; i++){
	    		groupA[i]=dataGroupA.get(g, i);
	    	}
	    	for (int i=0; i<groupB.length; i++){
	    		groupB[i]=dataGroupB.get(g, i);
	    	}
	    	Double db = Math.pow(2,getMean(groupA)-getMean(groupB));
	    	fm.A[g][0]= db.floatValue();
    	}
	    return fm;
    }
    public static void main(String[] args){
    	int[]array = {0,1};
    	for (int i=0; i<array.length;i++){
    		System.out.print(array[i]+"\t");
    	}
    	System.out.println(":"+array.length);
    	int[] result = getPermutedValues(array.length, array);
    	for (int i=0; i<result.length;i++){
    		System.out.print(result[i]+"\t");
    	}
    }
}
/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SAM.java,v $
 * $Revision: 1.7 $
 * $Date: 2004-05-20 21:22:43 $
 * $Author: braisted $
 * $State: Exp $
 */

/*
 * SAM.java
 *
 * Created on December 16, 2002, 12:55 PM
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tigr.util.ConfMap;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;
import org.tigr.util.*;

//import TDistribution;

import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.gui.impl.sam.*;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class SAM extends AbstractAlgorithm {
    
    private boolean stop = false;
    
    private int function;
    private float factor;
    private boolean absolute;
    private FloatMatrix expMatrix;
    
    private FloatMatrix imputedMatrix;    
    
    private Vector[] clusters;
    private int k; // # of clusters    
    
    private int numGenes, numExps;    
    
    private int[] groupAssignments, pairedGroupAExpts, pairedGroupBExpts;
    private boolean[] inSurvivalAnalysis, isCensored, useAllPerms;
    private int studyDesign;
    private int numMultiClassGroups = 0;
    private int numCombs, numUniquePerms;
    //private boolean useAllCombs;
    private boolean useKNearest;
    private int numNeighbors;
    //private boolean useAllUniquePerms;
    private double sNought = 0.0f;
    private double s0Percentile, oneClassMean;
    
    private double[] dArray, rArray, sortedDArray, dBarValues, survivalTimes, zkArray, globalAllSValues, globalAllQValues, globalSortedAllSValues;
    private int[] dkArray;
    private int[][] rkArray;
    private long[] randomSeeds;

    //private boolean passedThisPoint = false; // just for debugging purposes, delete this variable later
    
    /** Creates new SAM */
    public SAM() {
    }

    /**
     * This method should interrupt the calculation.
     */

    
    /**
     * This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        //System.out.println("SAM: execute entered");
	groupAssignments = data.getIntArray("group-assignments");
	
	AlgorithmParameters map = data.getParams();
	function = map.getInt("distance-function", EUCLIDEAN);
	factor   = map.getFloat("distance-factor", 1.0f);
	absolute = map.getBoolean("distance-absolute", false);
	
	boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
	int method_linkage = map.getInt("method-linkage", 0);
	boolean calculate_genes = map.getBoolean("calculate-genes", false);
	boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
	boolean saveImputedMatrix = map.getBoolean("saveImputedMatrix", false);
        boolean usePreviousGraph = map.getBoolean("use-previous-graph", false);
        
        double userPercentile = (double)(map.getFloat("userPercentile", 0.0f));
        boolean useTusherEtAlS0 = map.getBoolean("useTusherEtAlS0", false);
        boolean calculateQLowestFDR = map.getBoolean("calculateQLowestFDR", false);
        boolean useAllUniquePerms = map.getBoolean("useAllUniquePerms", false);
        numUniquePerms = map.getInt("numUniquePerms", 0);
        
	this.expMatrix = data.getMatrix("experiment");
	
	numGenes = this.expMatrix.getRowDimension();
	numExps = this.expMatrix.getColumnDimension();  
        inSurvivalAnalysis = new boolean[numExps];
        isCensored = new boolean[numExps];
        survivalTimes = new double[numExps];
        studyDesign = map.getInt("study-design", SAMInitDialog.TWO_CLASS_UNPAIRED);
        if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
            FloatMatrix pairedAExptsMatrix = data.getMatrix("pairedAExptsMatrix");
            FloatMatrix pairedBExptsMatrix = data.getMatrix("pairedBExptsMatrix");
            pairedGroupAExpts = new int[pairedAExptsMatrix.getRowDimension()];
            pairedGroupBExpts = new int[pairedBExptsMatrix.getRowDimension()];
            for (int i = 0; i < pairedAExptsMatrix.getRowDimension(); i++) {
                pairedGroupAExpts[i] = (int)(pairedAExptsMatrix.A[i][0]);
                pairedGroupBExpts[i] = (int)(pairedBExptsMatrix.A[i][0]);
            }
        }
        if (studyDesign == SAMInitDialog.MULTI_CLASS) {
            numMultiClassGroups = map.getInt("numMultiClassGroups", 0);
        }
        if (studyDesign == SAMInitDialog.ONE_CLASS) {
            oneClassMean = (double)(map.getFloat("oneClassMean", 0.0f));
        }
        if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
            FloatMatrix inAnalysisMatrix = data.getMatrix("inAnalysisMatrix");
            FloatMatrix isCensoredMatrix = data.getMatrix("isCensoredMatrix");
            FloatMatrix survivalTimesMatrix = data.getMatrix("survivalTimesMatrix");
            
            for (int i = 0; i < inAnalysisMatrix.getRowDimension(); i++) {
                if (inAnalysisMatrix.A[i][0] == 0.0f) {
                    inSurvivalAnalysis[i] = false;
                } else {
                    inSurvivalAnalysis[i] = true;
                }
                if (isCensoredMatrix.A[i][0] == 0.0f) {
                    isCensored[i] = false;
                } else {
                    isCensored[i] = true;
                }
                survivalTimes[i] = (double)survivalTimesMatrix.A[i][0];
            }
        }
        
        /*
        for (int i = 0; i < inSurvivalAnalysis.length; i++) {
            System.out.println("inSurvivalAnalysis[" + i + "] =" + inSurvivalAnalysis[i]);
        }
        for (int i = 0; i < isCensored.length; i++) {
            System.out.println("isCensored[" + i + "] =" + isCensored[i]);
        }  
        for (int i = 0; i < survivalTimes.length; i++) {
            System.out.println("survivalTimes[" + i + "] =" + survivalTimes[i]);
        }  
         */      
        
        numCombs = map.getInt("num-combs", 100);
        if (useAllUniquePerms) {
            numCombs = numUniquePerms;
        }
        //useAllCombs = map.getBoolean("use-all-combs", false);
        useKNearest = map.getBoolean("use-k-nearest", true);
        numNeighbors = map.getInt("num-neighbors", 10);
        
        double pi0Hat = 0; 
        double delta = 0.0d;
        double[] deltaGrid = new double[1001];
        int[] numSigGenesByDelta = new int[1];
        int[] sortedDArrayIndices = new int[1];
        double[] medNumFalselyCalledGenesByDelta = new double[1];
        double[] ninetiethPercentileFalselyCalledGenesByDelta = new double[1];
        double[] FDRmedian = new double[1];
        double[] FDR90thPercentile = new double[1]; 
        double[] qLowestFDR = new double[1];
        
        if (!usePreviousGraph) { 
        
            imputedMatrix = new FloatMatrix(numGenes, numExps);
            
            if (useKNearest) {
                imputedMatrix = imputeKNearestMatrix(expMatrix, numNeighbors);
            } else {
                imputedMatrix = imputeRowAverageMatrix(expMatrix);
            }
            
            /*
            if (saveImputedMatrix) {
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("Data"));   
                int returnVal = fc.showSaveDialog(SAMGUI.SAMFrame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        PrintWriter out = new PrintWriter(new FileOutputStream(file));
                        //int[] groupAssgn = getGroupAssignments();
                        //
                        for (int i = 0; i < groupAssgn.length; i++) {
                            out.print(groupAssgn[i]);
                            if (i < groupAssgn.length - 1) {
                                out.print("\t");
                            }
                        }
                        out.println();
                         //
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    //this is where a real application would save the file.
                    //log.append("Saving: " + file.getName() + "." + newline);
                } else {
                    //log.append("Save command cancelled by user." + newline);
                }
            }
             */

            SAMState.imputedMatrix = imputedMatrix;

            //The following three statements are just for initialization purposes
            zkArray = new double[1];
            rkArray = new int[1][];
            dkArray = new int[1];
                
            if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
                zkArray = getZkArray();
                rkArray = getRkArray();
                dkArray = getDkArray();
            }
            
            /*
            for (int i = 0; i < zkArray.length; i++) {
                System.out.println("zkArray[" + i + "] = " + zkArray[i]);
            }
            
            for (int i = 0; i < dkArray.length; i++) {
                System.out.println("dkArray[" + i + "] = " + dkArray[i]);
            }  
            
            for (int i = 0; i < rkArray.length; i++) {
                System.out.println("rkArray[" + i + "] :");
                for (int j = 0; j < rkArray[i].length; j++) {
                    System.out.println("rkArray[" + i + "][" + j + "] =" + rkArray[i][j]);
                }
            }
             */
            
            /*
            try {
                printMatrix(imputedMatrix, "imputedMatrix.txt");
                printMatrix(expMatrix, "origExpMatrix.txt");
            } catch (Exception e) {
                e.printStackTrace();
            }
             */
            globalAllSValues = getAllSValues();
            QSort sortSValues = new QSort(globalAllSValues);
            globalSortedAllSValues = sortSValues.getSortedDouble();            
        
            //System.out.println("sAlpha(0.59) = " + getSAlpha(59.0d));
            if (useTusherEtAlS0) {
                /*
                globalAllSValues = getAllSValues();
                QSort sortSValues = new QSort(globalAllSValues);
                globalSortedAllSValues = sortSValues.getSortedDouble();    
                 */           
                globalAllQValues = getQValues();
                sNought = getSNought(); 
                SAMState.sNought = sNought;
                SAMState.s0Percentile = s0Percentile;
            } else {
                sNought = getSAlpha(userPercentile); 
                SAMState.sNought = sNought; 
                s0Percentile = userPercentile; 
                SAMState.s0Percentile = userPercentile;
            }
            //System.out.println("s0 = " + sNought);

            dArray = new double[numGenes];
            rArray = new double[numGenes];
            for (int i = 0; i < dArray.length; i++) {
                //System.out.println("current gene = " + i);
                dArray[i] = getD(i, imputedMatrix); //  UNCOMMENT
                rArray[i] = getR(i, imputedMatrix);
                //System.out.println("dArray[" + i + "] = " + dArray[i]);
            }
            
            SAMState.dArray = dArray;
            SAMState.rArray = rArray;
            
            /*
            try {
                File outfile = new File("nameVsD_and_r.txt");
                PrintWriter out = new PrintWriter(new FileOutputStream(outfile));  
                for (int i = 0; i < dArray.length; i++) {
                    out.print(dArray[i] +"\t"); // UNCOMMENT
                    out.print(getR(i, imputedMatrix) + "\t");
                    out.print(getS(i, imputedMatrix));
                    out.print("\n");
                }
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            */
            
         
         
        //passedThisPoint = true; //for debugging only, remove this later
        
        //System.out.println("SAM.execute(): after populating dArray[]");
        
            QSort sortDArray = new QSort(dArray);
            sortedDArray = sortDArray.getSortedDouble();
            SAMState.sortedDArray = sortedDArray;
            sortedDArrayIndices = sortDArray.getOrigIndx();
            SAMState.sortedDArrayIndices = sortedDArrayIndices;
            double[][] permutedDValues = new double[numCombs][numGenes];
            //if (useAllUniquePerms) {
            //}

            AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numCombs);
            if (useAllUniquePerms) {
                event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numUniquePerms);
            }
            fireValueChanged(event2);
            event2.setId(AlgorithmEvent.PROGRESS_VALUE);   
            
            Random rand  = new Random();
            randomSeeds  = new long[numCombs];
            for (int i = 0; i < numCombs; i++) {
                randomSeeds[i] = rand.nextLong();
            }
            
            if (!useAllUniquePerms) {
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }  
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);            
                    //System.out.println("execute(): permutation " + i);
                    int[] permutedExpts = new int[1];
                    boolean[] changeSign = new boolean[1];
                    if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.MULTI_CLASS) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL)) {
                        //System.out.print("Permutation " + i + ": ");
                        Vector validExpts = new Vector();
                        if (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) {
                            for (int j = 0; j < groupAssignments.length; j++) {
                                if (groupAssignments[j] != SAMInitDialog.NEITHER_GROUP) {
                                    validExpts.add(new Integer(j));
                                }
                            }                        
                        } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
                            for (int j = 0; j < groupAssignments.length; j++) {
                                if (groupAssignments[j] != 0) {
                                    validExpts.add(new Integer(j));
                                }
                            }
                        } else if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
                            for (int j = 0; j < inSurvivalAnalysis.length; j++) {
                                if (inSurvivalAnalysis[j]) {
                                    validExpts.add(new Integer(j));
                                }
                            }
                        } 

                        int[] validArray = new int[validExpts.size()];
                        for (int j = 0; j < validArray.length; j++) {
                            validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                        } 
                        //System.out.print("valid array: ");
                        //printIntArray(validArray);
                        permutedExpts = getPermutedValues(numExps, validArray); //returns an int array of size "numExps", with the valid values permuted 
                        //printIntArray(permutedExpts);
                    } else if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
                        //System.out.print("Permutation " + i + ": ");
                        permutedExpts = permuteWithinPairs(randomSeeds[i]); //returns an int array with some paired experiment indices permuted
                        //System.out.println();
                    } else if (studyDesign == SAMInitDialog.ONE_CLASS) {
                        Vector validExpts = new Vector();
                        for (int j = 0; j < groupAssignments.length; j++) {
                            if (groupAssignments[j] == 1) {
                                validExpts.add(new Integer(j));
                            }
                        } 

                        int[] validArray = new int[validExpts.size()];
                        for (int j = 0; j < validArray.length; j++) {
                            validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                        }                    

                        changeSign = getOneClassChangeSignArray(randomSeeds[i], validArray);
                    }

                    // *** DONE UP TO HERE 5/30/03 ***

                    //printIntArray(permutedExpts);
                    FloatMatrix permutedMatrix;
                    if (studyDesign == SAMInitDialog.ONE_CLASS) {
                        permutedMatrix = getOneClassPermMatrix(imputedMatrix, changeSign);
                    } else {
                        permutedMatrix = getPermutedMatrix(imputedMatrix, permutedExpts);
                    }

                    // ****DONE UP TO HERE 10/29/03

                    double[] permDArray = new double[permutedMatrix.getRowDimension()];
                    for (int j = 0; j < permutedMatrix.getRowDimension(); j++) {
                        permDArray[j] = getD(j, permutedMatrix);
                    }

                    QSort sortPermDArray = new QSort(permDArray);
                    double[] sortedPermDArray = sortPermDArray.getSortedDouble();

                    for (int j = 0; j < sortedPermDArray.length; j++) {
                        permutedDValues[i][j] = sortedPermDArray[j];
                    }

                }
                
            } else { // if (useAllPerms)
                int[] permutedExpts = new int[numExps];
                
                for (int i = 0; i < numExps; i++) {
                    permutedExpts[i] = i;
                }
                
                if (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) {
                    Vector usedExptsVector = new Vector();
                    int numGroupAValues = 0;
                    for (int i = 0; i < groupAssignments.length; i++) {
                        if (groupAssignments[i] != SAMInitDialog.NEITHER_GROUP) {
                           usedExptsVector.add(new Integer(i));
                        } 
                        if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                            numGroupAValues++;
                        }
                    }
                    int[] usedExptsArray = new int[usedExptsVector.size()];
                    
                    for (int i = 0; i < usedExptsArray.length; i++) {
                        usedExptsArray[i] = ((Integer)(usedExptsVector.get(i))).intValue();
                    }
                    
                    int[] combArray = new int[numGroupAValues];
                    for (int i = 0; i < combArray.length; i++) {
                        combArray[i] = -1;
                    }  
                    
                    int numGroupBValues = usedExptsArray.length - numGroupAValues;

                    int permCounter = 0;
                    
                    while (Combinations.enumerateCombinations(usedExptsArray.length, numGroupAValues, combArray)) {
                        
                        if (stop) {
                            throw new AbortException();
                        }
                        event2.setIntValue(permCounter);
                        event2.setDescription("Permuting matrix: Current permutation = " + (permCounter+1));
                        fireValueChanged(event2); 
                        
                        int[] notInCombArray = new int[numGroupBValues];
                        int notCombCounter = 0;                 
                        
                        for (int i = 0; i < usedExptsArray.length; i++) {
                            if(!belongsInArray(i, combArray)) {
                                notInCombArray[notCombCounter] = i;
                                notCombCounter++;
                            }                            
                        }
                        
                        for (int i = 0; i < combArray.length; i++) {
                            permutedExpts[usedExptsArray[i]] = usedExptsArray[combArray[i]];
                        }
                        for (int i = 0; i < notInCombArray.length; i++) {
                            permutedExpts[usedExptsArray[combArray.length + i]] = usedExptsArray[notInCombArray[i]];
                        }
                        
                        FloatMatrix permutedMatrix = getPermutedMatrix(imputedMatrix, permutedExpts);  
                        
                        double[] permDArray = new double[permutedMatrix.getRowDimension()];
                        for (int j = 0; j < permutedMatrix.getRowDimension(); j++) {
                            permDArray[j] = getD(j, permutedMatrix);
                        }
                        
                        QSort sortPermDArray = new QSort(permDArray);
                        double[] sortedPermDArray = sortPermDArray.getSortedDouble();
                        
                        for (int j = 0; j < sortedPermDArray.length; j++) {
                            permutedDValues[permCounter][j] = sortedPermDArray[j];
                        } 
                        
                        permCounter++;                        
                    }
                    
                } else if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
                    for (int i = 0; i < numUniquePerms; i++) {
                        
                        if (stop) {
                            throw new AbortException();
                        }                     
                        event2.setIntValue(i);
                        event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                        fireValueChanged(event2);                     
                        permutedExpts = permuteWithinPairsAllPerms(i);
                        
                        FloatMatrix permutedMatrix = getPermutedMatrix(imputedMatrix, permutedExpts);  
                        
                        double[] permDArray = new double[permutedMatrix.getRowDimension()];
                        for (int j = 0; j < permutedMatrix.getRowDimension(); j++) {
                            permDArray[j] = getD(j, permutedMatrix);
                        }
                        
                        QSort sortPermDArray = new QSort(permDArray);
                        double[] sortedPermDArray = sortPermDArray.getSortedDouble();
                        
                        for (int j = 0; j < sortedPermDArray.length; j++) {
                            permutedDValues[i][j] = sortedPermDArray[j];
                        }                        
                    }
                    
                } else if (studyDesign == SAMInitDialog.ONE_CLASS) {
                    for (int i = 0; i < numUniquePerms; i++) {
                        if (stop) {
                            throw new AbortException();
                        }                     
                        event2.setIntValue(i);
                        event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                        fireValueChanged(event2);   
                        
                        Vector validExpts = new Vector();
                        for (int j = 0; j < groupAssignments.length; j++) {
                            if (groupAssignments[j] == 1) {
                                validExpts.add(new Integer(j));
                            }
                        } 

                        int[] validArray = new int[validExpts.size()];
                        for (int j = 0; j < validArray.length; j++) {
                            validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                        }                    

                        boolean[] changeSign = getOneClassChangeSignArrayAllUniquePerms(i, validArray);      
                        
                        FloatMatrix permutedMatrix = getOneClassPermMatrix(imputedMatrix, changeSign);
                        
                        double[] permDArray = new double[permutedMatrix.getRowDimension()];
                        for (int j = 0; j < permutedMatrix.getRowDimension(); j++) {
                            permDArray[j] = getD(j, permutedMatrix);
                        }
                        
                        QSort sortPermDArray = new QSort(permDArray);
                        double[] sortedPermDArray = sortPermDArray.getSortedDouble();
                        
                        for (int j = 0; j < sortedPermDArray.length; j++) {
                            permutedDValues[i][j] = sortedPermDArray[j];
                        }                        
                    }
                    
                } else if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
                    Vector validExpts = new Vector();
                    
                    for (int j = 0; j < inSurvivalAnalysis.length; j++) {
                        if (inSurvivalAnalysis[j]) {
                            validExpts.add(new Integer(j));
                        } 
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }                    
                    
                    int[] comb = new int[validArray.length];
                    for (int i = 0; i < comb.length; i++) {
                        comb[i] = -1;
                    }                    
                    
                    int permCounter = 0;
                    while (Permutations.enumeratePermutations(validArray.length, validArray.length, comb)) {
                        if (stop) {
                            throw new AbortException();
                        }
                        event2.setIntValue(permCounter);
                        event2.setDescription("Permuting matrix: Current permutation = " + (permCounter+1));
                        fireValueChanged(event2);       
                        
                        for (int i = 0; i < validArray.length; i++) {
                            permutedExpts[validArray[i]] = validArray[comb[i]]; 
                        }    
                        
                        FloatMatrix permutedMatrix = getPermutedMatrix(imputedMatrix, permutedExpts); 
                        
                        double[] permDArray = new double[permutedMatrix.getRowDimension()];
                        for (int j = 0; j < permutedMatrix.getRowDimension(); j++) {
                            permDArray[j] = getD(j, permutedMatrix);
                        }
                        
                        QSort sortPermDArray = new QSort(permDArray);
                        double[] sortedPermDArray = sortPermDArray.getSortedDouble();
                        
                        for (int j = 0; j < sortedPermDArray.length; j++) {
                            permutedDValues[permCounter][j] = sortedPermDArray[j];
                        }                         
                        
                        permCounter++;
                    }
                }
                                
              
            }

            dBarValues = new double[numGenes];

            for (int i = 0; i < numGenes; i++) {
                double[] currentGene = new double[numCombs];
                for (int j = 0; j < numCombs; j++) {
                    currentGene[j] = permutedDValues[j][i];
                }
                dBarValues[i] = getMean(currentGene);
            }
            
            SAMState.dBarValues = dBarValues;
            /*
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
             */

            double[] oneDimPermutedDValues = new double[numCombs*numGenes];
            int counter1 = 0;
            //System.out.println("Creating oneDimPermutedDValues ....");
            for (int i = 0; i < numCombs; i++) {
                for (int j = 0; j < numGenes; j++) {
                    oneDimPermutedDValues[counter1] = permutedDValues[i][j];
                    counter1++;
                }
            }
            //System.out.println("oneDimPermutedDValues created.");
            
            //System.out.println("sorting oneDimPermutedDValues ....");
            QSort sortAllPermutedDValues = new QSort(oneDimPermutedDValues);
            double[] sortedAllPermutedDValues = sortAllPermutedDValues.getSortedDouble();
            //System.out.println("Sorting complete.");
            int[] sortedAllPermutedDValuesIndices = sortAllPermutedDValues.getOrigIndx();
            int percentile25thIndex = (int)Math.round((sortedAllPermutedDValues.length)*0.25 - 1);  
            if (percentile25thIndex < 0) {
                percentile25thIndex = 0;
            } else if (percentile25thIndex >= sortedAllPermutedDValues.length) {
                percentile25thIndex = sortedAllPermutedDValues.length - 1;
            }
            int percentile75thIndex = (int)Math.round((sortedAllPermutedDValues.length)*0.75 - 1); 
            if (percentile75thIndex < 0) {
                percentile75thIndex = 0;
            } else if (percentile75thIndex >= sortedAllPermutedDValues.length) {
                percentile75thIndex = sortedAllPermutedDValues.length - 1;
            }        

            double q25 = sortedAllPermutedDValues[percentile25thIndex];
            double q75 = sortedAllPermutedDValues[percentile75thIndex];

            //System.out.println("q25 = " + q25 + ", q75 =" + q75);

            int piCounter = 0;

            for (int i = 0; i < dArray.length; i++) {
                if ((dArray[i] > q25) && (dArray[i]< q75)) {
                    piCounter++;
                }
            }

            pi0Hat = (double)(piCounter/(0.5d*numGenes));
            
            pi0Hat = Math.min(pi0Hat, 1.0d);
            SAMState.pi0Hat = pi0Hat;
             
            
            //System.out.println("piHat = " + pi0Hat);

            double maximum = getMax(dArray);
            double minimum = getMin(dArray);

            //System.out.println("maximum = " + maximum + ", minimum = " + minimum);

            
            //float maxDelta = 0.0f;
            if (Math.abs(maximum) > Math.abs(minimum)) {
                delta = (float)(0.25*Math.abs(maximum));
                //maxDelta = Math.abs(maximum);
            } else {
                delta = (float)(0.25*Math.abs(minimum));
                //maxDelta = Math.abs(minimum);
            }


            double[] diffValues = new double[sortedDArray.length];

            for (int i = 0; i < diffValues.length; i++) {
                diffValues[i] = Math.abs(sortedDArray[i] - dBarValues[i]);
            }

            double maxDelta = getMax(diffValues);
            double minDelta = getMin(diffValues);
            //System.out.println("maxDelta = " + maxDelta + ", minDelta = " + minDelta);

            double deltaIncrement = (double)((maxDelta - minDelta)/1000d);
            
            double currentDelta = minDelta;
            for (int i = 0 ; i < deltaGrid.length; i++) {
                deltaGrid[i] = currentDelta;
                currentDelta = currentDelta + deltaIncrement;
            }
            
            SAMState.deltaGrid = deltaGrid;
            
            double[] cutUp = new double[deltaGrid.length];
            double[] cutLow = new double[deltaGrid.length];
            
            //System.out.println("Calculating cutUp and cutLow ....");
            for (int i = 0; i < cutUp.length; i++) {
                cutUp[i] = getCutUp(deltaGrid[i]);
                cutLow[i] = getCutLow(deltaGrid[i]);
            }
            
            //System.out.println("cutUp and cutLow calculated");

            numSigGenesByDelta = new int[deltaGrid.length];
            
            //System.out.println("calculating numSigGenesByDelta ...");
            
            for (int i = 0; i < deltaGrid.length; i++) {
                numSigGenesByDelta[i] = getNumSigGenesByDelta(cutUp[i], cutLow[i]);
            }
            
            //System.out.println("numSigGenesByDelta calcuated");
            
            SAMState.numSigGenesByDelta = numSigGenesByDelta;

            medNumFalselyCalledGenesByDelta = new double[deltaGrid.length];
            ninetiethPercentileFalselyCalledGenesByDelta = new double[deltaGrid.length];
            
            //System.out.println("calculating FDR median and 90th percentile ...");
            
            for (int i = 0; i < deltaGrid.length; i++) {
                medNumFalselyCalledGenesByDelta[i] = getMedNumFalselyCalledGenesByDelta(permutedDValues, cutUp[i], cutLow[i]);
                ninetiethPercentileFalselyCalledGenesByDelta[i] = getNinetiethPercentileFalselyCalledGenesByDelta(permutedDValues, cutUp[i], cutLow[i]);
            }
            for (int i = 0; i < deltaGrid.length; i++) {
                medNumFalselyCalledGenesByDelta[i] = pi0Hat*medNumFalselyCalledGenesByDelta[i];
                ninetiethPercentileFalselyCalledGenesByDelta[i] = pi0Hat*ninetiethPercentileFalselyCalledGenesByDelta[i];
            }   
            
            
            SAMState.medNumFalselyCalledGenesByDelta = medNumFalselyCalledGenesByDelta;
            SAMState.ninetiethPercentileFalselyCalledGenesByDelta = ninetiethPercentileFalselyCalledGenesByDelta;

            FDRmedian = new double[deltaGrid.length];
            FDR90thPercentile = new double[deltaGrid.length];

            for (int i = 0; i < deltaGrid.length; i++) {
                FDRmedian[i] = (medNumFalselyCalledGenesByDelta[i]*100d)/numSigGenesByDelta[i];
                FDR90thPercentile[i] = ninetiethPercentileFalselyCalledGenesByDelta[i]*100d/numSigGenesByDelta[i];
            }

            //System.out.println("FDR median and 90th percentile calculated.");
            
            SAMState.FDRmedian = FDRmedian;
            SAMState.FDR90thPercentile = FDR90thPercentile;
            
            //qLowestFDR = new double[numGenes]; 
                        
            // THIS PORTION DELETED FOR SIMON; PROBABLY MAKE OPTIONAL ANYWAY 
             
            //System.out.println("Calculating qLowesrFDRs ...");
           /*
            if (calculateQLowestFDR) {
                qLowestFDR = new double[numGenes];
                
                AlgorithmEvent event3 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, qLowestFDR.length);
                fireValueChanged(event3);
                event3.setId(AlgorithmEvent.PROGRESS_VALUE);  
                
                for (int i = 0; i < numGenes; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event3.setIntValue(i);
                    event3.setDescription("Calculating q values: Current gene = " + (i+ 1));
                    fireValueChanged(event3);
                    
                    Vector sigDeltaIndices = new Vector();
                    for (int j = 0; j < deltaGrid.length; j++) {
                        if (isSignificant(i, cutUp[j], cutLow[j])) {
                            sigDeltaIndices.add(new Integer(j));
                        }
                    }
                    
                    double[] currentGeneFDRs = new double[sigDeltaIndices.size()];
                    for (int j = 0; j < sigDeltaIndices.size(); j++) {
                        int currentDeltaIndex = ((Integer)sigDeltaIndices.get(j)).intValue();
                        double currDelta = deltaGrid[currentDeltaIndex];
                        currentGeneFDRs[j] = getMedNumFalselyCalledGenesByDelta(permutedDValues, getCutUp(currDelta), getCutLow(currDelta));
                        currentGeneFDRs[j] = (double)(currentGeneFDRs[j]*pi0Hat*100d)/(double)numSigGenesByDelta[currentDeltaIndex];
                    }
                    if (currentGeneFDRs.length > 0) {
                        
                        qLowestFDR[i] = getMin(currentGeneFDRs);
                        
                    } else {
                        qLowestFDR[i] = Double.NaN;
                    }
                    //System.out.println("qLowestFDR[" + i + "] = " + qLowestFDR[i]);
                }
            } 
            */
            
            if (calculateQLowestFDR){
                qLowestFDR = new double[numGenes];
                
                AlgorithmEvent event3 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, qLowestFDR.length);
                fireValueChanged(event3);
                event3.setId(AlgorithmEvent.PROGRESS_VALUE);  
                
                for (int i = 0; i < numGenes; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event3.setIntValue(i);
                    event3.setDescription("Calculating q values: Current gene = " + (i+ 1));
                    fireValueChanged(event3);
                    
                    //Vector sigDeltaIndices = new Vector();
                    int sigDeltaIndex = 0;
                    boolean sigFound = false;
                    for (int j = deltaGrid.length - 1; j >= 0; j--) {
                        if (isSignificant(i, cutUp[j], cutLow[j])) {
                            //sigDeltaIndices.add(new Integer(j));
                            sigDeltaIndex = j;
                            sigFound = true;
                            break;
                        }
                    }
                    
                    //double[] currentGeneFDRs = new double[sigDeltaIndices.size()];
                    //for (int j = 0; j < sigDeltaIndices.size(); j++) {
                        //int currentDeltaIndex = ((Integer)sigDeltaIndices.get(j)).intValue();

                    //}
                    if (sigFound) {
                        double currDelta = deltaGrid[sigDeltaIndex];
                        qLowestFDR[i] = getMedNumFalselyCalledGenesByDelta(permutedDValues, getCutUp(currDelta), getCutLow(currDelta));
                        qLowestFDR[i] = (double)(qLowestFDR[i]*pi0Hat*100d)/(double)numSigGenesByDelta[sigDeltaIndex];                        
                        //qLowestFDR[i] = getMin(currentGeneFDRs);                        
                    } else {
                        qLowestFDR[i] = Double.NaN;
                    }
                    //System.out.println("qLowestFDR[" + i + "] = " + qLowestFDR[i]);
                }                
            } else {
                qLowestFDR = new double[numGenes];
            }
            
            //System.out.println("qLowestFDRs calculated.");
            
            SAMState.qLowestFDR = qLowestFDR;

            FloatMatrix qLowestFDRMatrix = new FloatMatrix(qLowestFDR.length, 1);

            for (int i = 0; i < qLowestFDR.length; i++) {
                qLowestFDRMatrix.A[i][0] = (float)(qLowestFDR[i]);
            }

            /*
            try {
                File outfile = new File("deltaGrid_values.txt");
                PrintWriter out = new PrintWriter(new FileOutputStream(outfile));
                out.print("Delta\tMedian # false\t90th percentile false\t#genes called\tFDR% Median\tFDR% 90th percentile\n");
                for (int i = 0; i < deltaGrid.length; i++) {
                    out.print(deltaGrid[i] +"\t");
                    out.print(medNumFalselyCalledGenesByDelta[i] + "\t");
                    out.print(ninetiethPercentileFalselyCalledGenesByDelta[i] + "\t");
                    out.print(numSigGenesByDelta[i] + "\t");
                    out.print(FDRmedian[i] + "\t");
                    out.print(FDR90thPercentile[i] + "\t\n");
                }
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        

            try {
                File outfile = new File("qValues_by_Genes.txt");
                PrintWriter out = new PrintWriter(new FileOutputStream(outfile));

                for (int i = 0; i < qLowestFDR.length; i++) {
                    out.print(qLowestFDR[i] +"\n");

                }
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
             */

        } else { // if (usePreviousGraph)
            imputedMatrix = SAMState.imputedMatrix;
            //oneClassMean = SAMState.oneClassMean;
            dBarValues = SAMState.dBarValues;
            sortedDArray = SAMState.sortedDArray;
            sortedDArrayIndices = SAMState.sortedDArrayIndices;
            delta = SAMState.delta;
            deltaGrid = SAMState.deltaGrid;
            numSigGenesByDelta = SAMState.numSigGenesByDelta;
            medNumFalselyCalledGenesByDelta = SAMState.medNumFalselyCalledGenesByDelta;
            sNought = SAMState.sNought;
            s0Percentile = SAMState.s0Percentile;
            pi0Hat = SAMState.pi0Hat;
            ninetiethPercentileFalselyCalledGenesByDelta = SAMState.ninetiethPercentileFalselyCalledGenesByDelta;
            FDRmedian = SAMState.FDRmedian;
            FDR90thPercentile = SAMState.FDR90thPercentile;
            qLowestFDR = SAMState.qLowestFDR;
            dArray = SAMState.dArray;
            rArray =SAMState.rArray;
        }
        

        FloatMatrix dValuesMatrix = new FloatMatrix(dArray.length, 1); // to send to AlgorithmData result
        FloatMatrix rValuesMatrix = new FloatMatrix(rArray.length, 1); // to send to AlgorithmData result
        for (int i = 0; i < dArray.length; i++) {
            dValuesMatrix.A[i][0] = (float)dArray[i];
            rValuesMatrix.A[i][0] = (float)rArray[i];
        }
        
        FloatMatrix deltaGridMatrix = new FloatMatrix(deltaGrid.length, 1);
        FloatMatrix medNumFalseMatrix = new FloatMatrix(medNumFalselyCalledGenesByDelta.length, 1);
        FloatMatrix false90thMatrix = new FloatMatrix(ninetiethPercentileFalselyCalledGenesByDelta.length, 1);
        FloatMatrix numSigMatrix = new FloatMatrix(numSigGenesByDelta.length, 1);
        FloatMatrix FDRMedianMatrix = new FloatMatrix(FDRmedian.length, 1);
        FloatMatrix FDR90thMatrix = new FloatMatrix(FDR90thPercentile.length, 1);
        
        for (int i = 0; i < deltaGrid.length; i++) {
            deltaGridMatrix.A[i][0] = (float)deltaGrid[i];
            medNumFalseMatrix.A[i][0] = (float)medNumFalselyCalledGenesByDelta[i];
            false90thMatrix.A[i][0] = (float)ninetiethPercentileFalselyCalledGenesByDelta[i];
            numSigMatrix.A[i][0] = (float)numSigGenesByDelta[i];
            FDRMedianMatrix.A[i][0] = (float)FDRmedian[i];
            FDR90thMatrix.A[i][0] = (float)FDR90thPercentile[i];
        }   
        
        FloatMatrix qLowestFDRMatrix = new FloatMatrix(qLowestFDR.length, 1);
        
        for (int i = 0; i < qLowestFDR.length; i++) {
            qLowestFDRMatrix.A[i][0] = (float)(qLowestFDR[i]);
        }       
        
        //************** DONE UP TO HERE 1/21/03 ***********************
        
        //double delta = (double)(0.25*maxDelta); 
        
        
        
        
        
        //SCRIPTING SUPPORT (JCB)
        
        //check if delta value is deliverd
        float scriptDelta = data.getParams().getFloat("delta", -1f);        
        //if we are scripting this can be T or F, else it's T
        boolean graphInteraction = data.getParams().getBoolean("permit-graph-interaction", true);
        
        if(scriptDelta != -1)
            delta = (double)scriptDelta;        
                   
        SAMGraph sg = new SAMGraph(SAMGUI.SAMFrame, studyDesign, dBarValues, sortedDArray,/* dArray,*/ delta, deltaGrid, numSigGenesByDelta, medNumFalselyCalledGenesByDelta, ninetiethPercentileFalselyCalledGenesByDelta, FDRmedian, FDR90thPercentile, true);
        
        if(graphInteraction) { //if true set the sg visible and get delta from there
             sg.setVisible(true);        
            delta = sg.getDelta();
        }         
        //END SCRIPTING SUPPORT CHANGES (JCB)
        
        
        
        
        delta = sg.getDelta();
        SAMState.delta = delta;
        String numSig = sg.getNumSig();
        String numFalseSigMed = sg.getNumFalseSigMed();
        String numFalseSig90th = sg.getNumFalseSig90th();
        String FDRMedian = sg.getFDRMedian();
        String FDR90th =sg.getFDR90th();
        
        double upperCutoff = getCutUp(delta);
        double lowerCutoff = getCutLow(delta);
        
        FloatMatrix dBarMatrix = new FloatMatrix(dBarValues.length, 1);
        FloatMatrix sortedDMatrix = new FloatMatrix(sortedDArray.length, 1);
        
        for (int i = 0; i < dBarValues.length; i++) {
            dBarMatrix.A[i][0] = (float)dBarValues[i];
        }
        for (int i = 0; i < sortedDArray.length; i++) {
            sortedDMatrix.A[i][0] = (float)sortedDArray[i];
        }    

        FloatMatrix foldChangeMatrix = new FloatMatrix(numGenes, 1);
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED)||(studyDesign == SAMInitDialog.TWO_CLASS_PAIRED)) {
            for (int i = 0; i < numGenes; i++) {
                foldChangeMatrix.A[i][0] = (float)getFoldChange(i);
            }
        }
        
        Vector posSigGenes = new Vector();
        Vector negSigGenes = new Vector();
        Vector nonSigGenes = new Vector();
        
        boolean posSigEncountered = false;
        boolean negSigEncountered = false;
        
        int lowestPosSigIndex = 0;
        int highestNegSigIndex = 0;
        
        for (int i = 0; i < dBarValues.length; i++) {
            if ( (dBarValues[i] > 0.0d) && ((sortedDArray[i] - dBarValues[i]) > delta) ) {
                lowestPosSigIndex = i;
                posSigEncountered = true;
                break;
            }
        }
        
        for (int i = 0; i < dBarValues.length; i++) {
            if ( (dBarValues[i] < 0.0d) && ((dBarValues[i] - sortedDArray[i]) > delta) ) {
                highestNegSigIndex = i;
                negSigEncountered = true; 
            }
        }
        
        //System.out.println("sortedDArrayIndices.length = " + sortedDArrayIndices.length);
        
        boolean useFoldChange = sg.useFoldChange();
        double foldChange = 0d;
        if ((useFoldChange)&& ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED))) {
            foldChange = sg.getFoldChangeValue();
            
            if ((posSigEncountered)&&(negSigEncountered)) {
                for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                    if (satisfiesNegFoldChangeCriterion(foldChange, sortedDArrayIndices[i])) {
                        negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    } else {
                        nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    }
                }
                
                for (int i = (highestNegSigIndex + 1); i < lowestPosSigIndex; i++) {
                    nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
                
                for (int i = lowestPosSigIndex; i < dBarValues.length; i++) {
                    if (satisfiesPosFoldChangeCriterion(foldChange, sortedDArrayIndices[i])) {
                        posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    } else {
                        nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    }
                }
                
            } else if((posSigEncountered)&&(!negSigEncountered)) {
                for (int i = 0; i < lowestPosSigIndex; i++) {
                    nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
                
                for (int i = lowestPosSigIndex; i < dBarValues.length; i++) {                    
                    if (satisfiesPosFoldChangeCriterion(foldChange, sortedDArrayIndices[i])) {
                        posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    } else {
                        nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    }
                }
            } else if ((!posSigEncountered) && (negSigEncountered)) {
                for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                    if (satisfiesNegFoldChangeCriterion(foldChange, sortedDArrayIndices[i])) {
                        negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    } else {
                        nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    }
                }
                for (int i = (highestNegSigIndex + 1); i < dBarValues.length; i++) {
                    nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
            } else if ((!posSigEncountered) && (!negSigEncountered)) {
                for (int i = 0; i < dBarValues.length; i++) {
                    nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
            }            
            
        } else {
           
            if ((posSigEncountered)&&(negSigEncountered)) {
                for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                    negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
                
                for (int i = (highestNegSigIndex + 1); i < lowestPosSigIndex; i++) {
                    nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
                
                for (int i = lowestPosSigIndex; i < dBarValues.length; i++) {
                    posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
                
            } else if((posSigEncountered)&&(!negSigEncountered)) {
                for (int i = 0; i < lowestPosSigIndex; i++) {
                    nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
                
                for (int i = lowestPosSigIndex; i < dBarValues.length; i++) {
                    posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
            } else if ((!posSigEncountered) && (negSigEncountered)) {
                for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                    negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
                for (int i = (highestNegSigIndex + 1); i < dBarValues.length; i++) {
                    nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
            } else if ((!posSigEncountered) && (!negSigEncountered)) {
                for (int i = 0; i < dBarValues.length; i++) {
                    nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                }
            }
        }
        

        /*
        for (int i = 0; i < dArray.length; i++) {
            if ( ((sortedDArray[i] - dBarValues[i]) > delta) && (sortedDArray[i] > 0.0d) ) {
                posSigGenes.add(new Integer(sortedDArrayIndices[i]));
            } else if ( ((sortedDArray[i] - dBarValues[i]) < (-1.0d)*delta) && (sortedDArray[i] < 0.0d) ) {
                negSigGenes.add(new Integer(sortedDArrayIndices[i]));
            } else {
                nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
            }
        }
         */
        
        /*
        FloatMatrix fakeMatrix = new FloatMatrix(numGenes, 2); //just to print out d and dBar Values to a file for debugging
        
        
        for (int i = 0; i < numGenes; i++) {
            fakeMatrix.A[i][0] = sortedDArray[i];
            fakeMatrix.A[i][1] = dBarValues[i];
        }
        try {
            printMatrix(fakeMatrix, "dVsdBar.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
        
        //System.out.println("posSigGenes.size() = " + posSigGenes.size() + ", negSigGenes.size() = " + negSigGenes.size() + ", nonSigGenes.size() = " + nonSigGenes.size());
        
        Vector allSigGenes = new Vector();
        allSigGenes.addAll(posSigGenes);
        allSigGenes.addAll(negSigGenes);
        
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
            k = 4; //# of clusters;
        } else {
            k = 2;
        }
        clusters = new Vector[k];
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {        
            clusters[0] = posSigGenes;
            clusters[1] = negSigGenes;
            clusters[2] = allSigGenes;
            clusters[3] = nonSigGenes;
        } else {
            clusters[0] = posSigGenes;
            clusters[1] = nonSigGenes;            
        }
        
	FloatMatrix means = getMeans(clusters);
	FloatMatrix variances = getVariances(clusters, means);        
        
        AlgorithmEvent event = null;
	if (hierarchical_tree) {
	    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, clusters.length, "Calculate Hierarchical Trees");
	    fireValueChanged(event);
	    event.setIntValue(0);
	    event.setId(AlgorithmEvent.PROGRESS_VALUE);
	    fireValueChanged(event);
	}
        
	Cluster result_cluster = new Cluster();
	NodeList nodeList = result_cluster.getNodeList();
	int[] features;        
	for (int i=0; i<clusters.length; i++) {
	    if (stop) {
		throw new AbortException();
	    }
	    features = convert2int(clusters[i]);
	    Node node = new Node(features);
	    nodeList.addNode(node);
	    if (hierarchical_tree) {
		node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
		event.setIntValue(i+1);
		fireValueChanged(event);
	    }
	}  
        
	// prepare the result
	AlgorithmData result = new AlgorithmData();
	result.addCluster("cluster", result_cluster);
	result.addParam("number-of-clusters", String.valueOf(clusters.length));
        result.addParam("numSigGenes", String.valueOf(numSig));
        result.addParam("numFalseSigMed", String.valueOf(numFalseSigMed));
        result.addParam("numFalseSig90th", numFalseSig90th);
        result.addParam("FDRMedian", FDRMedian);
        result.addParam("FDR90th", FDR90th);
        result.addParam("useFoldChange", String.valueOf(useFoldChange));
        result.addParam("foldChangeValue", String.valueOf((float)foldChange));
	//result.addParam("unassigned-genes-exist", String.valueOf(unassignedExists));
	result.addMatrix("clusters_means", means);
	result.addMatrix("clusters_variances", variances);
        result.addParam("delta", String.valueOf((float)delta));
        if (Double.isInfinite(upperCutoff)) {
            result.addParam("upperCutoff", String.valueOf(Float.POSITIVE_INFINITY));
        } else {
            result.addParam("upperCutoff", String.valueOf((float)upperCutoff));
        }
        if (Double.isInfinite(lowerCutoff)) {
            result.addParam("lowerCutoff", String.valueOf(Float.NEGATIVE_INFINITY));
        } else {
            result.addParam("lowerCutoff", String.valueOf((float)lowerCutoff));
        }
        result.addParam("sNought", String.valueOf((float)sNought));
        result.addParam("s0Percentile", String.valueOf((float)s0Percentile));
        result.addParam("pi0Hat", String.valueOf((float)pi0Hat));
        result.addMatrix("dValuesMatrix", dValuesMatrix);
        result.addMatrix("rValuesMatrix", rValuesMatrix);
        result.addMatrix("foldChangeMatrix", foldChangeMatrix);
        result.addMatrix("dBarMatrixX", dBarMatrix);
        result.addMatrix("sortedDMatrixY", sortedDMatrix);
        result.addMatrix("qLowestFDRMatrix", qLowestFDRMatrix);
        result.addMatrix("deltaGridMatrix", deltaGridMatrix);
        result.addMatrix("medNumFalseMatrix", medNumFalseMatrix);
        result.addMatrix("false90thMatrix", false90thMatrix);
        result.addMatrix("numSigMatrix", numSigMatrix);
        result.addMatrix("FDRMedianMatrix", FDRMedianMatrix);
        result.addMatrix("FDR90thMatrix", FDR90thMatrix);
        result.addMatrix("imputedMatrix", imputedMatrix);
        //result.addParam("study-design", String.valueOf(studyDesign));
	return result;        
        //System.out.println("Matrices imputed");
        
        /*
        try {
            printMatrix(expMatrix, "expMatrix.txt");
            printMatrix(imputedMatrix, "imputedMatrix.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
         
    }
    
    public void abort() {
        stop = true;
    }

    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
	NodeValueList nodeList = new NodeValueList();
	AlgorithmData data = new AlgorithmData();
	FloatMatrix experiment = getSubExperiment(this.expMatrix, features);
	data.addMatrix("experiment", experiment);
	data.addParam("distance-function", String.valueOf(this.function));
	data.addParam("distance-absolute", String.valueOf(this.absolute));
	data.addParam("method-linkage", String.valueOf(method));
	HCL hcl = new HCL();
	AlgorithmData result;
	if (genes) {
	    data.addParam("calculate-genes", String.valueOf(true));
	    result = hcl.execute(data);
	    validate(result);
	    addNodeValues(nodeList, result);
	}
	if (experiments) {
	    data.addParam("calculate-genes", String.valueOf(false));
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

    private int[] convert2int(Vector source) {
	int[] int_matrix = new int[source.size()];
	for (int i=0; i<int_matrix.length; i++) {
	    int_matrix[i] = (int)((Integer)source.get(i)).intValue();
	}
	return int_matrix;
    }
    
    private FloatMatrix getMeans(Vector[] clusters) {
	FloatMatrix means = new FloatMatrix(clusters.length, numExps);
	FloatMatrix mean;
	for (int i=0; i<clusters.length; i++) {
	    mean = getMean(clusters[i]);
	    means.A[i] = mean.A[0];
	}
	return means;
    }   
    
    private FloatMatrix getMean(Vector cluster) {
	FloatMatrix mean = new FloatMatrix(1, numExps);
	float currentMean;
	int n = cluster.size();
	int denom = 0;
	float value;
	for (int i=0; i<numExps; i++) {
            //System.out.println("getMean(): i = " + i);
	    currentMean = 0f;
	    denom = 0;
	    for (int j=0; j<n; j++) {
                //System.out.println("getMean(): j = " + j);
		value = expMatrix.get(((Integer) cluster.get(j)).intValue(), i);
		if (!Float.isNaN(value)) {
		    currentMean += value;
		    denom++;
		}
	    }
	    mean.set(0, i, currentMean/(float)denom);
	}
	
	return mean;
    }  
    
    private FloatMatrix getVariances(Vector[] clusters, FloatMatrix means) {
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
    
    private float getSampleNormalizedSum(Vector cluster, int column, float mean) {
	final int size = cluster.size();
	float sum = 0f;
	float value;
	validN = 0;
	for (int i=0; i<size; i++) {
	    value = expMatrix.get(((Integer) cluster.get(i)).intValue(), column);
	    if (!Float.isNaN(value)) {
		sum += Math.pow(value-mean, 2);
		validN++;
	    }
	}
	return sum;
    }  
    
    private float getSampleVariance(Vector cluster, int column, float mean) {
	return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
	
    } 
    
    
    private boolean belongsInArray(int i, int[] arr) {
	boolean belongs = false;
	
	for (int j = 0; j < arr.length; j++) {
	    if (i == arr[j]) {
		belongs = true;
		break;
	    }
	}
	
	return belongs;
    }    
    
    private double getFoldChange(int gene) {
        float[] currentGene = new float[numExps];
        for (int i = 0; i < numExps; i++) {
            currentGene[i] = imputedMatrix.A[gene][i];
        }
        double unloggedCurrentGene[] = new double[currentGene.length];
        
        for (int i = 0; i < unloggedCurrentGene.length; i++) {
            unloggedCurrentGene[i] = Math.pow(2, (double)currentGene[i]);
        }
        
        double[] groupAValues = new double[1];
        double[] groupBValues = new double[1];

        if (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) {
            int groupACounter = 0;
            int groupBCounter = 0;
            
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                    groupACounter++;
                } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
                    groupBCounter++;
                }
            }
            
            groupAValues = new double[groupACounter];
            groupBValues = new double[groupBCounter];
            int groupAIndex = 0;
            int groupBIndex = 0;
            
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                    groupAValues[groupAIndex] = unloggedCurrentGene[i];
                    groupAIndex++;
                } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
                    groupBValues[groupBIndex] = unloggedCurrentGene[i];
                    groupBIndex++;
                }
            }
            
        } else if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
            groupAValues = new double[pairedGroupAExpts.length];
            groupBValues = new double[pairedGroupBExpts.length];
            
            for (int i = 0; i < groupAValues.length; i++) {
                groupAValues[i] = unloggedCurrentGene[pairedGroupAExpts[i]];
                groupBValues[i] = unloggedCurrentGene[pairedGroupBExpts[i]];
            }
        }
        
        double meanA = getMean(groupAValues);
        double meanB = getMean(groupBValues);   
        
        return (double)(meanB/meanA);
    }
    
    private boolean satisfiesPosFoldChangeCriterion(double fold, int gene) {
        //System.out.println("Pos: fold = " + fold);
        boolean satisfies = false;
        float[] currentGene = new float[numExps];
        for (int i = 0; i < numExps; i++) {
            currentGene[i] = imputedMatrix.A[gene][i];
        }
        double unloggedCurrentGene[] = new double[currentGene.length];
        
        for (int i = 0; i < unloggedCurrentGene.length; i++) {
            unloggedCurrentGene[i] = Math.pow(2, (double)currentGene[i]);
        }
        
        double[] groupAValues = new double[1];
        double[] groupBValues = new double[1];        

        if (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) {
            int groupACounter = 0;
            int groupBCounter = 0;
            
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                    groupACounter++;
                } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
                    groupBCounter++;
                }
            }
            
            groupAValues = new double[groupACounter];
            groupBValues = new double[groupBCounter];
            int groupAIndex = 0;
            int groupBIndex = 0;
            
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                    groupAValues[groupAIndex] = unloggedCurrentGene[i];
                    groupAIndex++;
                } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
                    groupBValues[groupBIndex] = unloggedCurrentGene[i];
                    groupBIndex++;
                }
            }
        } else if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
            groupAValues = new double[pairedGroupAExpts.length];
            groupBValues = new double[pairedGroupBExpts.length];
            
            for (int i = 0; i < groupAValues.length; i++) {
                groupAValues[i] = unloggedCurrentGene[pairedGroupAExpts[i]];
                groupBValues[i] = unloggedCurrentGene[pairedGroupBExpts[i]];
            }            
        }
        
        double meanA = getMean(groupAValues);
        double meanB = getMean(groupBValues);
        
        if ((meanA <= 0)|| (meanB <= 0)) {
            return false;
        }
        
        if (Math.abs((double)(meanB/meanA)) >=  fold) {
            //System.out.println("Pos: gene " + gene + ", fold change = " +  (float)Math.abs((double)(meanB/meanA)) + ", getFoldChange(" + gene + ") = " + getFoldChange(gene) );
            return true;
        } else {
            return false;
        }
        
        //return false;
    }

    private boolean satisfiesNegFoldChangeCriterion(double fold, int gene) {
        //System.out.println("Neg: fold = " + fold);
        boolean satisfies = false;
        float[] currentGene = new float[numExps];
        for (int i = 0; i < numExps; i++) {
            currentGene[i] = imputedMatrix.A[gene][i];
        }
        double unloggedCurrentGene[] = new double[currentGene.length];
        
        for (int i = 0; i < unloggedCurrentGene.length; i++) {
            unloggedCurrentGene[i] = Math.pow(2, (double)currentGene[i]);
        }
        
        double[] groupAValues = new double[1];
        double[] groupBValues = new double[1];        

        if (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) {
            int groupACounter = 0;
            int groupBCounter = 0;
            
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                    groupACounter++;
                } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
                    groupBCounter++;
                }
            }
            
            groupAValues = new double[groupACounter];
            groupBValues = new double[groupBCounter];
            int groupAIndex = 0;
            int groupBIndex = 0;
            
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                    groupAValues[groupAIndex] = unloggedCurrentGene[i];
                    groupAIndex++;
                } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
                    groupBValues[groupBIndex] = unloggedCurrentGene[i];
                    groupBIndex++;
                }
            }
            
        } else if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
            groupAValues = new double[pairedGroupAExpts.length];
            groupBValues = new double[pairedGroupBExpts.length];
            
            for (int i = 0; i < groupAValues.length; i++) {
                groupAValues[i] = unloggedCurrentGene[pairedGroupAExpts[i]];
                groupBValues[i] = unloggedCurrentGene[pairedGroupBExpts[i]];
            }              
        }
        
        double meanA = getMean(groupAValues);
        double meanB = getMean(groupBValues);
        
        if ((meanA <= 0)|| (meanB <= 0)) {
            return false;
        }
        
        if (Math.abs((double)(meanB/meanA)) <= (double)(1/fold)) {
            //System.out.println("Neg: gene " + gene + ", fold change = " +  (float)Math.abs((double)(meanA/meanB)) + ", getFoldChange(" + gene + ") = " + getFoldChange(gene) );
            return true;
        } else {
            return false;
        }
        
        //return false;
    }    
    
    private void printIntArray(int[] intArray) {
        for (int i = 0; i < intArray.length; i++) {
            System.out.print(""+ intArray[i]);
        }
        System.out.println();
    }
    
    private boolean isSignificant(int gene, double currentCutUp, double currentCutLow) {
        boolean isSig = false;
        if ((dArray[gene] >= currentCutUp) || (dArray[gene] <= currentCutLow)) {
            isSig = true;
        }
        return isSig;
    }
    
  
    private double getMedNumFalselyCalledGenesByDelta(double permDVals[][], double currentCutUp, double currentCutLow) {
        double[] falselyCalledGenes = new double[permDVals.length];
        for (int i = 0; i < falselyCalledGenes.length; i++) {
            double[] currentPerm = new double[permDVals[i].length];
            for (int j = 0; j < currentPerm.length; j++) {
                currentPerm[j] = permDVals[i][j];
            }
            int numFalse = 0;
            
            for (int j = 0; j < currentPerm.length; j++) {
                if ((currentPerm[j] >= currentCutUp) || (currentPerm[j] <= currentCutLow)) {
                    numFalse++;
                }
            }
            falselyCalledGenes[i] = (double)numFalse;
        }
        
        return getMedian(falselyCalledGenes);
    }
    
    private double getNinetiethPercentileFalselyCalledGenesByDelta(double permDVals[][], double currentCutUp, double currentCutLow) {
        double[] falselyCalledGenes = new double[permDVals.length];
        for (int i = 0; i < falselyCalledGenes.length; i++) {
            double[] currentPerm = new double[permDVals[i].length];
            for (int j = 0; j < currentPerm.length; j++) {
                currentPerm[j] = permDVals[i][j];
            }
            int numFalse = 0;
            
            for (int j = 0; j < currentPerm.length; j++) {
                if ((currentPerm[j] >= currentCutUp) || (currentPerm[j] <= currentCutLow)) {
                    numFalse++;
                }
            }
            falselyCalledGenes[i] = (double)numFalse;
        }
        
        QSort sortFalselyCalled = new QSort(falselyCalledGenes);
        double[] sortedFalselyCalledGenes = sortFalselyCalled.getSortedDouble();
        int ninetiethPercentileIndex = (int)Math.round((sortedFalselyCalledGenes.length)*0.90 - 1);
        
        return sortedFalselyCalledGenes[ninetiethPercentileIndex];
        
        //return getMedian(falselyCalledGenes);
    }    
    
    private double getCutUp(double currentDelta) {
        boolean posSigEncountered = false;
        //boolean negSigEncountered = false;
        double cutUp = Double.POSITIVE_INFINITY;
        int lowestPosSigIndex = 0;
        int highestNegSigIndex = 0;
        
        for (int i = 0; i < dBarValues.length; i++) {
            if ( (dBarValues[i] > 0.0d) && ((sortedDArray[i] - dBarValues[i]) > currentDelta) ) {
                lowestPosSigIndex = i;
                posSigEncountered = true;
                break;
            }
        }
        
        if (posSigEncountered) {
            cutUp = sortedDArray[lowestPosSigIndex];
        } else {
            cutUp = Double.POSITIVE_INFINITY;
        }
        
        return cutUp;
    }
    
    
    private double getCutLow(double currentDelta) {
       int highestNegSigIndex = 0; 
       boolean negSigEncountered = false;
       double cutLow = Double.NEGATIVE_INFINITY;
       
        for (int i = 0; i < dBarValues.length; i++) {
            if ( (dBarValues[i] < 0.0d) && ((dBarValues[i] - sortedDArray[i]) > currentDelta) ) {
                highestNegSigIndex = i;
                negSigEncountered = true; 
            }
        }  
       
       if (negSigEncountered) {
           cutLow = sortedDArray[highestNegSigIndex];
       } else {
           cutLow = Double.NEGATIVE_INFINITY;
       }
       return cutLow;
    }
    
    private int getNumSigGenesByDelta(double currentCutUp, double currentCutLow) {
        int numSigGenes = 0;
        for (int i = 0; i < sortedDArray.length; i++) {
            if ((sortedDArray[i] >= currentCutUp)||(sortedDArray[i] <= currentCutLow)) {
                numSigGenes++;
            }
        }
        return numSigGenes;
    }
    
    private FloatMatrix getPermutedMatrix(FloatMatrix inputMatrix, int[] permExpts) {
        FloatMatrix permutedMatrix = new FloatMatrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
        for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
            for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                permutedMatrix.A[i][j] = inputMatrix.A[i][permExpts[j]];
            }
        }
        return permutedMatrix;
    }
    
    private FloatMatrix getOneClassPermMatrix(FloatMatrix inputMatrix, boolean[] changeSign) {
        FloatMatrix permutedMatrix = new FloatMatrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());

        for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
            for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                if (changeSign[j]) {
                    permutedMatrix.A[i][j] = (float)(inputMatrix.A[i][j] - 2.0f*(inputMatrix.A[i][j] - oneClassMean));
                } else {
                    permutedMatrix.A[i][j] = inputMatrix.A[i][j];
                }
            }
        }
        
        return permutedMatrix;
    }
    
    private int[] permuteWithinPairs(long seed) {
        int[] permutedValues = new int[numExps];
        for (int i = 0; i < permutedValues.length; i++) {
            permutedValues[i] = i;
        }
        
        int temp;
        Random generator2 =new Random(seed);
        for (int i = 0; i < pairedGroupAExpts.length; i++) {
            
            boolean swap = generator2.nextBoolean();
            //System.out.print(swap + " ");
            if (swap) {
                temp = permutedValues[pairedGroupBExpts[i]];
                permutedValues[pairedGroupBExpts[i]] = permutedValues[pairedGroupAExpts[i]];
                permutedValues[pairedGroupAExpts[i]] = temp;
            }
        }
        
        try {
            Thread.sleep(10);
        } catch (Exception exc) {
            exc.printStackTrace();
        }        
        
        return permutedValues;
    }
    
    private int[] permuteWithinPairsAllPerms(int num) {
        int[] permutedValues = new int[numExps];
        for (int i = 0; i < permutedValues.length; i++) {
            permutedValues[i] = i;
        }
        
        int temp;
        //Random generator2 =new Random(seed);
        boolean[] changeSign = getChangeSignArrayForAllPairedPerms(num);
        for (int i = 0; i < pairedGroupAExpts.length; i++) {
            
            boolean swap = changeSign[i];
            //System.out.print(swap + " ");
            if (swap) {
                temp = permutedValues[pairedGroupBExpts[i]];
                permutedValues[pairedGroupBExpts[i]] = permutedValues[pairedGroupAExpts[i]];
                permutedValues[pairedGroupAExpts[i]] = temp;
            }
        }
        /*
        try {
            Thread.sleep(10);
        } catch (Exception exc) {
            exc.printStackTrace();
        } 
         */       
        
        return permutedValues;
    }    
    
    boolean[] getChangeSignArrayForAllPairedPerms(int num) {
        boolean[] permutArray = new boolean[pairedGroupAExpts.length];
        
        for (int i = 0; i < permutArray.length; i++) {
            permutArray[i] = false;
        }
        
        int numPairs = pairedGroupAExpts.length;
        
        String binaryString = Integer.toBinaryString(num);
        //System.out.println(binaryString);
        char[] binArray = binaryString.toCharArray();
        if (binArray.length < numPairs) {
            Vector binVector = new Vector();
            for (int i = 0; i < (numPairs - binArray.length); i++) {
                binVector.add(new Character('0'));
            }
            
            for (int i = 0; i < binArray.length; i++) {
                binVector.add(new Character(binArray[i]));
            }
            binArray = new char[binVector.size()]; 
            
            for (int i = 0; i < binArray.length; i++) {
                binArray[i] = ((Character)(binVector.get(i))).charValue();
            }
        } 
        /*
        for (int i = 0; i < binArray.length; i++) {
            System.out.print(binArray[i]);
        }
        System.out.println();
         */
        //int counter = 0;
        
        for (int i = 0; i < permutArray.length; i++) {

            if (binArray[i] == '1') {
                permutArray[i] = true;
            } else {
                permutArray[i] = false;
            }

        }
        /*
        for (int i = 0; i < oneClassPermutArray.length; i++) {
            System.out.print(oneClassPermutArray[i] + " ");
        }
        System.out.println();
        */
        return permutArray;
    }    
    
    private int[] getPermutedValues(int arrayLength, int[] validArray) {//returns an integer array of length "arrayLength", with the valid values (the currently included experiments) permuted
        int[] permutedValues = new int[arrayLength];
        for (int i = 0; i < permutedValues.length; i++) {
            permutedValues[i] = i;
        }
        /*
        Vector validExpts = new Vector();
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED)||(studyDesign == SAMInitDialog.MULTI_CLASS)) {
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] != 0) {
                    validExpts.add(new Integer(i));
                }
            }
        } else if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
            for (int i = 0; i < inSurvivalAnalysis.length; i++) {
                if (inSurvivalAnalysis[i]) {
                    validExpts.add(new Integer(i));
                }
            }
        }
        
        int[] validArray = new int[validExpts.size()];
        for (int i = 0; i < validArray.length; i++) {
            validArray[i] = ((Integer)(validExpts.get(i))).intValue();
        }
         */
        
        int[] permutedValidArray = new int[validArray.length];
        for (int i = 0; i < validArray.length; i++) {
            permutedValidArray[i] = validArray[i];
        }
        
        for (int i = permutedValidArray.length; i > 1; i--) {
            Random generator2 =new Random();
            //Random generator2 = new Random(randomSeeds[i - 2]);
            int randVal = generator2.nextInt(i - 1);
            int temp = permutedValidArray[randVal];
            permutedValidArray[randVal] = permutedValidArray[i - 1];
            permutedValidArray[i - 1] = temp;
        }  
        
        for (int i = 0; i < validArray.length; i++) {
            //permutedValues[validArray[i]] = permutedValues[permutedValidArray[i]];
            permutedValues[validArray[i]] = permutedValidArray[i];
        }
        
        /*
        long[] randomSeeds = new long[permutedValues.length - 1];
       
        
        for (int i = 0; i < randomSeeds.length; i++) {
            Random generator = new Random(i);
            randomSeeds[i] = generator.nextLong();
            //System.out.println("randomSeeds[" + i + "] =" + randomSeeds[i]);
        }
        */
        /*
        for (int i = permutedValues.length; i > 1; i--) {
            Random generator2 =new Random();
            //Random generator2 = new Random(randomSeeds[i - 2]);
            int randVal = generator2.nextInt(i - 1);
            int temp = permutedValues[randVal];
            permutedValues[randVal] = permutedValues[i - 1];
            permutedValues[i - 1] = temp;
        }
        */
        
        try {
            Thread.sleep(10);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        
        
        return permutedValues;
        
    }
    
    private boolean[] getOneClassChangeSignArray(long seed, int[] validExpts) {
        boolean[] changeSignArray = new boolean[numExps];
        for (int i = 0; i < changeSignArray.length; i++) {
            changeSignArray[i] = false;            
        }
        
        Random generator2 = new Random(seed);
        for (int i = 0; i < validExpts.length; i++) {
            changeSignArray[validExpts[i]] = generator2.nextBoolean();
        }
        
        return changeSignArray;
    }
    
    private boolean[] getOneClassChangeSignArrayAllUniquePerms(int num, int[] validExpts) {
        boolean[] changeSignArray = new boolean[numExps];
        for (int i = 0; i < changeSignArray.length; i++) {
            changeSignArray[i] = false;            
        }
        
        //Random generator2 = new Random(seed);
        int numValidExps = validExpts.length;

        String binaryString = Integer.toBinaryString(num);
        //System.out.println(binaryString);
        char[] binArray = binaryString.toCharArray();
        if (binArray.length < numValidExps) {
            Vector binVector = new Vector();
            for (int i = 0; i < (numValidExps - binArray.length); i++) {
                binVector.add(new Character('0'));
            }
            
            for (int i = 0; i < binArray.length; i++) {
                binVector.add(new Character(binArray[i]));
            }
            binArray = new char[binVector.size()]; 
            
            for (int i = 0; i < binArray.length; i++) {
                binArray[i] = ((Character)(binVector.get(i))).charValue();
            }
        }
        
        for (int i = 0; i < validExpts.length; i++) {
            if (binArray[i] == '1') {
                changeSignArray[validExpts[i]] = true;
            } else {
                changeSignArray[validExpts[i]] = false;
            }
        }
        
        return changeSignArray;
    }    
    
    private float getMax(float[] array) {
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (max < array[i]) {
                max = array[i];
            }
        }
        return max;
    }
    
    private float getMin(float[] array) {
        float min = Float.POSITIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (min > array[i]) {
                min = array[i];
            }
        }
        return min;
    }   
    
    private double getMax(double[] array) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (max < array[i]) {
                max = array[i];
            }
        }
        return max;
    }
    
    private double getMin(double[] array) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (min > array[i]) {
                min = array[i];
            }
        }
        return min;
    }    
    
    private double rTwoClassUnpaired(int gene, FloatMatrix matrix) {
        float[] groupAValues, groupBValues;
        int groupACount = 0;
        int groupBCount = 0;
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                groupACount++;
            } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
                groupBCount++;
            }
        } 
        
        groupAValues = new float[groupACount];
        groupBValues = new float[groupBCount];
        
        groupACount = 0;
        groupBCount = 0;
        
        for (int i = 0; i < groupAssignments.length; i++) {
        
        /*    
        if (passedThisPoint) {
                System.out.println("rTwoClassUnpaired(): groupACount = " + groupACount + ", groupBCount = " + groupBCount + ", gene = " + gene + ", i = " + i);
            }
         */
            if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                groupAValues[groupACount] = matrix.A[gene][i];
                groupACount++;
            } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
                groupBValues[groupBCount] = matrix.A[gene][i];
                groupBCount++;                
            }
        }
        
        double r = (double)(getMean(groupBValues) - getMean(groupAValues));
        //System.out.println("rTwoClassUnpaired(" + gene + ") = " + r);
        //return Math.abs(r);
        return r;
    }
    
    private double rTwoClassPaired(int gene, FloatMatrix matrix) {
        double zk = 0;
        
        for (int k = 0; k < pairedGroupAExpts.length; k++) {
            zk = zk + (matrix.A[gene][pairedGroupBExpts[k]] - matrix.A[gene][pairedGroupAExpts[k]]);
        }
        
        return (double)(zk/(double)(pairedGroupAExpts.length));
    }
    
    private double rOneClass(int gene, FloatMatrix matrix) {
        int validN = 0;
        double xiBar = 0d;
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == 1) {
                validN++;
                xiBar = xiBar + (matrix.A[gene][i] - oneClassMean);
            }
        }
        
        return (double)(xiBar/(double)validN);
        
    }
    
    private double rMultiClass(int gene, FloatMatrix matrix) {
       float[] geneValues = new float[matrix.getColumnDimension()];
       
       for (int i = 0; i < geneValues.length; i++) {
           geneValues[i] = matrix.A[gene][i];
       }
       
       int[] groupCounts = getGroupCounts();
       double[] xBarIK = getXBarIk(geneValues);
       
       double sigmaNK = 0d;
       double piNK = 1d;
       for (int i = 0; i < groupCounts.length; i++) {
           sigmaNK = sigmaNK + groupCounts[i];
           piNK = piNK*groupCounts[i];
       }
       
       double xBar = (double)getMean(geneValues);
       
       double term2 = 0d;
       
       for (int i = 0; i < groupCounts.length; i++) {
           term2 = term2 + groupCounts[i]*Math.pow((xBarIK[i] - xBar), 2);
       }
       
       double r = Math.pow((sigmaNK/piNK)*term2, 0.5d);
       return r;
    }

    private double sTwoClassPaired(int gene, FloatMatrix matrix) {
        double r = rTwoClassPaired(gene, matrix);
        double num = 0;
        
        for (int k = 0; k < pairedGroupAExpts.length; k++) {
            num = num + Math.pow(((double)(matrix.A[gene][pairedGroupBExpts[k]]) - (double)(matrix.A[gene][pairedGroupAExpts[k]]) - r), 2d);
        }
        
        int K = pairedGroupAExpts.length;
        return Math.sqrt((double)num/(double)(K*(K - 1)));
    }
    
    private double sOneClass(int gene, FloatMatrix matrix) {
        double xiBar = rOneClass(gene, matrix);
        double sValue = 0d;
        int validN = 0;
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == 1) {
                validN++;
                sValue = sValue + Math.pow((matrix.A[gene][i] - xiBar), 2);
            }
        }
        
        return Math.sqrt( (double)(sValue/(double)(validN*(validN - 1))) );
    }
    
    private double sMultiClass(int gene, FloatMatrix matrix) {
       float[] geneValues = new float[matrix.getColumnDimension()];
       for (int i = 0; i < geneValues.length; i++) {
           geneValues[i] = matrix.A[gene][i];
       }
       double[][] geneValuesByGroups = getGeneValuesByGroups(geneValues);
       /*
       for (int i = 0; i < geneValues.length; i++) {
           geneValues[i] = matrix.A[gene][i];
       }
        */
       
       int[] groupCounts = getGroupCounts();
       double[] xBarIK = getXBarIk(geneValues);
       
       int sumNkMinusOne = 0;
       
       for (int i = 0; i < groupCounts.length; i++) {
           sumNkMinusOne = sumNkMinusOne + (groupCounts[i] - 1);
       }
       
       double term1 = (double)(1/(double)sumNkMinusOne);
       
       double term2 = 0d;
       
       for (int i = 0; i < groupCounts.length; i++) {
           term2 = term2 + (double)(1/(double)groupCounts[i]);
       }
       
       double term3 = 0;
       
       for (int k = 0; k < groupCounts.length; k++) {
           double currentTerm = 0d;
           for (int j = 0; j < groupCounts[k]; j++) {
               currentTerm = currentTerm + Math.pow((geneValuesByGroups[k][j] - xBarIK[k]), 2.0d);
           }
           
           term3 = term3 + currentTerm;
       }
       
       return Math.pow(term1*(term2*term3), 0.5d);
    }
    
    private double rCensoredSurvival(int gene, FloatMatrix matrix) {
        double r = 0;
        
        //double[] zkArray = getZkArray();
        //int[] dkArray = getDkArray();
        
        for (int k = 0; k < zkArray.length; k++) {
            r = r + (getXStarIK(gene, matrix, k) - dkArray[k]*getXBarIK(gene, matrix, k));
        }
        
        return r;
    }
    
    private double sCensoredSurvival(int gene, FloatMatrix matrix) {
        double s = 0;
        //double[] zkArray = getZkArray();
        //int[] dkArray = getDkArray(); 
        //int[][] rkArray = getRkArray();
        
        for (int k = 0; k < zkArray.length; k++) {
            double term1 = (double)(dkArray[k]/(double)(rkArray[k].length));
            double term2 = 0;
            double xBarIK = getXBarIK(gene, matrix, k);
            for (int l = 0; l < rkArray[k].length; l++) {
                int currentExp = rkArray[k][l];
                //for (int i = 0; i < numGenes; i++) {
                    term2 = term2 + Math.pow(((double)(matrix.A[gene][currentExp]) - xBarIK), 2) ;
                //}
            }
            
            s = s + term1*term2;
        }
        
        return Math.pow(s, 0.5);
    }
    
    
    
    private int[][] getRkArray() { // indices of samples (observations) at unique death times zk
        //double[] zkArray = getZkArray();
        
        int[][] rkArray = new int[zkArray.length][];
        
        for (int i = 0; i < zkArray.length; i++) {
            Vector currentRkVector = new Vector();
            double currentTime = zkArray[i];
            for (int j = 0; j < numExps; j++) {
                if ((inSurvivalAnalysis[j]) && (survivalTimes[j] >= currentTime)) {
                    currentRkVector.add(new Integer(j));
                }
            }
            
            rkArray[i] = new int[currentRkVector.size()];
            
            for (int j = 0; j < rkArray[i].length; j++) {
                rkArray[i][j] = ((Integer)currentRkVector.get(j)).intValue();
            }
        }
        
        return rkArray;
    }
    

    private double[] getZkArray() { // unique death times zk
        Vector deathTimes = new Vector();
                
        for (int i = 0; i < survivalTimes.length; i++) {
            if ((inSurvivalAnalysis[i]) && (!isCensored[i])) {
                deathTimes.add(new Double(survivalTimes[i]));
            }
        }
        
        HashSet s = new HashSet(deathTimes);
        Vector deathTimesSet = new Vector(s);   
        
        double[] zkArray = new double[deathTimesSet.size()];
        
        for (int i = 0; i < zkArray.length; i++) {
            zkArray[i] = ((Double)deathTimesSet.get(i)).doubleValue();
        }
        
        return zkArray;
    }
    
    private int[] getDkArray() { // array of number of deaths at each unique death time zk
        //double[] zkArray = getZkArray();
        int[] dkArray = new int[zkArray.length];
        
        for (int i = 0; i < zkArray.length; i++) {
            int counter = 0;
            for (int j = 0; j < numExps; j++) {
                if ((inSurvivalAnalysis[j]) && (survivalTimes[j] == zkArray[i]) && (!isCensored[j])) {
                    counter++;
                }
            }
            
            dkArray[i] = counter;
        }
        
        return dkArray;
    }
    
    private double getXStarIK(int gene, FloatMatrix matrix, int k) {
        double x = 0;
        //double[] zkArray = getZkArray(); 
        double zk = zkArray[k];
        for (int i = 0; i < inSurvivalAnalysis.length; i++) {
            if (inSurvivalAnalysis[i] && (survivalTimes[i] == zk) && (!isCensored[i])) {
                //for (int j = 0; j < numGenes; j++) {
                    x = x + (double)matrix.A[gene][i];
                    
                //}
            }
        }
        
        return x;
    }
    
    private double getXBarIK(int gene, FloatMatrix matrix, int k) { // for censored survival designs 
        double x = 0;
        //int counter = 0;
        //double[] zkArray = getZkArray();
        //int[][] rkArray = getRkArray();
        double zk = zkArray[k]; 
        int mk = rkArray[k].length;
        for (int i = 0; i < inSurvivalAnalysis.length; i++) {
            if (inSurvivalAnalysis[i] && (survivalTimes[i] >= zk)) {
                //counter++;
                //for (int j = 0; j < numGenes; j++) {
                    x = x + (double)matrix.A[gene][i];
                    
                //}
            }
        }
        
        return (double)(x/mk);
    }    
    
    
    
    private double[][] getGeneValuesByGroups(float[] geneValues) {
        int[] groupCounts = getGroupCounts();
        double[][] geneValuesByGroups = new double[groupCounts.length][];
        
        for (int i = 0; i < geneValuesByGroups.length; i++) {
            geneValuesByGroups[i] = new double[groupCounts[i]];
        }
        
        int[] groupCounters = new int[groupCounts.length];
        for (int i = 0; i < groupCounters.length; i++) {
            groupCounters[i] = 0;
        }
        
        for (int i = 0; i < groupAssignments.length; i++) {
            int currentGroup = groupAssignments[i];
            if (currentGroup != 0) {
                geneValuesByGroups[currentGroup - 1][groupCounters[currentGroup -1]] = (double)geneValues[i];
                groupCounters[currentGroup - 1]++;
            }
        } 
        
        return geneValuesByGroups;
    }
    
    
    private double[] getXBarIk(float[] geneValues) { //for multi-class model
        int[] groupCounts = getGroupCounts();
        float[][] geneValuesByGroups = new float[groupCounts.length][];
        
        for (int i = 0; i < geneValuesByGroups.length; i++) {
            geneValuesByGroups[i] = new float[groupCounts[i]];
        }
        
        int[] groupCounters = new int[groupCounts.length];
        for (int i = 0; i < groupCounters.length; i++) {
            groupCounters[i] = 0;
        }
        
        for (int i = 0; i < groupAssignments.length; i++) {
            int currentGroup = groupAssignments[i];
            if (currentGroup != 0) {
                geneValuesByGroups[currentGroup - 1][groupCounters[currentGroup -1]] = geneValues[i];
                groupCounters[currentGroup - 1]++;
            }
        }
        
        float[] groupMeans = new float[groupCounts.length];
        //DONE UP TO HERE MARCH 03, 2003
        
        for (int i = 0; i < groupMeans.length; i++) {
            groupMeans[i] = getMean(geneValuesByGroups[i]);
        }
        
        double[] convertedGroupMeans = new double[groupMeans.length];
        
        for (int i = 0; i < convertedGroupMeans.length; i++) {
            convertedGroupMeans[i] = (double)(groupMeans[i]);
        }
        return convertedGroupMeans;
    }
    
    private int[] getGroupCounts() { // for multi-class model
       int[] groupCounts = new int[numMultiClassGroups];
       for (int i = 0; i < groupCounts.length; i++) {
           groupCounts[i] = 0;
       }
       for (int i = 0; i < groupAssignments.length; i++) {
           int currentGroup = groupAssignments[i];
           if (currentGroup != 0) {
            groupCounts[currentGroup - 1]++;
           }
       }
       
       return groupCounts;        
    }
    
    
    
    
    private double sTwoClassUnpaired(int gene, FloatMatrix matrix) {
        float[] groupAValues, groupBValues;
        int groupACount = 0;
        int groupBCount = 0;
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                groupACount++;
            } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
                groupBCount++;
            }
        } 
        
        groupAValues = new float[groupACount];
        groupBValues = new float[groupBCount];
        //System.out.println("sTwoClassUnpaired(): groupACount = " + groupACount);
        //System.out.println("sTwoClassUnpaired(): groupBCount = " + groupBCount);
        
        groupACount = 0;
        groupBCount = 0;
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                groupAValues[groupACount] = matrix.A[gene][i];
                groupACount++;
            } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
                groupBValues[groupBCount] = matrix.A[gene][i];
                groupBCount++;                
            }
        }
        
        /*
        System.out.print("sTwoClassUnpaired(): groupAValues = ");
        for (int i = 0; i < groupAValues.length; i++) {
            System.out.print(groupAValues[i] + " ");
        }
        System.out.println();
        
        System.out.print("sTwoClassUnpaired(): groupBValues = ");
        for (int i = 0; i < groupBValues.length; i++) {
            System.out.print(groupBValues[i] + " ");
        }
        System.out.println();
         */        
        
        int nA = groupAValues.length;
        int nB = groupBValues.length;
        
        float meanA = getMean(groupAValues);
        float meanB = getMean(groupBValues);
       // System.out.println("nA = " + nA + ", nB = " +nB + ", meanA = " + meanA + ", meanB = " + meanB);
        
        float varA = getVar(groupAValues);
        float varB = getVar(groupBValues);
        //System.out.println("varA = " + varA + ", varB = " + varB);
        
        //double ssquare = (1/nA + 1/nB)*(varA + varB)/(nA + nB - 2);
        //System.out.println("ssquare = " + ssquare);
        //System.out.println("((float)1/nA + (float)1/nB) = " + ((float)1/nA +(float)1/nB) + ", (varA + varB) = " + (varA +varB) + ", (nA + nB - 2) = " + (nA + nB - 2));
        
        double s = Math.sqrt(((float)1/nA + (float)1/nB)*(varA + varB)/(nA + nB - 2));
        
        //float ss = (float)s;
        /*
        if (Float.isNaN(ss)){
            System.out.println("sTwoClassUnpaired(" + gene + ") = " + ss);
            System.out.println("nA = " + nA + ", nB = " + nB + ", varA = " + varA + ", varB = " + varB);
        }
         */
        //System.out.println("s = " +s);
        return s;
        //return ss;
    }
    
    private float getVar(float[] values) {
        float mean = getMean(values);
        float var = 0;
        
        for (int i = 0; i < values.length; i++) {
            if (!Float.isNaN(values[i])) {
                float sqDev = (values[i] - mean)*(values[i] - mean);
                var = var + sqDev;
                
            }
        }
        /*
        if (Float.isNaN(var)) {
            System.out.print("getVar(): values[] = ");
            for (int i = 0; i < values.length; i++) {
                System.out.print(" " + values[i]);
                //System.out.println();
            }
            System.out.println();
            System.out.println("getVar(): mean = " + mean);
        }
         */
        
        
        return var;
    }
    
    private double getVar(double[] values) {
        double mean = getMean(values);
        double var = 0;
        for (int i = 0; i < values.length; i++) {
            if (!Double.isNaN(values[i])) {
                double sqDev = (values[i] - mean)*(values[i] - mean);
                var = var + sqDev;
            }
        }
        /*
        if (Float.isNaN(var)) {
            System.out.print("getVar(): values[] = ");
            for (int i = 0; i < values.length; i++) {
                System.out.print(" " + values[i]);
                //System.out.println();
            }
            System.out.println();
            System.out.println("getVar(): mean = " + mean);
        }
         */
        
        return var;
    }    
    
    private float getVariance(float[] values) {
        float mean = getMean(values);
        float var = 0;
        int validN = 0;
        for (int i = 0; i < values.length; i++) {
            if (!Float.isNaN(values[i])) {
                float sqDev = (values[i] - mean)*(values[i] - mean);
                var = var + sqDev;
                validN++;
            }
        }
        
        if (validN == 1) {
            return 0.0f;
        } else if (validN < 1) {
            return Float.NaN;
        }
        /*
        if (Float.isNaN(var)) {
            System.out.print("getVar(): values[] = ");
            for (int i = 0; i < values.length; i++) {
                System.out.print(" " + values[i]);
                //System.out.println();
            }
            System.out.println();
            System.out.println("getVar(): mean = " + mean);
        }
         */
        
        
        return var/validN;
    }
    
    private double getVariance(double[] values) {
        double mean = getMean(values);
        double var = 0;
        int validN = 0;
        for (int i = 0; i < values.length; i++) {
            if (!Double.isNaN(values[i])) {
                double sqDev = (values[i] - mean)*(values[i] - mean);
                var = var + sqDev;
                validN++;
            }
        }
        
        if (validN == 1) {
            return 0.0d;
        } else if (validN < 1) {
            return Double.NaN;
        }        
        /*
        if (Float.isNaN(var)) {
            System.out.print("getVar(): values[] = ");
            for (int i = 0; i < values.length; i++) {
                System.out.print(" " + values[i]);
                //System.out.println();
            }
            System.out.println();
            System.out.println("getVar(): mean = " + mean);
        }
         */
        
        return var/validN;
    }
    
    private double[] getQValues() {
        double[] qValues = new double[101];
        qValues[0] = Double.NEGATIVE_INFINITY;
        for (int i = 1; i < qValues.length; i++) { // ****** NOTE THE INDICES
            qValues[i] = getSAlpha(i);
        }
        
        return qValues;
    }
    
    private double getSNought() throws AlgorithmException {
        double sNot = 0;
        
        /*
        double[] qValues = new double[101];
        qValues[0] = Double.NEGATIVE_INFINITY;
        for (int i = 1; i < qValues.length; i++) { // ****** NOTE THE INDICES
            //System.out.println("in getSNought(): populating qValues[" + i + "]");
            qValues[i] = getSAlpha(i);
            //System.out.println("qValues[" + i + "] = " + qValues[i]);
        }
         */
 //**** UP TO HERE 12/19/02 
        
        /*
        for (int i = 0; i < qValues.length; i++) {
            System.out.println("inside getSNought(): qValues[" + i + "] = " + qValues[i]);
        }
         */        
        double[] qValues = globalAllQValues;
        //double[] alphaArray = new double[21];
        double[] alphaArray = new double[101];
        double currentAlpha = 0;
        for (int i = 0; i < alphaArray.length; i++) {
            alphaArray[i] = currentAlpha;
            //currentAlpha = currentAlpha + 5;
            currentAlpha = currentAlpha + 1;
            if (currentAlpha > 100) {
                currentAlpha = 100;
            }
        }
        
        /*
        for (int i = 0; i < alphaArray.length; i++) {
            System.out.println("inside getSNought(): alphaArray[" + i + "] = " + alphaArray[i]);
        }
         */
        
        double[] cvAlphaArray = new double[alphaArray.length];
        
	AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, alphaArray.length);
	fireValueChanged(event);
	event.setId(AlgorithmEvent.PROGRESS_VALUE);        
        
        for (int i = 0; i < alphaArray.length; i++) {
            //System.out.println("inside getSNought(): cvAlphaArray[" +i + "] ");
            if (stop) {
                throw new AbortException();
            }  
            event.setIntValue(i);
            event.setDescription("Calculating S0: Current alpha = " + alphaArray[i]);
            fireValueChanged(event);            
            cvAlphaArray[i] = getCvAlpha(alphaArray[i], qValues);
            //System.out.println("inside getSNought(): cvAlphaArray[" +i + "] = " + cvAlphaArray[i]);
        }
        /*
        for (int i = 0; i < cvAlphaArray.length; i++) {
            System.out.println("cvAlphaArray[" + i + "] = " + cvAlphaArray[i]);
        }
         */
        
        
        QSort sortCvAlphaArray = new QSort(cvAlphaArray);
        double[] sortedCvAlphaArray = sortCvAlphaArray.getSortedDouble();
        int[] sortedCvAlphaArrayIndices = sortCvAlphaArray.getOrigIndx();
        /*
        for (int i = 0; i < sortedCvAlphaArray.length; i++) {
            System.out.println("sortedCvAlphaArray[" + i + "] = " + sortedCvAlphaArray[i]);
        }   
        
        for (int i = 0; i < sortedCvAlphaArrayIndices.length; i++) {
            System.out.println("sortedCvAlphaArrayIndices[" + i + "] = " + sortedCvAlphaArrayIndices[i]);
        }        
        */
        int minAlphaIndex = sortedCvAlphaArrayIndices[0];

        double argminAlpha = alphaArray[minAlphaIndex];
        /*
        System.out.println("minAlphaIndex = " + minAlphaIndex + ", argminAlpha = " + argminAlpha);     
        
        for (int i = 0; i < alphaArray.length; i++) {
            System.out.println("sAlpha(" + alphaArray[i] + ") = " + getSAlpha(alphaArray[i]));
        }
         */
        
        sNot = getSAlpha(argminAlpha);
        //IMPLEMENT HERE: TO GET ARGMIN OF CVALPHA
        //System.out.println("sNought = " + sNot);
        s0Percentile = argminAlpha;
        return sNot;
    }
    

    private double getCvAlpha(double alpha, double[] qValues) {
        //Vector vjValues = new Vector();
        
        /*
        for (int j = 1; j < 101; j++) {
        //for (int j = 1; j < numExps + 1; j++) {
            double vJ = getMAD(j, alpha); //MAD = median absolute deviation
            vjValues.add(new Double(vJ));
        }
         */
        
        //double[] vjValuesArray = new double[vjValues.size()];
        double[] vjValuesArray = new double[100];
        for (int i = 0; i < vjValuesArray.length; i++) {
            //vjValuesArray[i] = ((Double)vjValues.get(i)).doubleValue();
            vjValuesArray[i] = getMAD(i + 1, alpha); //MAD = median absolute deviation
        }
        
        
        double var = getVar(vjValuesArray);
        double mean = getMean(vjValuesArray);
        /*
        for (int i = 0; i < vjValuesArray.length; i++) {
            System.out.println("getCvAlphaA(): alpha = " + alpha + ", vjValuesArray[" + i + "] = " + vjValuesArray[i]);
        }
        System.out.println("getcvAlpha(): alpha = " + alpha + ", Variance (vjValuesArray) = " + var/vjValuesArray.length + ", mean(vjValuesArray) = " + mean);
         */
        
        double n = 0d;
        for (int i = 0; i < vjValuesArray.length; i++) {
            if (!Double.isNaN(vjValuesArray[i])) {
                n = n + 1;
            }
        }
        
        if (n == 1d) {
            return 0d;
        } else if (n == 0d) {
            return Double.NaN;
        }
        
        double stdDev = (double)Math.sqrt(var/(n - 1));
        
        //System.out.println("getcvAlpha(): alpha = " + alpha + ": " + stdDev/mean);
        
        return (stdDev / mean);
        
    }
    
    private double getMAD(int j, double alpha) { //MAD  = median absolute deviation
        //double[] sValues = getAllSValues();
        double[] sValues = globalAllSValues;
        //System.out.println("getMAD(): sValues.length = " + sValues.length);
        
        /*
        for (int i = 0; i < sValues.length; i++) {
            System.out.println("getMAD(): sValues[" + i + "] = " + sValues[i]);
        }
         */
        
        //double[] qValues = getQValues();
        double[] qValues = globalAllQValues;
        /*
        for (int i = 0; i < qValues.length; i++) {
            System.out.println("getMAD(): qValues[" + i + "] = " + qValues[i]);
        }   
         */     
        //System.out.println("getMAD(): qValues.length = " + qValues.length);
        Vector validSValuesAndGenes = getValidSValuesAndGenes(sValues, qValues, j);
        Vector validSValues = (Vector)validSValuesAndGenes.get(0);
        Vector validGenes = (Vector)validSValuesAndGenes.get(1);
        //System.out.println("getMAD(" + j + ", " + alpha + "): validSValues.size() = " + validSValues.size() + ", validGenes.size() = " + validGenes.size());
                
        double[] dValues = new double[validSValues.size()];
        //System.out.println("getMAD(): dValues.length = " + dValues.length);
        
        for (int i = 0; i < dValues.length; i++) {
            int currentGene = ((Integer)validGenes.get(i)).intValue();
            //float currentSValue = ((Float)validSValues.get(i)).floatValue();
            //float currentSAlpha = getSAlpha(alpha);
            double currentD = getDAlpha(currentGene, alpha);
            dValues[i] = currentD;
        }
        /*
        for (int i = 0; i < dValues.length; i++) {
            System.out.println("getMAD(): dValues[" + i + "] = " + dValues[i]);
        }
         */
        
        double medianD = getMedian(dValues);
        //double medianD = getMedian(dValues)/0.64d;
        //System.out.println("getMAD(): medianD = " + medianD);
        
        double[] absDevValues = new double[dValues.length];
        
        for (int i = 0; i < dValues.length; i++) {
            absDevValues[i] = Math.abs(dValues[i] - medianD);
        }
        
        //double medianAbsDev = getMedian(absDevValues)/0.64d;
        double medianAbsDev = getMedian(absDevValues)/0.6745d; //SAM manual says 0.64, but apparently it should be 0.6745 to normalize the MAD; from SAM newsgroup and James MacDonald, U. Mich.
        //System.out.println("getMAD(): medianAbsDev = " + medianAbsDev);
        //double medianAbsDev = getMedian(absDevValues);
        return medianAbsDev;
        
        //COMPLETE THIS METHOD
    }
    
    private double getMedian(double[] array) {
        QSort sortArray = new QSort(array);
        double median = 0;
        double[] sortedArray = sortArray.getSortedDouble();
        //System.out.println("getMedian(): sortedArray.length = " + sortedArray.length);
        if ((sortedArray.length)%2 == 0) {
            double mid2 = (double)(sortedArray.length/2);
            //System.out.println("mid2 = " + mid2);
            int midIndex2 = (int)Math.round(mid2);
            //System.out.println("midIndex2 = " + midIndex2);
            int midIndex1 = midIndex2 - 1;
            //System.out.println("midIndex1 = " + midIndex1);
            median = (double)((sortedArray[midIndex2] + sortedArray[midIndex1])/2.0d);
            
        } else {
            double mid = (double)(sortedArray.length/2 - 0.5);
            int midIndex = (int)Math.round(mid);
            median = sortedArray[midIndex];
        }
        
        return median;
    }
    
    
   private Vector getValidSValuesAndGenes(double[] sValues, double[] qValues, int j) {
       Vector validSValuesAndGenes = new Vector();
       Vector validSValues = new Vector();
       Vector validGenes = new Vector();
       /*
       for (int i = 0; i < sValues.length; i++) {
           System.out.println("sValues[" + i + "] = " + sValues[i]);
       }
       System.out.println();

       for (int i = 0; i < qValues.length; i++) {
           System.out.println("qValues[" + i + "] = " + qValues[i]);
       }
        */
         
       //System.out.println("j = " +j);
       
       if (j == 100) {
           for (int i = 0; i < sValues.length; i++) {
               if (sValues[i] >= qValues[j]) {
                   validSValues.add(new Double(sValues[i]));
                   validGenes.add(new Integer(i));
               } 
           }           
           /*
           for (int i = 0; i < sValues.length; i++) {
               validSValues.add(new Double(sValues[i]));
               validGenes.add(new Integer(i));
           }
            */
       } else {
           //Vector validSValuesVector = new Vector();
           for (int i = 0; i < sValues.length; i++) {
               if ( (sValues[i] >= qValues[j])&&(sValues[i] < qValues[j+1]) ) {
                   validSValues.add(new Double(sValues[i]));
                   validGenes.add(new Integer(i));
               } else if ((sValues[i] == qValues[j])&&(sValues[i] == qValues[j+1])) {
                   validSValues.add(new Double(sValues[i]));
                   validGenes.add(new Integer(i));                   
               }
           }
           
       }
       /*
       for (int i = 0; i < validSValues.size(); i++) {
           int currentGene = ((Integer)validGenes.get(i)).intValue();
           double currentSValue = ((Double)validSValues.get(i)).doubleValue();
           if (j == 100) {
            System.out.println("getValidSValuesAndGenes(): percentile (j) = " + j + "; qValues[" + j + "] = " + qValues[j] +  "; validGene = " + currentGene + ", validSValue = " + currentSValue);   
           } else {
            System.out.println("getValidSValuesAndGenes(): percentile (j) = " + j + "; qValues[" + j + "] = " + qValues[j] + ", qValues[" + (j+1) + "] = " + qValues[j+1] + "; validGene = " + currentGene + ", validSValue = " + currentSValue);
           }
       }
        */
       
       validSValuesAndGenes.add(validSValues);
       validSValuesAndGenes.add(validGenes);
       
       return validSValuesAndGenes;
   }
    
   
   
   private double getD(int gene, FloatMatrix matrix) {
       return (getR(gene, matrix)/(getS(gene, matrix) + sNought));
   }
   
    
    private double getDAlpha(int gene, double alpha) {
        return ( getR(gene, imputedMatrix)/(getS(gene, imputedMatrix) + getSAlpha(alpha)) );
    }
    
    private double getSAlpha(double percentile) {
        double sAlpha = 0;
        /*
        double[] sValues = new double[numGenes];
        for (int i = 0; i < numGenes; i++) {
            sValues[i] = getS(i, imputedMatrix);
        }
         */
        
        //QSort sortSValues = new QSort(sValues);
       // double[] sortedSValues = sortSValues.getSortedDouble();
        double[] sortedSValues = globalSortedAllSValues;
        //int percentileIndex = (int)Math.round((sortedSValues.length - 1)*percentile/100);
        int percentileIndex = (int)(Math.floor(sortedSValues.length*percentile/100)) - 1;
        if (percentileIndex < 0) {
            percentileIndex = 0;
        } else if (percentileIndex  >= sortedSValues.length) {
            percentileIndex = sortedSValues.length - 1;
        }
        //System.out.println("getSAlpha(): percentile = " + percentile + ", percentileIndex = " + percentileIndex);
        sAlpha = sortedSValues[percentileIndex];
        return sAlpha;
    }
    
    private double[] getAllSValues() {
        double[] sValues = new double[numGenes];
        for (int i = 0; i < numGenes; i++) {
            sValues[i] = getS(i, imputedMatrix);
        
        }
        
        return sValues;
    }
    
    private double getS(int gene, FloatMatrix matrix) {
        double s  = 0;
        if (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) {
            //System.out.println("getS(): this block entered");
            return sTwoClassUnpaired(gene, matrix);
        } else if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
            return sTwoClassPaired(gene, matrix);
        } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
            return sMultiClass(gene, matrix);
        } else if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
            return sCensoredSurvival(gene, matrix);
        } else if (studyDesign == SAMInitDialog.ONE_CLASS) {
            return sOneClass(gene, matrix);
        }
        
        return s;
    }
    
    private double getR(int gene, FloatMatrix matrix) {
        double r = 0;
        
        if (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) {
            return rTwoClassUnpaired(gene, matrix);
        } else if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
            return rTwoClassPaired(gene, matrix);
        } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
            return rMultiClass(gene, matrix);
        } else if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
            return rCensoredSurvival(gene, matrix);
        } else if (studyDesign == SAMInitDialog.ONE_CLASS) {
            return rOneClass(gene, matrix);
        }
        
        return r;
    }
    
    private void printMatrix(FloatMatrix mat, String fileName) throws Exception {
        File outFile = new File(fileName);
        PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
        for (int i = 0; i < mat.getRowDimension(); i++) {
            for (int j = 0; j < mat.getColumnDimension(); j++) {
                out.print(mat.A[i][j]);
                if (j < mat.getColumnDimension() - 1) {
                    out.print("\t");
                }
            }
            out.print("\n");
        }
        out.flush();
        out.close();
    }
    
    private FloatMatrix imputeRowAverageMatrix(FloatMatrix inputMatrix) {
        int numRows = inputMatrix.getRowDimension();
        int numCols = inputMatrix.getColumnDimension();
        FloatMatrix resultMatrix = new FloatMatrix(numRows, numCols);
        for (int i = 0; i < numRows; i++) {
            float[] currentRow = new float[numCols];
            float[] currentOrigRow = new float[numCols];
            for (int j = 0; j < numCols; j++) {
                currentRow[j] = inputMatrix.A[i][j];
                currentOrigRow[j] = inputMatrix.A[i][j];
            }
            for (int k = 0; k < numCols; k++) {
                if (Float.isNaN(inputMatrix.A[i][k])) {
                    currentRow[k] = getMean(currentOrigRow);
                }
            }
            
            for (int l = 0; l < numCols; l++) {
                resultMatrix.A[i][l] = currentRow[l];
            }
        }
        
        return resultMatrix;
    }
    
    private float getMean(float[] row) {
        float mean = 0.0f;
        int validN = 0;
        
        for (int i = 0; i < row.length; i++) {
            if (!Float.isNaN(row[i])) {
                mean = mean + row[i];
                validN++;
            }
        }
        
        if (validN == 0) {
            validN = 1; // if the whole row is NaN, it will be set to zero;
        }
        
        mean = (float)(mean / validN);
        
        return mean;
    }
    
    private double getMean(double[] row) {
        double mean = 0.0f;
        int validN = 0;
        
        for (int i = 0; i < row.length; i++) {
            if (!Double.isNaN(row[i])) {
                mean = mean + row[i];
                validN++;
            }
        }
        
        if (validN == 0) {
            validN = 1; // if the whole row is NaN, it will be set to zero;
        }
        
        double finalMean = (double)(mean / validN);
        
        return finalMean;
    }    
    

    private FloatMatrix imputeKNearestMatrix(FloatMatrix inputMatrix, int k) { 
        int numRows = inputMatrix.getRowDimension();
        int numCols = inputMatrix.getColumnDimension();
        FloatMatrix resultMatrix = new FloatMatrix(numRows, numCols);
        
        for (int i = 0; i < numRows; i++) {
            if (isMissingValues(inputMatrix, i)) {
                //System.out.println("gene " + i + " is missing values");
                Vector nonMissingExpts = new Vector();
                for (int j = 0; j < numCols; j++) {
                    if (!Float.isNaN(inputMatrix.A[i][j])) {
                        nonMissingExpts.add(new Integer(j));
                    }
                }
                Vector geneSubset = getValidGenes(i, inputMatrix, nonMissingExpts); //getValidGenes() returns a Vector of genes that have valid values for all the non-missing expts
                
                //System.out.println(" Valid geneSubset.size() = " + geneSubset.size());
                
                /*
                for (int j = 0; j < geneSubset.size(); j++) {
                    System.out.println(((Integer)geneSubset.get(j)).intValue());
                }
                 */
                
                Vector kNearestGenes = getKNearestGenes(i, k, inputMatrix, geneSubset, nonMissingExpts);
                
                /*
                System.out.println("k nearest genes of gene " + i + " : ");
                
                for (int j = 0; j < kNearestGenes.size(); j++) {
                    System.out.println("" + ((Integer)kNearestGenes.get(j)).intValue());
                }
                 */
                
                //TESTED UPTO HERE -- 12/18/2002***********
                //
                /*
                System.out.print("Gene " + i + " :\t"); 
                for (int j = 0; j < numCols; j++) {
                    System.out.print("" +inputMatrix.A[i][j]);
                    System.out.print("\t");
                }
                System.out.println();
                System.out.println("Matrix of k Nearest Genes");
                printSubMatrix(kNearestGenes, inputMatrix);                
                */    //
                for (int j = 0; j < numCols; j++) {
                    if (!Float.isNaN(inputMatrix.A[i][j])) {
                        resultMatrix.A[i][j] = inputMatrix.A[i][j]; 
                    } else {
                        
                        //System.out.println("just before entering getExptMean(): kNearestGenes.size() = " + kNearestGenes.size());

                        //resultMatrix.A[i][j] = getExptMean(j, kNearestGenes, inputMatrix);
                        resultMatrix.A[i][j] = getExptWeightedMean(i, j, kNearestGenes, inputMatrix);
                    }
                }
                //DONE UPTO HERE
            }
            
            else {
                for (int j = 0; j < numCols; j++) {
                    resultMatrix.A[i][j] = inputMatrix.A[i][j];
                }
            }
        }
        
        return imputeRowAverageMatrix(resultMatrix);
    }
    
    private void printSubMatrix(Vector geneSet, FloatMatrix mat) {
        for (int i = 0; i < geneSet.size(); i++) {
            int currentGene = ((Integer)geneSet.get(i)).intValue();
            System.out.print("Gene " + ((Integer)geneSet.get(i)).intValue() + " :\t");
            for (int j = 0; j < mat.getColumnDimension(); j++) {
                System.out.print("" + mat.A[currentGene][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    private float getExptMean(int expt, Vector geneVector, FloatMatrix mat) {
        float mean = 0;
        int validN = 0;
        for (int i = 0; i < geneVector.size(); i++) {
            int currentGene = ((Integer)geneVector.get(i)).intValue();
            if (!Float.isNaN(mat.A[currentGene][expt])) {
                mean = mean + mat.A[currentGene][expt];
                validN++;
            }
        }
        
        //System.out.println("mean = " + mean);
        if (validN > 0) {
            mean = mean / validN;
        } else {
            mean = Float.NaN;
        }
        
        //System.out.println(" Inside getExptMean(): mean = " + mean);
        
        return mean;
    }
    
    private float getExptWeightedMean(int gene, int expt, Vector geneVector, FloatMatrix mat) { 
        float weightedMean = 0.0f;
        int validN = 0;
        float numerator = 0.0f;
        float recipNeighborDistances[] = new float[geneVector.size()];
        for (int i = 0; i < recipNeighborDistances.length; i++) {
            int currentGene = ((Integer)geneVector.get(i)).intValue();
            if (!Float.isNaN(mat.A[currentGene][expt])) {
                float distance = ExperimentUtil.geneEuclidianDistance(mat, null, gene, currentGene, factor);
                if (distance == 0.0f) {
                    distance = Float.MIN_VALUE;
                }
                recipNeighborDistances[i] = (float)(1.0f/distance);
                numerator = numerator + (float)(recipNeighborDistances[i]*mat.A[currentGene][expt]);
                validN++;
            } else {
                recipNeighborDistances[i] = 0.0f;
            }

        }
        
        float denominator = 0.0f;
        for (int i = 0; i < recipNeighborDistances.length; i++) {
            denominator = denominator + recipNeighborDistances[i];
        }
        
        weightedMean = (float)(numerator/(float)denominator);
        return weightedMean;
    }
    
    

    Vector getKNearestGenes(int gene, int k, FloatMatrix mat, Vector geneSubset, Vector nonMissingExpts) {
        Vector allValidGenes = new Vector();
        Vector nearestGenes = new Vector();
        Vector geneDistances = new Vector();
        for (int i = 0; i < geneSubset.size(); i++) {
            int currentGene = ((Integer)geneSubset.get(i)).intValue();
            if (gene != currentGene) {
                float currentDistance = ExperimentUtil.geneEuclidianDistance(mat, null, gene, currentGene, factor);
                //System.out.println("Current distance = " + currentDistance);
                geneDistances.add(new Float(currentDistance));
                allValidGenes.add(new Integer(currentGene));
            }
        }
        
        float[] geneDistancesArray = new float[geneDistances.size()];
        for (int i = 0; i < geneDistances.size(); i++) {
            float currentDist = ((Float)geneDistances.get(i)).floatValue();
            geneDistancesArray[i] = currentDist;
        }
        
        QSort sortGeneDistances = new QSort(geneDistancesArray);
        float[] sortedDistances = sortGeneDistances.getSorted();
        int[] sortedDistanceIndices = sortGeneDistances.getOrigIndx();
        
        for (int i = 0; i < k; i++) {
            int currentGeneIndex = sortedDistanceIndices[i];
            int currentNearestGene = ((Integer)allValidGenes.get(currentGeneIndex)).intValue();
            nearestGenes.add(new Integer(currentNearestGene));
        }
        
        return nearestGenes;
    }
    
    private boolean isMissingValues(FloatMatrix mat, int row) {//returns true if the row of the matrix has any NaN values
                
        for (int i = 0; i < mat.getColumnDimension(); i++) {
            if (Float.isNaN(mat.A[row][i])) {
                return true;
            }
        }
        
        return false;
    }
    
    
    private Vector getValidGenes(int gene, FloatMatrix mat, Vector validExpts) { //returns the indices of those genes in "mat" that have valid values for all the validExpts
        Vector validGenes = new Vector();
        
        for (int i = 0; i < mat.getRowDimension(); i++) {
            if (hasAllExpts(i, mat, validExpts)) {//returns true if gene i in "mat" has valid values for all the validExpts
                validGenes.add(new Integer(i));
            }
        }
        
        if (validGenes.size() < numNeighbors) { // if the number of valid genes is < k, other genes will be added to validGenes in increasing order of Euclidean distance until validGenes.size() = k
            int additionalGenesNeeded = numNeighbors - validGenes.size();
            Vector additionalGenes = getAdditionalGenes(gene, additionalGenesNeeded, validGenes, mat);
            for (int i = 0; i < additionalGenes.size(); i++) {
                validGenes.add(additionalGenes.get(i));
            }
        }
      
        return validGenes;
    }
    
    private Vector getAdditionalGenes(int currentGene, int numGenesNeeded, Vector alreadyPresentGenes, FloatMatrix mat) {
        Vector additionalGenes = new Vector();
        Vector allGenes = new Vector();
        Vector geneDistances = new Vector();
        
        for (int i = 0; i < mat.getRowDimension(); i++) {
            if (i != currentGene) {
                float currentDistance = ExperimentUtil.geneEuclidianDistance(mat, null, i, currentGene, factor);
                geneDistances.add(new Float(currentDistance));
                allGenes.add(new Integer(i));                
            }
        }
        
        float[] geneDistancesArray = new float[geneDistances.size()];
        for (int i = 0; i < geneDistances.size(); i++) {
            float currentDist = ((Float)geneDistances.get(i)).floatValue();
            geneDistancesArray[i] = currentDist;
        }     
        
        QSort sortGeneDistances = new QSort(geneDistancesArray);
        float[] sortedDistances = sortGeneDistances.getSorted();
        int[] sortedDistanceIndices = sortGeneDistances.getOrigIndx();
        
        int counter = 0;
        
        for (int i = 0; i < sortedDistanceIndices.length; i++) {
            int currentIndex = sortedDistanceIndices[i];
            int currentNearestGene = ((Integer)allGenes.get(currentIndex)).intValue(); 
            if (belongsIn(alreadyPresentGenes, currentNearestGene)) {
                continue;
            } else {
                additionalGenes.add(new Integer(currentNearestGene));
                counter++;
                if (counter >= numGenesNeeded) {
                    break;
                }
            }
        }
        
        return additionalGenes;
    }
    
    private boolean belongsIn(Vector geneVector, int gene) {
        for (int i = 0; i < geneVector.size(); i++) {
            int currentGene = ((Integer)geneVector.get(i)).intValue();
            if (gene == currentGene) {
                return true;
            }
        }
        
        return false;
    }
    
    
    private boolean hasAllExpts(int gene, FloatMatrix mat, Vector validExpts) {//returns true if "gene" in "mat" has valid values for all the validExpts

        for (int i = 0; i < validExpts.size(); i++) {
            int expIndex = ((Integer)validExpts.get(i)).intValue();
            if (Float.isNaN(mat.A[gene][expIndex])) {
                return false;
            }
        }
        
        return true;
    }
    
}

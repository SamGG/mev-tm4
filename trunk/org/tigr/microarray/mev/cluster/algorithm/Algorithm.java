/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Algorithm.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm;

/**
 * Interface of a class which is used to proceed a calculation.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface Algorithm {
    
    // distance functions constants
    public static final int DEFAULT = 0;
    public static final int PEARSON = 1;
    public static final int COSINE = 2;
    public static final int COVARIANCE = 3;
    public static final int EUCLIDEAN = 4;
    public static final int DOTPRODUCT = 5;
    public static final int PEARSONUNCENTERED = 6;
    public static final int PEARSONSQARED = 7;
    public static final int MANHATTAN = 8;
    public static final int SPEARMANRANK = 9;
    public static final int KENDALLSTAU = 10;
    public static final int MUTUALINFORMATION = 11;
    
    /**
     * This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException;
    
    /**
     * This method should interrupt the calculation.
     */
    public void abort();
    
    /**
     * Adds an <code>AlgorithmListener</code> to this <code>Algorithm</code>.
     *
     * @param l the <code>AlgorithmListener</code> to be added.
     */
    public void addAlgorithmListener(AlgorithmListener l);
    
    /**
     * Removes an <code>AlgorithmListener</code> from this <code>Algorithm</code>.
     *
     * @param l the <code>AlgorithmListener</code> to be removed.
     */
    public void removeAlgorithmListener(AlgorithmListener l);
}

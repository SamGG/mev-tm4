/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: QSort.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-17 16:57:18 $
 * $Author: nbhagaba $
 * $State: Exp $
 */
package org.tigr.util;
import java.util.Vector;

//import java.io.*;

//SORTS IN ASCENDING ORDER

public class QSort {
    
    private int[] origIndx;
    private float[] sorted;
    private double[] sortedDouble;
    private int[] NaNIndices;
    public static final int ASCENDING = 1;
    public static final int DESCENDING = 2;
    private boolean ascending;    
    
    public QSort(float[] origA){
        float[] copyA = new float[origA.length];
        this.ascending = true;
        Vector NaNIndicesVector = new Vector();
        for (int i = 0; i < copyA.length; i++) {
            copyA[i] = origA[i];
            if (Float.isNaN(origA[i])) {
                NaNIndicesVector.add(new Integer(i));
            }
        }
        
        NaNIndices = new int[NaNIndicesVector.size()];
        
        for (int i = 0; i < NaNIndices.length; i++) {
            NaNIndices[i] = ((Integer)(NaNIndicesVector.get(i))).intValue();
        }
	sort(copyA);
    }
    
    public QSort(double[] origA){
        double[] copyA = new double[origA.length];
        this.ascending = true;
        Vector NaNIndicesVector = new Vector();        
        for (int i = 0; i < copyA.length; i++) {
            copyA[i] = origA[i];
            if (Double.isNaN(origA[i])) {
                NaNIndicesVector.add(new Integer(i));
            }            
        }  
        
        NaNIndices = new int[NaNIndicesVector.size()];
        
        for (int i = 0; i < NaNIndices.length; i++) {
            NaNIndices[i] = ((Integer)(NaNIndicesVector.get(i))).intValue();
        }        
	sort(copyA);
    }   
    
    public QSort(float[] origA, int ascOrDesc) {
        this(origA);
        if (ascOrDesc == QSort.ASCENDING) {
            this.ascending = true;
        } else if (ascOrDesc == QSort.DESCENDING) {
            this.ascending = false;
        }
    }
    
    public QSort(double[] origA, int ascOrDesc) {
       this(origA);
        if (ascOrDesc == QSort.ASCENDING) {
            this.ascending = true;
        } else if (ascOrDesc == QSort.DESCENDING) {
            this.ascending = false;
        }
    }    
    
    public void sort(float a[]) {
	
	origIndx = new int[a.length];
	for (int i = 0; i <= origIndx.length - 1; i++){
	    origIndx[i] = i;
	}
	quickSort(a, 0, a.length - 1);
    }
    
    public void sort(double a[]) {
	
	origIndx = new int[a.length];
	for (int i = 0; i <= origIndx.length - 1; i++){
	    origIndx[i] = i;
	}
	quickSort(a, 0, a.length - 1);
    }    
    
    void quickSort(float a[], int lo0, int hi0) {
	int lo = lo0;
	int hi = hi0;
	float mid;
	
	if ( hi0 > lo0) {
	    
	 /* Arbitrarily establishing partition element as the midpoint of
	  * the array.
	  */
	    mid = a[ ( lo0 + hi0 ) / 2 ];
	    
	    // loop through the array until indices cross
	    while( lo <= hi ) {
	    /* find the first element that is greater than or equal to
	     * the partition element starting from the left Index.
	     */
		while( ( lo < hi0 ) && ( a[lo] < mid ))
		    ++lo;
		
	    /* find an element that is smaller than or equal to
	     * the partition element starting from the right Index.
	     */
		while( ( hi > lo0 ) && ( a[hi] > mid ))
		    --hi;
		
		// if the indexes have not crossed, swap
		if( lo <= hi ) {
		    swap(a, lo, hi);
		    ++lo;
		    --hi;
		}
	    }
	    
	 /* If the right index has not reached the left side of array
	  * must now sort the left partition.
	  */
	    if( lo0 < hi )
		quickSort( a, lo0, hi );
	    
	 /* If the left index has not reached the right side of array
	  * must now sort the right partition.
	  */
	    if( lo < hi0 )
		quickSort( a, lo, hi0 );
	    
	}
	sorted = a;
    }
    
    void quickSort(double a[], int lo0, int hi0) {
	int lo = lo0;
	int hi = hi0;
	double mid;
	
	if ( hi0 > lo0) {
	    
	 /* Arbitrarily establishing partition element as the midpoint of
	  * the array.
	  */
	    mid = a[ ( lo0 + hi0 ) / 2 ];
	    
	    // loop through the array until indices cross
	    while( lo <= hi ) {
	    /* find the first element that is greater than or equal to
	     * the partition element starting from the left Index.
	     */
		while( ( lo < hi0 ) && ( a[lo] < mid ))
		    ++lo;
		
	    /* find an element that is smaller than or equal to
	     * the partition element starting from the right Index.
	     */
		while( ( hi > lo0 ) && ( a[hi] > mid ))
		    --hi;
		
		// if the indexes have not crossed, swap
		if( lo <= hi ) {
		    swap(a, lo, hi);
		    ++lo;
		    --hi;
		}
	    }
	    
	 /* If the right index has not reached the left side of array
	  * must now sort the left partition.
	  */
	    if( lo0 < hi )
		quickSort( a, lo0, hi );
	    
	 /* If the left index has not reached the right side of array
	  * must now sort the right partition.
	  */
	    if( lo < hi0 )
		quickSort( a, lo, hi0 );
	    
	}
	sortedDouble = a;
    }    
    
    private void swap(float a[], int i, int j) {
	
	float T;
	T = a[i];
	a[i] = a[j];
	a[j] = T;
	
	int TT = origIndx[i];
	origIndx[i] = origIndx[j];
	origIndx[j] = TT;
    }
    
    private void swap(double a[], int i, int j) {
	
	double T;
	T = a[i];
	a[i] = a[j];
	a[j] = T;
	
	int TT = origIndx[i];
	origIndx[i] = origIndx[j];
	origIndx[j] = TT;
    }    
    
    public float[] getSorted(){
        Vector sortedVector = new Vector();
        Vector NaNSortedIndices = new Vector();
        
        for (int i = 0; i < sorted.length; i++) {
            if (Float.isNaN(sorted[i])) {
                NaNSortedIndices.add(new Integer(i));
            } else {
                sortedVector.add(new Float(sorted[i]));
            }
        }
        
        for (int i = 0; i < NaNSortedIndices.size(); i++) {
            sortedVector.add(0, new Float(Float.NaN));
        }
        for (int i = 0; i < sortedVector.size(); i++) {
            sorted[i] = ((Float)(sortedVector.get(i))).floatValue();
        }
        if (!ascending) {
            float[] revSorted = reverse(sorted);
            return revSorted;
        } else {        
            return sorted;
        }        
	//return sorted;
    }
    
    public double[] getSortedDouble(){
        Vector sortedVector = new Vector();
        Vector NaNSortedIndices = new Vector();
        
        for (int i = 0; i < sortedDouble.length; i++) {
            if (Double.isNaN(sortedDouble[i])) {
                NaNSortedIndices.add(new Integer(i));
            } else {
                sortedVector.add(new Double(sortedDouble[i]));
            }
        }
        
        for (int i = 0; i < NaNSortedIndices.size(); i++) {
            sortedVector.add(0, new Double(Double.NaN));
        }
        for (int i = 0; i < sortedVector.size(); i++) {
            sortedDouble[i] = ((Double)(sortedVector.get(i))).doubleValue();
        } 
        
        if (!ascending) {
            double[] revSortedDouble = reverse(sortedDouble);
            return revSortedDouble;
        } else {        
            return sortedDouble;
        }
    }    
    
    public int[] getOrigIndx(){
        Vector origIndxVector = new Vector();
        for (int i = 0; i < origIndx.length; i++) {
            if (!isNaNIndex(origIndx[i])) {
                origIndxVector.add(new Integer(origIndx[i]));
            }
        }
        
        for (int i = 0; i < NaNIndices.length; i++) {
            origIndxVector.add(0, new Integer(NaNIndices[i]));
        }
        
        for (int i = 0; i < origIndxVector.size(); i++) {
            origIndx[i] = ((Integer)(origIndxVector.get(i))).intValue();
        }
        
        if (!ascending) {
            return reverse(origIndx);
        } else {        
            return origIndx;
        }
    }

    private boolean isNaNIndex(int index) {
        for (int i = 0; i < NaNIndices.length; i++) {
            if (index == NaNIndices[i]) {
                return true;
            }
        }
        
        return false;
    }
    
    private int[] reverse(int[] arr) {
        int[] revArr = new int[arr.length];
        int  revCount = 0;
        int count = arr.length - 1;
        for (int i=0; i < arr.length; i++) {
            revArr[revCount] = arr[count];
            revCount++;
            count--;
        }
        return revArr;
    }
    
    private float[] reverse(float[] arr) {
        float[] revArr = new float[arr.length];
        int  revCount = 0;
        int count = arr.length - 1;
        for (int i=0; i < arr.length; i++) {
            revArr[revCount] = arr[count];
            revCount++;
            count--;
        }
        return revArr;
    }    
    
    private double[] reverse(double[] arr) {
        double[] revArr = new double[arr.length];
        int  revCount = 0;
        int count = arr.length - 1;
        for (int i=0; i < arr.length; i++) {
            revArr[revCount] = arr[count];
            revCount++;
            count--;
        }
        return revArr;
    }    
    
}


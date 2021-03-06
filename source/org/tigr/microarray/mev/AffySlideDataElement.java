/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

/*
 * $RCSfile: AffySlideDataElement.java,v $
 * $Revision: 1.11 $
 * $Date: 2007-12-19 21:39:34 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.annotation.IAnnotation;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.gui.IData;

public class AffySlideDataElement extends ArrayElement implements ISlideDataElement {
	
	protected String UID;
	protected int[] rows;
	protected int[] columns;
	protected float[] currentIntensity, trueIntensity;
	protected String[] extraFields;
	
	// pcahan
	//protected String detection;
	protected char detection;
	
	//wwang add for p-value filter
	protected float pvalue;
	protected int flags;
	protected boolean isNull = false;
	protected boolean isNonZero = true;
	
	
	
	/**
	 * Raktim - Model MeVAnnotation Object
	 * 6/12/07
	 */
	
	MevAnnotation annotation;
	
	/**
	 * Raktim - MeVAnnotation Constructor
	 * Constructs a <code>SlideDataElement</code> with specified meta rows,
	 * meta columns, intensities, descriptions and an Annotation Object.
	 */
	public AffySlideDataElement(String UID, int[] rows, int[] columns, float[] intensities, String[] values, MevAnnotation ann) {
		this.UID = UID;
		this.rows = copyArray(rows);
		this.columns = copyArray(columns);
		this.currentIntensity = copyArray(intensities);
		this.trueIntensity = copyArray(intensities);
		this.extraFields = copyArray(values);
		this.annotation = ann;
	}
	
	/**
	 * Constructs a <code>SlideDataElement</code> with specified meta rows,
	 * meta columns, intensities and descriptions.
	 */
	public AffySlideDataElement(String UID, int[] rows, int[] columns, float[] intensities, String[] values) {
		this.UID = UID;
		this.rows = copyArray(rows);
		this.columns = copyArray(columns);
		this.currentIntensity = copyArray(intensities);
		this.trueIntensity = copyArray(intensities);
		this.extraFields = copyArray(values);
	}
	
	/**
	 * Constructs a <code>SlideDataElement</code> with specified meta rows,
	 * meta columns, intensities and descriptions.
	 */
	public AffySlideDataElement(int[] rows, int[] columns, float[] intensities, String[] values) {
		this.rows = copyArray(rows);
		this.columns = copyArray(columns);
		this.currentIntensity = copyArray(intensities);
		this.trueIntensity = copyArray(intensities);
		this.extraFields = copyArray(values);
	}
	/**
	 * Constructor used by XMLEncoder to restore a previously-saved state.  
	 * 
	 * Read state-saving developer documents before altering.
	 * 
	 * @param rows
	 * @param cols
	 * @param extraFields
	 * @param uid
	 * @param isNull
	 * @param isNonZero
	 * @param detection
	 */
	public AffySlideDataElement(int[] rows, int[] columns, String[] extraFields, String uid, boolean isNull, boolean isNonZero, char detection, float pValue, int flags){
		this.rows = copyArray(rows);
		this.columns = copyArray(columns);
		this.extraFields = copyArray(extraFields);
		this.UID = uid;
		this.isNull = isNull;
		this.isNonZero = isNonZero;
		this.detection = detection;
		this.currentIntensity = new float[2];
		this.trueIntensity = new float[2];
		this.pvalue = pValue;
		this.flags = flags;
	}
	
	/**
	 * Copy constructor.
	 */
	public AffySlideDataElement(ISlideDataElement sde) {
		this.UID = sde.getUID();
		this.rows = sde.getRows();
		this.columns = sde.getColumns();
		this.currentIntensity = copyArray(sde.getCurrentIntensity());
		this.trueIntensity = copyArray(sde.getTrueIntensity());
		this.extraFields = sde.getExtraFields();
		if(sde.getDetection() != null && sde.getDetection().length() != 0)
			this.setDetection(sde.getDetection());
		this.setPvalue(sde.getPvalue());
		this.isNull = sde.getIsNull();
		this.isNonZero = sde.isNonZero();
		if(sde instanceof AffySlideDataElement && ((AffySlideDataElement)sde).annotation != null) {
			this.annotation = (MevAnnotation)sde.getElementAnnotation().clone();
		}
	}
	
	/**
	 * Sets the extra fields (annotation), appends if fields exist
	 */
	public void setExtraFields(String [] values){
		if(values == null) return;
		if(this.extraFields == null)
			this.extraFields = values;
		else{
			String [] newFields = new String[this.extraFields.length+values.length];
			for(int i = 0; i < this.extraFields.length; i++)
				newFields[i] = this.extraFields[i];
			for(int i = 0; i < values.length; i++)
				newFields[i+this.extraFields.length] = values[i];
			this.extraFields = newFields;
		}
	}
	
	/**
	 * Creates clone of a string array.
	 */
	private String[] copyArray(String[] array) {
		if (array == null) {
			return null;
		}
		String[] result = new String[array.length];
		System.arraycopy(array, 0, result, 0, array.length);
		return result;
	}
	
	/**
	 * Creates clone of an int array.
	 */
	private int[] copyArray(int[] array) {
		if (array == null) {
			return null;
		}
		int[] result = new int[array.length];
		System.arraycopy(array, 0, result, 0, array.length);
		return result;
	}
	
	/**
	 * Creates clone of a float array.
	 */
	private float[] copyArray(float[] array) {
		if (array == null) {
			return null;
		}
		float[] result = new float[array.length];
		System.arraycopy(array, 0, result, 0, array.length);
		return result;
	}
	
	/**
	 * Returns an array of spot meta rows.
	 */
	public int[] getRows() {
		return rows;
	}
	
	/**
	 * Returns an array of spot meta column.
	 */
	public int[] getColumns() {
		return columns;
	}
	
	/**
	 * Returns an array of spot descriptions.
	 */
	public String[] getExtraFields() {
		return extraFields;
	}
	
	/**
	 * Returns an array of current intensities.
	 */
	public float[] getCurrentIntensity() {
		return currentIntensity;
	}
	
	/**
	 * Returns an array of true intensities.
	 */
	public float[] getTrueIntensity() {
		return trueIntensity;
	}
	
	/**
	 * Sets value for specified row type.
	 */
	public void setRow(int rowType, int value) {
		switch (rowType) {
		case BASE: rows[0] = value; break;
		case META: rows[1] = value; break;
		case SUB : rows[2] = value; break;
		}
	}
	
	/**
	 * Sets value for specified column type.
	 */
	public void setColumn(int columnType, int value) {
		switch (columnType) {
		case BASE: columns[0] = value; break;
		case META: columns[1] = value; break;
		case SUB:  columns[2] = value; break;
		}
	}
	
	//Replaces getRow, getMetaRow, getSubRow
	public int getRow(int rowType) {
		int targetRow = -1;
		switch (rowType) {
		case BASE: targetRow = rows[0]; break;
		case META: targetRow = rows[1]; break;
		case SUB: targetRow  = rows[2]; break;
		}
		return targetRow;
	}
	
	/**
	 * Returns index of specified column type.
	 */
	public int getColumn(int columnType) {
		int targetColumn = -1;
		switch (columnType) {
		case BASE: targetColumn = columns[0]; break;
		case META: targetColumn = columns[1]; break;
		case SUB: targetColumn  = columns[2]; break;
		}
		return targetColumn;
	}
	
	/**
	 * Returns a spot location.
	 */
	public int getLocation(int positionType, int features) {
		int location = -1;
		switch (positionType) {
		case BASE: location = columns[0] + (rows[0] - 1) * features; break;
		case META: location = columns[1] + (rows[1] - 1) * features; break;
		case SUB : location = columns[2] + (rows[2] - 1) * features; break;
		}
		return location;
	}
	
	/**
	 * Returns intensity value by its specified type.
	 */
	public float getIntensity(int intensityType) {
		float targetIntensity = -1;
		switch (intensityType) {
		case CY3:
			targetIntensity = currentIntensity[0];
			break;
		case CY5:
			targetIntensity = currentIntensity[1];
			break;
		}
		return (float)targetIntensity;
	}
	
	/**
	 * Sets an intensity value.
	 */
	public void setIntensity(int intensityType, float value) {
		switch (intensityType) {
		case CY3: currentIntensity[0] = value; break;
		case CY5: currentIntensity[1] = value; break;
		}
	}
	
	public void setDetection(String value){
		detection = value.charAt(0);
	}
	public void setPvalue(float value){
		pvalue=value;
	}
	public void setGenePixFlags(int value){
		flags=value;
	}
	/**
	 * Sets true intensity value.
	 */
	public void setTrueIntensity(int intensityType, float value) {
		switch (intensityType) {
		case CY3: trueIntensity[0] = value; break;
		case CY5: trueIntensity[1] = value; break;
		}
	}
	
	
	//Replaces getTrueCy3, getTrueCy5
	public float getTrueIntensity(int intensityType) {
		float targetIntensity = -1;
		switch (intensityType) {
		case CY3: targetIntensity = trueIntensity[0]; break;
		case CY5: targetIntensity = trueIntensity[1]; break;
		}
		return targetIntensity;
	}
	
	/**
	 * Returns a ratio value for specified intensities.
	 */
	public float getRatio(int intensityIndex1, int intensityIndex2, int logState) {
		return getRatio(intensityIndex1, intensityIndex2, logState, isNonZero);
	}
	
	public float getRatio(int intensityIndex1, int intensityIndex2, int logState, boolean nonZero) {
		float ratio;
		
		// - tigr_original -
		
		if (nonZero) {
			if ((getIntensity(intensityIndex1) == 0) && (getIntensity(intensityIndex2) == 0)) {
				return Float.NaN;
			} else if (getIntensity(intensityIndex1) == 0) {
				ratio = 1f / (float)getIntensity(intensityIndex2);
			} else if (getIntensity(intensityIndex2) == 0) {
				ratio = (float)getIntensity(intensityIndex1)/1f;
			} else {
				ratio = (float)getIntensity(intensityIndex1)/(float)getIntensity(intensityIndex2);
			}
		} else {
			if (getIntensity(intensityIndex1) == 0) return Float.NaN;
			if (getIntensity(intensityIndex2) == 0) return Float.NaN;
			ratio = (float)getIntensity(intensityIndex1)/(float)getIntensity(intensityIndex2);
		}
		if (logState == IData.LOG)
			//LOG
			//ratio = (float) Xcon.log2(ratio);
			ratio = (float)(Math.log(ratio)/Math.log(2.0));
		return ratio;
		
		//- end tigr_original -
		
		
		//        return this.getIntensity(0);
		
		/* pcahan
		 pick a channel to exclude and simply return the other channel intensity
		 think about normalization effects - currentintensity - just get trueintensity
		 */
		//if (getIntensity(intensityIndex1) == 0) && (getIntensity(intensityIndex2) == 0)
		
	}
	
	/**
	 * Returns a true ratio value for specified intensities.
	 */
	public double getTrueRatio(int intensityIndex1, int intensityIndex2, int logState) {
		return getTrueRatio(intensityIndex1, intensityIndex2, logState, isNonZero);
	}
	
	public double getTrueRatio(int intensityIndex1, int intensityIndex2, int logState, boolean nonZero) {
		//return (double) this.getIntensity(0);
		
		
		double ratio;
		
		
		if (nonZero) {
			if ((getTrueIntensity(intensityIndex1) == 0) && (getTrueIntensity(intensityIndex2) == 0)) {
				return Float.NaN;
			} else if (getTrueIntensity(intensityIndex1) == 0) {
				ratio = (double) 1.0 / (double) getTrueIntensity(intensityIndex2);
			} else if (getTrueIntensity(intensityIndex2) == 0) {
				ratio = (double) getTrueIntensity(intensityIndex1) / (double) 1.0;
			} else {
				ratio = (double) getTrueIntensity(intensityIndex1) / (double) getTrueIntensity(intensityIndex2);
			}
		} else {
			if (getTrueIntensity(intensityIndex1) == 0) return Float.NaN;
			if (getTrueIntensity(intensityIndex2) == 0) return Float.NaN;
			ratio = (double) getTrueIntensity(intensityIndex1) / (double) getTrueIntensity(intensityIndex2);
		}
		
		if (logState == IData.LOG)
			//LOG
			//ratio = (float) Xcon.log2(ratio);
			ratio = (float)(Math.log(ratio)/Math.log(2.0));
		return ratio;
	}
	
	public float getIntensityMean(int intensityType) {
		return(float) 0.0;
		
		
	}
	
	/**
	 * Sets the non-zero flag.
	 */
	public void setNonZero(boolean state) {this.isNonZero = state;}
	
	/**
	 * Returns the non-zero flag.
	 */
	public boolean isNonZero() {return this.isNonZero;}
	
	/**
	 * Sets the isNull flag.
	 */
	public void setIsNull(boolean state) {this.isNull = state;}
	
	/**
	 * Returns the isNull flag.
	 */
	public boolean getIsNull() {return this.isNull;}
	
	/**
	 * @return true, if spot contains no zero intensities.
	 */
	public boolean hasNoZeros() {
		if ((getTrueIntensity(CY3) != 0) && (getTrueIntensity(CY5) != 0)) return true;
		else return false;
	}
	
	public String getDetection(){
		return String.valueOf(detection);
	}
	public float getPvalue(){
		return pvalue;
	}
	
	public int getGenePixFlags(){
		return flags;
	}
	public String getAttributeString(int number) {
		String retVal = "";
		int coordinatePairs = TMEV.getCoordinatePairCount() * 2; //Did you see the (* 2)?
		int intensities = TMEV.getIntensityCount();
		int extraFields = this.extraFields.length;
		
		if (number < coordinatePairs) {
			if (number % 2 == 0)
				retVal = String.valueOf(rows[number/2]);
			else
				retVal = String.valueOf(columns[(number+1)/2]);
		} else if (number < coordinatePairs + intensities) {
			retVal = String.valueOf(currentIntensity[number - (coordinatePairs)]);
		} else if (number < coordinatePairs + intensities + extraFields) {
			retVal = getFieldAt(number - (coordinatePairs + intensities));
		} else retVal = "";
		return retVal;
	}
	
	
	/**
	 * Returns a description by specified index. Or empty string if feild index
	 * does not exist
	 */
	public String getFieldAt(int index) {
		if(index < 0 || index > (extraFields.length - 1))
			return "";
		return extraFields[index];
	}
	
	/**
	 * Returns clone on this element.
	 */
	public ISlideDataElement copy() {
		return new AffySlideDataElement(this);
	}
	
	public ISlideDataElement clone() {
		return copy();
	}
	
	public String toString() {
		return "SDE " + getRow(BASE) + ", " + getColumn(BASE);
	}
	
	/** Sets the UID field
	 */
	public void setUID(String uid) {
		this.UID = uid;
	}
	
	/** Returns the UID field
	 */
	public String getUID() {
		return this.UID;
	}
	
	/**
	 * Raktim - Element Annotation Object
	 */
	public IAnnotation getElementAnnotation() {
		return annotation;
	}
	
	// Sarita--State saving trial
	
	public void setElementAnnotation(IAnnotation annotation) {
		this.annotation=(MevAnnotation)annotation;
	}
    
}

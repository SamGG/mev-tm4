/**
 * Copyright @ 1999-2005, The Institute for Genomic Research (TIGR),
 * 2005-2008, the Dana-Farber Cancer Institute (DFCI).
 * All rights reserved.
 */
package org.tigr.microarray.mev.annotation;

import java.util.Hashtable;

import org.tigr.microarray.mev.cluster.gui.IData;

/**
 * @author eleanora
 *
 */
public class MevChipAnnotation implements IChipAnnotation {

	private Hashtable<String, String> annotations;
	
	public MevChipAnnotation() {
		annotations = new Hashtable<String, String>();
		annotations.put(AnnotationFieldConstants.DATA_TYPE, new Integer(IData.DATA_TYPE_TWO_INTENSITY).toString());
	}
	
	
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#getChipName()
	 */
	public String getChipName() {
		String temp = annotations.get(AnnotationFieldConstants.CHIP_NAME);
		if(temp != null)
			return temp;
		return AnnotationFieldConstants.NOT_AVAILABLE;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#getChipType()
	 */
	public String getChipType() {
		String temp = annotations.get(AnnotationFieldConstants.CHIP_TYPE);
		if(temp != null)
			return temp;
		return AnnotationFieldConstants.NOT_AVAILABLE;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#getGenomeBuild()
	 */
	public String getGenomeBuild() {
		String temp = annotations.get(AnnotationFieldConstants.GENOME_BUILD);
		if(temp != null)
			return temp;
		return AnnotationFieldConstants.NOT_AVAILABLE;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#getSpeciesName()
	 */
	public String getSpeciesName() {
		String temp = annotations.get(AnnotationFieldConstants.SPECIES_NAME);
		if(temp != null)
			return temp;
		return AnnotationFieldConstants.NOT_AVAILABLE;
	}
	
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#getAnnFileName()
	 */
	public String getAnnFileName() {
		String temp = annotations.get(AnnotationFieldConstants.ANNOTATION_FILE_NAME);
		if(temp != null)
			return temp;
		return AnnotationFieldConstants.NOT_AVAILABLE;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#getDataType()
	 */
	public String getDataType() {
		String temp = annotations.get(AnnotationFieldConstants.DATA_TYPE);
		if(temp != null)
			return temp;
		return AnnotationFieldConstants.NOT_AVAILABLE;
	}
	
	
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#setChipName(java.lang.String)
	 */
	public void setChipName(String temp) {
		annotations.put(AnnotationFieldConstants.CHIP_NAME, temp);
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#setChipType(java.lang.String)
	 */
	public void setChipType(String temp) {
		annotations.put(AnnotationFieldConstants.CHIP_TYPE, temp);
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#setGenomeBuild(java.lang.String)
	 */
	public void setGenomeBuild(String temp) {
		annotations.put(AnnotationFieldConstants.GENOME_BUILD, temp);
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#setSpeciesName(java.lang.String)
	 */
	public void setSpeciesName(String temp) {
		annotations.put(AnnotationFieldConstants.SPECIES_NAME, temp);
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#setAnnFileName(java.lang.String)
	 */
	public void setAnnFileName(String temp) {
		annotations.put(AnnotationFieldConstants.ANNOTATION_FILE_NAME, temp);
	}
	
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.annotation.IExperimentAnnotation#setDataType(java.lang.String)
	 */
	public void setDataType(String temp) {
		annotations.put(AnnotationFieldConstants.DATA_TYPE, temp);
	}
}

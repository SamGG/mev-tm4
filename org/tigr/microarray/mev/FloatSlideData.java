/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: FloatSlideData.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.util.Vector;
import java.util.Properties;
import javax.swing.JOptionPane;

import org.tigr.util.Xcon;
import org.tigr.util.math.LinearEquation;
import org.tigr.microarray.mev.cluster.gui.IData;

import org.tigr.midas.util.ColumnWorker;
import org.tigr.midas.engine.TotInt;
import org.tigr.midas.engine.IterativeLinReg;
import org.tigr.midas.engine.IterativeLogMean;
import org.tigr.midas.engine.RatioStats;
import org.tigr.midas.Constant;
import org.tigr.midas.display.ParameterPane;


/**
 *  Class to store CY3 and CY5 float values of a slide data.
 *  It is possible to get ArrayIndexOutOfBoundsException if an index
 *  was invalid.
 */
public class FloatSlideData implements ISlideData {
    
    private String name; // slide name
    private String filename; // slide file name
    private float[] currentCY3; // currentCY3 values
    private float[] currentCY5; // currentCY5 values
    private float[] trueCY3; // trueCY3 values
    private float[] trueCY5; // trueCY5 values
    private static boolean isNonZero = true;
    private int normalizedState = 0;
    private int sortState = 0;
    private ISlideMetaData slideMetaData;
    private SpotInformationData spotInfoData;
    private boolean abbrName = false;
    private int dataType = IData.DATA_TYPE_TWO_INTENSITY;
    
    // pcahan
    // affy detection flag is MAS's (P)resent, (A)bsent, (M)arginal
    private String[] detection;
    
    /**
     * Creates a <code>FloatSlideData</code> with specified reference
     * to a microarray meta data.
     */
    public FloatSlideData(ISlideMetaData slideMetaData) {
        this(slideMetaData, slideMetaData.getSize());
    }
    
    /**
     * Creates a <code>FloatSlideData</code> with specified reference
     * to a microarray meta data and initial size.
     * @param size the size of slide data.
     */
    public FloatSlideData(ISlideMetaData slideMetaData, int size) {
        this.slideMetaData = slideMetaData;
        trueCY3 = new float[size];
        trueCY5 = new float[size];
        detection = new String[size];
    }
    
    /**
     * Returns a reference to a microarray meta data.
     */
    public ISlideMetaData getSlideMetaData() {
        return slideMetaData;
    }
    
     /**
     * Sets the data type attribute see static type variables in <code>IData</code>
     */
    public void setDataType(int type){
        this.dataType = type;
    }
    
    /**
     * Returns the data type attribute
     */
    public int getDataType(){
        return this.dataType;
    }
    
    /**
     *  Sets the spot information data and associated column headers.
     */
    public void setSpotInformationData(String [] columnHeaders, String [][] spotInfoData){
        this.spotInfoData = new SpotInformationData(columnHeaders, spotInfoData);
    }
    
    /**
     *  Sets the spot information data and associated column headers.
     */
    public void setSpotInformationData(SpotInformationData spotInfoData){
        this.spotInfoData = spotInfoData;
    }
    
    /**
     * Returns the <code>SpotInformationData</code> object
     */
    public SpotInformationData getSpotInformationData(){ return this.spotInfoData; }
    
    /**
     * Returns size of a microarray.
     */
    public int getSize() {
        return slideMetaData.getSize();
    }
    
    /**
     * Sets a microarray name.
     */
    public void setSlideDataName(String name) {
        this.name = name;
    }
    
    /**
     *  sets boolean to indicate to abbr file and data name
     */
    public void toggleNameLength(){
        this.abbrName = (!this.abbrName);
    }
    
    /**
     * Returns the name of a microarray.
     */
    public String getSlideDataName() {
        if(!this.abbrName)
            return this.name;
        else{
            if(this.name.length() < 26)
                return this.name;
            return this.name.substring(0, 25)+"...";
        }
    }
    
    public String getFullSlideDataName() {
        return this.name;
    }
    
    /**
     * Sets a microarray file name.
     */
    public void setSlideFileName(String filename) {
        this.filename = filename;
    }
    
    /**
     * Returns a microarray file name.
     */
    public String getSlideFileName() {
        if(!this.abbrName)
            return this.filename;
        else{
            if(this.filename.length() < 26)
                return this.filename;
            return this.filename.substring(0, 25)+"...";
        }
    }
    
    /**
     * Used to assign the initial values.
     */
    public void setIntensities(int index, float cy3, float cy5) {
        trueCY3[index] = cy3;
        trueCY5[index] = cy5;
    }
    
    /**
     *  Assigns current (normalized intensities)
     */
    public void setCurrentIntensities(int index, float cy3, float cy5){
        if(this.currentCY3 != null && this.currentCY5 != null){
            this.currentCY3[index] = cy3;
            this.currentCY5[index] = cy5;
        }
    }
    
    /**
     * Returns CY3 value for specified index.
     */
    public float getCY3(int index) {
        if(normalizedState == ISlideData.NO_NORMALIZATION)
            return trueCY3[index];
        else
            return currentCY3[index];
    }
    
    /**
     * Returns CY5 value for specified index.
     */
    public float getCY5(int index) {
        if(normalizedState == ISlideData.NO_NORMALIZATION)
            return trueCY5[index];
        else
            return currentCY5[index];
    }
    
    /**
     * Returns a microarray max ratio value.
     */
    public float getMaxRatio() {
        return getMaxRatio(IData.LINEAR);
    }
    
    /**
     * Returns a microarray min ratio value.
     */
    public float getMinRatio() {
        return getMinRatio(IData.LINEAR);
    }
    
    /**
     * Returns a microarray max ratio value, with specified log state.
     */
    public float getMaxRatio(int logState) {
        float ratio, maxRatio = Float.MIN_VALUE;
        final int size = getSize();
        for (int i = 0; i < size; i++) {
            ratio = getRatio(i, logState);
            if (ratio > maxRatio)
                maxRatio = ratio;
        }
        return maxRatio;
    }
    
    /**
     * Returns a microarray max ratio value of specified intensities.
     */
    public float getMaxRatio(int index1, int index2, int logState) {
        if (index1 == index2) {
            throw new IllegalArgumentException("The indices should not be equals.");
        }
        float ratio, maxRatio = Float.MIN_VALUE;
        float[] numerator, denumerator;
        if (index1 == ISlideDataElement.CY5) {
            numerator = trueCY5;
            denumerator = trueCY3;
        } else {
            numerator = trueCY3;
            denumerator = trueCY5;
        }
        final int size = getSize();
        for (int i = 0; i < size; i++) {
            ratio = getRatio(numerator[i], denumerator[i], logState);
            if (ratio > maxRatio)
                maxRatio = ratio;
        }
        return maxRatio;
    }
    
    /**
     * Returns a microarray min ratio value.
     */
    public float getMinRatio(int logState) {
        float ratio, minRatio = Float.MAX_VALUE;
        final int SIZE = getSize();
        for (int i = SIZE; --i >= 0;) {
            ratio = getRatio(i, logState);
            if (ratio < minRatio)
                minRatio = ratio;
        }
        return minRatio;
    }
    
    /**
     * Returns a microarray max ratio value of specified intensities.
     */
    public float getMinRatio(int index1, int index2, int logState) {
        if (index1 == index2) {
            throw new IllegalArgumentException("The indices should not be equals.");
        }
        float ratio, minRatio = Float.MAX_VALUE;
        float[] numerator, denumerator;
        if (index1 == ISlideDataElement.CY5) {
            numerator = trueCY5;
            denumerator = trueCY3;
        } else {
            numerator = trueCY3;
            denumerator = trueCY5;
        }
        final int size = getSize();
        for (int i = 0; i < size; i++) {
            ratio = getRatio(numerator[i], denumerator[i], logState);
            if (ratio < minRatio)
                minRatio = ratio;
        }
        return minRatio;
    }
    
    /**
     * Returns a ratio value with specified index and log state.
     */
    public final float getRatio(int index, int logState) {
        if(normalizedState == ISlideData.NO_NORMALIZATION)
            return getRatio(trueCY5[index], trueCY3[index], logState);
        else
            return getRatio(currentCY5[index], currentCY3[index], logState);
    }
    
    /**
     * Sets the non-zero flag.
     */
    public void setNonZero(boolean value) {
        isNonZero = value;
    }
    
    /**
     * Returns a ratio of specified values.
     */
    public static final float getRatio(float numerator, float denominator, int logState) {
        float ratio;
        if(denominator < 0 || numerator < 0)
            return Float.NaN;
        if (isNonZero) {
            if (denominator == 0 && numerator == 0) {
                return Float.NaN;
            } else if (numerator == 0) {
                ratio = 1f/denominator;
            } else if (denominator == 0) {
                ratio = numerator;
            } else {
                ratio = numerator/denominator;
            }
        } else {
            if (denominator == 0)
                return Float.NaN;
            if (numerator == 0)
                return Float.NaN;
            ratio = numerator/denominator;
        }
        if (logState == IData.LOG)
            ratio = (float)(Math.log(ratio)/Math.log(2.0));
        return ratio;
    }
    
    /**
     * Returns a microarray max CY3 value.
     */
    public float getMaxCY3() {
        if(this.normalizedState == ISlideData.NO_NORMALIZATION)
            return getMaxCY(trueCY3);
        else
            return getMaxCY(currentCY3);
        
    }
    
    /**
     * Returns a microarray max CY5 value.
     */
    public float getMaxCY5() {
        if(this.normalizedState == ISlideData.NO_NORMALIZATION)
            return getMaxCY(trueCY5);
        else
            return getMaxCY(currentCY5);
    }
    
    /**
     * Returns max value from a specified array.
     */
    private float getMaxCY(float[] CY) {
        float maxIntensity = 0f;
        final int SIZE = getSize();
        for (int i = SIZE; --i >= 0;) {
            if (CY[i] > maxIntensity)
                maxIntensity = CY[i];
        }
        return maxIntensity;
    }
    
    /**
     * Returns a microarray min CY3 value.
     */
    public float getMinCY3() {
        if(this.normalizedState == ISlideData.NO_NORMALIZATION)
            return getMinIntensity(trueCY3, true);
        else
            return getMinIntensity(currentCY3, true);
    }
    
    /**
     * Returns a microarray min CY5 value.
     */
    public float getMinCY5() {
        if(this.normalizedState == ISlideData.NO_NORMALIZATION)
            return getMinIntensity(trueCY5, true);
        else
            return getMinIntensity(currentCY5, true);
    }
    
    /**
     * Returns min value from a specified array.
     */
    private float getMinIntensity(float[] CY, boolean acceptZeros) {
        float intensity, minIntensity = Float.MAX_VALUE;
        final int SIZE = getSize();
        for (int i = SIZE; --i >= 0;) {
            intensity = CY[i];
            if (!acceptZeros) {
                if ((intensity < minIntensity) && (intensity != 0))
                    minIntensity = intensity;
            } else {
                if (intensity < minIntensity)
                    minIntensity = intensity;
            }
        }
        return minIntensity;
    }
    
    /**
     * Returns a microarray normalization state.
     */
    public int getNormalizedState() {
        return normalizedState;
    }
    
    /**
     * Sets a microarray normalization state.
     */
    public void setNormalizedState(int normalizedState) {
        this.normalizedState = normalizedState;
    }
    
    /**
     * Returns a microarray sort state.
     */
    public int getSortState() {
        return sortState;
    }
    
    /**
     * Sets a microarray sort state.
     */
    public void setSortState(int sortState) {
        this.sortState = sortState;
    }
    
    // pcahan
    public void setDetection(int index, String value){
        detection[index] = value;
    }
    
    // pcahan
    public String getDetection(int index){
        return detection[index];
    }
    
    /**
     * Returns max intencity of specified type.
     */
    public float getMaxIntensity(int intensityType) {
        switch (intensityType) {
            case ISlideDataElement.CY3:
                return getMaxCY3();
            case ISlideDataElement.CY5:
                return getMaxCY5();
        }
        return Float.NaN;
    }
    
    /**
     * Returns min intencity of specified type.
     */
    public float getMinIntensity(int intensityType, boolean acceptZeros) {
        return getMinIntensity(getIntensities(intensityType), acceptZeros);
    }
    
    /**
     * Returns an array of intensities with specified type.
     */
    private float[] getIntensities(int type) {
        switch (type) {
            case ISlideDataElement.CY3:
                return trueCY3;
            case ISlideDataElement.CY5:
                return trueCY5;
        }
        return null;
    }
    
    /**
     * Returns a microarray max product value of specified intensities.
     */
    public float getMaxProduct(int index1, int index2) {
        float product = 0, maxProduct = 0;
        float[] v_1 = getIntensities(index1);
        float[] v_2 = getIntensities(index2);
        final int size = getSize();
        for (int i = 0; i < size; i++) {
            product = v_1[i] * v_2[i];
            if (product > maxProduct)
                maxProduct = product;
        }
        return maxProduct;
    }
    
    /**
     * Returns a microarray min product value of specified intensities.
     */
    public float getMinProduct(int index1, int index2, boolean acceptZeros) {
        return getMinProduct(index1, index2, acceptZeros, 0);
    }
    
    /**
     * Returns a microarray min product value of specified intensities and lower cutoff.
     */
    public float getMinProduct(int index1, int index2, boolean acceptZeros, int lowCutoff) {
        float product = 0, minProduct = Float.MAX_VALUE;
        float[] v_1 = getIntensities(index1);
        float[] v_2 = getIntensities(index2);
        final int size = getSize();
        for (int i = 0; i < size; i++) {
            product = v_1[i] * v_2[i];
            if (product >= lowCutoff) {
                if (!acceptZeros) {
                    if ((product < minProduct) && (product != 0))
                        minProduct = product;
                } else {
                    if (product < minProduct)
                        minProduct = product;
                }
            }
        }
        return minProduct;
    }
    
    /**
     * Returns an <code>ISlideDataElement</code> by specified index.
     */
    public ISlideDataElement getSlideDataElement(int index) {
        ISlideDataElement sde = slideMetaData.toSlideDataElement(index);
        sde.setIntensity(ISlideDataElement.CY3, getCY3(index));
        sde.setIntensity(ISlideDataElement.CY5, getCY5(index));
        sde.setTrueIntensity(ISlideDataElement.CY3, getCY3(index));
        sde.setTrueIntensity(ISlideDataElement.CY5, getCY5(index));
        //pcahan
        sde.setDetection(getDetection(index));
        
        return sde;
    }
    
    /**
     * Doesn't supported method.
     */
    public void addSlideDataElement(ISlideDataElement element) {
        throwNotImplemented("addSlideDataElement");
    }
    
    /**
     * Creates copy of an original microarray data.
     */
    private void createDataCopy() {
        if (this.currentCY3 == null || this.currentCY5 == null) {
            this.currentCY3 = new float[trueCY3.length];
            this.currentCY5 = new float[trueCY5.length];
            System.arraycopy(trueCY3, 0, currentCY3, 0, currentCY3.length);
            System.arraycopy(trueCY5, 0, currentCY5, 0, currentCY5.length);
        }
    }
    
    /**
     * Creates copy of an original microarray data.
     */
    public void createCurrentIntensityArrays() {
        if (this.currentCY3 == null || this.currentCY5 == null) {
            this.currentCY3 = new float[trueCY3.length];
            this.currentCY5 = new float[trueCY5.length];
            System.arraycopy(trueCY3, 0, currentCY3, 0, currentCY3.length);
            System.arraycopy(trueCY5, 0, currentCY5, 0, currentCY5.length);
        }
    }
    
    
    /**********************************************************************
     * Data Normalization Code.  04-2003 MeV will support only Total Intensity
     * Iterative Linear Regression, Ratio Statistics, and Iterative Log Mean Centering.
     * Support classes and normalization algorithms have been incorporated from
     * TIGR-MIDAS 2.16 (Author: Wei Liang, The Institute for Genomic Research)
     * which remains the primary tool for data normalization
     *
     * ColumnWorker is the main data structure used by the algorithms
     *
     * A typical normalization proceeds as follows:
     * 1.) "True Intensities" are accumulated into double arrays and a ColumnWorker is
     *      constructed.
     * 2.) The ColumnWorker is passed via constructor into a normalization algorithm
     *     which performs the required manipulations. (parameters are also in the constructor)
     * 3.) Data is retrieved from the resulting ColumnWorker and the data is placed
     *     into the appropriate SlideDataElements (or arrays) as "Current Intensities"
     *     (normalized).
     *
     * Conventions for normalization:
     *
     *  "normalizedState" a class variable is used to indicate which intensities and
     *  ratio's to deliver.  ISlideData.NO_NORMALIZATION forces the return of "True"
     *  intensities, meaning unaltered,  while any other state indicates that "Current"
     *  intensities (altered) should be returned.
     *
     * Handling zeros:
     *
     * A zero in one or both channels would greatly impact the normalization of points
     * containing two good intensities.  For this reason the following convention for handling
     * one or two zero intensities has been adopted.  If one or both intensities are zero this
     * data is not passed on to the ColumnWorker for normalization and following the normalization
     * the "current" intensities are set to equal the "true", original, intensities.
     *
     * By this convention the points are preserved AND the data that has been normalized
     * matches MIDAS output.
     *
     ************************************************************************************/
    
    
    /**
     * Applies a microarray data normalization.
     */
    public void applyNormalization(int normalizationMode, Properties props) {
        createDataCopy();
        switch (normalizationMode) {
            case ISlideData.NO_NORMALIZATION:
                applyNoNormalization();
                break;
            case ISlideData.TOTAL_INTENSITY:
                applyTotalIntensity();
                break;
            case ISlideData.LEAST_SQUARES: // not used
                applyLeastSquares();
                break;
            case ISlideData.LINEAR_REGRESSION:
                applyLinearRegression(props);
                break;
            case ISlideData.RATIO_STATISTICS_95: // not used
                applyRatioStatistics(props);
                break;
            case ISlideData.RATIO_STATISTICS_99:
                applyRatioStatistics(props);
                break;
            case ISlideData.ITERATIVE_LOG:
                applyIterativeLog(props);
                break;
            case ISlideData.LOWESS:  // not used
                applyLowess(10);
                break;
        }
        setNormalizedState(normalizationMode);
    }
    
    //Don't forget to change over to the list versions of the functions
    public void applyNormalizationList(int normalizationMode) {
        createDataCopy();
        switch (normalizationMode) {
            case ISlideData.TOTAL_INTENSITY_LIST:
                applyTotalIntensity();
                break;
            case ISlideData.LEAST_SQUARES_LIST:
                applyLeastSquares();
                break;
            case ISlideData.LINEAR_REGRESSION_LIST:
                applyLinearRegression(new Properties());
                break;
            case ISlideData.RATIO_STATISTICS_95_LIST:
                applyRatioStatistics(new Properties());
                break;
            case ISlideData.RATIO_STATISTICS_99_LIST:
                applyRatioStatistics(new Properties());
                break;
            case ISlideData.ITERATIVE_LOG_LIST:
                applyIterativeLog(new Properties());
                break;
            case ISlideData.LOWESS_LIST:
                applyLowess(10);
                break;
        }
        setNormalizedState(normalizationMode);
    }
    
    /**
     * Restore true values.
     */
    private void applyNoNormalization() {
        if (this.trueCY3 != null && this.trueCY5 != null) {
            System.arraycopy(trueCY3, 0, currentCY3, 0, trueCY3.length);
            System.arraycopy(trueCY5, 0, currentCY5, 0, trueCY5.length);
        }
    }
    
    
    /**
     * Applies total intensity normalization.
     */
    public void applyTotalIntensity() {
        boolean [] goodValues = new boolean[this.getSize()];
        ColumnWorker cw = constructColumnWorker(goodValues);
        cw = ((new TotInt(cw, false)).getFileTotIntColumnWorker());
        setNormalizedIntensities(cw, goodValues);
        normalizedState = ISlideData.TOTAL_INTENSITY;
    }
    
    /**
     * Applies linear regression normalization.
     */
    public void applyLinearRegression(Properties properties) {
        boolean [] goodValues = new boolean[this.getSize()];
        ColumnWorker cw = constructColumnWorker(goodValues);
        
        try{
            String  mode = (String)properties.get("mode");
            double sd = Double.parseDouble((String)properties.get("standard-deviation"));
            
            cw = ((new IterativeLinReg(cw, sd, mode)).getIterLinRegColumnWorker());
            setNormalizedIntensities(cw, goodValues);
            normalizedState = ISlideData.LINEAR_REGRESSION;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(new javax.swing.JFrame(), "Error Performing Normalization: Data Unchanged", "Normalization Error : Aborted", javax.swing.JOptionPane.WARNING_MESSAGE);
            normalizedState = ISlideData.NO_NORMALIZATION;
            e.printStackTrace();
        }
    }
    
    /**
     * Applies ratio statistics normalization.
     */
    public void applyRatioStatistics(Properties properties) {
        boolean [] goodValues = new boolean[this.getSize()];
        ColumnWorker cw = constructColumnWorker(goodValues);
        
        try{
            int confInt = Integer.parseInt((String)properties.get("confidence-interval"));
            
            cw = ((new RatioStats(cw, false, confInt)).getRatioStatsColumnWorker());
            setNormalizedIntensities(cw, goodValues);
            normalizedState = ISlideData.LINEAR_REGRESSION;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(new javax.swing.JFrame(), "Error Performing Normalization: Data Unchanged", "Normalization Error : Aborted", javax.swing.JOptionPane.WARNING_MESSAGE);
            normalizedState = ISlideData.NO_NORMALIZATION;
            e.printStackTrace();
        }
    }
    
    /**
     * Applies iterative log normalization.
     */
    public void applyIterativeLog(Properties properties) {
        boolean [] goodValues = new boolean[this.getSize()];
        ColumnWorker cw = constructColumnWorker(goodValues);
        
        try{
            double sd = Double.parseDouble((String)properties.get("standard-deviation"));
            
            cw = ((new IterativeLogMean(cw, sd)).getIterLogMeanColumnWorker());
            setNormalizedIntensities(cw, goodValues);
            normalizedState = ISlideData.ITERATIVE_LOG;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(new javax.swing.JFrame(), "Error Performing Normalization: Data Unchanged", "Normalization Error : Aborted", javax.swing.JOptionPane.WARNING_MESSAGE);
            normalizedState = ISlideData.NO_NORMALIZATION;
        }
    }
    
    /**
     * Creates a ColumnWorker object for normalization
     */
    private ColumnWorker constructColumnWorker(boolean [] goodValues){
        int n = this.getSize();
        int size = n;
        double CY3;
        double CY5;
        double [] cy3 = new double[n];
        double [] cy5 = new double[n];
        String [] metaCombo = new String[n];
        int metaRow, metaColumn;
        double [] goodCy3;
        double [] goodCy5;
        n = 0;
        for(int i = 0; i < size; i++){
            
            metaRow = this.getSlideDataElement(i).getRow(ISlideDataElement.META);
            metaColumn = this.getSlideDataElement(i).getColumn(ISlideDataElement.META);
            
            CY3 = (double)trueCY3[i];
            CY5 = (double)trueCY5[i];
            if(CY3 != 0 && CY5 != 0){
                goodValues[i] = true;
                cy3[n] = CY3;
                cy5[n] = CY5;
                metaCombo[n] = Integer.toString(metaRow)+"_"+Integer.toString(metaColumn);
                n++;
            }
        }
        goodCy3 = new double[n];
        goodCy5 = new double[n];
        System.arraycopy(cy3, 0, goodCy3, 0, n);
        System.arraycopy(cy5, 0, goodCy5, 0, n);
        System.arraycopy(metaCombo, 0, metaCombo, 0, n);
        return new ColumnWorker(goodCy3, goodCy5, metaCombo);
    }
    
    /**
     *  Extracts data from a ColumnWorker into ISlideData current (normalized) intensities
     */
    private void setNormalizedIntensities(ColumnWorker cw, boolean [] goodIntensity){
        double [] cy3 = cw.getColumnOneArray();
        double [] cy5 = cw.getColumnTwoArray();
        int goodIndex= 0;
        for(int i = 0 ; i < goodIntensity.length; i++){
            if(goodIntensity[i]){
                currentCY3[i] = (float)cy3[goodIndex];
                currentCY5[i] = (float)cy5[goodIndex];
                goodIndex++;
            }
            else{
                currentCY3[i] = trueCY3[i];
                currentCY5[i] = trueCY5[i];
            }
        }
    }
    
    /**********************************************************************
     * End supported and used normalization code
     */
    
    
    
    /**
     * Applies total intensity normalization.
     */
    private final float applyTotalIntensity(float value, float totalCy3, float totalCy5) {
        if (value > 0) {
            return value*totalCy3/totalCy5;
        } else {
            return 0f;
        }
    }
    
    /**
     * Returns intensities sum of specified type.
     */
    private float getSumIntensity(int intensityType) {
        float totalIntensity = 0f;
        float[] intensities = getIntensities(intensityType);
        for (int i = 0; i < intensities.length; i++) {
            totalIntensity += intensities[i];
        }
        return totalIntensity;
    }
    
    /**
     * Returns non-zero intensities sum of specified type.
     */
    private float getSumNonZeroIntensity(int intensityType) {
        float totalIntensity = 0;
        float[] intensities = getIntensities(intensityType);
        for (int i = 0; i < intensities.length; i++) {
            if (slideMetaData.hasNoZeros(i))
                totalIntensity += intensities[i];
        }
        return totalIntensity;
    }
    
    /**
     * Applies least squares normalization.
     */
    private void applyLeastSquares() {
        if (getNormalizedState() != SlideData.LEAST_SQUARES) {
            LinearEquation linearEquation = getRegressionEquation(true);
            final int size = getSize();
            for (int i = 0; i < size; i++) {
                currentCY5[i] = applyLeastSquares(trueCY5[i], linearEquation);
            }
        }
    }
    
    /**
     * Applies least squares normalization.
     */
    private float applyLeastSquares(float value, LinearEquation linearEquation) {
        if (value > 0) {
            return(float)((value-linearEquation.getYIntercept())*(1/linearEquation.getSlope()));
        } else {
            return 0f;
        }
    }
    
    /**
     * Calculates a linear equation.
     */
    private LinearEquation getRegressionEquation(boolean useTrueValues) {
        LinearEquation linearEquation;
        double x = 0, y = 0;
        double sum = 0, sumX = 0, sumY = 0;
        double sumX2 = 0, sumY2 = 0, sumXY = 0;
        double weight = 1; //Change when set dynamically
        double delta = 0, a = 0, b = 0;
        double variance = 1; //Length of potence
        double regressionCoefficient = 0;
        double sigmaA = 0, sigmaB = 0;
        float[] cy3 = useTrueValues ? trueCY3 : currentCY3;
        float[] cy5 = useTrueValues ? trueCY5 : currentCY5;
        final int size = getSize();
        for (int i = 0; i < size; i++) {
            x = (double) cy3[i];
            y = (double) cy5[i];
            if (x != 0 && y != 0) {
                sum += weight;
                sumX += (weight * x);
                sumY += (weight * y);
                sumX2 += (weight * x * x);
                sumY2 += (weight * y * y);
                sumXY += (weight * x * y);
            }
        }
        
        delta = (sum * sumX2) - (sumX * sumX);
        a = ((sumX2 * sumY) - (sumX * sumXY)) / delta;
        b = ((sumXY * sum) - (sumX * sumY)) / delta;
        sigmaA = Math.sqrt(variance * sumX2 / delta);
        sigmaB = Math.sqrt(variance * sum / delta);
        regressionCoefficient = ((sum * sumXY - sumX * sumY) / Math.sqrt(delta * (sum * sumY2) - (sumY * sumY)));
        
        linearEquation = new LinearEquation(b, a, regressionCoefficient);
        return linearEquation;
    }
    
    
    
    private void applyLowess(int bins) {
        throwNotImplemented("applyLowess");
    }
    
    private void throwNotImplemented(String methodName) {
        throw new RuntimeException("Method '"+methodName+"' is not supported");
    }
    
    
}

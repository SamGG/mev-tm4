/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TtestCentroidViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-05-02 16:57:56 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.ttest;

import java.awt.Frame;
import java.beans.Expression;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;

public class TtestCentroidViewer extends CentroidViewer {
    
    private Vector rawPValues, adjPValues, tValues, dfValues, meansA, meansB, sdA, sdB, oneClassMeans, oneClassSDs;
    private int tTestDesign;
    /**
     * Construct a <code>TtestCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public TtestCentroidViewer(Experiment experiment, int[][] clusters, int tTestDesign, Vector oneClassMeans, Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, Vector rawPValues, Vector adjPValues, Vector tValues, Vector dfValues) {
        super(experiment, clusters);
        this.rawPValues = rawPValues;
        this.adjPValues = adjPValues;
        this.tValues = tValues;
        this.dfValues = dfValues;
        this.meansA = meansA;
        this.meansB = meansB;
        this.tTestDesign = tTestDesign;
        this.oneClassMeans = oneClassMeans;
        this.oneClassSDs = oneClassSDs;
        this.sdA = sdA; 
        this.sdB =sdB;
    }

    /**
     * @inheritDoc
     */
    public TtestCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes,
    		 Integer tTestDesign, Vector oneClassMeans, Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, Vector rawPValues, Vector adjPValues, Vector tValues, Vector dfValues) {
    	super(e, clusters, variances, means, codes);
        this.rawPValues = rawPValues;
        this.adjPValues = adjPValues;
        this.tValues = tValues;
        this.dfValues = dfValues;
        this.meansA = meansA;
        this.meansB = meansB;
        this.tTestDesign = tTestDesign.intValue();
        this.oneClassMeans = oneClassMeans;
        this.oneClassSDs = oneClassSDs;
        this.sdA = sdA; 
        this.sdB =sdB;
     }
 
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.experiment, this.clusters, this.variances, this.means, this.codes,  
    			new Integer(this.tTestDesign), this.oneClassMeans, this.oneClassSDs, this.meansA, this.meansB, this.sdA, this.sdB, this.rawPValues, this.adjPValues, this.tValues, this.dfValues});
    }
    
    /**
     * Saves all clusters.
     */
    protected void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            saveExperiment(frame, getExperiment(), getData(), getClusters());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Save the viewer cluster.
     */
    protected void onSaveCluster() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            saveExperiment(frame, getExperiment(), getData(), getCluster());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Saves values from specified experiment and its rows.
     */
    public void saveExperiment(Frame frame, Experiment experiment, IData data, int[] rows) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            saveCluster(file, experiment, data, rows);
        }
    }
    
    /**
     * Saves values from specified experiment and cluster.
     */
    public void saveExperiment(Frame frame, Experiment experiment, IData data, int[][] clusters) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            File aFile;
            for (int i=0; i<clusters.length; i++) {
                if (clusters[i] == null || clusters[i].length == 0) {
                    continue;
                }
                aFile = new File(file.getPath()+"-"+String.valueOf(i+1)+".txt");
                saveCluster(aFile, experiment, data, clusters[i]);
            }
        }
    }
    
    private void saveCluster(File file, Experiment experiment, IData data, int[] rows) throws Exception {
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = data.getFieldNames();
        out.print("Original row");
        out.print("\t");
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            //if (i < fieldNames.length - 1) {
                out.print("\t");
            //}
        }
        if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) || (tTestDesign == TtestInitDialog.PAIRED)) {        
            out.print("GroupA mean\t");
            out.print("GroupA std.dev.\t");
            out.print("GroupB mean\t");
            out.print("GroupB std.dev.\t");
            out.print("Absolute t value");
        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            out.print("Gene mean\t");
            out.print("Gene std.dev.\t");
            out.print("t value");
        }
        //out.print("\t");
        
        out.print("\t");
        out.print("Degrees of freedom\t");
        out.print("Raw p value\t");
        out.print("Adj p value");
        
        //out.print("UniqueID\tName");
        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            out.print("\t");
            out.print(data.getSampleName(experiment.getSampleIndex(i)));
        }
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            out.print(Integer.toString(experiment.getGeneIndexMappedToData(rows[i]) + 1));
            //out.print(data.getUniqueId(rows[i]));
            out.print("\t");
            //out.print(data.getGeneName(rows[i]));
            for (int k = 0; k < fieldNames.length; k++) {
                out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), k));
                //if (k < fieldNames.length - 1) {
                    out.print("\t");
                //}
            }
            if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) || (tTestDesign == TtestInitDialog.PAIRED)) {            
                out.print(((Float)meansA.get(rows[i])).floatValue() + "\t");
                out.print(((Float)sdA.get(rows[i])).floatValue() + "\t");
                out.print(((Float)meansB.get(rows[i])).floatValue() + "\t");
                out.print(((Float)sdB.get(rows[i])).floatValue() + "\t");
            } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                out.print(((Float)oneClassMeans.get(rows[i])).floatValue() + "\t");
                out.print(((Float)oneClassSDs.get(rows[i])).floatValue() + "\t");
            } 
            //out.print("\t");
            out.print("" + ((Float)tValues.get(rows[i])).floatValue());
            out.print("\t");
            out.print("" + ((Float)dfValues.get(rows[i])).intValue());
            out.print("\t");            
            out.print("" + ((Float)rawPValues.get(rows[i])).floatValue());
            out.print("\t");            
            out.print("" + ((Float)adjPValues.get(rows[i])).floatValue());            
            for (int j=0; j<experiment.getNumberOfSamples(); j++) {
                out.print("\t");
                out.print(Float.toString(experiment.get(rows[i], j)));
            }
            out.print("\n");
        }
        out.flush();
        out.close();
    }
    
    /**
     * Returns a file choosed by the user.
     */
    private static File getFile(Frame frame) {
        File file = null;
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
        fc.addChoosableFileFilter(new ExpressionFileFilter());
        fc.setFileView(new ExpressionFileView());
        int ret = fc.showSaveDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
        return file;
    }
    
}


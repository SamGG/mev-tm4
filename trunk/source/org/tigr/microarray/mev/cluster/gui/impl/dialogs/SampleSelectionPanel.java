/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * SampleSelectionPanel.java
 *
 * Created on December 12, 2002, 4:56 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;


/**
 *
 * @author  braisted
 */
public class SampleSelectionPanel extends javax.swing.JPanel {

    boolean result = true;
    
    /** Creates new form SampleSelectionPanel */
    public SampleSelectionPanel() {
        this(Color.gray, Color.black, false, null);
    }
    
   /** Creates new form SampleSelectionPanel */
    public SampleSelectionPanel(Color background, Color foreground, boolean etchedTitleBorder, String borderTitle){
        initComponentsWithoutTheStupidForm();
        setBackground(background);
        setForeground(foreground);
        this.clusterGenesButton.setBackground(background);
        this.clusterGenesButton.setForeground(foreground);
        this.clusterSamplesButton.setBackground(background);
        this.clusterSamplesButton.setForeground(foreground);
        
        if(etchedTitleBorder)
        setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), borderTitle, 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Dialog", 1, 12), Color.black));               
    }
    
    public void setButtonText(String geneButtonText, String experimentButtonText){
        this.clusterGenesButton.setText(geneButtonText);
        this.clusterSamplesButton.setText(experimentButtonText);
        this.validate();
    }   
    
    public boolean isClusterGenesSelected(){
        return this.clusterGenesButton.isSelected();
    }
    
    public void setClusterGenesSelected(boolean value){
        clusterGenesButton.setSelected(value);
    }

    public void setExperimentButtonActionListener(ActionListener listener){
        clusterSamplesButton.addActionListener(listener);
    }
    
    public void setGeneButtonActionListener(ActionListener listener){
        clusterGenesButton.addActionListener(listener);
    }
    public void setSampleButtonActionListener(ActionListener listener){
        clusterSamplesButton.addActionListener(listener);
    }
    public void setGeneButtonItemListener(ItemListener listener){
        clusterGenesButton.addItemListener(listener);
    }    
    public void setSampleButtonItemListener(ItemListener listener){
        clusterSamplesButton.addItemListener(listener);
    }
    public javax.swing.JRadioButton getGeneButton(){
    	return clusterGenesButton;
    }
    public javax.swing.JRadioButton getSampleButton(){
    	return clusterSamplesButton;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        selectButtonGroup = new javax.swing.ButtonGroup();
        clusterGenesButton = new javax.swing.JRadioButton();
        clusterSamplesButton = new javax.swing.JRadioButton();
        
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        clusterGenesButton.setSelected(true);
        clusterGenesButton.setText("Cluster Genes");
        selectButtonGroup.add(clusterGenesButton);
        clusterGenesButton.setFocusPainted(false);
        clusterGenesButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        add(clusterGenesButton, gridBagConstraints1);
        
        clusterSamplesButton.setText("Cluster Experiments");
        selectButtonGroup.add(clusterSamplesButton);
        clusterSamplesButton.setFocusPainted(false);
        clusterSamplesButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        add(clusterSamplesButton, gridBagConstraints1);
        
    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup selectButtonGroup;
    private javax.swing.JRadioButton clusterGenesButton;
    private javax.swing.JRadioButton clusterSamplesButton;
    // End of variables declaration//GEN-END:variables

        
    private void initComponentsWithoutTheStupidForm() {
        selectButtonGroup = new javax.swing.ButtonGroup();
        clusterGenesButton = new javax.swing.JRadioButton();
        clusterSamplesButton = new javax.swing.JRadioButton();
        
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        clusterGenesButton.setSelected(true);
        clusterGenesButton.setText("Cluster Genes");
        selectButtonGroup.add(clusterGenesButton);
        clusterGenesButton.setFocusPainted(false);
        clusterGenesButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        add(clusterGenesButton, gridBagConstraints1);
        
        clusterSamplesButton.setText("Cluster Samples");
        selectButtonGroup.add(clusterSamplesButton);
        clusterSamplesButton.setFocusPainted(false);
        clusterSamplesButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        add(clusterSamplesButton, gridBagConstraints1);
        
    }


}
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SVMTrain_ClassifierSelectDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:21:56 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

public class SVMTrain_ClassifierSelectDialog extends javax.swing.JDialog {
    
    int result = JOptionPane.CANCEL_OPTION;
    
    /** Creates new form SVMTrain_ClassifierSelectDialog */
    public SVMTrain_ClassifierSelectDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        Listener listener = new Listener();
        
        this.continueButton.addActionListener(listener);
        this.continueButton.setActionCommand("ok-command"); 
        
        this.cancelButton.addActionListener(listener);
        this.cancelButton.setActionCommand("cancel-command");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        sampleSelection = new javax.swing.ButtonGroup();
        processSelection = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        runGenes = new javax.swing.JRadioButton();
        runExperiments = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        trainAndClassifyButton = new javax.swing.JRadioButton();
        trainOnlyButton = new javax.swing.JRadioButton();
        classifyOnlyButton = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        continueButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        calcHCLCheckBox = new javax.swing.JCheckBox();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("SVM Sample and Process Selection");
        setBackground(java.awt.Color.lightGray);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setBackground(java.awt.Color.white);
        jPanel1.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED), "Evaluation Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14)));
        runGenes.setBackground(java.awt.Color.white);
        runGenes.setSelected(true);
        runGenes.setText("Classify Genes");
        runGenes.setToolTipText("");
        sampleSelection.add(runGenes);
        runGenes.setFocusPainted(false);
        jPanel1.add(runGenes);

        runExperiments.setBackground(java.awt.Color.white);
        runExperiments.setText("Classify Experiments");
        sampleSelection.add(runExperiments);
        runExperiments.setFocusPainted(false);
        jPanel1.add(runExperiments);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBackground(java.awt.Color.white);
        jPanel2.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED), "SVM Process Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14)));
        trainAndClassifyButton.setBackground(java.awt.Color.white);
        trainAndClassifyButton.setSelected(true);
        trainAndClassifyButton.setText("Train SVM then Classify");
        trainAndClassifyButton.setToolTipText("Train SMV then immediately classify");
        processSelection.add(trainAndClassifyButton);
        trainAndClassifyButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanel2.add(trainAndClassifyButton, gridBagConstraints);

        trainOnlyButton.setBackground(java.awt.Color.white);
        trainOnlyButton.setText("Train SVM (skip classify)");
        trainOnlyButton.setToolTipText("Train SVM only... output are result SVM weights");
        processSelection.add(trainOnlyButton);
        trainOnlyButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanel2.add(trainOnlyButton, gridBagConstraints);

        classifyOnlyButton.setBackground(java.awt.Color.white);
        classifyOnlyButton.setText("Classify using existing SVM file");
        classifyOnlyButton.setToolTipText("Trains current data using and SVM file of weights");
        processSelection.add(classifyOnlyButton);
        classifyOnlyButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanel2.add(classifyOnlyButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel2, gridBagConstraints);

        continueButton.setText("Continue");
        jPanel4.add(continueButton);

        cancelButton.setText("Cancel");
        jPanel4.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel4, gridBagConstraints);

        jPanel3.setBackground(java.awt.Color.white);
        jPanel3.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Hierarchical Clustering", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14)));
        jPanel3.setMinimumSize(new java.awt.Dimension(310, 55));
        jPanel3.setPreferredSize(new java.awt.Dimension(310, 55));
        calcHCLCheckBox.setBackground(java.awt.Color.white);
        calcHCLCheckBox.setText("Calculate Hierarchical Clustering on SVM Results");
        calcHCLCheckBox.setBorder(new javax.swing.border.CompoundBorder());
        calcHCLCheckBox.setFocusPainted(false);
        jPanel3.add(calcHCLCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel3, gridBagConstraints);

        pack();
    }//GEN-END:initComponents
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
     
    /**
     * Returns true if gene SVM's are to be evaluated
     */
    public boolean isEvaluateGenesSelected(){
        if(this.runGenes.isSelected())
            return true;
        else
            return false;
    }
    
    /**
     *  Returns a constant indicating which SVM process to run
     *  Train and classify, train only, classify only
     */
    public int getSVMProcessSelection(){
        if(this.trainAndClassifyButton.isSelected())
            return SVMGUI.TRAIN_AND_CLASSIFY;
        else if(this.trainOnlyButton.isSelected())
            return SVMGUI.TRAIN_ONLY;
        else
            return SVMGUI.CLASSIFY_ONLY;
    }
    
    /**
     * Returns boolean selection for calculating HCL on SVM results
     */
    public boolean getHCLSelection(){
        return this.calcHCLCheckBox.isSelected();
    }
    
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new SVMTrain_ClassifierSelectDialog(new javax.swing.JFrame(), true).show();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton trainOnlyButton;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton trainAndClassifyButton;
    private javax.swing.JButton continueButton;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JCheckBox calcHCLCheckBox;
    private javax.swing.ButtonGroup processSelection;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton runExperiments;
    private javax.swing.JRadioButton classifyOnlyButton;
    private javax.swing.ButtonGroup sampleSelection;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton runGenes;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
     
    private class Listener extends DialogListener { 
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                try {
                    result = JOptionPane.OK_OPTION;
                } catch (Exception exception) {
                    exception.printStackTrace();
                    result = JOptionPane.CANCEL_OPTION;
                }
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
        
    }
    
}

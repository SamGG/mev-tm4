/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMTrain_ClassifierSelectDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

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
        java.awt.GridBagConstraints gridBagConstraints1;
        
        setTitle("SVM Sample and Process Selection");
        setBackground(java.awt.Color.lightGray);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
        jPanel1.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED), "Evaluation Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14)));
        jPanel1.setBackground(java.awt.Color.white);
        runGenes.setToolTipText("");
        runGenes.setSelected(true);
        runGenes.setText("Classify Genes");
        runGenes.setBackground(java.awt.Color.white);
        sampleSelection.add(runGenes);
        runGenes.setFocusPainted(false);
        jPanel1.add(runGenes);
        
        runExperiments.setText("Classify Experiments");
        runExperiments.setBackground(java.awt.Color.white);
        sampleSelection.add(runExperiments);
        runExperiments.setFocusPainted(false);
        jPanel1.add(runExperiments);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(4, 0, 0, 0);
        getContentPane().add(jPanel1, gridBagConstraints1);
        
        jPanel2.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        jPanel2.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED), "SVM Process Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14)));
        jPanel2.setBackground(java.awt.Color.white);
        trainAndClassifyButton.setToolTipText("Train SMV then immediately classify");
        trainAndClassifyButton.setSelected(true);
        trainAndClassifyButton.setText("Train SVM then Classify");
        trainAndClassifyButton.setBackground(java.awt.Color.white);
        processSelection.add(trainAndClassifyButton);
        trainAndClassifyButton.setFocusPainted(false);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanel2.add(trainAndClassifyButton, gridBagConstraints2);
        
        trainOnlyButton.setToolTipText("Train SVM only... output are result SVM weights");
        trainOnlyButton.setText("Train SVM (skip classify)");
        trainOnlyButton.setBackground(java.awt.Color.white);
        processSelection.add(trainOnlyButton);
        trainOnlyButton.setFocusPainted(false);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanel2.add(trainOnlyButton, gridBagConstraints2);
        
        classifyOnlyButton.setToolTipText("Trains current data using and SVM file of weights");
        classifyOnlyButton.setText("Classify using existing SVM file");
        classifyOnlyButton.setBackground(java.awt.Color.white);
        processSelection.add(classifyOnlyButton);
        classifyOnlyButton.setFocusPainted(false);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 2;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanel2.add(classifyOnlyButton, gridBagConstraints2);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel2, gridBagConstraints1);
        
        continueButton.setText("Continue");
        jPanel4.add(continueButton);
        
        cancelButton.setText("Cancel");
        jPanel4.add(cancelButton);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel4, gridBagConstraints1);
        
        jPanel3.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Hierarchical Clustering", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14)));
        jPanel3.setBackground(java.awt.Color.white);
        jPanel3.setPreferredSize(new java.awt.Dimension(310, 55));
        jPanel3.setMinimumSize(new java.awt.Dimension(310, 55));
        calcHCLCheckBox.setText("Calculate Hierarchical Clustering on SVM Results");
        calcHCLCheckBox.setBackground(java.awt.Color.white);
        calcHCLCheckBox.setBorder(new javax.swing.border.CompoundBorder());
        calcHCLCheckBox.setFocusPainted(false);
        jPanel3.add(calcHCLCheckBox);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel3, gridBagConstraints1);
        
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
    private javax.swing.ButtonGroup sampleSelection;
    private javax.swing.ButtonGroup processSelection;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton runGenes;
    private javax.swing.JRadioButton runExperiments;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton trainAndClassifyButton;
    private javax.swing.JRadioButton trainOnlyButton;
    private javax.swing.JRadioButton classifyOnlyButton;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton continueButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JCheckBox calcHCLCheckBox;
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

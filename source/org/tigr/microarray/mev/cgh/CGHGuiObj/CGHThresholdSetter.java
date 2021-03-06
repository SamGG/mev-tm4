/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CGHThresholdSetter.java
 *
 * Created on March 27, 2003, 10:58 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;


/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHThresholdSetter extends javax.swing.JDialog {
    private int result;
    float ampThresh, delThresh, ampThresh2Copy, delThresh2Copy;


    /** Creates new form CGHThresholdSetter */
    public CGHThresholdSetter(java.awt.Frame parent, float ampThresh, float delThresh, float ampThresh2Copy, float delThresh2Copy) {
        super(parent, true);

        initComponents();

        txtDel.setText(delThresh + "");
        txtAmp.setText(ampThresh + "");
        txtAmp2Copy.setText(ampThresh2Copy + "");
        txtDel2Copy.setText(delThresh2Copy + "");

        btnOK.addActionListener(new Listener());
        btnOK.setActionCommand("ok-command");

        btnCancel.addActionListener(new Listener());
        btnCancel.setActionCommand("cancel-command");

        setSize(700, 100);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        pnlThresholds = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtAmp = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtDel = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtDel2Copy = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtAmp2Copy = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        pnlThresholds.setBackground(java.awt.Color.white);
        jLabel1.setText("Amp Thresh");
        pnlThresholds.add(jLabel1);

        txtAmp.setPreferredSize(new java.awt.Dimension(60, 20));
        pnlThresholds.add(txtAmp);

        jLabel2.setText("Del Thresh");
        pnlThresholds.add(jLabel2);

        txtDel.setPreferredSize(new java.awt.Dimension(60, 20));
        pnlThresholds.add(txtDel);

        jLabel3.setText("Del Thresh 2 Copy");
        pnlThresholds.add(jLabel3);

        txtDel2Copy.setPreferredSize(new java.awt.Dimension(60, 20));
        pnlThresholds.add(txtDel2Copy);

        jLabel4.setText("Amp Thresh 2 Copy");
        pnlThresholds.add(jLabel4);

        txtAmp2Copy.setPreferredSize(new java.awt.Dimension(60, 20));
        pnlThresholds.add(txtAmp2Copy);

        getContentPane().add(pnlThresholds, java.awt.BorderLayout.CENTER);

        btnOK.setText("OK");
        jPanel2.add(btnOK);

        btnCancel.setText("Cancel");
        jPanel2.add(btnCancel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }//GEN-END:initComponents

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    public int showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return result;
    }

    /** Getter for property ampThresh.
     * @return Value of property ampThresh.
     */
    public float getAmpThresh() {
        return ampThresh;
    }

    /** Setter for property ampThresh.
     * @param ampThresh New value of property ampThresh.
     */
    public void setAmpThresh(float ampThresh) {
        this.ampThresh = ampThresh;
    }

    /** Getter for property delThresh.
     * @return Value of property delThresh.
     */
    public float getDelThresh() {
        return delThresh;
    }

    /** Setter for property delThresh.
     * @param delThresh New value of property delThresh.
     */
    public void setDelThresh(float delThresh) {
        this.delThresh = delThresh;
    }

    /** Getter for property ampThresh2Copy.
     * @return Value of property ampThresh2Copy.
     */
    public float getAmpThresh2Copy() {
        return ampThresh2Copy;
    }

    /** Setter for property ampThresh2Copy.
     * @param ampThresh2Copy New value of property ampThresh2Copy.
     */
    public void setAmpThresh2Copy(float ampThresh2Copy) {
        this.ampThresh2Copy = ampThresh2Copy;
    }

    /** Getter for property delThresh2Copy.
     * @return Value of property delThresh2Copy.
     */
    public float getDelThresh2Copy() {
        return delThresh2Copy;
    }

    /** Setter for property delThresh2Copy.
     * @param delThresh2Copy New value of property delThresh2Copy.
     */
    public void setDelThresh2Copy(float delThresh2Copy) {
        this.delThresh2Copy = delThresh2Copy;
    }

    private class Listener extends WindowAdapter implements ActionListener {

	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    if (command.equals("ok-command")) {
		try {

                    ampThresh = Float.parseFloat(txtAmp.getText());
                    delThresh = Float.parseFloat(txtDel.getText());
                    ampThresh2Copy = Float.parseFloat(txtAmp2Copy.getText());
                    delThresh2Copy = Float.parseFloat(txtDel2Copy.getText());

		    result = JOptionPane.OK_OPTION;
		} catch (Exception e) {
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField txtDel;
    private javax.swing.JTextField txtAmp2Copy;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel pnlThresholds;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField txtDel2Copy;
    private javax.swing.JTextField txtAmp;
    private javax.swing.JButton btnOK;
    private javax.swing.JButton btnCancel;
    // End of variables declaration//GEN-END:variables

}

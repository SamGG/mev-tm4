/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ModalDialog.java
 *
 * Created on November 22, 2003, 4:14 AM
 */

package org.tigr.microarray.util.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 *
 * @author  Adam Margolin
 */
public class ModalDialog extends javax.swing.JDialog {
    private int result;
    
    /** Creates new form ModalDialog */
    public ModalDialog(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
        
	Listener listener = new Listener();
	JPanel btnsPanel = createBtnsPanel(listener);
	
	Container content = getContentPane();
        
        /*
	content.setLayout(new GridBagLayout());
	
	content.add(btnsPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
	,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 8, 4, 8), 0, 0));
         */
        content.setLayout(new BorderLayout());
        content.add(btnsPanel, BorderLayout.SOUTH);
        
	addWindowListener(listener);
	pack();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
        pack();
    }//GEN-END:initComponents
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * Shows the dialog.
     */
    public int showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return result;
    }
    
    /**
     * Creates a panel with 'OK' and 'Cancel' buttons.
     */
    private JPanel createBtnsPanel(ActionListener listener) {
	GridLayout gridLayout = new GridLayout();
	JPanel panel = new JPanel(gridLayout);
	
	JButton okButton = new JButton("OK");
	okButton.setActionCommand("ok-command");
	okButton.addActionListener(listener);
	panel.add(okButton);
	
	JButton cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand("cancel-command");
	cancelButton.addActionListener(listener);
	gridLayout.setHgap(4);
	panel.add(cancelButton);
	
	getRootPane().setDefaultButton(okButton);
	
	return panel;
    }
    
    /**
     * Listener to listen to window and action events.
     */
    private class Listener extends WindowAdapter implements ActionListener {
	
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command.equals("ok-command")) {
		result = JOptionPane.OK_OPTION;
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
    // End of variables declaration//GEN-END:variables
    
}
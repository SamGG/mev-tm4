/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * NewickFileOutputDialog.java
 *
 * Created on January 7, 2005, 10:37 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
/**
 *
 * @author  braisted
 */
public class NewickFileOutputDialog extends AlgorithmDialog {

    private int result = JOptionPane.CANCEL_OPTION;    
    private boolean isGeneTree = false;
        
    private JComboBox annBox;
    private JTextField fileField;
    //private JCheckBox zeroRootBox;
    
    /** Creates a new instance of NewickFileOutputDialog */
    public NewickFileOutputDialog(Frame parent, String[] annotationKeys, int orientation, String fileType) {
        super(parent, fileType+" File Output Dialog", true);

        if(orientation == HCLTree.HORIZONTAL)
            isGeneTree = true;
        
        JLabel annSelectionLabel;
        if(isGeneTree) {
            annSelectionLabel = new JLabel("Select a Gene Label:");
        } else {
            annSelectionLabel = new JLabel("Select a Sample Label:");                      
        }
                       
        annBox = new JComboBox(annotationKeys);
        annBox.setSelectedIndex(0);
       
        //zeroRootBox = new JCheckBox("Include 0.0 Height Root", false);
        //zeroRootBox.setOpaque(false);
        //zeroRootBox.setFocusPainted(false);
        
        JLabel fileLabel = new JLabel("Output File:");
        fileField = new JTextField(50);
        fileField.setMargin(new Insets(0,3,0,3));
        fileField.setHorizontalAlignment(JTextField.TRAILING);
        fileField.setText( TMEV.getFile("data").getAbsolutePath() + System.getProperty("file.separator") + "newick_output.txt"); 

        JButton browseButton = new JButton("Select Output File");
        browseButton.setActionCommand("browse-command");
        browseButton.setFocusPainted(false);
        
        ParameterPanel panel = new ParameterPanel("Output Parameters");
        panel.setLayout(new GridBagLayout());
        
        panel.add(annSelectionLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,20,5,0), 0, 0));
        panel.add(annBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,30,0), 0, 0));
        //panel.add(zeroRootBox, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,30,0), 0, 0));
        panel.add(fileLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,5,0), 0, 0));
        panel.add(fileField, new GridBagConstraints(0,3,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,20,0), 0, 0));
        panel.add(browseButton, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,20,20), 0, 0));
        
        addContent(panel);

        Listener listener = new Listener();
        browseButton.addActionListener(listener);        
        
        setActionListeners(listener);

        setSize(470, 300);
    }
        
        
    /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
        
    private boolean validateOutputFile() {
        File outFile = new File(this.fileField.getText());
        File parent = outFile.getParentFile();
        return (parent.exists() && parent.isDirectory());
    }
    
    private void resetControls() {
        this.annBox.setSelectedIndex(0);
        //this.zeroRootBox.setSelected(false);
        this.fileField.setText(TMEV.getFile("data").getAbsolutePath() + System.getProperty("file.separator") + "newick_output.txt");
    }
    
    public File getOutputFile() {
        return new File(this.fileField.getText());
    }
   
    public String getAnnotationKey() {
        return (String)this.annBox.getSelectedItem();
    }
    
    public static void main(String [] args) {
        String [] names = new String[3];
        names[0] = "TC";
        names[1] = "GenBank";
        names[2] = "Putative Role (Guess)";
        NewickFileOutputDialog d = new NewickFileOutputDialog(new Frame(), names, HCLTree.HORIZONTAL,  "Newick");
        d.showModal();
    }
    
        /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                //validate file
                if(!validateOutputFile()) {
                    JOptionPane.showMessageDialog(NewickFileOutputDialog.this, "The directory specified for file output does not exist.<BR>Please select again.", "Newick Output File Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
            else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            }            
            else if (command.equals("info-command")){
            	HelpWindow.launchBrowser(NewickFileOutputDialog.this, "Newick File Output Dialog");
            }
            else if (command.equals("browse-command")) {
            	String dataPath = TMEV.getDataPath();
                File fileLoc = TMEV.getFile("data/"); 
                // if the data path is null go to default, if not null and not exist then to to default
                // else use the dataPath
                if(dataPath != null) {
                    fileLoc = new File(dataPath);
                    if(!fileLoc.exists()) {
                        fileLoc = TMEV.getFile("data/");
                    }
                }
                JFileChooser chooser = new JFileChooser(fileLoc);
                chooser.setFileSelectionMode(javax.swing.DefaultListSelectionModel.SINGLE_SELECTION);
                if(chooser.showSaveDialog(NewickFileOutputDialog.this) == JFileChooser.APPROVE_OPTION) {
                    fileField.setText(chooser.getSelectedFile().getAbsolutePath());
                    NewickFileOutputDialog.this.validate();
                    //NewickFileOutputDialog.this.pack();
                }
                return;
            }
            dispose();
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
}

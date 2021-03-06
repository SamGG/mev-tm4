/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ScriptAlgorithmInitDialog.java
 *
 * Created on March 4, 2004, 10:56 AM
 */

package org.tigr.microarray.mev.script.scriptGUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.action.ActionManager;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.script.util.ScriptConstants;

/** The ScriptAlgorithmInitDialog permits algorithm selection during script construction.
 * Three broad scripting algorithm categories exist: analyis (clustering, stat, and classification),
 * adjustment algorithms (filters, tranformations), and cluster selections algorithms.
 * @author braisted
 */
public class ScriptAlgorithmInitDialog extends AlgorithmDialog {
    
    /** result
     */    
    private int result;    
    /** algorithm index
     */    
    private int algorithmIndex = -1;    
    /** algorithm type
     */    
    private String algorithmType;
    /** current selected algorithm name
     */    
    private String currentAlgorithmName;    
    /** main panel for components
     */    
    private JPanel mainPanel;
    /** action manager to accumulate available mev algorithms for presentation
     */    
    private ActionManager actionManager;    
    /** previews the selections
     */    
    private PreviewPanel previewPanel;
    /** analysis algorithm selection controls
     */    
    private AlgorithmSelectionPanel algSelPanel;
    /** adjustment algorithm controls
     */    
    private AdjustmentSelectionPanel adjSelPanel;
    /** cluster selection parameter controls
     */    
    private ClusterSelectionPanel clusterSelectionPanel;
    
        /*
         * CLASS DEVELOPMENT NOTES
         *
         * The main panel will hold all content except super class elements.
         * Three tabbed panes will correspond to three algorithm types,
         * <cluster | data adjustment | normalization | cluster selection>
         * Cluster selection applys to multicluster result only.
         * Other type can apply to any result.
         *
         * Internal classes handle representation of algorithm types.
         * Selection type and specific information is displayed in top preview panel.
         *
         * The main return values will be a type string and associated parameters
         * in the case of cluster selection criteria.
         */
    
    
    /** Creates a new instance of ScriptAlgorithmInitDialog
     * @param manager ActionManager to provide available algorithms
     * @param nodeType Node type, data node type can restrict algorithm selection.
     * @param isAffyData Indicates if loaded data is for Affymetrix(TM) data, need more filter controls.
     */
    public ScriptAlgorithmInitDialog(ActionManager manager, String nodeType, boolean isAffyData) {
        super(new JFrame(), "Script Algorithm Initialization Dialog", true);
        this.actionManager = manager;
        algorithmType = ScriptConstants.ALGORITHM_TYPE_CLUSTER;;//default
        result = JOptionPane.CANCEL_OPTION;
        
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.white);
        
        previewPanel = new PreviewPanel();
        algSelPanel = new AlgorithmSelectionPanel(actionManager);
        adjSelPanel = new AdjustmentSelectionPanel(isAffyData);
        clusterSelectionPanel = new ClusterSelectionPanel();
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Analysis Algorithms", algSelPanel);
        tabbedPane.addTab("Adjustment Algorithms", adjSelPanel);
        tabbedPane.addTab("Cluster Selection Algorithms", clusterSelectionPanel);
        
        if(nodeType.equals(ScriptConstants.OUTPUT_DATA_CLASS_GENE_MULTICLUSTER_OUTPUT)
        || nodeType.equals(ScriptConstants.OUTPUT_DATA_CLASS_EXPERIMENT_MULTICLUSTER_OUTPUT)
        || nodeType.equals(ScriptConstants.OUTPUT_DATA_CLASS_MULTICLUSTER_OUTPUT)) {
            tabbedPane.setEnabledAt(0, false);
            tabbedPane.setEnabledAt(1, false);
            tabbedPane.setSelectedIndex(2);
        } else {
            tabbedPane.setEnabledAt(2, false);
        }
        
        mainPanel.add(previewPanel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        mainPanel.add(tabbedPane, new GridBagConstraints(0,1,1,2,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        
        Listener listener = new Listener();
        addWindowListener(listener);
        addContent(mainPanel);
        setActionListeners(listener);
        pack();
        if(!isAffyData)
            setSize(new Dimension(510, 587));
        else
            setSize(new Dimension(510, 665));
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
    
    /** Resets the controls
     */    
    public void onReset() {
        this.previewPanel.reset();
        this.algSelPanel.reset();
        this.adjSelPanel.reset();
        this.clusterSelectionPanel.reset();
        this.algorithmIndex = -1;
        this.algorithmType  = "";
        this.currentAlgorithmName = "";
    }
    
    
    /** returns the algorithm class
     */    
    public String getAlgorithmType() {
        return algorithmType;
    }
    
    /** returns algorithm name
     */    
    public String getAlgorithmName() {
        return currentAlgorithmName;
    }
    
    /** returns the algorthms index
     */    
    public int getAlgorithmIndex() {
        int index;
        if(currentAlgorithmName != null) {
            String number = (String)(algSelPanel.indexHash.get(currentAlgorithmName));
            if(number != null) {
                return Integer.parseInt(number);
            }
        }
        
        return -1;
        
    }
    
    
    
    
    /*
     *
     * Internal Classes
     *
     */
    
    
    /* Preview Panel simply reports the current selections and key information
     * such as algorithm category and algorithm name.
     */
    
    /** Previews the algorithm selection
     */    
    private class PreviewPanel extends ParameterPanel {
        
        JTextField categoryValueField;
        JTextField algValueField;
        
        public PreviewPanel() {
            super("Selection Preview");
            super.setLayout(new GridBagLayout());
            JLabel categoryLabel = new JLabel("Algorithm Category: ");
            categoryValueField = new JTextField("Not Selected", 25);
            categoryValueField.setEditable(false);
            
            JLabel algNameLabel = new JLabel("Algorithm: ");
            algValueField = new JTextField("Not Selected", 25);
            algValueField.setEditable(false);
            
            add(categoryLabel, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,20,0,0), 0,0));
            add(categoryValueField, new GridBagConstraints(1,0,1,1,0,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,0,0), 0,0));
            add(algNameLabel, new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,20,5,0), 0,0));
            add(algValueField, new GridBagConstraints(1,1,1,1,0,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,0), 0,0));
        }
        
        public void reset() {
            algValueField.setText("Not Selected");
            categoryValueField.setText("Not Selected");
        }
        
        public void setValues(String cat, String name) {
            algValueField.setText(name);
            categoryValueField.setText(cat);
        }
    }
    
    /* AlgorithmSelectinoPanel displays all clustering, stat and classification
     * algorithms by retrieving them from the action manager.
     * The buttons presented have a simple listener to report the algorithm name.
     *
     * Button arrangemet is to match rows and columns in number with prefference to
     * extra rows if needed.
     */
    
    /** Supports analysis algorithm selection.
     */    
    private class AlgorithmSelectionPanel extends ParameterPanel {
        
        ButtonPanel buttonPanel;
        ButtonListener listener;
        Hashtable descriptions;
        //Hashtable locations;
        Hashtable indexHash;
        
        AlgorithmSelectionPanel(ActionManager actionManager) {
            super("Algorithm Selection");
            setLayout(new GridBagLayout());
            setBackground(Color.white);
            listener = new ButtonListener();
            buttonPanel = new ButtonPanel();
            buttonPanel.setLayout(new GridBagLayout());
            buttonPanel.setBackground(Color.white);
            descriptions = new Hashtable();
            indexHash = new Hashtable();
            // locations = new Hashtable();
            addAlgorithmButtons(actionManager);
            add(buttonPanel, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        }
        
        
        private void addAlgorithmButtons(ActionManager manager) {
            int algCnt = 0, x, y;
            Action action;
            
            while ((action = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(algCnt)))!=null)
                algCnt++;
            
            x = 1;
            y = algCnt;
            
            while(true){
                x++;
                y = (int)Math.floor(algCnt/x);
                y += x*y < algCnt ? 1 : 0;
                if(y<=x)
                    break;
            }
            
            algCnt = 0;
            JButton button;
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4,4,4,4);
            int cnt = 0;
            for(int i = 0; i < x; i++){
                for(int j = 0; j < y; j++){
                    action = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(cnt));
                    if(action != null) {
                        button = new JButton();
                        button.setFocusPainted(false);
                        button.setActionCommand((String)action.getValue(Action.NAME));
                        button.addActionListener(listener);
                        button.setIcon((Icon)action.getValue(ActionManager.LARGE_ICON));
                        button.setText("");
                        button.setToolTipText((String)action.getValue(Action.SHORT_DESCRIPTION));
                        button.setPreferredSize(new Dimension(45,45));
                        button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                        gbc.gridx = j;
                        gbc.gridy = i;
                        buttonPanel.add(button, gbc);
                        descriptions.put(action.getValue(Action.NAME), action.getValue(Action.SHORT_DESCRIPTION));
                        indexHash.put(action.getValue(Action.NAME), String.valueOf(cnt));
                        try {
                        	ClassLoader cl = Thread.currentThread().getContextClassLoader();
                            Class clazz = Class.forName(((String)action.getValue(ActionManager.PARAMETER)), true, cl);
                            IClusterGUI gui = (IClusterGUI)clazz.newInstance();
                            button.setEnabled(gui instanceof IScriptGUI);
                        } catch (Exception e ) {  }
                        // locations.put(button, new Point(button.getLocation()));
                    }
                    cnt++;
                }
            }
        }
        
        public void reset() {
            buttonPanel.setSelected(false);
            buttonPanel.repaint();
        }
        
        /**
         */        
        private class ButtonPanel extends JPanel {
            boolean isSelected;
            Point selectedSector;
            JButton selectedButton;
            
            public ButtonPanel() {
                super();
                isSelected = false;
            }
            
            public void setSelection(JButton button) {
                isSelected = true;
                selectedButton = button;
                repaint();
            }
            
            public void setSelected(boolean value) {
                isSelected = value;
            }
            
            public void paint(Graphics g) {
                super.paint(g);
                if(isSelected && selectedButton != null) {
                    selectedSector = selectedButton.getLocation();
                    g.setColor(Color.blue);
                    g.drawRect(selectedSector.x-5, selectedSector.y-5, 54, 54);
                    g.drawRect(selectedSector.x-4, selectedSector.y-4, 52, 52);
                }
            }
        }
        
        // Sets current algorithm
        private class ButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent ae) {
                String algorithmName = ae.getActionCommand();
                currentAlgorithmName = algorithmName;
                Object button = ae.getSource();
                
                if(button != null && button instanceof JButton) {
                    buttonPanel.setSelection((JButton)button);
                    algorithmType = ScriptConstants.ALGORITHM_TYPE_CLUSTER;
                    previewPanel.setValues("Analysis Algorithm", algorithmName+": "+((String)descriptions.get(algorithmName)));
                }
            }
        }
        
    }
    
    
    
    
    /** Support selection of adjustment algorithm
     */    
    private class AdjustmentSelectionPanel extends ParameterPanel {
        //Filters (affymetrics as well detection filter and ....)
        //Gene Based transformations
        //NormSpots /RMS /SD MeanCenter DigSpots
        //Experiments (as above)
        //intensity adjustments
        
        ButtonGroup bg;
        JCheckBox noneBox;
        
        public AdjustmentSelectionPanel(boolean isAffyData) {
            super("Adjustment Selection");
            setLayout(new GridBagLayout());
            AdjustmentBoxListener listener = new AdjustmentBoxListener();
            
            bg  = new ButtonGroup();
            noneBox = new JCheckBox("none", true);  //used for reset() method
            bg.add(noneBox);
            
            //Filter Panel
            ParameterPanel filterPanel = new ParameterPanel("Gene/Row Filters");
            filterPanel.setLayout(new GridBagLayout());
            JCheckBox percBox = createCheckBox("Percentage Cutoff", "Requires x% valid expression values to retain a gene", listener);
            JCheckBox lowerBox = createCheckBox("Lower Cutoffs", "Cy3 and Cy5 must have a minium value \n to retain gene. (see info page)", listener);
            filterPanel.add(percBox, new GridBagConstraints(0,0,1,1,0,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(5,0,10,25), 0,0));
            filterPanel.add(lowerBox, new GridBagConstraints(1,0,1,1,0,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(5,25,10,0), 0,0));
            
            //Gene Panel
            ParameterPanel genePanel = new ParameterPanel("Gene/Row Based Adjustments");
            genePanel.setLayout(new GridBagLayout());
            
            JCheckBox gNormBox = createCheckBox("Normalize Genes/Rows","Adjust Vector ||v||=1", listener);
            JCheckBox gRMSBox = createCheckBox("Divide Genes/Rows by RMS","Divide values by spot's RMS", listener);
            JCheckBox gSDBox = createCheckBox("Divide Genes/Rows by SD","Divide values by spot's SD", listener);
            JCheckBox gMCBox = createCheckBox("Mean Center Genes/Rows","Divide values by spot's Mean", listener);
            JCheckBox gMedCBox = createCheckBox("Median Center Genes/Rows","Divide values by spot's Median", listener);
            JCheckBox gDigBox = createCheckBox("Digital Genes/Rows","Bins spot's values into log2(#Spots) int value bins ", listener);
            
            genePanel.add(gNormBox, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(5,10,10,0), 0,0));
            genePanel.add(gRMSBox, new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,10,10,0), 0,0));
            genePanel.add(gSDBox, new GridBagConstraints(0,2,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,10,10,0), 0,0));
            genePanel.add(gMCBox, new GridBagConstraints(0,3,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,10,10,0), 0,0));
            genePanel.add(gMedCBox, new GridBagConstraints(0,4,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,10,10,0), 0,0));
            genePanel.add(gDigBox, new GridBagConstraints(0,5,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,10,10,0), 0,0));
            
            //Experiment Panel
            ParameterPanel expPanel = new ParameterPanel("Sample/Column Based Adjustments");
            expPanel.setLayout(new GridBagLayout());
            
            JCheckBox eNormBox = createCheckBox("Normalize Samples/Columns","Adjust Vector ||v||=1", listener);
            JCheckBox eRMSBox = createCheckBox("Divide Samples/Columns by RMS","Divide values by sample's RMS", listener);
            JCheckBox eSDBox = createCheckBox("Divide Samples/Columns by SD","Divide values by sample's SD", listener);
            JCheckBox eMCBox = createCheckBox("Mean Center Samples/Columns","Divide values by sample's Mean", listener);
            JCheckBox eMedCBox = createCheckBox("Median Center Samples/Columns","Divide values by sample's Median", listener);
            JCheckBox eDigBox = createCheckBox("Digital Samples/Columns","Bins values into log2(#Exps) int value bins ", listener);
            
            expPanel.add(eNormBox, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(5,10,10,0), 0,0));
            expPanel.add(eRMSBox, new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,10,10,0), 0,0));
            expPanel.add(eSDBox, new GridBagConstraints(0,2,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,10,10,0), 0,0));
            expPanel.add(eMCBox, new GridBagConstraints(0,3,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,10,10,0), 0,0));
            expPanel.add(eMedCBox, new GridBagConstraints(0,4,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,10,10,0), 0,0));
            expPanel.add(eDigBox, new GridBagConstraints(0,5,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,10,10,0), 0,0));
            
            //Policy Panel  ** This is a global policy not an adjustment
            //ParameterPanel polPanel = new ParameterPanel("Experiment Based Adjustments");
            //polPanel.setLayout(new GridBagLayout());
            
            //Log Transformations
            // Do we want or need these, applies log two transformation to FloatMatrix which is in log 2

            add(filterPanel, new GridBagConstraints(0,0,2,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,10,0), 0,0));
            add(genePanel, new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,10,0), 0,0));
            add(expPanel, new GridBagConstraints(1,1,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,10,0), 0,0));
            if(isAffyData) {
                //affy based Detection and Fold filters
                ParameterPanel affyPanel = new ParameterPanel("Affimetrix Filtering Options");
                affyPanel.setLayout(new GridBagLayout());
                JCheckBox affyDetectionBox = createCheckBox("Detection Filter", "Filters by detection flag.",listener);
                JCheckBox affyFoldBox = createCheckBox("Fold Filter", "Filters by fold change.",listener);
                affyPanel.add(affyDetectionBox, new GridBagConstraints(0,0,1,1,0,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(5,0,10,25), 0,0));
                affyPanel.add(affyFoldBox, new GridBagConstraints(1,0,1,1,0,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(5,25,10,0), 0,0));
                           
                add(affyPanel, new GridBagConstraints(0,2,2,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,10,0), 0,0));                
            }
        }
        

        private JCheckBox createCheckBox(String label, String toolTip, ActionListener listener) {
            JCheckBox box = new JCheckBox(label);
            box.setBackground(Color.white);
            box.setFocusPainted(false);
            box.setActionCommand(label);
            box.setToolTipText(toolTip);
            box.addActionListener(listener);
            bg.add(box);
            return box;
        }
        
        public void reset() {
            noneBox.setSelected(true);
        }
        
        // Sets current algorithm
        private class AdjustmentBoxListener implements ActionListener {
            public void actionPerformed(ActionEvent ae) {
                String algorithmName = ae.getActionCommand();
                currentAlgorithmName = algorithmName;
                previewPanel.setValues("Adjustment Algorithm", algorithmName);
                algorithmType = ScriptConstants.ALGORITHM_TYPE_ADJUSTMENT;
            }
        }
        
    }
    
    private class NormalizationSelectionPanel extends JPanel {
        //
        
    }
    
    /** Supports selection of cluster selection algorithms
     */    
    private class ClusterSelectionPanel extends JPanel {
        JRadioButton diversityButton;
        JRadioButton centroidEntropyButton;
        JRadioButton noButton;
        
        public ClusterSelectionPanel() {
            super();
            setLayout(new GridBagLayout());
            ButtonListener listener = new ButtonListener();
            
            ParameterPanel panel = new ParameterPanel();
            panel.setLayout(new GridBagLayout());
            
            diversityButton = new JRadioButton("Diversity Ranking Cluster Selection", false);
            diversityButton.setFocusPainted(false);
            diversityButton.setOpaque(false);
            diversityButton.addActionListener(listener);

            centroidEntropyButton = new JRadioButton("Centroid Variance or Entropy Cluster Selection", false);
            centroidEntropyButton.setFocusPainted(false);
            centroidEntropyButton.setOpaque(false);            
            centroidEntropyButton.addActionListener(listener);
                        
            noButton = new JRadioButton("hidden", true);
            ButtonGroup bg = new ButtonGroup();
            bg.add(diversityButton);
            bg.add(centroidEntropyButton);
            bg.add(noButton);
            
            panel.add(centroidEntropyButton, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0), 0,0));
            panel.add(diversityButton, new GridBagConstraints(0,1,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,0,0,0), 0,0));
        
            add(panel, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));            
        }
        
        public void reset() {
            noButton.setSelected(true);
        }
        
        // Sets current algorithm
        private class ButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent ae) {
                if(diversityButton.isSelected()) {
                    previewPanel.setValues("Cluster Selection", "Diversity Ranking Cluster Selection");
                    algorithmType = ScriptConstants.ALGORITHM_TYPE_CLUSTER_SELECTION;
                    currentAlgorithmName = "Diversity Ranking Cluster Selection";
                } else if(centroidEntropyButton.isSelected()) {
                    previewPanel.setValues("Cluster Selection", "Centroid Entropy Ranking Cluster Selection");
                    algorithmType = ScriptConstants.ALGORITHM_TYPE_CLUSTER_SELECTION;
                    currentAlgorithmName = "Centroid Entropy/Variance Ranking Cluster Selection";                    
                }
            }
        }
    }
    
    
    
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == okButton) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (source == cancelButton) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
            else if (source == resetButton) {
                onReset();
            }
            else if (source == infoButton){
            	HelpWindow.launchBrowser(ScriptAlgorithmInitDialog.this, "Script Algorithm Initialization Dialog");
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
}

/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: MultipleArrayViewer.java,v $
 * $Revision: 1.20 $
 * $Date: 2004-07-27 19:56:10 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.io.File;

import java.awt.Frame;
import java.awt.Color;
import java.awt.Point;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import java.text.DateFormat;

import javax.swing.Action;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import javax.media.jai.JAI;
import com.sun.media.jai.codec.ImageEncodeParam;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.util.SlideDataSorter;
import org.tigr.microarray.util.awt.SetElementSizeDialog;
import org.tigr.microarray.util.awt.SetSlideFilenameDialog;

import org.tigr.util.swing.ImageFileFilter;
import org.tigr.util.swing.BMPFileFilter;
import org.tigr.util.swing.JPGFileFilter;
import org.tigr.util.swing.PNGFileFilter;
import org.tigr.util.swing.TIFFFileFilter;
import org.tigr.microarray.util.swing.ExpressionFileView;
import org.tigr.microarray.util.swing.ExpressionFileFilter;
import org.tigr.microarray.util.awt.ColorSchemeSelectionDialog;

import org.tigr.microarray.mev.SetLowerCutoffsDialog;
import org.tigr.microarray.mev.SetPercentageCutoffsDialog;

import org.tigr.microarray.mev.action.ActionManager;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;

import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.impl.st.HCLSupportTree; //Temporary: see onShowSupportTreeLegend()

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;

import org.tigr.microarray.mev.file.ExpressionFileLoader;

import org.tigr.microarray.mev.cluster.clusterUtil.*;
import org.tigr.microarray.mev.file.SuperExpressionFileLoader;
import org.tigr.microarray.mev.script.ScriptManager;

public class MultipleArrayViewer extends ArrayViewer implements Printable {
   public static final long serialVersionUID = 100010201010001L;    
    
    private MultipleArrayMenubar menubar;
    private MultipleArrayToolbar toolbar;
    private JSplitPane splitPane;
    private JScrollPane viewScrollPane;
    private JLabel statusLabel;
    // the tree and special nodes and scroll pane
    private JScrollPane treeScrollPane;
    private ResultTree tree;
    private DefaultMutableTreeNode clusterNode;
    private DefaultMutableTreeNode analysisNode;
    private DefaultMutableTreeNode scriptNode;
    private DefaultMutableTreeNode historyNode;
    // current viewer
    private IViewer viewer;
    // callback reference
    private IFramework framework = new FrameworkImpl();
    // features data
    private MultipleArrayData data = new MultipleArrayData();
    //Action Manager
    private ActionManager manager;
    
    private int resultCount = 1;
    
    private ClusterRepository geneClusterRepository;
    private ClusterRepository experimentClusterRepository;
    private ClusterTable geneClusterManager;
    private ClusterTable experimentClusterManager;
    private ScriptManager scriptManager;
    private HistoryViewer historyLog;
    
    private File currentAnalysisFile;
    private boolean modifiedResult = false;
    
    
    /**
     * Construct a <code>MultipleArrayViewer</code> with default title,
     * creates menu and tool bars from new instance of action manager,
     * creates the navigation tree and the scroll pane to be used to display
     * a calculation result, creates a status bar.
     */
    public MultipleArrayViewer() {
        super(new JFrame("TIGR Multiple Array Viewer"));
        
        // listener
        EventListener eventListener = new EventListener();
        mainframe.addWindowListener(eventListener);
        manager = new ActionManager(eventListener, TMEV.getFieldNames(), TMEV.getGUIFactory());
        
        menubar = new MultipleArrayMenubar(manager);
        mainframe.setJMenuBar(menubar);
        
        toolbar = new MultipleArrayToolbar(manager);
        mainframe.getContentPane().add(toolbar, BorderLayout.NORTH);
        
        viewScrollPane = createViewScrollPane(eventListener);
        viewScrollPane.setBackground(Color.white);
        
        treeScrollPane = createTreeScrollPane(eventListener);
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, viewScrollPane);
        splitPane.setOneTouchExpandable(true);
        mainframe.getContentPane().add(splitPane, BorderLayout.CENTER);
        
        statusLabel = new JLabel("TIGR MultiExperiment Viewer");
        mainframe.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        mainframe.pack();
        splitPane.setDividerLocation(.3);
        
        systemDisable(TMEV.DB_AVAILABLE);
        systemDisable(TMEV.DATA_AVAILABLE);
    }
    
    
    /**
     * Construct a <code>MultipleArrayViewer</code> with default title,
     * creates menu and tool bars from new instance of action manager,
     * creates the navigation tree and the scroll pane to be used to display
     * a calculation result, creates a status bar.
     */
    public MultipleArrayViewer(MultipleArrayData arrayData) {
        super(new JFrame("TIGR Multiple Array Viewer"));
        
        // listener
        EventListener eventListener = new EventListener();
        mainframe.addWindowListener(eventListener);
        manager = new ActionManager(eventListener, TMEV.getFieldNames(), TMEV.getGUIFactory());
        
        data = arrayData;
        
        menubar = new MultipleArrayMenubar(manager);
        
        //have new session but need to build field names into menus
        menubar.addLabelMenuItems(TMEV.getFieldNames());
        menubar.addSortMenuItems(TMEV.getFieldNames());

        //need to populate the experiment label menu items
        menubar.addExperimentLabelMenuItems(arrayData.getSlideNameKeyVectorUnion());
        mainframe.setJMenuBar(menubar);
        
        toolbar = new MultipleArrayToolbar(manager);
        mainframe.getContentPane().add(toolbar, BorderLayout.NORTH);
        
        viewScrollPane = createViewScrollPane(eventListener);
        viewScrollPane.setBackground(Color.white);
        
        treeScrollPane = createTreeScrollPane(eventListener);
        
        //Add the time stamp node
        Date date = new Date(System.currentTimeMillis());
        DateFormat format = DateFormat.getDateTimeInstance();
        format.setTimeZone(TimeZone.getDefault());
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(format.format(date));
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        treeModel.insertNodeInto(node, analysisNode, analysisNode.getChildCount());
        
        setNormalizedState(arrayData.getNormalizationState());
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, viewScrollPane);
        splitPane.setOneTouchExpandable(true);
        mainframe.getContentPane().add(splitPane, BorderLayout.CENTER);
        
        statusLabel = new JLabel("TIGR MultiExperiment Viewer");
        mainframe.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        mainframe.pack();
        splitPane.setDividerLocation(.3);
        
        systemEnable(TMEV.DATA_AVAILABLE);
        fireDataChanged();
        
        //systemDisable(TMEV.DB_AVAILABLE);
        //systemDisable(TMEV.DATA_AVAILABLE);
    }
    
    
    
    /**
     * Sets toolbar and menubar states.
     */
    public void systemDisable(int state) {
        menubar.systemDisable(state);
        toolbar.systemDisable(state);
    }
    
    /**
     * Sets toolbar and menubar states.
     */
    public void systemEnable(int state) {
        menubar.systemEnable(state);
        toolbar.systemEnable(state);
    }
    
    /**
     * Returns a reference to an instance of algorithm factory.
     */
    public AlgorithmFactory getAlgorithmFactory() {
        return TMEV.getAlgorithmFactory();
    }
    
    /**
     * Returns a reference to an instance of microarrays data.
     */
    public IData getData() {
        return data;
    }
    
    /**
     * Runs a single array viewer for specified column.
     */
    private void displaySingleArrayViewer(int column) {
        Manager.createNewSingleArrayViewer(data.getFeature(column));
    }
    
    /**
     * Runs a slide element info dialog for a specified spot.
     */
    private void displaySlideElementInfo(int column, int row) {
        Manager.displaySlideElementInfo(mainframe, data, column, row);
    }
    
    /*********************************************
     *  This section of code defines methods to save the state of MeV
     *  to file.
     *
     *  Process:
     *
     *  -Save a time stamp
     *  -Save MultipleArrayData
     *  -Save Analysis Counter
     *  -Save the Analysis Node via ResultTree
     *  -Save the ClusterRepositories
     *  -Save the History Node via ResultTree
     *
     */
    
    public void saveAnalysisAs() {
        try {
            
            final JFileChooser chooser = new JFileChooser(TMEV.getFile("data/"));
            chooser.setFileView(new AnalysisFileView());
            chooser.setFileFilter(new AnalysisFileFilter());
            chooser.setApproveButtonText("Save");
            JPanel panel = new JPanel(new GridBagLayout());
            
            final javax.swing.JDialog dialog = new javax.swing.JDialog(getFrame(), "Save Dialog", true);
            
            chooser.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    String cmd = ae.getActionCommand();
                    if(cmd.equals(JFileChooser.APPROVE_SELECTION)) {
                        File file = chooser.getSelectedFile();
                        dialog.dispose();
                        try {
                            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                            saveState(oos, file);
                        } catch (IOException ioe) {
                            JOptionPane.showMessageDialog(MultipleArrayViewer.this, "I/O Exception, Error saving analysis. File ("+(file != null ? file.getName() : "name unknown")+")", "Save Analysis", JOptionPane.ERROR_MESSAGE);
                            ioe.printStackTrace();
                        }
                    } else {
                        dialog.dispose();
                    }
                }
            });
            
            
            javax.swing.JTextPane pane = new javax.swing.JTextPane();
            pane.setContentType("text/html");
            pane.setEditable(false);
            
            String text = "<html><body><font face=arial size=4><b><center>Analysis Save and Restoration Warning</center><b><hr size=3><br>";//<hr size=3>";
            text += "<font face=arial size=4>Proper restoration of analysis files is dependent on the Java and Java Virtual Machine versions used to open the file. ";
            text += "Analysis files should be opened using Java and Java Virtual Machine versions that match the versions used to save the file.<br><br>";
            
            text += "If version inconsistencies are found when loading an analysis file the saved and current versions " ;
            text +=  "will be reported at that time.  This problem only arises when moving analysis files between computers ";
            text += "running different versions of Java.<br><br></body></html>";
            
            pane.setMargin(new Insets(10,10,10,10));
            pane.setFont(new java.awt.Font("arial", java.awt.Font.PLAIN, 4));
            pane.setText(text);
            JPanel panePanel = new JPanel(new GridBagLayout());
            panePanel.setBorder(BorderFactory.createLineBorder(Color.black));
            panePanel.add(pane,  new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0) );
            panePanel.setPreferredSize(new Dimension(chooser.getPreferredSize().width,((int)(chooser.getPreferredSize().height/1.4))));
            
            panel.add(panePanel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(5,5,5,5), 0, 0));
            panel.add(chooser, new GridBagConstraints(0,1,1,1,0,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));
            
            
            dialog.getContentPane().add(panel);
            dialog.pack();
            
            Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setLocation((screenSize.width - dialog.getSize().width)/2, (screenSize.height - dialog.getSize().height)/2);
            dialog.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveAnalysis() {
        if(this.currentAnalysisFile != null) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.currentAnalysisFile));
                saveState(oos, currentAnalysisFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "State was not saved.  Error finding file to save. \n"+
            "Please use the \"Save As...\" menu item.", "Save Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    
    private void saveState(ObjectOutputStream os, File file) throws IOException {
        final ObjectOutputStream oos = os;
        final String filePath = file.getAbsolutePath();
        this.currentAnalysisFile = file;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try{
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    oos.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_2);
                    // Save MeV tag
                    oos.writeObject(TMEV.VERSION);
                    //Save JRE Version
                    String jre = System.getProperty("java.version");
                    if(jre != null)
                        oos.writeObject(jre);
                    else
                        oos.writeObject("unknown");
                    //Save JVM Version
                    String jvm = System.getProperty("java.vm.version");
                    if(jvm != null)
                        oos.writeObject(jvm);
                    else
                        oos.writeObject("unknown");
                    // Save Date
                    oos.writeLong(System.currentTimeMillis());
                    // Save IData
                    oos.writeObject(data);
                    // Save Analysis Counter
                    oos.writeInt(resultCount);
                    // Save Analysis Tree
                    tree.writeResults(oos);
                    // Save Cluster Repositories
                    saveClusterRepositories(oos);
                    // Record the save to history
                    addHistory("Save Analysis: "+filePath);
                    // Save History Tree
                    tree.writeHistory(oos, historyNode);
                    //reset result changed boolean
                    modifiedResult = false;
                    //enable save menu item, current file is already set
                    menubar.systemEnable(TMEV.ANALYSIS_LOADED);
                    oos.flush();
                    oos.close();
                    setCursor(Cursor.DEFAULT_CURSOR);
                } catch (IOException ioe){
                    setCursor(Cursor.DEFAULT_CURSOR);
                    JOptionPane.showMessageDialog(MultipleArrayViewer.this, "Analysis was not saved.  Error writing output file.",
                    "Save Error", JOptionPane.WARNING_MESSAGE);
                    ioe.printStackTrace();
                }
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    
    
    private void loadState(ObjectInputStream is) throws IOException, ClassNotFoundException {
        final ObjectInputStream ois = is;
        
        Thread thread = new Thread( new Runnable(){
            public void run() {
                try {
                    
                    String savedMEVVersion = (String)ois.readObject();
                    String savedJREVersion = (String)ois.readObject();
                    String savedJVMVersion = (String)ois.readObject();
                    String jreVersion = System.getProperty("java.version");
                    String jvmVersion = System.getProperty("java.vm.version");
                    
                    if(!savedMEVVersion.equals(TMEV.VERSION) || !savedJREVersion.equals(jreVersion) || !savedJVMVersion.equals(jvmVersion)) {
                        String msg = "";
                        String error = "<b>";
                        int errorCount = 0;
                        
                        if(!savedMEVVersion.equals(TMEV.VERSION)) {
                            errorCount++;
                            error += "<br>";
                            error += "Current MeV Version: "+TMEV.VERSION+"<br>";
                            error += "Analysis File MeV Version: "+savedMEVVersion+"<br>";
                        }
                        if(!savedJREVersion.equals(jreVersion)) {
                            errorCount++;
                            error += "<br>";
                            error += "Current Java Runtime Version: "+jreVersion+"<br>";
                            error += "Java Runtime Version During Save: "+savedJREVersion+"<br>";
                        }
                        if(!savedJVMVersion.equals(jvmVersion)) {
                            errorCount++;
                            error += "<br>";
                            error += "Current Java Virtual Machine Version: "+jvmVersion+"<br>";
                            error += "Java Virtual Machine Version During Save: "+savedJVMVersion+"<br>";
                        }
                        
                        msg += "<html><body><font face=arial size=4>The following inconsistenc"+(errorCount == 1 ? "y was" : "ies were")+
                        " detected during analysis file loading:<br>" + error + "</b><br>";
                        if(errorCount > 1) {
                            msg += "These differences ";
                        } else {
                            msg += "This difference ";
                        }
                        msg += "could affect analysis file loading. <br><br>";
                        msg += "Hit <b>\"OK\"</b> to continue loading. (Loading errors will be reported if they occur)<br>";
                        msg += "Hit <b>\"Cancel\"</b> to abort the loading process.</body></html>";
                        
                        JPanel panel = new JPanel();
                        panel.setBackground(Color.white);
                        panel.setBorder(BorderFactory.createLineBorder(Color.black));
                        javax.swing.JTextPane pane = new javax.swing.JTextPane();
                        pane.setEditable(false);
                        pane.setMargin(new Insets(10,10,10,10));
                        pane.setBackground(Color.white);
                        pane.setContentType("text/html");
                        pane.setText(msg);
                        panel.add(pane);
                        
                        int choice = JOptionPane.showConfirmDialog(getFrame(), panel, "Analysis Load Version Confirmations", JOptionPane.WARNING_MESSAGE);
                        //  JOptionPane.showMessageDialog(getFrame(), msg, "Analysis Load Version Confirmations", JOptionPane.WARNING_MESSAGE);
                        if(choice != JOptionPane.OK_OPTION)
                            return;
                    }
                    long dateLong = ois.readLong();
                    //Load IData object and set annotation field names
                    loadIData(ois);
                    
                    //set the current result count
                    resultCount = ois.readInt();
                    
                    //load analysis viewers
                    loadAnalysisNode(ois);
                    
                    //load cluster repositories
                    loadClusterRepositories(ois);
                    
                    //load history
                    loadHistoryNode(ois);
                    
                    //Add time node to the analysis node
                    Date date = new Date(System.currentTimeMillis());
                    DateFormat format = DateFormat.getDateTimeInstance();
                    
                    format.setTimeZone(TimeZone.getDefault());
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(format.format(date));
                    DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
                    treeModel.insertNodeInto(node, analysisNode, analysisNode.getChildCount());
                    
                    TreePath path = new TreePath(((DefaultTreeModel)tree.getModel()).getPathToRoot(analysisNode));
                    tree.expandPath(path);
                    path = new TreePath(((DefaultTreeModel)tree.getModel()).getPathToRoot(historyNode));
                    tree.expandPath(path);
                    
                    //signal mev analysis loaded
                    menubar.systemEnable(TMEV.ANALYSIS_LOADED);

                    //pcahan
                    if(TMEV.getDataType() == TMEV.DATA_TYPE_AFFY){
                        menubar.addAffyFilterMenuItems();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MultipleArrayViewer.this, "Analysis was not loaded.  Error reading input file.",
                    "Load Analysis Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    
    
    private void loadClusterRepositories(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        if(ois.readBoolean()){
            this.geneClusterRepository = (ClusterRepository)ois.readObject();
            this.data.setGeneClusterRepository(this.geneClusterRepository);
            this.geneClusterRepository.setFramework(this.framework);
            
            this.geneClusterManager = new ClusterTable(this.geneClusterRepository, framework);
            DefaultMutableTreeNode genesNode = new DefaultMutableTreeNode(new LeafInfo("Gene Clusters", this.geneClusterManager), false);
            addNode(this.clusterNode, genesNode);
            
        }
        if(ois.readBoolean()){
            this.experimentClusterRepository = (ClusterRepository)ois.readObject();
            this.data.setExperimentClusterRepository(this.experimentClusterRepository);
            this.experimentClusterRepository.setFramework(this.framework);
            
            this.experimentClusterManager = new ClusterTable(this.experimentClusterRepository, framework);
            DefaultMutableTreeNode experimentNode = new DefaultMutableTreeNode(new LeafInfo("Experiment Clusters", this.experimentClusterManager), false);
            addNode(this.clusterNode, experimentNode);
        }
    }
    
    
    private void loadAnalysisNode(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        
        DefaultMutableTreeNode node = tree.loadResults(ois);
        
        if(node != null){
            
            int location = tree.getModel().getIndexOfChild(tree.getRoot(), analysisNode);
            tree.removeNode(analysisNode);
            analysisNode = node;
            tree.insertNode(analysisNode, tree.getRoot(), location);
            tree.setAnalysisNode(analysisNode);
        }
    }
    
    private void loadHistoryNode(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        
        DefaultMutableTreeNode node = tree.loadResults(ois);
        
        if(node != null){
            tree.removeNode(historyNode);
            historyNode = node;
            tree.insertNode(historyNode, tree.getRoot(), tree.getRoot().getChildCount());
            historyLog = (HistoryViewer)(((LeafInfo)(((DefaultMutableTreeNode)historyNode.getChildAt(0)).getUserObject())).getViewer());
        }
    }
    
    
    private void loadIData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        //loads IData and sets TMEV field names fields
        this.data = (MultipleArrayData)(ois.readObject());
        
        //pcahan
        int data_type = this.data.getDataType();
        if (data_type!=0 || data_type!=1){
          TMEV.setDataType(TMEV.DATA_TYPE_AFFY);
        }
        
        //resets the log state depending on data type
        this.data.setDataType(this.data.getDataType());
        
        //get the experiment label keys
        this.menubar.replaceExperimentLabelMenuItems(data.getSlideNameKeyArray());
        
        //populate the display menu
        this.menubar.replaceLabelMenuItems(TMEV.getFieldNames());
        this.menubar.replaceSortMenuItems(TMEV.getFieldNames());

        setMaxCY3AndCY5();
        systemEnable(TMEV.DATA_AVAILABLE);
        fireMenuChanged();
        fireDataChanged();
        fireHeaderChanged();
    }
    
    
    private void saveClusterRepositories(ObjectOutputStream oos) throws IOException {
        if(this.geneClusterRepository == null)
            oos.writeBoolean(false);
        else{
            oos.writeBoolean(true);
            oos.writeObject(this.geneClusterRepository);
        }
        if(this.experimentClusterRepository == null)
            oos.writeBoolean(false);
        else{
            oos.writeBoolean(true);
            oos.writeObject(this.experimentClusterRepository);
        }
    }
    
    
    private void loadAnalysis() {
        File file;
        try {
            JFileChooser chooser = new JFileChooser(TMEV.getFile("data/"));
            chooser.setFileView(new AnalysisFileView());
            chooser.setFileFilter(new AnalysisFileFilter());
            if(chooser.showOpenDialog(this) == JOptionPane.OK_OPTION) {
                file = chooser.getSelectedFile();
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                loadState(ois);
                this.currentAnalysisFile = file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*****************************
     *
     * Script code
     */
    private void onNewScript() {
        if(this.scriptManager == null) {
            scriptManager = new ScriptManager(framework, scriptNode, manager);
        }
        scriptManager.addNewScript();
    }
    
    private void onLoadScript() {
        if(this.scriptManager == null) {
            scriptManager = new ScriptManager(framework, scriptNode, manager);
        }
        scriptManager.loadScript();
    }
    
    
    /**
     * Returns the status bar text.
     */
    private String getStatusText() {
        return statusLabel.getText();
    }
    
    /**
     * Sets the status bar text.
     */
    private void setStatusText(String text) {
        statusLabel.setText(text);
    }
    
    /**
     * Returns an user object of a selected LeafInfo.
     */
    private Object getUserObject() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        if (node == null) {
            return null;
        }
        Object leaf = node.getUserObject();
        if (!(leaf instanceof LeafInfo)) {
            return null;
        }
        return((LeafInfo)leaf).getUserObject();
    }
    
    /**
     * Returns the framework main frame.
     */
    public JFrame getFrame() {
        return mainframe;
    }
    
    /**
     * Moves the scroll pane content into specified coordinaties.
     */
    public void setContentLocation(int x, int y) {
        Dimension viewSize = viewScrollPane.getViewport().getViewSize();
        Dimension extSize  = viewScrollPane.getViewport().getExtentSize();
        if (extSize.height+y > viewSize.height) {
            y = viewSize.height - extSize.height;
        }
        viewScrollPane.getViewport().setViewPosition(new Point(x, y));
    }
    
    /**
     * Creates the navigation tree.
     */
    private JScrollPane createTreeScrollPane(EventListener listener) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("MultipleExperimentViewer");
        
        this.viewer = new MultipleArrayCanvas(this.framework, new Insets(0, 10, 0, 20));
        
        DefaultMutableTreeNode mainViewNode = new DefaultMutableTreeNode(new LeafInfo("Main View", viewer), false);
        
        root.add(mainViewNode);
        
        clusterNode = new DefaultMutableTreeNode(new LeafInfo("Cluster Manager"));
        root.add(clusterNode);
        
        analysisNode = new DefaultMutableTreeNode(new LeafInfo("Analysis Results"));
        root.add(analysisNode);
        
        scriptNode = new DefaultMutableTreeNode(new LeafInfo("Script Manager"));
        root.add(scriptNode);
        
        historyNode = new DefaultMutableTreeNode(new LeafInfo("History"));
        root.add(historyNode);
        historyLog = new HistoryViewer();
        historyNode.add(new DefaultMutableTreeNode(new LeafInfo("History Log", historyLog)));
        
        tree = new ResultTree(root);
        tree.setAnalysisNode(analysisNode);
        
        tree.addTreeSelectionListener(listener);
        tree.addMouseListener(listener);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setSelectionPath(new TreePath(mainViewNode.getPath()));
        tree.setEditable(false);
        
        ToolTipManager.sharedInstance().registerComponent(tree);
        
        
        
        return new JScrollPane(tree);
    }
    
    /**
     * Creates the scroll pane to display calculation results.
     */
    private JScrollPane createViewScrollPane(EventListener listener) {
        JScrollPane scrollPane = new JScrollPane();
        //scrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        //scrollPane.getVerticalScrollBar().setToolTipText("Use up/down/pgup/pgdown to scroll image");
        KeyStroke up = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0);
        KeyStroke down = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0);
        KeyStroke pgup = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_UP, 0);
        KeyStroke pgdown = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_DOWN, 0);
        scrollPane.registerKeyboardAction(listener, "lineup", up, JComponent.WHEN_IN_FOCUSED_WINDOW);
        scrollPane.registerKeyboardAction(listener, "linedown", down, JComponent.WHEN_IN_FOCUSED_WINDOW);
        scrollPane.registerKeyboardAction(listener, "pageup", pgup, JComponent.WHEN_IN_FOCUSED_WINDOW);
        scrollPane.registerKeyboardAction(listener, "pagedown", pgdown, JComponent.WHEN_IN_FOCUSED_WINDOW);
        return scrollPane;
    }
    
    /**
     * Sets a current viewer. The viewer content will be inserted
     * into the scroll pane view port and the viewer header will
     * be used as the scroll pane header view.
     */
    /*private void setCurrentViewer(IViewer viewer) {
        if (viewer == null || viewer.getContentComponent() == null) {
            return;
        }
        if (this.viewer != null) {
            this.viewer.onDeselected();
        }
        this.viewer = viewer;
        this.viewScrollPane.setViewportView(this.viewer.getContentComponent());
     
        JPanel emptycorner = new JPanel();
        emptycorner.setBackground(Color.white);
        emptycorner.setOpaque(true);
     
        if (viewer instanceof GDMGeneViewer == true) {
     
                GDMGeneViewer gdmV = (GDMGeneViewer)viewer;
     
                gdmV.setMultipleArrayData(data);
     
                gdmV.setMainFrame(mainframe);
     
                JComponent colHeader = gdmV.getColumnHeaderComponent();
                if (colHeader != null) {
                    this.viewScrollPane.setColumnHeaderView(colHeader);
                } else {
                    this.viewScrollPane.setColumnHeader(null);
                }
     
                JComponent rowHeader = gdmV.getRowHeaderComponent();
                if (rowHeader != null) {
                    this.viewScrollPane.setRowHeaderView(rowHeader);
                } else {
                    this.viewScrollPane.setRowHeader(null);
                }
     
                JComponent upperRightCornerSB = gdmV.getUpperRightCornerSB();
                if (upperRightCornerSB != null) {
                    this.viewScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, upperRightCornerSB);
                }
     
                JComponent lowerLeftCornerSB = gdmV.getLowerLeftCornerSB();
                if (lowerLeftCornerSB != null) {
                    this.viewScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, lowerLeftCornerSB);
                }
     
                this.viewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                this.viewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
     
        } else if (viewer instanceof GDMExpViewer == true) {
     
                GDMExpViewer gdmV = (GDMExpViewer)viewer;
     
                gdmV.setMultipleArrayData(data);
     
                gdmV.setMainFrame(mainframe);
     
                JComponent colHeader = gdmV.getColumnHeaderComponent();
                if (colHeader != null) {
                    this.viewScrollPane.setColumnHeaderView(colHeader);
                } else {
                    this.viewScrollPane.setColumnHeader(null);
                }
     
                JComponent rowHeader = gdmV.getRowHeaderComponent();
                if (rowHeader != null) {
                    this.viewScrollPane.setRowHeaderView(rowHeader);
                } else {
                    this.viewScrollPane.setRowHeader(null);
                }
     
                JComponent upperRightCornerSB = gdmV.getUpperRightCornerSB();
                if (upperRightCornerSB != null) {
                    this.viewScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, upperRightCornerSB);
                }
     
                JComponent lowerLeftCornerSB = gdmV.getLowerLeftCornerSB();
                if (lowerLeftCornerSB != null) {
                    this.viewScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, lowerLeftCornerSB);
                }
     
                this.viewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                this.viewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
     
        } else {
     
                JComponent header = viewer.getHeaderComponent();
                if (header != null) {
                    this.viewScrollPane.setColumnHeaderView(header);
     
                    if (this.viewScrollPane.getCorner(JScrollPane.UPPER_RIGHT_CORNER) != null) {
                        this.viewScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, emptycorner);
                    }
     
                } else {
                    this.viewScrollPane.setColumnHeader(null);
                }
                this.viewScrollPane.setRowHeader(null);
     
        }
     
        this.viewer.onSelected(framework);
        doViewLayout();
        handleThumbnailButton(this.viewer);
    }
     */
    
    /**
     * Sets a current viewer. The viewer content will be inserted
     * into the scroll pane view port and the viewer header will
     * be used as the scroll pane header view.
     */
    
    private void setCurrentViewer(IViewer viewer) {
        if (viewer == null || viewer.getContentComponent() == null) {
            return;
        }
        if (this.viewer != null) {
            this.viewer.onDeselected();
        }
        this.viewer = viewer;
        this.viewScrollPane.setViewportView(this.viewer.getContentComponent());
        
        //Top Header (column header)
        JComponent header = viewer.getHeaderComponent();
        if (header != null) {
            this.viewScrollPane.setColumnHeaderView(header);
        } else {
            this.viewScrollPane.setColumnHeader(null);
        }
        
        //Left header (row header)
        JComponent rowHeader = viewer.getRowHeaderComponent();
        if (rowHeader != null) {
            this.viewScrollPane.setRowHeaderView(rowHeader);
        } else {
            this.viewScrollPane.setRowHeader(null);
        }
        
        //Corner components
        JComponent cornerComponent = viewer.getCornerComponent(IViewer.UPPER_LEFT_CORNER);
        if (cornerComponent != null)
            this.viewScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, cornerComponent);
        
        cornerComponent = viewer.getCornerComponent(IViewer.UPPER_RIGHT_CORNER);
        if (cornerComponent != null)
            this.viewScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerComponent);
        
        cornerComponent = viewer.getCornerComponent(IViewer.LOWER_LEFT_CORNER);
        if (cornerComponent != null)
            this.viewScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, cornerComponent);
        
        this.viewer.onSelected(framework);
        doViewLayout();
        handleThumbnailButton(this.viewer);
    }
    
    
    /**
     * Sets state of the thumbnail button according to a viewer
     * thumbnail enabled attribute state.
     * At the moment only a <code>MultipleArrayCanvas</code> support
     * thumbnail feature.
     */
    private void handleThumbnailButton(IViewer viewer) {
        if (viewer instanceof MultipleArrayCanvas) {
            this.toolbar.setThumbnailEnabled(((MultipleArrayCanvas)viewer).isThumbnailEnabled());
        } else {
            this.toolbar.setThumbnailEnabled(false);
        }
    }
    
    /**
     * Returns a current viewer.
     */
    private IViewer getCurrentViewer() {
        return viewer;
    }
    
    /**
     * Invokes onClose method for all the viewers when the framework is
     * going to be closed.
     */
    private void fireOnCloseEvent(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof LeafInfo) {
            LeafInfo leafInfo = (LeafInfo)userObject;
            IViewer viewer = leafInfo.getViewer();
            if (viewer != null) {
                viewer.onClosed();
            }
        }
        for (int i=0; i<node.getChildCount(); i++) {
            fireOnCloseEvent((DefaultMutableTreeNode)node.getChildAt(i));
        }
    }
    
    /**
     * Invoked by a window listener when frame close button was pressed.
     */
    private void onClose() {
        onSaveCheck();
        
        addHistory("Close Viewer");
        
        TMEV.setDataType(TMEV.DATA_TYPE_TWO_DYE);  //default type
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        fireOnCloseEvent((DefaultMutableTreeNode)model.getRoot());
        mainframe.dispose();
        Manager.removeComponent(this);
    }
    
    /**
     * Checks to see if the session should be saved
     */
    private void onSaveCheck() {
        //meets three criteria, has data loaded, result is modified, allowed to prompt
        if(this.modifiedResult && this.data != null && TMEV.permitSavePrompt){
            AnalysisSaveDialog dialog = new AnalysisSaveDialog(this.getFrame());
            int result = dialog.showModal();
            boolean permitSave = dialog.askAgain();
            if(result == JOptionPane.YES_OPTION){
                saveAnalysisAs();
            }
            if(TMEV.permitSavePrompt != permitSave) {
                TMEV.setPermitPrompt(permitSave);
            }
        }
    }
    
    /**
     * Creates an image for specified viewer.
     */
    private BufferedImage createDefaultImage(IViewer viewer) {
        JComponent content = viewer.getContentComponent();
        JComponent header  = viewer.getHeaderComponent();
        int width  = content.getWidth();
        int height = content.getHeight();
        if (header != null) {
            width = Math.max(width, header.getWidth());
            height += header.getHeight();
        }
        // BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(256,1);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);  //need to use this type for image creation
        Graphics2D g = image.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        
        if (header != null) {
            int headerHeight = header.getHeight();
            g.setClip(0, 0, width, headerHeight);
            header.paint(g);
            g.translate(0, headerHeight);
            g.setClip(0, 0, width, height-headerHeight);
        } else {
            g.setClip(0, 0, width, height);
        }
        content.paint(g);
        return image;
    }
    
    /**
     * Saves a current viewer image into the user specified file.
     */
    private void onSaveImage() {
        final JFileChooser chooser = new JFileChooser(TMEV.getFile("data?"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new BMPFileFilter());
        chooser.addChoosableFileFilter(new JPGFileFilter());
        chooser.addChoosableFileFilter(new PNGFileFilter());
        chooser.addChoosableFileFilter(new TIFFFileFilter());
        int chooserState = chooser.showSaveDialog(getFrame());
        if (chooserState == JFileChooser.APPROVE_OPTION) {
            IViewer viewer = getCurrentViewer();
            BufferedImage image = viewer.getImage();
            if (image == null) {
                image = createDefaultImage(viewer);
            }
            final File fFile = chooser.getSelectedFile();
            final BufferedImage fImage = image;
            final String fFormat = ((ImageFileFilter)chooser.getFileFilter()).getFileFormat();
            final ImageEncodeParam fParam = ((ImageFileFilter)chooser.getFileFilter()).getImageEncodeParam();
            try {
                Thread thread = new Thread() {
                    public void run() {
                        JAI.create("filestore", fImage, fFile.getPath(), fFormat, fParam);
                        Manager.message(getFrame(), "Image saved: "+fFile.getPath());
                    }
                };
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            } catch (Exception e) {
                Manager.message(getFrame(), e);
            }
        }
    }
    
    
    /**
     * Loads file with a microarray data.
     */
    private void onLoadFile() {
        try {
            ISlideData slideData = loadSlideData(data.getSlideMetaData());
            if (slideData != null){
                addFeature(slideData);
                setMaxCY3AndCY5();
            }
        } catch (Exception e) {
            ShowThrowableDialog.show(mainframe, "Load File Error", e);
        }
    }
    
    /**
     * Loads microarrays data from cluster formatted file.
     */
    private void onLoadCluster() {
        loadFromStanfordFile("Select a Cluster File to Open");
    }
    
    /**
     * Loads microarrays data from stanford formatted file.
     */
    private void onLoadStanford() {
        loadFromStanfordFile("Select a Stanford File to Open");
    }
    
    /**
     * Loads microarrays data from a database.
     *
     * Note: does'nt implemented at the moment.
     */
    private void onLoadDatabase() {
        SetDatabaseDialog sdd = new SetDatabaseDialog(getFrame());
        if (sdd.showModal() != JOptionPane.OK_OPTION) {
            return;
        }
        String database = sdd.getDatabase();
        
        // STUB: file names shold be loaded from the 'database'
        String[] files = new String[] {"L4A1", "L4A2", "L4A3"};
        
        SetSlideFilenameDialog ssfd = new SetSlideFilenameDialog(getFrame(), files);
        if (ssfd.showModal() != JOptionPane.OK_OPTION) {
            return;
        }
        String filename = ssfd.getFileName();
        System.out.println("db  : "+database);
        System.out.println("file: "+filename);
    }
    
    /**
     * Loads stanford file.
     * @param title the title for standard file chooser dialog.
     */
    private void loadFromStanfordFile(String title) {
        try {
            ISlideData[] slideData = super.loadStanfordFile(title);
            if (slideData != null) {
                addFeatures(slideData);
                setMaxCY3AndCY5();
            }
        } catch (Exception e) {
            ShowThrowableDialog.show(mainframe, "Load Data Error", e);
        }
    }
    
    /**
     * Loads data from the user specified directory.
     */
    private void onLoadDirectory() {
        try {
            ISlideData[] slideData = loadDirectory(data.getSlideMetaData());
            if (slideData != null) {
                addFeatures(slideData);
                setMaxCY3AndCY5();
            }
        } catch (Exception e) {
            ShowThrowableDialog.show(mainframe, "Load Directory Error", e);
        }
    }
    
    /**
     * Sets Initial Max CY3 and CY5 in menu
     */
    private void setMaxCY3AndCY5(){
        this.menubar.setMaxCY3Scale(data.getMaxCY3());
        this.menubar.setMaxCY5Scale(data.getMaxCY5());
    }
    
    /**
     * Invoked when a label menu item is selected.
     */
    private void onLabelChanged(Action action) {
        String index = (String)action.getValue(ActionManager.PARAMETER);
        menubar.setLabelIndex(Integer.parseInt(index));
        fireMenuChanged();
    }
    
    /**
     * Invoked when a label menu item is selected.
     */
    private void onExperimentLabelChanged(Action action) {
        String key = (String)action.getValue(ActionManager.PARAMETER);
        //menubar.setExperimentLabelIndex(Integer.parseInt(index));
        this.data.setSampleLabelKey(key);
        fireMenuChanged();
    }
    
    
    private void onExperimentLabelAdded() {
        
        boolean safeToReorderExperiments = false;
        
        //make sure no results exist and cluster repositories are null, then safe to reorder.
        //note that result counter starts at 1 and holds the index for the next result
        safeToReorderExperiments = (this.resultCount < 2 && this.geneClusterRepository == null && this.experimentClusterRepository == null);
        
        //get the longest key set from loaded samples
        Vector featureAttributes = this.data.getSlideNameKeyVectorUnion();
        
        
        ExperimentLabelEditor editor = new ExperimentLabelEditor(this.getFrame(), featureAttributes, this.data, safeToReorderExperiments);
        
        //return if not OK
        if(editor.showModal() != JOptionPane.OK_OPTION)
            return;             
        
        //get data and keys
        String [][] data = editor.getLabelDataWithoutKeys();
        String [] keys = editor.getLabelKeys();

        //add/update features
        for(int i=0; i < keys.length; i++)
            this.data.addNewExperimentLabel(keys[i], data[i]);

        //add the new label to the experiment label menu    
        this.menubar.replaceExperimentLabelMenuItems(keys);
        
        //now the data has been updated, check for reordering request
        if(safeToReorderExperiments && editor.isReorderedSelected()) { 
            int [] order = editor.getNewOrderScheme();
            ArrayList featuresList = new ArrayList(order.length);
            for(int i = 0; i < order.length; i++) {
                featuresList.add(this.data.getFeature(order[i]));
            }
            //set new features list
            this.data.setFeaturesList(featuresList);        
        }        
        this.fireDataChanged();
    }
    
    /**
     * Adds a microarray data into the framework.
     */
    private void addFeature(ISlideData slideData) {
        data.addFeature(slideData);
        systemEnable(TMEV.DATA_AVAILABLE);
        fireDataChanged();
    }
    
    /**
     * Adds an array of microarrays data into the framework.
     */
    private void addFeatures(ISlideData[] slideData) {
        data.addFeatures(slideData);
        systemEnable(TMEV.DATA_AVAILABLE);
        fireDataChanged();
    }
    
    /**
     * Notifies a current viewer what the framework data is changed.
     */
    public void fireDataChanged() {
        IViewer viewer = getCurrentViewer();
        if (viewer == null) {
            return;
        }
        viewer.onDataChanged(data);
        doViewLayout();
    }
    
    /**
     * Notifies a current viewer what the framework menu is changed.
     */
    private void fireMenuChanged() {
        IViewer viewer = getCurrentViewer();
        if (viewer == null) {
            return;
        }
        viewer.onMenuChanged(menubar.getDisplayMenu());
        doViewLayout();
    }
    
    
    /**
     * Invoked when the header name is truncated or expanded.
     */
    private void fireHeaderChanged() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        Object userObject = node.getUserObject();
        if (!(userObject instanceof LeafInfo)) {
            return;
        }
        setCurrentViewer(((LeafInfo)userObject).getViewer());
    }
    
    /**
     * Updates the scroll pane size according to a current
     * viewer one.
     */
    private void doViewLayout() {
        JViewport header = viewScrollPane.getColumnHeader();
        if (header != null) {
            header.doLayout();
        }
        viewScrollPane.getViewport().doLayout();
        viewScrollPane.doLayout();
        viewScrollPane.repaint();
    }
    
    /**
     * Normalize the framework data with specified mode.
     */
    private void onNormalizeData(int mode) {
        setCursor(Cursor.WAIT_CURSOR);
        data.normalize(mode, this);
        addHistory("Normalization State: "+SlideData.normalizationString(mode));
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    /**
     * Normalize the framework data with specified mode.
     */
    private void onNormalizeDataList(int mode) {
        setCursor(Cursor.WAIT_CURSOR);
        data.normalizeList(mode);
        addHistory(SlideData.normalizationString(mode));
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    /**
     * Invoked when the navigation tree node is changed.
     */
    private void onNodeChanged(TreeSelectionEvent event) {
        JTree tree = (JTree)event.getSource();
        TreePath path = event.getPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        Object userObject = node.getUserObject();
        if (!(userObject instanceof LeafInfo)) {
            return;
        }
        setCurrentViewer(((LeafInfo)userObject).getViewer());
    }
    
    /**
     * Sets a spot size.
     */
    private void onElementSizeChanged(int width, int height) {
        menubar.setElementSize(width, height);
        fireMenuChanged();
    }
    
    /**
     * Sets the user specified spot size.
     */
    private void onElementSizeChanged() {
        SetElementSizeDialog sesd = new SetElementSizeDialog(getFrame(), menubar.getDisplayMenu().getElementSize());
        if (sesd.showModal() == JOptionPane.OK_OPTION) {
            Dimension size = sesd.getElementSize();
            onElementSizeChanged(size.width, size.height);
        }
    }
    
    /**
     * Sets the color pallete colors
     */
    private void onColorSchemeChange(int colorScheme){
        int initColorScheme = menubar.getColorScheme();
        if(colorScheme == IDisplayMenu.GREEN_RED_SCHEME || colorScheme == IDisplayMenu.BLUE_YELLOW_SCHEME)
            this.menubar.setColorSchemeIndex(colorScheme);
        else{
            ColorSchemeSelectionDialog dialog = new ColorSchemeSelectionDialog((Frame)getFrame(), true, menubar.getNegativeGradientImage(), menubar.getPositiveGradientImage());
            if(dialog.showModal() != JOptionPane.OK_OPTION)
                return;
            this.menubar.setPositiveCustomGradient(dialog.getPositiveGradient());
            this.menubar.setNegativeCustomGradient(dialog.getNegativeGradient());
            this.menubar.setColorSchemeIndex(colorScheme);
        }
        fireMenuChanged();
    }
    
    
    
    /**
     * Invoked when pallete style is changed.
     */
    private void onPaletteStyleChanged(int style) {
        menubar.setPaletteStyle(style);
        fireMenuChanged();
    }
    /**
     *  Sets the current (selected) state of gradient use
     */
    private void onColorGradientChange(boolean gradientState){
        menubar.setColorGradientState(gradientState);
        fireMenuChanged();
    }
    
    /**
     * Invoked when tracing menu item is changed.
     */
    private void onTracing() {
        menubar.setTracing(!menubar.getDisplayMenu().isTracing());
        fireMenuChanged();
    }
    
    /**
     * Invoked when GR scale menu item is changed.
     */
    private void onGRScale() {
        menubar.setGRScale(!menubar.getDisplayMenu().isGRScale());
        fireMenuChanged();
    }
    
    /**
     * Invoked when anti-aliasing menu item is changed.
     */
    private void onAntiAliasing() {
        menubar.setAntiAliasing(!menubar.getDisplayMenu().isAntiAliasing());
        fireMenuChanged();
    }
    
    /**
     * Invoked when draw borders menu item is changed.
     */
    private void onDrawBorders() {
        menubar.setDrawBorders(!menubar.getDisplayMenu().isDrawingBorder());
        fireMenuChanged();
    }
    
    /**
     * Invoked when a sort menu item is changed.
     */
    private void onSort(Action action) {
        String index = (String)action.getValue(ActionManager.PARAMETER);
        onSort(Integer.parseInt(index));
    }
    
    /**
     * Sorts the framework data.
     */
    private void onSort(int style) {
        data.sort(style);
        fireDataChanged();
    }
    
    /**
     * Shows the system info dialog.
     */
    private void onSystemInfo() {
        int width = 640, height = 550;
        InformationPanel infoPanel = new InformationPanel();
        JFrame frame = new JFrame("System Information");
        frame.getContentPane().add(infoPanel);
        frame.setSize(width, height);
        Dimension screenSize = getToolkit().getScreenSize();
        frame.setLocation(screenSize.width/2 - width/2, screenSize.height/2 - height/2);
        frame.setResizable(false);
        frame.setVisible(true);
        infoPanel.Start();
    }
    
    /**
     * Shows algorithms default distance functions.
     */
    private void onDefaultDistance() {
        String defaultText = "<html>"+
        "<font color=\"#000000\"><b><u>Default Distances</u></b></font>"+
        "<p>"+
        "<table border=20 cellspacing=10 cellpadding = 10 width= 380 height= 400>"+
        "<tr><th><u><center><width=200>Algorithm</center></u></th><th width = 150><u><center>Default Metric</center></u></th></tr>"+
        "<tr><td><center>HCL, ST, SOTA, KMC, KMS, SOM, CAST, GSH, FOM</center></td><td><center>Euclidean</center></td></tr>"+
        // "<tr><td></td></td><td></td></tr>"+
        //"<tr>"+
        "<tr><td><center>PCA</center></td><td><center>Covariance</center></td></tr>"+
        "<tr><td><center>SVM</center></td><td><center>Dot Product</center></td></tr>"+
        "<tr><td><center>RN, QTC, PTM</center></td><td><center>Pearson Correlation</center></td></tr>"+
        "</center></table>"+
        "</html>";
        JOptionPane.showMessageDialog(getFrame(), new JLabel(defaultText), "Default Distances", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void setNormalizedState(int originalMode){
        if(originalMode == ISlideData.NO_NORMALIZATION){
            menubar.setNormalizedButtonState(5);
        } else {
            addHistory(SlideData.normalizationString(originalMode));
            if(originalMode == ISlideData.TOTAL_INTENSITY){
                menubar.setNormalizedButtonState(0);
            } else if(originalMode == ISlideData.LINEAR_REGRESSION){
                menubar.setNormalizedButtonState(1);
            } else if(originalMode == ISlideData.RATIO_STATISTICS_95 ||
            originalMode == ISlideData.RATIO_STATISTICS_99){
                menubar.setNormalizedButtonState(2);
            } else if(originalMode == ISlideData.ITERATIVE_LOG){
                menubar.setNormalizedButtonState(3);
            }
        }
    }
    
    /**
     * Normalize the framework data.
     */
    private void onNormalize(int mode) {
        final int originalMode = data.getFeature(0).getNormalizedState();
        final int Mode = mode;
        setCursor(Cursor.WAIT_CURSOR);
        try{
            Thread thread = new Thread(new Runnable(){
                public void run(){
                    String result = data.normalize(Mode, MultipleArrayViewer.this);
                    if(!result.equals("no_change")){                // if not aborted in dialog before start
                        if(result.equals("normalized"))                // if normalized
                            addHistory(SlideData.normalizationString(Mode));
                        else if(result.equals("process_abort_reset")){  // if process started then aborted, reset to no norm.
                            addHistory("Norm. aborted, reset to raw state");
                            menubar.setNormalizedButtonState(5); //move radio button
                        }
                        fireDataChanged();
                        setCursor(Cursor.DEFAULT_CURSOR);
                    } else {
                        // process aborted before it starts no change in data, return button state
                        if(originalMode == ISlideData.NO_NORMALIZATION)
                            menubar.setNormalizedButtonState(5);
                        else if(originalMode == ISlideData.TOTAL_INTENSITY)
                            menubar.setNormalizedButtonState(0);
                        else if(originalMode == ISlideData.LINEAR_REGRESSION)
                            menubar.setNormalizedButtonState(1);
                        else if(originalMode == ISlideData.RATIO_STATISTICS_95 ||
                        originalMode == ISlideData.RATIO_STATISTICS_99)
                            menubar.setNormalizedButtonState(2);
                        else if(originalMode == ISlideData.ITERATIVE_LOG)
                            menubar.setNormalizedButtonState(3);
                    }
                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace(); }
        setCursor(Cursor.DEFAULT_CURSOR);
        fireDataChanged();
    }
    
    
    /**
     * Normalize the framework data.
     */
    private void onNormalizeList(int mode) {
        setCursor(Cursor.WAIT_CURSOR);
        data.normalizeList(mode);
        addHistory(SlideData.normalizationString(mode));
        fireDataChanged();
        setCursor(Cursor.DEFAULT_CURSOR);
    }
    
    /**
     * Runs a printer job.
     */
    private void onPrintImage() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(this, pj.defaultPage());
        if (pj.printDialog()) {
            try {
                pj.print();
            } catch (PrinterException pe) {
                pe.printStackTrace();
            }
        }
    }
    
    /**
     * Prints a current viewer image.
     */
    public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
        if (pi >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        IViewer viewer = getCurrentViewer();
        BufferedImage bImage = viewer.getImage();
        if (bImage == null) {
            bImage = createDefaultImage(viewer);
        }
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform t2d = new AffineTransform();
        t2d.translate(pf.getImageableX(), pf.getImageableY());
        double xscale  = pf.getImageableWidth() / (double)bImage.getWidth();
        double yscale  = pf.getImageableHeight() / (double)bImage.getHeight();
        double scale = Math.min(xscale, yscale);
        t2d.scale(scale, scale);
        try {
            g2.drawImage(bImage, t2d, this);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Printable.NO_SUCH_PAGE;
        }
        return Printable.PAGE_EXISTS;
    }
    
    /**
     * Adds a specified node into the analysis node.
     */
    private synchronized void addAnalysisResult(DefaultMutableTreeNode node) {
        if (node == null) {
            return;
        }
        String nodeTitle = (String) node.getUserObject();
        nodeTitle += " ("+resultCount+")";
        resultCount++;
        modifiedResult = true;
        node.setUserObject(nodeTitle);
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        treeModel.insertNodeInto(node, analysisNode, analysisNode.getChildCount());
        TreeSelectionModel selModel = tree.getSelectionModel();
        TreePath treePath = new TreePath(node.getPath());
        selModel.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
        JScrollBar bar = this.treeScrollPane.getHorizontalScrollBar();
        if(bar != null)
            bar.setValue(0);
        
        addHistory("Analysis Result: "+nodeTitle);
        /// this.saveAnalysis();
        // this.loadAnalysis();
   /*     try{
             ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("c:/Temp/out.out"));
             tree.writeResults(oos);
             oos.flush();
             oos.close();
    
             ObjectInputStream ois = new ObjectInputStream(new FileInputStream("c:/Temp/out.out"));
    
             treeModel.insertNodeInto(tree.loadResults(ois), analysisNode, analysisNode.getChildCount());
    
        } catch (Exception e) { e.printStackTrace();}
    **/
    }
    
    /**
     * Adds info into the history node.
     */
    private void addHistory(String info) {
        historyLog.addHistory(info);
        /*
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        treeModel.insertNodeInto(node, historyNode, historyNode.getChildCount());
        TreeSelectionModel selModel = tree.getSelectionModel();
        TreePath treePath = new TreePath(node.getPath());
        selModel.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
        this.treeScrollPane.getHorizontalScrollBar().setValue(0);
         */
    }
    
    /**
     * Runs an analysis task and inserts its result into the analysis node.
     */
    private void onAnalysis(Action action) {
        String className = (String)action.getValue(ActionManager.PARAMETER);
        try {
            Class clazz = Class.forName(className);
            final IClusterGUI gui = (IClusterGUI)clazz.newInstance();
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        DefaultMutableTreeNode result = gui.execute(framework);
                        addAnalysisResult(result);
                    } catch (AbortException e) {
                        // analysis was canceled by the user
                    } catch (Exception e) {
                        ShowThrowableDialog.show(mainframe, "Analysis Error", false, e);
                    }
                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        } catch (ClassCastException e) {
            System.out.println("Error: org.tigr.microarray.mev.cluster.gui.IClusterGUI interface is expected.");
            ShowThrowableDialog.show(mainframe, "Analysis Error", false, e);
        } catch (Exception e) {
            ShowThrowableDialog.show(mainframe, "Analysis Error", false, e);
        }
    }
    
    /**
     * Deletes a selected navigation tree node.
     */
    private void onDeleteNode() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        if (node == null || node.getParent() == null) {
            return;
        }
        fireOnCloseEvent(node);
        TreePath parentPath = new TreePath(((DefaultMutableTreeNode)node.getParent()).getPath());
        ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(node);
        ((TreeSelectionModel)tree.getSelectionModel()).setSelectionPath(parentPath);
        tree.scrollPathToVisible(parentPath);
        
        String nodeName = " ";
        Object object = node.getUserObject();
        if(object instanceof LeafInfo)
            nodeName = ((LeafInfo)object).toString();
        else if(object instanceof String)
            nodeName = (String)object;
        addHistory("Deleted Node: "+nodeName);
        
    }
    
    
    
    /** pcahan
     * Sets the user specified Detection Filter.
     */
    private void onSetDetectionFilter() {
        //SetDetectionFilterDialog sdfd = new SetDetectionFilterDialog(getFrame(), data.getDetectionFilter() );
        
        int num_samples = data.getFeaturesCount();
        String[] sample_names = new String[num_samples];
        for (int i = 0; i < num_samples; i++){
            sample_names[i] = data.getFullSampleName(i);
        }
        SetDetectionFilterDialog sdfd;
        if ( data.getdfSet() ) {
            sdfd = new SetDetectionFilterDialog(getFrame(), sample_names, data.getDetectionFilter() );
        }
        else {
            sdfd = new SetDetectionFilterDialog(getFrame(),
            sample_names);
            data.setdfSet(true);
        }
        //SetDetectionFilterDialog sdfd = new SetDetectionFilterDialog(getFrame(), data.getDetectionFilter() );
        if (sdfd.showModal() == JOptionPane.OK_OPTION) {
            data.setDetectionFilter(sdfd.getDetectionFilter());
            if (data.isDetectionFilter()) {
                addHistory("Detection Filter (" + data.getDetectionFilter() + ")");
                addHistory(data.getExperiment().getNumberOfGenes() + " genes will used in subsequent analyses");
            }
        }
    }
    
    
    private void onSetFoldFilter() {
        SetFoldFilterDialog ffd;
        int num_samples = data.getFeaturesCount();
        String[] sample_names = new String[num_samples];
        for (int i = 0; i < num_samples; i++){
            sample_names[i] = data.getFullSampleName(i);
        }

        if ( data.getffSet() ) {
            ffd = new SetFoldFilterDialog(getFrame(),sample_names);
        }
        else{
            ffd = new SetFoldFilterDialog(getFrame(),sample_names);
            data.setffSet(true);
        }

        if (ffd.showModal() == JOptionPane.OK_OPTION) {
            data.setFoldFilter(ffd.getFoldFilter());
            if (data.isFoldFilter()) {
                addHistory("Fold Filter (" + data.getFoldFilter().toString() + ")");
                addHistory(data.getExperiment().getNumberOfGenes() + " genes will used in subsequent analyses");

            }
        }
    }
    
    
    
    
    /**
     * Sets the user specified lower cutoffs.
     */
    private void onSetLowerCutoffs() {
        SetLowerCutoffsDialog slcd = new SetLowerCutoffsDialog(getFrame(), data.getLowerCY3Cutoff(), data.getLowerCY5Cutoff());
        if (slcd.showModal() == JOptionPane.OK_OPTION) {
            data.setLowerCutoffs(slcd.getLowerCY3Cutoff(), slcd.getLowerCY5Cutoff());
            if (data.isLowerCutoffs()) {
                addHistory("Lower cutoffs (" + data.getLowerCY3Cutoff() + ", " + data.getLowerCY5Cutoff() + ")");
                addHistory(data.getExperiment().getNumberOfGenes() + " genes will used in subsequent analyses");
            }
        }
    }
    
    /**
     * Sets the user specified use lower cutoffs flag.
     */
    private void onUseLowerCutoffs(AbstractButton item) {
        data.setUseLowerCutoffs(item.isSelected());
        if (data.isLowerCutoffs()) {
            addHistory("Lower cutoffs (" + data.getLowerCY3Cutoff() + ", " + data.getLowerCY5Cutoff() + ")");
        } else {
            addHistory("Lower cutoffs not used");
        }
        addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");
    }
    
    /**
     * Sets the user specified percentage cutoffs.
     */
    private void onSetPercentageCutoffs() {
        SetPercentageCutoffsDialog spcd = new SetPercentageCutoffsDialog(getFrame(), data.getPercentageCutoff());
        if (spcd.showModal() == JOptionPane.OK_OPTION) {
            data.setPercentageCutoff(spcd.getPercentageCutoff());
            if (data.isPercentageCutoff()) {
                addHistory("Percentage cutoff (" + data.getPercentageCutoff() + "%)");
                addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");
            }
        }
    }
    
    /**
     * Sets the user specified use percentage cutoffs flag.
     */
    private void onUsePercentageCutoffs(AbstractButton item) {
        data.setUsePercentageCutoff(item.isSelected());
        if (data.isPercentageCutoff()) {
            addHistory("Percentage cutoff (" + data.getPercentageCutoff() + "%)");
        } else {
            addHistory("Percentage cutoffs not used");
        }
        addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");
    }
    
    
    /**
     * Sets the user specified use detection filter flag.
     * pcahan
     */
    private void onUseDetectionFilter(AbstractButton item) {
        data.setUseDetectionFilter(item.isSelected());
        if (data.isDetectionFilter()) {
            addHistory("Detection Filter (" + data.getDetectionFilter() + ")");
        } else {
            addHistory("Detection Filter not used.");
        }
        addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");
    }
    
    private void onUseFoldFilter(AbstractButton item) {
        data.setUseFoldFilter(item.isSelected());
        if (data.isFoldFilter()) {
            addHistory("Fold Filter (" + data.getDetectionFilter() + ")");
        } else {
            addHistory("Fold Filter not used.");
        }
        addHistory(data.getExperiment().getNumberOfGenes() + " genes will be used in subsequent analyses");
    }
    
    ////////////////////////////////////////////////
    //                                            //
    // Some methods to adjust the framework data. //
    //                                            //
    ////////////////////////////////////////////////
    
    private void onLog2Transform() {
        //data.log10toLog2();
        data.log2Transform();
        fireDataChanged();
        addHistory("Log2 Transform");
    }
    
    private void onNormalizeSpots() {
        data.normalizeSpots();
        fireDataChanged();
        addHistory("Normalize Spots");
    }
    
    private void onDivideSpotsRMS() {
        data.divideSpotsRMS();
        fireDataChanged();
        addHistory("Divide Spots by RMS");
    }
    
    private void onDivideSpotsSD() {
        data.divideSpotsSD();
        fireDataChanged();
        addHistory("Divide Spots by SD");
    }
    
    // pcahan
    private void onDivideGenesMedian() {
        data.divideGenesMedian();
        fireDataChanged();
        addHistory("Per gene normalization -- Divide Genes by Median");
    }
    
    private void onDivideGenesMean() {
        data.divideGenesMean();
        fireDataChanged();
        addHistory("Per gene normalization -- Divide Genes by Mean");
    }
    
    private void onMeanCenterSpots() {
        data.meanCenterSpots();
        fireDataChanged();
        addHistory("Mean Center Spots");
    }
    
    private void onMedianCenterSpots() {
        data.medianCenterSpots();
        fireDataChanged();
        addHistory("Median Center Spots");
    }
    
    private void onDigitalSpots() {
        data.digitalSpots();
        fireDataChanged();
        addHistory("Digital Spots");
    }
    
    private void onNormalizeExperiments() {
        data.normalizeExperiments();
        fireDataChanged();
        addHistory("Normalize Experiments");
    }
    
    private void onDivideExperimentsRMS() {
        data.divideExperimentsRMS();
        fireDataChanged();
        addHistory("Divide Experiments by RMS");
    }
    
    private void onDivideExperimentsSD() {
        data.divideExperimentsSD();
        fireDataChanged();
        addHistory("Divide Experiments by SD");
    }
    
    private void onMeanCenterExperiments() {
        data.meanCenterExperiments();
        fireDataChanged();
        addHistory("Mean Center Experiments");
    }
    
    private void onMedianCenterExperiments() {
        data.medianCenterExperiments();
        fireDataChanged();
        addHistory("Median Center Experiments");
    }
    
    private void onDigitalExperiments() {
        data.digitalExperiments();
        fireDataChanged();
        addHistory("Digital Experiments");
    }
    
    private void onLog10toLog2() {
        data.log10toLog2();
        fireDataChanged();
        addHistory("Log10 to Log2");
    }
    
    private void onAdjustIntensities(AbstractButton item) {
        data.setNonZero(item.isSelected());
        fireDataChanged();
    }
    
    /**
     * Saves the framework data ratio values.
     */
    private void onSaveMatrix() {
        try {
            ExperimentUtil.saveExperiment(mainframe, data.getExperiment(), data);
            addHistory("Save Data Matrix to File");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainframe, "Can not save matrix!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the user specified upper limits.
     */
    private void onSetUpperLimits() {
        IDisplayMenu menu = menubar.getDisplayMenu();
        SetUpperLimitsDialog suld = new SetUpperLimitsDialog(mainframe, menu.getMaxCY3Scale(), menu.getMaxCY5Scale());
        if (suld.showModal() == JOptionPane.OK_OPTION) {
            menubar.setMaxCY3Scale(suld.getUpperCY3());
            menubar.setMaxCY5Scale(suld.getUpperCY5());
            fireMenuChanged();
        }
        addHistory("Intensity Limits Set: Upper Cy3 = "+ suld.getUpperCY3() +" Upper Cy5 = "+ suld.getUpperCY5());
    }
    
    /**
     * Sets the user specified ratio scale.
     */
    private void onSetRatioScale() {
        IDisplayMenu menu = menubar.getDisplayMenu();
        SetRatioScaleDialog srsd = new SetRatioScaleDialog(mainframe, menu.getMaxRatioScale(), menu.getMinRatioScale());
        if (srsd.showModal() == JOptionPane.OK_OPTION) {
            menubar.setMaxRatioScale(srsd.getUpperRatio());
            menubar.setMinRatioScale(srsd.getLowerRatio());
            fireMenuChanged();
        }
        addHistory("Ratio Color Sat. Limits Set: Lower = "+ srsd.getLowerRatio() +" Upper = "+ srsd.getUpperRatio());
    }
    
    /**
     * Removes all published clusters.
     */
    private void onDeleteAll() {
        data.deleteColors();
        if(this.geneClusterManager != null)
            this.geneClusterManager.deleteAllClusters();
        fireDataChanged();
        fireMenuChanged();
        addHistory("Deleted All Gene Clusters");
    }
    
    /**
     * Removes all published Experiment clusters
     */
    private void onDeleteAllExperimentClusters() {
        data.deleteExperimentColors();
        if(this.experimentClusterManager != null)
            this.experimentClusterManager.deleteAllClusters();
        fireDataChanged();
        fireMenuChanged();
        addHistory("Deleted All Experiment Clusters");
    }
    
    /**
     * Shows a thumbnail for the current viewer.
     * At the moment only <code>MultipleArrayCanvas</code> supports this feature.
     */
    private void onShowThumbnail() {
        IViewer viewer = getCurrentViewer();
        if (viewer instanceof MultipleArrayCanvas) {
            ((MultipleArrayCanvas)viewer).onShowThumbnail();
        }
    }
    
    /**
     * Shows the legend for HCLSupportTree coloring
     * This method will no longer be supported when the legend
     * is displayed as part of the HCLSupportTree image
     */
    private void onShowSupportTreeLegend() {
        JFrame legendFrame = new JFrame("Support Tree Legend");
        JPanel legendPanel = HCLSupportTree.getColorLegendPanel();
        legendFrame.getContentPane().add(legendPanel);
        legendFrame.setSize(200, 300);
        legendFrame.setLocation(300, 100);
        legendFrame.setVisible(true);
    }
    
    
    private void selectNode(DefaultMutableTreeNode node){
        this.tree.setSelectionPath(new TreePath(node.getPath()));
    }
    
    /**
     *  Allows node additions to tree from objects with a framework reference
     */
    private void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child){
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        model.insertNodeInto(child,parent, parent.getChildCount());
        this.treeScrollPane.getHorizontalScrollBar().setValue(0);
        fireDataChanged();
    }
    
    /***********
     *
     * Cluster saving and repository code
     *
     */
    
    /**
     * Stores a cluster with specified indices.
     */
    private Color storeCluster(int [] indices, Experiment experiment, int clusterType){
        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        TreePath path = this.tree.getSelectionPath();
        DefaultMutableTreeNode clusterNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        Object leafInfo = clusterNode.getUserObject();
        if(!(leafInfo instanceof LeafInfo))
            return null;
        if(path.getPathCount() < 3)
            return null;
        Cluster cluster;
        Color clusterColor = null;
        String clusterID = ((LeafInfo)clusterNode.getUserObject()).toString();
        DefaultMutableTreeNode algorithmNode = (DefaultMutableTreeNode)path.getPathComponent(2);
        String algorithmName = (String)algorithmNode.getUserObject();
        if(clusterType == ClusterRepository.GENE_CLUSTER){
            if(this.geneClusterRepository == null){
                this.geneClusterRepository = new ClusterRepository(data.getFeaturesSize(), framework, true);
                this.data.setGeneClusterRepository(this.geneClusterRepository);
            }
            cluster = geneClusterRepository.storeCluster(this.resultCount-1, algorithmName, clusterID, indices, clusterNode, experiment);
            if(cluster != null){
                clusterColor = cluster.getClusterColor();
                if(geneClusterManager == null){
                    this.geneClusterManager = new ClusterTable(this.geneClusterRepository, framework);
                    DefaultMutableTreeNode genesNode = new DefaultMutableTreeNode(new LeafInfo("Gene Clusters", this.geneClusterManager), false);
                    addNode(this.clusterNode, genesNode);
                } else{
                    geneClusterManager.onRepositoryChanged(geneClusterRepository);
                }
            }
            geneClusterRepository.printRepository();
        } else {
            if(this.experimentClusterRepository == null){
                this.experimentClusterRepository = new ClusterRepository(data.getFeaturesCount(), framework);
                this.data.setExperimentClusterRepository(this.experimentClusterRepository);
            }
            cluster = experimentClusterRepository.storeCluster(this.resultCount-1, algorithmName, clusterID, indices, clusterNode, experiment);
            if(cluster != null){
                clusterColor = cluster.getClusterColor();
                if(experimentClusterManager == null){
                    this.experimentClusterManager = new ClusterTable(this.experimentClusterRepository, framework);
                    DefaultMutableTreeNode experimentNode = new DefaultMutableTreeNode(new LeafInfo("Experiment Clusters", this.experimentClusterManager), false);
                    addNode(this.clusterNode, experimentNode);
                } else{
                    experimentClusterManager.onRepositoryChanged(experimentClusterRepository);
                }
            }
            experimentClusterRepository.printRepository();
        }
        
        if(cluster != null) {
            int serNum = cluster.getSerialNumber();
            String algName = cluster.getAlgorithmName();
            
            if(clusterType == Cluster.GENE_CLUSTER)
                addHistory("Save Gene Cluster: Serial #: "+String.valueOf(serNum)+", Algorithm: "+
                algName+", Cluster: "+clusterID);
            else
                addHistory("Save Experiment Cluster: Serial #: "+String.valueOf(serNum)+", Algorithm: "+
                algName+", Cluster: "+clusterID);
        }
        
        fireDataChanged();
        tree.repaint();
        return clusterColor;
    }
    
    /**
     * Stores cluster with provieded indices, allows storage if indices are a subset of
     * the displayed clusters (as in <code>HCLViewer</code>
     */
    private Color storeSubCluster(int [] indices, Experiment experiment, int clusterType){
        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        TreePath path = this.tree.getSelectionPath();
        DefaultMutableTreeNode clusterNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        Object leafInfo = clusterNode.getUserObject();
        if(!(leafInfo instanceof LeafInfo))
            return null;
        if(path.getPathCount() < 3)
            return null;
        Cluster cluster;
        Color clusterColor = null;
        String clusterID = ((LeafInfo)clusterNode.getUserObject()).toString();
        DefaultMutableTreeNode algorithmNode = (DefaultMutableTreeNode)path.getPathComponent(2);
        String algorithmName = (String)algorithmNode.getUserObject();
        if(clusterType == ClusterRepository.GENE_CLUSTER){
            if(this.geneClusterRepository == null){
                this.geneClusterRepository = new ClusterRepository(data.getFeaturesSize(), framework, true);
                this.data.setGeneClusterRepository(this.geneClusterRepository);
            }
            cluster = geneClusterRepository.storeSubCluster(this.resultCount-1, algorithmName, clusterID, indices, clusterNode, experiment);
            if(cluster != null){
                clusterColor = cluster.getClusterColor();
                if(geneClusterManager == null){
                    this.geneClusterManager = new ClusterTable(this.geneClusterRepository, framework);
                    DefaultMutableTreeNode genesNode = new DefaultMutableTreeNode(new LeafInfo("Gene Clusters", this.geneClusterManager), false);
                    addNode(this.clusterNode, genesNode);
                } else{
                    geneClusterManager.onRepositoryChanged(geneClusterRepository);
                }
            }
            geneClusterRepository.printRepository();
        } else {
            if(this.experimentClusterRepository == null){
                this.experimentClusterRepository = new ClusterRepository(data.getFeaturesCount(), framework);
                this.data.setExperimentClusterRepository(this.experimentClusterRepository);
            }
            cluster = experimentClusterRepository.storeSubCluster(this.resultCount-1, algorithmName, clusterID, indices, clusterNode, experiment);
            if(cluster != null){
                clusterColor = cluster.getClusterColor();
                if(experimentClusterManager == null){
                    this.experimentClusterManager = new ClusterTable(this.experimentClusterRepository, framework);
                    DefaultMutableTreeNode experimentNode = new DefaultMutableTreeNode(new LeafInfo("Experiment Clusters", this.experimentClusterManager), false);
                    addNode(this.clusterNode, experimentNode);
                } else{
                    experimentClusterManager.onRepositoryChanged(experimentClusterRepository);
                }
            }
            experimentClusterRepository.printRepository();
        }
        //Record history
        if(cluster != null) {
            int serNum = cluster.getSerialNumber();
            String algName = cluster.getAlgorithmName();
            
            if(clusterType == Cluster.GENE_CLUSTER)
                addHistory("Save Gene Cluster: Serial #: "+String.valueOf(serNum)+", Algorithm: "+
                algName+", Cluster: "+clusterID);
            else
                addHistory("Save Experiment Cluster: Serial #: "+String.valueOf(serNum)+", Algorithm: "+
                algName+", Cluster: "+clusterID);
        }
        
        fireDataChanged();
        tree.repaint();
        return clusterColor;
    }
    
    public boolean removeCluster(int [] indices, Experiment experiment, int clusterType){
        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        TreePath path = this.tree.getSelectionPath();
        DefaultMutableTreeNode clusterNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        Object leafInfo = clusterNode.getUserObject();
        boolean removed = false;
        if(!(leafInfo instanceof LeafInfo))
            return removed;
        if(path.getPathCount() < 3)
            return removed;
        String clusterID = ((LeafInfo)clusterNode.getUserObject()).toString();
        DefaultMutableTreeNode algorithmNode = (DefaultMutableTreeNode)path.getPathComponent(2);
        String algorithmName = (String)algorithmNode.getUserObject();
        
        if(clusterType == ClusterRepository.GENE_CLUSTER){
            if(this.geneClusterRepository == null)
                return removed;
            removed = this.geneClusterRepository.removeCluster(indices, algorithmName, clusterID);
            this.geneClusterManager.onRepositoryChanged(this.geneClusterRepository);
        } else {
            if(this.experimentClusterRepository == null)
                return removed;
            removed = this.experimentClusterRepository.removeCluster(indices, algorithmName, clusterID);
            this.experimentClusterManager.onRepositoryChanged(this.experimentClusterRepository);
        }
        if(removed)
            fireDataChanged();
        
        return removed;
    }
    
    
    public boolean removeSubCluster(int [] indices, Experiment experiment, int clusterType){
        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        TreePath path = this.tree.getSelectionPath();
        DefaultMutableTreeNode clusterNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        Object leafInfo = clusterNode.getUserObject();
        boolean removed = false;
        if(!(leafInfo instanceof LeafInfo))
            return removed;
        if(path.getPathCount() < 3)
            return removed;
        String clusterID = ((LeafInfo)clusterNode.getUserObject()).toString();
        DefaultMutableTreeNode algorithmNode = (DefaultMutableTreeNode)path.getPathComponent(2);
        String algorithmName = (String)algorithmNode.getUserObject();
        
        if(clusterType == ClusterRepository.GENE_CLUSTER){
            if(this.geneClusterRepository == null)
                return removed;
            removed = this.geneClusterRepository.removeSubCluster(indices, algorithmName, clusterID);
            this.geneClusterManager.onRepositoryChanged(this.geneClusterRepository);
        } else {
            if(this.experimentClusterRepository == null)
                return removed;
            removed = this.experimentClusterRepository.removeSubCluster(indices, algorithmName, clusterID);
            this.experimentClusterManager.onRepositoryChanged(this.experimentClusterRepository);
        }
        if(removed)
            fireDataChanged();
        
        return removed;
    }
    
    private void launchNewMAV(int [] indices, Experiment experiment, String label, int clusterType){
        MultipleArrayData newData;
        if(indices.length < 1){
            JOptionPane.showMessageDialog(this.getFrame(), "The selected cluster does not contain any members. The new viewer session has been aborted.", "New Session Abort", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if(clusterType == Cluster.GENE_CLUSTER){
            newData = this.data.getDataSubset(indices);
        } else {
            newData = this.data.getDataSubset(indices, experiment.getRowMappingArrayCopy());
        }
        Manager.createNewMultipleArrayViewer(newData, label);
        addHistory("Launch New MAV: "+label);
    }
    
    private void openClusterNode(String algorithmNode, String clusterNode){
        DefaultMutableTreeNode node = findNode(algorithmNode, clusterNode);
        if(node == null){
            return;
        }
        selectNode(node);
    }
    
    private DefaultMutableTreeNode findNode(String parent, String child){
        int childCount = this.analysisNode.getChildCount();
        DefaultMutableTreeNode curr = this.analysisNode;
        DefaultMutableTreeNode target = null;
        for(int i = 0; i < childCount; i++){
            curr = (DefaultMutableTreeNode)(analysisNode.getChildAt(i));
            Object userObject = curr.getUserObject();
            if(userObject instanceof String && ((String)userObject).equals(parent)){
                target = curr;
                break;
            }
            else if(userObject instanceof LeafInfo && (((LeafInfo)userObject).toString()).equals(parent)){
                target = curr;
                break;
            }
        }
        
        if(target == null)
            return null;
        
        childCount = target.getChildCount();
        
        for(int i = 0; i < childCount; i++){
            curr = (DefaultMutableTreeNode)(target.getChildAt(i));
            Object userObject = curr.getUserObject();
            if(userObject instanceof String && ((String)userObject).equals(child)){
                target = curr;
                break;
            }
            else if(userObject instanceof LeafInfo && (((LeafInfo)userObject).toString()).equals(parent)){
                target = curr;
                break;
            }
        }
        
        if(target != curr)
            return null;
        
        return (DefaultMutableTreeNode)curr;
    }
    
    public DefaultMutableTreeNode getCurrentNode(){
        TreePath path = this.tree.getSelectionPath();
        if(path == null)
            return null;
        return (DefaultMutableTreeNode)path.getLastPathComponent();
    }
    
    public DefaultMutableTreeNode getNode(Object object) {
        return this.tree.getNode(object);
    }
    
    /**
     *  Handles new data load.  Vector contains ISlideData objects.
     */
    public void fireDataLoaded(ISlideData [] features, int dataType){
        if(features == null || features.length < 1)
            return;
        if(TMEV.getFieldNames() != null && this.data.getFeaturesCount() < 1){
            this.menubar.addLabelMenuItems(TMEV.getFieldNames());
            //add the experiment key vector that is longest
            this.menubar.addExperimentLabelMenuItems(getSlideNameKeyVectorUnion(features));
            this.menubar.addSortMenuItems(TMEV.getFieldNames());
            this.menubar.setLabelIndex(0);
            
            //pcahan
            if (TMEV.getDataType() == TMEV.DATA_TYPE_AFFY){
                this.menubar.addAffyFilterMenuItems();
            }
        }
        data.addFeatures(features);
        data.setDataType(dataType);
        // if we have field names and data is not loaded
        //if(TMEV.getDataType() == TMEV.DATA_TYPE_AFFY)
        //    this.menubar.addAffyFilterMenuItems();
        
        // pcahan - convoluted but it works
        if ( (TMEV.getDataType() == TMEV.DATA_TYPE_AFFY) &&
        (data.getDataType() == IData.DATA_TYPE_AFFY_ABS) &&
        (!this.menubar.get_affyNormAdded())) {
            this.menubar.addAffyNormMenuItems();
        }
        
        setMaxCY3AndCY5();
        systemEnable(TMEV.DATA_AVAILABLE);
        fireMenuChanged();
        fireDataChanged();
        fireHeaderChanged();
        
        // record it for history's sake
        String [] featureNames = new String[features.length];
        for(int i = 0; i < features.length; i++){
            featureNames[i] = features[i].getSlideFileName();
            if(i == 0)
                addHistory("Load Data File: "+featureNames[i]);
            else {
                if(featureNames[i].equals(featureNames[i-1]))
                    break;
                addHistory("Load Data File: "+featureNames[i]);
            }
        }
        if(features.length > 1)
            addHistory(features.length+" experiments loaded.");
        else
            addHistory("1 experiment loaded.");
            
        if(features.length > 0)
                addHistory(features[0].getSize()+"genes loaded.");
    }
    
    
    
    /**
     * Returns the key vector for the sample with the longest sample name key list
     */
    private Vector getSlideNameKeyVectorUnion(ISlideData [] features) {
        Vector keyVector;
        Vector fullKeyVector = new Vector();
        String key;
        for( int i = 0; i < features.length; i++) {
            keyVector = features[i].getSlideDataKeys();
            for(int j = 0; j < keyVector.size(); j++) {
                key = (String)(keyVector.elementAt(j));
                if(!fullKeyVector.contains(key))
                    fullKeyVector.addElement(key);
            }
        }
        return fullKeyVector;
    }
    
    /**
     *  Loads data using <code>SuperExpressionFileLoader</code>.
     */
    private void loadData(){
        SuperExpressionFileLoader loader = new SuperExpressionFileLoader(this);
        
        //Add time node to the analysis node
        Date date = new Date(System.currentTimeMillis());
        DateFormat format = DateFormat.getDateTimeInstance();
        format.setTimeZone(TimeZone.getDefault());
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(format.format(date));
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        treeModel.insertNodeInto(node, analysisNode, analysisNode.getChildCount());
    }
    
    /**
     * Returns specfied the cluster repository, possibly null
     */
    protected ClusterRepository getClusterRepository(int clusterType){
        if(clusterType == Cluster.GENE_CLUSTER)
            return this.geneClusterRepository;
        else
            return this.experimentClusterRepository;
    }
    
    /**
     *  Returns the <CODE>ResultTree</CODE> object
     */
    protected ResultTree getResultTree() {
        return this.tree;
    }
    
    
    
    /**
     * The listener to listen to mouse, action, tree, keyboard and window events.
     */
    private class EventListener extends MouseAdapter implements ActionListener, TreeSelectionListener, KeyListener, WindowListener, java.io.Serializable {
        
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals(ActionManager.CLOSE_COMMAND)) {
                onClose();
            } else if (command.equals(ActionManager.LOAD_FILE_COMMAND)) {
                onLoadFile();
            } else if (command.equals(ActionManager.LOAD_EXPRESSION_COMMAND)) {
                //onLoadExpressionFile();
            } else if (command.equals(ActionManager.LOAD_DIRECTORY_COMMAND)) {
                onLoadDirectory();
            } else if (command.equals(ActionManager.LOAD_COMMAND)) {
                loadData();
            }else if (command.equals(ActionManager.LOAD_CLUSTER_COMMAND)) {
                onLoadCluster();
            } else if (command.equals(ActionManager.LOAD_STANFORD_COMMAND)) {
                onLoadStanford();
            } else if (command.equals(ActionManager.LOAD_DB_COMMAND)) {
                onLoadDatabase();
            } else if(command.equals(ActionManager.TOGGLE_ABBR_EXPT_NAMES_CMD)) {
                data.toggleExptNameLength();
                fireDataChanged();
                fireMenuChanged();
                fireHeaderChanged();
                doViewLayout();
            } else if (command.equals(ActionManager.DISPLAY_LABEL_CMD)) {
                onLabelChanged((Action)event.getSource());
            } else if (command.equals(ActionManager.DISPLAY_EXPERIMENT_LABEL_CMD)) {
                onExperimentLabelChanged((Action)event.getSource());
            } else if (command.equals(ActionManager.ADD_NEW_EXPERIMENT_LABEL_CMD)) {
                onExperimentLabelAdded();
            } else if (command.equals(ActionManager.DISPLAY_10X10_CMD)) {
                onElementSizeChanged(10, 10);
            } else if (command.equals(ActionManager.DISPLAY_20X5_CMD)) {
                onElementSizeChanged(20,  5);
            } else if (command.equals(ActionManager.DISPLAY_50X10_CMD)) {
                onElementSizeChanged(50, 10);
            } else if (command.equals(ActionManager.DISPLAY_5X2_CMD)) {
                onElementSizeChanged( 5,  2);
            } else if (command.equals(ActionManager.DISPLAY_OTHER_CMD)) {
                onElementSizeChanged();
            } else if (command.equals(ActionManager.GREEN_RED_COLOR_SCHEME_CMD)){
                onColorSchemeChange(IDisplayMenu.GREEN_RED_SCHEME);
            } else if (command.equals(ActionManager.BLUE_YELLOW_COLOR_SCHEME_CMD)){
                onColorSchemeChange(IDisplayMenu.BLUE_YELLOW_SCHEME);
            } else if (command.equals(ActionManager.CUSTOM_COLOR_SCHEME_CMD)){
                onColorSchemeChange(IDisplayMenu.CUSTOM_COLOR_SCHEME);
            }else if (command.equals(ActionManager.COLOR_GRADIENT_CMD)){
                onColorGradientChange(((javax.swing.JCheckBoxMenuItem)(event.getSource())).isSelected());
            } else if (command.equals(ActionManager.DISPLAY_GREEN_RED_CMD)) {
                onPaletteStyleChanged(IDisplayMenu.GREENRED);
            } else if (command.equals(ActionManager.DISPLAY_GR_RATIO_SPLIT_CMD)) {
                onPaletteStyleChanged(IDisplayMenu.RATIOSPLIT);
            } else if (command.equals(ActionManager.DISPLAY_GR_OVERLAY_CMD)) {
                onPaletteStyleChanged(IDisplayMenu.OVERLAY);
            } else if (command.equals(ActionManager.DISPLAY_TRACING_CMD)) {
                onTracing();
            } else if (command.equals(ActionManager.DISPLAY_GR_SCALE_CMD)) {
                onGRScale();
            } else if (command.equals(ActionManager.DISPLAY_USE_ANTIALIASING_CMD)) {
                onAntiAliasing();
            } else if (command.equals(ActionManager.DISPLAY_DRAW_BORDERS_CMD)) {
                onDrawBorders();
            } else if (command.equals(ActionManager.SORT_BY_LOCATION_CMD)) {
                onSort(SlideDataSorter.SORT_BY_LOCATION);
            } else if (command.equals(ActionManager.SORT_BY_RATIO_CMD)) {
                onSort(SlideDataSorter.SORT_BY_RATIO);
            } else if (command.equals(ActionManager.SORT_LABEL_CMD)) {
                onSort((Action)event.getSource());
            } else if (command.equals(ActionManager.SYSTEM_INFO_CMD)) {
                onSystemInfo();
            } else if (command.equals(ActionManager.DEFAULT_DISTANCES_CMD)) {
                onDefaultDistance();
            } else if (command.equals(ActionManager.TOTAL_INTENSITY_CMD)) {
                onNormalize(ISlideData.TOTAL_INTENSITY);
            } else if (command.equals(ActionManager.LINEAR_REGRESSION_CMD)) {
                onNormalize(ISlideData.LINEAR_REGRESSION);
            } else if (command.equals(ActionManager.RATIO_STATISTICS_CMD)) {
                onNormalize(ISlideData.RATIO_STATISTICS_99);
            } else if (command.equals(ActionManager.ITERATIVE_LOG_CMD)) {
                onNormalize(ISlideData.ITERATIVE_LOG);
            } else if (command.equals(ActionManager.TOTAL_INTENSITY_LIST_CMD)) {
                onNormalizeList(ISlideData.TOTAL_INTENSITY_LIST);
            } else if (command.equals(ActionManager.LINEAR_REGRESSION_LIST_CMD)) {
                onNormalizeList(ISlideData.LINEAR_REGRESSION_LIST);
            } else if (command.equals(ActionManager.RATIO_STATISTICS_LIST_CMD )) {
                onNormalizeList(ISlideData.RATIO_STATISTICS_99_LIST);
            } else if (command.equals(ActionManager.ITERATIVE_LOG_LIST_CMD)) {
                onNormalizeList(ISlideData.ITERATIVE_LOG_LIST);
            } else if (command.equals(ActionManager.NO_NORMALIZATION_CMD)) {
                onNormalize(ISlideData.NO_NORMALIZATION);
            } else if (command.equals(ActionManager.SAVE_IMAGE_COMMAND)) {
                onSaveImage();
            } else if (command.equals(ActionManager.PRINT_IMAGE_COMMAND)) {
                onPrintImage();
            } else if (command.equals(ActionManager.ANALYSIS_COMMAND)) {
                onAnalysis((Action)event.getSource());
            } else if (command.equals(ActionManager.DEFAULT_DISTANCE_CMD)) {
                menubar.setDistanceFunction(Algorithm.DEFAULT);
            } else if (command.equals(ActionManager.PEARSON_CORRELATION_CMD)) {
                menubar.setDistanceFunction(Algorithm.PEARSON);
            } else if (command.equals(ActionManager.PEARSON_UNCENTERED_CMD)) {
                menubar.setDistanceFunction(Algorithm.PEARSONUNCENTERED);
            } else if (command.equals(ActionManager.PEARSON_SQUARED_CMD)) {
                menubar.setDistanceFunction(Algorithm.PEARSONSQARED);
            } else if (command.equals(ActionManager.COSINE_CORRELATION_CMD)) {
                menubar.setDistanceFunction(Algorithm.COSINE);
            } else if (command.equals(ActionManager.COVARIANCE_VALUE_CMD)) {
                menubar.setDistanceFunction(Algorithm.COVARIANCE);
            } else if (command.equals(ActionManager.EUCLIDEAN_DISTANCE_CMD)) {
                menubar.setDistanceFunction(Algorithm.EUCLIDEAN);
            } else if (command.equals(ActionManager.AVERAGE_DOT_PRODUCT_CMD)) {
                menubar.setDistanceFunction(Algorithm.DOTPRODUCT);
            } else if (command.equals(ActionManager.MANHATTAN_DISTANCE_CMD)) {
                menubar.setDistanceFunction(Algorithm.MANHATTAN);
            } else if (command.equals(ActionManager.MUTUAL_INFORMATION_CMD)) {
                menubar.setDistanceFunction(Algorithm.MUTUALINFORMATION);
            } else if (command.equals(ActionManager.SPEARMAN_RANK_CORRELATION_CMD)) {
                menubar.setDistanceFunction(Algorithm.SPEARMANRANK);
            } else if (command.equals(ActionManager.KENDALLS_TAU_CMD)) {
                menubar.setDistanceFunction(Algorithm.KENDALLSTAU);
            } else if (command.equals(ActionManager.ABSOLUTE_DISTANCE_CMD)) {
                menubar.setDistanceAbsolute(((AbstractButton)event.getSource()).isSelected());
            } else if (command.equals(ActionManager.DELETE_NODE_CMD)) {
                onDeleteNode();
            } else if (command.equals(ActionManager.SET_LOWER_CUTOFFS_CMD)) {
                onSetLowerCutoffs();
            } else if (command.equals(ActionManager.SET_PERCENTAGE_CUTOFFS_CMD)) {
                onSetPercentageCutoffs();
            } else if (command.equals(ActionManager.USE_PERCENTAGE_CUTOFFS_CMD)) {
                onUsePercentageCutoffs((AbstractButton)event.getSource());
            } else if (command.equals(ActionManager.USE_LOWER_CUTOFFS_CMD)) {
                onUseLowerCutoffs((AbstractButton)event.getSource());
            }
            
            // pcahan
            /*else if (command.equals(ActionManager.SET_DETECTION_FILTER_CMD)) {
                onSetDetectionFilter();
            } else if (command.equals(ActionManager.SET_FOLD_FILTER_CMD)) {
                onSetFoldFilter();
            } else if (command.equals(ActionManager.USE_DETECTION_FILTER_CMD)) {
                onUseDetectionFilter( (AbstractButton) event.getSource());
            } else if (command.equals(ActionManager.USE_FOLD_FILTER_CMD)) {
                onUseFoldFilter( (AbstractButton) event.getSource());
            }
             */
            // pcahan
            else if (command.equals(ActionManager.SET_DETECTION_FILTER_CMD)) {
                onSetDetectionFilter();
            } else if (command.equals(ActionManager.SET_FOLD_FILTER_CMD)) {
                onSetFoldFilter();
            } else if (command.equals(ActionManager.USE_DETECTION_FILTER_CMD)) {
                onUseDetectionFilter( (AbstractButton) event.getSource());
            } else if (command.equals(ActionManager.USE_FOLD_FILTER_CMD)) {
                onUseFoldFilter( (AbstractButton) event.getSource());
            } else if (command.equals(ActionManager.DIVIDE_GENES_MEDIAN_CMD)) {
                onDivideGenesMedian();
            } else if (command.equals(ActionManager.DIVIDE_GENES_MEAN_CMD)) {
                onDivideGenesMean();
                
            } else if (command.equals(ActionManager.LOG2_TRANSFORM_CMD)) {
                onLog2Transform();
            } else if (command.equals(ActionManager.NORMALIZE_SPOTS_CMD)) {
                onNormalizeSpots();
            } else if (command.equals(ActionManager.DIVIDE_SPOTS_RMS_CMD)) {
                onDivideSpotsRMS();
            } else if (command.equals(ActionManager.DIVIDE_SPOTS_SD_CMD)) {
                onDivideSpotsSD();
            } else if (command.equals(ActionManager.MEAN_CENTER_SPOTS_CMD)) {
                onMeanCenterSpots();
            } else if (command.equals(ActionManager.MEDIAN_CENTER_SPOTS_CMD)) {
                onMedianCenterSpots();
            } else if (command.equals(ActionManager.DIGITAL_SPOTS_CMD)) {
                onDigitalSpots();
            } else if (command.equals(ActionManager.NORMALIZE_EXPERIMENTS_CMD)) {
                onNormalizeExperiments();
            } else if (command.equals(ActionManager.DIVIDE_EXPERIMENTS_RMS_CMD)) {
                onDivideExperimentsRMS();
            } else if (command.equals(ActionManager.DIVIDE_EXPERIMENTS_SD_CMD)) {
                onDivideExperimentsSD();
            } else if (command.equals(ActionManager.MEAN_CENTER_EXPERIMENTS_CMD)) {
                onMeanCenterExperiments();
            } else if (command.equals(ActionManager.MEDIAN_CENTER_EXPERIMENTS_CMD)) {
                onMedianCenterExperiments();
            } else if (command.equals(ActionManager.DIGITAL_EXPERIMENTS_CMD)) {
                onDigitalExperiments();
            } else if (command.equals(ActionManager.LOG10_TO_LOG2_CMD)) {
                onLog10toLog2();
            } else if (command.equals(ActionManager.ADJUST_INTENSITIES_0_CMD)) {
                onAdjustIntensities((AbstractButton)event.getSource());
            } else if (command.equals(ActionManager.SAVE_MATRIX_COMMAND)) {
                onSaveMatrix();
            } else if (command.equals(ActionManager.DISPLAY_SET_UPPER_LIMITS_CMD)) {
                onSetUpperLimits();
            } else if (command.equals(ActionManager.DISPLAY_SET_RATIO_SCALE_CMD)) {
                onSetRatioScale();
            } else if (command.equals(ActionManager.DELETE_ALL_EXPERIMENT_CLUSTERS_COMMAND)) {
                onDeleteAllExperimentClusters();
            } else if (command.equals(ActionManager.DELETE_ALL_COMMAND)) {
                onDeleteAll();
            } else if (command.equals(ActionManager.SHOW_THUMBNAIL_COMMAND)) {
                onShowThumbnail();
            } else if (command.equals(ActionManager.SHOW_SUPPORTTREE_LEGEND_COMMAND)) {
                onShowSupportTreeLegend(); //Accessible here -- temporarily
            } else if (command.equals(ActionManager.LOAD_ANALYSIS_COMMAND)) {
                loadAnalysis();
            } else if (command.equals(ActionManager.SAVE_ANALYSIS_COMMAND)) {
                saveAnalysis();
            } else if (command.equals(ActionManager.SAVE_ANALYSIS_AS_COMMAND)) {
                saveAnalysisAs();
            } else if (command.equals(ActionManager.NEW_SCRIPT_COMMAND)) {
                onNewScript();
            } else if (command.equals(ActionManager.LOAD_SCRIPT_COMMAND)) {
                onLoadScript();
            } else {
                System.out.println("unhandled command = " + command);
            }
        }
        
        public void valueChanged(TreeSelectionEvent event) {
            onNodeChanged(event);
        }
        
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        /**
         * Shows a popup menu for a selected navigation tree node.
         */
        private void maybeShowPopup(MouseEvent e) {
            
            if (!e.isPopupTrigger()) {
                return;
            }
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            if (selPath == null) {
                return;
            }
            tree.setSelectionPaths(new TreePath[] {selPath});
            JPopupMenu popup = null;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath.getLastPathComponent();
            Object userObject = node.getUserObject();
            if (userObject instanceof LeafInfo) {
                popup = ((LeafInfo)userObject).getJPopupMenu();
            }
            // adds the delete menu item for a custom node
            if (selPath.getPathCount() > 2) {
                if(node.getParent() == clusterNode)
                    return;
                if (popup == null) {
                    popup = new JPopupMenu();
                    popup.add(createDeleteMenuItem());
                } else {
                    if (!isContainsDeleteItem(popup)) {
                        popup.addSeparator();
                        popup.add(createDeleteMenuItem());
                    }
                }
            }
            if (popup != null) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        
        /**
         * Creates a delete menu item.
         */
        private JMenuItem createDeleteMenuItem() {
            JMenuItem menuItem = new JMenuItem("Delete");
            menuItem.setActionCommand(ActionManager.DELETE_NODE_CMD);
            menuItem.addActionListener(this);
            return menuItem;
        }
        
        /**
         * Checkes if node already contains the delete item.
         */
        private boolean isContainsDeleteItem(JPopupMenu popup) {
            Component[] components = popup.getComponents();
            for (int i=components.length; --i >= 0;) {
                if (components[i] instanceof JMenuItem) {
                    if (((JMenuItem)components[i]).getActionCommand().equals(ActionManager.DELETE_NODE_CMD)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        public void keyReleased(KeyEvent event) {}
        public void keyPressed(KeyEvent e) {}
        public void keyTyped(KeyEvent e) {}
        
        public void windowOpened(WindowEvent e) {}
        public void windowClosing(WindowEvent e) {
            onClose();
        }
        public void windowClosed(WindowEvent e) {}
        public void windowIconified(WindowEvent e) {}
        public void windowDeiconified(WindowEvent e) {}
        public void windowActivated(WindowEvent e) {}
        public void windowDeactivated(WindowEvent e) {}
    }
    
    /**
     * This <code>IFramework</code> implementation delegates
     * all its invokations to the outer class.
     */
    private class FrameworkImpl implements IFramework, java.io.Serializable {
        public static final long serialVersionUID = 10201020001L;
        
        public IData getData() {
            return MultipleArrayViewer.this.getData();
        }
        public AlgorithmFactory getAlgorithmFactory() {
            return MultipleArrayViewer.this.getAlgorithmFactory();
        }
        public IDisplayMenu getDisplayMenu() {
            return menubar.getDisplayMenu();
        }
        public IDistanceMenu getDistanceMenu() {
            return menubar.getDistanceMenu();
        }
        public Frame getFrame() {
            return MultipleArrayViewer.this.getFrame();
        }
        public void setContentLocation(int x, int y) {
            MultipleArrayViewer.this.setContentLocation(x, y);
        }
        public void displaySingleArrayViewer(int feature) {
            MultipleArrayViewer.this.displaySingleArrayViewer(feature);
        }
        public void displaySlideElementInfo(int feature, int probe) {
            MultipleArrayViewer.this.displaySlideElementInfo(feature, probe);
        }
        public String getStatusText() {
            return MultipleArrayViewer.this.getStatusText();
        }
        public void setStatusText(String text) {
            MultipleArrayViewer.this.setStatusText(text);
        }
        public Object getUserObject() {
            return MultipleArrayViewer.this.getUserObject();
        }
        public void setTreeNode(DefaultMutableTreeNode node){
            MultipleArrayViewer.this.selectNode(node);
        }
        
        public void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
            MultipleArrayViewer.this.addNode(parent, child);
        }
        
        public Color storeCluster(int[] indices, Experiment experiment, int clusterType){
            return MultipleArrayViewer.this.storeCluster(indices, experiment, clusterType);
        }
        
        public Color storeSubCluster(int[] indices, Experiment experiment, int clusterType){
            return MultipleArrayViewer.this.storeSubCluster(indices, experiment, clusterType);
        }
        
        public boolean removeSubCluster(int[] indices, Experiment experiment, int clusterType) {
            return MultipleArrayViewer.this.removeSubCluster(indices, experiment, clusterType);
        }
        
        public boolean removeCluster(int[] indices, Experiment experiment, int clusterType) {
            return MultipleArrayViewer.this.removeCluster(indices, experiment, clusterType);
        }
        
        public void launchNewMAV(int[] indices, Experiment experiment, String label, int clusterType){
            MultipleArrayViewer.this.launchNewMAV(indices, experiment, label, clusterType);
        }
        
        public void openClusterNode(String algorithmNode, String clusterID) {
            MultipleArrayViewer.this.openClusterNode(algorithmNode, clusterID);
        }
        
        public ClusterRepository getClusterRepository(int clusterType){
            return MultipleArrayViewer.this.getClusterRepository(clusterType);
        }
        
        /** Returns the currently selected node.
         */
        public DefaultMutableTreeNode getCurrentNode() {
            return MultipleArrayViewer.this.getCurrentNode();
        }
        
        /** Returns the result node containing the supplied object
         */
        public DefaultMutableTreeNode getNode(Object object) {
            return MultipleArrayViewer.this.getNode(object);
        }
        
        /** Adds string to history node
         */
        public void addHistory(String historyEvent) {
            MultipleArrayViewer.this.addHistory(historyEvent);
        }
        
        /** Returns the ResultTree object
         */
        public ResultTree getResultTree() {
            return MultipleArrayViewer.this.getResultTree();
        }
        
        /** Adds result to the ResultTree
         */
        public void addAnalysisResult(DefaultMutableTreeNode resultNode) {
            MultipleArrayViewer.this.addAnalysisResult(resultNode);
        }
        
    }
    
}

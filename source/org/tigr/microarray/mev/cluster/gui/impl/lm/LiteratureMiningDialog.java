/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * BNInitDialog.java
 *
 * Created on August 03, 2006
 */

package org.tigr.microarray.mev.cluster.gui.impl.lm;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterBrowser;
import org.tigr.microarray.mev.cluster.gui.impl.bn.BNConstants;
import org.tigr.microarray.mev.cluster.gui.impl.bn.BNDownloadManager;
import org.tigr.microarray.mev.cluster.gui.impl.bn.BNSupportDataFile;
import org.tigr.microarray.mev.cluster.gui.impl.bn.BNUpdateManager;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEEntrezSupportDataFile;
import org.tigr.microarray.mev.resources.IResourceManager;
import org.tigr.microarray.mev.resources.ResourcererAnnotationFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

/** Accumulates parameters for execution of LM analysis.
 * Based on EASEInitDialog
 * @author eleanora
 */
public class LiteratureMiningDialog extends AlgorithmDialog {
	/** Result when dialog is dismissed.
	 */
	private int result = JOptionPane.CANCEL_OPTION;
	ConfigPanel configPanel;
	PriorSelectionPanel priorsPanel;
	DiscretizingPanel discPanel;
	ClassNumPanel classnumPanel;
	XmlBifPanel useGoPanel;
	RunBNPanel runBNPanel;
	PopSelectionPanel popPanel;
	BootStrapPanel bootStrapPanel;
	ClusterBrowser browser;
	EventListener listener;
	BNParameterPanel bnParamPanel;
	AlphaPanel statParamsPanel;
	JTabbedPane tabbedPane;
	Font font;
	//String sep;
	Frame parent;
	IFramework framework;
	String kegg_sp = null;
	//RM specific attributes
	protected String arrayName, speciesName;
    protected Hashtable<String, Vector<String>> speciestoarrays;
    protected IResourceManager resourceManager;
    protected boolean useLoadedAnnotationFile = false;
    File annotationFile;
    
	/** Creates a new instance of LiteratureMiningDialog
	 * @param parent Parent Frame
	 * @param repository Cluster repository to construct <CODE>ClusterBrowser</CODE>
	 * @param annotationLabels Annotation types
	 */
	public LiteratureMiningDialog(IFramework frame, ClusterRepository repository, String [] annotationLabels, IResourceManager rm, String speciesName, String arrayName, Hashtable<String, Vector<String>> speciestoarrays) {
		super(frame.getFrame(), "LM: Literature Mining Analysis", true);
		this.parent = frame.getFrame();
		this.framework = frame;
		
		//RM related
		this.speciesName = speciesName;
        this.arrayName = arrayName;
        this.resourceManager = rm;
        this.speciestoarrays = speciestoarrays;
        
        //System.out.println("speciesName " + speciesName);
        //System.out.println("arrayName " + arrayName);
        
		font = new Font("Dialog", Font.BOLD, 12);
		listener = new EventListener();
		addWindowListener(listener);
		//Tabbed pane creation
		tabbedPane = new JTabbedPane();
		//config panel        
		configPanel = new ConfigPanel();        

		JPanel popNClusterPanel = new JPanel(new GridBagLayout());
		popNClusterPanel.setBackground(Color.white);
		popPanel = new PopSelectionPanel();
		browser = new ClusterBrowser(repository);

		//re-enable this panel when population selection from file is available
		//popNClusterPanel.add(popPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		popNClusterPanel.add(browser, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		tabbedPane.add("Population and Cluster Selection", popNClusterPanel);
		bnParamPanel = new BNParameterPanel(annotationLabels);        
		//TODO removed tabbedpane until other features are enabled that require rearranging
		//the dialog
		//tabbedPane.add("Annotation Parameters", bnParamPanel);
		//TODO removed tabbedpane until other features are enabled that require rearranging
		//the dialog
		//statParamsPanel = new AlphaPanel();
		//tabbedPane.add("Statistical Parameters", statParamsPanel);
		JPanel parameters = new JPanel(new GridBagLayout());
		parameters.setBackground(Color.white);
		//mode panel
		priorsPanel = new PriorSelectionPanel(!(repository == null || repository.isEmpty()));
		bootStrapPanel = new BootStrapPanel();
		discPanel = new DiscretizingPanel();
		classnumPanel = new ClassNumPanel();
		useGoPanel = new XmlBifPanel();
		// runBNPanel=new RunBNPanel();
		//tabbedPane.add("Running Bayesian Network Parameters", runBNPanel);
		//Removed - Raktim
		parameters.add(configPanel, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));       
		parameters.add(priorsPanel, new GridBagConstraints(0,1,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		//parameters.add(discPanel, new GridBagConstraints(0,2,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		//parameters.add(classnumPanel, new GridBagConstraints(0,3,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(useGoPanel, new GridBagConstraints(0,2,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		//parameters.add(runBNPanel, new GridBagConstraints(0,5,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(tabbedPane, new GridBagConstraints(0,3,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		addContent(parameters);
		setActionListeners(listener);
		if(repository == null || repository.isEmpty()) {
			Component comp = tabbedPane.getComponentAt(0);
			JPanel panel = (JPanel)comp;
			panel.removeAll();
			panel.validate();
			panel.setOpaque(false);
			panel.add(new JLabel("Empty Cluster Repository"), new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(15,0,10,0),0,0));
			panel.add(new JLabel("Please create a gene cluster and launch LM again."), new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
			tabbedPane.setSelectedIndex(0);
			okButton.setEnabled(false);
		}
		this.setSize(600,750);
	}
	/** Creates a new instance of LiteratureMiningDialog
	 * @param parent Parent Frame
	 * @param repository Cluster repository to construct <CODE>ClusterBrowser</CODE>
	 * @param annotationLabels Annotation types
	 */
	public LiteratureMiningDialog(Frame parent, String [] annotationLabels) {
		super(parent, " LM: Literature Mining Analysis", true);
		this.parent = parent;
		font = new Font("Dialog", Font.BOLD, 12);
		listener = new EventListener();
		addWindowListener(listener);
		//Tabbed pane creation
		tabbedPane = new JTabbedPane();

		//config panel     
		configPanel = new ConfigPanel();        

		JPanel popNClusterPanel = new JPanel(new GridBagLayout());
		popNClusterPanel.setBackground(Color.white);
		popPanel = new PopSelectionPanel();
		// browser = new ClusterBrowser(repository);
		JPanel emptyClusterPanel = new JPanel(new GridBagLayout());
		String text = "<center><b>Note: When running LM in script mode the cluster<br>";
		text += "under analysis is determined by the preceding algorithm<br>";
		text += "that feeds source data into LM.</center>";
		JTextPane textArea = new JTextPane();
		textArea.setEditable(false);
		textArea.setBackground(Color.lightGray);
		textArea.setContentType("text/html");
		textArea.setText(text);
		emptyClusterPanel.add(textArea, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		popNClusterPanel.add(popPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		popNClusterPanel.add(emptyClusterPanel, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		tabbedPane.add("Population and Cluster Selection", popNClusterPanel);
		//bnParamPanel = new BNParameterPanel(annotationLabels);
		//TODO removed tabbedpane until other features are enabled that require rearranging
		//the dialog
		//tabbedPane.add("Runing Bayesian Network Parameters", bnParamPanel);
		//statParamsPanel = new AlphaPanel();
		//TODO removed tabbedpane until other features are enabled that require rearranging
		//the dialog
		//tabbedPane.add("Statistical Parameters", statParamsPanel);
		JPanel parameters = new JPanel(new GridBagLayout());
		parameters.setBackground(Color.white);
		//mode paneli
		priorsPanel = new PriorSelectionPanel(true);
		bootStrapPanel = new BootStrapPanel();
		discPanel = new DiscretizingPanel();
		classnumPanel = new ClassNumPanel();
		useGoPanel = new XmlBifPanel();
		//runBNPanel=new RunBNPanel(); 
		//tabbedPane.add("Running Bayesian Network Parameters", runBNPanel);
		//Removed - Raktim
		parameters.add(configPanel, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));       
		parameters.add(priorsPanel, new GridBagConstraints(0,1,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		//parameters.add(discPanel, new GridBagConstraints(0,2,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		//parameters.add(classnumPanel, new GridBagConstraints(0,3,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(useGoPanel, new GridBagConstraints(0,2,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		//parameters.add(runBNPanel, new GridBagConstraints(0,5,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(tabbedPane, new GridBagConstraints(0,3,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		addContent(parameters);
		setActionListeners(listener);
		/*   if(repository == null || repository.isEmpty()) {
            Component comp = tabbedPane.getComponentAt(0);
            JPanel panel = (JPanel)comp;
            panel.removeAll();
            panel.validate();
            panel.setOpaque(false);
            panel.add(new JLabel("Empty Cluster Repository"), new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(15,0,10,0),0,0));
            panel.add(new JLabel("Only Annotation Survey is Enabled"), new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
            tabbedPane.setSelectedIndex(1);
        }
		 */
		this.setSize(600,750);
	}
	/** Shows the dialog.
	 * @return  */
	public int showModal() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		show();
		return result;
	}
	/** Resets dialog controls.
	 */
	private void resetControls(){
	}
	/** Indicates if mode is cluster analysis, if not mode is annotation survey.
	 * @return  */
	public boolean isClusterModeSelected(){
		return this.priorsPanel.litSourceCheckbox.isSelected();
	}
	/** Returns the cluster selected for analysis.
	 * @return  */
	public Cluster getSelectedCluster(){
		return this.browser.getSelectedCluster();
	}

	public boolean isPopFileModeSelected() {
		return popPanel.fileButton.isSelected();
	}

	/** Returns the population fille to load
	 */
	public String getPopulationFileName() {
		return this.popPanel.getPopFile();
	}
	/** Returns the name of the converter file selected.
	 * If none selected null is returned.
	 */
	public String getConverterFileName(){
		return bnParamPanel.getConverterFileName();
	}
	/** Returns the minimum clusters size if trimming result.
	 */
	public int getMinClusterSize() {
		String value = bnParamPanel.minClusterSizeField.getText();
		try {
			int size = Integer.parseInt(value);
			return size;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	/** Returns the base file location for LM file system
	 */
	public String getBaseFileLocation() {
		return configPanel.getBaseFileLocation();
		//return configPanel.defaultFileBaseLocation.getText().trim();
	}
	public int getNumberClass(){
		return this.classnumPanel.getNumClasses();
	}
	public int getNumberBin(){
		return this.discPanel.getNumLevels();
	}
	public boolean isLit(){
		return this.priorsPanel.litSourceCheckbox.isSelected();
	}
	public boolean isPPI(){
		return this.priorsPanel.ppiSourceCheckbox.isSelected();
	}
	public boolean isKEGG(){
		return this.priorsPanel.keggSourceCheckbox.isSelected();
	}
	public void setLit() {
		this.priorsPanel.litSourceCheckbox.setSelected(true);
	}
	public boolean isAll(){
		if(isLit() && isPPI() && isKEGG())
			return true;
		return false;
	}
	public boolean isLitAndKegg(){
		if(isLit() && isKEGG())
			return true;
		return false;
	}
	public boolean isPpiAndKegg(){
		if(isKEGG() && isPPI() && !isLit())
			return true;
		return false;
	}
	public boolean isBoth(){
		if(isLit() && isPPI())
			return true;
		return false;
	}
	public boolean isNone(){
		if(!isLit() && !isPPI() && !isKEGG()){
			return true;
		}else
			return false;
	}
	public boolean useGoTerm(){
		return this.useGoPanel.useGoButton.isSelected();
	}
	public String numParents(){
		return this.runBNPanel.numParents();
	}
	//TODO gray out file selection
	public boolean isClusterSource(){
		return this.popPanel.dataButton.isSelected();
	}
	//public String getAnnotationField(){
	//return annotKeyPanel.getAnnotationKeyType();
	// }
	public int getNumIterations(){
		return this.bootStrapPanel.getNumIterations();
	}
	public float getConfThreshold(){
		return this.bootStrapPanel.getConfThreshold();
	}
	public boolean isBootstrapping(){
		return this.bootStrapPanel.isBootstrapping();
	}
	public String getKeggSpecies() {
		return kegg_sp;
	}
	
	/** Returns a list of file names corresponding to files mapping
	 * indices to annotation terms (themes).
	 */
	public String [] getAnnToGOFileList(){
		return this.bnParamPanel.getAnnToGOFileList();
	}

	//RM function
	 public File getAnnotationFile() {
	    	return annotationFile;
	 }
	 
	/** Contains mode controls. (anal. or survey)
	 */
	private class PriorSelectionPanel extends JPanel {
		private JCheckBox litSourceCheckbox;
		private JCheckBox ppiSourceCheckbox;
		private JCheckBox keggSourceCheckbox;
		/** Constructs a mode panel.
		 * @param haveClusters
		 */
		public PriorSelectionPanel(boolean haveClusters){
			super(new GridBagLayout());
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Network Priors Sources", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));

			litSourceCheckbox = new JCheckBox("Literature Mining",true);
			litSourceCheckbox.setFocusPainted(false);
			litSourceCheckbox.setBackground(Color.white);
			litSourceCheckbox.setHorizontalAlignment(JRadioButton.CENTER);
			litSourceCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					popPanel.setEnableControls(true);
				}
			});

			ppiSourceCheckbox = new JCheckBox("Protein-Protein Interactions");
			ppiSourceCheckbox.setToolTipText("Uses protein-protein interaction data to create a seed network.");
			ppiSourceCheckbox.setFocusPainted(false);
			ppiSourceCheckbox.setBackground(Color.white);
			ppiSourceCheckbox.setHorizontalAlignment(JRadioButton.CENTER);
			ppiSourceCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					popPanel.setEnableControls(false);
				}
			});

			keggSourceCheckbox = new JCheckBox("KEGG Interactions");
			keggSourceCheckbox.setToolTipText("Uses KEGG pathway interactions to create a seed network.");
			keggSourceCheckbox.setFocusPainted(false);
			keggSourceCheckbox.setBackground(Color.white);
			keggSourceCheckbox.setHorizontalAlignment(JRadioButton.CENTER);
			keggSourceCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					popPanel.setEnableControls(true);
				}
			});
			
			ppiSourceCheckbox.setSelected(false);
			ppiSourceCheckbox.setEnabled(false);
			litSourceCheckbox.setEnabled(true);
			keggSourceCheckbox.setEnabled(true);
			
			add(litSourceCheckbox, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
			add(keggSourceCheckbox, new GridBagConstraints(1,0,1,1,1.0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
			add(ppiSourceCheckbox, new GridBagConstraints(2,0,1,1,1.0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
		}
	}
	/** Contains mode controls. (anal. or survey)
	 */
	private class DiscretizingPanel extends JPanel {
		private JTextField numLevelsField;
		private JLabel numLevelsLabel;
		/** Constructs a mode panel.
		 * @param haveClusters
		 */
		public DiscretizingPanel(){
			super(new GridBagLayout());
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Discretizing Expression Values", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
			numLevelsField = new JTextField("3", 1);
			numLevelsField.setBackground(Color.white);
			numLevelsLabel = new JLabel(" Number of Levels");
			numLevelsLabel.setBackground(Color.white);
			numLevelsField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					popPanel.setEnableControls(true);
				}
			});

			add(numLevelsLabel, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,15,0),0,0));
			add(numLevelsField, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL, new Insets(0,0,15,0),0,0));
		}
		public int getNumLevels(){return new Integer(numLevelsField.getText()).intValue();}
	}    
	/** 
	 */
	private class ClassNumPanel extends JPanel {
		private JTextField numClassesField;
		private JLabel numClassesLabel;
		/** Constructs a mode panel.
		 * @param haveClusters
		 */
		public ClassNumPanel(){
			super(new GridBagLayout());
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Sample Classification", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
			numClassesField = new JTextField("2", 2);
			numClassesField.setBackground(Color.white);
			numClassesLabel = new JLabel(" Number of Sample Classes");
			numClassesLabel.setBackground(Color.white);
			numClassesField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					popPanel.setEnableControls(true);
				}
			});

			add(numClassesLabel, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,15,0),0,0));
			add(numClassesField, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL, new Insets(0,0,15,0),0,0));
		}
		public int getNumClasses(){return new Integer(numClassesField.getText()).intValue();}
	}    
	/** Contains mode controls. (anal. or survey)
	 */
	private class BootStrapPanel extends JPanel {
		private JTextField numIterationsField;
		private JLabel numIterationsLabel; 
		private JTextField confThresholdField;
		private JLabel confThresholdLabel;
		private JCheckBox isBootstrappingCheckbox;
		private JLabel isBootStrappingLabel;
		private JLabel bootstrappingNotAvailable;
		/** Constructs a mode panel.
		 * @param haveClusters
		 */
		public BootStrapPanel(){
			super(new GridBagLayout());
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Bootstrapping", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
			isBootstrappingCheckbox = new JCheckBox();
			isBootstrappingCheckbox.setSelected(false);
			isBootstrappingCheckbox.setEnabled(true);
			isBootstrappingCheckbox.setBackground(Color.white);
			isBootStrappingLabel = new JLabel(" Bootstrapping");
			isBootStrappingLabel.setBackground(Color.white);
			isBootstrappingCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					bootStrapPanel.setEnableControls(isBootstrappingCheckbox.isSelected());
				}
			});
			numIterationsField = new JTextField("100", 4);
			numIterationsField.setBackground(Color.white);
			numIterationsLabel = new JLabel(" Number of Iterations");
			numIterationsLabel.setBackground(Color.white);

			confThresholdField = new JTextField(".1", 2);
			confThresholdField.setBackground(Color.white);
			confThresholdLabel = new JLabel(" Confidence Threshold");
			confThresholdLabel.setBackground(Color.white);

			bootstrappingNotAvailable = new JLabel("Bootstrapping is not yet available");
			bootstrappingNotAvailable.setForeground(Color.red);
			isBootstrappingCheckbox.setEnabled(false);
			numIterationsField.setEnabled(false);
			confThresholdField.setEnabled(false);
			isBootStrappingLabel.setForeground(Color.gray);
			numIterationsLabel.setForeground(Color.gray);
			confThresholdLabel.setForeground(Color.gray);
			add(bootstrappingNotAvailable, 	new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,15,0),0,0));
			add(isBootstrappingCheckbox, 	new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,15,0),0,0));
			add(isBootStrappingLabel, 		new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL, new Insets(0,0,15,0),0,0));
			add(numIterationsField, 		new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL, new Insets(0,0,15,0),0,0));
			add(numIterationsLabel, 		new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,15,0),0,0));
			add(confThresholdField, 		new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL, new Insets(0,0,15,0),0,0));
			add(confThresholdLabel, 		new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,15,0),0,0));
		}
		public void setEnableControls(boolean enableControls){
			numIterationsField.setEnabled(enableControls);
			confThresholdField.setEnabled(enableControls);
		}
		public int getNumIterations(){return new Integer(numIterationsField.getText()).intValue();}
		public float getConfThreshold(){return new Float(confThresholdField.getText()).floatValue();}
		public boolean isBootstrapping(){return isBootstrappingCheckbox.isSelected();}
	}    
	private class PopSelectionPanel extends ParameterPanel {
		JRadioButton fileButton;
		JRadioButton dataButton;
		JTextField popField;
		JButton browseButton;
		JLabel fileLabel;
		public PopSelectionPanel() {
			super("Population Selection");
			setLayout(new GridBagLayout());
			ButtonGroup bg = new ButtonGroup();
			fileButton = new JRadioButton("Population from File", true);
			fileButton.setBackground(Color.white);
			fileButton.setFocusPainted(false);
			bg.add(fileButton);
			fileButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					browseButton.setEnabled(fileButton.isSelected());
					popField.setEnabled(fileButton.isSelected());
					popField.setBackground(Color.white);
					fileLabel.setEnabled(fileButton.isSelected());
				}
			});
			dataButton = new JRadioButton("Population from Current Viewer");
			dataButton.setBackground(Color.white);
			dataButton.setFocusPainted(false);
			bg.add(dataButton);
			dataButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					browseButton.setEnabled(fileButton.isSelected());
					popField.setEnabled(fileButton.isSelected());
					popField.setBackground(Color.lightGray);
					fileLabel.setEnabled(fileButton.isSelected());
				}
			});

			browseButton = new JButton("File Browser");
			browseButton.setFocusPainted(false);
			browseButton.setPreferredSize(new Dimension(150, 25));
			browseButton.setSize(150, 25);
			browseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					JFileChooser chooser = new JFileChooser(TMEV.getFile("Data/"));
					chooser.setDialogTitle("Population File Selection");
					chooser.setMultiSelectionEnabled(false);
					if(chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION){
						updatePopField(chooser.getSelectedFile().getPath());
					}
				}
			});
			fileLabel = new JLabel("File: ");
			popField = new JTextField(25);
			//EH disabling 'select cluster from file' options until that feature is ready. 
			JLabel filePopNotAvailable = new JLabel("Population selection from file is not yet available.");
			filePopNotAvailable.setForeground(Color.red);
			fileLabel.setForeground(Color.gray);
			popField.setEnabled(false);
			fileButton.setEnabled(false);
			browseButton.setEnabled(false);
			dataButton.setSelected(true);
			add(filePopNotAvailable,new GridBagConstraints(0,0,3,1,1,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10,30,0,0), 0,0));
			add(fileButton, 		new GridBagConstraints(0,1,3,1,1,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10,30,0,0), 0,0));
			add(fileLabel, 			new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,30,0,0), 0,0));
			add(popField, 			new GridBagConstraints(1,2,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,0,0), 0,0));
			add(browseButton, 		new GridBagConstraints(2,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,25,0,20), 0,0));
			add(dataButton, 		new GridBagConstraints(0,3,3,1,1,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(15,30,20,0), 0,0));
		}
		private void setEnableControls(boolean enable) {
			fileButton.setEnabled(enable);
			dataButton.setEnabled(enable);
			popField.setEnabled(enable);
			browseButton.setEnabled(enable);
			fileLabel.setEnabled(enable);
			setOpaque(enable);
			tabbedPane.setEnabledAt(0, enable);
		}
		private void updatePopField(String file) {
			this.popField.setText(file);
		}
		private String getPopFile() {
			return popField.getText();
		}        
	}
	/*
    private class AnnotKeyPanel extends JPanel{
    	JComboBox fieldNamesBox;
    	public AnnotKeyPanel(String[] fieldNames){
	        super(new GridBagLayout());
	        	setLayout(new GridBagLayout());
	        	setBackground(Color.white);
            	this.fieldNamesBox = new JComboBox(fieldNames);
            	this.fieldNamesBox.setEditable(false);
    			setBackground(Color.white);
    			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Genbank accession field", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
    			add(new JLabel("Genbank Accession Annotation Field:  "), new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
    			add(this.fieldNamesBox, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
    	}
        public String getAnnotationKeyType(){
            return (String)this.fieldNamesBox.getSelectedItem();
        }
    }
	 */  
	private class XmlBifPanel extends JPanel{
		private JRadioButton useGoButton,useDFSButton;
		private ButtonGroup bGroup;
		public XmlBifPanel(){
			super(new GridBagLayout());
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			useGoButton = new JRadioButton("Use GO Terms to direct edges"); 
			useGoButton.setHorizontalAlignment(JRadioButton.CENTER);
			useGoButton.setBackground(Color.white);
			// GO Functionality is not working currently
			useGoButton.setEnabled(false);
			useDFSButton = new JRadioButton("Use Depth-First Search to direct edges",true);           	
			useDFSButton.setHorizontalAlignment(JRadioButton.CENTER);
			useDFSButton.setBackground(Color.white);
			bGroup=new ButtonGroup();
			bGroup.add(useDFSButton);
			bGroup.add(useGoButton);
			setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "How to direct edges for graph", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
			add(this.useDFSButton, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.useGoButton, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		}
		public boolean useGoTerms(){
			return useGoButton.isEnabled();
		}
	}
	private class RunBNPanel extends JPanel{
		private JLabel numLabel=new JLabel("Maximum Number of Parents:");
		private JLabel slabel=new JLabel("Search algorithm: Hill Climbing");
		private JLabel scorelabel=new JLabel("Scoring Scheme: BDeu");
		private JTextField nParents=new JTextField("3");
		public RunBNPanel(){
			super(new GridBagLayout());
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Running Bayesian Network Parameters ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
			add(this.slabel, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.scorelabel, new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.numLabel, new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.nParents, new GridBagConstraints(0,3,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		}
		public String numParents(){
			return nParents.getText();
		}
	}
	/** Contains annotation parameter controls.
	 */
	private class BNParameterPanel extends JPanel {
		JTextField converterFileField;
		JList fileList;
		JButton browserButton;
		JTextField minClusterSizeField;
		JComboBox fieldNamesBox;
		JList annFileList;
		Vector annVector;
		JButton removeButton;
		JCheckBox useAnnBox;
		JLabel fileLabel;
		/** Constructs a new BNParameterPanel
		 * @param fieldNames annotation types
		 */
		public BNParameterPanel(String [] fieldNames) {
			//Conversion File Panel
			JPanel convPanel = new JPanel(new GridBagLayout());
			convPanel.setBackground(Color.white);
			convPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Annotation Conversion File", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
			useAnnBox = new JCheckBox("use annotation converter", false);
			useAnnBox.setActionCommand("use-converter-command");
			useAnnBox.addActionListener(listener);
			useAnnBox.setBackground(Color.white);
			useAnnBox.setFocusPainted(false);
			useAnnBox.setEnabled(false);
			converterFileField = new JTextField(30);
			converterFileField.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.lightGray, Color.gray));
			converterFileField.setEnabled(false);
			converterFileField.setBackground(Color.lightGray);
			browserButton = new JButton("File Browser");
			browserButton.setActionCommand("converter-file-browser-command");
			browserButton.setFocusPainted(false);
			browserButton.setPreferredSize(new Dimension(150, 25));
			browserButton.setSize(150, 25);
			browserButton.addActionListener(listener);
			browserButton.setEnabled(false);
			JLabel converterNotAvailableLabel = new JLabel("Annotation conversion is not yet available");
			converterNotAvailableLabel.setForeground(Color.red);
			fileLabel = new JLabel("File :");
			fileLabel.setEnabled(false);
			convPanel.add(converterNotAvailableLabel,	new GridBagConstraints(0,0,2,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,0,10,5), 0,0));
			convPanel.add(useAnnBox, 					new GridBagConstraints(0,1,3,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
			convPanel.add(fileLabel, 					new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
			convPanel.add(this.browserButton, 			new GridBagConstraints(0,3,3,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL,new Insets(0,15,0,0),0,0));
			convPanel.add(this.converterFileField, 		new GridBagConstraints(1,2,2,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
			//Annotation file panel
			JPanel annPanel = new JPanel(new GridBagLayout());
			annPanel.setBackground(Color.white);
			annPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Gene Annotation / Gene Ontology Linking Files", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
			JLabel filesLabel = new JLabel("Files: ");
			annVector = new Vector();
			annFileList = new JList(new DefaultListModel());
			annFileList.setCellRenderer(new ListRenderer());
			annFileList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			JScrollPane annPane = new JScrollPane(annFileList);

			JButton  annButton = new JButton("Add Files");
			annButton.setActionCommand("ann-file-browser-command");
			annButton.addActionListener(listener);
			annButton.setFocusPainted(false);
			annButton.setPreferredSize(new Dimension(150, 25));
			annButton.setSize(150, 25);
			removeButton = new JButton("Remove Selected");
			removeButton.setActionCommand("remove-ann-file-command");
			removeButton.addActionListener(listener);
			removeButton.setFocusPainted(false);
			removeButton.setPreferredSize(new Dimension(150, 25));
			removeButton.setSize(150, 25);
			removeButton.setEnabled(false);

			JPanel fillPanel = new JPanel();
			fillPanel.setBackground(Color.white);
			//disabling annotation loading until feature is available
			JLabel annPanelNotAvailable = new JLabel("GO Annotation Linking is not yet available.");
			annPanelNotAvailable.setForeground(Color.red);
			annButton.setEnabled(false);
			annPane.setEnabled(false);
			filesLabel.setForeground(Color.gray);
			annPanel.add(annPanelNotAvailable,		new GridBagConstraints(0,0,2,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
			annPanel.add(fillPanel, 				new GridBagConstraints(0,1,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
			annPanel.add(annButton, 				new GridBagConstraints(1,1,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,0,10,5), 0,0));
			annPanel.add(removeButton, 				new GridBagConstraints(2,1,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0,5,10,0), 0,0));
			annPanel.add(filesLabel,			 	new GridBagConstraints(0,2,1,1,0.0,0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
			annPanel.add(annPane, 					new GridBagConstraints(1,2,2,1,0.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
			//sep = System.getProperty("file.separator");
			File file = new File(getBaseFileLocation()+"/Data/Convert/");
			String tempPath = file.getPath();
			Vector fileVector = new Vector();
			fileList = new JList(fileVector);
			if(file.exists()){
				String [] listFileNames = file.list();
				for(int i = 0; i < listFileNames.length; i++){
					File tempFile = new File(tempPath+BNConstants.SEP+listFileNames[i]);
					if(tempFile.isFile())
						fileVector.add(listFileNames[i]);
				}
				if(fileVector.size() > 0){
					converterFileField.setText(tempPath+BNConstants.SEP+((String)fileVector.elementAt(0)));
				}
			}

			minClusterSizeField = new JTextField(5);
			minClusterSizeField.setText("5");
			JPanel contentPanel = new JPanel(new GridBagLayout());
			JPanel bnFilePanel = new JPanel(new GridBagLayout());
			this.setLayout(new GridBagLayout());
			//annotKeyPanel = new AnnotKeyPanel(fieldNames);

			// this.add(annotKeyPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			this.add(convPanel, new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			this.add(annPanel, new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		}

		private void updateFileDirectoryField(){
			File file = new File((String)this.fileList.getSelectedValue());
			if(file == null)
				return;
			String tempPath = file.getParent();
			int fileIndex = this.fileList.getSelectedIndex();
			String fileName = (String)(this.fileList.getModel().getElementAt(this.fileList.getSelectedIndex()));
			this.converterFileField.setText(tempPath+BNConstants.SEP+fileName);
		}
		private void updateAnnFileList(File [] files){
			File file;
			for(int i = 0; i < files.length; i++){
				file = files[i];
				if(!((DefaultListModel) annFileList.getModel()).contains(file)){
					((DefaultListModel) annFileList.getModel()).addElement(file);
				}
			}
			annFileList.validate();
		}
		/** Returns the converter file name (or null if none)
		 */
		 public String getConverterFileName(){
			 if(this.useAnnBox.isSelected())
				 return converterFileField.getText();
			 return null;
		 }
		 /** Returns the annotation type string.
		  */
		 // public String getAnnotationKeyType(){
		 //   return annotKeyPanel.getAnnotationKeyType();
		 // }
		 private class BNListListener implements ListSelectionListener {
			 public void valueChanged(ListSelectionEvent listSelectionEvent) {
				 updateFileDirectoryField();
			 }
		 }
		 private void updateConverterFileField(String field){
			 this.converterFileField.setText(field);
		 }
		 /** Returns the list of annotation-theme mapping files.
		  */
		 public String [] getAnnToGOFileList(){
			 String [] fileNames = new String[((DefaultListModel) annFileList.getModel()).size()];
			 for(int i = 0; i < fileNames.length; i++){
				 fileNames[i] = ((File)(((DefaultListModel)annFileList.getModel()).elementAt(i))).getPath();
			 }
			 return fileNames;
		 }
		 public void removeSelectedFiles(){
			 int [] indices = annFileList.getSelectedIndices();
			 for(int i = 0; i < indices.length; i++){
				 // annFileList.remove(indices[i]);
				 ((DefaultListModel)annFileList.getModel()).removeElementAt(indices[i]);
			 }
			 if(annFileList.getModel().getSize() < 1){
				 this.removeButton.setEnabled(false);
				 okButton.setEnabled(false);
			 }
			 annFileList.validate();
		 }
		 private class ListRenderer extends DefaultListCellRenderer {
			 public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				 super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				 File file = (File) value;
				 setText(file.getName());
				 return this;
			 }
		 }
	}
	/** Contains statistical parameter controls.
	 */
	private class AlphaPanel extends JPanel{
		//Stats
		private JCheckBox fisherBox;
		private JCheckBox easeBox;
		//mult. corrections
		private JCheckBox bonferroniBox;
		private JCheckBox sidakBox;
		private JCheckBox bonferroniStepBox;
		private JCheckBox permBox;
		private JTextField permField;
		private JLabel permLabel;
		//Trim params
		private JCheckBox trimBox;
		private JCheckBox trimNBox;
		private JLabel trimNLabel;
		private JTextField trimNField;
		private JCheckBox trimPercentBox;
		private JLabel trimPercentLabel;
		private JTextField trimPercentField;

		/** Constucts a new AlphaPanel.
		 */
		public AlphaPanel(){
			super(new GridBagLayout());
			setBackground(Color.white);
			bootStrapPanel = new BootStrapPanel();  	
			//STAT PANEL
			JPanel statPanel = new JPanel(new GridBagLayout());
			statPanel.setBackground(Color.white);
			statPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Reported Statistic", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
			/*
            ButtonGroup bg = new ButtonGroup();
            fisherBox = new JCheckBox("Fisher Exact Probability", true);
            fisherBox.setBackground(Color.white);
            fisherBox.setFocusPainted(false);
            bg.add(fisherBox);
            easeBox = new JCheckBox("EASE Score", false);
            easeBox.setBackground(Color.white);
            easeBox.setFocusPainted(false);
            bg.add(easeBox);
            statPanel.add(fisherBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            statPanel.add(easeBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));

            //P-value Correction Panel
            JPanel correctionPanel = new JPanel(new GridBagLayout());
            correctionPanel.setBackground(Color.white);
            correctionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Multiplicity Corrections", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
            bonferroniBox = new JCheckBox("Bonferroni Correction", false);
            bonferroniBox.setBackground(Color.white);
            bonferroniBox.setFocusPainted(false);
            bonferroniStepBox = new JCheckBox("Bonferroni Step Down Correction", false);
            bonferroniStepBox.setBackground(Color.white);
            bonferroniStepBox.setFocusPainted(false);
            sidakBox = new JCheckBox("Sidak Method", false);
            sidakBox.setBackground(Color.white);
            sidakBox.setFocusPainted(false);
            permBox = new JCheckBox("Resampling Probability Analysis", false);
            permBox.setActionCommand("permutation-analysis-command");
            permBox.setBackground(Color.white);
            permBox.setFocusPainted(false);
            permBox.addActionListener(listener);
            //permBox.setEnabled(false);
            permField = new JTextField("1000", 10);
            permField.setBackground(Color.white);
            //permField.setEnabled(false);
            permLabel = new JLabel("Number of Permutations");
            permLabel.setBackground(Color.white);
            //permLabel.setEnabled(false);
            correctionPanel.add(bonferroniBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
            correctionPanel.add(sidakBox, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
            correctionPanel.add(bonferroniStepBox, new GridBagConstraints(0,1,2,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
            correctionPanel.add(permBox, new GridBagConstraints(0,2,2,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
            correctionPanel.add(permLabel, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,15,0),0,0));
            correctionPanel.add(permField, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.VERTICAL, new Insets(0,0,15,0),0,0));
            //Trim Panel
            JPanel trimPanel = new JPanel(new GridBagLayout());
            trimPanel.setBackground(Color.white);
            trimPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Trim Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
            trimBox = new JCheckBox("Trim Resulting Groups", false);
            trimBox.setActionCommand("trim-result-command");
            trimBox.addActionListener(listener);
            trimBox.setBackground(Color.white);
            trimBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            trimBox.setFocusPainted(false);
            bg = new ButtonGroup();
            trimNBox = new JCheckBox("Select Minimum Hit Number", true);
            trimNBox.setActionCommand("trim-result-command");
            trimNBox.addActionListener(listener);
            trimNBox.setEnabled(false);
            trimNBox.setBackground(Color.white);
            trimNBox.setFocusPainted(false);
            bg.add(trimNBox);
            trimNLabel = new JLabel("Min. Hits");
            trimNLabel.setBackground(Color.white);
            trimNLabel.setEnabled(false);
            trimNField = new JTextField("5", 10);
            trimNField.setEnabled(false);
            trimPercentBox = new JCheckBox("Select Minimum Hit Percentage", false);
            trimPercentBox.setActionCommand("trim-result-command");
            trimPercentBox.addActionListener(listener);
            trimPercentBox.setEnabled(false);
            trimPercentBox.setBackground(Color.white);
            trimPercentBox.setFocusPainted(false);
            bg.add(trimPercentBox);
            trimPercentLabel = new JLabel("Percent Hits");
            trimPercentLabel.setBackground(Color.white);
            trimPercentLabel.setEnabled(false);
            trimPercentField = new JTextField("5", 10);
            trimPercentField.setEnabled(false);
            trimPanel.add(trimBox, new GridBagConstraints(0,0,3,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));
            trimPanel.add(trimNBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            trimPanel.add(trimNLabel, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(0,20,0,15),0,0));
            trimPanel.add(trimNField, new GridBagConstraints(2,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
            trimPanel.add(trimPercentBox, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
            trimPanel.add(trimPercentLabel, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(10,20,0,15),0,0));
            trimPanel.add(trimPercentField, new GridBagConstraints(2,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE, new Insets(10,0,0,0),0,0));
			 */
			//Add panels to main panel
			//add(statPanel, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			//add(correctionPanel, new GridBagConstraints(0,1,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			//add(trimPanel, new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			discPanel = new DiscretizingPanel();
			classnumPanel = new ClassNumPanel();
			//TODO removed bootStrapPanel until bootstrapping is enabled in module
			add(discPanel, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			add(classnumPanel, new GridBagConstraints(0,1,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			//add(bootStrapPanel, new GridBagConstraints(0,2,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		}
		/** Indicates if permutations are selected.
		 */
		public boolean performPermutations(){
			return permBox.isSelected();
		}
		public void setEnablePermutations(){
			permLabel.setEnabled(permBox.isSelected());
			permField.setEnabled(permBox.isSelected());
		}
		public void validateTrimOptions(){
			if(this.trimBox.isSelected()){
				trimNBox.setEnabled(true);
				trimPercentBox.setEnabled(true);
				trimNLabel.setEnabled(trimNBox.isSelected());
				trimNField.setEnabled(trimNBox.isSelected());
				trimPercentLabel.setEnabled(!trimNBox.isSelected());
				trimPercentField.setEnabled(!trimNBox.isSelected());
			} else {
				trimNBox.setEnabled(false);
				trimPercentBox.setEnabled(false);
				trimNLabel.setEnabled(false);
				trimNField.setEnabled(false);
				trimPercentLabel.setEnabled(false);
				trimPercentField.setEnabled(false);
			}
		}
	}
	private class ConfigPanel extends ParameterPanel {

		JTextField defaultFileBaseLocation;
		JComboBox organismListBox;
		JComboBox arrayListBox;
		JLabel chooseOrg, chooseArray, browseLabel, statusLabel;
		JButton getBNSupportFileButton;
		
		public ConfigPanel() {
			super("Location of Support File(s)");
			setLayout(new GridBagLayout());
			JButton cngFilesButton = new JButton("Change");
			cngFilesButton.setActionCommand("select-file-base-command");
			cngFilesButton.addActionListener(listener);
			cngFilesButton.setToolTipText("<html>Select the directory where LM  files reside.</html>");
			//JLabel fileLocation = new JLabel("File(s) Location:");
			
			String _loc = TMEV.getSettingForOption(BNConstants.BN_LM_LOC_PROP);
			if(_loc == null || _loc.equals(""))
				_loc = TMEV.getDataPath();
			
			defaultFileBaseLocation = new JTextField(new File(_loc).getAbsolutePath());
			defaultFileBaseLocation.setEditable(true);
			
			//Borrowed from EASE for RM
			getBNSupportFileButton = new JButton("Download");
			getBNSupportFileButton.setActionCommand("download-support-file-command");
			getBNSupportFileButton.addActionListener(listener);
			getBNSupportFileButton.setToolTipText("<html>Downloads BN support files<br>for a selected species and array type.</html>");
			
			chooseOrg = new JLabel("Organism");
			chooseArray = new JLabel("Array Platform");
			browseLabel = new JLabel("or Browse for another BN file(s) location:");
			statusLabel = new JLabel("Click to download");
			
			if(speciestoarrays == null || speciestoarrays.size() == 0) {
				organismListBox = new JComboBox();
				organismListBox.addItem("No organisms listed");
				organismListBox.setEnabled(false);
				
				arrayListBox = new JComboBox();
				arrayListBox.addItem("No species listed");
				arrayListBox.setEnabled(false);
			} else {
				
				organismListBox = new JComboBox(new Vector<String>(speciestoarrays.keySet()));

				try {
					organismListBox.setSelectedItem(speciesName);
				} catch (NullPointerException npe) {
					// Show Error Dialog & return
					JOptionPane.showMessageDialog(
							parent, 
							"Organism information unavailable",
							"Aborting execution", JOptionPane.ERROR_MESSAGE);
					return;
				}
				arrayListBox = new JComboBox(speciestoarrays.get(organismListBox.getSelectedItem()));
				
				try {
					arrayListBox.setSelectedItem(arrayName);
				} catch (NullPointerException npe) {
					// Show Error Dialog & return
					JOptionPane.showMessageDialog(
							parent, 
							"Organism information unavailable",
							"Aborting execution", JOptionPane.ERROR_MESSAGE);
					return;
				}
				arrayListBox.setEnabled(true); 
			}
			
			arrayListBox.addActionListener(listener);
			arrayListBox.setActionCommand("array-selected-command");
			organismListBox.addActionListener(listener);
			organismListBox.setActionCommand("organism-selected-command");
			
			add(chooseOrg, 				new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 	GridBagConstraints.BOTH, new Insets(5, 30, 0, 0), 0, 0));
			add(chooseArray, 				new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 	GridBagConstraints.BOTH, new Insets(5, 30, 0, 0), 0, 0));
			add(organismListBox, 			new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 	GridBagConstraints.BOTH, new Insets(5, 30, 0, 0), 0, 0));
			add(arrayListBox, 				new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 	GridBagConstraints.BOTH, new Insets(5, 30, 0, 0), 0, 0));
			add(statusLabel, 				new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.EAST, 	GridBagConstraints.BOTH, new Insets(5, 25, 0, 20),0, 0));
			add(getBNSupportFileButton, 	new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.EAST, 	GridBagConstraints.BOTH, new Insets(5, 25, 0, 20), 0, 0));
			add(browseLabel, 				new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.WEST, 	GridBagConstraints.BOTH, new Insets(10, 30, 0, 0),0, 0));
			add(defaultFileBaseLocation, 	new GridBagConstraints(0, 3, 2, 1, 1, 0, GridBagConstraints.WEST, 	GridBagConstraints.BOTH, new Insets(10, 30, 5, 0), 0, 0));
			add(cngFilesButton, 	new GridBagConstraints(4, 3, 1, 1, 0, 0, GridBagConstraints.EAST, 	GridBagConstraints.BOTH, new Insets(5, 25, 5, 20), 0, 0));
			
			try {
				boolean b = resourceManager.fileIsInRepository(new BNSupportDataFile(organismListBox.getSelectedItem().toString(), arrayListBox.getSelectedItem().toString()));
				if(b) {
					getBNSupportFileButton.setText("Select This");
				} else {
					getBNSupportFileButton.setText("Download");
				}
			} catch (NullPointerException npe) {
				getBNSupportFileButton.setText("Download");
			}
			updateSelection();
			//
			//add(fileLocation, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
			//add(defaultFileBaseLocation,  new GridBagConstraints(1,0,1,1,2,0,GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));            
			//add(cngFilesButton, new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
		}
		public void selectFileSystem() {
			String startDir = defaultFileBaseLocation.getText();
			File file = new File(startDir);
			if(!file.exists()) {                
				file = TMEV.getFile("data/bn");
				if(file == null) {
					file = new File(System.getProperty("user.dir"));
				}
			}
			JFileChooser chooser = new JFileChooser(file);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(chooser.showOpenDialog(LiteratureMiningDialog.this) == JOptionPane.OK_OPTION) {
				String dir = chooser.getSelectedFile().getAbsolutePath().trim();
				if(dir.contains(" ")){
					JOptionPane.showMessageDialog(parent, 
							"Spaces are not allowed in Path. \n Selected a different location", 
							"LM Initialization: Illegal Char in Path", 
							JOptionPane.ERROR_MESSAGE);
					defaultFileBaseLocation.grabFocus();
					defaultFileBaseLocation.selectAll();
					//defaultFileBaseLocation.setCaretPosition(0);
					return;
				}
				defaultFileBaseLocation.setText(chooser.getSelectedFile().getAbsolutePath());
				TMEV.storeProperty(BNConstants.BN_LM_LOC_PROP, defaultFileBaseLocation.getText());
			}
		}
		public String getBaseFileLocation() {
			return defaultFileBaseLocation.getText();
		}
		
		//RM hanlder functions
		public void selectSpecies() {
			arrayListBox.removeAllItems();
			Vector<String> arraysForThisSpecies = speciestoarrays.get(organismListBox.getSelectedItem());
			for (int i = 0; i < arraysForThisSpecies.size(); i++) {
				arrayListBox.addItem(arraysForThisSpecies.elementAt(i));
			}
		}
		public void updateSelection() {
			if(arrayListBox.getSelectedItem() == null) {
				return;
			}
			String selectedOrganism = organismListBox.getSelectedItem().toString();
			String selectedArray = arrayListBox.getSelectedItem().toString();
			if(selectedOrganism != null && selectedArray != null) {
				if(resourceManager.fileIsInRepository(new BNSupportDataFile(selectedOrganism, selectedArray))) {
					statusLabel.setText("Click to Select");
					getBNSupportFileButton.setText("Select");
				} else {
					statusLabel.setText("Click to Download");
					getBNSupportFileButton.setText("Download");
				}
				getBNSupportFileButton.setEnabled(true);
			        try {
			        	ResourcererAnnotationFileDefinition def = new ResourcererAnnotationFileDefinition(speciesName, arrayName);
			        	annotationFile = resourceManager.getSupportFile(def, false);
			        } catch (SupportFileAccessError sfae) {
			        	//disable population from file button
			        	useLoadedAnnotationFile = false;
				        //popPanel.fileButton.setSelected(true);
				        //popPanel.preloadedAnnotationButton.setSelected(false);
				        //popPanel.preloadedAnnotationButton.setEnabled(false);
			        }
			} else {
				getBNSupportFileButton.setEnabled(false);
			}
		}

		private void onDownloadSupportFile() {
			try {
				BNSupportDataFile bnSuppFile = new BNSupportDataFile(organismListBox.getSelectedItem().toString(), arrayListBox.getSelectedItem().toString());
				if(bnSuppFile == null) {
					System.out.println("BNSuppFile obj is null");
				} else {
					System.out.println("bnSuppFile.isSingleFile(): " + bnSuppFile.isSingleFile());
					System.out.println("bnSuppFile.getUniqueName(): " + bnSuppFile.getUniqueName());
					try {
						System.out.println("bnSuppFile.getURL(): " + bnSuppFile.getURL().toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("bnSuppFile.isSingleFile(): " + bnSuppFile.isSingleFile());
				}
				File f = resourceManager.getSupportFile(bnSuppFile, true);
				System.out.println("FTP & unzipping Complete: " + f.getAbsolutePath());
				//TODO Remove hard coded path
				String srcDirPath = f.getAbsolutePath() + BNConstants.SEP + bnSuppFile.getUniqueName();
				File srcDir = new File(srcDirPath);
				String dstDirPath = System.getProperty("user.dir") + BNConstants.SEP + "data" + BNConstants.SEP + "BN_files" + BNConstants.SEP + bnSuppFile.getUniqueName();
				File dstDir = new File(dstDirPath);
				//Copy files to data directory
				try {
					Useful.copyDirectory(srcDir, dstDir);
				} catch (IOException ioe){
					ioe.printStackTrace();
				}
				//End Copying Files
				defaultFileBaseLocation.setText(dstDir.getAbsolutePath());
				TMEV.storeProperty(BNConstants.BN_LM_LOC_PROP, defaultFileBaseLocation.getText());
				getBNSupportFileButton.setText("Select This");
				statusLabel.setText("Selected");
				getBNSupportFileButton.setEnabled(false);
			} catch (SupportFileAccessError sfae) {
				statusLabel.setText("Failure");
				sfae.printStackTrace();
			} catch (NullPointerException npe) {
				statusLabel.setText("Failure");
				npe.printStackTrace();
				System.out.println("LMDialog.onDownloadSupportFile() - NullPointerException");
			}
		}
	}
	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	private class EventListener extends DialogListener implements ItemListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("use-converter-command")) {
				if(bnParamPanel.useAnnBox.isSelected()){
					bnParamPanel.browserButton.setEnabled(true);
					bnParamPanel.converterFileField.setEnabled(true);
					bnParamPanel.converterFileField.setBackground(Color.white);
					bnParamPanel.fileLabel.setEnabled(true);
				} else {
					bnParamPanel.browserButton.setEnabled(false);
					bnParamPanel.converterFileField.setEnabled(false);
					bnParamPanel.converterFileField.setBackground(Color.lightGray);
					bnParamPanel.fileLabel.setEnabled(false);
				}
			} else if (command.equals("converter-file-browser-command")){
				File convertFile = new File(getBaseFileLocation()+"/Data/Convert");
				JFileChooser chooser = new JFileChooser(convertFile);
				chooser.setDialogTitle("Annotation Converter Selection");
				chooser.setMultiSelectionEnabled(false);
				if(chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION){
					bnParamPanel.updateConverterFileField(chooser.getSelectedFile().getPath());
				}
				return;
			} else if (command.equals("ann-file-browser-command")){
				File classFile = new File(getBaseFileLocation()+"/Data/Class/");
				JFileChooser chooser = new JFileChooser(classFile);
				chooser.setDialogTitle("Annotation --> GO Term, File(s) Selection");
				chooser.setMultiSelectionEnabled(true);
				if(chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION){
					bnParamPanel.updateAnnFileList(chooser.getSelectedFiles());
					bnParamPanel.removeButton.setEnabled(true);
					okButton.setEnabled(true);
				}
			} else if (command.equals("remove-ann-file-command")){
				bnParamPanel.removeSelectedFiles();
				//    } else if (command.equals("permutation-analysis-command")){
				//        statParamsPanel.setEnablePermutations();
				//    } else if (command.equals("trim-result-command")){
				//        statParamsPanel.validateTrimOptions();
			} else if (command.equals("select-file-base-command")) {
				configPanel.selectFileSystem();
			} else if (command.equals("update-files-command")) {
				//TODO add an update manager like the one in EASE module
				BNUpdateManager manager = new BNUpdateManager((JFrame)parent,configPanel.getBaseFileLocation());
				manager.updateFiles();
			} else if (command.equals("ok-command")) {
				System.out.println("LIT Mining Dlg. OK Cmd");
				result = JOptionPane.OK_OPTION;
				//Check to see if user is connected to internet
				Hashtable repInfo = BNDownloadManager.getRepositoryInfoCytoscape();
		    	String codeBase = ((String)repInfo.get("cytoscape_webstart")).trim();
		    	String libDir = ((String)repInfo.get("cytoscape_lib_dir")).trim();
		    	String pluginsDir = ((String)repInfo.get("cytoscape_plugins_dir")).trim();
		    	
		    	if(codeBase == null || libDir == null) {
		    		JOptionPane.showMessageDialog(new JFrame(), "Internet Connection error or Error reading properties file, will try with default values", "Cytoscape may not launch", JOptionPane.ERROR_MESSAGE);
		    		return;
		    	}
				
		    	BNConstants.setCodeBaseLocation(codeBase);
		    	BNConstants.setLibDirLocation(libDir);
		    	BNConstants.setPluginsDirLocation(pluginsDir);
		    	
				// Validate if selected options have supporting file(s)
				String fileBase = configPanel.getBaseFileLocation();
				
				if(fileBase.contains(" ")){
					JOptionPane.showMessageDialog(parent, 
							"Spaces are not allowed in Path. \n Selected a different location", 
							"BN Initialization: Illegal Char in Path", 
							JOptionPane.ERROR_MESSAGE);
					configPanel.defaultFileBaseLocation.grabFocus();
					configPanel.defaultFileBaseLocation.selectAll();
					//configPanel.defaultFileBaseLocation.setCaretPosition(0);
					return;
				}
				
				if(isLit()){
					//Check if Lit File(s) exist
					if(!(new File(fileBase + BNConstants.SEP + BNConstants.RESOURCERER_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.RESOURCERER_FILE + " is missing",
								"LM Initialization: Missing File", JOptionPane.WARNING_MESSAGE);
						return;
					}

					if(!(new File(fileBase + BNConstants.SEP + BNConstants.ACCESSION_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.ACCESSION_FILE + " is missing",
								"LM Initialization: Missing File", JOptionPane.WARNING_MESSAGE);
						return;
					}

					if(!(new File(fileBase + BNConstants.SEP + BNConstants.GENE_DB_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.GENE_DB_FILE + " is missing",
								"LM Initialization: Missing File", JOptionPane.WARNING_MESSAGE);
						return;
					}

					if(!(new File(fileBase + BNConstants.SEP + BNConstants.PUBMED_DB_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.PUBMED_DB_FILE + " is missing",
								"LM Initialization: Missing File", JOptionPane.WARNING_MESSAGE);
						return;
					}
				}

				if(isPPI()) {
					if(!(new File(fileBase + BNConstants.SEP + BNConstants.PPI_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.PPI_FILE + " is missing",
								"LM Initialization: Missing File", JOptionPane.WARNING_MESSAGE);
						return;
					}
				}

				if(isKEGG()) {
					//Make sure if KEGG is selected as priors the files are downloaded if it doesnot exist 
					//Check if Species Name is available, if not prompt for it
					kegg_sp = null;
					//Array for KEGG supported oraganism
					//String kegg_org[] = new String[]{"Human", "Mouse", "Rat" };
					if(framework.getData().isAnnotationLoaded()) {
						kegg_sp = framework.getData().getChipAnnotation().getSpeciesName().trim();
						//JOptionPane pane = new JOptionPane(sp); JDialog dlg = pane.createDialog(new JFrame(), "Annotation Species is- "+ sp); dlg.show();
					}
					if(kegg_sp == null) {
						kegg_sp = (String)JOptionPane.showInputDialog(null, "Select a Species", "Annotation Unknown",
								JOptionPane.WARNING_MESSAGE, null, BNConstants.KEGG_ORG, BNConstants.KEGG_ORG[0]);

						//JOptionPane pane = new JOptionPane(sp); JDialog dlg = pane.createDialog(new JFrame(), "Dialog"); dlg.show();
					} else if(!isKeggOrgSupported(kegg_sp)) {
						//!sp.equalsIgnoreCase("Human") || !sp.equalsIgnoreCase("Mouse") || !sp.equalsIgnoreCase("Rat")) {
						if (JOptionPane.showConfirmDialog(new JFrame(),
								"Do you want to continue ? ", "Species " + kegg_sp + " not Supported for KEGG",
								JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
							return;
						} 
					}

					//Change species name to match KEGG file prefix
					if(kegg_sp.equalsIgnoreCase("Human"))
						kegg_sp="hsa";
					else if (kegg_sp.equalsIgnoreCase("Mouse"))
						kegg_sp="mmu";
					else if (kegg_sp.equalsIgnoreCase("Rat"))
						kegg_sp="rno";

					//System.out.println("User Dir: " + System.getProperty("user.dir"));
					//System.out.println("User fileBase: " + fileBase);
					//String keggFilebase = System.getProperty("user.dir") + BNConstants.SEP + "data" + BNConstants.SEP + "BN_files" + BNConstants.SEP + "kegg";
					if(!(new File(BNConstants.KEGG_FILE_BASE)).exists()){
						boolean success = (new File(BNConstants.KEGG_FILE_BASE)).mkdir();
						if (!success) {
							// Directory creation failed
							JOptionPane.showMessageDialog(
									parent, 
									"Failed to create directory",
									"Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					String keggFileName = kegg_sp + BNConstants.KEGG_FILE;
					if(!(new File(BNConstants.KEGG_FILE_BASE + BNConstants.SEP + keggFileName)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"KEGG file is missing, will try to download",
								"LM Initialization: Missing File", JOptionPane.ERROR_MESSAGE);

						//Download kegg file for species
						BNDownloadManager dwnMgr = new BNDownloadManager((JFrame)parent, BNConstants.KEGG_FILE_BASE, "Trying to Download KEGG File", keggFileName, false);
						if(!dwnMgr.updateFiles())
							return;
					}
				}
				
				if(isNone()) {
					JOptionPane.showMessageDialog(
							parent, 
							"Network Priors Source(s) not selected",
							"LM Initialization: Missing Selection", JOptionPane.WARNING_MESSAGE);
					return;
				}

				if(useGoTerm()) {
					if(!(new File(fileBase + BNConstants.SEP + BNConstants.GB_GO_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.GB_GO_FILE + " is missing",
								"LM Initialization: Missing File", JOptionPane.WARNING_MESSAGE);
						return;
					}
				}

				//Create "tmp" & "results" directories if they don't exist report 
				//problems if any encountered.
				if(!(new File(fileBase + BNConstants.SEP + BNConstants.RESULT_DIR)).exists()) {
					boolean success = (new File(fileBase + BNConstants.SEP + BNConstants.RESULT_DIR)).mkdir();
					if (!success) {
						// Directory creation failed
						JOptionPane.showMessageDialog(
								parent, 
								"Dir: " + 
								fileBase + BNConstants.SEP + BNConstants.RESULT_DIR + " cannot be created",
								"LM Initialization: Dir create error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					System.out.println("Dir: " + 
							fileBase + BNConstants.SEP + BNConstants.RESULT_DIR + " created successfully !!");
				}

				if(!(new File(fileBase + BNConstants.SEP + BNConstants.TMP_DIR)).exists()) {
					boolean success = (new File(fileBase + BNConstants.SEP + BNConstants.TMP_DIR)).mkdir();
					if (!success) {
						// Directory creation failed
						JOptionPane.showMessageDialog(
								parent, 
								"Dir: " + 
								fileBase + BNConstants.SEP + BNConstants.TMP_DIR + " cannot be created",
								"LM Initialization: Dir create error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					System.out.println("Dir: " + 
							fileBase + BNConstants.SEP + BNConstants.TMP_DIR + " created successfully !!");
				}
				if(isClusterModeSelected() && popPanel.fileButton.isSelected()) {
					String fileName = popPanel.popField.getText();
					if(fileName == null || fileName.equals("") || fileName.equals(" ")) {
						JOptionPane.showMessageDialog(parent, "You have selected to use a population file but have not "+
								"entered a file name.  \nPlease enter a file or use the file browser to select a file.", "LM Initialization: Missing Parameter", JOptionPane.WARNING_MESSAGE);
						tabbedPane.setSelectedIndex(0);
						popPanel.popField.grabFocus();
						popPanel.popField.selectAll();
						popPanel.popField.setCaretPosition(0);
						return;
					}
				}
				/*
                if(getAnnToGOFileList().length == 0) {
                    JOptionPane.showMessageDialog(parent, "You have not selected any gene annotation/gene ontology linking files. \n"+
                    "Please enter files or use the browser to select files.", "LM Initialization: Missing Parameter", JOptionPane.WARNING_MESSAGE);
                    tabbedPane.setSelectedIndex(1);
                    bnParamPanel.browserButton.grabFocus();
                    return;
                }
				 */
				if(bnParamPanel.useAnnBox.isSelected()) {
					String fileName = bnParamPanel.getConverterFileName();
					if( fileName == null || fileName.equals("") || fileName.equals(" ") ) {
						JOptionPane.showMessageDialog(parent, "You have selected to use an annotation conversion file but have not made a file selection.\n" +
								"Please enter a file name or browse to select a file.", "LM Initialization: Missing Parameter", JOptionPane.WARNING_MESSAGE);
						tabbedPane.setSelectedIndex(1);
						bnParamPanel.browserButton.grabFocus();
						return;
					}
				}                
				BNConstants.setBaseFileLocation(fileBase);
				TMEV.storeProperty(BNConstants.BN_LM_LOC_PROP, fileBase);
				//TMEV.setDataPath(fileBase);
				dispose();
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")){
				resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")){
				HelpWindow.launchBrowser(LiteratureMiningDialog.this, "LM Initialization Dialog");
			//RM related handlers
			} else if (command.equals("organism-selected-command")) {
				configPanel.selectSpecies();
				configPanel.updateSelection();
			} else if (command.equals("array-selected-command")) {
				configPanel.updateSelection();
			} else if (command.equals("download-support-file-command")) {
				configPanel.onDownloadSupportFile();
			}
		}
		
		/**
		 * Function to check if the organism is currently supported by KEGG files
		 * @param kegg_org
		 * @param sp
		 * @return
		 */
		private boolean isKeggOrgSupported(String sp) {
			for(int i=0; i < BNConstants.KEGG_ORG.length; i++){
				if(sp.equalsIgnoreCase(BNConstants.KEGG_ORG[i]))
					return true;
			}
			return false;
		}

		public void itemStateChanged(ItemEvent e) {
			//okButton.setEnabled(genes_box.isSelected() || cluster_box.isSelected());
		}
		public void windowClosing(WindowEvent e) {
			result = JOptionPane.CLOSED_OPTION;
			dispose();
		}
	}
	
	public static void main(String [] args) {
		String [] labels = new String [3];
		labels[0] = "TC#";
		labels[1] = "GB#";
		labels[2] = "Role";

		LiteratureMiningDialog eid = new LiteratureMiningDialog(new JFrame(), labels);
		eid.showModal();
	}
}

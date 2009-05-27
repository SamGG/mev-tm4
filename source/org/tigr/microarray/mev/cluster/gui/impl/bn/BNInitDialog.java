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
package org.tigr.microarray.mev.cluster.gui.impl.bn;
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
import javax.swing.JDialog;
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

import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterBrowser;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEEntrezSupportDataFile;
import org.tigr.microarray.mev.resources.IResourceManager;
import org.tigr.microarray.mev.resources.ResourcererAnnotationFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

/** Accumulates parameters for execution of BN analysis.
 * Based on EASEInitDialog
 * @author raktim
 */
public class BNInitDialog extends AlgorithmDialog {

	/** Result when dialog is dismissed.
	 */
	private int result = JOptionPane.CANCEL_OPTION;
	ConfigPanel configPanel;
	PriorSelectionPanel priorsPanel;
	DiscretizingPanel discPanel;
	ClassNumPanel classnumPanel;
	XmlBifPanel useGoPanel;
	RunBNPanel runBNPanel;
	//PopSelectionPanel popPanel;
	BootStrapPanel bootStrapPanel;
	ClusterBrowser browser;
	EventListener listener;
	BNParameterPanel bnParamPanel;
	JTabbedPane tabbedPane;
	Font font;
	String sep;
	Frame parent;
	IFramework framework;
	String searchAlgorithm = "TabuSearch"; //"HillClimber";
	String scoreType = "BAYES"; //"BDeu";
	boolean useArc = true;
	File fileDir = null;
	String kegg_sp = null;
	// RM specific attributes
	protected String arrayName, speciesName;
	protected Hashtable<String, Vector<String>> speciestoarrays;
	protected IResourceManager resourceManager;
	protected boolean useLoadedAnnotationFile = false;
	File annotationFile;

	/** Creates a new instance of BNInitDialog
	 * @param parent Parent Frame
	 * @param repository Cluster repository to construct <CODE>ClusterBrowser</CODE>
	 * @param annotationLabels Annotation types
	 */
	public BNInitDialog(IFramework frame, ClusterRepository repository, String [] annotationLabels, IResourceManager rm, String speciesName, String arrayName, Hashtable<String, Vector<String>> speciestoarrays) {
		super(frame.getFrame(), "BN: Bayes Network Analysis", true);
		this.parent = frame.getFrame(); //parent;
		this.framework = frame;

		// RM related
		this.speciesName = speciesName;
		this.arrayName = arrayName;
		this.resourceManager = rm;
		this.speciestoarrays = speciestoarrays;

		font = new Font("Dialog", Font.BOLD, 12);
		listener = new EventListener();
		addWindowListener(listener);

		//Tabbed pane creation
		tabbedPane = new JTabbedPane();

		//config panel        
		configPanel = new ConfigPanel();        

		JPanel popNClusterPanel = new JPanel(new GridBagLayout());
		popNClusterPanel.setBackground(Color.white);
		//popPanel = new PopSelectionPanel();
		browser = new ClusterBrowser(repository);

		//re-enable this panel when population selection from file is available
		//popNClusterPanel.add(popPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		popNClusterPanel.add(browser, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		tabbedPane.add("Population and Cluster Selection", popNClusterPanel);

		bnParamPanel = new BNParameterPanel(annotationLabels);        
		JPanel parameters = new JPanel(new GridBagLayout());
		parameters.setBackground(Color.white);

		//mode panel
		priorsPanel = new PriorSelectionPanel(!(repository == null || repository.isEmpty()));
		bootStrapPanel = new BootStrapPanel();
		discPanel = new DiscretizingPanel();
		classnumPanel = new ClassNumPanel();
		useGoPanel = new XmlBifPanel();
		runBNPanel = new RunBNPanel();
		tabbedPane.add("Running BN Parameters", runBNPanel);
		parameters.add(configPanel, new GridBagConstraints(0,0,2,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0));       
		parameters.add(priorsPanel, new GridBagConstraints(0,1,2,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0));
		parameters.add(discPanel, new GridBagConstraints(0,2,1,1,1.0,0,GridBagConstraints.EAST,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0));
		parameters.add(classnumPanel, new GridBagConstraints(1,2,1,1,1.0,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0));
		parameters.add(useGoPanel, new GridBagConstraints(0,4,2,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(bootStrapPanel, new GridBagConstraints(0,5,2,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		//parameters.add(runBNPanel, new GridBagConstraints(0,5,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(tabbedPane, new GridBagConstraints(0,6,2,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

		addContent(parameters);
		setActionListeners(listener);

		if(repository == null || repository.isEmpty()) {
			Component comp = tabbedPane.getComponentAt(0);
			JPanel panel = (JPanel)comp;
			panel.removeAll();
			panel.validate();
			panel.setOpaque(false);
			panel.add(new JLabel("Empty Cluster Repository"), new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(15,0,10,0),0,0));
			panel.add(new JLabel("Please create a gene cluster and launch BN again."), new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
			tabbedPane.setSelectedIndex(0);
			okButton.setEnabled(false);
		}

		this.setSize(600,750);
	}

	/** Creates a new instance of BNInitDialog
	 * @param parent Parent Frame
	 * @param repository Cluster repository to construct <CODE>ClusterBrowser</CODE>
	 * @param annotationLabels Annotation types
	 */
	public BNInitDialog(Frame parent, String [] annotationLabels) {
		super(parent, "BN:  Bayes Network Analysis", true);
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

		JPanel emptyClusterPanel = new JPanel(new GridBagLayout());
		String text = "<center><b>Note: When running BN in script mode the cluster<br>";
		text += "under analysis is determined by the preceding algorithm<br>";
		text += "that feeds source data into BN.</center>";
		JTextPane textArea = new JTextPane();
		textArea.setEditable(false);
		textArea.setBackground(Color.white);
		textArea.setContentType("text/html");
		textArea.setText(text);
		emptyClusterPanel.add(textArea, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

		//popNClusterPanel.add(popPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		popNClusterPanel.add(emptyClusterPanel, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		tabbedPane.add("Population and Cluster Selection", popNClusterPanel);

		JPanel parameters = new JPanel(new GridBagLayout());
		parameters.setBackground(Color.white);

		//mode paneli
		priorsPanel = new PriorSelectionPanel(true);
		bootStrapPanel = new BootStrapPanel();
		discPanel = new DiscretizingPanel();
		classnumPanel = new ClassNumPanel();
		useGoPanel = new XmlBifPanel();
		runBNPanel=new RunBNPanel(); 
		tabbedPane.add("Running Bayesian Network Parameters", runBNPanel);
		parameters.add(configPanel, new GridBagConstraints(0,0,2,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));       
		parameters.add(priorsPanel, new GridBagConstraints(0,1,2,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(discPanel, new GridBagConstraints(0,2,1,1,1.0,0,GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(classnumPanel, new GridBagConstraints(1,2,1,1,1.0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(useGoPanel, new GridBagConstraints(0,4,2,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(bootStrapPanel, new GridBagConstraints(0,5,2,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		//parameters.add(runBNPanel, new GridBagConstraints(0,5,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		parameters.add(tabbedPane, new GridBagConstraints(0,6,2,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

		addContent(parameters);
		setActionListeners(listener);

		this.setSize(600,800);
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

	/** Returns the base file location for BN file system
	 */
	public String getBaseFileLocation() {
		return configPanel.getBaseFileLocation();
		//return TMEV.getFile("data/bn").getAbsolutePath();
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
	public String getAlgorithm(){
		return this.searchAlgorithm;
	}
	public void setAlgorithm(String sa){
		this.searchAlgorithm=sa;
	}
	public String getScoreType(){
		return this.scoreType;
	}
	public void setScoreType(String st){
		this.scoreType=st;
	}
	public boolean useArcRev(){
		return this.useArc;
	}
	public void setUseArcRev(boolean ua){
		this.useArc=ua;
	}
	public int getNumIterations(){
		return this.bootStrapPanel.getNumIterations();
	}
	public float getConfThreshold(){
		return this.bootStrapPanel.getConfThreshold();
	}
	public boolean isBootstrapping(){
		return this.bootStrapPanel.isBootstrapping();
	}
	public int getKFolds(){
		return Integer.parseInt(this.runBNPanel.kFolds());
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
			//setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Network Priors Sources", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));

			litSourceCheckbox = new JCheckBox("Literature Mining",true);
			litSourceCheckbox.setFocusPainted(false);
			litSourceCheckbox.setBackground(Color.white);
			litSourceCheckbox.setHorizontalAlignment(JRadioButton.CENTER);
			litSourceCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					//popPanel.setEnableControls(true);
				}
			});


			ppiSourceCheckbox = new JCheckBox("Protein-Protein Interactions");
			ppiSourceCheckbox.setToolTipText("Uses protein-protein interaction data to create a seed network.");
			ppiSourceCheckbox.setFocusPainted(false);
			ppiSourceCheckbox.setBackground(Color.white);
			ppiSourceCheckbox.setHorizontalAlignment(JRadioButton.CENTER);
			ppiSourceCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					//popPanel.setEnableControls(true);
				}
			});

			keggSourceCheckbox = new JCheckBox("KEGG Interactions");
			keggSourceCheckbox.setToolTipText("Uses KEGG pathway interactions to create a seed network.");
			keggSourceCheckbox.setFocusPainted(false);
			keggSourceCheckbox.setBackground(Color.white);
			keggSourceCheckbox.setHorizontalAlignment(JRadioButton.CENTER);
			keggSourceCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					//popPanel.setEnableControls(true);
				}
			});


			ppiSourceCheckbox.setSelected(false);
			ppiSourceCheckbox.setEnabled(true);
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
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Discretize Expression Values", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));

			numLevelsField = new JTextField("3", 1);
			numLevelsField.setBackground(Color.white);

			numLevelsLabel = new JLabel(" Number of States");
			numLevelsLabel.setBackground(Color.white);

			numLevelsField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					//popPanel.setEnableControls(true);
				}
			});
			add(numLevelsLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(0,0,0,5),0,0));
			add(numLevelsField, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

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

			numClassesField = new JTextField("0", 2);
			numClassesField.setBackground(Color.white);

			numClassesLabel = new JLabel(" Number of Sample Classes");
			numClassesLabel.setBackground(Color.white);

			numClassesField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					//popPanel.setEnableControls(true);
				}
			});
			add(numClassesLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(0,0,0,5),0,0));
			add(numClassesField, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

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

		//private JLabel bootstrappingNotAvailable;

		/** Constructs a mode panel.
		 * @param haveClusters
		 */
		public BootStrapPanel(){
			super(new GridBagLayout());
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Bootstrapping Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));

			isBootstrappingCheckbox = new JCheckBox("Bootstrapping");
			isBootstrappingCheckbox.setSelected(true);
			isBootstrappingCheckbox.setEnabled(true);
			isBootstrappingCheckbox.setBackground(Color.white);
			//isBootStrappingLabel = new JLabel("Is Bootstrapping");
			//isBootStrappingLabel.setBackground(Color.GRAY);
			isBootstrappingCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					bootStrapPanel.setEnableControls(isBootstrappingCheckbox.isSelected());
				}
			});

			numIterationsField = new JTextField("20", 4);
			numIterationsField.setBackground(Color.white);
			numIterationsLabel = new JLabel(" Number of Iterations");
			numIterationsLabel.setBackground(Color.white);
			confThresholdField = new JTextField("0.7", 2);
			confThresholdField.setBackground(Color.white);
			confThresholdLabel = new JLabel(" Confidence Threshold");
			confThresholdLabel.setBackground(Color.white);

			isBootstrappingCheckbox.setEnabled(true);
			numIterationsField.setEnabled(true);
			confThresholdField.setEnabled(true);

			add(isBootstrappingCheckbox, 	new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0,0,0,30),0,0));
			//add(isBootStrappingLabel, 		new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0,0,0,5),0,0));
			add(numIterationsLabel, 		new GridBagConstraints(2,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,5),0,0));
			add(numIterationsField, 		new GridBagConstraints(3,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,15),0,0));            
			add(confThresholdLabel, 		new GridBagConstraints(4,1,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,5),0,0));
			add(confThresholdField, 		new GridBagConstraints(5,1,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,15),0,0));

		}
		public void setEnableControls(boolean enableControls){
			numIterationsField.setEnabled(enableControls);
			confThresholdField.setEnabled(enableControls);
		}
		public int getNumIterations(){return new Integer(numIterationsField.getText()).intValue();}
		public float getConfThreshold(){return new Float(confThresholdField.getText()).floatValue();}
		public boolean isBootstrapping(){return isBootstrappingCheckbox.isSelected();}
	}    

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
			useDFSButton = new JRadioButton("Use Depth-First Search to direct edges", true);           	
			useDFSButton.setHorizontalAlignment(JRadioButton.CENTER);
			useDFSButton.setBackground(Color.white);
			//GO Functionality is not working currently
			useGoButton.setEnabled(false);
			bGroup=new ButtonGroup();
			bGroup.add(useGoButton);
			bGroup.add(useDFSButton);
			//setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "How to direct edges for graph", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
			add(this.useDFSButton, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.useGoButton, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		}

		public boolean useGoTerms(){
			return useGoButton.isEnabled();
		}
	}

	private class RunBNPanel extends JPanel{
		private JLabel numLabel=new JLabel("Max Number of Parents:");
		private JLabel slabel=new JLabel("Search Algorithm:");
		private JLabel scorelabel=new JLabel("Scoring Scheme:");
		private JLabel arclabel=new JLabel("Use Arc Reversal:");
		private JTextField nParents=new JTextField("3");
		//Raktim - Added for K-Fold Cross validation specs.
		private JLabel foldLabel=new JLabel("Cross Validation Folds(K):");
		private JTextField kFolds=new JTextField("10");
		private JLabel foldWarning = new JLabel("(Fold min is 2 & can't be greater than #samples.)");

		String[] sOption={"K2","HillClimber","SimulatedAnnealing","TabuSearch","GeneticSearch"};
		JComboBox searchOptionBox=new JComboBox(sOption);
		String[] scoreOption={"BAYES","BDeu","ENTROPY","MDL"};
		JComboBox scoreOptionBox=new JComboBox(scoreOption);
		String[] arcOption={"True","False"};
		JComboBox arcOptionBox=new JComboBox(arcOption);
		public RunBNPanel(){
			super(new GridBagLayout());
			final int sampleCnt = framework.getData().getFeaturesCount();
			if(sampleCnt < 10) 
				kFolds.setText(String.valueOf(sampleCnt));
			foldWarning.setForeground(Color.RED);
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			searchOptionBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae) {
					String sa=(String)searchOptionBox.getSelectedItem();
					setAlgorithm(sa);
				}
			});
			searchOptionBox.setSelectedIndex(3);
			scoreOptionBox.setSelectedIndex(0);
			scoreOptionBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae) {
					String st=(String)scoreOptionBox.getSelectedItem();
					setScoreType(st);
				}
			});
			arcOptionBox.setSelectedIndex(0);
			arcOptionBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae) {
					boolean at=(Boolean)arcOptionBox.getSelectedItem();
					setUseArcRev(at);
				}
			});
			//setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Running Bayesian Network Parameters ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
			add(this.slabel, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.searchOptionBox, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.scorelabel, new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.scoreOptionBox, new GridBagConstraints(1,1,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.arclabel, new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.arcOptionBox, new GridBagConstraints(1,2,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.numLabel, new GridBagConstraints(0,3,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.nParents, new GridBagConstraints(1,3,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.foldLabel, new GridBagConstraints(0,4,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.kFolds, new GridBagConstraints(1,4,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
			add(this.foldWarning, new GridBagConstraints(0,5,2,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		}

		public String numParents(){
			return nParents.getText();
		}

		public String kFolds(){
			return kFolds.getText();
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
			converterFileField.setBackground(Color.white);

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

			sep = System.getProperty("file.separator");
			File file = new File(getBaseFileLocation()+"/Data/Convert/");
			String tempPath = file.getPath();
			Vector fileVector = new Vector();
			fileList = new JList(fileVector);
			if(file.exists()){
				String [] listFileNames = file.list();
				for(int i = 0; i < listFileNames.length; i++){
					File tempFile = new File(tempPath+sep+listFileNames[i]);
					if(tempFile.isFile())
						fileVector.add(listFileNames[i]);
				}
				if(fileVector.size() > 0){
					converterFileField.setText(tempPath+sep+((String)fileVector.elementAt(0)));
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
			this.converterFileField.setText(tempPath+sep+fileName);
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
			cngFilesButton.setToolTipText("<html>Select the directory where BN  files reside.</html>");

			JLabel fileLocation = new JLabel("File(s) Location:");
			//defaultFileBaseLocation = new JTextField(TMEV.getFile("data/bn").getAbsolutePath(), 25);
			//defaultFileBaseLocation = new JTextField(new File(System.getProperty("user.dir")).getAbsolutePath());
			String _loc = TMEV.getSettingForOption(BNConstants.BN_LM_LOC_PROP);
			if(_loc == null)
				_loc = TMEV.getDataPath();
			if(_loc.equals("") || _loc.equals(""))
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
			browseLabel = new JLabel("or Browse for another Ease data file system:");
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
				} catch (NullPointerException npe) {/* Leave as default */}
				arrayListBox = new JComboBox(speciestoarrays.get(organismListBox.getSelectedItem()));

				try {
					arrayListBox.setSelectedItem(arrayName);
				} catch (NullPointerException npe) {/* Leave as default */}

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
				//file = TMEV.getFile("data/bn");
				//if(file == null) {
				//file = new File(System.getProperty("user.dir"));
				file = new File(TMEV.getDataPath());
				//}
			}
			JFileChooser chooser = new JFileChooser(file);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(chooser.showOpenDialog(BNInitDialog.this) == JOptionPane.OK_OPTION) {
				String dir = chooser.getSelectedFile().getAbsolutePath().trim();

				if(dir.contains(" ")){
					JOptionPane.showMessageDialog(parent, 
							"Spaces are not allowed in Path. \n Selected a different location", 
							"BN Initialization: Illegal Char in Path", 
							JOptionPane.ERROR_MESSAGE);
					defaultFileBaseLocation.grabFocus();
					defaultFileBaseLocation.selectAll();
					//defaultFileBaseLocation.setCaretPosition(0);
					return;
				}

				defaultFileBaseLocation.setText(chooser.getSelectedFile().getAbsolutePath());
				//TMEV.setDataPath(defaultFileBaseLocation.getText());
				TMEV.storeProperty(BNConstants.BN_LM_LOC_PROP, defaultFileBaseLocation.getText());
			}
		}

		public String getBaseFileLocation() {
			//System.out.println("getBaseFileLocation() called");
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
					bnParamPanel.converterFileField.setBackground(Color.white);
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
			} else if (command.equals("select-file-base-command")) {
				configPanel.selectFileSystem();
			} else if (command.equals("update-files-command")) {
				//TODO add an update manager like the one in EASE module
				BNUpdateManager manager = new BNUpdateManager((JFrame)parent,configPanel.getBaseFileLocation());
				manager.updateFiles();
			} else if (command.equals("ok-command")) {
				result = JOptionPane.OK_OPTION;
				System.out.println("BN Dlg. OK Cmd");
				//Check to see if user is connected to Internet
				Hashtable repInfo = BNDownloadManager.getRepositoryInfoCytoscape();
				String codeBase = ((String)repInfo.get("cytoscape_webstart")).trim();
				String libDir = ((String)repInfo.get("cytoscape_lib_dir")).trim();
				String pluginsDir = ((String)repInfo.get("cytoscape_plugins_dir")).trim();

				if(codeBase == null || libDir == null) {
					JOptionPane.showMessageDialog(new JFrame(), "Internet Connection error or Error reading properties file, will try with default values", "Cytoscape may not launch", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				System.out.println("Jnlp codeBase: " + codeBase);
				System.out.println("Jnlp libDir: " + libDir);
				System.out.println("Jnlp pluginsDir: " + pluginsDir);
				BNConstants.setCodeBaseLocation(codeBase);
				BNConstants.setLibDirLocation(libDir);
				BNConstants.setPluginsDirLocation(pluginsDir);

				//Check if cluster size exceeds max genes allowed
				if(getSelectedCluster().getSize() > BNConstants.MAX_GENES) {
					JOptionPane.showMessageDialog(parent, "Cluster size exceeds max gene limit of " + BNConstants.MAX_GENES + ". Please select or create a different one.", "Error!", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Validate if selected options have supporting file(s)
				String fileBase =  getBaseFileLocation(); //configPanel.getBaseFileLocation();

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
								"BN Initialization: Missing File", JOptionPane.ERROR_MESSAGE);
						return;
					}

					if(!(new File(fileBase + BNConstants.SEP + BNConstants.ACCESSION_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.ACCESSION_FILE + " is missing",
								"BN Initialization: Missing File", JOptionPane.ERROR_MESSAGE);
						return;
					}

					if(!(new File(fileBase + BNConstants.SEP + BNConstants.GENE_DB_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.GENE_DB_FILE + " is missing",
								"BN Initialization: Missing File", JOptionPane.ERROR_MESSAGE);
						return;
					}

					if(!(new File(fileBase + BNConstants.SEP + BNConstants.PUBMED_DB_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.PUBMED_DB_FILE + " is missing",
								"BN Initialization: Missing File", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				if(isPPI()) {
					if(!(new File(fileBase + BNConstants.SEP + BNConstants.PPI_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.PPI_FILE + " is missing",
								"BN Initialization: Missing File", JOptionPane.ERROR_MESSAGE);
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
								"BN Initialization: Missing File", JOptionPane.ERROR_MESSAGE);

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
							"BN Initialization: Missing Selection", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if(useGoTerm()) {
					if(!(new File(fileBase + BNConstants.SEP + BNConstants.GB_GO_FILE)).exists()) {
						JOptionPane.showMessageDialog(
								parent, 
								"File: " + 
								fileBase + BNConstants.SEP + BNConstants.GB_GO_FILE + " is missing",
								"BN Initialization: Missing File", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				//**End of Validation

				if(bnParamPanel.useAnnBox.isSelected()) {
					String fileName = bnParamPanel.getConverterFileName();
					if( fileName == null || fileName.equals("") || fileName.equals(" ") ) {
						JOptionPane.showMessageDialog(parent, "You have selected to use an annotation conversion file but have not made a file selection.\n" +
								"Please enter a file name or browse to select a file.", "BN Initialization: Missing Parameter", JOptionPane.WARNING_MESSAGE);
						tabbedPane.setSelectedIndex(1);
						bnParamPanel.browserButton.grabFocus();
						return;
					}
				}    

				String val = runBNPanel.kFolds().trim();
				int fld = -1;
				try {
					//System.out.println("In Try block");
					fld = Integer.parseInt(val);
				}
				catch(NumberFormatException nfe ){
					//System.out.println("In Try block Excp");
					JOptionPane.showMessageDialog(parent, "Numbers only Pls", "Error", JOptionPane.ERROR_MESSAGE); 
					runBNPanel.kFolds.grabFocus();
					return;
				}
				int sampleCnt = framework.getData().getFeaturesCount();
				if(fld < 2 | fld > sampleCnt){
					//System.out.println("Invalid input " + fld);
					JOptionPane.showMessageDialog(parent, "Min is 2 and cannot be greater than # of sample.", "Error", JOptionPane.ERROR_MESSAGE); 
					runBNPanel.kFolds.grabFocus();
					return;
				}

				if(bootStrapPanel.isBootstrappingCheckbox.isEnabled()){
					float flt = 0.00f;
					try {
						flt = Float.parseFloat(bootStrapPanel.confThresholdField.getText().trim());
					}
					catch (NumberFormatException nfe){
						//System.out.println("In Try block Excp");
						JOptionPane.showMessageDialog(parent, "Float value only", "Error", JOptionPane.ERROR_MESSAGE); 
						bootStrapPanel.confThresholdField.grabFocus();
						return;
					}
					if(flt < 0.50f | flt > 0.99f ) {
						JOptionPane.showMessageDialog(parent, "Value expected between 0.50 & 0.99", "Error", JOptionPane.ERROR_MESSAGE); 
						bootStrapPanel.confThresholdField.grabFocus();
						return;
					}

					int itr = 0;
					try {
						//System.out.println("In Try block");
						itr = Integer.parseInt(bootStrapPanel.numIterationsField.getText().trim());
					}
					catch (NumberFormatException nfe){
						//System.out.println("In Try block Excp");
						JOptionPane.showMessageDialog(parent, "Integers only", "Error", JOptionPane.ERROR_MESSAGE); 
						bootStrapPanel.numIterationsField.grabFocus();
						return;
					}
				}

				//Number of Bins validation
				try {
					if(discPanel.getNumLevels() != 3) {
						JOptionPane.showMessageDialog(parent, "Currently Supports 3 States only\n Up, Neutral and Down", "Error", JOptionPane.ERROR_MESSAGE); 
						discPanel.numLevelsField.setText("3");
						discPanel.numLevelsField.grabFocus();
						return;
					}
				}
				catch (NumberFormatException nfe){
					//System.out.println("In Try block Excp");
					JOptionPane.showMessageDialog(parent, "States can be Integers only", "Error", JOptionPane.ERROR_MESSAGE); 
					discPanel.numLevelsField.setText("3");
					discPanel.numLevelsField.grabFocus();
					return;
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
								"BN Initialization: Dir create error", JOptionPane.ERROR_MESSAGE);
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
								"BN Initialization: Dir create error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					System.out.println("Dir: " + 
							fileBase + BNConstants.SEP + BNConstants.TMP_DIR + " created successfully !!");
				}

				BNConstants.setBaseFileLocation(fileBase);
				//TMEV.setDataPath(fileBase);
				TMEV.storeProperty(BNConstants.BN_LM_LOC_PROP, fileBase);
				dispose();
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")){
				resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")){
				HelpWindow hw = new HelpWindow(BNInitDialog.this, "BN Initialization Dialog");
				//result = JOptionPane.CANCEL_OPTION;
				if(hw.getWindowContent()){
					hw.setSize(600,600);
					hw.setLocation();
					hw.show();
				}
				else {
					hw.setVisible(false);
					hw.dispose();
				}
				// RM related handlers
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
		BNInitDialog eid = new BNInitDialog(new JFrame(), labels);
		eid.showModal();
	}
}

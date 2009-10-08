package org.tigr.microarray.mev.cluster.gui.impl.gsea;



import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.GeneAnnotationImportDialog;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;
import org.tigr.microarray.mev.file.AnnotationDownloadHandler;
import org.tigr.microarray.mev.file.GBA;
import org.tigr.microarray.mev.file.SuperExpressionFileLoader;
import org.tigr.microarray.util.FileLoaderUtility;
import org.tigr.util.swing.GeneMatrixFileFilter;
import org.tigr.util.swing.GeneMatrixTransposeFileFilter;
import org.tigr.util.swing.TXTFileFilter;



public class ParameterPanel extends JPanel implements IWizardParameterPanel{

	//Collects all information (parameters) required to run the algorithm
	private javax.swing.JPanel parameterPanel;
	private javax.swing.JLabel probeInformationLabel;
	private javax.swing.JLabel probe2GeneLabel;
	private javax.swing.JLabel probe2GeneInfo;
	private javax.swing.JComboBox choiceBox;
	private javax.swing.JLabel minGeneLabel;
	private javax.swing.JTextField geneNumber;
	private javax.swing.JLabel SDCutoffLabel;
	private javax.swing.JTextField sdTextField;
	private javax.swing.JLabel permutationLabel;
	private javax.swing.JTextField permutationTextField;
	private IFramework fwork;
	private GBA gba;
	private String annotationFilePath;
	
	//Collects all information pertaining to gene sets
	private javax.swing.JPanel genesetPanel;
	private javax.swing.JPanel choicePanel;
	private javax.swing.JPanel fileSelectionPanel, buttonPanel, listPanel, selectFilePanel;
	private javax.swing.JPanel identifierSelectionPanel;
	private javax.swing.JRadioButton mSigDBbutton, localFileButton;
	private javax.swing.ButtonGroup buttonGrp;
	private javax.swing.JLabel selectFile, availableLabel, selectedLabel;
	private javax.swing.JList availableList, selectedList;
	private javax.swing.JButton addButton, addAllButton, removeButton, removeAllButton;
	private javax.swing.JScrollPane availableScrollPane, selectedScrollPane;
	private javax.swing.JTextField pathTextField;
	private javax.swing.JButton browse;
	private javax.swing.JPanel autoDownloadPanel;
	private javax.swing.JLabel downloadGenesetLabel;
	private javax.swing.JLabel downloadStatusLabel;
	private javax.swing.JButton DownloadButton;
	private javax.swing.JLabel genesetPanelEmptyLabel;
	private javax.swing.JLabel geneIdentifierLabel;
	private javax.swing.JComboBox geneIdentifierBox;
	private String fileFilter;
	
	
	
	
	
	//Collects all information pertaining to gene annotation. Is visible only if annotations
	//are NOT loaded
	private javax.swing.JPanel annotationPanel;
	private AnnotationDownloadHandler adh;
	
	private AlgorithmData algData;
	
	public ParameterPanel(AlgorithmData algData, JFrame parent, IFramework framework) {
		this.algData=algData;
		this.fwork=framework;
		initializePanel();
		initialize(GSEAConstants.MAX_PROBE, Integer.toString(5), "0.6", Integer.toString(1000));
	}
	
	
	public void initializePanel(){
		this.setPreferredSize(new Dimension(1000, 850));
		setBackground(Color.WHITE);
		setLayout(new GridBagLayout());
		gba=new GBA();
		String[] Key= {"Max_Probe", "Median_Probe", "Standard_Deviation"};

		
        probe2GeneLabel=new JLabel();
		probe2GeneLabel.setText("Select a method to collapse probes"); 
		probe2GeneLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		
		choiceBox=new JComboBox(Key);
		choiceBox.addActionListener(new Listener());
		
		probeInformationLabel=new JLabel();
	    probeInformationLabel.setText("<html>( If multiple probes in the expression data map to one gene, MeV can use either the <br> " +
	    							   "1. Probe with the maximum expression value. " +
	    							   "2. Median of the expression values. " +
	    							   "3. SD of the expression values...whichever you choose) </html>");
	 
	    probeInformationLabel.setHorizontalTextPosition(SwingConstants.LEFT);
	    probeInformationLabel.setFont(new Font("Dialog",Font.ITALIC, 12));
        
		
		SDCutoffLabel=new JLabel();
		SDCutoffLabel.setText("Select a cut off, if you chose Standard Deviation to collapse probes");
		SDCutoffLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		SDCutoffLabel.setEnabled(false);
		
		sdTextField=new JTextField();
		sdTextField.setPreferredSize(new Dimension(100, 30));
		sdTextField.setEditable(true);
		sdTextField.setEnabled(false);
		sdTextField.setActionCommand("standard-deviation");
		
		
	
		minGeneLabel=new JLabel();
		minGeneLabel.setText("Minimum number of genes per gene set"); 
		minGeneLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		//minGeneLabel.setFont(new Font("Dialog",Font.PLAIN, 12));
	
		
		geneNumber=new JTextField();
		geneNumber.setPreferredSize(new Dimension(100, 30));
		geneNumber.setEditable(true);
		geneNumber.setActionCommand("gene-number");
		
		permutationLabel=new JLabel();
		permutationLabel.setText("Enter number of permutations");
		permutationLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		
		permutationTextField=new JTextField();
		permutationTextField.setPreferredSize(new Dimension(100,30));
		permutationTextField.setEditable(true);
		permutationTextField.setActionCommand("permutations");
		
			
		parameterPanel = new JPanel();
		parameterPanel.setBackground(Color.WHITE);
        parameterPanel.setLayout(new GridBagLayout());
        parameterPanel.setBorder(new EtchedBorder());
        
        //Add all components to parameterPanel
    	gba.add(parameterPanel, probe2GeneLabel, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(parameterPanel, choiceBox, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(parameterPanel, probeInformationLabel, 0, 3, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(2, 2, 2, 2), 0, 0);

		gba.add(parameterPanel, SDCutoffLabel, 0, 4, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(parameterPanel, sdTextField, 2, 4, GBA.RELATIVE, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0); 
		

		gba.add(parameterPanel, minGeneLabel, 0, 7, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(parameterPanel, geneNumber, 2, 7, GBA.RELATIVE, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0); 
		
		gba.add(parameterPanel, permutationLabel, 0, 8, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(parameterPanel, permutationTextField, 2, 8, GBA.RELATIVE, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0); 

		
	    genesetPanel=new JPanel();
	    genesetPanel.setBackground(Color.WHITE);
	    genesetPanel.setLayout(new GridBagLayout());
        genesetPanel.setBorder(new EtchedBorder());
        
        //Create and Add the radio buttons to choice panel
//        choicePanel=new JPanel();
//        choicePanel.setLayout(new GridBagLayout());
//        
//        mSigDBbutton=new JRadioButton();
//        mSigDBbutton.setText("Download from MSigDB");
//        mSigDBbutton.setSelected(false);
//        
//        localFileButton=new JRadioButton();
//        localFileButton.setText("Load local geneset file/files");
//        localFileButton.setSelected(true);
//        
//        buttonGrp=new ButtonGroup();
//        buttonGrp.add(mSigDBbutton);
//        buttonGrp.add(localFileButton);
//        
//        gba.add(choicePanel, mSigDBbutton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
//        gba.add(choicePanel, localFileButton, 2, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
//        
//        autoDownloadPanel=new JPanel();
//        autoDownloadPanel.setLayout(new GridBagLayout());
//        downloadGenesetLabel = new javax.swing.JLabel();
//        downloadGenesetLabel.setText("Download gene sets from the Broad-MIT FTP site");
//
//		genesetPanelEmptyLabel = new javax.swing.JLabel();
//
//		DownloadButton = new javax.swing.JButton();
//		DownloadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
//		DownloadButton.setPreferredSize(new Dimension(100, 30));
//		DownloadButton.addActionListener(new Listener());
//		DownloadButton.setText("Download");
//
//		gba.add(autoDownloadPanel, downloadGenesetLabel, 0, 1, 2, 1, 0, 0, GBA.H,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
//		gba.add(autoDownloadPanel, genesetPanelEmptyLabel, 1, 1, 1, 1, 0, 0, GBA.H,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
//		gba.add(autoDownloadPanel, DownloadButton, 2, 1, GBA.REMAINDER, 1, 0, 0,GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
//
//        
        
        
        //Create and Add components to fileSelectionPanel 
        fileSelectionPanel=new JPanel();
        fileSelectionPanel.setLayout(new GridBagLayout());
        fileSelectionPanel.setBackground(Color.white);
        
        selectFilePanel = new JPanel();
		selectFilePanel.setLayout(new GridBagLayout());
		selectFilePanel.setBackground(Color.white);
		
        selectFile = new JLabel("Select the directory containing your gene sets");

		browse = new JButton("Browse");
		browse.setSize(new Dimension(100, 30));
		browse.setPreferredSize(new Dimension(100, 30));
		browse.addActionListener(new Listener(){
			public void actionPerformed(ActionEvent e){
				onBrowse();
			}
			
		});
	
		
		pathTextField = new JTextField();
		pathTextField.setEditable(false);
		pathTextField.setFont(new Font("monospaced", Font.BOLD, 12));
		pathTextField.setPreferredSize(new Dimension(500, 20));
		
		
		gba.add(selectFilePanel, selectFile, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(selectFilePanel, pathTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(selectFilePanel, browse, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

		
		
		listPanel=new JPanel();
		listPanel.setLayout(new GridBagLayout());
		listPanel.setBackground(Color.white);
		availableLabel=new JLabel("Available");
		selectedLabel=new JLabel("Selected");
		
		availableList=new javax.swing.JList(new DefaultListModel());
		selectedList=new javax.swing.JList(new DefaultListModel());
		
		availableScrollPane=new JScrollPane(availableList);
		availableScrollPane.setPreferredSize(new Dimension(250, 90));
		selectedScrollPane=new JScrollPane(selectedList);
		selectedScrollPane.setPreferredSize(new Dimension(250,90));
		
		addButton=new JButton("Add");
		addButton.setPreferredSize(new Dimension(100,20));
		addButton.addActionListener(new Listener(){
			public void actionPerformed(ActionEvent e){
				onAdd();
			}
		});
		
		addAllButton=new JButton("Add All");
		addAllButton.setPreferredSize(new Dimension(100,20));
		addAllButton.addActionListener(new Listener(){
			public void actionPerformed(ActionEvent e){
				onAddAll();
			}
		});
		
		removeButton=new JButton("Remove");
		removeButton.setPreferredSize(new Dimension(100,20));
		removeButton.addActionListener(new Listener(){
			public void actionPerformed(ActionEvent e){
				onRemove();
			}
			
		});
		
		removeAllButton=new JButton("Remove All");
		removeAllButton.setPreferredSize(new Dimension(100,20));
		removeAllButton.addActionListener(new Listener(){
			public void actionPerformed(ActionEvent e){
				onRemoveAll();
			}
			
		});
		
		buttonPanel=new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		buttonPanel.setBackground(Color.white);
		gba.add(buttonPanel, addButton, 0, 0, 1, 1, 1, 1, GBA.N,
				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(buttonPanel, addAllButton, 0, 1, 1, 1, 1, 1, GBA.N,
				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(buttonPanel, removeButton, 0, 2, 1, 1, 1, 1, GBA.N,
				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(buttonPanel, removeAllButton, 0, 3, 1, 1, 1, 1,
				GBA.N, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

		
		
		
		gba.add(listPanel, availableLabel, 0, 0, 1, 1, 0, 0, GBA.N,
				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(listPanel, availableScrollPane, 0, 1, 1, 4, 5, 1,
				GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
//		gba.add(listPanel, new JPanel(), 1, 0, 1, 1, 0, 0, GBA.B, GBA.C,
//				new Insets(0, 0, 0, 0), 0, 0);
		gba.add(listPanel, buttonPanel, 1, 1, 1, 4, 1, 1, GBA.B,
				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(listPanel, selectedLabel, 2, 0, 1, 1, 0, 0, GBA.N,
				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(listPanel, selectedScrollPane, 2, 1, 1, 4, 5, 1,
				GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

		
		
		gba.add(fileSelectionPanel, selectFilePanel, 0, 0, 1, 1, 1, 1, GBA.B,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		gba.add(fileSelectionPanel, listPanel, 0, 2, 1, 1, 1, 1, GBA.B,
				GBA.C, new Insets(2, 2, 2, 2), 0, 0);

        //Create and Add components to identifierSelectionPanel
        identifierSelectionPanel=new JPanel();
        identifierSelectionPanel.setLayout(new GridBagLayout());
        identifierSelectionPanel.setBackground(Color.white);
        geneIdentifierLabel=new JLabel("Select the identifier used to annotate genes in selected gene set/s");
        
        Field[]fields=AnnotationFieldConstants.class.getFields();
        String[]annotation=new String[fields.length+1];
        annotation[0]="";
        try{
        for(int index=0; index<fields.length; index++){
        	annotation[index+1]=(String)fields[index].get(new AnnotationFieldConstants());
        }
        }catch(Exception e){
        	
        }
        
        geneIdentifierBox=new JComboBox(annotation);
        geneIdentifierBox.addActionListener(new Listener());
        
        gba.add(identifierSelectionPanel, geneIdentifierLabel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2,2,2,2), 0,0);
        gba.add(identifierSelectionPanel, geneIdentifierBox, 0, 1, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(2,2,2,2), 0,0);
        
        //Add choicePanel, fileSelectionPanel and identifierSelectionPanel to the geneset panel
        
       
//        gba.add(genesetPanel, choicePanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
//        gba.add(genesetPanel, autoDownloadPanel, 0, 1, 2, 1, 0, 0, GBA.H,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
	//	gba.add(genesetPanel,fileSelectionPanel, 0, 2, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
	  //  gba.add(genesetPanel, identifierSelectionPanel, 0, 6, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
        gba.add(genesetPanel,fileSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
  	    gba.add(genesetPanel, identifierSelectionPanel, 0, 5, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
       
	    annotationPanel=new JPanel();
	    annotationPanel.setLayout(new GridBagLayout());
        annotationPanel.setBorder(new EtchedBorder());
       
	       
	    adh = new AnnotationDownloadHandler(fwork);
	   // System.out.println("Annotation has been loaded:"+fwork.getData().isAnnotationLoaded());
		if(fwork.getData().isAnnotationLoaded()) {
						
			annotationPanel.setVisible(false);
			adh.setOptionalMessage("Annotation is already loaded for array " + fwork.getData().getChipAnnotation().getChipType());
			adh.setAnnFilePath(fwork.getData().getChipAnnotation().getAnnFileName());
		}
		adh.addListener(new Listener());
		annotationPanel = adh.getAnnotationLoaderPanel(gba);
		annotationPanel.setBackground(Color.white);
		
		adh.setDownloadEnabled(!fwork.getData().isAnnotationLoaded());
		
		
	    gba.add(this, parameterPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
	    gba.add(this, genesetPanel, 0, 2, 1, 1, 100, 100, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
	    gba.add(this, annotationPanel, 0, 4, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
		
		    
	    revalidate();
	   
	   
		
		
	}
	
    
	public void initialize(String collapseMode, String minGenes, String SDcutoff, String num_Perms ) {
		this.choiceBox.setSelectedItem(collapseMode);
		this.geneNumber.setText(minGenes);
		this.sdTextField.setText(SDcutoff);
		this.permutationTextField.setText(num_Perms);
		
		
	}
	
	
	
	public void clearValuesFromAlgorithmData() {
		
		algData.getParams().getMap().remove("probe_value");
		algData.getParams().getMap().remove("gene-number");
		algData.getParams().getMap().remove("standard-deviation-cutoff");
		algData.getParams().getMap().remove("permutations");
		
		
	}

	
	public void onDisplayed() {
		// TODO Auto-generated method stub
		
	}

	
	public void populateAlgorithmData() {
		if(choiceBox.getSelectedItem()!=null){
			if(choiceBox.getSelectedItem().equals("Max_Probe"))
				algData.addParam("probe_value", GSEAConstants.MAX_PROBE);
			if(choiceBox.getSelectedItem().equals("Median_Probe"))
				algData.addParam("probe_value", GSEAConstants.MEDIAN_PROBE);
			if(choiceBox.getSelectedItem().equals("Standard_Deviation")){
				algData.addParam("probe_value", GSEAConstants.SD);
				algData.addParam("standard-deviation-cutoff", sdTextField.getText());
			}
		}
			else{
			algData.addParam("probe_value", GSEAConstants.MAX_PROBE);
			algData.addParam("standard-deviation-cutoff", "NA");
			}
		
		//if(geneNumber.getText()!=null)
			algData.addParam("gene-number", geneNumber.getText());
			algData.addParam("permutations", permutationTextField.getText());
			algData.addParam("gene-set-directory", pathTextField.getText());
			
			if(getFileFilter().equalsIgnoreCase("Gene Matrix and Gene Matrix Transpose (*.gmt, *.gmx)"))
				algData.addParam("gene-identifier", AnnotationFieldConstants.GENE_SYMBOL);
			else{
				algData.addParam("gene-identifier", (String)geneIdentifierBox.getSelectedItem());
			}
			
			
			algData.addStringArray("gene-set-files", getAllSelectedItems());
			
			
			if (adh.isAnnotationSelected()) {
				algData.addParam("annotation-file", adh.getAnnFilePath());
			} else if(fwork.getData().isAnnotationLoaded()) {
				algData.addParam("annotation-file", fwork.getData().getChipAnnotation().getAnnFileName());
			}else{
				algData.addParam("annotation-file", "");
			}
		
	}
	
	private void updateLabel(String name) {
		choiceBox.setSelectedItem(name);
		
	}
	
	
	
	public void onAdd(){
		int[] chosenIndices = availableList.getSelectedIndices();
		Object[] chosenObjects = new Object[chosenIndices.length];

		for (int i = chosenIndices.length - 1; i >= 0; i--) {
			Object addItem = ((DefaultListModel) availableList
					.getModel()).getElementAt(chosenIndices[i]);
			chosenObjects[i] = addItem;
		}

		for (int i = 0; i < chosenIndices.length; i++) {
			((DefaultListModel) selectedList.getModel())
					.addElement(chosenObjects[i]);
		}

		

	}
	
	
	public void onAddAll(){
		int elementCount = ((DefaultListModel) availableList.getModel())
		.size();
		for (int i = 0; i < elementCount; i++) {
			Object addItem = ((DefaultListModel) availableList
					.getModel()).getElementAt(i);
			((DefaultListModel) selectedList.getModel())
			.addElement(addItem);
		}

	}
	
	
	
	public void onRemove(){
		int[] chosenIndices = selectedList.getSelectedIndices();

		// Designed with copy-then-add functionality in mind
		for (int i = chosenIndices.length - 1; i >= 0; i--) {
			((DefaultListModel) selectedList.getModel())
					.remove(chosenIndices[i]);
		}
	
		
	}
	
	
	public void onRemoveAll(){
		// Designed with copy-then-add functionality in mind
		((DefaultListModel) selectedList.getModel()).removeAllElements();
	}

	
	public String[]getAllSelectedItems(){
		String[]selectedFiles=new String[selectedList.getModel().getSize()];
		for(int index=0; index<selectedFiles.length; index++){
			selectedFiles[index]=((File)selectedList.getModel().getElementAt(index)).getName();
		}
		return selectedFiles;
	}
	
	
	public void onBrowse(){
		FileLoaderUtility fileLoad=new FileLoaderUtility();
		Vector retrievedFileNames=new Vector();
		JFileChooser fileChooser = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.addChoosableFileFilter(new TXTFileFilter());
		fileChooser.addChoosableFileFilter(new GeneMatrixFileFilter());
		fileChooser.addChoosableFileFilter(new GeneMatrixTransposeFileFilter());
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		int retVal = fileChooser.showOpenDialog(this);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			
			((DefaultListModel) availableList.getModel()).clear();
			((DefaultListModel) selectedList.getModel()).clear();

			File selectedFile = fileChooser.getSelectedFile();
			String path=selectedFile.getAbsolutePath();
			retrievedFileNames=fileLoad.getFileNameList(selectedFile.getAbsolutePath());
			pathTextField.setText(path);
			
			if(fileChooser.getFileFilter().getDescription().equalsIgnoreCase("Gene Matrix Files (*.gmx)")
					||fileChooser.getFileFilter().getDescription().equalsIgnoreCase("Gene Matrix Transpose File(*.gmt)")){
				geneIdentifierBox.setSelectedItem(AnnotationFieldConstants.GENE_SYMBOL);
				geneIdentifierBox.setEnabled(false);
			}else{	
				geneIdentifierBox.setSelectedItem(geneIdentifierBox.getItemAt(0));
				geneIdentifierBox.setEnabled(true);
			}
				
				

		
			if(retrievedFileNames.size()==0){
				pathTextField.setText("No files of type "+fileChooser.getFileFilter().getDescription()+"were found");
				
			}
			
			for (int i = 0; i < retrievedFileNames.size(); i++) {
				
				Object fileName=retrievedFileNames.get(i);
				boolean acceptFile=fileChooser.getFileFilter().accept((File)fileName);
								
				if(acceptFile) {
					pathTextField.setText(path);
					String Name=fileChooser.getName((File) fileName);
					setFileFilter(fileChooser.getFileFilter().getDescription());
					
					((DefaultListModel) availableList.getModel())
						.addElement(new File(Name));
				}
			}
			
			if(((DefaultListModel)availableList.getModel()).getSize()==0){
				String eMsg="<html><font color=red>" +"No files matching the selected filter<br> "+
				fileChooser.getFileFilter().getDescription()+"<br>"+"were found!!</font></html>";
				
				((DefaultListModel)availableList.getModel()).add(0, eMsg);
			}
			

		}
	

	}
	
	private void setFileFilter(String filter){
		fileFilter=filter;
	}
	
	private String getFileFilter(){
		return fileFilter;
	}
	
	/**
	 * processAnnotationFile() function 
	 * 1. Reads the selected annotation file 
	 * 2. Calls "GeneAnnotationImportDialog" to correctly map the unique identifier
	 * in the annotation file (probe id) to the unique identifier in the
	 * expression data loaded. 
	 * 3. Calls "addResourcererGeneAnnotation", which makes the necessary changes in SlideDataElement
	 * 
	 */
	public void processAnnotationFile() {
		try {
			String[] dataFieldNames = fwork.getData().getFieldNames();

			AnnotationFileReader reader = AnnotationFileReader
			.createAnnotationFileReader(getAnnotationFile());
			//EH
			GeneAnnotationImportDialog importDialog = new GeneAnnotationImportDialog(
					new JFrame(), dataFieldNames, reader.getAvailableAnnotations());//MevAnnotation.getFieldNames());

			if (importDialog.showModal() == JOptionPane.OK_OPTION) {
				((MultipleArrayData) fwork.getData()).addResourcererGeneAnnotation(
						importDialog.getDataAnnotationKey(), reader
						.getAffyAnnotation());
				fwork.getData().setChipAnnotation(reader.getAffyChipAnnotation());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fwork.getData().setAnnotationLoaded(true);
	}

	
	private File getAnnotationFile() {
		return new File(this.adh.getAnnFilePath());
	}

		
	
	
	private class Listener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			String command = e.getActionCommand();
			if(e.getSource().equals(choiceBox)) {
				updateLabel((String)choiceBox.getSelectedItem());
				if(((String)choiceBox.getSelectedItem()).equalsIgnoreCase("Standard_Deviation")){
					SDCutoffLabel.setEnabled(true);
					sdTextField.setEnabled(true);

				}else{
					SDCutoffLabel.setEnabled(false);
					sdTextField.setEnabled(false);
				}
			}else if(command.equalsIgnoreCase("gene-number")) {
				geneNumber.setText(geneNumber.getText());
			}else if(command.equalsIgnoreCase("standard-deviation")){
				sdTextField.setText(sdTextField.getText());
			}else if(command.equalsIgnoreCase("permutations")){
				permutationTextField.setText(permutationTextField.getText());
			}else if (command.equalsIgnoreCase(AnnotationDownloadHandler.GOT_ANNOTATION_FILE)) {
				processAnnotationFile();
			}
			
			
			

		}

	}
	
	
	
	public GridBagConstraints buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
		gbc=new GridBagConstraints();
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
        return gbc;
    }
	
	
	
	
	
}

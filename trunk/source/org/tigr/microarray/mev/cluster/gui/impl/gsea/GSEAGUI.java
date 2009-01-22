package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.GeneData;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.GeneDataElement;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.GeneSetElement;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.Geneset;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.ProbetoGene;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.ReadGeneSet;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.util.FloatMatrix;

public class GSEAGUI implements IClusterGUI {
	 	
		private Algorithm gsea;
		private Progress progress;
	    private Experiment experiment;
	    private GSEAExperiment gseaExperiment;
	    private IData idata;
	    private Logger logger;
	    protected Listener listener;
	    protected String[][]geneToProbeMapping;
	    //max_columns decides the number of columns to display in the
	    //table viewer
	    int max_columns;
	    private int[][]geneset_clusters;
	    
	   

	
	public DefaultMutableTreeNode execute(IFramework framework)	throws AlgorithmException {
		
		this.experiment = framework.getData().getExperiment();
        this.idata = framework.getData();
        FloatMatrix matrix = experiment.getMatrix();
		int number_of_samples = experiment.getNumberOfSamples();
		
        AlgorithmData algData = new AlgorithmData();
        DefaultMutableTreeNode resultNode = null;
		
		
		Geneset[]gset=null;
		Geneset[]geneset=null;
		GeneData[]gData=null;
		
		algData.addMatrix("matrix",matrix);
					
		JFrame mainFrame = (JFrame)(framework.getFrame());
		//Need the "." after the step names, to keep track of the highlighting
		String [] steps = {"Data Selection.","Parameter Selection.", "Execute."};		
		
		GSEAInitWizard wiz=new GSEAInitWizard(idata, mainFrame, "GSEA Initialization", true, algData,steps,  1, new StepsPanel(), framework.getClusterRepository(1), framework);
		
		 listener = new Listener();
	     logger = new Logger(framework.getFrame(), "Gene Set Enrichment Analysis", listener);
	         
		
		
		if(wiz.showModal() == JOptionPane.OK_OPTION) {
			logger.show();
					
			
			String genesetFilePath=algData.getParams().getString("gene-set-file");
			String extension=checkFileNameExtension(genesetFilePath);
			
			//Get the collapse mode; MAX_PROBE OR MEDIAN_PROBE
			String collapsemode=algData.getParams().getString("probe_value");
			//Get the minimum number of genes per gene set
			int min_genes=Integer.parseInt(algData.getParams().getString("gene-number"));
			
			//Get the SD cutoff
			
			String cutoff=algData.getParams().getString("standard-deviation-cutoff");
			
			//First step is to convert the expression data into GeneData object. Depending on the type of 
			//gene set file loaded, the gene identifier to use will vary.
			
			ProbetoGene ptg=new ProbetoGene(algData, idata);
			logger.append("Collapsing probes to genes \n");
			
			//If extension is gmt or gmx, the gene identifier defaults to GENE_SYMBOL.
			if(extension.equalsIgnoreCase("gmt")||extension.equalsIgnoreCase("gmx")){
				gData=ptg.convertProbeToGene(AnnotationFieldConstants.GENE_SYMBOL, collapsemode, cutoff);
							
			}
			//If extension is .txt, gene identifier is ENTREZ_ID as of now. 
			else if(extension.equalsIgnoreCase("txt")){
				gData=ptg.convertProbeToGene(AnnotationFieldConstants.ENTREZ_ID, collapsemode, cutoff);
			}
			
			gseaExperiment=ptg.returnGSEAExperiment();
			Vector genesInExpressionData=algData.getVector("Unique-Genes-in-Expressionset");
//			System.out.println("Number of unique genes in data set:"+genesInExpressionData.size());
			
			//Second step is to read the Gene Set file itself. Once this is done, the gene sets will have to be further
			//processed to remove the genes, which are present in the gene set but NOT in GeneData (expressiondata). 
			//Gene set object is also processed to remove the gene sets which do not have the minimum number of genes
			//as specified by the user.
			
			logger.append("Reading gene set files \n");
			
			ReadGeneSet rgset=new ReadGeneSet(extension, genesetFilePath);
			try{
				if(extension.equalsIgnoreCase("gmx"))
					gset=rgset.read_GMXformatfile(genesetFilePath);
				else if(extension.equalsIgnoreCase("gmt"))
					gset=rgset.read_GMTformatfile(genesetFilePath);
				else if(extension.equalsIgnoreCase("txt"))
					gset=rgset.read_TXTformatfile(genesetFilePath);
			
				Geneset[] gene_set=rgset.removeGenesNotinExpressionData(gset, genesInExpressionData);//--commented for testing
				//Geneset[] gene_set=gset;//Added for Testing to see, if removeGenes is screwing up 
				

			//Third step is to generate Association Matrix. The Association Matrix generated, does not include gene set
			//which do not satisfy the minimum number of genes criteria
			logger.append("Creating Association Matrix \n");
				
			FloatMatrix amat=rgset.createAssociationMatrix(gene_set, genesInExpressionData, min_genes);
			algData.addGeneMatrix("association-matrix", amat);
					
			
			//System.out.println("size of excluded gene set is:"+rgset.getExcludedGeneSets().size());
			geneset=new Geneset[gene_set.length-rgset.getExcludedGeneSets().size()];
			
			logger.append("Removing gene sets that do not pass the minimum genes criteria \n");
			geneset=rgset.removeGenesetsWithoutMinimumGenes(rgset.getExcludedGeneSets(), gene_set);
			
			//Add the Gene set names to AlgorithmData
			algData.addVector("gene-set-names", geneset[0].getAllGenesetNames());
			
						
			//Add to Algorithm Data, so that can access from viewers
			algData.addVector("excluded-gene-sets", rgset.getExcludedGeneSets());
			
			
			
			/****
			 *@TO Do:  I think i may need to add a function to probetoGene that returns GSEAExperiment.
			 * No way of passing it around otherwise. ?
			 */
			
			
			
			
			
			}catch(Exception e){
				e.printStackTrace();
			}
			gsea = framework.getAlgorithmFactory().getAlgorithm("GSEA");
			gsea.addAlgorithmListener(listener);
			logger.append("Algorithm execution begins... \n");
			AlgorithmData result = gsea.execute(algData);	
			logger.append("Algorithm excecution ends...\n");
			logger.dispose();
			
		
		//String array containing Gene to Probe mapping, which will be used in the table viewers	
			geneToProbeMapping=gData[0].getProbetoGeneMapping(gData);
		
			//Decides the number of columns in the table viewer. 
			//The reason being one Gene may map to one probe and another to ten. 
			 
			this.max_columns=gData[0].get_max_num_probes_mapping_to_gene();
			
			//Add code to generate a 2-d integer array 
		//	geneset_clusters=new int [geneset.length][max_columns];
			geneset_clusters=GenesettoProbeMapping(gData, geneset);
			
			
			//add clusters, means, and variances---Move it to gesa.java 
			FloatMatrix clusterMeans = this.getMeans(matrix, geneset_clusters);
			FloatMatrix clusterVars = this.getVariances(matrix, clusterMeans, geneset_clusters);
			algData.addIntMatrix("clusters", geneset_clusters);
			algData.addMatrix("cluster-means", clusterMeans);
			algData.addMatrix("cluster-variances", clusterVars);
		//Move ends
			
			/*Printing the gene to probe mapping
			for(int i=0; i<geneset_clusters.length; i++){
				System.out.println("Gene set cluster"+i);
				int[] temp=geneset_clusters[i];
				for(int j=0; j<temp.length; j++){
					System.out.print(temp[j]);
					System.out.print('\t');
				}
				System.out.println("----------------------------");
				System.out.println();
			}*/
			
			
			
			
			resultNode = createResultNode(result, idata, null);//--commented for Testing
				
		
		}
		
		
		return resultNode;
			
		}
				
			
		
		
			
	
	/*
	 *  The class to listen to progress, monitor and algorithms events.
    */
   private class Listener extends DialogListener implements AlgorithmListener {
       
       public void valueChanged(AlgorithmEvent event) {
           switch (event.getId()) {
               case AlgorithmEvent.SET_UNITS:
                   progress.setUnits(event.getIntValue());
                   progress.setDescription(event.getDescription());
                   break;
               case AlgorithmEvent.PROGRESS_VALUE:
                   progress.setValue(event.getIntValue());
                   progress.setDescription(event.getDescription());
                   break;
           }
       }
       
       public void actionPerformed(ActionEvent e) {
           String command = e.getActionCommand();
           if (command.equals("cancel-command")) {
               gsea.abort();
               progress.dispose();
           }
       }
       
       public void windowClosing(WindowEvent e) {
           gsea.abort();
           progress.dispose();
       }
   }
	
	
   private DefaultMutableTreeNode createResultNode(AlgorithmData result, IData idata, GSEAExperiment experiment) {
		DefaultMutableTreeNode node = null;
		
		
			node = new DefaultMutableTreeNode("GSEA-Significant Gene sets");
			addPValueGraphImage(node, result, (String[][])result.getObjectMatrix("geneset-pvals"));
			addTableViews(node, result, experiment, idata);
			addExpressionImages(node, result, this.experiment);
		
			
		return node;
	}
	
   private void addPValueGraphImage(DefaultMutableTreeNode root, AlgorithmData result, String[][]pValues){
	   
	
	   PValuesGraphViewer pvg=new PValuesGraphViewer(0, 500, 0, 500, 0, pValues.length, 0, 1, 100, 100, 100, 100, "Gene set p-Values Plot", "Gene set", "p-Values", pValues);
	   root.add(new DefaultMutableTreeNode(new LeafInfo("Geneset p-Value Graph", pvg)));
	   
	  // PValueViewer pvv=new PValueViewer(this.experiment, pValues);
	   
   }
   
   

   private void addExpressionImages(DefaultMutableTreeNode root,  AlgorithmData result, Experiment experiment) {

					
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
		GSEAExperimentViewer expViewer = new GSEAExperimentViewer(experiment, geneset_clusters);
		GSEACentroidViewer centroidViewer=new GSEACentroidViewer(experiment, geneset_clusters);
		
		float [][] means = result.getMatrix("cluster-means").A;
    	float [][] vars = result.getMatrix("cluster-variances").A;
        centroidViewer.setMeans(means);
        centroidViewer.setVariances(vars);

		
		DefaultMutableTreeNode clusterNode;
		
		Vector gene_set_names=result.getVector("gene-set-names");
		
		//Loop generates a folder for every gene set. Each folder/geneset has an experiment viewer, centroid viewer and table viewer
		  for (int i=0; i<gene_set_names.size(); i++) {
	            clusterNode = new DefaultMutableTreeNode((String)gene_set_names.get(i));
	            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Expression Image", expViewer, new Integer(i))));
	            //Will be uncommented when Centroid and expression graph viewers are implemented in GSEA
	            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Centroid Graph", centroidViewer, new CentroidUserObject(i,CentroidUserObject.VARIANCES_MODE))));
	            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Expression Graph", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
	        //    clusterNode.add(new DefaultMutableTreeNode(resultMatrix[i][1]));
	            
	            node.add(clusterNode);
	        }
	 	 		
		root.add(node);
	}

   
   /**
    * @TO DO: Move to GSEA.java
    *  Calculates means for the clusters
    */
   private FloatMatrix getMeans(FloatMatrix data, int [][] clusters){
       FloatMatrix means = new FloatMatrix(clusters.length, data.getColumnDimension());
       for(int i = 0; i < clusters.length; i++){
           means.A[i] = getMeans(data, clusters[i]);
       }
       return means;
   }
  
   
   /**
    *  TO DO: Move to GSEA.java
    *  Returns a set of means for an element
    */
   private float [] getMeans(FloatMatrix data, int [] indices){
       int nSamples = data.getColumnDimension();
       float [] means = new float[nSamples];
       float sum = 0;
       float n = 0;
       float value;
       for(int i = 0; i < nSamples; i++){
           n = 0;
           sum = 0;
           for(int j = 0; j < indices.length; j++){
               value = data.get(indices[j],i);
               if(!Float.isNaN(value)){
                   sum += value;
                   n++;
               }
           }
           if(n > 0)
               means[i] = sum/n;
           else
               means[i] = Float.NaN;
       }
       return means;
   }
   
  

   /** Returns a matrix of standard deviations grouped by cluster and element
    * @param data Expression data
    * @param means calculated means
    * @param clusters cluster indices
    * @return
    */
   private FloatMatrix getVariances(FloatMatrix data, FloatMatrix means, int [][] clusters){
       int nSamples = data.getColumnDimension();
       FloatMatrix variances = new FloatMatrix(clusters.length, nSamples);
       for(int i = 0; i < clusters.length; i++){
           variances.A[i] = getVariances(data, means, clusters[i], i);
       }
       return variances;
   }
   
   /** Calculates the standard deviation for a set of genes.  One SD for each experiment point
    * in the expression vectors.
    * @param data Expression data
    * @param means previously calculated means
    * @param indices gene indices for cluster members
    * @param clusterIndex the index for the cluster to work upon
    * @return
    */
   private float [] getVariances(FloatMatrix data, FloatMatrix means, int [] indices, int clusterIndex){
       int nSamples = data.getColumnDimension();
       float [] variances = new float[nSamples];
       float sse = 0;
       float mean;
       float value;
       int n = 0;
       for(int i = 0; i < nSamples; i++){
           mean = means.get(clusterIndex, i);
           n = 0;
           sse = 0;
           for(int j = 0; j < indices.length; j++){
               value = data.get(indices[j], i);
               if(!Float.isNaN(value)){
                   sse += (float)Math.pow((value - mean),2);
                   n++;
               }
           }
           if(n > 1)
               variances[i] = (float)Math.sqrt(sse/(n-1));
           else
               variances[i] = 0.0f;
       }
       return variances;
   }
   
   
   
   
   /**
    * 
    * 
    * 
    */
   private int[][]GenesettoProbeMapping(GeneData[] gData, Geneset[]gset){
	   
	   int genesetIndex=0;
	 
	   int geneDataElementIndex=0;
	 //  Vector geneDataElement=gData[0].getAllGeneDataElement();
	   
	   //The size of geneset_clusters would be equal to the number of gene sets 
	   geneset_clusters=new int[gset.length][];
	   
	   //Vector containing the indices of probes mapping to a gene
	   Vector probe_mappings;
	   
	   //integer array of probe_mappings
	   int[]probe_mappings_array;
	   
	   //Iterate over gene sets
	   while(genesetIndex<gset.length){
		   int genesetElementIndex=0;
		   //Fetch the vector containing  gene set elements
		   Vector gsElementVector=gset[genesetIndex].getGenesetElements();
		   probe_mappings=new Vector();
		   //Iterate over the elements in the gene sets
		   while(genesetElementIndex<gsElementVector.size()){
			   //Retrieve the gene name from the gene set
			   GeneSetElement gselement=(GeneSetElement)gsElementVector.get(genesetElementIndex);
			   String Gene=(String)gselement.getGene();
		//	   System.out.println("Gene:"+Gene);
			   //Retrieve the index of this gene from Gene Data Element 
			   GeneDataElement gde=(GeneDataElement)gData[0].getGeneDataElement(Gene);
			   
			   //Populate the probe_mappings vector here
			   for(int index=0; index<gde.getProbePosition().size(); index++){
				   probe_mappings.add((Integer)gde.getProbePosition().get(index));
			//	   System.out.print(gde.getProbePosition().get(index));
			//	   System.out.print('\t');
			   }
			  // System.out.println();
			  genesetElementIndex=genesetElementIndex+1;
			   
		   } //Gene set elements while loop ends
		   //Populate the probe_mappings_array
		   probe_mappings_array=new int[probe_mappings.size()];
		   for(int index=0; index<probe_mappings.size(); index++){
			   probe_mappings_array[index]=((Integer)probe_mappings.get(index)).intValue();
		   }
		   
		   geneset_clusters[genesetIndex]=probe_mappings_array;
		   genesetIndex=genesetIndex+1;
		   
	   }//outer while loop ends
	   
	   
	   
	   return geneset_clusters;
   }
   
   
   private void addTableViews(DefaultMutableTreeNode root, AlgorithmData result, GSEAExperiment experiment, IData data) {
   	DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
   	GSEATableViewer tabViewer;
   	String[][]pVals =(String[][]) result.getObjectMatrix("geneset-pvals");
   	String[]headernames={"Gene Set", "Lower-pValues (Under-Enriched)", "Upper-pValues (Over-Enriched)"};
   	
   	
   //Display Significant Gene Sets
   	tabViewer = new GSEATableViewer(headernames,pVals, root,experiment);
   	node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Gene Sets", tabViewer, new Integer(0))));
   
   //Display Excluded Gene sets
   	Vector temp=result.getVector("excluded-gene-sets");
   	String[][]_dummy=new String[temp.size()][1];
   	
   	for(int i=0; i<temp.size(); i++){
   		
   			_dummy[i][0]=(String)temp.get(i);
   			//System.out.println("Excluded gene set name:"+(String)temp.get(i));
   		
   	}
    String[]header1={"Excluded Gene Sets"};  	
  	tabViewer = new GSEATableViewer(header1,_dummy);
   	node.add(new DefaultMutableTreeNode(new LeafInfo("Excluded Gene Sets", tabViewer, new Integer(0))));
 
   	
   	
  //Display Collapse Probe to Gene 
   String[]header2=new String[max_columns+1];
    header2[0]="Gene";
   	for(int i=0; i<max_columns; i++){
   		header2[i+1]="Probes";
   	}
   	
   	
    tabViewer=new GSEATableViewer(header2,geneToProbeMapping, root, experiment);
    node.add(new DefaultMutableTreeNode(new LeafInfo("Probe to Gene Mapping", tabViewer, new Integer(0))));
   	
   	
   	root.add(node);
   }

   
		
	
	/**
	 * checkFileNameExtension returns the extension of the file.
	 * @param fileName
	 * @return
	 */
	
	public String checkFileNameExtension(String fileName){
		String extension=fileName.substring(fileName.lastIndexOf('.')+1, fileName.length());
		//System.out.println("Extension:"+extension);	
		return extension;
	}
	
	public static void main(String[] args){
		
		
	}
	
	
	}	



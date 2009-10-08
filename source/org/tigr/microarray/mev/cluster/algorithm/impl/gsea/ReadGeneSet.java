package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEAConstants;
import org.tigr.microarray.mev.cluster.gui.impl.util.MatrixFunctions;
import org.tigr.microarray.mev.file.StringSplitter;
import org.tigr.util.FloatMatrix;


/**
 * 
 * @author Sarita Nair
 * ReadGeneSet parses user provided gene set files and populates the appropriate 
 * data structures.
 * 
 * Currently reads MIT provided GMX and GMT files. It also accepts TXT format files.
 * The GMX and GMT files have Gene Symbols as identifiers. 
 * TXT format is similar to the GMT format files, with two exceptions  
 * 1. There is NO description column after gene set name column. 
 * 2. TXT format uses ENTREZ Gene as identifier
 *  
 *
 */

public class ReadGeneSet {
	Geneset[] set=null;
	String[][]excludedGenes;
	Vector excluded_geneSets=new Vector();


	GeneSetElement gsElement;
	Vector genesetNames=new Vector();
	String filePath;
	String fileExtension;

	public ReadGeneSet(String extension, String fPath){
		filePath=fPath;
		fileExtension=extension;

	}

	/**
	 * 
	 * @param filePath
	 * @throws Exception
	 * This function reads the gmx format files. There can be one or many
	 * gene sets in a file. It creates an array of Genesets. The number of gene
	 * sets is determined from the first row of the gmx file, which is a tab seperated
	 * list of gene set names. 
	 * 
	 * The genes present within a Geneset are stored in GeneSetElement. Each row (GeneSetElement)
	 * is added to the corresponding GeneSet.
	 * 
	 * The names of All the UNIQUE genes present in the gene set is also stored in the
	 * first GeneSet, as a vector.
	 * 
	 * The names of All the genesets is also stored as a vector in the first GeneSet    
	 * 
	 * 
	 * 
	 */

	public Geneset[] read_GMXformatfile(String filePath) throws Exception{

		int num_of_linesinFile=this.getCountOfLines(new File(filePath));
		int num_of_rows=num_of_linesinFile-2;

		String temp;
		int curpos=0;

		BufferedReader bread=new BufferedReader(new FileReader(new File(filePath)));

		String currentLine=bread.readLine();
		parseGenesetNamesfromGMX(currentLine);

		int num_geneSets=this.genesetNames.size();


		set=new Geneset[num_geneSets];

		for(int i=0; i<set.length;i++){

			String gsetName=(String)genesetNames.get(i);
			set[i]=new Geneset();
			set[i].setGeneSetName(gsetName);
		}

	//	set[0].setAllGenesetNames(genesetNames);
		bread.readLine();//Ignore the second line, which contains description
		StringSplitter split=new StringSplitter(GSEAConstants.TAB_CHAR);

		while((currentLine=bread.readLine())!=null){
			split.init(currentLine);
			int index=0;


			while(index<num_geneSets){
				Vector gene=new Vector();
				temp=split.nextToken().trim();
				if(temp!=null && !temp.equalsIgnoreCase("null")&& !temp.equalsIgnoreCase("")&&!temp.equalsIgnoreCase("na")){
					gsElement=new GeneSetElement(String.valueOf(curpos), temp);
//					if(!set[index].getGenesinGeneset().contains(temp))//Commented this. IGeneset does NOT have this functionality any more.
//						set[index].setGenesinGeneset(temp);

				}else{
					gsElement=new GeneSetElement(String.valueOf(curpos), "NA");

				}
				set[index].setGeneSetElement(gsElement, curpos);

				index=index+1;

			}
			curpos=curpos+1;


		}
		bread.close();

		/*	System.out.println("Printing Gene sets.......................");
		for(int i=0; i<set.length; i++){
			System.out.println("Gene set is:"+set[i].getGeneSetName(i));
			for(int j=0; j<set[i].getGenesetElements().size(); j++){
				System.out.println("setElement:"+set[i].getGeneSetElement(j).getGene());
			}
		}
		System.out.println("Printing Gene sets...............ENDS........");*/

		//System.out.println("unique genes:"+set[0].getAllGenesinGeneset().size());	
		return set;
	}


	/**
	 * read_GMTformatfile reads a GMT format gene set file. There can be one or many
	 * gene sets in a file. It creates an array of Genesets.
	 * 
	 * The length of the gene sets may be different. Unlike read_GMXformatfile, where 
	 * we would have prior knowledge of the largest number of genes per geneset(considered
	 * equal to the number of non null rows in file minus the comment and header lines);
	 * we do not have that luxury here.
	 *   
	 *
	 * 
	 * 
	 * @param filePath
	 * @return
	 * @throws Exception
	 */






	public Geneset[] read_GMTformatfile(String filePath)throws Exception{

//		System.out.println("read_GMTformatfiles");
		String temp;

		parseGenesetNamesfromGMT(filePath);
		BufferedReader bread=new BufferedReader(new FileReader(new File(filePath)));
		String currentLine;

		int index=0;
		int num_geneSets=this.genesetNames.size();
		//	System.out.println("gene set size:"+num_geneSets);

		set=new Geneset[num_geneSets];

		for(int i=0; i<num_geneSets;i++){

			String gsetName=(String)genesetNames.get(i);
			set[i]=new Geneset();
			set[i].setGeneSetName(gsetName);
			//System.out.println("gene set name:"+set[i].geneSetName+":"+i);
		}

	//	set[0].setAllGenesetNames(genesetNames);

		StringSplitter split=new StringSplitter(GSEAConstants.TAB_CHAR);
		while((currentLine=bread.readLine())!=null&&currentLine.trim().length()!=0){
			//System.out.print("currentline:"+currentLine);
			split.init(currentLine);
			String geneSetName=split.nextToken().trim();//First column has gene set names
			split.nextToken();//Second column contains descriptions
			int curpos=0;


			while(split.hasMoreTokens()&& genesetNames.contains(geneSetName)){
				temp=split.nextToken().trim();
				if(temp!=null && !temp.equalsIgnoreCase("null")&& !temp.equalsIgnoreCase("") && !temp.equalsIgnoreCase("na")){
					gsElement=new GeneSetElement(String.valueOf(curpos), temp);
					//System.out.println("Gene setname:"+set[index].geneSetName);
					//System.out.println("gene name is:"+temp);
					//System.out.println("index:"+index);
					//System.out.println("curpos:"+curpos);
					set[index].setGeneSetElement(gsElement, curpos);
//					if(!set[index].getGenesinGeneset().contains(temp)){--commented because IGeneset does NOT have this functionality any more 
//						set[index].setGenesinGeneset(temp);
//					}
					curpos=curpos+1;
				}

			}

			index=index+1;


		}
		bread.close();
		/*	System.out.println("Printing Gene sets. in read_GMTFile function......................");
		for(int i=0; i<set[0].getAllGenesetNames().size(); i++){
			System.out.println("Gene set is:"+set[0].getGeneSetName(i));
			for(int j=0; j<set[i].getGenesinGeneset().size(); j++){
				System.out.println("Gene in gene set is:"+(String)set[i].getGenesinGeneset().get(j));
			}
		}
		System.out.println("Printing Gene sets...............ENDS........");*/

		return set;
	}

	/**
	 * read_TXTformatfile functions similar to the read_GMTformatfile
	 * @param filePath
	 * @return
	 * @throws Exception
	 */

	public Geneset[] read_TXTformatfile(String filePath)throws Exception{


		String temp;
       
		parseGenesetNamesfromGMT(filePath);
		BufferedReader bread=new BufferedReader(new FileReader(new File(filePath)));
		String currentLine;

		int index=0;
		int num_geneSets=this.genesetNames.size();
		

		set=new Geneset[num_geneSets];

		for(int i=0; i<num_geneSets;i++){

			String gsetName=(String)genesetNames.get(i); 

			set[i]=new Geneset();
			set[i].setGeneSetName(gsetName);
			//System.out.println("gene set name:"+set[i].geneSetName+":"+i);
		}
		 getCountOfLines(new File(filePath));
//		set[0].setAllGenesetNames(genesetNames);

		StringSplitter split=new StringSplitter(GSEAConstants.TAB_CHAR);
		while((currentLine=bread.readLine())!=null && currentLine.trim().length()!=0){
			//System.out.print("currentline:"+currentLine);
			currentLine=currentLine.trim();
			split.init(currentLine);
			String geneSetName=split.nextToken().trim();//First column has gene set names
			//geneSetName=geneSetName.trim();
			int curpos=0;


			while(split.hasMoreTokens()&& genesetNames.contains(geneSetName)){
			//	System.out.println("gene set name:"+geneSetName);
				//System.out.println("index:"+index);
				//Remove any leading/trailing white spaces.
				temp=split.nextToken().trim();
				if(temp!=null && !temp.equalsIgnoreCase("null")&& !temp.equalsIgnoreCase("") && !temp.equalsIgnoreCase("na")&& temp.length()!=0){

					gsElement=new GeneSetElement(String.valueOf(curpos), temp);

					//System.out.println("Gene setname:"+set[index].geneSetName);

					//System.out.println("gene name is:"+temp);

					//System.out.println("index:"+index);


					//System.out.println("curpos:"+curpos);
					set[index].setGeneSetElement(gsElement, curpos);
					curpos=curpos+1;
				}

			}

			index=index+1;


		}
		bread.close();

	
		return set;
	}
	
	/**
	 * Reads multiple gene set files. The files can be a mix of gmt and gmx OR multiple txt formats.
	 * Each type is associated with a different gene identifier and so cannot be mixed
	 * Returns an array of gene sets
	 * @param fileList (Names of files)
	 * @param dirPath (Directory path)
	 * @return
	 */
	public Geneset[] readMultipleFiles(String[] fileList, String dirPath)throws Exception{
		
		HashMap<String,Geneset> geneSetList=new HashMap<String, Geneset>();
		int num_genesets=0;
		Geneset[]gSet;
		for(int index=0; index<fileList.length; index++){
			String fileExtension=checkFileNameExtension((String)fileList[index]);
			String filePath=dirPath+"/"+(String)fileList[index];
			//System.out.println("filePath:"+filePath);
			if(fileExtension.equalsIgnoreCase("gmt")){
				gSet=read_GMTformatfile(filePath);

			}else if(fileExtension.equalsIgnoreCase("gmx")){
				gSet=read_GMXformatfile(filePath);
			}else{
				//System.out.println("Read TXT format files");
				gSet=read_TXTformatfile(filePath);
			}
			//Add individual gene set to arraylists		
			for(int i=0; i<gSet.length; i++){
				
				if(!geneSetList.containsKey(gSet[i].getGeneSetName()))
						geneSetList.put(gSet[i].getGeneSetName(), gSet[i]);
			}

			
		}
		

		gSet=new Geneset[geneSetList.size()];
		Iterator it=geneSetList.keySet().iterator();
		int index=0;
		
		while(it.hasNext()){
            
			gSet[index]=(Geneset)geneSetList.get(it.next());
			index=index+1;
			
		}
	/*	System.out.println("Gene set array size:"+gSet.length);
		System.out.println("Printing Gene sets. in readMultipleFile function......................");
		for(int i=0; i<gSet[0].getAllGenesetNames().size(); i++){
			System.out.println("Gene set is:"+gSet[0].getGeneSetName());
			for(int j=0; j<gSet[i].getGenesinGeneset().size(); j++){
				System.out.println("Gene in gene set is:"+(String)gSet[i].getGenesinGeneset().get(j));
			}
		}
		System.out.println("Printing Gene sets...............ENDS........");*/

		
		
		
		
        return(gSet);

		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	/**
	 * 
	 * @param gset
	 * @return
	 */
	public int getMaxnumberofGenesinGeneSet(Geneset[]gset){
		int max=0;

		for(int i=0; i<gset.length;i++){
			int length=gset[i].getGenesinGeneset().size();
			if(length>max)
				max=length;

		}

		return max;	


	}






	/**
	 * parseGenesetNamesfromGMX
	 * This function parses the gene set names from the gene set file.
	 * In GMX format files, the first line is the names of the gene sets. 
	 * Due to the format, it becomes complicated down the line to leave out the gene
	 * sets which have null or NA or empty string as names.
	 *    
	 *    
	 * @param line
	 */


	public void parseGenesetNamesfromGMX(String line){

		String temp;
		Vector _tempGeneset=new Vector();
		int index=0;

		StringSplitter split=new StringSplitter(GSEAConstants.TAB_CHAR);
		split.init(line);
		while(split.hasMoreTokens()&&(temp=split.nextToken())!=null&&temp.trim().length()!=0){
			//Remove any leading or trailing spaces.
			temp=temp.trim();
//			if(!_tempGeneset.contains(temp)){
//				_tempGeneset.add(index,temp);
//				index=index+1;
//			}
//			
			if(!genesetNames.contains(temp)){
				genesetNames.add(index,temp);
				index=index+1;

			}

			
			
		}
	

	}


	/**
	 * parseGenesetNamesfromGMT
	 * Parses the names of the gene sets in the file. 
	 * @filePath
	 * 
	 * 
	 *  
	 */
	public void parseGenesetNamesfromGMT(String filePath) throws IOException{
		BufferedReader bread=new BufferedReader(new FileReader(new File(filePath)));
		StringSplitter split=new StringSplitter(GSEAConstants.TAB_CHAR);
		String currentLine;
		Vector _tempGeneset=new Vector();

		int index=0;

		while((currentLine=bread.readLine())!=null && currentLine.trim().length()!=0){
			currentLine=currentLine.trim();
			split.init(currentLine); 

			//Remove any leading or trailing spaces
			String _temp=(split.nextToken()).trim();


			if(_temp!=null){

				if(!genesetNames.contains(_temp)){
					genesetNames.add(index,_temp);
					index=index+1;

				}

			}

		}

		bread.close();
		
	}

	/**
	 * Returns number of lines in the specified file.
	 */
	public int getCountOfLines(File file) throws IOException {

		int count = 0;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String currentLine;

		while ((currentLine = reader.readLine()) != null) {

			count++;
		}
		reader.close();
	
		return count;
	}

	/**
	 * removeGenesNotinExpressionData returns a GeneSet array, containing all gene sets as in the
	 * original one. The differEnce is that 
	 * 
	 * 1. The new Genesets would have ONLY the genes present in expression data.
	 * 2. If a Geneset does NOT contain any elements, it is still included, but the zeroth GeneSetElement is set to null
	 * 
	 * 
	 * @param gset
	 * @param genesInExpressionData
	 * @return
	 */



	public Geneset[] removeGenesNotinExpressionData(Geneset[]gset, Vector genesInExpressionData){
		Geneset[]newGeneSet=new Geneset[gset.length];
		Vector geneSetElement;
		//Vector geneSetNames=gset[0].getAllGenesetNames();
		int setIndex=0;
	/*	System.out.println("Printing original gene set");
    	for(int i=0; i<gset.length; i++){
    		System.out.print("Gene set name:"+gset[i].geneSetName);
    		System.out.print('\t');
    		for(int j=0; j<gset[i].getGenesinGeneset().size(); j++){
    			System.out.print((String)gset[i].getGenesinGeneset().get(j));
    			System.out.print('\t');
    		}
    		System.out.println();
    	}
    	System.out.println("Printing original gene set......ENDS");*/



		while(setIndex<gset.length){

			//Get the gene set name 
			String gsetName=(String)gset[setIndex].getGeneSetName();
			//Initialize the new gene sets
			newGeneSet[setIndex]=new Geneset();
			newGeneSet[setIndex].setGeneSetName(gsetName);

		
			//Check if the gene set is empty OR name of gene set is NA/null/"". If so, add the gene set
			//to the new list, with zeroth gene set element set to null
			if(gset[setIndex].getGenesetElements()==null||gsetName.equalsIgnoreCase("na")||gsetName.equalsIgnoreCase("null")||gsetName.length()<1 ){
				GeneSetElement gsElement=null;

				newGeneSet[setIndex].setGeneSetElement(gsElement, 0);
			//	newGeneSet[setIndex].setGenesinGeneset(null);--deprecated function

			} else{
				//Get the gene set elements coRresponding to that gene set
				ArrayList<IGeneSetElement> temp=gset[setIndex].getGenesetElements();

				int geneindex=0;
				//Go through each element to check if it (gene represented by it)exists in the expression data. If so, create a genesetElement and add it to the vector
				//genesinGeneSet. Each GeneSetElement corresponds to a gene
				for(int j=0; j<temp.size(); j++){

					String gene=((GeneSetElement)temp.get(j)).getGene();
					if(genesInExpressionData.contains(gene) && !gene.equalsIgnoreCase("na")){
						GeneSetElement gsElement=new GeneSetElement(String.valueOf(geneindex), gene);


						newGeneSet[setIndex].setGeneSetElement(gsElement, geneindex);
						
						//increment the gene index
						geneindex=geneindex+1;
					}



				}
				temp.clear();

			}//End of else loop
			setIndex=setIndex+1;

		}

	/*		System.out.println("Printing gene sets after removing unwanted genes");
    	for(int i=0; i<newGeneSet.length; i++){
    		System.out.print(newGeneSet[i].getGeneSetName()+":");
    		System.out.print('\t');
    		for(int j=0; j<newGeneSet[i].getGenesinGeneset().size(); j++){
    			System.out.print((String)newGeneSet[i].getGenesinGeneset().get(j));
    			System.out.print('\t');
    		}
    		System.out.println();
    	}*/




		return newGeneSet;

	}







	/**
	 * removeGenesetsWithoutMinimumGenes
	 * This function removes the rows (genesets) which do not have atleast the minimum
	 * number of genes, as specified by user
	 * @param excludedgeneSets
	 * @param oldGenesets
	 * @return
	 */ 


	public Geneset[]removeGenesetsWithoutMinimumGenes(Vector excludedgeneSets, Geneset[]oldGenesets){


		Geneset[]newGenesets=new Geneset[oldGenesets.length-excludedgeneSets.size()];
		Vector geneSetNames=new Vector();

		int oldIndex=0;
		int newIndex=0;


		while(oldIndex <oldGenesets.length){
			//Get the gene set name 
			String gsetName=(String)oldGenesets[oldIndex].getGeneSetName();
			if(!excludedgeneSets.contains(gsetName)){
				geneSetNames.add(newIndex, gsetName);
				//Get the gene set elements coRresponding to that gene set
				ArrayList<IGeneSetElement> temp=oldGenesets[oldIndex].getGenesetElements();
				newGenesets[newIndex]=new Geneset();
				int geneindex=0;
				// Each GeneSetElement corresponds to a gene
				for(int j=0; j<temp.size(); j++){

					String gene=((GeneSetElement)temp.get(j)).getGene();

					GeneSetElement gsElement=new GeneSetElement(String.valueOf(j), gene);

					newGenesets[newIndex].setGeneSetName(gsetName);
					newGenesets[newIndex].setGeneSetElement(gsElement, j);
				//	newGenesets[newIndex].setGenesinGeneset(gene);


				}
				temp.clear();
				newIndex=newIndex+1;
			}//If loop ends
			oldIndex=oldIndex+1;


		}//While ends
		if(geneSetNames.isEmpty()){
			//newGenesets[0].setAllGenesetNames(geneSetNames);--deprecated
	
			String eMsg="<html>All the gene sets FAIL to pass the minimum genes cutoff. <br>"+ 
			"<html>You can try lowering the cutoff and running the analysis. </html>";
			JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.ERROR_MESSAGE);

		
		}

		return newGenesets;
	}

	/**
	 * createAssociationMatrix generates an association matrix of genes and gene sets.
	 * This is a floatMatrix (filled with (1 or 0)) with rows=number of genesets and cols= number of genes.
	 * If a gene is present in a gene set, matrix[row][col]=1.
	 * 
	 * FloatMatrix class sets all elements to 0 at the time of instantiation, if no value is provided. So for genes
	 * not present in the geneset, no need to explicitly set the matrix element to 0.
	 * 
	 * The matrix that is returned does not contain the gene sets which have rowSums < min_genes 
	 * 
	 * @param gSets
	 * @param unique_genes_in_dataset
	 * @return
	 */
	public FloatMatrix createAssociationMatrix(Geneset[]gSets, Vector unique_genes_in_dataset, int min_genes){

		
		// Rowsize is equal to the number of genesets
		int rowSize=(new GSEAUtils().getGeneSetNames(gSets)).size();
		//	System.out.println("row size of Amat:"+rowSize);
		
		//Colsize is equal to the number of genes in the data set
		int colSize=unique_genes_in_dataset.size();
		//System.out.println("col size of Amat:"+colSize);
		
		//Initial Association Matrix
		FloatMatrix _tempMatrix=new FloatMatrix(rowSize, colSize);
		//Final matrix after we remove gene sets NOT containing minimum number of genes
		FloatMatrix aMatrix=null;
		Vector _tempgeneset=new Vector();
		int rowIndex=0;
		int colIndex=0;

		//Loop through the rows (gene sets)
		while(rowIndex<rowSize){

			//Check if the Gene set is empty. If so, all the columns of the Amat would be 0
			if(gSets[rowIndex].getGenesinGeneset().size()<1){
				for(int j=0; j<colSize; j++)
					_tempMatrix.set(rowIndex, j, 0);
			}else{

				ArrayList<String> _genesinGeneset=new ArrayList<String>();
				_genesinGeneset=gSets[rowIndex].getGenesinGeneset();

				for(int j=0; j<_genesinGeneset.size(); j++){
					String Gene=(String)_genesinGeneset.get(j);
					Gene=Gene.trim();


					for(int k=0; k<colSize; k++){
						String uniq_genes=(String)unique_genes_in_dataset.get(k);
						uniq_genes=uniq_genes.trim();
						//Set the matrix cell to 1 if gene in the gene set also present in data set
						if(uniq_genes.equals(Gene)){
							_tempMatrix.set(rowIndex, k, 1);

						}

					}


				}



			}


			rowIndex=rowIndex+1;
		}


		//Remove the rows whose sum < number of required genes as specified by the user. 
		MatrixFunctions matrixFunc=new MatrixFunctions();
		//Returns the rowsums of the Amat generated earlier.
		Vector rowSums=matrixFunc.getRowSums(_tempMatrix);
		int geneSetIndex=0;
		int index=0;
		
		
		//Loops through the rowSums vector and populates the excluded genes vector
		while(index<rowSums.size()){
			//If rowSums < min_genes, do not include the row in the aMat. Move on
			if(((Float)rowSums.get(index)).floatValue()<min_genes){
				_tempgeneset.add(gSets[index].getGeneSetName());

			}
			index=index+1;
		}
		
		if(_tempgeneset.size()==rowSize){
			String eMsg="<html> None of the genes in the gene set that you supplied, match the genes in the expression data. <br>"+
					"<html> One of the things to check for would be if the expression data and gene set contain genes from the <br>"+
					"<html>same organism. Sorry, you cannot proceed with the analysis </br></html>";
			JOptionPane.showMessageDialog(new JFrame(),eMsg , "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		//aMatrix may have lesser rows, depending on whether all gene sets have minimum number of genes as user specified.
		aMatrix=new FloatMatrix(rowSize-_tempgeneset.size(), colSize);
		index=0;
		while(index<rowSums.size()){
			if(((Float)rowSums.get(index)).floatValue()>=min_genes){
				aMatrix.setMatrix(geneSetIndex, geneSetIndex, 0, colSize-1, _tempMatrix.getMatrix(index, index, 0, colSize-1));
				geneSetIndex=geneSetIndex+1;
			}
			index=index+1;

		}
		//Forces garbage collection ?
		_tempMatrix=null;

		//Sets the excluded gene sets in the vector
		
		setExcludedGeneSets(_tempgeneset);
		//Return the Association matrix which has only those genesets, having minimum number of genes, as specified by user.
		return aMatrix;
	}

	/**
	 * setExcludedGeneSets contains the gene sets which have been dropped from the analysis.
	 * This can happen when the genesets do not have the minimum number of genes as specified by the user.
	 * 
	 * @param genesets
	 */  

	public void setExcludedGeneSets(Vector genesets){
		this.excluded_geneSets=genesets;
	}

	public Vector getExcludedGeneSets(){
		return this.excluded_geneSets;
	}

	/**
	 * getExcludedGenesfromGeneset returns the genes which have been removed from gene sets.
	 * The reason being that they are not present in the expression data.
	 * 
	 * @return
	 */
	public String[][]getExcludedGenesfromGeneset(){
		return excludedGenes;
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
	



	public static void main(String[] str){

		/*	JFileChooser fileChooser = new JFileChooser(
				SuperExpressionFileLoader.DATA_PATH);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retVal = fileChooser.showOpenDialog(null);
		File selectedFile;

		if (retVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			ReadGeneSet_Old rgeneset=new ReadGeneSet_Old("gmx",selectedFile.getAbsolutePath());
			try{
			Geneset[]gset=rgeneset.read_GMXformatfile(selectedFile.getAbsolutePath());
			}catch(Exception e){
				e.printStackTrace();
			}
		}*/


	}





}

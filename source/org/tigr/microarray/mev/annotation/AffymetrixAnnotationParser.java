package org.tigr.microarray.mev.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.tigr.util.StringSplitter;

public class AffymetrixAnnotationParser {
	
	Vector columnNames=new Vector();
	Hashtable<String, MevAnnotation> annoHash;
	
	
	public static AffymetrixAnnotationParser createAnnotationFileParser(File affyFile) throws IOException {
		AffymetrixAnnotationParser newParser = new AffymetrixAnnotationParser();
       	newParser.loadAffyAnnotation(affyFile);
		return newParser;
	}

	/**
	 *  
	 * @author Sarita Nair
	 */
    private AffymetrixAnnotationParser() {}

    private void loadAffyAnnotation(File annotationFile)throws IOException{
    	StringSplitter ss = new StringSplitter(',');
    	String currentLine, probeID;
    	int counter = 0;
    	MevAnnotation annotationObj; 
    	this.columnNames=getColumnHeader(annotationFile);
    	String _temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    	Vector<String> _tmpGO=new Vector<String>();

    	int numLines = this.getCountOfLines(annotationFile);
    	annoHash = new Hashtable<String, MevAnnotation>(numLines);
    	BufferedReader reader = new BufferedReader(new FileReader(annotationFile));
    	while ((currentLine = reader.readLine()) != null) {

    		annotationObj = new MevAnnotation(); 
    		probeID = "";

    		//Skip the lines which start with #.
    		while(currentLine.startsWith("#")){
    			currentLine=reader.readLine();
    		}

    		ss.init(currentLine);

    		for(int i = 0; i < columnNames.size(); i++){

    			if(ss.hasMoreTokens()){
    				_temp = ss.nextToken();
    			}
    			String field=((String)columnNames.get(i));
    			int index=columnNames.indexOf((Object)field);
    			//System.out.println("index:"+index);
    			//System.out.println("field:"+field);
    			//System.out.println("_temp:"+_temp);

    			Vector<String> _tmpGo = new Vector<String>();
    			if(field.equalsIgnoreCase(AnnotationFieldConstants.CLONE_ID)&&index==i){
    				probeID=_temp;
    				annotationObj.setCloneID(_temp);
    				System.out.println("clone id:"+probeID);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENBANK_ACC)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setGenBankAcc(_temp);
    				//	System.out.println("Genbank acc:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.UNIGENE_ID)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setUnigeneID(_temp);
    				//	System.out.println("Unigene id:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENE_TITLE)&&index==i){ 
    				if(_temp==""){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setGeneTitle(_temp);
    				//System.out.println("Gene_Title:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENE_SYMBOL)&&index==i){ 
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setGeneSymbol(_temp);
    				//	System.out.println("Gene Symbol:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.CYTOBAND)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				setAlignmentInfo(_temp, annotationObj);
    				//	System.out.println("Cytoband:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.ENTREZ_ID)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setLocusLinkID(_temp);
    				System.out.println("Entrez id:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.REFSEQ_ACC)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				String mRnaRefSeqs[] = parsemRnaIds(_temp);
    				annotationObj.setRefSeqTxAcc(mRnaRefSeqs);
    				//	System.out.println("RefSeq Acc:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GO_TERMS)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				_tmpGo = parseGoTerms(_temp, "///"); 
    				annotationObj.setGoTerms((String[]) _tmpGo.toArray(new String[_tmpGo.size()]));
    				//	System.out.println("GO_Terms:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.TGI_TC)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setTgiTC(_temp);
    				//	System.out.println("TGI_TC:"+_temp);
    			}


    		}//For loop ends...
    		if(probeID!=null)
    			annoHash.put(probeID, annotationObj);
    		else{
    			String eMsg = "<html>Probe ID Missing..This is a REQUIRED field <br>" +
    			"<html>The following descriptor was found for this probe<br> "+
    			annotationObj.getProbeDesc();
    			JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.INFORMATION_MESSAGE);

    		}	




    	}//While loop ends

    	reader.close();
    }
    
    
    public Hashtable<String, MevAnnotation> getAffyAnnotation(){
    	return annoHash;
    }
    
    
    
    private Vector getColumnHeader(File targetFile)throws IOException {
    	System.out.println("getColumnHeader");
    	BufferedReader reader = new BufferedReader(new FileReader(targetFile));
    	StringSplitter split = new StringSplitter(',');
    	String currentLine;
    	Vector columnNames=new Vector();
    	
    	
    	//Skip the lines that begin with #
    	while((currentLine=reader.readLine()).startsWith("#")){
    		
    		currentLine=reader.readLine();
    		
    	}
    	
    	
    	
    	//Extracting column names and columns positions 
    	//while((currentLine=reader.readLine())!=null) {
    		//Remove any leading and trailing spaces
    		currentLine=currentLine.trim();
    		split.init(currentLine);
    		int columnNumber=0;
    		
    		while(split.hasMoreTokens()){
    			String _temp=split.nextToken().trim();
    			
    			if(_temp.contains("Probe Set ID")){
    				System.out.println(_temp);
    				columnNames.add(columnNumber, AnnotationFieldConstants.CLONE_ID);
    				columnNumber=columnNumber+1;
    			}else if(_temp.equalsIgnoreCase("UniGene ID")){
    				System.out.println(_temp);
    				columnNames.add(columnNumber, AnnotationConstants.UNIGENE_ID);
    				columnNumber=columnNumber+1;
    			}else if(_temp.equalsIgnoreCase("Gene Title")){
    				System.out.println(_temp);
    				columnNames.add(columnNumber, AnnotationConstants.GENE_TITLE);
    				columnNumber=columnNumber+1;
    			}else if(_temp.equalsIgnoreCase("Gene Symbol")){
    				System.out.println(_temp);
    				columnNames.add(columnNumber, AnnotationFieldConstants.GENE_SYMBOL);
    				columnNumber=columnNumber+1;
    			}else if(_temp.equalsIgnoreCase("Chromosomal Location")){
    				System.out.println(_temp);
    				columnNames.add(columnNumber, AnnotationFieldConstants.CYTOBAND);
    				columnNumber=columnNumber+1;
    			}else if(_temp.equalsIgnoreCase("Entrez Gene")){
    				System.out.println(_temp);
    				columnNames.add(columnNumber, AnnotationFieldConstants.ENTREZ_ID);
    				columnNumber=columnNumber+1;
    			}else if(_temp.equalsIgnoreCase("RefSeq Protein ID")){
    				System.out.println(_temp);
    				columnNames.add(columnNumber, AnnotationFieldConstants.PROTEIN_ACC);
    				columnNumber=columnNumber+1;
    			}else if(_temp.equalsIgnoreCase("Gene Ontology Biological Process")){
    				System.out.println(_temp);
    				if(!columnNames.contains(AnnotationFieldConstants.GO_TERMS))
    					columnNames.add(columnNumber, AnnotationFieldConstants.GO_TERMS);
    				columnNumber=columnNumber+1;
    			}
    			
    			_temp=null;
    			
    		}
    		
    //	}//Outer while loop ends
    	
    	reader.close();
    	return columnNames;
    }
    
    

    private Vector<String> parseGoTerms(String _temp, String delim) {
    	Vector<String> terms = new Vector<String>();
    	StringTokenizer tokens = new StringTokenizer(_temp, delim);
    	while(tokens.hasMoreTokens()){
    		terms.add(tokens.nextToken().trim());
    	}
    	return terms;
	}
    
    
    
    
    
    
    //This will only take the first entry, in case there are multiple entries for chromosomes.
    private void setAlignmentInfo(String temp, MevAnnotation obj){
    	/*
    	 * Template to parse
    	 */
    	//chr6:30964144-30975910 (+) // 95.63 // p21.33 /// chr6_cox_hap1:2304770-2316538 (+) // 95.56 // /// chr6_qbl_hap2:2103099-2114867 (+) // 95.45 //
    	if(!temp.trim().startsWith("chr")) { //Alignment info not available
    		obj.setProbeStrand(ChipAnnotationFieldConstants.NOT_AVAILABLE);
        	obj.setProbeChromosome(ChipAnnotationFieldConstants.NOT_AVAILABLE);
        	try {
        		obj.setProbeTxStartBP("-1");
        		obj.setProbeTxEndBP("-1");
        	} catch (Exception e) {
        		
        	}
    		return;
    	}
    	
    	int index = temp.indexOf("(");
    	String strand = temp.substring(index+1, index+2).trim();
    	//System.out.println("Strand" + strand);
    	
    	String _temp = temp.substring(0,index-1).trim();
    	int chrInd = _temp.indexOf(":");
    	String chr = _temp.substring(0, chrInd);
    	chr = chr.substring(3, chr.length());
    	//System.out.println("chr" + chr);
    	
    	int txInd = _temp.indexOf("-");
    	String txSt = _temp.substring(chrInd+1, txInd);
    	String txEnd = _temp.substring(txInd+1, _temp.length());
    	//System.out.println("txSt, txEnd " + txSt + " " + txEnd);
    	
    	obj.setProbeStrand(strand);
    	obj.setProbeChromosome(chr);
    	try {
    		obj.setProbeTxStartBP(txSt);
    		obj.setProbeTxEndBP(txEnd);
    	} catch (Exception e) {
    		System.out.println("Contains Illegal Char: " + txSt + ", " + txEnd);
    	}
    }

   
    //Function modified by Sarita
    private String[] parsemRnaIds(String _temp){
    	Vector<String> mrna = new Vector<String>();
    	StringTokenizer tokens = new StringTokenizer(_temp, "///");
    	while(tokens.hasMoreTokens()){
    		mrna.add(tokens.nextToken().trim());
    	}
    	
    	String[] strArray = new String[mrna.size()];
    	mrna.toArray(strArray);
    	return strArray;
    }

   
    
    
    
    private String[] parseProteinIds(String _temp){
    	Vector<String> prots = new Vector<String>();
    	StringTokenizer tokens = new StringTokenizer(_temp, "///");
    	while(tokens.hasMoreTokens()){
    		prots.add(tokens.nextToken().trim());
    	}
    	
    	String[] strArray = new String[prots.size()];
    	prots.toArray(strArray);
    	return strArray;
    }
   

    
    public int getCountOfLines(File f) {
    	int numLines = 0;
    	try {
	    	BufferedReader reader = new BufferedReader(new FileReader(f));
	        while (reader.readLine() != null) {
	        	numLines++;
	        }
    	} catch (IOException e){
    		
    	}
    	return numLines;
    }
    
public static void main(String[] args){
	try{
	AffymetrixAnnotationParser aap=AffymetrixAnnotationParser.createAnnotationFileParser(new File("C:/Users/sarita/Desktop/HG-U133A_2.csv"));
	Hashtable temp=new Hashtable();
	temp=aap.getAffyAnnotation();
	
	System.out.println("SIze of hashtable is:"+temp.size());	
	
	}catch(Exception e){
		e.printStackTrace();
	}
}
    
    
    
    
}
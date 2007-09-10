/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* GetUnionOfInters.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.io.FileNotFoundException;import java.util.ArrayList;
/**
 * The class <code>GetUnionOfInters</code> computes the union of 2 sets of interactions represented 
 * as <code>ArrayList</code>s of <code>SimpleGeneEdge</code> objects
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class GetUnionOfInters {

    /**
     * The <code>uniquelyMergeArrayLists</code> method returns the union of 2 sets of interactions 
     * represented as <code>ArrayList</code>s of <code>SimpleGeneEdge</code> objects
     *
     * @param al1 an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects, in this application, gene interactions
     * @param al2 an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects, in this application, gene interactions
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects, in this application, 
     * gene interactions corresponding to the union of the 2 given sets of interactions
     * @exception NullArgumentException if an error occurs if at least one of the given <code>ArrayList</code>s was null
     */
    public static ArrayList uniquelyMergeArrayLists(ArrayList al1, ArrayList al2) throws NullArgumentException {
	if(al1 == null || al2 == null){
	    throw new NullArgumentException("At least one the arguments ArrayLists was null: al1="+al1+"al2="+al2);
	}
	ArrayList toWrite = new ArrayList();
	SimpleGeneEdge sGE = null;
	for(int i = 0; i < al1.size(); i++){
	    sGE = (SimpleGeneEdge) al1.get(i);
	    if(!UsefulInteractions.containsEitherWay(toWrite, sGE)){
		toWrite.add(sGE);
		toWrite.add(new SimpleGeneEdge(sGE.getTo(), sGE.getFrom(), sGE.getWeight()));
	    }
	}
	for(int i = 0; i < al2.size(); i++){
	    sGE = (SimpleGeneEdge) al2.get(i);
	    if(!UsefulInteractions.containsEitherWay(toWrite, sGE)){
		toWrite.add(sGE);
		toWrite.add(new SimpleGeneEdge(sGE.getTo(), sGE.getFrom(), sGE.getWeight()));
	    }
	}
	if(toWrite.size() == 0){
	    return null;
	}
	return toWrite;
    }


    /**
     * The <code>test</code> method tests the <code>uniquelyMergeArrayLists</code> method
     * by taking 2 <code>ArrayList</code> representing interactions and returning their union as an <code>ArrayList</code>
     *
     * @param interFileName1 a <code>String</code> corresponding to the name of the file 
     * containing the first set of interactions in modified SIF cytoscape format: "node1 (pp) node2 = weight" 
     * where the weight is a double. Example: "node1 (pp) node2 = 1.0". This format is used for edgeAttributes files
     * in Cytoscape except the initial comment line.
     * @param interFileName2 a <code>String</code> corresponding to the name of the file
     * containing the second set of interactions in modified SIF cytoscape format: "node1 (pp) node2 = weight" 
     * where the weight is a double. Example: "node1 (pp) node2 = 1.0". 
     * This format is used for edgeAttributes files in Cytoscape except the initial comment line.
     * @param outUnionOfInterFileName a <code>String</code> corresponding to the name of the output file 
     * where the union of the 2 given sets of interactions will be written in modified SIF cytoscape format: 
     * "node1 (pp) node2 = weight" where the weight is a double. Example: "node1 (pp) node2 = 1.0". 
     * This format is used for edgeAttributes files in Cytoscape except the initial comment line.
     */
    public static void test(String interFileName1, String interFileName2, String outUnionOfInterFileName){
	try {
	    Useful.checkFile(interFileName1);
	    Useful.checkFile(interFileName2);
	    ArrayList al1 = UsefulInteractions.readInteractionsWithWeights(interFileName1);
	    ArrayList al2 = UsefulInteractions.readInteractionsWithWeights(interFileName2);
	    UsefulInteractions.writeSifFileUndirWithWeights(uniquelyMergeArrayLists(al1, al2),outUnionOfInterFileName);
	}
	catch(FileNotFoundException fnfe){
	    System.out.println(fnfe);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
    }
    
    /**
     * The <code>usage</code> method displays the usage of this class
     *
     */
    public static void usage(){
	    System.out.println("Usage: java GetUnionOfInters interWithWeights1 interWithWeights2 outUnionInterWithWeights");
	    System.exit(0);
    }
    
    public static void main(String[] argv){
	if(argv.length != 3){
	    usage();
	}
	String interFileName1 = argv[0];
	String interFileName2 = argv[1];
	String outUnionOfInterFileName = argv[2];
	test(interFileName1, interFileName2, outUnionOfInterFileName);
    }
    

}

/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: GalFileFilter.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.io.File;
import java.util.Vector;

public class GalFileFilter extends ExpressionFileFilter {
    
    public boolean accept(File f) {
	String extension = "";
	if (f.isDirectory()) return true;
	
	if (f.getName().endsWith(".gal")) return true;
	else return false;
    }
    
    public String getDescription() {
	return "Axon GAL Files (*.gal)";
    }
    
    public Vector loadExpressionFile(File file) {
	return new Vector();
    }
}
/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExpressionFileFilter.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:11 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.util.swing;

import java.io.File;
import javax.swing.filechooser.*;
import org.tigr.util.swing.*;

public class ExpressionFileFilter extends FileFilter {
    
    public boolean accept(File f) {
	if (f.isDirectory()) {
	    return true;
	}
	
	String extension = Utils.getExtension(f);
	if (extension != null) {
	    if (extension.equals(Utils.txt)) {
		return true;
	    } else {
		return false;
	    }
	}
	
	return false;
    }
    
    // The description of this filter
    public String getDescription() {
	return "Expression file (*.txt)";
    }
}

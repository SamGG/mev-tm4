/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SequenceFileFilter.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;
import org.tigr.util.swing.*;

public class SequenceFileFilter extends FileFilter {
    
    // Accept all directories and all gif, jpg, or tiff files.
    public boolean accept(File f) {
	if (f.isDirectory()) {
	    return true;
	}
	
	String extension = Utils.getExtension(f);
	if (extension != null) {
	    if (extension.equals(Utils.seq) || extension.equals(Utils.fna)) {
		return true;
	    } else {
		return false;
	    }
	}
	
	return false;
    }
    
    // The description of this filter
    public String getDescription() {
	return "Sequence files (*.seq, *.fna)";
    }
}

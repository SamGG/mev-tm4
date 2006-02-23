/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: JPGFileFilter.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.sun.media.jai.codec.ImageEncodeParam;
import com.sun.media.jai.codec.JPEGEncodeParam;

public class JPGFileFilter extends FileFilter implements ImageFileFilter {
    
    // Accept all directories and all gif, jpg, or tiff files.
    public boolean accept(File f) {
	if (f.isDirectory()) {
	    return true;
	}
	
	String extension = Utils.getExtension(f);
	if (extension != null) {
	    if (extension.equals(Utils.jpg)) {
		return true;
	    } else {
		return false;
	    }
	}
	
	return false;
    }
    
    // The description of this filter
    public String getDescription() {
	return "JPG image files (*.jpg)";
    }
    
    public ImageEncodeParam getImageEncodeParam() {
	JPEGEncodeParam param = new JPEGEncodeParam();
	param.setQuality(1.0f);
	return param;
    }
    
    public String getFileFormat() {
	return "JPEG";
    }
}

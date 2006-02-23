/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ClassificationFileFilter.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:55 $
 * $Author: caliente $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ClassificationFileFilter extends FileFilter {

    // Accept all directories and all svc files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("svc")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "Classification file (*.svc)";
    }

    /*
     * Get the extension of a file.
     */
    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}

/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IGUIFactory.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:38:25 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

/**
 * Each gui distribution must contain class, which
 * implements this interface.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface IGUIFactory {
    
    /**
     * Returns the array of analysis descriptions.
     * @see AnalysisDescription
     */
    public AnalysisDescription[] getAnalysisDescriptions();
}

/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AnalysisAction.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:40:11 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.action;

import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import org.tigr.microarray.mev.cluster.gui.AnalysisDescription;

public class AnalysisAction extends AbstractAction {
    
    private ActionManager manager;
    
    /**
     * Constructs an <code>AnalysisAction</code> from specified description.
     * @see AnalysisDescription
     */
    public AnalysisAction(ActionManager manager, AnalysisDescription desc) {
	this.manager = manager;
	putValue(Action.NAME, desc.getName());
	putValue(Action.SHORT_DESCRIPTION, desc.getTooltip());
	putValue(Action.ACTION_COMMAND_KEY, ActionManager.ANALYSIS_COMMAND);
	putValue(Action.SMALL_ICON, desc.getSmallIcon());
	putValue(ActionManager.LARGE_ICON, desc.getLargeIcon());
	putValue(ActionManager.PARAMETER, desc.getClassName());
    }
    
    /**
     * Delegates this invokation to a wrapped action manager.
     * @see ActionManager
     */
    public void actionPerformed(ActionEvent e) {
	manager.forwardAction(new ActionEvent(this, e.getID(), (String)getValue(Action.ACTION_COMMAND_KEY)));
    }
    
}

/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * TFACentroidsViewer.java
 *
 * Created on February 27, 2004, 11:56 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

/**
 *
 * @author  nbhagaba
 */
public class TFACentroidsViewer extends CentroidsViewer {
    
    private static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    private static final String SET_Y_TO_EXPERIMENT_MAX_CMD = "set-y-to-exp-max-cmd";
    private static final String SET_Y_TO_CLUSTER_MAX_CMD = "set-y-to-cluster-max-cmd";
    
    private JPopupMenu popup;
    private JMenuItem setOverallMaxMenuItem;
    private JMenuItem setClusterMaxMenuItem; 
    String[] auxTitles;
    Object[][] auxData;    
    
    /** Creates a new instance of TFACentroidsViewer */
    public TFACentroidsViewer(Experiment experiment, int[][] clusters, String[] auxTitles, Object[][] auxData) {
        super(experiment, clusters);
		initialize(auxTitles, auxData);
    }
	/**
	 * @inheritDoc
	 */
	public TFACentroidsViewer(CentroidViewer cv, String[] auxTitles, Object[][] auxData) {
		super(cv);
		initialize(auxTitles, auxData);
	}
	
	public Expression getExpression(){
		Object[] parentConstructorArgs = super.getExpression().getArguments();
		return new Expression(this, this.getClass(), "new", 
				new Object[]{parentConstructorArgs[0], parentConstructorArgs[1], parentConstructorArgs[2], 
				auxTitles, auxData});
	}
	
	private void initialize(String[] auxTitles, Object[][] auxData){
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        this.auxTitles = auxTitles;
        this.auxData = auxData;        
        getContentComponent().addMouseListener(listener);         
    }
    
    /**
     * Creates a popup menu.
     */
    private JPopupMenu createJPopupMenu(Listener listener) {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup, listener);
        return popup;
    }   
    
    /**
     * Adds the viewer specific menu items.
     */
    private void addMenuItems(JPopupMenu menu, Listener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Save all clusters", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_ALL_CLUSTERS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        setOverallMaxMenuItem = new JMenuItem("Set Y to overall max...", GUIFactory.getIcon("Y_range_expand.gif"));
        setOverallMaxMenuItem.setActionCommand(SET_Y_TO_EXPERIMENT_MAX_CMD);
        setOverallMaxMenuItem.addActionListener(listener);
        setOverallMaxMenuItem.setEnabled(false);
        menu.add(setOverallMaxMenuItem);
        
        setClusterMaxMenuItem = new JMenuItem("Set Y to cluster max...", GUIFactory.getIcon("Y_range_expand.gif"));
        setClusterMaxMenuItem.setActionCommand(SET_Y_TO_CLUSTER_MAX_CMD);
        setClusterMaxMenuItem.addActionListener(listener);
        menu.add(setClusterMaxMenuItem);
    } 
    
    /**
     * Saves all clusters.
     */
    private void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            //ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
            ExperimentUtil.saveAllGeneClustersWithAux(frame, this.getExperiment(), this.getData(), this.getClusters(), auxTitles, auxData);            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }    

    /**
     * The class to listen to mouse and action events.
     */
    private class Listener extends MouseAdapter implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
                onSaveClusters();
            } else if(command.equals(SET_Y_TO_EXPERIMENT_MAX_CMD)){
                setAllYRanges(CentroidViewer.USE_EXPERIMENT_MAX);
                setClusterMaxMenuItem.setEnabled(true);
                setOverallMaxMenuItem.setEnabled(false);
                repaint();
            } else if(command.equals(SET_Y_TO_CLUSTER_MAX_CMD)){
                setAllYRanges(CentroidViewer.USE_CLUSTER_MAX);
                setClusterMaxMenuItem.setEnabled(false);
                setOverallMaxMenuItem.setEnabled(true);
                repaint();
            }
        }
        
        private void setAllYRanges(int yRangeOption){
            int numClusters = getClusters().length;
            for(int i = 0; i < numClusters; i++){
                centroidViewer.setClusterIndex(i);
                centroidViewer.setYRangeOption(yRangeOption);
            }
        }
        
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }    
    
}

/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CASTCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:49:58 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.cast;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class CASTCentroidsViewer extends CentroidsViewer {
    
    private static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    private static final String SET_Y_TO_EXPERIMENT_MAX_CMD = "set-y-to-exp-max-cmd";
    private static final String SET_Y_TO_CLUSTER_MAX_CMD = "set-y-to-cluster-max-cmd";
    
    private JPopupMenu popup;
    private JMenuItem setOverallMaxMenuItem;
    private JMenuItem setClusterMaxMenuItem;
    
    /**
     * Constructs a <code>CASTCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public CASTCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
    }
	public CASTCentroidsViewer(CentroidViewer cv) {
		super(cv);
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
            ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void setAllYRanges(int yRangeOption){
        int numClusters = getClusters().length;
        for(int i = 0; i < numClusters; i++){
            centroidViewer.setClusterIndex(i);
            centroidViewer.setYRangeOption(yRangeOption);
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

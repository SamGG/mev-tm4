/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOMExperimentViewer.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import java.awt.Frame;
import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.awt.image.BufferedImage;

import javax.swing.JMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JColorChooser;

import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;

import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

public class SOMExperimentViewer implements IViewer {    
    
    private JPopupMenu popup;
    private ExperimentViewer expViewer;
    private SOMExperimentHeader header;
    
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    /**
     * Constructs a <code>SOMExperimentViewer</code> with specified
     * experiment, clusters and codes.
     */
    public SOMExperimentViewer(Experiment experiment, int[][] clusters, FloatMatrix codes) {
	Listener listener = new Listener();
	this.popup = createJPopupMenu(listener);
	
	this.expViewer = new ExperimentViewer(experiment, clusters);
	this.expViewer.getContentComponent().addMouseListener(listener);
	this.header = new SOMExperimentHeader(expViewer.getHeaderComponent(), codes, clusters);
	//this.header.setColorImages(expViewer.getPosColorImage(), expViewer.getNegColorImage());
	this.header.setColorImages(expViewer.getNegColorImage(), expViewer.getPosColorImage());
	this.header.setMissingColor(expViewer.getMissingColor());
	this.header.addMouseListener(listener);
    }
    
    /**
     * Returns the header component.
     */
    public JComponent getHeaderComponent() {
	return header;
    }
    
    /**
     * Returns the wrapped experiment viewer.
     */
    public JComponent getContentComponent() {
	return expViewer.getContentComponent();
    }
    
    public BufferedImage getImage() {
	return expViewer.getImage();
    }
    
    /**
     * Updates header and contents attributes when the viewer is selected.
     */
    public void onSelected(IFramework framework) {
	expViewer.onSelected(framework);
	header.setCurrentCluster(((Integer)framework.getUserObject()).intValue());
	IDisplayMenu menu = framework.getDisplayMenu();
        header.setColorImages(menu.getPositiveGradientImage(), menu.getNegativeGradientImage());
	header.setValues(Math.abs(menu.getMaxRatioScale()), -Math.abs(menu.getMinRatioScale()));
	header.setAntiAliasing(menu.isAntiAliasing());
	header.setDrawBorders(menu.isDrawingBorder());
	header.updateSize(menu.getElementSize());
    }
    
    /**
     * Updates experiment data.
     */
    public void onDataChanged(IData data) {
	expViewer.onDataChanged(data);
    }
    
    /**
     * Updates header and contents attributes when the display menu is changed.
     */
    public void onMenuChanged(IDisplayMenu menu) {
	expViewer.onMenuChanged(menu);
        header.setColorImages(menu.getPositiveGradientImage(), menu.getNegativeGradientImage());
	header.setValues(Math.abs(menu.getMaxRatioScale()), -Math.abs(menu.getMinRatioScale()));
	header.setAntiAliasing(menu.isAntiAliasing());
	header.setDrawBorders(menu.isDrawingBorder());
	header.updateSize(menu.getElementSize());
    }
    
    public void onDeselected() {}
    public void onClosed() {}
    
    /**
     * Creates a popup menu.
     */
    private JPopupMenu createJPopupMenu(Listener listener) {
	JPopupMenu popup = new JPopupMenu();
	addMenuItems(popup, listener);
	return popup;
    }
    
    /**
     * Adds viewer specific menu items.
     */
    protected void addMenuItems(JPopupMenu menu, ActionListener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Store cluster", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(STORE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Launch new session", GUIFactory.getIcon("analysis16.gif"));
        menuItem.setActionCommand(LAUNCH_NEW_SESSION_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Delete public cluster", GUIFactory.getIcon("delete16.gif"));
        menuItem.setActionCommand(SET_DEF_COLOR_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Save cluster...", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save all clusters...", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_ALL_CLUSTERS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }
    
    /**
     * Saves clusters.
     */
    private void onSaveClusters() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	try {
	    expViewer.saveClusters(frame);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}
    }
    
    /**
     * Save the viewer cluster.
     */
    private void onSaveCluster() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	try {
	    expViewer.saveCluster(frame);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}
    }
    
    /**
     * Sets a public color.
     */
    private void onSetColor() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	Color newColor = JColorChooser.showDialog(frame, "Choose color", CentroidViewer.DEF_CLUSTER_COLOR);
	if (newColor != null) {
	    expViewer.setClusterColor(newColor);
	}
    }
    
    /**
     * Removes a public color.
     */
    private void onSetDefaultColor() {
	expViewer.setClusterColor(null);
    }
    
    /**
     * The class to listen to mouse and action events.
     */
    private class Listener extends MouseAdapter implements ActionListener {
	
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command.equals(SAVE_CLUSTER_CMD)) {
		onSaveCluster();
	    } else if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
		onSaveClusters();
	    } else if (command.equals(SET_DEF_COLOR_CMD)) {
		onSetDefaultColor();
	    }  else if (command.equals(STORE_CLUSTER_CMD)) {
		expViewer.storeCluster();
	    } else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
                expViewer.launchNewSession();
            }
	}
	
	public void mouseReleased(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	public void mousePressed(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	private void maybeShowPopup(MouseEvent e) {
	    
	    if (!e.isPopupTrigger() || expViewer.getCluster() == null || expViewer.getCluster().length == 0) {
		return;
	    }
	    popup.show(e.getComponent(), e.getX(), e.getY());
	}
    }
}

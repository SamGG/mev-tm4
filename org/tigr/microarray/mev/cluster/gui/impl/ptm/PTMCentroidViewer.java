/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PTMCentroidViewer.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-02-05 22:10:55 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JColorChooser;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;


public class PTMCentroidViewer extends CentroidViewer {
       
    private JPopupMenu popup;
    private Vector templateVector;
    private String[] auxTitles;
    private Object[][] auxData;
    
    /**
     * Construct a <code>PTMCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public PTMCentroidViewer(Experiment experiment, int[][] clusters, Vector templateVector, String[] auxTitles, Object[][] auxData) {
	super(experiment, clusters);
	Listener listener = new Listener();
	this.popup = createJPopupMenu(listener);
	this.templateVector = templateVector;
        this.auxTitles = auxTitles;
        this.auxData = auxData;
	getContentComponent().addMouseListener(listener);
    }
    
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        oos.writeObject(this.templateVector);
        oos.writeObject(this.auxData);
        oos.writeObject(this.auxTitles);
    }    
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {        
        this.templateVector = (Vector)ois.readObject();
        this.auxData = (Object [][])ois.readObject();
        this.auxTitles = (String [])ois.readObject();
            
        Listener listener = new Listener();
	this.popup = createJPopupMenu(listener);
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
     * Saves all clusters.
     */
    private void onSaveClusters() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	try {
	    ExperimentUtil.saveAllGeneClustersWithAux(frame, getExperiment(), getData(), getClusters(), auxTitles, auxData);
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
	    ExperimentUtil.saveGeneClusterWithAux(frame, getExperiment(), getData(), getCluster(), auxTitles, auxData);
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
	Color newColor = JColorChooser.showDialog(frame, "Choose color", DEF_CLUSTER_COLOR);
	if (newColor != null) {
	    setClusterColor(newColor);
	}
    }
    
    /**
     * Removes a public color.
     */
    private void onSetDefaultColor() {
	setClusterColor(null);
    }
    
    /**
     * Paints chart into specified graphics.
     */
    public void paint(Graphics g) {
	FontMetrics metrics = g.getFontMetrics();
	Rectangle rect = new Rectangle(40, 20, getWidth()-80, getHeight() - 40 - getNamesWidth(metrics));
	paint((Graphics2D)g, rect, true);
    }
    
    /**
     * Paints chart into specified graphics and with specified bounds.
     */
    public void paint(Graphics2D g, Rectangle rect, boolean drawMarks) {
	super.subPaint(g, rect, drawMarks);
	
	if (isAntiAliasing) {
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	
	final int left = rect.x;
	final int top = rect.y;
	final int width  = rect.width;
	final int height = rect.height;
	if (width < 5 || height < 5) {
	    return;
	}
	
	final int zeroValue = top + (int)Math.round(height/2f);
	final int numberOfSamples  = experiment.getNumberOfSamples();
	
	//System.out.println("maxYValue = " + maxYValue);
	
        if(yRangeOption == CentroidViewer.USE_EXPERIMENT_MAX)
            maxYValue = maxExperimentValue;
        else if(this.yRangeOption == CentroidViewer.USE_CLUSTER_MAX)
            maxYValue = maxClusterValue;
        
	if (maxYValue == 0) {
	    maxYValue = 1;
	}
	
	final float factor = height/(2f*maxYValue);
	
	//System.out.println("factor = " + factor);
	final float stepX  = width/(float)(numberOfSamples-1);
	final int   stepsY = (int)maxYValue+1;
	
	if (this.drawVariances /*&& clusters[clusterIndex].length > 0*/) {
	    // draw variances
	    g.setColor(bColor);
	    for (int i=0; i<numberOfSamples; i++) {
		//System.out.println("(this.means[this.clusterIndex][" + i + "] = " + this.means[this.clusterIndex][i]);
		//System.out.println("this.variances[this.clusterIndex][" + i + "] = " + this.variances[this.clusterIndex][i]);
		
		if(Float.isNaN(this.means[this.clusterIndex][i]) || Float.isNaN(this.variances[this.clusterIndex][i]) || (this.variances[this.clusterIndex][i] < 0.0f)) {
		    continue;
		}
		
		g.drawLine(left+(int)Math.round(i*stepX)  , zeroValue - (int)Math.round((this.means[this.clusterIndex][i]-this.variances[this.clusterIndex][i])*factor),
		left+(int)Math.round(i*stepX)  , zeroValue - (int)Math.round((this.means[this.clusterIndex][i]+this.variances[this.clusterIndex][i])*factor));
		g.drawLine(left+(int)Math.round(i*stepX)-3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]-this.variances[this.clusterIndex][i])*factor),
		left+(int)Math.round(i*stepX)+3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]-this.variances[this.clusterIndex][i])*factor));
		g.drawLine(left+(int)Math.round(i*stepX)-3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]+this.variances[this.clusterIndex][i])*factor),
		left+(int)Math.round(i*stepX)+3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]+this.variances[this.clusterIndex][i])*factor));
	    }
	}
	
	//System.out.println("PTMCentroidViewer: After if(drawVariances)");
	
	if (this.drawValues /*&& clusters[clusterIndex].length > 0*/) {
	    // draw values
	    float fValue, sValue;
	    Color color;
	    for (int sample=0; sample<numberOfSamples-1; sample++) {
		for (int probe=0; probe<getCluster().length; probe++) {
		    fValue = this.experiment.get(getProbe(probe), sample);
		    sValue = this.experiment.get(getProbe(probe), sample+1);
		    if (Float.isNaN(fValue) || Float.isNaN(sValue)) {
			continue;
		    }
		    color = this.data.getProbeColor(this.experiment.getGeneIndexMappedToData(getProbe(probe)));
		    color = color == null ? DEF_CLUSTER_COLOR : color;
		    g.setColor(color);
		    g.drawLine(left+(int)Math.round(sample*stepX)    , zeroValue - (int)Math.round(fValue*factor),
		    left+(int)Math.round((sample+1)*stepX), zeroValue - (int)Math.round(sValue*factor));
		}
	    }
	}
	
	//System.out.println("PTMCentroidViewer: after if(drawValues)");
	
	if (this.drawCodes && this.codes != null && clusters[clusterIndex].length > 0) {
	    g.setColor(Color.gray);
	    for (int i=0; i<numberOfSamples-1; i++) {
		g.drawLine(left+(int)Math.round(i*stepX), zeroValue-(int)Math.round(this.codes[this.clusterIndex][i]*factor), left+(int)Math.round((i+1)*stepX), zeroValue-(int)Math.round(this.codes[this.clusterIndex][i+1]*factor));
	    }
	}
	
	//System.out.println("PTMCentroidViewer: after if(drawCodes)");
	
	// draw zero line
	g.setColor(Color.black);
	g.drawLine(left, zeroValue, left+width, zeroValue);
	
	//System.out.println("PTMCentroidViewer: after draw zero line");
	
	// draw magenta line
	if (getCluster() != null && getCluster().length > 0 /*&& clusters[clusterIndex].length > 0*/) {
	    g.setColor(Color.magenta);
	    for (int i=0; i<numberOfSamples-1; i++) {
		
		if(Float.isNaN(this.means[this.clusterIndex][i]) || Float.isNaN(this.means[this.clusterIndex][i+1])) {
		    continue;
		}
		
		g.drawLine(left+(int)Math.round(i*stepX), zeroValue-(int)Math.round(this.means[this.clusterIndex][i]*factor), left+(int)Math.round((i+1)*stepX), zeroValue-(int)Math.round(this.means[this.clusterIndex][i+1]*factor));
	    }
	}
	
	//System.out.println("PTMCentroidViewer: after draw magenta line");
	//draw template
	float[] templateArray = new float[templateVector.size()];
	//System.out.println("templateVector.size()" + templateVector.size());
	
	for (int i = 0; i < templateArray.length; i++) {
	    
	    templateArray[i] = ((Float)(templateVector.get(i))).floatValue();
	    //System.out.println("templateArray[" + i + "] = " + templateArray[i]);
	}
	
	for (int i = 0; i < templateArray.length; i++) {
	    templateArray[i] = templateArray[i] - 0.5f;
	}
	
	//System.out.println("before drawing template, after creating templateArray");
	
	for (int i = 0; i < numberOfSamples - 1; i++) {
	    g.setColor(Color.red);
	    //System.out.println("drawing template point " + i);
	    //System.out.println("templateArray[" + i + "] = " + templateArray[i]);
	    //System.out.println("templateArray[" + (i + 1) + "] = " + templateArray[i +1]);
	    if (!Float.isNaN(templateArray[i])) {
		g.fillOval(left+(int)Math.round(i*stepX) - 2, zeroValue-(int)Math.round(templateArray[i]*factor) - 2, 5, 5);
	    }
	    if (!Float.isNaN(templateArray[i+1])) {
		g.fillOval(left+(int)Math.round((i+1)*stepX) - 2, zeroValue-(int)Math.round(templateArray[i+1]*factor) - 2, 5, 5);
	    }
	    if (Float.isNaN(templateArray[i]) || Float.isNaN(templateArray[i+1])) {
		continue;
	    }
	    g.setColor(Color.blue);
	    g.drawLine(left+(int)Math.round(i*stepX), zeroValue-(int)Math.round(templateArray[i]*factor), left+(int)Math.round((i+1)*stepX), zeroValue-(int)Math.round(templateArray[i+1]*factor));
	}
	
	//System.out.println("PTMCentroidViewer: after draw template");
	
	// draw rectangle
	g.setColor(Color.black);
	g.drawRect(left, top, width, height);
	
	//System.out.println("PTMCentroidViewer: after draw rectangle");
	
	// draw X items
	for (int i=1; i<numberOfSamples-1; i++) {
	    g.drawLine(left+(int)Math.round(i*stepX), top+height-5, left+(int)Math.round(i*stepX), top+height);
	}
	
	//System.out.println("PTMCentroidViewer: after draw X items");
	
	//draw Y items
	for (int i=1; i<stepsY; i++) {
	    g.drawLine(left, zeroValue-(int)Math.round(i*factor), left+5, zeroValue-(int)Math.round(i*factor));
	    g.drawLine(left, zeroValue+(int)Math.round(i*factor), left+5, zeroValue+(int)Math.round(i*factor));
	}
	
	//System.out.println("PTMCentroidViewer: after draw Y Items");
	if(this.showRefLine && this.drawReferenceBlock){          
           java.awt.Composite initComposite = g.getComposite();
           g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.3f));
           g.setColor(Color.yellow);
           g.fillRect(xref-3, 20, 7, height); 
           g.setComposite(initComposite);
           g.setColor(Color.blue);
           g.drawLine(xref, 20, xref, height+20);
           framework.setStatusText("Experiment = "+data.getSampleName(experiment.getSampleIndex(currExpRefLine))+",   mean = "+ this.means[this.clusterIndex][currExpRefLine]+",   sd = "+ this.variances[this.clusterIndex][currExpRefLine]);
        }
        
	// draw genes info
	g.setColor(bColor);
	if (drawMarks) {
	    FontMetrics metrics = g.getFontMetrics();
	    String str;
	    int strWidth;
	    //draw Y digits
	    for (int i=1; i<stepsY; i++) {
		str = String.valueOf(i);
		strWidth = metrics.stringWidth(str);
		g.drawString(str, left-10-strWidth, zeroValue+5-(int)Math.round(i*factor));
		str = String.valueOf(-i);
		strWidth = metrics.stringWidth(str);
		g.drawString(str, left-10-strWidth, zeroValue+5+(int)Math.round(i*factor));
	    }
	    // draw X samples names
	    g.rotate(-Math.PI/2.0);
	    final int max_name_width = getNamesWidth(metrics);
	    for (int i=0; i<numberOfSamples; i++) {
		g.drawString(data.getSampleName(experiment.getSampleIndex(i)), -height-top-10-max_name_width, left+(int)Math.round(i*stepX)+3);
	    }
	    g.rotate(Math.PI/2.0);
	}
	
	//System.out.println("PTMCentroidViewer: after if(drawMarks)");
	
	if (getCluster() != null && getCluster().length > 0 && this.drawVariances /*&& clusters[clusterIndex].length > 0*/) {
	    // draw points
	    g.setColor(bColor);
	    for (int i=0; i<numberOfSamples; i++) {
		
		if (Float.isNaN(this.means[this.clusterIndex][i])) {
		    continue;
		}
		
		g.fillOval(left+(int)Math.round(i*stepX)-3, zeroValue-(int)Math.round(this.means[this.clusterIndex][i]*factor)-3, 6, 6);
	    }
	}
	
	//System.out.println("PTMCentroidViewer: after draw points");
	
	g.setColor(bColor);
	if (getCluster() == null || getCluster().length == 0) {
	    g.drawString("No Genes", left+10, top+20);
	} else {
	    g.drawString(getCluster().length+" Genes", left+10, top+20);
	}
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
	    } else if(command.equals(SET_Y_TO_EXPERIMENT_MAX_CMD)){
                yRangeOption = CentroidViewer.USE_EXPERIMENT_MAX;
                setClusterMaxMenuItem.setEnabled(true);
                setOverallMaxMenuItem.setEnabled(false);
                repaint();
            } else if(command.equals(SET_Y_TO_CLUSTER_MAX_CMD)){
                yRangeOption = CentroidViewer.USE_CLUSTER_MAX;
                setClusterMaxMenuItem.setEnabled(false);
                setOverallMaxMenuItem.setEnabled(true);
                repaint();
            } else if (command.equals(STORE_CLUSTER_CMD)) {
		storeCluster();
	    } else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
                launchNewSession();
            } else if(command.equals(TOGGLE_REF_LINE_CMD)){
                showRefLine = !showRefLine;
                repaint();
            }  
	}
	
	public void mouseReleased(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	private void maybeShowPopup(MouseEvent e) {
	    if (!e.isPopupTrigger() || getCluster() == null || getCluster().length == 0) {
		return;
	    }
	    popup.show(e.getComponent(), e.getX(), e.getY());
	}
    }
    
}

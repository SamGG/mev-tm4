/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: FOMGraph.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-03 16:07:39 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.fom;

import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;

public class FOMGraph extends JPanel implements java.io.Serializable {
    
    private float[] values;
    private String[] xItems;
    private String[] yItems;
    private String title, xLabel, yLabel;
    private Insets insets;
    private boolean isAntiAliasing = false;
    
    private int maxXItem, maxYItem;
    private float maxYValue = 1f;
    private Color pointColor = Color.red;
    private Color valuesLineColor = Color.blue;
    private Color mouseLineColor = Color.magenta;
    private Color gridLineColor = Color.yellow;
    private Color axisLineColor = Color.black;
    private int pointSize = 5;
    
    private MouseHandler mouseHandler;
    
    public FOMGraph(float[] values, String title, String xLabel, String yLabel) {
	if (values == null) {
	    throw new IllegalArgumentException("values == null");
	}
	setBackground(Color.white);
	this.insets = new Insets(60, 60, 60, 60);
	this.values = values;
	this.title = title;
	this.xLabel = xLabel;
	this.yLabel = yLabel;
	this.mouseHandler = new MouseHandler();
	addMouseMotionListener(mouseHandler);
    }
    
    public void setItems(String[] xItems, String[] yItems) {
	this.xItems = xItems;
	this.yItems = yItems;
	this.maxXItem = getMaxWidth(xItems);
	this.maxYItem = getMaxWidth(yItems);
    }
    
    public void setMaxYValue(float value) {
	this.maxYValue = value;
    }
    
    public BufferedImage getImage() {
        BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(),getHeight());               
	Graphics2D g = image.createGraphics();
	g.setColor(Color.white);
	g.fillRect(0, 0, image.getWidth(), image.getHeight());
	paint(g);
	return image;
    }
    
    /**
     * Sets the anti-aliasing attribute.
     */
    public void setAntiAliasing(boolean value) {
	this.isAntiAliasing = value;
    }
    
    public void paint(Graphics g1D) {
	super.paint(g1D);
	Graphics2D g = (Graphics2D)g1D;
	FontMetrics metrics = g.getFontMetrics();
	int descent = metrics.getDescent();
	if (this.isAntiAliasing) {
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	
	Dimension size = getSize();
	final int left = this.insets.left+this.maxYItem;
	final int top = this.insets.top;
	final int width = size.width - left - this.insets.right;
	final int height = size.height - this.insets.bottom - top - this.maxXItem;
	final float xScale = (float)width/(float)(this.values.length-1);
	final float yScale = (float)height/this.maxYValue;
	
	int strWidth;
	// draw title
	strWidth = metrics.stringWidth(this.title);
	g.drawString(this.title, left+(width-strWidth)/2, (this.insets.top+descent)/2);
	// draw xLabel
	strWidth = metrics.stringWidth(this.xLabel);
	g.drawString(this.xLabel, left+(width-strWidth)/2, this.insets.top+height+this.maxXItem+(this.insets.top+descent)/2);
	// draw yLabel
	strWidth = metrics.stringWidth(this.yLabel);
	g.rotate(-Math.PI/2.0);
	g.drawString(this.yLabel, -(this.insets.top+(height+strWidth)/2), (this.insets.left+descent)/2);
	g.rotate(Math.PI/2.0);
	// draw grid
	g.setColor(this.gridLineColor);
	if (this.yItems != null) {
	    final float yItemsStep = height/(float)(this.yItems.length-1);
	    for (int i=0; i<this.yItems.length; i++) {
		if (this.yItems[this.yItems.length-i-1] != null) {
		    g.drawLine(left, top+(int)Math.round(i*yItemsStep), left+width, top+(int)Math.round(i*yItemsStep));
		}
	    }
	}
	if (this.xItems != null) {
	    final float xItemsStep = width/(float)(this.xItems.length-1);
	    for (int i=0; i<this.xItems.length; i++) {
		if (this.xItems[i] != null) {
		    g.drawLine(left+(int)Math.round(i*xItemsStep), top, left+(int)Math.round(i*xItemsStep), top+height);
		}
	    }
	}
	// draw vertical line
	g.setColor(this.axisLineColor);
	g.drawLine(left, top, left, top+height);
	// draw y items
	if (this.yItems != null) {
	    final float yItemsStep = height/(float)(this.yItems.length-1);
	    for (int i=0; i<this.yItems.length; i++) {
		g.drawLine(left-5, top+(int)Math.round(i*yItemsStep), left+5, top+(int)Math.round(i*yItemsStep));
		if (this.yItems[this.yItems.length-i-1] != null) {
		    g.drawString(this.yItems[this.yItems.length-i-1], left-7-metrics.stringWidth(this.yItems[this.yItems.length-i-1]), top+(int)Math.round(i*yItemsStep)+descent);
		}
	    }
	}
	// draw x items
	if (this.xItems != null) {
	    final float xItemsStep = width/(float)(this.xItems.length-1);
	    for (int i=0; i<this.xItems.length; i++) {
		g.drawLine(left+(int)Math.round(i*xItemsStep), top+height-5, left+(int)Math.round(i*xItemsStep), top+height+5);
	    }
	    g.rotate(-Math.PI/2.0);
	    final int bottom = top+height+7;
	    for (int i=0; i<this.xItems.length; i++) {
		if (this.xItems[i] != null) {
		    g.drawString(this.xItems[i], -(bottom+metrics.stringWidth(this.xItems[i])), left+(int)Math.round(i*xItemsStep)+descent);
		}
	    }
	    g.rotate(Math.PI/2.0);
	}
	// draw horizontal line
	g.drawLine(left, top+height, left+width, top+height);
	
	// draw value lines
	g.setColor(this.valuesLineColor);
	int x1, y1, x2, y2;
	for (int i=0; i<this.values.length-1; i++) {
	    x1 = left+Math.round(i*xScale);
	    y1 = top+height-Math.round(this.values[i]*yScale);
	    x2 = left+Math.round((i+1)*xScale);
	    y2 = top+height-Math.round(this.values[i+1]*yScale);
	    g.drawLine(x1, y1, x2, y2);
	}
	// draw value points
	g.setColor(this.pointColor);
	for (int i=0; i<this.values.length; i++) {
	    g.fillOval(left+Math.round(i*xScale)-this.pointSize/2, top+height-Math.round(this.values[i]*yScale)-this.pointSize/2, this.pointSize, this.pointSize);
	}
	this.mouseHandler.validate();
    }
    
    private int getMaxWidth(String[] items) {
	if (items == null) {
	    return 0;
	}
	FontMetrics metrics = getFontMetrics(getFont());
	int width = 0;
	for (int i=0; i<items.length; i++) {
	    if (items[i] != null) {
		width = Math.max(width, metrics.stringWidth(items[i]));
	    }
	}
	return width;
    }
    
    private void drawMouseCross(int x, int y) {
	Graphics2D g = (Graphics2D)getGraphics();
	g.setColor(this.mouseLineColor);
	g.setXORMode(getBackground());
	Dimension size = getSize();
	final int left = insets.left + this.maxYItem;
	final int right = size.width - this.insets.right;
	final int top = insets.top;
	final int bottom = size.height - this.insets.bottom - this.maxXItem;
	g.drawLine(x, top, x, bottom);
	g.drawLine(left, y, right, y);
	g.setPaintMode();
	g.dispose();
    }
    
    private class MouseHandler extends MouseMotionAdapter implements java.io.Serializable {
	
	private Point prevCoords = new Point(-1, -1);
	
	public void validate() {
	    if (isCoordsValid(prevCoords.x, prevCoords.y)) {
		drawMouseCross(prevCoords.x, prevCoords.y);
	    }
	    prevCoords.setLocation(-1, -1);
	}
	
	public void mouseMoved(MouseEvent e) {
	    int x = e.getX();
	    int y = e.getY();
	    if (isCoordsValid(prevCoords.x, prevCoords.y)) {
		drawMouseCross(prevCoords.x, prevCoords.y);
	    }
	    if (isCoordsValid(x, y)) {
		drawMouseCross(x, y);
	    }
	    prevCoords.setLocation(x, y);
	}
	
	private boolean isCoordsValid(int x, int y) {
	    Dimension size = getSize();
	    final int left = insets.left+maxYItem;
	    final int top = insets.top;
	    final int right = size.width - insets.right;
	    final int bottom = size.height - insets.bottom - maxXItem;
	    return(left < x && x < right) && (top < y && y < bottom);
	}
    }
    
}

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PTMInfoViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:51:08 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class PTMInfoViewer extends ViewerAdapter implements java.io.Serializable {
    
    private JComponent header;
    private JTextArea  content;
    private boolean clusterGenes = true;
    
    /**
     * Constructs a <code>PTMInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public PTMInfoViewer(int[][] clusters, int genes) {
        header  = createHeader();
        content = createContent(clusters, genes);
        setMaxWidth(content, header);
    }
    
    /**
     * Constructs a <code>KMCInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public PTMInfoViewer(int[][] clusters, int genes, boolean clusterGenes) {
        header  = createHeader();
        this.clusterGenes = clusterGenes;
        content = createContent(clusters, genes);
        setMaxWidth(content, header);
    }
    public PTMInfoViewer(JTextArea content, JComponent header){
    	this.header = header;
    	this.content = content;
        setMaxWidth(content, header);
    }
    
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
        return content;
    }
    
    /**
     * Returns the viewer header.
     */
    public JComponent getHeaderComponent() {
        return header;
    }
    
    /**
     * Creates the viewer header.
     */
    private JComponent createHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Cluster Information</b></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextArea createContent(int[][] clusters, int genes) {
        JTextArea area = new JTextArea(clusters.length*3, 20);
        area.setMargin(new Insets(0, 10, 0, 0));
        area.setEditable(false);
        StringBuffer sb = new StringBuffer(clusters.length*3*10);
        if(clusterGenes){
            for (int counter = 0; counter < clusters.length; counter++) {
                if (counter == 0) {
                    sb.append("Matched genes ");
                    sb.append("\t\t");
                    sb.append("# of Matched Genes: "+clusters[counter].length);
                    sb.append("\n\t\t");
                    sb.append("% of Genes that Match Template: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                }
                else if (counter == 1) {
                    sb.append("Unmatched genes ");
                    sb.append("\t");
                    sb.append("# of Unmatched Genes: "+clusters[counter].length);
                    sb.append("\n\t\t");
                    sb.append("% of Genes that do not Match Template: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                }
            }
        }
        else{
            for (int counter = 0; counter < clusters.length; counter++) {
                if (counter == 0) {
                    sb.append("Matched experiments ");
                    sb.append("\t");
                    sb.append("# of Matched Experiments: "+clusters[counter].length);
                    sb.append("\n\t\t");
                    sb.append("% of Experiments that Match Template: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                }
                else if (counter == 1) {
                    sb.append("Unmatched experiments ");
                    sb.append("\t");
                    sb.append("# of Unmatched Experiments: "+clusters[counter].length);
                    sb.append("\n\t\t");
                    sb.append("% of Experiments that do not Match Template: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                }
            }
            
        }
        area.setText(sb.toString());
        area.setCaretPosition(0);
        return area;
    }
    
    /**
     * Synchronize content and header sizes.
     */
    private void setMaxWidth(JComponent content, JComponent header) {
        int c_width = content.getPreferredSize().width;
        int h_width = header.getPreferredSize().width;
        if (c_width > h_width) {
            header.setPreferredSize(new Dimension(c_width, header.getPreferredSize().height));
        } else {
            content.setPreferredSize(new Dimension(h_width, content.getPreferredSize().height));
        }
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
}

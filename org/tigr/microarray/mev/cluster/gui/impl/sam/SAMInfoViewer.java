/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SAMInfoViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-08 16:59:46 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class SAMInfoViewer extends ViewerAdapter {
    
    private JComponent header;
    private JTextArea  content;
    private int studyDesign;
    
    /**
     * Constructs a <code>KMCInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public SAMInfoViewer(int[][] clusters, int genes, int studyDesign) {
        this.studyDesign = studyDesign;
	header  = createHeader();
	content = createContent(clusters, genes);
       
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
	area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));  
	StringBuffer sb = new StringBuffer(clusters.length*3*10);
        //System.out.println("SAMIInfoViewer.createContent(): studyDesign = " + studyDesign);
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
            for (int counter = 0; counter < clusters.length; counter++) {
                if (counter == 0) {
                    sb.append("Positive Significant Genes ");
                    sb.append("\t");
                    sb.append("# of Positive Significant Genes: "+clusters[counter].length);
                    sb.append("\n\t\t");
                    sb.append("% of Positive Significant Genes: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                } else if (counter == 1) {
                    sb.append("Negative Significant Genes ");
                    sb.append("\t");
                    sb.append("# of Negative Significant Genes: "+clusters[counter].length);
                    sb.append("\n\t\t");
                    sb.append("% of Negative Significant Genes: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                } else if (counter == 2) {
                    sb.append("All Significant Genes ");
                    sb.append("\t");
                    sb.append("Total # of Significant Genes: "+clusters[counter].length);
                    sb.append("\n\t\t");
                    sb.append("% of Genes that are Significant: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                } else if (counter == 3) {
                    sb.append("Non-Significant Genes ");
                    sb.append("\t");
                    sb.append("Total # of Non-Significant Genes: "+clusters[counter].length);
                    sb.append("\n\t\t");
                    sb.append("% of Genes that are Not Significant: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                }
            }
        } else {
            for (int counter = 0; counter < clusters.length; counter++) {
                if (counter == 0) {
                    sb.append("Significant Genes ");
                    sb.append("\t");
                    sb.append("# of Significant Genes: "+clusters[counter].length);
                    sb.append("\n\t\t");
                    sb.append("% of Significant Genes: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                } else if (counter == 1) {
                    sb.append("Non-significant Genes ");
                    sb.append("\t");
                    sb.append("# of Non-significant Genes: "+clusters[counter].length);
                    sb.append("\n\t\t");
                    sb.append("% of Non-significant Genes: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
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
}

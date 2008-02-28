/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: InfoDisplay.java,v $
 * $Revision: 1.14 $
 * $Date: 2007-12-20 22:29:22 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.tigr.graph.GC;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.annotation.AnnoAttributeObj;
import org.tigr.microarray.mev.annotation.IAnnotation;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.util.Xcon;
import org.tigr.util.awt.ActionInfoDialog;
import org.tigr.util.awt.GBA;

public class InfoDisplay extends ActionInfoDialog  {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int feature;
    private int probe;
    private MultipleArrayData data;
    private ISlideData slideData;
    private ISlideDataElement element;
    private int dataType;
    
    private int LINEAR = 0; // to return just ratio
    private int LOG = 1;   //for log2(ratio)
    
    // from single array wiewer
    public InfoDisplay(JFrame parent, ISlideData slideData, ISlideDataElement element, int probe) {
        super(parent, false);
        this.probe = probe;
        dataType = slideData.getDataType();
        init(slideData, element);
    }
    
    // from multiple array wiewer
    public InfoDisplay(JFrame parent, MultipleArrayData data, int feature, int probe) {
        super(parent, false);
        this.data = data;
        this.feature = feature;
        this.probe = probe;
        this.dataType = data.getDataType();
        init(data.getFeature(feature), data.getSlideDataElement(feature, probe));
    }
    
    private void init(ISlideData slideData, ISlideDataElement element) {
        this.slideData = slideData;
        this.element = element;
        
        Font infoDisplayFont = new Font("Arial", Font.PLAIN, 10); //new Font("monospaced", Font.PLAIN, 10);
        JLabel spotImage = new JLabel(new ImageIcon(org.tigr.microarray.mev.InfoDisplay.class.getResource("/org/tigr/images/spot.gif")));
        
        EventListener listener = new EventListener();
        
        JEditorPane infoDisplayTextPane = new JEditorPane();
        infoDisplayTextPane.setContentType("text/html");
        
        
        
        infoDisplayTextPane.setFont(infoDisplayFont);
        infoDisplayTextPane.setBackground(new Color(Integer.parseInt("FFFFCC",16)));
        infoDisplayTextPane.setEditable(false);
        infoDisplayTextPane.setMargin(new Insets(10,15,10,10));
        infoDisplayTextPane.setText(createMessage(element));
        infoDisplayTextPane.setCaretPosition(0);
        
        
        
        
        JButton viewGeneGraphButton = new JButton("Gene Graph");
        viewGeneGraphButton.setActionCommand("view-gene-graph");
        viewGeneGraphButton.addActionListener(listener);
        viewGeneGraphButton.setEnabled(false);
        if (data != null)
            viewGeneGraphButton.setEnabled(true);
        
        JButton viewExperimentButton = new JButton("Sample Detail");
        //viewExperimentButton.setEnabled(false);
        //viewExperimentButton.setToolTipText("Temporarily Disabled -- visit www.tigr.org/software/TM4 for update.");
        viewExperimentButton.setActionCommand("view-experiment");
        viewExperimentButton.addActionListener(listener);
        /**
         * Raktim, Temporary Fix for CGH Data
         * All graph views from this option deals with log10 or log transformed data.
         * Need to figure out how to handle the scenario with CGH data
         */
        if(data.isCGHData()) viewExperimentButton.setEnabled(false);
        
        JButton setColorButton = new JButton("Set Gene Color");
        setColorButton.setActionCommand("set-color");
        setColorButton.addActionListener(listener);
        setColorButton.setEnabled(false);
        
        JButton closeButton = new JButton("Close Spot Information");
        closeButton.setActionCommand("close");
        closeButton.addActionListener(listener);
        
        contentPane.setLayout(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(infoDisplayTextPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.white);
        GBA gba = new GBA();
        gba.add(contentPane, scrollPane, 0, 0, 3, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(contentPane, viewGeneGraphButton, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.W, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(contentPane, viewExperimentButton, 1, 2, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(contentPane, setColorButton, 2, 2, 1, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(contentPane, closeButton, 0, 3, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(contentPane, spotImage, 2, 3, 1, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 5), 0, 0);
        pack();
        setSize(600, 500);
        setResizable(true);
        setTitle("Spot Information");
        setLocation(300, 100);
        show();
    }
    
    /*
    private String createMessage(ISlideDataElement element) {
        int stringLength = 0;
        int trueRow = element.getRow(ISlideDataElement.BASE);
        int trueColumn = element.getColumn(ISlideDataElement.BASE);
        float cy3 = this.slideData.getCY3(this.probe);
        float cy5 = this.slideData.getCY5(this.probe);
        
        //	float cy3 = element.getTrueIntensity(ISlideDataElement.CY3);
        //  float cy5 = element.getTrueIntensity(ISlideDataElement.CY5);
        //    float ratio = element.getTrueRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, LINEAR);
        //    float logRatio = element.getTrueRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, LOG);
        float ratio = cy5/cy3;
        
        float logRatio;
        
        if(cy3 < 0 || cy5 < 0) //for data input where neg. is posible.
            logRatio = Float.NaN;
        else
            logRatio = (float)(Math.log(ratio)/Math.log(2.0));

        String message = "<html><body bgcolor = \"#FFFFCC\"><basefont face = \"Arial\"><table cellpadding=4 valign=top><th colspan=2>Location and Intensities</th>";
        
        if(dataType == this.data.DATA_TYPE_TWO_INTENSITY){
            message += "<tr><td>Row</td><td>" + trueRow + "</td></tr>"+
            "<tr><td>Column</td><td>" + trueColumn + "</td></tr>";
            message += "<tr><td>Cy3</td><td><b>" + cy3 + "</b></td></tr>"+
            "<tr><td>Cy5</td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td>Ratio</td><td>" + ratio + "</td></tr>"+
            "<tr><td>log2(Ratio)</td><td><b>" + logRatio + "</b></td></tr>";
        } else if(dataType == this.data.DATA_TYPE_AFFY_ABS){
            message += "<tr><td>File Index</td><td>+" + trueRow + "</td></tr>";
            message += "<tr><td>Affy Loading Mode</td><td>Absolute</td></tr>";
            message += "<tr><td>Intensity</td><td><b>" + cy5 + "</b></td></tr>";
        } else if(dataType == this.data.DATA_TYPE_AFFY_REF){
            message += "<tr><td>File Index</td><td>" + trueRow + "</td></tr>";
            message += "<tr><td>Affy Loading Mode</td><td>Reference</td></tr>";
            message += "<tr><td>Sample Intensity</td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td>Ref. Intensity</td><td><b>" + cy3 + "</b></td></tr>";
            message += "<tr><td>Ratio (Sample/Ref.)</td><td>" + ratio + "</td></tr>"+
            "<tr><td>log2(Ratio)</td><td><b>" + logRatio + "</b></td></tr>";
        } else if(dataType == this.data.DATA_TYPE_AFFY_MEAN){
            message += "<tr><td>File Index</td><td>" + trueRow + "</td></tr>";
            message += "<tr><td>Affy Loading Mode</td><td>Array Set Mean Int. as Ref.</td></tr>";
            message += "<tr><td>Sample Intensity</td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td>Array Set Mean Int.</td><td><b>" + cy3 + "</b></td></tr>";
            message += "<tr><td>Ratio (Sample/Mean)</td><td>" + ratio + "</td></tr>"+
            "<tr><td>log2(Ratio)</td><td><b>" + logRatio + "</b></td></tr>";
        } else if(dataType == this.data.DATA_TYPE_AFFY_MEDIAN){
            message += "<tr><td>File Index</td><td>" + trueRow + "</td></tr>";
            message += "<tr><td>Affy Loading Mode</td><td>Spot Median as Ref.</td></tr>";
            message += "<tr><td>Sample Intensity</td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td>Spot Median Intensity</td><td><b>" + cy3 + "</b></td></tr>";
            message += "<tr><td>Ratio (Sample/Median)</td><td>" + ratio + "</td></tr>"+
            "<tr><td>log2(Ratio)</td><td><b>" + logRatio + "</b></td></tr>";
        } else { //ratio only, like stanford
            logRatio = cy5;
            message += "<tr><td>File Index</td><td>" + trueRow + "</td></tr>";
            message += "<tr><td>Value</td><td><b>" + logRatio + "</b></td></tr>";
        }
       // message += "</table>";

        //experiment annotation
        Vector keys = slideData.getSlideDataKeys();
        Hashtable expLabels =  slideData.getSlideDataLabels();
        message += "<th colspan=2>Experiment Annotation</th/>";
        String key, value;
        for(int i = 0; i < keys.size(); i++){
            key = (String)(keys.elementAt(i));
            value = (String)(expLabels.get(key));
            if(value == null)
                value = "";
            message += "<tr valign=top><td>" + key + "</td><td>" + value + "</td></tr>";         
        }
      //  message += "</table>";
        
        //gene annotation
        String[] fieldNames = TMEV.getFieldNames();
        if(fieldNames != null && fieldNames.length > 0){
            message += "<th>Gene Annotation</th>";
            for (int i = 0; i < fieldNames.length; i++) {
                message += "<tr valign=top><td>" + fieldNames[i] + "</td><td>" + element.getFieldAt(i) + "</td></tr>";
            }
          //  message += "</table>";
        }
        
        //spot specific information
        SpotInformationData spotData = this.slideData.getSpotInformationData();
        if(spotData != null){
            String [] spotInfoLabels = spotData.getSpotInformationHeader();
            String [] info = spotData.getSpotInformationArray(probe);
            message += "<th colspan=2>Spot Information</th>";
            for (int i = 0; i < spotInfoLabels.length; i++) {
                message += "<tr valign=top><td>" + spotInfoLabels[i] + "</td><td>" + info[i] + "</td></tr>";
            }
           // message += "</table>";
        }
        message += "</table>";
        message += "</basefont></body></html>";
        return message;
    }
    */
    
    /* with italics 
    private String createMessage(ISlideDataElement element) {
        int stringLength = 0;
        int trueRow = element.getRow(ISlideDataElement.BASE);
        int trueColumn = element.getColumn(ISlideDataElement.BASE);
        float cy3 = this.slideData.getCY3(this.probe);
        float cy5 = this.slideData.getCY5(this.probe);
        
        //	float cy3 = element.getTrueIntensity(ISlideDataElement.CY3);
        //  float cy5 = element.getTrueIntensity(ISlideDataElement.CY5);
        //    float ratio = element.getTrueRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, LINEAR);
        //    float logRatio = element.getTrueRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, LOG);
        float ratio = cy5/cy3;
        
        float logRatio;
        
        if(cy3 < 0 || cy5 < 0) //for data input where neg. is posible.
            logRatio = Float.NaN;
        else
            logRatio = (float)(Math.log(ratio)/Math.log(2.0));

        String message = "<html><body bgcolor = \"#FFFFCC\"><basefont face = \"Arial\"><table cellpadding=4 valign=top><th colspan=2 halign=left>Location and Intensities</th>";
        
        if(dataType == this.data.DATA_TYPE_TWO_INTENSITY){
            message += "<tr><td><i>Row</i></td><td>" + trueRow + "</td></tr>"+
            "<tr><td><i>Column</i></td><td>" + trueColumn + "</td></tr>";
            message += "<tr><td>Cy3</td><td><b>" + cy3 + "</b></td></tr>"+
            "<tr><td><i>Cy5</i></td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td><i>Ratio</i></td><td>" + ratio + "</td></tr>"+
            "<tr><td><i>log2(Ratio)</i></td><td><b>" + logRatio + "</b></td></tr>";
        } else if(dataType == this.data.DATA_TYPE_AFFY_ABS){
            message += "<tr><td><i>File Index</i></td><td>+" + trueRow + "</td></tr>";
            message += "<tr><td><i>Affy Loading Mode</i></td><td>Absolute</td></tr>";
            message += "<tr><td><i>Intensity<i>/</td><td><b>" + cy5 + "</b></td></tr>";
        } else if(dataType == this.data.DATA_TYPE_AFFY_REF){
            message += "<tr><td><i>File Index</i></td><td>" + trueRow + "</td></tr>";
            message += "<tr><td><i>Affy Loading Mode</i></td><td>Reference</td></tr>";
            message += "<tr><td><i>Sample Intensity</i></td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td><i>Ref. Intensity</i></td><td><b>" + cy3 + "</b></td></tr>";
            message += "<tr><td><i>Ratio (Sample/Ref.)</i></td><td>" + ratio + "</td></tr>"+
            "<tr><td><i>log2(Ratio)</i></td><td><b>" + logRatio + "</b></td></tr>";
        } else if(dataType == this.data.DATA_TYPE_AFFY_MEAN){
            message += "<tr><td><i>File Index</i></td><td>" + trueRow + "</td></tr>";
            message += "<tr><td><i>Affy Loading Mode</i></td><td>Array Set Mean Int. as Ref.</td></tr>";
            message += "<tr><td><i>Sample Intensity</i></td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td><i>Array Set Mean Int.</i></td><td><b>" + cy3 + "</b></td></tr>";
            message += "<tr><td><i>Ratio (Sample/Mean)</i></td><td>" + ratio + "</td></tr>"+
            "<tr><td><i>log2(Ratio)</i></td><td><b>" + logRatio + "</b></td></tr>";
        } else if(dataType == this.data.DATA_TYPE_AFFY_MEDIAN){
            message += "<tr><td><i>File Index</i></td><td>" + trueRow + "</td></tr>";
            message += "<tr><td><i>Affy Loading Mode</i></td><td>Spot Median as Ref.</td></tr>";
            message += "<tr><td><i>Sample Intensity</i></td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td><i>Spot Median Intensity</i></td><td><b>" + cy3 + "</b></td></tr>";
            message += "<tr><td><i>Ratio (Sample/Median)</i></td><td>" + ratio + "</td></tr>"+
            "<tr><td><i>log2(Ratio)</i></td><td><b>" + logRatio + "</b></td></tr>";
        } else { //ratio only, like stanford
            logRatio = cy5;
            message += "<tr><td><i>File Index</i></td><td>" + trueRow + "</td></tr>";
            message += "<tr><td><i>Value</i></td><td><b>" + logRatio + "</b></td></tr>";
        }
       // message += "</table>";

        //experiment annotation
        Vector keys = slideData.getSlideDataKeys();
        Hashtable expLabels =  slideData.getSlideDataLabels();
        message += "<th colspan=2 halign=left>Experiment Annotation</th/>";
        String key, value;
        for(int i = 0; i < keys.size(); i++){
            key = (String)(keys.elementAt(i));
            value = (String)(expLabels.get(key));
            if(value == null)
                value = "";
            message += "<tr valign=top><td><i>" + key + "</i></td><td>" + value + "</td></tr>";         
        }
      //  message += "</table>";
        
        //gene annotation
        String[] fieldNames = TMEV.getFieldNames();
        if(fieldNames != null && fieldNames.length > 0){
            message += "<th colspan=2 halign=left>Gene Annotation</th>";
            for (int i = 0; i < fieldNames.length; i++) {
                message += "<tr valign=top><td><i>" + fieldNames[i] + "</i></td><td>" + element.getFieldAt(i) + "</td></tr>";
            }
          //  message += "</table>";
        }
        
        //spot specific information
        SpotInformationData spotData = this.slideData.getSpotInformationData();
        if(spotData != null){
            String [] spotInfoLabels = spotData.getSpotInformationHeader();
            String [] info = spotData.getSpotInformationArray(probe);
            message += "<th colspan=2 halign=left>Spot Information</th>";
            for (int i = 0; i < spotInfoLabels.length; i++) {
                message += "<tr valign=top><td><i>" + spotInfoLabels[i] + "</i></td><td>" + info[i] + "</td></tr>";
            }
           // message += "</table>";
        }
        message += "</table>";
        message += "</basefont></body></html>";
        return message;
    }
     **/
    
    private String createMessage(ISlideDataElement element) {
    	
    	
        int stringLength = 0;
        int trueRow = element.getRow(ISlideDataElement.BASE);
        int trueColumn = element.getColumn(ISlideDataElement.BASE);
        
        float cy3 = this.slideData.getCY3(this.probe);
        float cy5 = this.slideData.getCY5(this.probe);
        
        //	float cy3 = element.getTrueIntensity(ISlideDataElement.CY3);
        //  float cy5 = element.getTrueIntensity(ISlideDataElement.CY5);
        //    float ratio = element.getTrueRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, LINEAR);
        //    float logRatio = element.getTrueRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, LOG);
        float ratio = cy5/cy3;
        
        float logRatio;
        
        if(cy3 < 0 || cy5 < 0) //for data input where neg. is posible.
            logRatio = Float.NaN;
        else
            logRatio = (float)(Math.log(ratio)/Math.log(2.0));

        String message = "<html><body bgcolor = \"#FFFFCC\"><basefont face = \"Arial\"><table cellpadding=4 valign=top><th colspan=2 align=left valign=center><font size=6>Location and Intensities</font></th>";
        
        if(dataType == IData.DATA_TYPE_TWO_INTENSITY){
            message += "<tr><td><i>Row</i></td><td>" + trueRow + "</td></tr>"+
            "<tr><td><i>Column</i></td><td>" + trueColumn + "</td></tr>";
            message += "<tr><td>Cy3</td><td><b>" + cy3 + "</b></td></tr>"+
            "<tr><td><i>Cy5</i></td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td><i>Ratio</i></td><td>" + ratio + "</td></tr>"+
            "<tr><td><i>log2(Ratio)</i></td><td><b>" + logRatio + "</b></td></tr>";
        } else if(dataType == IData.DATA_TYPE_AFFY_ABS){
            message += "<tr><td><i>File Index</i></td><td>+" + trueRow + "</td></tr>";
            message += "<tr><td><i>Affy Loading Mode</i></td><td>Absolute</td></tr>";
            message += "<tr><td><i>Intensity<i>/</td><td><b>" + cy5 + "</b></td></tr>";
        } else if(dataType == IData.DATA_TYPE_AFFY_REF){
            message += "<tr><td><i>File Index</i></td><td>" + trueRow + "</td></tr>";
            message += "<tr><td><i>Affy Loading Mode</i></td><td>Reference</td></tr>";
            message += "<tr><td><i>Sample Intensity</i></td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td><i>Ref. Intensity</i></td><td><b>" + cy3 + "</b></td></tr>";
            message += "<tr><td><i>Ratio (Sample/Ref.)</i></td><td>" + ratio + "</td></tr>"+
            "<tr><td><i>log2(Ratio)</i></td><td><b>" + logRatio + "</b></td></tr>";
        } else if(dataType == IData.DATA_TYPE_AFFY_MEAN){
            message += "<tr><td><i>File Index</i></td><td>" + trueRow + "</td></tr>";
            message += "<tr><td><i>Affy Loading Mode</i></td><td>Array Set Mean Int. as Ref.</td></tr>";
            message += "<tr><td><i>Sample Intensity</i></td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td><i>Array Set Mean Int.</i></td><td><b>" + cy3 + "</b></td></tr>";
            message += "<tr><td><i>Ratio (Sample/Mean)</i></td><td>" + ratio + "</td></tr>"+
            "<tr><td><i>log2(Ratio)</i></td><td><b>" + logRatio + "</b></td></tr>";
        } else if(dataType == IData.DATA_TYPE_AFFY_MEDIAN){
            message += "<tr><td><i>File Index</i></td><td>" + trueRow + "</td></tr>";
            message += "<tr><td><i>Affy Loading Mode</i></td><td>Spot Median as Ref.</td></tr>";
            message += "<tr><td><i>Sample Intensity</i></td><td><b>" + cy5 + "</b></td></tr>";
            message += "<tr><td><i>Spot Median Intensity</i></td><td><b>" + cy3 + "</b></td></tr>";
            message += "<tr><td><i>Ratio (Sample/Median)</i></td><td>" + ratio + "</td></tr>"+
            "<tr><td><i>log2(Ratio)</i></td><td><b>" + logRatio + "</b></td></tr>";
        } else { //ratio only, like stanford
            logRatio = cy5;
            message += "<tr><td><i>File Index</i></td><td>" + trueRow + "</td></tr>";
            message += "<tr><td><i>Value</i></td><td><b>" + logRatio + "</b></td></tr>";
        }
       // message += "</table>";

        //experiment annotation
        Vector keys = slideData.getSlideDataKeys();
        Hashtable expLabels =  slideData.getSlideDataLabels();
        message += "<th colspan=2 align=left valign=center><font size=6>Sample Annotation</font></header></th/>";
        String key, value;
        for(int i = 0; i < keys.size(); i++){
            key = (String)(keys.elementAt(i));
            value = (String)(expLabels.get(key));
            if(value == null)
                value = "";
            message += "<tr valign=top><td><i>" + key + "</i></td><td>" + value + "</td></tr>";         
        }
      //  message += "</table>";
        
        
        /**
         * Added by Sarita
         * This loop was introduced to enable display of annotation loaded in the MevAnnotation object
         * in the Spot Information box.
         * This has been implemented such that the static boolean variable "isAnnotationLoaded" is first checked,
         * to ensure that annotation has been loaded. If so, the Spot Information box displays
         * all the available annotation for that spot present in the MevAnnotation object.
         * 
         * If Annotation has not been loaded, then the information in "ExtraFields" is displayed.
         * "Extra Fields" has a subset of information present in MevAnnnotation.
         * 
         * 
         * 
         */
       
        String[] fieldNames = slideData.getSlideMetaData().getFieldNames();
        if(data.isAnnotationLoaded()) {
        	
        	message += "<th colspan=2 align=left valign=center><font size=6>Gene Annotation</font></th>";
        	
        	for (int i = 0; i < MevAnnotation.getFieldNames().length; i++) {                
        		IAnnotation anno=element.getElementAnnotation();
        		String[]annotations=data.getElementAnnotation(this.probe, MevAnnotation.getFieldNames()[i]); 
//        		System.out.println("InfoDisplay:annotations"+annotations[0]);
        		if(annotations.length>1) {
        			message += "<tr valign=top><td><i>"  +MevAnnotation.getFieldNames()[i]+ "</i></td><td>" + ((AnnoAttributeObj)data.getElementAnnotationObject(i, MevAnnotation.getFieldNames()[i])).toString() + "</td></tr>";
//        		System.out.println("annotation size is >1:");
        		}
        		else
        			
        		message += "<tr valign=top><td><i>" +MevAnnotation.getFieldNames()[i] +"</i></td><td>" + annotations[0] +"</td></tr>";
        		
        	}               
        }



        else {
        if(fieldNames != null && fieldNames.length > 0){

        	message += "<th colspan=2 align=left valign=center><font size=6>Gene Annotation</font></th>";
        	for (int i = 0; i < fieldNames.length; i++) {                

        		//pcahan change to call getDetection on the element rather than the field  
        		if(fieldNames[i].equals("Detection")){
        			message += "<tr valign=top><td><i>" + fieldNames[i] + "</i></td><td>" + element.getDetection() + "</td></tr>";
        		}
        		else if(fieldNames[i].equals("P-value")){
        			message += "<tr valign=top><td><i>" + fieldNames[i] + "</i></td><td>" + element.getPvalue() + "</td></tr>";;
        		}else{
        			message += "<tr valign=top><td><i>" + fieldNames[i] + "</i></td><td>" + element.getFieldAt(i) + "</td></tr>";  
        		}               
        	}

        }
        }

        //spot specific information
        SpotInformationData spotData = this.slideData.getSpotInformationData();
        if(spotData != null){
            String [] spotInfoLabels = spotData.getSpotInformationHeader();
            String [] info = spotData.getSpotInformationArray(probe);
            message += "<th colspan=2 align=left valign=center><font size=6>Spot Information</font></th>";
            for (int i = 0; i < spotInfoLabels.length; i++) {
                message += "<tr valign=top><td><i>" + spotInfoLabels[i] + "</i></td><td>" + info[i] + "</td></tr>";
            }
           // message += "</table>";
        }
        message += "</table>";
        message += "</basefont></body></html>";
        return message;
    }
    
    
    public void createGeneGraph() {
        JFrame graphFrame;
        GraphViewer graph;
        GraphPoint gp;
        GraphLine gl;
        GraphTick gt;
        ISlideData sde;
        
        ISlideData[] targets;
        
        targets = new ISlideData[data.getFeaturesCount()];
        int columnsInExperiments = data.getFeature(0).getSlideMetaData().getColumns();
        
        for (int i = 0; i < targets.length; i++) {
            targets[i] = data.getFeature(i);//.getSlideDataElement(element.getLocation(ISlideDataElement.BASE, columnsInExperiments) - 1);
        }
        
        int minCy3 = 0;
        int maxCy3 = targets.length;
        int minCy5 = Integer.MAX_VALUE;
        int maxCy5 = Integer.MIN_VALUE;
        
        int workingRatio = 0;
        
        for (int i = 0; i < targets.length; i++) {
            sde = targets[i];

            workingRatio = (int)sde.getRatio(this.probe, LOG); //Xcon.log2(sde.getRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, AC.LINEAR));

            if (workingRatio < minCy5) minCy5 = workingRatio;
            if (workingRatio > maxCy5) maxCy5 = workingRatio;
        }
        
        minCy5--;
        maxCy5++;
        
        int xGap = (int)(maxCy3 - minCy3);
        int yGap = (int)(maxCy5 - minCy5);
        
        graphFrame = new JFrame("Sample vs. Log Ratio");
        graph = new GraphViewer(graphFrame, 0, 500, 0, 500, minCy3, maxCy3, minCy5, maxCy5, 100, 100, 100, 100, "Sample vs. Log Ratio", "Sample Name", "Log2 (Cy5 / Cy3)");
        
        graph.setXAxisValue(0);
        graph.setYAxisValue(minCy3);
        
        for (int i = minCy3 + 1; i <= maxCy3; i++) {
            gl = new GraphLine(i, minCy5, i, maxCy5, Color.yellow);
            graph.addGraphElement(gl);
        }
        
        for (int i = minCy5; i <= maxCy5; i++) {
            if (i != 0) {
                gl = new GraphLine(minCy3, i, maxCy3, i, Color.yellow);
                graph.addGraphElement(gl);
            }
        }
        
        float cy3, cy5, cy3b, cy5b;
        double logCy3, logCy5, logCy3b, logCy5b;
        for (int i = 0; i < targets.length - 1; i++) {
            sde = targets[i];
            
            cy3 = sde.getCY3(this.probe);
            cy5 = sde.getCY5(this.probe);
            logCy3 = i + 1;
            
            if(dataType == IData.DATA_TYPE_RATIO_ONLY)
                logCy5 = cy5;
            else
                logCy5 = Xcon.log2((double) cy5 / (double) cy3);
            
            sde = targets[i + 1];
            
            cy3b = sde.getCY3(this.probe);
            cy5b = sde.getCY5(this.probe);
            logCy3b = i + 2;
            if(dataType == IData.DATA_TYPE_RATIO_ONLY)
                logCy5b = cy5b;
            else
                logCy5b = Xcon.log2((double) cy5b / (double) cy3b);
            if(!Double.isNaN(logCy5) && !Double.isNaN(logCy5b)){
                gl = new GraphLine(logCy3, logCy5, logCy3b, logCy5b, Color.blue);
                graph.addGraphElement(gl);
            }
        }
        for (int i = 0; i < targets.length; i++) {
            sde = targets[i];
            
            cy3 = sde.getCY3(this.probe);
            cy5 = sde.getCY5(this.probe);
            logCy3 = i + 1;
            if(dataType == IData.DATA_TYPE_RATIO_ONLY)
                logCy5 = cy5;
            else
                logCy5 = Xcon.log2((double) cy5 / (double) cy3);
            if(!Double.isNaN(logCy5)){
                gp = new GraphPoint(logCy3, logCy5, Color.blue, 3);
                graph.addGraphElement(gp);
            }
        }
        
        for (int i = minCy5; i <= maxCy5; i++) {
            if (i == 0) gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "0", Color.black);
            //else gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "2.0E" + i, Color.black);
            else gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "" + i, Color.black);
            graph.addGraphElement(gt);
        }
        
        for (int i = minCy3 + 1; i <= maxCy3; i++) {
            gt = new GraphTick(i, 8, Color.black, GC.HORIZONTAL, GC.C, data.getSampleName(i - 1), Color.black);
            graph.addGraphElement(gt);
        }
        
        graphFrame.setSize(500, 500);
        graph.setVisible(true);
    }
    
    class EventListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("close"))
                dispose();
            else if (command.equals("view-gene-graph")) {
                hide();
                createGeneGraph();
                dispose();
            } else if (command.equals("view-experiment")) {
                hide();
                Manager.createNewSingleArrayViewer(slideData);
                dispose();
            } else if (command.equals("set-color")) {
            }
            		
        }

    }	     
}
/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: PaperReferencesDialog.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-05-05 19:40:59 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.tigr.util.BrowserLauncher;
import org.tigr.util.awt.GBA;

public class PaperReferencesDialog extends JDialog implements java.awt.print.Printable,HyperlinkListener{
    
    private String text;
    private JEditorPane ed;
    
    public PaperReferencesDialog(Frame parent) {
        this(parent, "");
    }
    
    public PaperReferencesDialog(Frame parent, String labelText) {
        super(parent, "Papers / Publications Reference", false);
        EventListener listener = new EventListener();
        this.text = labelText;
        GBA gba = new GBA();
        Font font = new Font("serif", Font.PLAIN, 12);
        
        ed = new JEditorPane("text/html", labelText);
        ed.setEditable(false);
        ed.setMargin(new Insets(10,10,10,10));
        ed.setBackground(new Color(234,233,191));
        ed.setCaretPosition(0);
        ed.addHyperlinkListener(this);
        JScrollPane scrollPane = new JScrollPane(ed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel referencesPanel = new JPanel(new GridBagLayout());
        referencesPanel.setBackground(new Color(234,233,191));
        gba.add(referencesPanel, scrollPane, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C);
        
        JButton printButton = new JButton("Print");
        printButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        printButton.setFocusPainted(false);
        printButton.setActionCommand("print-command");
        printButton.addActionListener(listener);
        
        JButton closeButton = new JButton("  Close  ");
        closeButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        closeButton.setFocusPainted(false);
        closeButton.setActionCommand("close-command");
        closeButton.addActionListener(listener);
        closeButton.setSize(120,30);
        closeButton.setPreferredSize(new Dimension(120, 30));
        
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
     //   gba.add(buttonPanel, printButton, 0, 0, 1, 1, 0,0, GBA.NONE, GBA.C);
        gba.add(buttonPanel, closeButton, 0, 0, 1, 1, 1, 1, GBA.NONE, GBA.C);
        
        getContentPane().setLayout(new GridBagLayout());
        gba.add(getContentPane(), referencesPanel, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C);
        gba.add(getContentPane(), buttonPanel, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.C);
        
        setSize(550, 650);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
    }
    public static String createReferencesText() {
        String thisLine, html;
        html = "";
        try {
        	InputStream is = AcknowlegementDialog.class.getResourceAsStream("PaperReferences.html");
        	BufferedReader br = new BufferedReader(new InputStreamReader(is));
        	while ((thisLine = br.readLine()) != null) {  
        		html+=thisLine;
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return html;
    }
    
    public static void main(String [] args){
        PaperReferencesDialog d = new PaperReferencesDialog(new Frame(), PaperReferencesDialog.createReferencesText());
        //System.exit(0);
    }
    
    public int print(java.awt.Graphics g, java.awt.print.PageFormat format, int page) throws java.awt.print.PrinterException {
        if(page > 2)
            return Printable.NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D)g;
       	g2d.clip(new java.awt.geom.Rectangle2D.Double(0, 0, format.getImageableWidth(), format.getImageableHeight()));
	g2d.translate(format.getImageableX(), -(page) * format.getImageableHeight());   
        g2d.scale(1.0,1.0);
        g2d.drawString("Test String", 0,20);   
        ed.paint(g);
        return Printable.PAGE_EXISTS;      
    }
    
    private Book makeBook(PageFormat page, int numPages){
        Book book = new Book();
        book.append(this, page, numPages);
        return book;
    }
    
    private class EventListener implements ActionListener, KeyListener {
        
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("close-command")) {
                dispose();
            }
            else if(command.equals("print-command")){
                PrinterJob pj = PrinterJob.getPrinterJob();
                pj.setPrintable(PaperReferencesDialog.this, pj.defaultPage());
                int numPages = ed.getHeight();
                numPages /= pj.defaultPage().getImageableY();
                
                pj.setPageable(makeBook(pj.defaultPage(), numPages));
                if (pj.printDialog()) {
                    try {
                        pj.print();
                    } catch (PrinterException pe) {
                        System.out.println(pe);                       
                    }
                }
                
            }
        }
        
        
        
        public void keyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                dispose();
            }
        }
        
        public void keyReleased(KeyEvent event) {;}
        public void keyTyped(KeyEvent event) {;}
    }

	public void hyperlinkUpdate(HyperlinkEvent e) {
		// TODO Auto-generated method stub
		HyperlinkEvent.EventType type=e.getEventType();
		URL url=e.getURL();
		if(type==HyperlinkEvent.EventType.ACTIVATED){
			try {
				//BrowserLauncher.openURL("http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=9843981&query_hl=4&itool=pubmed_docsum" );
				BrowserLauncher.openURL(url.toString());
			} catch( IOException ex ) {
				//e.printStackTrace();
				
			}
		}
	}
}
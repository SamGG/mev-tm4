/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.bn;import javax.swing.JPanel;import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class RunWekaProgressPanel extends JFrame implements WindowListener, ActionListener {

	private JProgressBar progressBar;
	private JButton cancelButton;
	//MultipleArrayViewer mav;
	JPanel progressPanel;

	public RunWekaProgressPanel() {		setTitle("Running Data Mining...");   		/*
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				BNGUI.cancelRun = true;
				BNGUI.run = true;
				dispose();
			}
		});
		 */
		progressPanel = new JPanel(new BorderLayout());
		progressPanel.setPreferredSize(new Dimension(350, 100));

		progressBar = new JProgressBar(0, 100);
		//progressBar.setValue(0);
		progressBar.setString("");
		progressBar.setStringPainted(true);		progressBar.setPreferredSize(new Dimension(310, 30));


		progressPanel.add(progressBar, BorderLayout.PAGE_START);		//progressPanel.add(cancelButton, BorderLayout.SOUTH);
		progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));		setContentPane(progressPanel);

		progressPanel.setOpaque(true);		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack(); 
		setVisible(true);
	}	public void setString(String label) {		progressBar.setString(label);	}
	public void setIndeterminate(boolean b){
		progressBar.setIndeterminate(b);
	}
	public void actionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();
		if (command.equals("cancel")) {
			//mav.cancelLoadState();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			progressBar.setIndeterminate(true);
		}
	}
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowClosed(WindowEvent arg0) {
		//BNGUI.cancelRun = true;
		//BNGUI.run = true;
		//dispose();
	}

	public void windowClosing(WindowEvent arg0) {
		//BNGUI.cancelRun = true;
		//BNGUI.run = true;
		//dispose();
	}

	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}
}    

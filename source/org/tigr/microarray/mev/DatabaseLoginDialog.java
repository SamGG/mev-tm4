/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: DatabaseLoginDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:41 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.tigr.util.awt.ActionInfoDialog;
import org.tigr.util.awt.ActionInfoEvent;
import org.tigr.util.awt.GBA;

public class DatabaseLoginDialog extends ActionInfoDialog {
    private JFrame parent;
    JLabel usernameLabel, passwordLabel;
    JTextField usernameTextField;
    JPasswordField passwordField;
    JButton okButton, cancelButton;
    Font databaseLoginDialogFont;
    GBA gba;
    
    public DatabaseLoginDialog(JFrame parent) {
	super(parent, true);
	try {
	    this.parent = parent;
	    gba = new GBA();
	    
	    usernameLabel = new JLabel("Username: ");
	    
	    usernameTextField = new JTextField(10);
	    usernameTextField.addKeyListener(new EventListener());
	    
	    passwordLabel = new JLabel("Password: ");
	    
	    passwordField = new JPasswordField(10);
	    passwordField.setEchoChar('*');
	    passwordField.addKeyListener(new EventListener());
	    
	    okButton = new JButton("Login");
	    okButton.addActionListener(new EventListener());
	    
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new EventListener());
	    
	    contentPane.setLayout(new GridBagLayout());
	    gba.add(contentPane, usernameLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, usernameTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, passwordLabel, 0, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, passwordField, 1, 1, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, cancelButton, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, okButton, 1, 2, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    
	    pack();
	    setResizable(false);
	    setTitle("Database Login");
	    usernameTextField.grabFocus();
	    setLocation(150, 150);
	} catch (Exception e) {
	    System.out.println("Exception (DatabaseLoginDialog.const()): " + e);
	}
    }
    
    class EventListener implements ActionListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    if (event.getSource() == okButton) {
		String username = usernameTextField.getText();
		String password = new String(passwordField.getPassword());
		hide();
		
		Hashtable hash = new Hashtable();
		hash.put(new String("username"), username);
		hash.put(new String("password"), password);
		fireEvent(new ActionInfoEvent(this, hash));
		
		dispose();
	    } else if (event.getSource() == cancelButton) dispose();
	}
	
	public void keyPressed(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		String username = usernameTextField.getText();
		String password = new String(passwordField.getPassword());
		hide();
		
		Hashtable hash = new Hashtable();
		hash.put(new String("username"), username);
		hash.put(new String("password"), password);
		fireEvent(new ActionInfoEvent(this, hash));
		
		dispose();
	    }
	}
	
	public void keyReleased(KeyEvent event) {;}
	public void keyTyped(KeyEvent event) {;}
    }
}

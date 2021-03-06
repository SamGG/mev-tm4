/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.resources;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.swing.JOptionPane;

public abstract class FileDownloader {
	protected URL hostURL;
	RMProgress progress;
	int result = 0;
	
	protected FileDownloader(URL host) {
		try {
			this.hostURL = new URL(host.getProtocol(), host.getHost(), host.getPort(), "");
		} catch (MalformedURLException mue) {
		}
	}
	
	public static FileDownloader getInstance(URL host) {
		if (host.getProtocol().startsWith("ftp")) {
			return new FTPFileDownloader(host); 
		} else if (host.getProtocol().startsWith("sftp")) {
			return new SFTPFileDownloader(host);
		} else if (host.getProtocol().startsWith("http")) {
			return new HTTPDownloader(host);
		}
		return null;
	}

	public abstract boolean connect() throws IOException;
	public abstract void disconnect();
	public abstract Date getLastModifiedDate(String path);
	public abstract File getTempFile(String path) throws SupportFileAccessError;
	public abstract String[] getFileList(String path);
	public void destroy() {
		disconnect();
	}
	public boolean wasCancelled() {
		return progress.wasCancelled;
	}
	/**
	 * The class to listen to the dialog.
	 */
	public class DownloadProgressListener extends org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener implements WindowListener  {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();

			if (command.equals("cancel-command")) {
				progress.wasCancelled = true;
//				System.out.println("FileDownloader$DownloadProgressListener Cancelling...");
				result = JOptionPane.CANCEL_OPTION;
				progress.dispose();
			}
			if (command.equals("window-close-command")) {
				progress.wasCancelled = true;
				result = JOptionPane.CANCEL_OPTION;
				progress.dispose();
			}
		}

		public void windowClosing(WindowEvent e) {
			progress.wasCancelled = true;
			result = JOptionPane.CANCEL_OPTION;
//			System.out.println("closing window");
			progress.dispose();
		}
	}
}
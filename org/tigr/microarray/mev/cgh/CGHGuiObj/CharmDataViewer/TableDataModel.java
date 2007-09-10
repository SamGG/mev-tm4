package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import javax.swing.table.AbstractTableModel;

public class TableDataModel extends AbstractTableModel {
	Object[][] data;
    String[] columnNames = {"Probe Name",
				          "Chromosome",
				          "Log2 Ratio",
				          "Desc"};
  TableDataModel(Object[][] dataModel) {
	  data = dataModel;
	  
  }
	    public int getColumnCount() {
	        return columnNames.length;
	    }

	    public int getRowCount() {
	        return data.length;
	    }

	    public String getColumnName(int col) {
	        return columnNames[col];
	    }

	    public Object getValueAt(int row, int col) {
	        return data[row][col];
	    }

	    public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }

	    /*
	     * Don't need to implement this method unless your table's
	     * editable.
	     */
	    public boolean isCellEditable(int row, int col) {
	        //Note that the data/cell address is constant,
	        //no matter where the cell appears onscreen.
	    	/*
	        if (col < 2) {
	            return false;
	        } else {
	            return true;
	        }
	        */
	    	return false;
	    }

	    /*
	     * Don't need to implement this method unless your table's
	     * data can change.
	     */
	    public void setValueAt(Object value, int row, int col) {
	        data[row][col] = value;
	        fireTableCellUpdated(row, col);
	    }
	}

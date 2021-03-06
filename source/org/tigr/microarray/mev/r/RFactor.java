/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.r;

import java.util.*;

/** representation of a factor variable. In R there is no actual xpression
    type called "factor", instead it is coded as an int vector with a list
    attribute. The parser code of REXP converts such constructs directly into
    the RFactor objects and defines an own XT_FACTOR type 
    
    @version $Id: RFactor.java,v 1.3 2006-03-07 19:00:35 caliente Exp $
*/    
public class RFactor extends Object {
    /** IDs (content: Integer) each entry corresponds to a case, ID specifies the category */
    Vector id;
    /** values (content: String), ergo category names */
    Vector val;

    /** create a new, empty factor var */
    public RFactor() { id=new Vector(); val=new Vector(); }
    
    /** create a new factor variable, based on the supplied arrays.
	@param i array if IDs (0..v.length-1)
	@param v values - cotegory names */
    public RFactor(int[] i, String[] v) {
	id=new Vector(); val=new Vector();
	int j;
	if (i!=null && i.length>0)
	    for(j=0;j<i.length;j++)
		id.addElement(new Integer(i[j]));
	if (v!=null && v.length>0)
	    for(j=0;j<v.length;j++)
		val.addElement(new Integer(v[j]));
    }

    /** special constructor used by REXP parser to save some re-indexing
	and performing automatic index conversion
        @param i index array
        @param v vector of xpressions which should be all strings */
    public RFactor(int[] i, Vector v) {
	id=new Vector(); val=new Vector();
	int j;
	if (i!=null && i.length>0)
	    for(j=0;j<i.length;j++)
		id.addElement(new Integer(i[j]-1));
	if (v!=null && v.size()>0)
	    for(j=0;j<v.size();j++) {
		REXP x=(REXP)v.elementAt(j);
		val.addElement((x==null||x.Xt!=REXP.XT_STR)?null:(String)x.cont);
	    };
    };
    
    /** add a new element (by name)
	@param v value */
    public void add(String v) {
	int i=val.indexOf(v);
	if (i<0) {
	    i=val.size();
	    val.addElement(v);
	};
	id.addElement(new Integer(i));
    }

    /** returns name for a specific ID 
	@param i ID
	@return name. may throw exception if out of range */
    public String at(int i) {
	if (i<0||i>=id.size()) return null;
	return (String)val.elementAt(((Integer)id.elementAt(i)).intValue());
    }

    /** returns the number of caes */
    public int size() { return id.size(); }

    /** displayable representation of the factor variable */
    public String toString() {
	//return "{"+((val==null)?"<null>;":("levels="+val.size()+";"))+((id==null)?"<null>":("cases="+id.size()))+"}";
	StringBuffer sb=new StringBuffer("{levels=(");
	if (val==null)
	    sb.append("null");
	else
	    for (int i=0;i<val.size();i++) {
		sb.append((i>0)?",\"":"\"");
		sb.append((String)val.elementAt(i));
		sb.append("\"");
	    };
	sb.append("),ids=(");
	if (id==null)
	    sb.append("null");
	else
	    for (int i=0;i<id.size();i++) {
		if (i>0) sb.append(",");
		sb.append((Integer)id.elementAt(i));
	    };
	sb.append(")}");
	return sb.toString();
    }
}


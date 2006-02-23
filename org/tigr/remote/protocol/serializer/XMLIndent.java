/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: XMLIndent.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:02 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.protocol.serializer;

import java.io.PrintStream;
import java.io.PrintWriter;

class XMLIndent {

    private static final int STEP = 2;

    public XMLIndent() {
    }

    /**
     * Increase size of this indent.
     */
    public void inc() { m_count += STEP;}

    /**
     * Decrease size of this indent.
     */
    public void dec() {
        m_count -= STEP;
        if (m_count < 0) m_count = 0;
    }

    /**
     * Prints this indend to a specified stream.
     */
    public void print( PrintStream out )  {
        for (int i = 0; i < m_count; i++)
            out.print( ' ' );
    }

    /**
     * Prints this indend to a specified writer.
     */
    public void print( PrintWriter out )  {
        for (int i = 0; i < m_count; i++)
            out.print( ' ' );
    }

    private int m_count = 0;
}
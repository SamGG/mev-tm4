/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Parser.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:08 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import org.tigr.remote.protocol.Response;
import java.io.InputStream;

import org.tigr.util.ConfMap;

public abstract class Parser {
    /**
     * Must be overriden to parse data from specified stream.
     * @return an instance of Response as parse result.
     * @see Response
     */
    public abstract Response parseResponse( InputStream in ) throws ParserException ;

    /**
     * Creates a new instance of a <code>Parser</code> with specified configuration.
     */
    public static Parser createParser( ConfMap cfg ) throws ParserException {
        return new XMLResponseParser( cfg );
    }
}


/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RequestSerializer.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:08 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.serializer;

import java.io.OutputStream;

import org.tigr.util.ConfMap;
import org.tigr.remote.protocol.Request;

public abstract class RequestSerializer {
    /**
     * Must be overriden in concrete implementation.
     */
    public abstract void serializeRequest( Request req, OutputStream out ) throws SerializerException ;

    /**
     * Returns a <code>RequestSerializer</code> implementation with
     * specified configuration.
     */
    public static RequestSerializer createSerializer( ConfMap cfg ) throws SerializerException {
        return new RequestSerializerImpl();
    }
}


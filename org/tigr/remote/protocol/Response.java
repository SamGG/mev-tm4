/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Response.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

public class Response {
    
    /**
     * Constructs a <code>Response</code> with specified <code>FinishedJob</code>.
     * @see FinishedJob
     */
    public Response(FinishedJob job) { setJob( job );}
    
    /**
     * Returns reference to a <code>FinishedJob</code>.
     */
    public FinishedJob getJob() { return job;}
    
    /**
     * Sets a specified <code>FinishedJob</code>.
     */
    public void setJob( FinishedJob job ) { this.job = job;}
    
    private FinishedJob job;
}

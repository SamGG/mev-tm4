/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TerrainBillboard.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:33:21 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import javax.media.j3d.*; 
import javax.vecmath.*;

public class TerrainBillboard extends Billboard {

    private WakeupOnTransformChange wakeupFrameTG;

    public TerrainBillboard(TransformGroup transformgroup, int i, Point3f point3f, TransformGroup platformTransform) {
        super(transformgroup, i, point3f); 
        this.wakeupFrameTG = new WakeupOnTransformChange(platformTransform);
    }

    public void initialize() {
        super.wakeupOn(new WakeupOnElapsedFrames(0, true));
    }

    protected void wakeupOn(WakeupCondition wakeupcondition) {
        super.wakeupOn(this.wakeupFrameTG);
    }
}

/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLTreeData.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-05 20:25:10 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

public class HCLTreeData implements java.io.Serializable {
    
    static final long serialVersionUID = 1L;

    public int[] child_1_array;
    public int[] child_2_array;
    public int[] node_order;
    public float[] height;
    
    /** Getter for property child_1_array.
     * @return Value of property child_1_array.
     */
    public int[] getChild_1_array() {
        return this.child_1_array;
    }
    
    /** Setter for property child_1_array.
     * @param child_1_array New value of property child_1_array.
     */
    public void setChild_1_array(int[] child_1_array) {
        this.child_1_array = child_1_array;
    }
    
    /** Getter for property child_2_array.
     * @return Value of property child_2_array.
     */
    public int[] getChild_2_array() {
        return this.child_2_array;
    }
    
    /** Setter for property child_2_array.
     * @param child_2_array New value of property child_2_array.
     */
    public void setChild_2_array(int[] child_2_array) {
        this.child_2_array = child_2_array;
    }
    
    /** Getter for property node_order.
     * @return Value of property node_order.
     */
    public int[] getNode_order() {
        return this.node_order;
    }
    
    /** Setter for property node_order.
     * @param node_order New value of property node_order.
     */
    public void setNode_order(int[] node_order) {
        this.node_order = node_order;
    }
    
    /** Getter for property height.
     * @return Value of property height.
     */
    public float[] getHeight() {
        return this.height;
    }    
   
    /** Setter for property height.
     * @param height New value of property height.
     */
    public void setHeight(float[] height) {
        this.height = height;
    }    
    
}

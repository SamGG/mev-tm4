/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ClusterRepository.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.clusterUtil;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

import java.util.Vector;
import java.awt.Color;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

/** The ClusterRepository contains ClusterList objects created for
 * holding saved clusters particular analysis results.
 */
public class ClusterRepository extends Vector {
    
    public static final int GENE_CLUSTER = 0;
    public static final int EXPERIMENT_CLUSTER = 1;
    
    private int numberOfElements;
    /** Maintains a ClusterList array for each data element.
     * If an element is a member of a cluster then that element will
     * have a reference to it's cluster object.
     */
    private ClusterList [] elementClusters;
    
    /** A counter to assign cluster serial numbers as they are
     * added to the repository.
     */
    private int clusterSerialCounter = 0;
    /** true if the repository maintains gene clusters
     */
    private boolean geneClusterRepository = false;
    /** IFramework Object
     */
    private IFramework framework;
    
    
    /** Creates new ClusterRepository with a specified element count
     */
    public ClusterRepository(int numberOfElements, IFramework framework) {
        this.numberOfElements = numberOfElements;
        this.elementClusters = new ClusterList[numberOfElements];
        this.addClusterList(new ClusterList("Cluster Ops."));
        this.framework = framework;
    }
    
    /** Creates new ClusterRepository with specified cluster type*/
    public ClusterRepository(int numberOfElements, IFramework framework, boolean isGeneClusterRepository) {
        this.numberOfElements = numberOfElements;
        this.elementClusters = new ClusterList[numberOfElements];
        this.geneClusterRepository = isGeneClusterRepository;
        this.addClusterList(new ClusterList("Cluster Ops."));
        this.framework = framework;
    }
    
    /** Returns the color of the last cluster to which the element
     * (index) was assigned
     */
    public Color getColor(int index){
        if(elementClusters[index] == null  || elementClusters[index].size() == 0)
            return null;
        return elementClusters[index].lastCluster().getClusterColor();
    }
    
    /** Returns all cluster colors for the clusters to which
     * the element (index) was assigned
     */
    public Color [] getColors(int index){
        if(elementClusters[index] == null)
            return null;
        ClusterList list  = elementClusters[index];
        Color [] colors = new Color[list.size()];
        for(int i = 0; i < colors.length; i++){
            colors[i] = list.getClusterAt(i).getClusterColor();
        }
        return colors;
    }
    
    /** Returns the number of elements in the data set corresponding to
     * the repository.  (number of spots or experiments)
     */
    public int getDataElementCount(){
        return this.numberOfElements;
    }
    
    /** Returns true if the repository maintains gene clusters.
     */
    public boolean isGeneClusterRepository(){ return this.geneClusterRepository; }
    
    /** Returns a cluster list for the specified result index.
     */
    public ClusterList getClusterList(int index){
        if(isInRange(index))
            return ((ClusterList)elementAt(index));
        return null;
    }
    
    /** adds a provided Clusterlist
     */
    public void addClusterList(ClusterList list){
        add(list);
    }
    
    /** Adds a cluster to a specified ClusterList
     */
    public void addCluster(ClusterList list, Cluster cluster){
        if(cluster == null)
            return;
        
        //if not a cluster operation and cluster exists, modify cluster
        if(!((cluster.getSource()).equals("Cluster Op.")) && list.isClusterSaved(cluster.getClusterID())){
            Cluster savedCluster = list.getCluster(cluster.getClusterID());
            if(savedCluster == null){   //safety net
                list.addCluster(cluster);
                updateClusterMembership(cluster);
                return;
            }
            savedCluster.setClusterColor(cluster.getClusterColor());
            savedCluster.setClusterLabel(cluster.getClusterLabel());
            savedCluster.setClusterDescription(cluster.getClusterDescription());
            this.setClusterSerialCounter(this.getMaxClusterSerialNumber()-1);  //rollback counter
        } else {
            list.addCluster(cluster);
            updateClusterMembership(cluster);
        }
    }
    
        /** Adds a cluster to a specified ClusterList
     */
    public void addSubCluster(ClusterList list, Cluster cluster){
        if(cluster == null)
            return;
/*
        if(!((cluster.getSource()).equals("Cluster Op.")) && list.isClusterSaved(cluster.getClusterID())){
            Cluster savedCluster = list.getCluster(cluster.getClusterID());
            if(savedCluster == null){   //safety net
                list.addCluster(cluster);
                updateClusterMembership(cluster);
                return;
            }
            savedCluster.setClusterColor(cluster.getClusterColor());
            savedCluster.setClusterLabel(cluster.getClusterLabel());
            savedCluster.setClusterDescription(cluster.getClusterDescription());
            this.setClusterSerialCounter(this.getMaxClusterSerialNumber()-1);  //rollback counter
        } else {
 **/
            list.addCluster(cluster);
            updateClusterMembership(cluster);
      //  }
    }
    
    /** Adds a cluster to elements cluster list for elements
     * contained in a cluster
     */
    private void updateClusterMembership(Cluster cluster){
        int [] indices = cluster.getIndices();
        for(int i = 0; i < indices.length; i++){
            if(elementClusters[indices[i]] == null)
                elementClusters[indices[i]] = new ClusterList("element "+indices[i]);
            elementClusters[indices[i]].add(cluster);
        }
    }
    
    /** Removes clusters from the cluster list of each element
     * contained in the specified cluster.
     */
    private void removeClusterMembership(Cluster cluster){
        int [] indices = cluster.getIndices();
        for(int i = 0; i < indices.length; i++){
            if(elementClusters[indices[i]] != null)
                elementClusters[indices[i]].removeElement(cluster);
        }
    }
    
    /** Clears all cluster lists.
     */
    public void clearClusterLists(){
        for(int i = 0; i < size(); i++)
            this.getClusterList(i).clear();
        clearElementClusters();
        //   clear();
    }
    
    /** Clears the cluster lists for all elements.
     * Postcondition is that all elements do not have
     * references to any clusters.
     */
    private void clearElementClusters(){
        for(int i = 0; i < numberOfElements; i++)
            this.elementClusters[i] = null;
    }
    
    /** Returns true if an index for a cluster list is in range.
     */
    protected boolean isInRange(int index){
        return (index > -1 && index < size());
    }
    
    /** Stores a cluster given the passed parameters.
     */
    public Cluster storeCluster(int algorithmIndex, String algorithmName, String clusterID, int [] indices, Experiment experiment){
        
        ClusterList list =  findClusterList(algorithmName);
        
        if(list == null){
            list = new ClusterList(algorithmName);
            this.addClusterList(list);
        } else if(list.isClusterSaved(clusterID)){
            JOptionPane pane = new JOptionPane("Cluster has already been saved.  Would you like to " +
            "replace the existing attributes?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
            pane.setVisible(true);
            int option = pane.getOptionType();
            if(option == JOptionPane.CANCEL_OPTION || option == JOptionPane.NO_OPTION)
                return null;
        }
        
        ClusterAttributesDialog dialog = new ClusterAttributesDialog("Store Cluster Attributes", algorithmName, clusterID);
        if(dialog.showModal() != JOptionPane.OK_OPTION){
            return null;
        }
        
        Color clusterColor = dialog.getColor();
        String clusterLabel = dialog.getLabel();
        String clusterDescription = dialog.getDescription();
        this.clusterSerialCounter++;
        Cluster cluster = new Cluster(indices, "Algorithm", clusterLabel, algorithmName, clusterID, clusterDescription, algorithmIndex, this.clusterSerialCounter, clusterColor, experiment);
        addCluster(list, cluster);
        
        return cluster;
    }
    
    /** Stores a clsuter given the supplied parameters.
     */
    public Cluster storeCluster(int algorithmIndex, String algorithmName, String clusterID, int [] indices, DefaultMutableTreeNode clusterNode, Experiment experiment){
        
        ClusterList list =  findClusterList(algorithmName);
        
        if(list == null){
            list = new ClusterList(algorithmName);
            this.addClusterList(list);
        } else if(list.isClusterSaved(clusterID)){            
            int option = JOptionPane.showConfirmDialog(framework.getFrame(), "Cluster has already been saved.  Would you like to " +
            "modify the existing attributes?", "Cluster Saved Alert", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 
            if(option == JOptionPane.NO_OPTION)
                return null;
        }
        
        ClusterAttributesDialog dialog = new ClusterAttributesDialog("Store Cluster Attributes", algorithmName, clusterID);
        if(dialog.showModal() != JOptionPane.OK_OPTION){
            return null;
        }
        
        Color clusterColor = dialog.getColor();
        String clusterLabel = dialog.getLabel();
        String clusterDescription = dialog.getDescription();
        this.clusterSerialCounter++;
        Cluster cluster = new Cluster(indices, "Algorithm", clusterLabel, algorithmName, clusterID, clusterDescription, algorithmIndex, this.clusterSerialCounter, clusterColor, clusterNode, experiment);
        addCluster(list, cluster);
        
        return cluster;
    }
    
    /** 
     * Stores a clsuter given the supplied parameters.
     */
    public Cluster storeSubCluster(int algorithmIndex, String algorithmName, String clusterID, int [] indices, DefaultMutableTreeNode clusterNode, Experiment experiment){
        
        ClusterList list =  findClusterList(algorithmName);
         boolean modification = false;
        
        if(list == null){
            list = new ClusterList(algorithmName);
            this.addClusterList(list);
        } else if(list.isClusterSaved(clusterID)){      
            if(list.getCluster(clusterID).doIndicesMatch(indices)){
                int option = JOptionPane.showConfirmDialog(framework.getFrame(), "Cluster has already been saved.  Would you like to " +
                "modify the existing attributes?", "Cluster Saved Alert", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                modification = true;
            if(option == JOptionPane.NO_OPTION)
                return null;
            }
        }
        
        ClusterAttributesDialog dialog = new ClusterAttributesDialog("Store Cluster Attributes", algorithmName, clusterID);
        if(dialog.showModal() != JOptionPane.OK_OPTION){
            return null;
        }
        
        Color clusterColor = dialog.getColor();
        String clusterLabel = dialog.getLabel();
        String clusterDescription = dialog.getDescription();
        this.clusterSerialCounter++;
        Cluster cluster = new Cluster(indices, "Algorithm", clusterLabel, algorithmName, clusterID, clusterDescription, algorithmIndex, this.clusterSerialCounter, clusterColor, clusterNode, experiment);
        if(modification)
            addCluster(list, cluster);
        else
            addSubCluster(list, cluster);
        
        return cluster;
    }
    
    /** Returns the ClusterList given an algorithm result name
     * (i.e. KMC(2))
     */
    private ClusterList findClusterList(String algName){
        ClusterList curr;
        for(int i = 0; i < size(); i++){
            curr = this.getClusterList(i);
            if(curr.getAlgorithmName() == algName)
                return curr;
        }
        return null;
    }
    
    /** Returns a specialized cluster list responsible for holding
     * results from cluster operations.
     */
    public ClusterList getClusterOperationsList(){
        return findClusterList("Cluster Ops.");
    }
    
    /** Remove  a cluster given supplied parameters
     */
    public void removeCluster(int [] indices, String algorithmName, String clusterID){
        ClusterList list = findClusterList(algorithmName);
        if(list == null || list.size() == 0)
            return;
        Cluster cluster = list.getCluster(clusterID);
        
        list.removeCluster(clusterID);
        removeElementClusters(indices, cluster);
    }
    
    /** Remove  a cluster given supplied parameters
     */
    public void removeSubCluster(int [] indices, String algorithmName, String clusterID){
        ClusterList list = findClusterList(algorithmName);
        if(list == null || list.size() == 0)
            return;
                
        Cluster cluster = null;
        Cluster temp = null;
        for(int i = 0; i < list.size(); i++){
            temp = list.getClusterAt(i);
            if(temp.doIndicesMatch(indices))
                cluster = temp;
        }                
        if(cluster == null)
            return;   
        
        list.removeCluster(cluster);
        removeElementClusters(indices, cluster);
    }
    
    
    /** Removes a cluster from elenets cluster lists.
     */
    private void removeElementClusters(int [] indices, Cluster cluster){
        for(int i = 0; i < indices.length; i++){
            this.elementClusters[indices[i]].remove(cluster);
        }
    }
    
    /** Alters the color of a specified cluster (cluster serial number)
     */
    public void updateClusterColor(int serialNumber, Color color){
        Cluster cluster = getCluster(serialNumber);
        if(cluster != null){
            cluster.setClusterColor(color);
        }
    }
    
    /** Returns the cluster specified by the provided
     * serial number.
     */
    public Cluster getCluster(int serialNumber){
        Cluster cluster = null;
        ClusterList list = null;
        for(int i = 0; i < size(); i++){
            list = this.getClusterList(i);
            for(int j = 0; j < list.size(); j++){
                cluster = list.getClusterAt(j);
                if(serialNumber == cluster.getSerialNumber())
                    return cluster;
            }
        }
        return cluster;
    }
    
    /** Removes cluster specified by the serial number
     */
    public void removeCluster(int serialNumber){
        Cluster cluster = getCluster(serialNumber);
        ClusterList list;
        for(int i = 0; i < size(); i++){
            list = this.getClusterList(i);
            for(int j = 0; j < list.size(); j++){
                if(list.getClusterAt(j) == cluster){
                    list.removeCluster(serialNumber);
                }
            }
        }
        int [] indices = cluster.getIndices();
        removeClusterMembership(cluster);
    }
    
    /** Returns the next availible cluster serial
     * number and reserves it's use.
     */
    public int takeNextClusterSerialNumber(){
        this.clusterSerialCounter++;
        return clusterSerialCounter;
    }
    
    /**
     *  Returns the value of the maximum serial number
     */
    public int getMaxClusterSerialNumber(){
        return clusterSerialCounter;
    }
    
    /**
     *  Sets the cluster serial number, next reserved number is value + 1
     */
    public void setClusterSerialCounter(int value){
        this.clusterSerialCounter = value;
    }
    
    /** Prints out a summary of the repository.
     */
    public void printRepository(){
    }
    
    /** Saves the specified cluster
     */
    public void saveCluster(int serialNumber){
        Cluster cluster = getCluster(serialNumber);
        try{
            if(geneClusterRepository)
                ExperimentUtil.saveGeneCluster(framework.getFrame(), framework.getData(), cluster.getIndices());
            else
                ExperimentUtil.saveExperimentCluster(framework.getFrame(), cluster.getExperiment(), framework.getData(), cluster.getIndices());
        } catch (Exception e){
            JOptionPane.showMessageDialog(framework.getFrame(), "Error saving cluster.  Cluster not saved.", "Save Error", JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        }
    }
    
}

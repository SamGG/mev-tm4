/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ClusterRepository.java,v $
 * $Revision: 1.10 $
 * $Date: 2004-07-27 19:58:13 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.clusterUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Enumeration;
import java.util.Vector;
import java.awt.Color;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

import org.tigr.microarray.mev.cluster.clusterUtil.submit.SubmissionManager;

/** The ClusterRepository contains ClusterList objects created for
 * holding saved clusters particular analysis results.
 */
public class ClusterRepository extends Vector implements java.io.Serializable {
    public static final long serialVersionUID = 1000102010203030001L;
    
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
    
    /** IFramework implementation
     */
    private IFramework framework;
    
    /** Creates new ClusterRepository with a specified element count
     */
    public ClusterRepository(int numberOfElements, IFramework framework) {
        this.numberOfElements = numberOfElements;
        this.framework = framework;
        this.elementClusters = new ClusterList[numberOfElements];
        this.addClusterList(new ClusterList("Cluster Ops."));
    }
    
    /** Creates new ClusterRepository with specified cluster type*/
    public ClusterRepository(int numberOfElements, IFramework framework, boolean isGeneClusterRepository) {
        this.numberOfElements = numberOfElements;
        this.framework = framework;
        this.elementClusters = new ClusterList[numberOfElements];
        this.geneClusterRepository = isGeneClusterRepository;
        this.addClusterList(new ClusterList("Cluster Ops."));
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeBoolean(this.geneClusterRepository);
        oos.writeInt(this.clusterSerialCounter);
        oos.writeObject(this.elementClusters);
        oos.writeInt(this.numberOfElements);
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.geneClusterRepository = ois.readBoolean();
        this.clusterSerialCounter = ois.readInt();
        this.elementClusters = (ClusterList [])ois.readObject();
        this.numberOfElements = ois.readInt();
    }
    
    /**
     *  Sets the repository's framework field
     */
    public void setFramework(IFramework framework) {
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
        if(!((cluster.getSource()).equals("Cluster Op.")) && list.isClusterSaved(cluster.getClusterID(), cluster.getIndices())){
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
            moveClusterToEndInMembershipLists(savedCluster);
        } else {
            if(list == null){ // null cluster list for cluster operations. make one
                list = new ClusterList("Cluster Ops.");
                this.addClusterList(list);
            }
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
    
    /** Moves a cluster to the rear of the elementCluster lists ClusterList
     * (This is so that getColor() commands for gene color get the latest color
     * assigned rather than the color of the last cluster assigned.  This is to handle
     * modification of an existing cluster's color attribute.)
     */
    private void moveClusterToEndInMembershipLists(Cluster cluster){
        int [] indices = cluster.getIndices();
        for(int i = 0; i < indices.length; i++){
            if(elementClusters[indices[i]] == null)
                elementClusters[indices[i]] = new ClusterList("element "+indices[i]);
            //if it's in the list (and is should always be) remove it and put it back in
            //at the end.
            if(elementClusters[indices[i]].contains(cluster)){
                elementClusters[indices[i]].removeElement(cluster);
                elementClusters[indices[i]].addElement(cluster);
            }
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
        if(this.isGeneClusterRepository())
            framework.getData().deleteColors();
        else
            framework.getData().deleteExperimentColors();
    }
    
    /** Clears the cluster lists for all elements.
     * Postcondition is that all elements do not have
     * references to any clusters.
     */
    private void clearElementClusters(){
        for(int i = 0; i < numberOfElements; i++)
            this.elementClusters[i] = null;
    }
    
    public boolean isEmpty(){
        for(int i = 0; i < size(); i++)
            if(this.getClusterList(i).size() > 0)
                return false;
        return true;
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
        } else if(list.isClusterSaved(clusterID, indices)){
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
        } else if(list.isClusterSaved(clusterID, indices)){
            int option = JOptionPane.showConfirmDialog(new java.awt.Frame(), "Cluster has already been saved.  Would you like to " +
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
        } else if(list.isClusterSaved(clusterID, indices)){
            if(list.getCluster(clusterID).doIndicesMatch(indices)){
                int option = JOptionPane.showConfirmDialog(new java.awt.Frame(), "Cluster has already been saved.  Would you like to " +
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
    public boolean removeCluster(int [] indices, String algorithmName, String clusterID){
        ClusterList list = findClusterList(algorithmName);
        if(list == null || list.size() == 0)
            return false;
        Cluster cluster = list.getCluster(clusterID);
        if(cluster != null){
            int serialNumber = cluster.getSerialNumber();
            if(this.isGeneClusterRepository()){
                framework.getData().setProbesColor(indices, null);
                framework.addHistory("Remove Gene Cluster From Repository: Serial # "+String.valueOf(serialNumber));
            } else {
                framework.getData().setExperimentColor(indices, null);
                framework.addHistory("Remove Experiment Cluster From Repository: Serial # "+String.valueOf(serialNumber));
            }
        } else {
            return false;
        }
        list.removeCluster(clusterID);
        removeElementClusters(indices, cluster);
        return true;
    }
    
    /** Remove  a cluster given supplied parameters
     */
    public boolean removeSubCluster(int [] indices, String algorithmName, String clusterID){
        ClusterList list = findClusterList(algorithmName);
        if(list == null || list.size() == 0)
            return false;
        
        Cluster cluster = null;
        Cluster temp = null;
        for(int i = 0; i < list.size(); i++){
            temp = list.getClusterAt(i);
            if(temp.doIndicesMatch(indices))
                cluster = temp;
        }
        if(cluster == null)
            return false;
        
        int serialNumber = cluster.getSerialNumber();
        if(this.isGeneClusterRepository()){
            framework.getData().setProbesColor(indices, null);
            framework.addHistory("Remove Gene Cluster From Repository: Serial # "+String.valueOf(serialNumber));
        } else {
            framework.getData().setExperimentColor(indices, null);
            framework.addHistory("Remove Experiment Cluster From Repository: Serial # "+String.valueOf(serialNumber));
        }
        
        list.removeCluster(cluster);
        removeElementClusters(indices, cluster);
        return true;
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
    public boolean removeCluster(int serialNumber){
        Cluster cluster = getCluster(serialNumber);
        if(cluster == null)
            return false;
        
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
        
        if(this.isGeneClusterRepository()){
            framework.getData().setProbesColor(indices, null);
            framework.addHistory("Remove Gene Cluster From Repository: Serial # "+String.valueOf(serialNumber));
        } else {
            framework.getData().setExperimentColor(indices, null);
            framework.addHistory("Remove Experiment Cluster From Repository: Serial # "+String.valueOf(serialNumber));
        }
        return true;
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
    
    /* Creates a cluster by importing a gene list
     */
    public Cluster createClusterFromList() {
        ListImportDialog dialog;
        String [] ids;
        String key;
        int [] newIndices;
        Experiment experiment = framework.getData().getExperiment();
        
        
        if(this.isGeneClusterRepository()) {
            dialog = new ListImportDialog(this.framework.getData().getFieldNames(), true);
            if(dialog.showModal() == JOptionPane.OK_OPTION) {
                key = dialog.getFieldName();
                ids = dialog.getList();
                
 
                newIndices = getMatchingIndices(experiment, key, ids, true);
                                
           
                if(newIndices != null && newIndices.length > 0) {                                                           
                    ClusterAttributesDialog clusterDialog = new ClusterAttributesDialog("Store Cluster Attributes", "List Import", "Gene List");
                    if(clusterDialog.showModal() != JOptionPane.OK_OPTION){
                        return null;
                    }

                    ClusterList list = getClusterOperationsList();
                    Color clusterColor = clusterDialog.getColor();
                    String clusterLabel = clusterDialog.getLabel();
                    String clusterDescription = clusterDialog.getDescription();
                    this.clusterSerialCounter++;
                    Cluster cluster = new Cluster(newIndices, "List Import", clusterLabel, "List Import", "N/A", clusterDescription, list.getAlgorithmIndex(), this.clusterSerialCounter, clusterColor, experiment);                    
                    
                    addCluster(list, cluster);           
                    return cluster;
                }
            }
        } else{
            ISlideData slide = this.framework.getData().getFeature(0);
            if(slide == null)
                return null;
            
            Vector slideNameKeys = slide.getSlideDataKeys();
            String [] slideNames = new String[slideNameKeys.size()];
            for(int i = 0; i < slideNames.length; i++) {
                slideNames[i] = (String)(slideNameKeys.elementAt(i));
            }
            
            dialog = new ListImportDialog(slideNames, false);
            
            if(dialog.showModal() == JOptionPane.OK_OPTION) {
                key = dialog.getFieldName();
                ids = dialog.getList();
                newIndices = getMatchingIndices(experiment, key, ids, false);
                if(newIndices != null && newIndices.length > 0) {
                    ClusterAttributesDialog clusterDialog = new ClusterAttributesDialog("Store Cluster Attributes", "List Import", "Experiment List");
                    if(clusterDialog.showModal() != JOptionPane.OK_OPTION){
                        return null;
                    }

                    ClusterList list = getClusterOperationsList();
                    Color clusterColor = clusterDialog.getColor();
                    String clusterLabel = clusterDialog.getLabel();
                    String clusterDescription = clusterDialog.getDescription();
                    this.clusterSerialCounter++;
                    Cluster cluster = new Cluster(newIndices, "List Import", clusterLabel, "List Import", "N/A", clusterDescription, list.getAlgorithmIndex(), this.clusterSerialCounter, clusterColor, experiment);
                    
                    addCluster(list, cluster);   
                    return cluster;
                }
                
            }
            
        }
        return null;
    }
    
    private int [] getMatchingIndices(Experiment experiment, String key, String [] ids, boolean geneIndices) {
        int [] indices = null;
        int [] allIndices;
        String [] annList;
        Vector indicesVector = new Vector();
        IData data = framework.getData();
        
        if(geneIndices) {
            allIndices = experiment.getRowMappingArrayCopy();
            annList = framework.getData().getAnnotationList(key, allIndices);
            Vector idVector = new Vector(annList.length);
            for(int i = 0 ; i < ids.length; i++) {
                idVector.addElement(ids[i]);
            }
            for(int i = 0; i < annList.length; i++) {
                if(idVector.contains(annList[i]))
                    indicesVector.addElement(new Integer(allIndices[i]));
            }
            indices = new int[indicesVector.size()];
            for(int i = 0; i < indices.length; i++) {
                indices[i] = ((Integer)(indicesVector.elementAt(i))).intValue();
            }
            
        } else {
            allIndices = experiment.getColumnIndicesCopy();
            annList = new String[experiment.getNumberOfSamples()];
            data.setSampleLabelKey(key);
            
            Vector idVector = new Vector(annList.length);
            for(int i = 0; i < ids.length; i++) {
                idVector.addElement(ids[i]);
            }
            
            for(int i = 0; i < allIndices.length; i++) {
                annList[i] = data.getFullSampleName(i);
            }
            
            for(int i = 0; i < annList.length; i++) {
                if(idVector.contains(annList[i]))
                    indicesVector.addElement(new Integer(allIndices[i]));
            }
            
            indices = new int[indicesVector.size()];
            for(int i = 0; i < indices.length; i++) {
                indices[i] = ((Integer)(indicesVector.elementAt(i))).intValue();
            }
            
            
        }
        return indices;
    }
    
    public void submitCluster( Cluster cluster ) {
        SubmissionManager subManager = new SubmissionManager(this.framework, this);
        subManager.submit(cluster);
    }
    
}

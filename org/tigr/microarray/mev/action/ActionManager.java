/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ActionManager.java,v $
 * $Revision: 1.18 $
 * $Date: 2007-12-19 21:39:36 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.tigr.microarray.mev.cluster.gui.AnalysisDescription;
import org.tigr.microarray.mev.cluster.gui.IGUIFactory;

public class ActionManager implements java.io.Serializable {
    public static final long serialVersionUID = 100010201010001L;
    
    public static final String PARAMETER = "command-parameter";
    public static final String LARGE_ICON = "LargeIcon";
    public static final String CATEGORY="category";
    private HashMap actions = new HashMap();
    private ActionListener listener;
    
    /**
     * Constructs an <code>ActionManager</code> with specified
     * action listener, array of labels and gui factory.
     */
    public ActionManager(ActionListener listener, String[] labels, IGUIFactory factory) {
        this.listener = listener;
        initActions();
        initLabelActions(labels);
        initAnalysisActions(factory);
    }
    
    /**
     * Returns a wrapped action listaner.
     */
    public ActionListener getListener() {
        return listener;
    }
    
    /**
     * Rturns an action by its name.
     */
    public Action getAction(String name) {
    		return(Action)actions.get(name);
    }
    
    /**
     * Delegates this invokation to a wrapped action listener.
     */
    public void forwardAction(ActionEvent event) {
        listener.actionPerformed(event);
    }
    
    /**
     * Initializes main menu and toolbar actions.
     */
    private void initActions() {
    	actions.put(NEW_MULTIPLEARRAYVIEWER, new DefaultAction(this, NEW_MAV_NAME, NEW_MAV_COMMAND, getIcon(NEW_MAV_SMALLICON), getIcon(NEW_MAV_LARGEICON)));
        actions.put(LOAD_ACTION, new DefaultAction(this, LOAD_NAME, LOAD_COMMAND, getIcon(LOAD_FILE_SMALLICON), getIcon(LOAD_FILE_LARGEICON)));
        actions.put(LOAD_ANALYSIS_ACTION, new DefaultAction(this, LOAD_ANALYSIS_NAME, LOAD_ANALYSIS_COMMAND, getIcon(LOAD_ANALYSIS_SMALLICON), getIcon(LOAD_ANALYSIS_LARGEICON)));
        actions.put(SAVE_ANALYSIS_ACTION, new DefaultAction(this, SAVE_ANALYSIS_NAME, SAVE_ANALYSIS_COMMAND, getIcon(SAVE_ANALYSIS_SMALLICON), getIcon(SAVE_ANALYSIS_LARGEICON)));
        actions.put(SAVE_ANALYSIS_AS_ACTION, new DefaultAction(this, SAVE_ANALYSIS_AS_NAME, SAVE_ANALYSIS_AS_COMMAND, getIcon(SAVE_ANALYSIS_AS_SMALLICON), getIcon(SAVE_ANALYSIS_AS_LARGEICON)));
        actions.put(NEW_SCRIPT_ACTION, new DefaultAction(this, NEW_SCRIPT_NAME, NEW_SCRIPT_COMMAND, getIcon(NEW_SCRIPT_SMALLICON), getIcon(NEW_SCRIPT_LARGEICON)));
        actions.put(LOAD_SCRIPT_ACTION, new DefaultAction(this, LOAD_SCRIPT_NAME, LOAD_SCRIPT_COMMAND, getIcon(LOAD_SCRIPT_SMALLICON), getIcon(LOAD_SCRIPT_LARGEICON)));
        
        
        actions.put(LOAD_DIRECTORY_ACTION, new DefaultAction(this, LOAD_DIRECTORY_NAME, LOAD_DIRECTORY_COMMAND, getIcon(LOAD_DIRECTORY_SMALLICON), getIcon(LOAD_DIRECTORY_LARGEICON)));
        actions.put(LOAD_FILE_ACTION, new DefaultAction(this, LOAD_FILE_NAME, LOAD_FILE_COMMAND, getIcon(LOAD_FILE_SMALLICON), getIcon(LOAD_FILE_LARGEICON)));
        actions.put(LOAD_EXPRESSION_ACTION, new DefaultAction(this, LOAD_EXPRESSION_NAME, LOAD_EXPRESSION_COMMAND, getIcon(LOAD_EXPRESSION_SMALLICON), getIcon(LOAD_EXPRESSION_LARGEICON)));
        actions.put(LOAD_DB_ACTION, new DefaultAction(this, LOAD_DB_NAME, LOAD_DB_COMMAND, getIcon(LOAD_DB_SMALLICON), getIcon(LOAD_DB_LARGEICON)));
        actions.put(LOAD_STANFORD_ACTION, new DefaultAction(this, LOAD_STANFORD_NAME, LOAD_STANFORD_COMMAND, getIcon(LOAD_STANFORD_ICON)));
        actions.put(LOAD_CLUSTER_ACTION, new DefaultAction(this, LOAD_CLUSTER_NAME, LOAD_CLUSTER_COMMAND, getIcon(LOAD_CLUSTER_ICON)));
        actions.put(SAVE_MATRIX_ACTION, new DefaultAction(this, SAVE_MATRIX_NAME, SAVE_MATRIX_COMMAND, getIcon(SAVE_MATRIX_ICON)));
        actions.put(SAVE_IMAGE_ACTION, new DefaultAction(this, SAVE_IMAGE_NAME, SAVE_IMAGE_COMMAND, getIcon(SAVE_IMAGE_SMALLICON), getIcon(SAVE_IMAGE_LARGEICON)));
        actions.put(PRINT_IMAGE_ACTION, new DefaultAction(this, PRINT_IMAGE_NAME, PRINT_IMAGE_COMMAND, getIcon(PRINT_IMAGE_SMALLICON), getIcon(PRINT_IMAGE_LARGEICON)));
        actions.put(CLOSE_ACTION, new DefaultAction(this, CLOSE_NAME, CLOSE_COMMAND, getIcon(CLOSE_ICON)));
        actions.put(DELETE_ALL_ACTION, new DefaultAction(this, DELETE_ALL_NAME, DELETE_ALL_COMMAND, getIcon(DELETE_ALL_ICON)));
        actions.put(DELETE_ALL_EXPERIMENT_CLUSTERS_ACTION, new DefaultAction(this, DELETE_ALL_EXPERIMENT_CLUSTERS_NAME, DELETE_ALL_EXPERIMENT_CLUSTERS_COMMAND, getIcon(DELETE_ALL_ICON)));
        actions.put(SEARCH_ACTION, new DefaultAction(this, SEARCH_NAME, SEARCH_COMMAND, getIcon(SEARCH_ICON)));
        actions.put(IMPORT_GENE_LIST_ACTION, new DefaultAction(this, IMPORT_GENE_LIST_NAME, IMPORT_GENE_LIST_COMMAND, getIcon(IMPORT_GENE_LIST_ICON)));
        actions.put(IMPORT_SAMPLE_LIST_ACTION, new DefaultAction(this, IMPORT_SAMPLE_LIST_NAME, IMPORT_SAMPLE_LIST_COMMAND, getIcon(IMPORT_SAMPLE_LIST_ICON)));

        actions.put(APPEND_SAMPLE_ANNOTATION_ACTION, new DefaultAction(this, APPEND_SAMPLE_ANNOTATION_NAME, APPEND_SAMPLE_ANNOTATION_COMMAND, getIcon(APPEND_SAMPLE_ANNOTATION_ICON)));            
        actions.put(APPEND_GENE_ANNOTATION_ACTION, new DefaultAction(this, APPEND_GENE_ANNOTATION_NAME, APPEND_GENE_ANNOTATION_COMMAND, getIcon(APPEND_GENE_ANNOTATION_ICON)));            
        actions.put(CDNA_LOW_INTENSITY_ACTION, new DefaultAction(this, CDNA_LOW_INTENSITY_NAME, CDNA_LOW_INTENSITY_CMD, getIcon(CDNA_LOW_INTENSITY_ICON)));
        actions.put(OLIGEN_LOW_INTENSITY_ACTION, new DefaultAction(this, OLIGEN_LOW_INTENSITY_NAME, OLIGEN_LOW_INTENSITY_CMD, getIcon(OLIGEN_LOW_INTENSITY_ICON)));

       
        /* Raktim - Annotation Demo Only */
        actions.put(GENOME_ANNOTATION_ACTION, new DefaultAction(this, GENOME_ANNOTATION_NAME, GENOME_ANNOTATION_COMMAND, getIcon(GENOME_ANNOTATION_ICON)));
        /*

     
        
        /*
         * Raktim Sept 29, 05
         * CGH Actions
         */
        actions.put(LOAD_SAMPLE_LIST_ACTION, new DefaultAction(this, LOAD_SAMPLE_LIST_NAME, LOAD_SAMPLE_LIST_ACTION, getIcon("TreeInfoLeaf.gif")));
        actions.put(LOAD_WSL_ACTION, new DefaultAction(this, LOAD_WSL_NAME, LOAD_WSL_ACTION, getIcon(LOAD_WSL_SMALLICON)));
        actions.put(LOAD_CLONE_DISTRIBUTIONS_ACTION, new DefaultAction(this, LOAD_CLONE_DISTRIBUTIONS_NAME, LOAD_CLONE_DISTRIBUTIONS_ACTION, getIcon("p.gif")));
        actions.put(LOAD_CLONE_DISTRIBUTIONS_FROM_FILE_ACTION, new DefaultAction(this, LOAD_CLONE_DISTRIBUTIONS_FROM_FILE_NAME, LOAD_CLONE_DISTRIBUTIONS_FROM_FILE_ACTION, getIcon("p.gif")));
   
    
    
    
    }

    
    /**
     * Initializes 'display/label' menu actions.
     */
    public void initLabelActions(String[] labels) {
        DefaultAction action = new DefaultAction(this, "No Label", DISPLAY_LABEL_CMD);
        action.putValue(PARAMETER, String.valueOf(-1));
        actions.put(DISPLAY_LABEL_ACTION+String.valueOf(-1), action);
        
        if(labels == null)
            return;
        
        for (int i=0; i<labels.length; i++) {
            action = new DefaultAction(this, "Label by "+labels[i], DISPLAY_LABEL_CMD);
            action.putValue(PARAMETER, String.valueOf(i));
            actions.put(DISPLAY_LABEL_ACTION+String.valueOf(i), action);
        }
    }
    
    /**
     * Initializes analysis actions, using specified <code>IGUIFactory</code>.
     * @see IGUIFactory
     */
    private void initAnalysisActions(IGUIFactory factory) {
        if (factory == null) {
            return;
        }
        AnalysisDescription[] descs = factory.getAnalysisDescriptions();
        if (descs == null) {
            return;
        }
        int counter = 0;
        for (int i=0; i<descs.length; i++) {
            if (isValidDescription(descs[i])) {
            	actions.put(ANALYSIS_ACTION+String.valueOf(i), new AnalysisAction(this, descs[i]));
            	counter++;
            }
        }
    }
    /**
     * Raktim
     * Initializes The CGH Copy Number based analysis actions;
     * @param factory
     */
    public void initCghAnalysiActions(IGUIFactory factory){
	        if (factory == null) {
		    return;
		}
		AnalysisDescription[] descs = factory.getAnalysisDescriptions();
		if (descs == null) {
		    return;
		}
		int counter = 0;
		for (int i=0; i<descs.length; i++) {
		    if (isValidDescription(descs[i])) {
			actions.put(CGH_ANALYSIS_ACTION+String.valueOf(i), new AnalysisAction(this, descs[i]));
			counter++;
		    }
		}
    }
    
    /**
     * Checkes if specified <code>AnalysisDescription</code> is valid.
     */
    private boolean isValidDescription(AnalysisDescription desc) {
        return desc != null && desc.getName() != null && desc.getClassName() != null;
    }
    

    /**
     * Creates an image by specified name.
     */
    private ImageIcon getIcon(String name) {
        URL url = getClass().getResource("/org/tigr/images/"+name);
        if (url == null)
            return null;
        return new ImageIcon(url);
    }
    
    // analysis action(s)
    public static final String ANALYSIS_ACTION = "analysis-action";
    public static final String ANALYSIS_COMMAND = "analysis-command";
    
    //launch a new customized MAV
    public static final String NEW_MULTIPLEARRAYVIEWER  = "new-mav-load";
    public static final String  NEW_MAV_COMMAND = "newmav-command-load";
    public static final String  NEW_MAV_NAME    = "Customize Toolbar";
    private static final String NEW_MAV_SMALLICON = "addmultiple16.gif";
    private static final String NEW_MAV_LARGEICON = "addmultiple.gif";
    //load data action
    public static final String  LOAD_ACTION  = "action-load";
    public static final String  LOAD_COMMAND = "command-load";
    public static final String  LOAD_NAME    = "Load Data";
    private static final String LOAD_SMALLICON = "addmultiple16.gif";
    private static final String LOAD_LARGEICON = "addmultiple.gif";
    
    //load analysis action
    public static final String  LOAD_ANALYSIS_ACTION  = "action-load-analysis";
    public static final String  LOAD_ANALYSIS_COMMAND = "command-load-analysis";
    public static final String  LOAD_ANALYSIS_NAME    = "Open Analysis...";
    private static final String LOAD_ANALYSIS_SMALLICON = "addmultiple16.gif";
    private static final String LOAD_ANALYSIS_LARGEICON = "addmultiple.gif";
    
    //save analysis action
    public static final String  SAVE_ANALYSIS_ACTION  = "action-save-analysis";
    public static final String  SAVE_ANALYSIS_COMMAND = "command-save-analysis";
    public static final String  SAVE_ANALYSIS_NAME    = "Save Analysis";
    private static final String SAVE_ANALYSIS_SMALLICON = "save16.gif";
    private static final String SAVE_ANALYSIS_LARGEICON = "save16.gif";
    
    //save analysis action
    public static final String  SAVE_ANALYSIS_AS_ACTION  = "action-save-analysis-as";
    public static final String  SAVE_ANALYSIS_AS_COMMAND = "command-save-analysis-as";
    public static final String  SAVE_ANALYSIS_AS_NAME    = "Save Analysis As...";
    private static final String SAVE_ANALYSIS_AS_SMALLICON = "save16.gif";
    private static final String SAVE_ANALYSIS_AS_LARGEICON = "save16.gif";
    
    
    public static final String  NEW_SCRIPT_ACTION  = "action-new-script";
    public static final String  NEW_SCRIPT_COMMAND = "command-new-script";
    public static final String  NEW_SCRIPT_NAME    = "New Script";
    private static final String NEW_SCRIPT_SMALLICON = "newScript16.gif";
    private static final String NEW_SCRIPT_LARGEICON = "newScript16.gif";
    
    
    public static final String  LOAD_SCRIPT_ACTION  = "action-load-script";
    public static final String  LOAD_SCRIPT_COMMAND = "command-load-script";
    public static final String  LOAD_SCRIPT_NAME    = "Load Script";
    private static final String LOAD_SCRIPT_SMALLICON = "loadScript16.gif";
    private static final String LOAD_SCRIPT_LARGEICON = "loadScript16.gif";
    
    
    // load directory action
    public static final String  LOAD_DIRECTORY_ACTION  = "action-load-directory";
    public static final String  LOAD_DIRECTORY_COMMAND = "command-load-directory";
    public static final String  LOAD_DIRECTORY_NAME    = "Add Samples from Directory";
    private static final String LOAD_DIRECTORY_SMALLICON = "addmultiple16.gif";
    private static final String LOAD_DIRECTORY_LARGEICON = "addmultiple.gif";
    // load file action
    public static final String  LOAD_FILE_ACTION  = "action-load-file";
    public static final String  LOAD_FILE_COMMAND = "command-load-file";
    public static final String  LOAD_FILE_NAME    = "Add Sample from File";
    private static final String LOAD_FILE_SMALLICON    = "addfromfile16.gif";
    private static final String LOAD_FILE_LARGEICON    = "addfromfile.gif";
    
    // pcahan
    
    // load affy directory action
    public static final String  LOAD_AFFY_DIRECTORY_ACTION  = "action-affy-load-directory";
    public static final String  LOAD_AFFY_DIRECTORY_COMMAND = "command-affy-load-directory";
    public static final String  LOAD_AFFY_DIRECTORY_NAME    = "Add Affymetrix Chips from Directory";
    private static final String LOAD_AFFY_DIRECTORY_SMALLICON = "addmultiple16.gif";
    private static final String LOAD_AFFY_DIRECTORY_LARGEICON = "addmultiple.gif";
    
    // load Affy action
    public static final String  LOAD_AFFY_ACTION  = "action-load-affy";
    public static final String  LOAD_AFFY_COMMAND = "command-load-affy";
    public static final String  LOAD_AFFY_NAME    = "Add Sample from Affymetrix datafile";
    private static final String LOAD_AFFY_SMALLICON    = "addfromfile16.gif";
    private static final String LOAD_AFFY_LARGEICON    = "addfromfile.gif";
    
    
    // load expression action
    public static final String  LOAD_EXPRESSION_ACTION  = "action-load-expression";
    public static final String  LOAD_EXPRESSION_COMMAND = "command-load-expression";
    public static final String  LOAD_EXPRESSION_NAME    = "Add Sample from Expression File";
    private static final String LOAD_EXPRESSION_SMALLICON    = "addfromfile16.gif";
    private static final String LOAD_EXPRESSION_LARGEICON    = "addfromfile.gif";
    // load db action
    public static final String  LOAD_DB_ACTION  = "action-load-db";
    public static final String  LOAD_DB_COMMAND = "command-load-db";
    public static final String  LOAD_DB_NAME    = "Add Samples from DB";
    private static final String LOAD_DB_SMALLICON    = "addfromdb16.gif";
    private static final String LOAD_DB_LARGEICON    = "addfromdb.gif";
    // load stanford action
    public static final String  LOAD_STANFORD_ACTION  = "action-load-stanford";
    public static final String  LOAD_STANFORD_COMMAND = "command-load-stanford";
    private static final String LOAD_STANFORD_NAME    = "Add Samples from Stanford File";
    private static final String LOAD_STANFORD_ICON    = "addmultiple16.gif";
    // load cluster action
    public static final String  LOAD_CLUSTER_ACTION  = "action-load-cluster";
    public static final String  LOAD_CLUSTER_COMMAND = "command-load-cluster";
    private static final String LOAD_CLUSTER_NAME    = "Load Cluster";
    private static final String LOAD_CLUSTER_ICON    = "addfromfile16.gif";
    // save matrix action
    public static final String  SAVE_MATRIX_ACTION  = "action-save-matrix";
    public static final String  SAVE_MATRIX_COMMAND = "command-save-matrix";
    private static final String SAVE_MATRIX_NAME    = "Save Matrix";
    private static final String SAVE_MATRIX_ICON    = "expression.gif";
    // save image action
    public static final String  SAVE_IMAGE_ACTION  = "action-save-image";
    public static final String  SAVE_IMAGE_COMMAND = "command-save-image";
    public static final String  SAVE_IMAGE_NAME    = "Save Image";
    private static final String SAVE_IMAGE_SMALLICON    = "saveimage16.gif";
    private static final String SAVE_IMAGE_LARGEICON    = "saveimage.gif";
    // print image action
    public static final String  PRINT_IMAGE_ACTION  = "action-print-image";
    public static final String  PRINT_IMAGE_COMMAND = "command-print-image";
    private static final String PRINT_IMAGE_NAME    = "Print Image";
    private static final String PRINT_IMAGE_SMALLICON = "printimage16.gif";
    private static final String PRINT_IMAGE_LARGEICON = "printimage.gif";
    // close action
    public static final String  CLOSE_ACTION  = "action-close";
    public static final String  CLOSE_COMMAND = "command-close";
    private static final String CLOSE_NAME    = "Close";
    private static final String CLOSE_ICON    = "close16.gif";

  // delete all gene clusters action
    public static final String  DELETE_ALL_ACTION  = "delete-all-action";
    public static final String  DELETE_ALL_COMMAND = "delete-all-cmd";
    private static final String DELETE_ALL_NAME    = "Delete All Gene Clusters";
    private static final String DELETE_ALL_ICON    = "Delete16.gif";
    // delete all experiment clusters action
    public static final String  DELETE_ALL_EXPERIMENT_CLUSTERS_ACTION  = "delete-all-experiments-action";
    public static final String  DELETE_ALL_EXPERIMENT_CLUSTERS_COMMAND = "delete-all-experiments-cmd";
    private static final String DELETE_ALL_EXPERIMENT_CLUSTERS_NAME    = "Delete All Sample Clusters";
    // abbr. expt. names toggle
    public static final String TOGGLE_ABBR_EXPT_NAMES_ACTION = "toggle-abbr-expt-names-action";
    public static final String TOGGLE_ABBR_EXPT_NAMES_CMD = "toggle-abbr-expt-names-cmd";
    public static final String TOGGLE_ABBR_EXPT_NAMES_NAME = "Abbr. Sample Names";
    // display label actions
    public static final String DISPLAY_LABEL_ACTION = "display--label-action";
    public static final String DISPLAY_LABEL_CMD    = "display-label-cmd";
    // display experiment label actions
    public static final String ADD_NEW_EXPERIMENT_LABEL_ACTION = "add-new-experiment-action";
    public static final String ADD_NEW_EXPERIMENT_LABEL_CMD = "add-new-experiment-label";
    public static final String DISPLAY_EXPERIMENT_LABEL_ACTION = "display-experiment_label-action";
    public static final String DISPLAY_EXPERIMENT_LABEL_CMD    = "display-experiment-label-cmd";
    // sort label actions
    public static final String SORT_LABEL_ACTION = "sort-label-action";
    public static final String SORT_LABEL_CMD    = "sort-label-cmd";
   
    //wwang add for low intensity filter(cdna and oligne)
    public static final String CDNA_LOW_INTENSITY_ACTION="cdna-low-intensity-action";
    public static final String CDNA_LOW_INTENSITY_NAME = "two color microarray";
    public static final String CDNA_LOW_INTENSITY_ICON = "empty16.gif";
    public static final String CDNA_LOW_INTENSITY_CMD="cdna-low-intensity-cmd";
    
    public static final String OLIGEN_LOW_INTENSITY_ACTION="oligen-low-intensity-action";
    public static final String OLIGEN_LOW_INTENSITY_NAME = "one color microarray";
    public static final String OLIGEN_LOW_INTENSITY_ICON = "empty16.gif";
    public static final String OLIGEN_LOW_INTENSITY_CMD="oligen-low-intensity-cmd";
    // pcahan
    public static final String SET_DETECTION_FILTER_CMD = "set-detection-filter-cmd";
    public static final String USE_DETECTION_FILTER_CMD = "use-detection-filter-cmd";
    
    public static final String SET_FOLD_FILTER_CMD = "set-fold-filter-cmd";
    public static final String USE_FOLD_FILTER_CMD = "use-fold-filter-cmd";
    
    // adjust data commands
    public static final String LOG2_TRANSFORM_CMD  = "log2-transform-cmd";
    //wwang
    public static final String UNLOG2_TRANSFORM_CMD  = "unlog2-transform-cmd";
    public static final String NORMALIZE_SPOTS_CMD = "normalize-spots-cmd";
    public static final String DIVIDE_SPOTS_RMS_CMD = "divide-spots-rms-cmd";
    public static final String DIVIDE_SPOTS_SD_CMD = "divide-spots-sd-cmd";
    public static final String MEAN_CENTER_SPOTS_CMD = "mean-center-spots-cmd";
    public static final String MEDIAN_CENTER_SPOTS_CMD = "median-center-spots-cmd";
    public static final String DIGITAL_SPOTS_CMD = "digital-spots-cmd";
    public static final String NORMALIZE_EXPERIMENTS_CMD = "normalize-experiments-cmd";
    public static final String DIVIDE_EXPERIMENTS_RMS_CMD = "divide-experiments-rms-cmd";
    public static final String DIVIDE_EXPERIMENTS_SD_CMD = "divide-experiments-sd-cmd";
    public static final String MEAN_CENTER_EXPERIMENTS_CMD = "mean-center-experiments-cmd";
    public static final String MEDIAN_CENTER_EXPERIMENTS_CMD = "median-center-experiments-cmd";
    public static final String DIGITAL_EXPERIMENTS_CMD = "digital-experiments-cmd";
    public static final String LOG10_TO_LOG2_CMD = "log10-to-log2-cmd";
    public static final String LOG2_TO_LOG10_CMD = "log2-to-log10-cmd";
    public static final String USE_LOWER_CUTOFFS_CMD = "use-lower-cutoffs-cmd";
    //add for mas5
    public static final String USE_PRESENT_CALL_CMD = "use-present-call-cmd";
    //add for GCOS
    public static final String USE_GCOS_PERCENTAGE_CUTOFF_CMD = "use-gcos-percentage-cutoff-cmd";
    //add for pvalue filter
    public static final String USE_PVALUE_CUTOFF_CMD = "use-pvalue-percentage-cutoff-cmd";
    //add for Genepix flags filter
    public static final String USE_GENEPIXFLAGS_CMD = "use-genepixflags-cmd";
    public static final String USE_PERCENTAGE_CUTOFFS_CMD = "use-percentage-cutoffs-cmd";
    public static final String USE_VARIANCE_FILTER_CMD = "use-variance-filter-cmd";
    public static final String ADJUST_INTENSITIES_0_CMD = "adjust-intensities-0-cmd";
    //vu 7.22.05
	public static final String RAMA_CMD = "rama-cmd";
	public static final String RAMA_DOC_CMD = "rama-doc-cmd";

    // pcahan
    public static final String DIVIDE_GENES_MEDIAN_CMD = "divide-genes-median-cmd";
    public static final String UNDIVIDE_GENES_MEDIAN_CMD = "undivide-genes-median-cmd";
    
    public static final String DIVIDE_GENES_MEAN_CMD = "divide-genes-mean-cmd";
    public static final String UNDIVIDE_GENES_MEAN_CMD = "undivide-genes-mean-cmd";
    
    
    // normalization commands
    public static final String TOTAL_INTENSITY_CMD        = "total-intensity-cmd";
    public static final String LINEAR_REGRESSION_CMD      = "linear-regression-cmd";
    public static final String RATIO_STATISTICS_CMD       = "ratio-statistics-cmd";
    public static final String ITERATIVE_LOG_CMD          = "iterative-log-cmd";
    public static final String TOTAL_INTENSITY_LIST_CMD   = "total-intensity-list-cmd";
    public static final String LINEAR_REGRESSION_LIST_CMD = "linear-regression-list-cmd";
    public static final String RATIO_STATISTICS_LIST_CMD  = "ratio-statistics-list-cmd";
    public static final String ITERATIVE_LOG_LIST_CMD     = "iterative-log-list-cmd";
    public static final String NO_NORMALIZATION_CMD       = "no-normalization-cmd";
    // distance commands
    public static final String DEFAULT_DISTANCE_CMD = "default-distance-cmd";
    public static final String PEARSON_CORRELATION_CMD = "pearson-correlation-cmd";
    public static final String PEARSON_UNCENTERED_CMD = "pearson-uncentered-cmd";
    public static final String PEARSON_SQUARED_CMD = "pearson-squared-cmd";
    public static final String COSINE_CORRELATION_CMD = "cosine-correlation-cmd";
    public static final String COVARIANCE_VALUE_CMD = "covariance-value-cmd";
    public static final String EUCLIDEAN_DISTANCE_CMD = "euclidean-distance-cmd";
    public static final String AVERAGE_DOT_PRODUCT_CMD = "average-dot-product-cmd";
    public static final String MANHATTAN_DISTANCE_CMD = "manhattan-distance-cmd";
    public static final String MUTUAL_INFORMATION_CMD = "mutual-information-cmd";
    public static final String SPEARMAN_RANK_CORRELATION_CMD = "spearman-rank-correlation-cmd";
    public static final String KENDALLS_TAU_CMD = "kendalls-tau-cmd";
    public static final String ABSOLUTE_DISTANCE_CMD = "absolute-distance-cmd";
    // display commands\
    //Added by Sarita
    public static final String ACCESSIBLE_COLOR_SCHEME_CMD = "display-accessible-color-scheme-cmd";
    public static final String GREEN_RED_COLOR_SCHEME_CMD = "display-green-red-scheme-cmd";
    public static final String BLUE_YELLOW_COLOR_SCHEME_CMD = "display-blue-yellow-scheme-cmd";
    public static final String RAINBOW_COLOR_SCHEME_CMD = "display-rainbow-scheme-cmd";
    public static final String CUSTOM_COLOR_SCHEME_CMD = "display-custom-color-scheme-cmd";
    public static final String COLOR_GRADIENT_CMD = "display-color-gradient-cmd";
    public static final String DISPLAY_DRAW_BORDERS_CMD = "display-draw-borders-cmd";
    public static final String DISPLAY_SET_RATIO_SCALE_CMD = "display-set-ratio-scale-cmd";
    public static final String DISPLAY_5X2_CMD = "display-5x2-cmd";
    public static final String DISPLAY_10X10_CMD = "display-10x10-cmd";
    public static final String DISPLAY_20X5_CMD = "display-20x5-cmd";
    public static final String DISPLAY_50X10_CMD = "display-50x10-cmd";
    public static final String DISPLAY_OTHER_CMD = "display-other-cmd";
    public static final String SYSTEM_INFO_CMD = "system-info-cmd";
    public static final String DEFAULT_DISTANCES_CMD = "default-distances-cmd";
    // popup commands
    public static final String DELETE_NODE_CMD = "delete-node-cmd";

    public static final String  SEARCH_ACTION  = "search-action";
    public static final String  SEARCH_COMMAND = "search-cmd";
    private static final String SEARCH_NAME    = "Search";
    private static final String SEARCH_ICON    = "search_16.gif";
    
    public static final String IMPORT_GENE_LIST_COMMAND = "import-gene-list-command";
    public static final String IMPORT_GENE_LIST_ACTION = "import-gene-list-action";
    public static final String IMPORT_GENE_LIST_NAME = "Import Gene List";
    public static final String IMPORT_GENE_LIST_ICON = "import_list.gif";
    
    public static final String IMPORT_SAMPLE_LIST_COMMAND = "import-sample-list-command";
    public static final String IMPORT_SAMPLE_LIST_ACTION = "import-sample-list-action";
    public static final String IMPORT_SAMPLE_LIST_NAME = "Import Sample List";
    public static final String IMPORT_SAMPLE_LIST_ICON = "import_list.gif";
    
    public static final String SET_DATA_SOURCE_COMMAND = "set-data-source-command";
    
    public static final String APPEND_SAMPLE_ANNOTATION_COMMAND = "append-sample-annotation-command";
    public static final String APPEND_SAMPLE_ANNOTATION_ACTION = "append-sample-annotation-action";
    public static final String APPEND_SAMPLE_ANNOTATION_NAME = "Append Sample Annotation";
    public static final String APPEND_SAMPLE_ANNOTATION_ICON = "append_sample_annotation.gif";    

    public static final String APPEND_GENE_ANNOTATION_COMMAND = "append-gene-annotation-command";
    public static final String APPEND_GENE_ANNOTATION_ACTION = "append-gene-annotation-action";
    public static final String APPEND_GENE_ANNOTATION_NAME = "Append Gene Annotation";
    public static final String APPEND_GENE_ANNOTATION_ICON = "append_gene_annotation.gif";
   
    
   
     
     /* Raktim - Annotation Demo Only */
     public static final String GENOME_ANNOTATION_COMMAND = "genome-annotation-command";
     public static final String GENOME_ANNOTATION_ACTION = "genome-annotation-action";
     public static final String GENOME_ANNOTATION_NAME = "Genome Annotation";
     public static final String GENOME_ANNOTATION_ICON = "genome-annotation.gif";

    
    
    /**
     * Raktim Spet 29, 05
     * Adding CGH Commands
     * CGH display commands
     */
    public static final String SHOW_FLANKING_REGIONS = "show-flanking-regions";
    // popup commands
    //public static final String DELETE_NODE_CMD = "delete-node-cmd";
    public static final String RENAME_NODE_CMD = "rename-node-cmd";
    // help commands
    public static final String SHOW_SUPPORTTREE_LEGEND_COMMAND = "show-supporttree-legend-command";
    public static final String CGH_ELEMENT_LENGTH_5  = "cgh-element-length-5";
    public static final String CGH_ELEMENT_LENGTH_10  = "cgh-element-length-10";
    public static final String CGH_ELEMENT_LENGTH_20 = "cgh-element-length-20";
    public static final String CGH_ELEMENT_LENGTH_50  = "cgh-element-length-50";
    public static final String CGH_ELEMENT_LENGTH_100  = "cgh-element-length-100";
    public static final String CGH_ELEMENT_LENGTH_OTHER  = "cgh-element-length-other";
    public static final String CGH_ELEMENT_LENGTH_FIT = "cgh-element-length-fit";
    public static final String CGH_ELEMENT_WIDTH_5  = "cgh-element-width-5";
    public static final String CGH_ELEMENT_WIDTH_10  = "cgh-element-width-10";
    public static final String CGH_ELEMENT_WIDTH_20 = "cgh-element-width-20";
    public static final String CGH_ELEMENT_WIDTH_50  = "cgh-element-width-50";
    public static final String CGH_ELEMENT_WIDTH_100  = "cgh-element-width-100";
    public static final String CGH_ELEMENT_WIDTH_OTHER  = "cgh-element-width-other";
    public static final String CGH_ELEMENT_WIDTH_FIT = "cgh-element-width-fit";
    public static final String CGH_DISPLAY_TYPE_COMBINED = "cgh-display-type-combined";
    public static final String CGH_DISPLAY_TYPE_SEPARATED = "cgh-display-type-separated";
    public static final String CGH_DISPLAY_ORDER = "cgh-display-order";
    public static final String CGH_DELETE_SAMPLE = "cgh-delete-sample";
    public static final String CGH_DISPLAY_LABEL_WSL_ID = "cgh-display-label-wsl-id";
    public static final String CGH_DISPLAY_LABEL_ALIAS = "cgh-display-label-alias";
    public static final String CGH_DISPLAY_LABEL_ID1 = "cgh-display-label-id1";
    public static final String DELETED_BACS = "deleted-bacs";
    public static final String LOAD_SAMPLE_LIST_ACTION = "action-load-sample-list";
    public static final String LOAD_SAMPLE_LIST_NAME = "Load From Sample List";
    public static final String LOAD_WSL_ACTION = "action-load-wsl";
    public static final String LOAD_WSL_NAME = "Add Experiment from WSL";
    private static final String LOAD_WSL_SMALLICON    = "addfromdb16.gif";
    public static final String LOAD_CLONE_DISTRIBUTIONS_ACTION = "load-clone-distributions";
    public static final String LOAD_CLONE_DISTRIBUTIONS_NAME = "Load Clone Distributions";
    public static final String LOAD_CLONE_DISTRIBUTIONS_FROM_FILE_ACTION = "load-clone-distributions-from-file";
    public static final String LOAD_CLONE_DISTRIBUTIONS_FROM_FILE_NAME = "Load Clone Distributions From File";
    public static final String CGH_SET_THRESHOLDS = "cgh-set-thresholds";
    public static final String CGH_SET_MMSDs = "cgh-set-mmsds";
    public static final String CGH_COPY_NUMBER_BY_MMSDs = "cgh-copy-number-by-mmsds";
    public static final String CGH_COPY_NUMBER_BY_THRESHOLDS = "cgh-copy-number-by-thresholds";
    public static final String CGH_CLEAR_ANNOTATIONS = "cgh-clear-annotations";
    public static final String CGH_ANALYSIS_ACTION = "cgh-analysis-action";
    public static final String CGH_ANALYSIS_COMMAND = "cgh-analysis-command";
    public static final String CIRCLE_VIEWER_BACKGROUND = "circle-viewer-background";
    public static final String CLONE_VALUE_DISCRETE_DETERMINATION = "clone-ratio-discrete-determination";
    public static final String CLONE_VALUE_LOG_AVERAGE_INVERTED = "clone-ratio-log-invert-average";
    public static final String CLONE_VALUE_LOG_CLONE_DISTRIBUTION = "clone-ratio-log-clone-distribution";
    public static final String CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION = "clone-value-threshold-or-clone-distribution";
    public static final String SHOW_HEADER = "show-header";
    public static final String CLONE_P_THRESH = "clone-p-thresh";
    public static final String FLANKING_REGIONS_BY_THRESHOLD = "flanking-regions-by-threshold";
    public static final String FLANKING_REGIONS_BY_LOG_CLONE_DISTRIBUTION = "flanking-regions-by-log-clone-distribution";
    public static final String FLANKING_REGIONS_BY_THRESHOLD_OR_CLONE_DISTRIBUTION = "flanking-regions-by-threshold-or-clone-distribution";
    public static final String FIND_GENE = "find-gene";
    public static final String COMPARE_EXPERIMENTS = "compare-experiments";
    /* End CGH Commands */
    
  
    
    
}

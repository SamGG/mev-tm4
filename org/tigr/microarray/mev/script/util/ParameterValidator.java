/*
 * ParameterValidator.java
 *
 * Created on March 27, 2004, 10:19 PM
 */

package org.tigr.microarray.mev.script.util;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.dom.DOMImplementationImpl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import org.tigr.microarray.mev.script.ScriptManager;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

/**
 *
 * @author  braisted
 */

//TODO Value Type check, extend to all types, validate range.
//Validte that required are present.


public class ParameterValidator extends DefaultHandler{
    
    Element validationRoot;
    boolean haveValidationRoot;
    
    /** Creates a new instance of ParameterValidator */
    public ParameterValidator() {
        //parse validation file
        
    }
    
    public boolean loadParameterConstraints() {
        URL url = this.getClass().getResource("/org/tigr/microarray/mev/script/util/ParameterConstraints.xml");
        DOMParser parser = new DOMParser();
        
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setErrorHandler(this);
            parser.parse(url.toString());
            validationRoot = parser.getDocument().getDocumentElement();
            haveValidationRoot = true;
        } catch ( NullPointerException e ) {
            haveValidationRoot = false;
            JOptionPane.showMessageDialog(new JFrame(), "The parameter validation feature in support of scripting could not be initialized.\n"+
            "The constraint file \"ParameterConstraints.xml\" could not be located.\nScript capabilities will operate without full parameter validation",
            "Parameter Validation Initialization Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            haveValidationRoot = false;
            JOptionPane.showMessageDialog(new JFrame(), "The parameter validation feature in support of scripting could not be initialized properly.\n"+
            "\"ParameterConstraints.xml\" contained errors reported in the console window.\nScript capabilities will operate without full parameter validation",
            "Parameter Validation Initialization Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    public boolean isEnabled() {
        return haveValidationRoot;
    }
    
    /**
     *  Returns true if script parameters match the requirements
     *  defined in ScriptValidation.xml.  Algorithm parameter keys
     *  must match valid keys, value type, and value range as defined.
     */
    public boolean validate(ScriptManager manager, ScriptTree tree, ErrorLog log) {
        AlgorithmSet [] sets = tree.getAlgorithmSets();
        AlgorithmNode node;
        boolean isValid = true;
        log.reset();
        
        System.out.println("VALIDATE IN VALIDATOR");
        
        NodeList list = validationRoot.getElementsByTagName("script_algorithm");
        
        for(int setIndex = 0; setIndex < sets.length; setIndex++) {
            for(int algIndex = 0; algIndex < sets[setIndex].getAlgorithmCount(); algIndex++) {
                node = sets[setIndex].getAlgorithmNodeAt(algIndex);
                System.out.println("Validate Algorithm");
                if( !validateAlgorithm(node, list, log) )
                    isValid = false;
            }
        }
        return isValid;
    }
    
    
    /*  Validation Points
     *  1.) Is key valid?
     *  2.) Is value type valid?
     *  3.) Is value range ok?
     *  4.) Are all known required parameters present?
     */
    
    /**
     * Returns true if the algorithm parameters are valid.
     */
    private boolean validateAlgorithm( AlgorithmNode algorithmNode, NodeList algList, ErrorLog log) {
        AlgorithmData data = algorithmNode.getAlgorithmData();
        Map map = data.getParams().getMap();
        Iterator iter = map.keySet().iterator();
        String key;
        String value;
        String algName = algorithmNode.getAlgorithmName(); //(String)(map.get("name"));
        boolean isValid = true;
        
        //get algorithm name
        if(algName == null) {
            //Invalid, need algname, report and return false
            //REPORT
            
            return false;
        }
        
        //get algorithm dom Element
        Element algElement = getAlgorithmElement(algName, algList);
        if(algElement == null) {
            //invalid name or no entry in xml
            //REPORT
            ScriptParameterException spe = createScriptParameterException(algorithmNode, "Parameter constraint information is not available for this algorithm. "+
            "The algorithm parameters can not be validated.");
            log.recordParameterError(spe);
            return false;
        }
        
        Vector missingParameterVector = validateRequiredParameters(map.keySet(), algElement);
        
        if(missingParameterVector.size() > 0) {
            //missing required parameter
            //REPORT
            System.out.println("Missing Parameter > 0");
            for(int i = 0; i < missingParameterVector.size(); i++) {
                ScriptParameterException spe = new ScriptParameterException(algorithmNode.getAlgorithmName(), algorithmNode.getID(),
                algorithmNode.getDataNodeRef(), (String)(missingParameterVector.elementAt(i)), "N/A", "Missing required parameter.");
                log.recordParameterError(spe);
            }
            isValid = false;
        }
        
        //Test each parameter
        while(iter.hasNext()) {
            key = (String)(iter.next());
            value = (String)(map.get(key));
            if(!validateParameter(key, value, algElement, algorithmNode, log)) {
                isValid = false;
                
            }
        }
        return isValid;
    }
    
    
    
    private Element getAlgorithmElement(String algName, NodeList algList) {
        Element elem = null;
        String name;
        for(int i = 0; i < algList.getLength(); i++) {
            elem = (Element)(algList.item(i));
            if(elem.getAttribute("name").equals(algName))
                break;
        }
        return elem;
    }
    
    /**
     *  Returns true if required parameters are present.
     */
    private Vector validateRequiredParameters(Set keys, Element algElement) {
        Vector requiredKeys = new Vector();
        NodeList params = algElement.getElementsByTagName("param");
        
        for(int i = 0; i < params.getLength(); i++) {
            if(((Element)(params.item(i))).getAttribute("val_level").equalsIgnoreCase("REQUIRED")) {
                requiredKeys.add(((Element)(params.item(i))).getAttribute("key"));
            }
        }
        
        Vector keyVector = new Vector(keys);
        Vector missingKeys = new Vector();
        for(int i = 0; i < requiredKeys.size(); i++) {
            if(!keyVector.contains((String)(requiredKeys.elementAt(i))))
                missingKeys.add(requiredKeys.elementAt(i));
        }
        
        return missingKeys;
    }
    
    /**
     * Returns true if key, value type, and value range are valid.
     */
    private boolean validateParameter(String key, String value, Element algElement, AlgorithmNode algNode, ErrorLog log) {
        NodeList paramList = algElement.getElementsByTagName("param");
        Element paramElement = getParameterElement(paramList, key);
        if(paramElement == null) {
            //Invalid Key Name, not registered
            //REPORT
            ScriptParameterException spe = createScriptParameterException(algNode, paramElement, value,
            "Invalid key. Key name not recognized.");
            log.recordParameterError(spe);
            return false;
        }
        if(!validateValueType(value, paramElement, algElement, log)) {
            //value type error
            //REPORT
            ScriptParameterException spe = createScriptParameterException(algNode, paramElement, value,
            "Incorrect value type.");
            log.recordParameterError(spe);
            return false;
        }
        return true;
    }
    
    private ScriptParameterException createScriptParameterException(AlgorithmNode algNode, Element paramElement, String currValue, String message) {
        ScriptParameterException spe = new ScriptParameterException(algNode.getAlgorithmName(), algNode.getID(),
        algNode.getDataNodeRef(), paramElement.getAttribute("key"), currValue, message);
        return spe;
    }
    
    private ScriptParameterException createScriptParameterException(AlgorithmNode algNode, String message) {
        ScriptParameterException spe = new ScriptParameterException(algNode.getAlgorithmName(), algNode.getID(),
        algNode.getDataNodeRef(), "N/A", "N/A", message);
        return spe;
    }
    
    private Element getParameterElement(NodeList list, String key) {
        Element elem = null;
        for(int i = 0; i < list.getLength(); i++) {
            elem = (Element)(list.item(i));
            if(elem.getAttribute("key").equals(key)) {
                break;
            }
        }
        return elem;
    }
    
    private boolean validateValueType(String value, Element paramElement, Element algElement, ErrorLog log) {
        String type = paramElement.getAttribute("value_type");
        boolean isValid = true;
        try {
            if(type.equals("boolean")) {
                if(!value.equals("true") && !value.equals("false"))
                    isValid = true;
            } else if(type.equals("int")){
                Integer.parseInt(value);
            } else if(type.equals("long")){
                Long.parseLong(value);
            } else if(type.equals("float")) {
                Float.parseFloat(value);
            } else if(type.equals("double")) {
                Double.parseDouble(value);
            }
        } catch (NumberFormatException nfe) {
            //REPORT TYPE mismatched value type;
            return false;
        }
        return isValid;
    }
    
    public String getValidParameterTable(String algName) {
        if(!isEnabled())
            return null;
        
        String table = null;
        String key;
        String valueType;
        String min;
        String max;
        String valStatus;
        
        Element algElement= findAlgorithmElement(algName);
        
        Element paramElement;
        Element constraint;
        if(algElement != null) {
            Element paramList = (Element)(algElement.getElementsByTagName("param_list").item(0));
            if(paramList == null)
                return null;
            
            NodeList params = paramList.getElementsByTagName("param");
            
            table = "";
            table = "<h2>Valid Script Parameters for "+algName+"</h2>";
            table+= "<p>Note: Parameters that are not listed as \"Always\" required usually depend on the value of"+
            " other entered parameters to determine if they are required.</p>";
            
            table+="<table border=3><th>Key</th><th>Value Type</th><th>Min</th><th>Max</th><th>Required</th>";
            
            for(int i = 0; i < params.getLength(); i++ ) {
                paramElement = (Element)(params.item(i));
                NodeList constraintList =  paramElement.getElementsByTagName("constraint");
                
                key = paramElement.getAttribute("key");
                valueType = paramElement.getAttribute("value_type");
                valStatus = paramElement.getAttribute("val_level");
                min = " ";
                max = " ";
                if(constraintList != null && constraintList.getLength() > 0) {
                    constraint = (Element)(constraintList.item(0));
                    min = constraint.getAttribute("min");
                    max = constraint.getAttribute("max");
                }
                table+="<tr><td>"+key+"</td><td>"+valueType+"</td><td>"+min+"</td><td>"+max+"</td><td>"+ (valStatus.equalsIgnoreCase("REQUIRED") ? "Always" : "Dependant") +"</td></tr>";
            }
            table += "</table>";
        }
        return table;
    }
    
    private Element findAlgorithmElement(String algName) {
        NodeList list = validationRoot.getElementsByTagName("script_algorithm");
        Element algElement = null;
        String algElementName;
        for(int i = 0; i < list.getLength(); i++) {
            algElementName = null;
            algElement = (Element)(list.item(i));
            algElementName = algElement.getAttribute("name");
            if(algElementName != null && algElementName.equals(algName))
                return algElement;
        }
        return null;
    }
    
    
    
    //  WARNING Event Handler
    public void warning(SAXParseException e)
    throws SAXException {
        System.err.println("Warning:  "+e);
        
    }
    
    //  ERROR Event Handler
    public void error(SAXParseException e)
    throws SAXException {
        System.err.println("Error:  "+e);
        
    }
    
    //  FATAL ERROR Event Handler
    public void fatalError(SAXParseException e)
    throws SAXException {
        System.err.println("Fatal Error:  "+e);
        
    }
    
    public static void main(String [] args) {
        ParameterValidator pv = new ParameterValidator();
        pv.loadParameterConstraints();
        System.out.println("pv is enabled? = "+pv.isEnabled());
    }
}

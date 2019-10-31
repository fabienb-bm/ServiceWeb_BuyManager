/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POJO;

/**
* Nom de classe : Specs
* <br>
* Description : Permet de regrouper sous forme d'objet les spécifications techniques d'un produit
* <br>
* Date de la dernière modification : 06/2015
* 
* @author François DUPONT
*/
public class Specs {
    /*************************
    * Attributs
    ************************/
    static final String sKeyReach = "REACH";
    static final String sKeyLifeCycle= "LIFECYCLE";
    static final String sKeyROHS= "ROHS";
    
    static final String sROHSValueOK = "Compliant";
    
    /**
    * Valeur de la spécification
    */
    private String value = "";
    
    /**
    * La spécification
    */
    private String key = "";
    
    /**
    * L'unité de la spécification
    */
    private String unit = "";
    
    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
       
         switch (key) {
            case "SVHC":  // farnell
            case "reach_svhc_compliance": // octopart
                this.key = sKeyReach;
                break;
            case "lifecycle_status": 
                this.key = sKeyLifeCycle;
                break;    
            case "rohs_status": 
                this.key = sKeyROHS;
                break; 
            default:  
                this.key = key;
                break;   
         }
        
    }

    /**
     * Met a jour pour etre ROHS 
     * @param value 
     */
    public void setROHS(String value) {
        //force the good key
        this.setKey(sKeyROHS);
        //add the value
        switch (value) {
            case "RoHS Compliant":
            case "YES":
                this.setValue(sROHSValueOK);
                break;
            default:
                this.setValue(value);
                break;
        }
        //
    }
    
        /**
     * Met a jour pour etre ROHS 
     * @param value 
     */
    public void setREACH(String value) {
        this.setKey(sKeyReach);
        this.setValue(value);
    }
    
        
        /**
     * Met a jour pour etre ROHS 
     * @param value 
     */
    public void setLifeCycle(String value) {
        this.setKey(sKeyLifeCycle);
        this.setValue(value);
    }
    
    /**
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void add(Specs uneSpec) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.essai3;

import DB.ConnexionDB;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LME
 */
public class manufacturerMasterList {


    
    /**
     * identifiant
     */
    int manufacturerMasterListId;
    
    
    /**
     * identifiant master (chaine)
     */
    String MasterId;
    
    /**
     * 
     */
    String ManufacturerName;     
    /**
     * 
     */
    String ManufacturerAlternativeName;
    
    boolean ToDelete;
    
    boolean Supplier;

    /**
     *
     * @return
     */
    public int getManufacturerMasterListId() {
        return manufacturerMasterListId;
    }

    /**
     *
     * @param manufacturerMasterListId
     */
    public void setManufacturerMasterListId(int manufacturerMasterListId) {
        this.manufacturerMasterListId = manufacturerMasterListId;
    }

    /**
     *
     * @return
     */
    public String getMasterId() {
        return MasterId;
    }

    /**
     *
     * @param MasterId
     */
    public void setMasterId(String MasterId) {
        this.MasterId = MasterId;
    }

    /**
     *
     * @return
     */
    public String getManufacturerName() {
        return ManufacturerName;
    }

    /**
     *
     * @param ManufacturerName
     */
    public void setManufacturerName(String ManufacturerName) {
        this.ManufacturerName = ManufacturerName;
    }

    /**
     *
     * @return
     */
    public String getManufacturerAlternativeName() {
        return ManufacturerAlternativeName;
    }

    /**
     *
     * @param ManufacturerAlternativeName
     */
    public void setManufacturerAlternativeName(String ManufacturerAlternativeName) {
        this.ManufacturerAlternativeName = ManufacturerAlternativeName;
    }

    /**
     *
     * @return
     */
    public boolean isToDelete() {
        return ToDelete;
    }

    /**
     *
     * @param ToDelete
     */
    public void setToDelete(boolean ToDelete) {
        this.ToDelete = ToDelete;
    }

    /**
     *
     * @return
     */
    public boolean isSupplier() {
        return Supplier;
    }

    /**
     *
     * @param Supplier
     */
    public void setSupplier(boolean Supplier) {
        this.Supplier = Supplier;
    }
    
    /**
     *
     * @return
     */
    public static ArrayList<manufacturerMasterList> getAllSql(){
        //
        ArrayList<manufacturerMasterList> arrMasterfab = new ArrayList<manufacturerMasterList>();
        //
        String query = "Select * From manufacturerMasterList"   ;
        try {
            ResultSet listeFab = ConnexionDB.ExecuteQuery(query);
            while (listeFab.next()){
                //
                manufacturerMasterList objManuf = new  manufacturerMasterList();
                //
                objManuf.setAllFromResultSet(listeFab);
                arrMasterfab.add(objManuf);
            }
            
            
        } catch (SQLException ex) {
            Logger.getLogger(manufacturerMasterList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return arrMasterfab;
    }
    
    /**
     * 
     * @param reponse
     * @throws SQLException 
     */
    public void setAllFromResultSet(ResultSet reponse) throws SQLException {
        //
        this.manufacturerMasterListId    = reponse.getInt("manufacturerMasterListId");
        this.MasterId                    = reponse.getString("MasterId");
        this.ManufacturerName            = reponse.getString("ManufacturerName");    
        this.ManufacturerAlternativeName = reponse.getString("ManufacturerAlternativeName");
        this.ToDelete                    = reponse.getBoolean("ToDelete");
        this.Supplier                    = reponse.getBoolean("Supplier");
        //
    } 
    
    
}

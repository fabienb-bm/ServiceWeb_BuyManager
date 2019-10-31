/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LME
 */
public class clientCustomerKey {
    
    /**
    * identifiant de l'objet
    */
    private int clefClientID;
    
    
    /**
     * Clef du client 
     */
    private String customerKey;

    /**
     * identifiant du client
     */
    private int clientID;

    public int getClefClientID() {
        return clefClientID;
    }

    public void setClefClientID(int clefClientID) {
        this.clefClientID = clefClientID;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientId(int clientID) {
        this.clientID = clientID;
    }
            
    
     /**
     * met a jour l'objet à partir des champs de la BD
     * @param reponse 
     */
    void setAllFromResultSet(ResultSet reponse){
        
        try {
            //
            this.setClientId(reponse.getInt("clientId"));
            this.setClefClientID(reponse.getInt("clefClientID"));
            this.setCustomerKey(reponse.getString("customerKey"));
            //
        } catch (SQLException ex) {
            Logger.getLogger(ClientDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param clientID
     * @return ArrayList<clientCustomerKey> tableau de liste de clef client
     */
    static ArrayList<clientCustomerKey> getAllClientCutomerKeyFromClientID(int clientID){
        //
        ArrayList<clientCustomerKey> arrClientCustomerKey = new ArrayList<clientCustomerKey>();
        
        String requete = "SELECT * " +
                            "FROM clientCustomerKey  " +
                            "WHERE clientCustomerKey.clientID = '"+clientID+"' ";
        //
        //
        try {
            ResultSet reponse = ConnexionDB.ExecuteQuery(requete);
            while (reponse.next()){
                //
                clientCustomerKey ccKey = new clientCustomerKey();
                ccKey.setAllFromResultSet(reponse);
                arrClientCustomerKey.add(ccKey);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(clientCustomerKey.class.getName()).log(Level.SEVERE, null, ex);
        }
        //
        return arrClientCustomerKey;        
    }
    
    /**
     * insére une nouvelle clef client
     * @param clientId
     * @param cusKey
     * @throws SQLException 
     */
   static  void insertClientCutomerKey(int clientId, String cusKey) throws SQLException{
        String myQuery = "INSERT INTO clientCustomerKey "
                            + "( clientId, customerKey ) VALUES"
                            + "("+clientId+",'"+cusKey+"')";
        //
        ConnexionDB.ExecuteUpdate(myQuery);
        //
    }

}

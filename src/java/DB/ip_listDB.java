/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.sql.SQLException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author LME
 */
public class ip_listDB {
    
    /**
     * 
     */
    private int ip_listID;
    
    /**
     * 
     */
    private int clientID;
    
    /**
     * 
     */
    private String ip;
    
    /**
     * 
     */
    private Date date;

    /**
     * 
     */
    private String action;


    
    
    /**
     *
     * @return
     */
    public int getIp_listID() {
        return ip_listID;
    }

    /**
     *
     * @param ip_listID
     */
    public void setIp_listID(int ip_listID) {
        this.ip_listID = ip_listID;
    }

    /**
     *
     * @return
     */
    public int getClientID() {
        return clientID;
    }

    /**
     *
     * @param clientID
     */
    public void setClientId(int clientID) {
        this.clientID = clientID;
    }

    /**
     *
     * @return
     */
    public String getIp() {
        return ip;
    }

    /**
     *
     * @param ip
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     *
     * @return
     */
    public Date getDate() {
        return date;
    }

    /**
     *
     * @param date
     */
    public void setDate(Date date) {
        this.date = date;
    }
  
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    
    
    //Stock l'ip du client, son id, la date de la requete ainsi que le nom de sa requete
    static public void insertLog(HttpServletRequest request,int ClientID, String myAction) throws SQLException{
        if ( request != null ){
            String myQuery = "INSERT INTO ip_list "
                                + "( clientID, ip, date,action) VALUES"
                                + "("+ClientID+",'"+request.getRemoteAddr()+"',NOW(),'"+myAction+"')";

            ConnexionDB.ExecuteUpdate(myQuery);
        }
    }
    
    
    
}

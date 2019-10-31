package DB;

import java.util.Date;

/**
* Nom de classe : IpListDB
* <br>
* Description : Classe découlant de la table "ip_list" de la base de données.
* <br>
* Date de la dernière modification : 22/07/2014
* 
* @author Stagiaire (Florence Giraud)
*/
public class IpListDB {
    
    
    /*************************
    * Attributs
    ************************/
    
    
    /**
    * L'identifiant de l'IP (clef primaire de la table).
    * @see IpListDB#getIpListId() 
    * @see IpListDB#setIpListId(int) 
    */
    private int ipListId;

    /**
    * L'identifiant du client.
    * @see IpListDB#getClientId() 
    * @see IpListDB#setClientId(int) 
    */
    private int clientId;
    
    /**
    * L'adresse IP connectée.
    * @see IpListDB#getIp() 
    * @see IpListDB#setIp(java.lang.String) 
    */
    private String ip;
    
    /**
    * La dernière date à laquelle l'adresse IP s'est connectée.
    * @see IpListDB#getDate() 
    * @see IpListDB#setDate(java.util.Date) 
    */
    private Date date;
    
    
    /*************************
    * Constructeurs
    ************************/
    
    
    /**
    * Constructeur de la classe IpListDB.
    */
    public IpListDB() {
    }

    
    /*************************
    * GETTERS
    ************************/
    
    
    /**
    * Retoune la valeur de l'attribut "ipListId".
    * @return ipListId : l'identifiant de l'IP.
    */
    public int getIpListId() {
        return ipListId;
    }

    /**
    * Retoune la valeur de l'attribut "clientId".
    * @return clientId : l'identifiant du client.
    */
    public int getClientId() {
        return clientId;
    }

    /**
    * Retoune la valeur de l'attribut "ip".
    * @return ip : l'adresse IP connectée.
    */
    public String getIp() {
        return ip;
    }

    /**
    * Retoune la valeur de l'attribut "date".
    * @return date : la dernière date à laquelle l'adresse IP s'est connectée.
    */
    public Date getDate() {
        return date;
    }
    
    
    /*************************
    * SETTERS
    ************************/
    
    
    /**
    * Met à jour l'identifiant de l'IP.
    * @param ipListId identifiant de l'adresse IP.
    */
    public void setIpListId(int ipListId) {
        this.ipListId = ipListId;
    }

    /**
    * Met à jour l'identifiant du client.
    * @param clientId identifiant du client.
    */
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /**
    * Met à jour l'adresse IP.
    * @param ip adresse IP connectée.
    */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
    * Met à jour la dernière date à laquelle l'IP s'est connectée.
    * @param date dernière date à laquelle l'IP s'est connectée.
    */
    public void setDate(Date date) {
        this.date = date;
    }
}

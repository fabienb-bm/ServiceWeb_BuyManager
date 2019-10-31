package DB;

import com.mysql.jdbc.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
* Nom de classe : WebserviceDB
* <br>
* Description : Classe découlant de la table "webservice" de la base de données.
* <br>
* Date de la dernière modification : 07/08/2014
* 
* @author Stagiaire (Florence Giraud)
*/
public class WebserviceDB {
    
    
    /*************************
    * Attributs
    ************************/
    
    
    /**
    * L'identifiant du webservice.
    * @see WebserviceDB#getWsId() 
    * @see WebserviceDB#setWsId(int) 
    */
    private int wsId;

    /**
    * Le nom du webservice.
    * @see WebserviceDB#getNom() 
    * @see WebserviceDB#setNom(java.lang.String) 
    */
    private String nom;
    
   
    
    /*************************
    * Constructeurs
    **************************/
    
    
    /**
    * Constructeur de la classe WebserviceDB.
    */
    public WebserviceDB() {
    }

    
    /*************************
    * GETTERS
    *************************/
    
    
    /**
    * Retourne la valeur de l'attribut "wsId".
    * @return wsId : l'identifiant du webservice.
    */
    public int getWsId() {
        return wsId;
    }
    
    /**
    * Retourne la valeur de l'attribut "nom".
    * @return nom : le nom du webservice.
    */
    public String getNom() {
        return nom;
    }
      
    
    /*************************
    * SETTERS
    ************************/
    
    
    /**
    * Met à jour l'identifiant du webservice.
    * @param wsId le nouvel identifiant du webservice.
    */
    public void setWsId(int wsId) {
        this.wsId = wsId;
    }

    /**
    * Met à jour le nom du webservice.
    * @param nom le nouveau nom du webservice.
    */
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    /*************************
    * METHODES PUBLIQUES
    **************************/
    
    
    /**
    * Requête permettant la recupération de l'identifiant du webservice.
    * @param nomWs le nom du webservice dont on veut recuperer l'ID.
    * @return wsID : l'identifiant du webservice ou renvoie 0 si il n'a pas été trouvé.
    * @throws ClassNotFoundException : exception levée quand une classe n’a pas été trouvée.
    * @throws SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    */
    public static int requeteRecupWsId(String nomWs) throws ClassNotFoundException, SQLException
    {
        int wsID = 0;
        
        //
        //Requête
        String requete = "SELECT * FROM webservice";
        ResultSet reponse= ConnexionDB.ExecuteQuery(requete) ; 
        
        //Boucle sur la base de données
        while(reponse.next())
        {
            String nom = reponse.getString("nom");
            if (nom.equals(nomWs)){
                wsID = reponse.getInt("wsID");
            }
        }
        return wsID;
    }
    
    /**
    * Permet de récupérer l'ensemble des identifiants de tous les services web présent dans la base de données.
    * @return un tableau/liste de tous les identifiants des web service.
    * @throws SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    * @throws ClassNotFoundException : exception levée quand une classe n’a pas été trouvée.
    */
    public static ArrayList<Integer> requeteRecupTousLesIdsWs() throws SQLException, ClassNotFoundException
    {
        //declaration du tableau qui contiendra tous les ids des SW
        ArrayList<Integer> tabWsId = new ArrayList<Integer>();
        //
        //Connection        
        String requete = "SELECT wsID FROM webservice";
        ResultSet reponse= ConnexionDB.ExecuteQuery(requete) ; 
        
        //Boucle sur la base de données
        while(reponse.next())
        {
            int wsID = reponse.getInt("wsID");
            tabWsId.add(wsID);
        }
        return tabWsId;
    }
}

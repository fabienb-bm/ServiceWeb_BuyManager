package DB;

import POJO.SearchResult;
import WS.Digikey;
import WS.Farnell;
import WS.Arrow;
import WS.InterfaceWSInterrogeable;
import WS.Mouser;
import WS.Octopart;
import WS.Rs;
import WS.MyArrow;
import WS.FindChips;
import WS.TME;
import WS.WebService;
import com.essai3.ServiceResource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;

/**
 * Nom de classe : WsClientDB
 * <br>
 * Description : Classe découlant de la table "ws_client" de la base de données.
 * <br>
 * Date de la dernière modification : 07/08/2014
 * 
* @author Stagiaire (Florence Giraud)
 */
public class WsClientDB extends WebService {

    /**
     * ***********************
     * Attributs
    ***********************
     */
    /**
     * Constante de l'Id octopart de la classe webservice
     */
    static final int octopartWsId = 1;

    /**
     * Constante de l'Id farnell de la classe webservice
     */
    static final int farnellWsId = 2;

    /**
     * Constante de l'Id rs de la classe webservice
     */
    static final int rsWsId = 3;

    /**
     * Constante de l'Id tme de la classe webservice
     */
    public static final int tmeWsId = 4;

    /**
     * Constante de l'Id mouser de la classe webservice
     */
    static final int mouserWsId = 5;
    
    /**
     * Constante de l'Id digikey de la classe webservice
     */
    public static final int digikeyWsId = 6;
    
    //
    public static final int arrowWsId = 7;

    //
    public static final int myarrowWsId = 8;
    
    //
    public static final int findchipsWsId = 9;
    /**
     * L'identifiant du client.
     *
     * @see WsClientDB#getClientId()
     * @see WsClientDB#setClientId(int)
     */
    private int clientId;

    /**
     * L'identifiant du webservice.
     *
     * @see WsClientDB#getWsId()
     * @see WsClientDB#setWsId(int)
     */
    private int wsId;

    /**
     * Le magasin dans lequel la recherche doit être effectuée (Requête
     * Farnell).
     *
     * @see WsClientDB#getMagasin()
     * @see WsClientDB#setMagasin(java.lang.String)
     */
    private String magasin;

    /**
     * La priorité du service Web (la plus petite valeur est la plus haute
     * priorité).
     *
     * @see WsClientDB#getSwPrio()
     * @see WsClientDB#setSwPrio(int)
     */
    private int swPrio;

    /**
     * Indiquateur précisant si le SW doit être interrogé par défaut.
     *
     * @see WsClientDB#isInterroParDefaut()
     * @see WsClientDB#setInterroParDefaut(boolean)
     */
    private boolean interroParDefaut;


    //
    private String country = "FR";

    //
    private String key;

    private String password;
    //

    // login du client sur le webservice 
    private String login = "";

    /**
     *
     * @return
     */
    public String getLogin() {
        return login;
    }

    /**
     *
     * @param login
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     *
     * @return
     */
    public String getKey() {

        if (this.key != null) {
            return key;
        } else {
            return "";
        }
    }

    /**
     *
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     *
     * @return
     */
    public String getCountry() {
        return country;
    }

    /**
     *
     * @param myCountry
     */
    public void setCountry(String myCountry) {
        if (!myCountry.isEmpty()) {
            this.country = myCountry;
        }
    }
    
        /**
     *
     *  Met a vide le pay notament pour Arrow
     */
    public void setEmptyCountry() {
        this.country = "";
    }


    //
    /**
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    //
    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * ***********************
     * Constructeurs
    ***********************
     */
    /**
     * Constructeur vide de la classe WsClientDB.
     */
    public WsClientDB() {
    }

    /**
     * ***********************
     * GETTERS
    ***********************
     */
    /**
     * Retourne la valeur de l'attribut "clientId".
     *
     * @return clientId : l'identifiant du client.
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Retourne la valeur de l'attribut "wsId".
     *
     * @return wsId : l'identifiant du webservice.
     */
    public int getWsId() {
        return wsId;
    }

    /**
     * Retourne la valeur de l'attribut "magasin".
     *
     * @return magasin : le nom du magasin dans lequel la requête doit être
     * effectuée.
     */
    public String getMagasin() {
        if (this.magasin == null) {
            return "";
        }else{
            return magasin;

        }
    }

    /**
     * Retourne la valeur de l'attribut "swPrio".
     *
     * @return swPrio : la priorité à laquelle le SW est soumis.
     */
    public int getSwPrio() {
        return swPrio;
    }

    /**
     * Retourne la valeur de l'attribut "interroParDefaut".
     *
     * @return interroParDefaut : boolean indiquant si le SW doit être
     * interrogé.
     */
    public boolean isInterroParDefaut() {
        return interroParDefaut;
    }

    /**
     * ***********************
     * SETTERS
    ***********************
     */
    /**
     * Met à jour l'identifiant du client.
     *
     * @param clientId le nouvel identifiant du client.
     */
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /**
     * Met à jour l'identifiant du webservice.
     *
     * @param wsId le nouvel identifiant du webservice.
     */
    public void setWsId(int wsId) {
        this.wsId = wsId;
    }

    /**
     * Met à jour le magasin ciblé par la requete (Farnell).
     *
     * @param magasin le nouveau magasin dans lequel la requete sera effectuée.
     */
    public void setMagasin(String magasin) {
        this.magasin = magasin;
    }

    /**
     * Met à jour la priorité du SW.
     *
     * @param swPrio la nouvelle priorité à laquelle le SW est soumis.
     */
    public void setSwPrio(int swPrio) {
        this.swPrio = swPrio;
    }

    /**
     * Met à jour le boolean d'interrogation par défaut.
     *
     * @param interroParDefaut la nouvelle valeur du booleant précisant si le SW
     * doit être interrogé par défaut ou non.
     */
    public void setInterroParDefaut(boolean interroParDefaut) {
        this.interroParDefaut = interroParDefaut;
    }

    /**
     * **********************************
     * Methodes publiques - Requetes
    ***********************************
     */
    /**
     * Permet de vérifier si le client ciblé a les droits pour l'API ciblée.
     *
     * @param clientID l'identifiant du client.
     * @param webserviceID l'identifiant du webservice interrogé.
     * @return droitWs boolean indiquant si le client a les droits pour l'API
     * testée.
     * @throws ClassNotFoundException : exception levée quand une classe n’a pas
     * été trouvée.
     * @throws SQLException : exception levée suite à une erreur SQL (connexion
     * à la base/ mauvaise requête).
     */
    public static boolean requeteVerifDroitSW(int clientID, int webserviceID) throws ClassNotFoundException, SQLException {
        boolean droitWS = false;

        //
        //si le webservice existe
        if (webserviceID != 0) {
            //Requete
            String requete = "SELECT * FROM ws_client WHERE `clientID`='" + clientID + "'";
            ResultSet reponse = ConnexionDB.ExecuteQuery(requete);
            //Boucle sur la base de données
            while (reponse.next()) {
                int wsID = reponse.getInt("wsID");
                if (wsID == webserviceID) {
                    droitWS = true;
                }
            }
        }
        return droitWS;
    }

    /**
     * Permet de vérifier si le client souhaite faire la requête par défaut sur
     * cette API.
     *
     * @param clientID l'identifiant du client.
     * @param swID l'identifiant du SW dont on souhaite faire la requête.
     * @return un boolean avec true=on souhaite une interrogation de l'API.
     * @throws ClassNotFoundException : exception levée quand une classe n’a pas
     * été trouvée.
     * @throws SQLException : exception levée suite à une erreur SQL (connexion
     * à la base/ mauvaise requête).
     */
    public static boolean requeteVerifInterroSW(int clientID, int swID) throws ClassNotFoundException, SQLException {
        boolean interroSW = false;
        String interroParDefaut = null;

        //si le webservice existe
        if (swID != 0) {
            //Requete
            String requete = "SELECT `interroParDefaut` FROM ws_client WHERE `clientID`='" + clientID + "' AND `wsID`='" + swID + "'";
            ResultSet reponse = ConnexionDB.ExecuteQuery(requete);
            //Boucle sur la base de données
            while (reponse.next()) {
                interroParDefaut = reponse.getString("interroParDefaut");
            }
        }
        if ("true".equals(interroParDefaut)) {
            interroSW = true;
        }
        return interroSW;
    }

    /**
     * Permet de recuperer le magasin pour la requete Farnell.
     *
     * @param clientID l'identifiant du client.
     * @return le magasin : condition pour la requête de l'API de Farnell.
     * @throws java.sql.SQLException : exception levée suite à une erreur SQL
     * (connexion à la base/ mauvaise requête).
     * @throws java.lang.ClassNotFoundException : exception levée quand une
     * classe n’a pas été trouvée.
     */
    public static String requeteRecupMagasin(int clientID) throws SQLException, ClassNotFoundException {
        String magasin = null;
        //
        //recuperation de l'ID de Farnell
        int swID = WebserviceDB.requeteRecupWsId("farnell");

        //si le SW Farnell existe, on s'assure que le client a les droits
        if (swID != 0) {
            boolean droitFarnell = WsClientDB.requeteVerifDroitSW(clientID, swID);

            //si le client a les droits, on recupere le magasin
            if (droitFarnell) {
                String requete = "SELECT `magasin` FROM ws_client WHERE `clientID`='" + clientID + "' AND `wsID`='" + swID + "'";
                ResultSet reponse = ConnexionDB.ExecuteQuery(requete);
                while (reponse.next()) {
                    magasin = reponse.getString("magasin");
                }
            }
        }
        return magasin;
    }

    

    /**
     * Permet de modifier la valeur du boolean interroParDefaut.
     *
     * @param interroParDefaut booléen passé en paramètre lors de l'appel du SW
     * par Buymanager.
     * @param clientID l'identifiant du client.
     * @param wsID l'identifiant du service web.
     * @return vrai si l'update de la BD s'est déroulée sans erreur.
     * @throws java.sql.SQLException : exception levée suite à une erreur SQL
     * (connexion à la base/ mauvaise requête).
     * @throws java.lang.ClassNotFoundException : exception levée quand une
     * classe n’a pas été trouvée.
     */
    public static boolean requeteModifInterroParDefaut(boolean interroParDefaut, int clientID, int wsID) throws SQLException, ClassNotFoundException {
        //connexion à la BD
        //
        ConnexionDB.ExecuteUpdate("UPDATE ws_client SET `interroParDefaut`='" + interroParDefaut + "' WHERE `clientID`='" + clientID + "' and`wsID`='" + wsID + "'");
        //
        return true;
    }

    /**
     * Permet de modifier la valeur de l'attribut "prioWs" suivant la priorité
     * passée en paramètre pour un couple client-service web.
     *
     * @param prioWsParamètre la nouvelle valeur de la priorité d'un service
     * web.
     * @param clientID l'identifiant du client.
     * @param swID l'identifiant du service web.
     * @return un boolean valant true si la modification a été faite, false
     * sinon.
     * @throws SQLException : exception levée suite à une erreur SQL (connexion
     * à la base/ mauvaise requête).
     * @throws ClassNotFoundException : exception levée quand une classe n’a pas
     * été trouvée.
     */
    public static boolean requeteModifPrioWs(int prioWsParamètre, int clientID, int swID) throws SQLException, ClassNotFoundException {
        //connexion à la BD
        //
        ConnexionDB.ExecuteUpdate("UPDATE ws_client SET `swPrio`='" + prioWsParamètre + "' WHERE `clientID`='" + clientID + "' and`wsID`='" + swID + "'");

        return true;
    }

    /**
     * ***********************
     * Methodes publiques
    ***********************
     */
    /**
     * Permet la creation d'une liste de webservice à interroger suivant les
     * droits du client.
     *
     * @param clientID l'identifiant du client.
     * @param nbMPNs le nombre de MPNs qui seront questionnés.
     * @param listWS liste des web services à interroger
     * @return liste la liste des WebService autorisées pour le client.
     * @throws ClassNotFoundException : exception levée quand une classe n’a pas
     * été trouvée.
     * @throws SQLException : exception levée suite à une erreur SQL (connexion
     * à la base/ mauvaise requête).
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     */
    public static ArrayList<InterfaceWSInterrogeable> creationListeWs(int clientID, int nbMPNs, String listWS)
            throws ClassNotFoundException, SQLException, JSONException {
        ArrayList<InterfaceWSInterrogeable> liste = new ArrayList<InterfaceWSInterrogeable>();

        //Verification du nombre de requetes effectuées dans le mois    
        //si le nombre de requetes effectuées dans le mois égale celui des autorisées alors il n'est plus possible au client de faire de requete
        boolean reqPossible = UtilisationClientDB.requeteVerifCredit(clientID, nbMPNs);

        if (reqPossible == true) {
            //
            //connexion à la BD
            String requete;
            if (listWS.isEmpty() || listWS.equals("0")) {
                requete = "  SELECT ws_client.* "
                        + " FROM ws_client "
                        + " WHERE `clientID`='" + clientID + "' "
                        + " AND `interroParDefaut`='true' ";
            } else {
                String[] list = listWS.split("\\|\\|");                
                String params = "";
                for(int i = 0;i<list.length;i++){
                    if(i == list.length-1){
                        params += list[i];
                    }else{
                        params += list[i]+",";
                    }
                }
                requete = "  SELECT ws_client.* "
                        + " FROM ws_client "
                        + " WHERE `clientID`='" + clientID + "' "
                        + " AND `interroParDefaut`='true' "
                        + " AND `wsID` in ("+params+")";
            }

            ResultSet reponse = ConnexionDB.ExecuteQuery(requete);

            //Boucle sur la base de données
            while (reponse.next()) {
                WsClientDB wsClient;
                int wsID = reponse.getInt("wsID");
                //
                switch (wsID) {
                    case octopartWsId:
                        wsClient = new Octopart();
                        liste.add((Octopart) wsClient);
                        break;
                    case farnellWsId:
                        wsClient = new Farnell();
                        liste.add((Farnell) wsClient);
                        break;
                    case rsWsId:
                        wsClient = new Rs();
                        liste.add((Rs) wsClient);
                        break;
                    case tmeWsId:
                        wsClient = new TME();
                        liste.add((TME) wsClient);
                        break;
                    case mouserWsId:
                        wsClient = new Mouser();
                        liste.add((Mouser) wsClient);
                        break;
                    case digikeyWsId:
                        wsClient = new Digikey();
                        liste.add((Digikey) wsClient);
                        break;
                    case arrowWsId :
                        wsClient = new Arrow();
                        liste.add((Arrow) wsClient);
                        break;
                    case myarrowWsId :
                        wsClient = new MyArrow();
                        liste.add((MyArrow) wsClient);
                        break;
                    case findchipsWsId :
                        wsClient = new FindChips();
                        liste.add((FindChips) wsClient);
                        break;
                    default:
                        wsClient = new WsClientDB();
                        break;
                }

                /**
                 * Recupere la clef du web service
                 */
                wsClient.setWsId(wsID);
                wsClient.setClientId(reponse.getInt("clientID"));
                wsClient.setKey(reponse.getString("key"));
                wsClient.setLogin(reponse.getString("login"));
                wsClient.setPassword(reponse.getString("password"));
                //
                wsClient.setMagasin(reponse.getString("magasin"));

                /**
                 * Conserve le pays du Web service si celui ci est definit sinon
                 * utilise celui en parametre
                 */
                String myCountry = reponse.getString("country");
                if (myCountry != null) {
                    wsClient.setCountry(myCountry);
                }

            }
        }
        return liste;
    }
    
      

    /**
     * Permet la creation d'une liste de webservice à interroger suivant les
     * droits du client.
     *
     * @param clientID l'identifiant du client.
     * @param webServiceID
     * @return liste la liste des WebService autorisées pour le client.
     * @throws ClassNotFoundException : exception levée quand une classe n’a pas
     * été trouvée.
     * @throws SQLException : exception levée suite à une erreur SQL (connexion
     * à la base/ mauvaise requête).
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     */
    public static InterfaceWSInterrogeable getWsFromWebserviceID(int clientID, int webServiceID)
            throws ClassNotFoundException, SQLException, JSONException {
        InterfaceWSInterrogeable wsIntClient = null;
        //
        String requete = "  SELECT ws_client.* "
                + " FROM ws_client "
                + " WHERE `clientID`='" + clientID + "' "
                + "         AND `wsID`='" + webServiceID + "' "
                + "         AND `interroParDefaut`='true' ";

        ResultSet reponse = ConnexionDB.ExecuteQuery(requete);

        //Boucle sur la base de données
        while (reponse.next()) {
            wsIntClient = extractClientFromResultSet(reponse);

        }
        return wsIntClient;
    }
    
    /**
     * return true if the customer have specific 
     * @return 
     */
    public boolean getCustomerPriceAvailable(){
        return false;
    }

    /**
     *
     * @param reponse ResultSet resultat de l'interriogation de la table
     * wsClientBD
     * @return wsClient
     */
    protected static InterfaceWSInterrogeable extractClientFromResultSet(ResultSet reponse) {
        //
        WsClientDB wsClient = null;
        try {
            //
            int wsID = reponse.getInt("wsID");
            switch (wsID) {
                case octopartWsId:
                    wsClient = new Octopart();
                    break;
                case farnellWsId:
                    wsClient = new Farnell();
                    break;
                case rsWsId:
                    wsClient = new Rs();
                    break;
                case mouserWsId:
                    wsClient = new Mouser();
                    break;     
                case digikeyWsId:
                    wsClient = new Digikey();
                    break;   
                case myarrowWsId:
                    wsClient = new MyArrow();
                    break; 
                case findchipsWsId:
                    wsClient = new FindChips();
                    break; 
                default:
                    break;
            }

            if (wsClient != null) {
                /**
                 * Recupere la clef du web service
                 */
                wsClient.setClientId(reponse.getInt("clientID"));
                wsClient.setKey(reponse.getString("key"));
                wsClient.setLogin(reponse.getString("login"));
                wsClient.setMagasin(reponse.getString("magasin"));
                wsClient.setPassword(reponse.getString("password"));
                /**
                 * Conserve le pays du Web service si celui ci est definit sinon
                 * utilise celui en parametre
                 */
                String myCountry = reponse.getString("country");
                if (myCountry != null) {
                    //
                    wsClient.setCountry(myCountry);
                    //
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, e);

        }

        if (wsClient != null) {
            return (InterfaceWSInterrogeable) wsClient;
        } else {
            return null;
        }
    }


    
    
}

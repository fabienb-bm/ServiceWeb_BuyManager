package DB;

import com.mysql.jdbc.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* Nom de classe : ClientDB
* <br>
* Description : Classe découlante de la table "Client" de la base de données.
* <br>
* Date de la dernière modification : 22/07/2014
* 
* @author Stagiaire (Florence Giraud)
* TEST GGR
*/
public class ClientDB {

    /**
     * Constante version si clef web gate non valide détecte
     */
    static final int clefWebgateNonvalide = -1;
     
    /**
     * Constante version si clef client non valide
     */
    static final int clefClientNonvalide = -2;
    
    
            
    /*************************
    * Attributs
    ************************/
    
    
    /**
    * L'identifiant du client (clef primaire de la table).
    * @see ClientDB#getClientId() 
    * @see ClientDB#setClientId(int) 
    */
    private int clientId;

    /**
    * Le nom du client (nom de l'entreprise).
    * @see ClientDB#getNom() 
    * @see ClientDB#setNom(java.lang.String) 
    */
    private String nom;
    
    /**
    * La clé du client (agit comme un password).
    * @see ClientDB#getKey() 
    * @see ClientDB#setKey(java.lang.String) 
    */
    private String key ;
    
    /**
    * Le nombre de requêtes maximales que le client est autorisé à effectuer.
    * @see ClientDB#getNbReqAutorisees() 
    * @see ClientDB#setNbReqAutorisees(int) 
    */
    private int nbReqAutorisees;
    
    /**
     *  cLef client permettantde verifier l'utilisation unique  de la clef
     */
    private String customer_key;

  
    /**
     * version de RQT du client
     */
    private int CustomerVersion = clefWebgateNonvalide;
    
    
    /**
     * date d'expiration de la date d'expiration
     */
    private Date key_dateExpiration;
    
    
    /**
     * tableau de service avec prix dispo
     */
    private ArrayList<Integer> arrCustomerPrice;
    
    /**
     * liste des dernieres information du client
     */
    private UtilisationClientDB derniereUtilisation;

    /**
     * nombr ede de clef client disponible 
     *  => si entier > nombre licences
     *  => sinon si 0 ou texte => interdiction d'accés
     *  => si "*" => accés ilimité
     **/
    
    private String nbCustomerKey;

    public ArrayList<Integer> getArrCustomerPrice() {
        return arrCustomerPrice;
    }

    public void setArrCustomerPrice(ArrayList<Integer> arrCustomerPrice) {
        this.arrCustomerPrice = arrCustomerPrice;
    }
    
    public String getNbCustomerKey() {
        return nbCustomerKey;
    }
    

    public void setNbCustomerKey(String nbCustomerKey) {
        this.nbCustomerKey = nbCustomerKey;
    }
    
    
    private Integer nbMonthDeferred;
    
    public Integer getNbMonthDeferred() {
        return nbMonthDeferred;
    }
    

    public void setNbMonthDeferred(Integer nbMonthDeferred) {
        this.nbMonthDeferred = nbMonthDeferred;
    }
    
    /**
     * 
     * @return UtilisationClientDB dernier enregistrement d'utilisation disponible en base
     */
    public UtilisationClientDB getDerniereUtilisation() {
        return derniereUtilisation;
    }

    /**
     * affecte la dernier utilisation
     * @param derniereUtilisation 
     */
    public void setDerniereUtilisation(UtilisationClientDB derniereUtilisation) {
        this.derniereUtilisation = derniereUtilisation;
    }
    
    
    
    /*************************
    * Constructeurs
    ************************/
    
    
    /**
    * Constructeur vide de la classe ClientDB.
    */
    public ClientDB() {
        arrCustomerPrice = new ArrayList<Integer>();
    }
    
           
    /*************************
    * GETTERS
     * @return 
    ************************/
    public String getCustomer_key() {
        return customer_key;
    }

    /**
     *
     * @param customer_key
     */
    public void setCustomer_key(String customer_key) {
        this.customer_key = customer_key;
    }

    /**
     *
     * @return
     */
    public int getCustomerVersion() {
        return CustomerVersion;
    }

    /**
     *
     * @param CustomerVersion
     */
    public void setCustomerVersion(int CustomerVersion) {
        this.CustomerVersion = CustomerVersion;
    }

    /**
     *
     * @return
     */
    public Date getKey_dateExpiration() {
        return key_dateExpiration;
    }

    /**
     *
     * @param key_dateExpiration
     */
    public void setKey_dateExpiration(Date key_dateExpiration) {
        this.key_dateExpiration = key_dateExpiration;
    }
      
    
    /**
    * Retourne la valeur de l'attribut "clientId".
    * @return clientId : l'ID du client.
    */
    public int getClientId() {
        return clientId;
    }

    /**
    * Retourne la valeur de l'attribut "nom".
    * @return nom : le nom du client.
    */
    public String getNom() {
        return nom;
    }

    /**
    * Retourne la valeur de l'attribut "key".
    * @return key : la clé du client.
    */
    public String getKey() {
        return key;
    }

    /**
    * Retoune la valeur de l'attribut "nbReqAutorisees".
    * @return nbReqAutorisees : le nombre de requêtes maximales que le client peut effectuer.
    */
    public int getNbReqAutorisees() {
        return nbReqAutorisees;
    }
    
    
    /*************************
    * SETTERS
    ************************/
    
    
    /**
    * Met à jour l'id du client.
    * @param clientId le nouvel identifiant du client.
    */
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /**
    * Met à jour le nom du client.
    * @param nom le nouveau nom du client.
    */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
    * Met à jour la clé du client.
    * @param key la nouvelle clé du client.
    */
    public void setKey(String key) {
        this.key = key;
    }

    /**
    * Met à jour le nombre de requêtes autorisées pour le client.
    * @param nbReqAutorisees le nouveau nombre de requêtes autorisées.
    */
    public void setNbReqAutorisees(int nbReqAutorisees) {
        this.nbReqAutorisees = nbReqAutorisees;
    }

    
    
    /*************************
    * METHODES PUBLIQUES
    ************************/
    
    
    /**
    * Requête permettant de verifier si le client existe en verifiant sa clé.
    * @param clefVerif clé du client qui doit être verifiée.
     * @param clefClient
    * @return clientID : l'identifiant du client si la clé est bonne ou vaut zéro si la clé ne correspond à aucune entrée dans la base.
    * @throws ClassNotFoundException : exception levée quand une classe n’a pas été trouvée.
    * @throws SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    */
    
    public static int requeteVerifKeyClient(String clefVerif,String clefClient) throws ClassNotFoundException, SQLException
    {
        int clientID = 0;
        //
        //Requête
        String requete = "SELECT * FROM client where client.key='"+clefVerif+"'";
        ResultSet reponse= ConnexionDB.ExecuteQuery(requete);
        //Boucle sur la base
        if (reponse.next()){
            String nbCustomerKey = reponse.getString("nbCustomerKey");
            //
            if ( nbCustomerKey.equals("*") ) { // le carractere * correpond à une clef sans contrainte
                clientID = reponse.getInt("clientID");
            }else if ( ! nbCustomerKey.isEmpty() ){
                //
                clientID = reponse.getInt("clientID");
                String requeteClef = "SELECT * FROM clientCustomerKey "
                        + "WHERE clientCustomerKey.customerKey='"+clefClient+"' "
                        + "AND clientCustomerKey.clientID='"+clientID+"'  ";
                //
                ResultSet reponseClef= ConnexionDB.ExecuteQuery(requeteClef);
                if (!reponseClef.next()){
                   clientID = 0; 
                }
                //
            }
        } 
        return clientID;
    }
    
    
    /**
    * Requête permettant de recupérer le nombre de requêtes autorisées pour le client.
    * @param clientID l'identifiant du client.
    * @return nbReqAuto : le nombre de requêtes que le client peut effectuer.
    * @throws ClassNotFoundException : exception levée quand une classe n’a pas été trouvée.
    * @throws SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    */
    public static int requeteRecupReqAutoClient(int clientID) throws ClassNotFoundException, SQLException
    {
        //Requete
        String requete = "SELECT `nbReqAutorisees` FROM client WHERE `clientID`='"+clientID+"'";
        ResultSet reponse= ConnexionDB.ExecuteQuery(requete); 
        
        //Recuperation de la donnée
        int nbReqAuto = 0;
        while(reponse.next()){
            nbReqAuto = reponse.getInt("nbReqAutorisees");
        }
        return nbReqAuto;
    }
    
    
    /**
    * Requête permettant de recupérer le nombre de mois de report total autorisées pour le client.
    * @param clientID l'identifiant du client.
    * @return nbMonth : le nombre de mois que le client peut effectuer.
    * @throws ClassNotFoundException : exception levée quand une classe n’a pas été trouvée.
    * @throws SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    */
    public static int requeteRecupNbMonthDefferd(int clientID) throws ClassNotFoundException, SQLException
    {
 
        //Requete
        String requete = "SELECT `nbMonthDeferred` FROM client WHERE `clientID`='"+clientID+"'";
        ResultSet reponse= ConnexionDB.ExecuteQuery(requete); 
        
        //Recuperation de la donnée
        int nbMonth = 0;
        while(reponse.next()){
            nbMonth = reponse.getInt("nbMonthDeferred");
        }
        return nbMonth;
    }      
            
    /**
     * Renvoie l'objet clientBD qui contient les informations de l'utilisateur
     * @param key
     * @param customerKey
     * @return
     * @throws SQLException 
     */
    public static ClientDB getCustomerVersion( String key,String customerKey) throws SQLException {
        //
        ClientDB InfoClient = new ClientDB();
        //
        if (!key.equals("")) {
            //Requete
            String requete = "SELECT * " +
                                "FROM client  " +
                                "WHERE client.key = '"+key+"' ";

            ResultSet reponse= ConnexionDB.ExecuteQuery(requete); 
            //

            //
            //Recuperation de la donnée
            if(reponse.next()){
                //
                InfoClient.setAllFromResultSet(reponse);
                if (InfoClient.getClientId() <= 0) {
                    //
                    // CLient non trouvé dans la bdd
                    InfoClient.setCustomerVersion(clefClientNonvalide);
                } else {
                    InfoClient.derniereUtilisation = new UtilisationClientDB();
                    InfoClient.derniereUtilisation.setAllFromLast(InfoClient.clientId);
                    //
                    String nbCustomerKey = InfoClient.getNbCustomerKey();
                    //
                    if ( nbCustomerKey.equals("*") ) { // le carractere * correpond à une clef sans contrainte

                    }else if (!nbCustomerKey.isEmpty() && customerKey != null && !customerKey.isEmpty() ){
                        // si vide on retourne -2
                        // dans ce cas la clef client ne doit pas etre vide
                        ArrayList<clientCustomerKey> arrCCK =clientCustomerKey.getAllClientCutomerKeyFromClientID(InfoClient.clientId);
                        boolean bFound = false; 

                        /**
                         * Boucle complete pas top mais ca ira pour l'instant
                         */
                        for (clientCustomerKey objCCK : arrCCK) {
                            if (objCCK.getCustomerKey().equals(customerKey) ){
                                bFound = true;
                            }
                        }
                        // si les clef ne sont pas egales et que l'on peut encore en ajouter
                        if (!bFound && Integer.valueOf(nbCustomerKey)> arrCCK.size() ) {
                            //
                            clientCustomerKey.insertClientCutomerKey(InfoClient.clientId,customerKey);

                            //
                        }else if (!bFound ) {
                            InfoClient.setCustomerVersion(clefClientNonvalide);
                        }

                        //
                    }else{
                        // la clef n'est pas valide
                        InfoClient.setCustomerVersion(clefClientNonvalide);
                    }
                }

            }
        }
        
        return InfoClient;
    }
    
    /**
     * met a jour l'objet à partir des champs de la BD
     * @param reponse 
     */
    void setAllFromResultSet(ResultSet reponse){
        
        try {
            //
            this.setClientId(reponse.getInt("clientId"));
            this.setCustomerVersion(reponse.getInt("CustomerVersion"));
            this.setCustomer_key(reponse.getString("customer_key"));
            this.setKey(reponse.getString("key"));
            //
            try {
                this.setKey_dateExpiration(reponse.getDate("key_dateExpiration"));
            }catch (SQLException ex) {
                // il se peut que la date soit vide => évite un bug
                Logger.getLogger(ClientDB.class.getName()).log(Level.SEVERE, null, ex);   
            }
            //
            this.setNbReqAutorisees(reponse.getInt("nbReqAutorisees"));
            this.setNbMonthDeferred(reponse.getInt("NbMonthDeferred"));
            this.setNbCustomerKey(reponse.getString("nbCustomerKey"));
            this.setNom("nom");
            //
        } catch (SQLException ex) {
            Logger.getLogger(ClientDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
       
    
    /**
     * 
     * @param wsID Integer
     */
    public void setPrixClientDispo(Integer wsID){
        arrCustomerPrice.add(wsID);
    }
    
}

package DB;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
* Nom de classe : UtilisationClientDB
* <br>
* Description : Classe découlant de la table "utilisation_client" de la base de données.
* <br>
* Date de la dernière modification : 07/08/2014
* 
* @author Stagiaire (Florence Giraud)
*/
public class UtilisationClientDB {
    
    
    /*************************
    * Attributs
    **************************/
    
    
    /**
    * L'identifiant de la classe "utilisation_client".
    * @see UtilisationClientDB#getIdUtilisationClient() 
    * @see UtilisationClientDB#setIdUtilisationClient(int) 
    */
    private int idUtilisationClient;

    /**
    * L'identifiant du client.
    * @see UtilisationClientDB#getClientID() 
    * @see UtilisationClientDB#setClientID(int) 
    */
    private int clientID;
    
    /**
    * La date correspondant aux crédits restants.
    * @see UtilisationClientDB#getMoisAnnee() 
    * @see UtilisationClientDB#setMoisAnnee(java.sql.Date) 
    */
    private Date moisAnnee;
    
    /**
    * L'ensemble des crédits restant sur le mois en cours.
    * @see UtilisationClientDB#getCreditRestant() 
    * @see UtilisationClientDB#setCreditRestant(int) 
    */
    private int creditRestant;
    
    private int creditInitial;
    
    private int requeteOctopart;
            
    private int requeteRS;
    
    private int requeteFarnell;
    
    /*************************
    * Constructeurs
    ************************/
    
    
    /**
    * Constructeur vide de la classe UtilisationClientDB.
    */
    public UtilisationClientDB() {
    }
    
    
    /*************************
    * GETTERS
    ************************/
    
    
    /**
    * Retourne la valeur de l'attribut "idUtilisationClient".
    * @return idUtilisationClient : l'identifiant de la ligne dans la table "utilisation_client".
    */
    public int getIdUtilisationClient() {
        return idUtilisationClient;
    }

    /**
    * Retourne la valeur de l'attribut "clientID".
    * @return clientID : l'identifiant du client.
    */
    public int getClientID() {
        return clientID;
    }

    /**
    * Retourne la valeur de l'attribut "moisAnnee".
    * @return moisAnnee : la date correspondant aux crédits restants.
    */
    public Date getMoisAnnee() {
        return moisAnnee;
    }

    /**
    * Retourne la valeur de l'attribut "creditRestant".
    * @return creditRestant : le nombre de crédits restants pour le mois en cours.
    */
    public int getCreditRestant() {
        return creditRestant;
    }
    
    
    /*************************
    * SETTERS
    ************************/
    
    
    /**
    * Met à jour l'identifiant de la ligne appartant à la table "utilisation_client". 
    * @param idUtilisationClient le nouvel identifiant de la ligne. 
    */
    public void setIdUtilisationClient(int idUtilisationClient) {
        this.idUtilisationClient = idUtilisationClient;
    }

    /**
    * Met à jour l'identifiant du client.
    * @param clientID le nouvel identifiant du client.
    */
    public void setClientId(int clientID) {
        this.clientID = clientID;
    }

    /**
    * Met à jour la date correspondante aux crédits.
    * @param moisAnnee la nouvelle date liée aux crédits.
    */
    public void setMoisAnnee(Date moisAnnee) {
        this.moisAnnee = moisAnnee;
    }

    /**
    * Met à jour le nombre de crédits restants.
    * @param creditRestant la nouvelle quantité de crédits restants.
    */
    public void setCreditRestant(int creditRestant) {
        this.creditRestant = creditRestant;
    }
    
    
    /*************************
    * METHODES PUBLIQUES
    ************************/
    
    
    /**
    * Permet de récupérer le nombre de crédits restants au client durant le mois en cours.
    * @param clientID l'identifiant du client.
    * @return le nombre de crédits restants.
    * @throws java.sql.SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    * @throws java.lang.ClassNotFoundException : exception levée quand une classe n’a pas été trouvée.
    */
    public static int requeteNbCreditRestant (int clientID) throws SQLException, ClassNotFoundException
    {
        //Nombre de requetes utilisées par le client dans le mois en cours
        int nbReqRestantes = 0;
        
        //On verifie qu'une entrée pour le mois et l'année en cours existe
        Date date = null;
        String requeteDate = "SELECT `mois_annee` FROM utilisation_client WHERE `clientID`='"+clientID+"' AND YEAR(`mois_annee`)=YEAR(CURRENT_DATE) AND MONTH(`mois_annee`)=MONTH(CURRENT_DATE)";
        ResultSet reponseDate= ConnexionDB.ExecuteQuery(requeteDate); 
        while(reponseDate.next()){
            date = reponseDate.getDate("mois_annee");
        }
        
        //si aucune instance dans le mois en cours, on la crée
        if (date == null)
        {
            int credit = ClientDB.requeteRecupReqAutoClient(clientID);
            int nNbReportMois = ClientDB.requeteRecupNbMonthDefferd(clientID);
            //
            if (nNbReportMois == 0) {
                //
                // Interruption du service, le client n'aura pas le droit au reset de son compteur requete
                credit = 0;
                //
            }else if (nNbReportMois == 1){
                //
                // Mode par défaut, chaque début de mois on remet le compteur requête au max de ce qu'il est autorisé.
                //credit = credit; // histoire d'écrire une ligne
                //
            }else {
                //
                // Cas option report sur n mois max
                // On récupère le nombre de request restant du dernier mois et on ajoute le crédit en se limitant à un crédit équivalent 
                // a (Nb mois report max * credit par mois)
                String requeteCreditNonUtilisee = "SELECT `credit_restant`  FROM utilisation_client WHERE `clientID`="+clientID + " ORDER BY `mois_annee` DESC";
                ResultSet reponseCreditNotUsed= ConnexionDB.ExecuteQuery(requeteCreditNonUtilisee); 
                int OldCredit =0;
                if ( reponseCreditNotUsed.first() ) {
                    //
                    // Premier enregistrement = plus recent 
                    OldCredit= reponseCreditNotUsed.getInt("credit_restant");
                    //
                    if ( OldCredit < 0) {
                        OldCredit = 0;
                    }
                    //
                    // Si pour une raison x ou y WQ n'est pas utilisé pendant un ou plusieurs mois, on ne va pas cumuler les requêtes des mois sans utilisation
                }
                //
                int maxRequete = (credit * nNbReportMois);
                credit += OldCredit;
                //
                if (credit > maxRequete) {
                    credit = maxRequete;
                }
                //
            }
            ConnexionDB.ExecuteUpdate("INSERT INTO utilisation_client (`clientID`,`mois_annee`,`credit_restant`,`credit_initial`) VALUES ("+clientID+",CURRENT_DATE,"+credit+","+credit+")"); 
            
        }
        
        
     //Requete (on cherche à recuperer les entrées pour le mois et l'année en cours)
        String requete = "SELECT `credit_restant` FROM utilisation_client WHERE `clientID`='"+clientID+"' AND YEAR(`mois_annee`)=YEAR(CURRENT_DATE) AND MONTH(`mois_annee`)=MONTH(CURRENT_DATE)";
        ResultSet reponse= ConnexionDB.ExecuteQuery(requete); 
        while(reponse.next()){
            nbReqRestantes = reponse.getInt("credit_restant");
        }
        return nbReqRestantes;
    }
    
    
    /**
    * Permet de savoir si oui ou non le client dispose d'un nombre suffisant de crédits pour que l'interrogation puisse être effectuée. 
    * @param clientID l'identifiant du client.
    * @param nbReq le nombre de requetes que le client souhaite effectuer.
    * @return un boolean indiquant si oui ou non le client peut faire sa recherche.
    * @throws java.sql.SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    * @throws java.lang.ClassNotFoundException : exception levée quand une classe n’a pas été trouvée.
    */
    public static boolean requeteVerifCredit(int clientID, int nbReq) throws SQLException, ClassNotFoundException
    {
        //Si TRUE : crédits suffisants, si FALSE : crédits insuffisants
        boolean requetePermise = false;
        
        int nbReqRestantes = UtilisationClientDB.requeteNbCreditRestant(clientID);
    
        //s'il reste suffisament de crédits, la requete est autorisée
        if (nbReqRestantes >= nbReq){
            requetePermise = true;
        }
        return requetePermise;
    }
    
    
    /**
    * Permet de retirer des crédits suivant le nombre de requetes effectuées.
    * @param clientID l'identifiant du client ayant effectué la requête.
    * @param nbReq le nombre de requêtes effectuées.
    * @return true si l'update a réussie.
    * @throws SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    * @throws ClassNotFoundException : exception levée quand une classe n’a pas été trouvée.
    */
    public static boolean requeteSuppCredit(int clientID, int nbReq) throws SQLException, ClassNotFoundException{
        //     
        //modification du crédit restant 
        ConnexionDB.ExecuteUpdate("UPDATE utilisation_client SET `credit_restant` = GREATEST(`credit_restant` - "+nbReq+",0) WHERE `clientID`='"+clientID+"' AND YEAR(`mois_annee`)=YEAR(CURRENT_DATE) AND MONTH(`mois_annee`)=MONTH(CURRENT_DATE)");
        return true;
    }
    
    
    /**
    * Permet de retirer des crédits suivant le nombre de requetes effectuées.
    * @param clientID l'identifiant du client ayant effectué la requête.
    * @param nbReq le nombre de requêtes effectuées.
    * @return true si l'update a réussie.
    * @throws SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    */
    public static boolean requeteAjouteREQOcto(int clientID, int nbReq) throws SQLException{
        //
        //connexion à la BD
        ConnexionDB.ExecuteUpdate("UPDATE utilisation_client SET `requeteOctopart` = `requeteOctopart` + "+nbReq+" WHERE `clientID`='"+clientID+"' AND YEAR(`mois_annee`)=YEAR(CURRENT_DATE) AND MONTH(`mois_annee`)=MONTH(CURRENT_DATE)");
        return true;
    }   
    
    /**
    * Permet de retirer des crédits suivant le nombre de requetes effectuées.
    * @param clientID l'identifiant du client ayant effectué la requête.
    * @throws SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    */
    public void setAllFromLast(int clientID) throws SQLException{
        //
        //connexion à la BD
        String requete = "SELECT * "
                        + " FROM utilisation_client "
                        + " WHERE `clientID`='"+clientID+"' AND YEAR(`mois_annee`)=YEAR(CURRENT_DATE) AND MONTH(`mois_annee`)=MONTH(CURRENT_DATE)";
       
        ResultSet reponse = ConnexionDB.ExecuteQuery(requete);
        
        if (reponse.next()){
            this.clientID = reponse.getInt("clientID");
            this.creditRestant = reponse.getInt("credit_restant");
            this.creditInitial = reponse.getInt("credit_initial");
            this.idUtilisationClient = reponse.getInt("id_utilisation_client");
            this.moisAnnee = reponse.getDate("mois_annee");
            this.requeteFarnell = reponse.getInt("requeteFarnell");
            this.requeteOctopart = reponse.getInt("requeteOctopart");
            this.requeteRS = reponse.getInt("requeteRS");
        }
        
    }
    
    
    
    
}

package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
//
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Nom de classe : ConnectionDB
 * <br>
 * Description : Classe permettant la connexion à la database.
 * <br>
 * Date de la dernière modification : 22/07/2014
 * 
 * @author Stagiaire (Florence Giraud)
 */
public class ConnexionDB 
{
    
    /*************************
    * Attributs
    ************************/
    
    static private Connection connexion ;
    
    /*************************
    * METHODES PUBLIQUES
    **************************/
    
    
    /**
    * Permet la connexion à la base de données.
    * @return connexion : la connexion à la base de données.
    * @throws SQLException : exception levée suite à une erreur SQL (connexion à la base/ mauvaise requête).
    * @throws ClassNotFoundException : exception levée quand une classe n’a pas été trouvée.
    */
    public static Connection getBD() throws SQLException, ClassNotFoundException 
    { 
        //A changer suivant prod ou local test
        String mode = "prod";
        //
        //
        //on vérifie que la connexion n'est pas dejà chargée
        // => la connexion ne doit pas etre nul, elle doit 
        if(mode == "local")
        {
            if (connexion == null || connexion.isClosed() || !connexion.isValid(1000)){
                   Class.forName("com.mysql.jdbc.Driver");
                   //Hebergé
                   String host = "jdbc:mysql://buymanager.cy2rizizzzzx.eu-west-1.rds.amazonaws.com:3306/buymanagerdb";
                   Properties connectionProps = new Properties();
                   connectionProps.put("user", "buymanager");
                   connectionProps.put("password", "pertilience");
                   connectionProps.put("autoReconnect", "true");

                   //Connexion
                   connexion = DriverManager.getConnection(host,connectionProps);
            }     
        }else
        {
            if (connexion == null || connexion.isClosed() || !connexion.isValid(1000)){
                
                
                Class.forName("com.mysql.jdbc.Driver");
                //Hebergé
                String host = "jdbc:mysql://buymanagerdb.cy2rizizzzzx.eu-west-1.rds.amazonaws.com:3306/buymanagerdb";
                Properties connectionProps = new Properties();
                connectionProps.put("user", "buymanager");
                connectionProps.put("password", "pertilience");
                connectionProps.put("autoReconnect", "true");

                //Connexion
                connexion = DriverManager.getConnection(host,connectionProps);
                //
                //Appel du pilote de la base de données
//                try {
//                    javax.naming.Context initContext ;
//    //                //
//                    initContext = new InitialContext();
//    //                //
//    //                // initialisation de ce contexte
//                    javax.naming.Context envContext  = (javax.naming.Context)initContext.lookup("java:/comp/env/") ;
//    //                // lecture de la datasource définie par requête JNDI
//                    DataSource ds = (DataSource)envContext.lookup("jdbc/databaseWebQuote") ;
//    //                // demande d'une connexion à cette datasource
//                    connexion = ds.getConnection();
//    //                //
//                } catch (javax.naming.NoInitialContextException ex ) {
//    //               // pas de context passe en mode manuel => seulement pour les tests
//    //               //Appel du pilote de la base de données
//
//    //                
//                } catch (NamingException ex) {
//                    Logger.getLogger(ConnexionDB.class.getName()).log(Level.SEVERE, null, ex);
//                } 
            }
        }
        return connexion;
    }
    
    /**
     * Execute une Requete SQL 
     * @param requete
     * @return
     * @throws SQLException
     */
    public static ResultSet ExecuteQuery(String requete) throws SQLException 
    {   
        try {
            //
            Connection connexionLocal = (Connection) ConnexionDB.getBD();
            java.sql.Statement instruction=connexionLocal.createStatement() ;
            
            ResultSet reponse= instruction.executeQuery(requete);
            return reponse;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnexionDB.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        //
        // perte ed connexion avec la BD ???? si pas trapper semble tuer jusqu'au main...
        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException ex){
            // on passe la connexion a null , pour forcer une reconnexion...
            connexion = null;
            Logger.getLogger(ConnexionDB.class.getName()).log(Level.SEVERE, null, ex);
            //
            return null;         
        }
    }
    
    /**
     *
     * @param requete
     * @return
     * @throws SQLException
     */
    public static int ExecuteUpdate(String requete) throws SQLException 
    {   
        try {
            Connection connexionLocal = (Connection) ConnexionDB.getBD();
            java.sql.Statement instruction=connexionLocal.createStatement() ;
        
            return instruction.executeUpdate(requete); 

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnexionDB.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException ex){
            // on passe la connexion a null , pour forcer une reconnexion...
            connexion = null;
            Logger.getLogger(ConnexionDB.class.getName()).log(Level.SEVERE, null, ex);
            //
            return 0;         
        }
    }
    
}

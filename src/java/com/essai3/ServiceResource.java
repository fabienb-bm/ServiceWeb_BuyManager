package com.essai3;

import POJO.SearchResult;
import JSON.Json;
import WS.InterfaceWSInterrogeable;
import DB.WebserviceDB;
import DB.WsClientDB;
import DB.UtilisationClientDB;
import DB.ip_listDB;
import DB.ClientDB;
import DB.ConnexionDB;
import POJO.Source;
import WS.Arrow;
import WS.Digikey;
import WS.Farnell;
import WS.FindChips;
import WS.Mouser;
import WS.MyArrow;
import WS.Rs;
import WS.TME;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONException;

/**
 * Nom de classe : ServiceResource
 * <br>
 * Description : REST Web Service.
 * <br>
 * Date de la dernière modification : 08/08/2014
 * 
* @author Stagiaire (Florence Giraud)
 */
@Path("API")
public class ServiceResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ServiceResource
     */
    public ServiceResource() {
    }

    /**
     * Permet, à partir d'une liste de MPN (composée d'un ou plusieurs MPNs), de
     * recupérer toutes les infos des produits.
     *
     * @param key la clé du client.
     * @param listeMpn la liste sous forme de String de tous les MPNs à
     * rechercher.
     * @param listWS liste des web services à interroger
     * @param cusKey
     * @param optionPrix le choix de l'affichage des prix.
     * @param request
     * @param optionDS
     * @param qtyPrediction
     * @param optionSpec Choix pour agrégation des spécifications
     * @return - un Json des resultats.
     * @throws SQLException : exception levée suite à une erreur SQL (connexion
     * à la base/ mauvaise requête).
     * @throws ClassNotFoundException : exception levée quand une classe n’a pas
     * été trouvée.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     * @throws IOException : exception levée suite à un problème sur une entrée
     * ou une sortie (mauvais format).
     * @throws ParseException : exception levée suite à une erreur survenue
     * lorsque du « parse » du JSON (problème de sérialisation).
     */
    @GET
    @Produces("application/json")
    @Path("getMPNs")
    public String getMPNs(
            @QueryParam("tabMpn") final String listeMpn,
            @QueryParam("key") final String key, //1411:Octo/Farnell/Rs 2525:Octo/Farnell,            
            @DefaultValue("") @QueryParam("cusKey") final String cusKey,
            @DefaultValue("") @QueryParam("listWS") final String listWS,
            @DefaultValue("false") @QueryParam("option") final String optionPrix,
            @DefaultValue("false") @QueryParam("optionSpec") final String optionSpec,
            @DefaultValue("false") @QueryParam("optionDS") final String optionDS,
            @DefaultValue("") @QueryParam("qty") final String qtyPrediction,
            @Context HttpServletRequest request) throws SQLException, ClassNotFoundException, JSONException, IOException, ParseException {
        //verification que la key est la bonne et donc que le client existe
        int clientID = ClientDB.requeteVerifKeyClient(key, cusKey);

        //Si le client n'existe pas
        if (clientID == 0) {
            return "Erreur sur la clef";
        } else {
            //
            // log l'ip du client 
            ip_listDB.insertLog(request, clientID, "getMPNs");
            /////////////////////////////////////
            //RECUPERATION DES MPNs
            ///////////////////////////////////// 

            //Transformation de la liste de MPNs en tableau avec le séparateur "|#|"
            String[] tabMpn = listeMpn.split("\\|\\|");
            String[] tabQty = qtyPrediction.split("\\|\\|");

            /////////////////////////////////////
            //CREATION DU TABLEAU FORMANT LE JSON
            ///////////////////////////////////// 
            //on crée le tableau (futur Json) avec chaque MPN
            ArrayList<SearchResult> sourcesListe = new ArrayList<SearchResult>();
            //nb de MPNs demandés
            int nbMPNs = tabMpn.length;
            for (int i = 0; i < nbMPNs; i++) {
                if (tabMpn[i] != null) {
                    //cree un un object searchResult pour chaque mpn trouvé et associe le mpn de la requete au mpn orginal de cet objet
                    SearchResult sourcesResultat = new SearchResult();
                    String ref = tabMpn[i];
                    sourcesResultat.setMpnOriginal(ref);
                    //
                    // Ajout de la quantité pour le calcul du prix prévisionel
                    if (i < tabQty.length && tabQty[i] != null && tabQty[i].isEmpty() == false) {
                        //                              
                        sourcesResultat.setQtyPrediction(Double.valueOf(tabQty[i]));
                        //
                    }
                    //
                    sourcesListe.add(sourcesResultat);
                }
            }

            ////////////////////////
            //REMPLISSAGE DU TABLEAU
            ///////////////////////
            //Si le client existe :
            //optionParam vaut true si option==true sinon vaut false
            boolean optionP = "true".equals(optionPrix);
            boolean optionS = "true".equals(optionSpec);
            boolean optionDataS = "true".equals(optionDS);

            //on vérifie pour quelles APIs, le client a les droits et on fait une liste de ces APIs
            ArrayList<InterfaceWSInterrogeable> listeWsToGetAccess =  this.getAccessToken(clientID,listWS) ;
            
            //Test des apis pour vérifier si nous y avons accès : Exemple = Test les access token pour digikey
            for(int indiceWS = 0; indiceWS < listeWsToGetAccess.size() ;indiceWS++)
            { 
                String nameWS = listeWsToGetAccess.get(indiceWS).getNameWS();
                if(nameWS.equals("Digikey")){
                    boolean tokenOk = listeWsToGetAccess.get(indiceWS).getErrorStatus();                 
                }
            }
            
            //on vérifie pour quelles APIs, le client a les droits et on fait une liste de ces APIs
            ArrayList<InterfaceWSInterrogeable> listeWs = WsClientDB.creationListeWs(clientID, nbMPNs,listWS);
            
            
            //Appel de la fonction permettant le remplissage du tableau
            ArrayList<SearchResult> tabSourcesListe = this.getTableauComplet(listeWs, sourcesListe, clientID, optionP, optionS, optionDataS, nbMPNs);
            
            
            for (SearchResult resultParcours : sourcesListe) {
                //
                for ( Source sourceParcours : resultParcours.getTabSource() ) {
                    //
                    sourceParcours.initPrediction(resultParcours.getQtyPrediction());
                    //
                }
                //
            }
            ////////////////////////////
            //SERIALISATION DU TABLEAU
            ////////////////////////////
            //serialisation des résultats
            String resultats = Json.ToJsonArrSearchResult(tabSourcesListe, optionP, optionS, optionDataS);

            return resultats;

        }
    }

    /**
     * Permet, à partir d'une liste de MPN (composée d'un ou plusieurs MPNs), de
     * recupérer toutes les infos des produits.
     *
     * @param listeSku
     * @param key la clé du client.
     * @param cusKey
     * @param listWS liste des web services à interroger
     * @param serviceId
     * @param option le choix de l'affichage des prix.
     * @param request
     * @param optionDS
     * @param qtyPrediction
     * @param optionSpec
     * @return - un Json des resultats.
     * @throws SQLException : exception levée suite à une erreur SQL (connexion
     * à la base/ mauvaise requête).
     * @throws ClassNotFoundException : exception levée quand une classe n’a pas
     * été trouvée.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     * @throws IOException : exception levée suite à un problème sur une entrée
     * ou une sortie (mauvais format).
     * @throws ParseException : exception levée suite à une erreur survenue
     * lorsque du « parse » du JSON (problème de sérialisation).
     */
    @GET
    @Produces("application/json")
    @Path("getSKUs")
    public String getSKUs(
            @QueryParam("tabSku") final String listeSku,
            @QueryParam("key") final String key, //1411:Octo/Farnell/Rs 2525:Octo/Farnell
            @DefaultValue("") @QueryParam("cusKey") final String cusKey,
            @DefaultValue("") @QueryParam("listWS") final String listWS,
            @DefaultValue("0") @QueryParam("service") final int serviceId, // rs par defaut
            @DefaultValue("false") @QueryParam("option") final String option,
            @DefaultValue("false") @QueryParam("optionSpec") final String optionSpec,
            @DefaultValue("false") @QueryParam("optionDS") final String optionDS,
            @DefaultValue("") @QueryParam("qty") final String qtyPrediction,
            @Context HttpServletRequest request) throws SQLException, ClassNotFoundException, JSONException, IOException, ParseException, Exception {
        //verification que la key est la bonne et donc que le client existe
        int clientID = ClientDB.requeteVerifKeyClient(key, cusKey);

        //Si le client n'existe pas
        if (clientID == 0) {
            return "Erreur sur la clef";
        }
        //
        ip_listDB.insertLog(request, clientID, "getSKUs");
        //
        //Transformation de la liste de MPNs en tableau avec le séparateur "|#|"
        String[] tabSku = listeSku.split("\\|\\|");
        String[] tabQty = qtyPrediction.split("\\|\\|");
        //
        //on crée le tableau (futur Json) avec chaque MPN
        ArrayList<SearchResult> sourcesResultSku = new ArrayList<SearchResult>();
        //nb de MPNs demandés
        int nbMPNs = tabSku.length;
        for (int i = 0; i < nbMPNs; i++) {
            if (tabSku[i] != null) {
                SearchResult sourcesResultat = new SearchResult();
                String sku = tabSku[i];
                sourcesResultat.setSkuOriginal(sku);
                
                
                // Ajout de la quantité pour le calcul du prix prévisionel
                if (i < tabQty.length && tabQty[i] != null && tabQty[i].isEmpty() == false ) {
                    //                              
                    sourcesResultat.setQtyPrediction(Double.valueOf(tabQty[i]));
                    //
                }
                sourcesResultSku.add(sourcesResultat);
            }
        }

        //Si le client existe :
        //optionParam vaut true si option==true sinon vaut false
        boolean optionP = "true".equals(option);
        boolean optionS = "true".equals(optionSpec);
        boolean optionDataS = "true".equals(optionDS);
        //
        ArrayList<InterfaceWSInterrogeable> listeWs = new ArrayList<InterfaceWSInterrogeable>();
        if (serviceId == 0) {
            //on vérifie pour quelles APIs, le client a les droits et on fait une liste de ces APIs
            listeWs = WsClientDB.creationListeWs(clientID, tabSku.length,listWS);
        } else {
            listeWs.add(WsClientDB.getWsFromWebserviceID(clientID, serviceId));
        }

        //Contre performant d'utiliser les threads si un seul WebService est à appeler
        if (listeWs.size() > 1) {
            List<Future<ArrayList<SearchResult>>> futures = new ArrayList<Future<ArrayList<SearchResult>>>();
            ExecutorService executor = Executors.newFixedThreadPool(listeWs.size());
            //créé un thread pour chaque API
            for (InterfaceWSInterrogeable listeW : listeWs) {
                listeW.setParams(sourcesResultSku, optionP, optionS, optionDataS, clientID, nbMPNs, "sku");
                Future<ArrayList<SearchResult>> future = executor.submit((Callable<ArrayList<SearchResult>>) listeW);
                futures.add(future);
            }
            //recuperation des resultats
            for (Future<ArrayList<SearchResult>> f : futures) {
                try {
                    //interromp l'action si aucun résultat n'est revenu après une minute
                    ArrayList<SearchResult> renvoie = f.get();
                    if (renvoie != null) {
                        for (int i = 0; i < renvoie.size(); i++) {
                            sourcesResultSku.get(i).addTabSource(renvoie.get(i).getTabSource());
                        }
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            executor.shutdown();
        } else {
            for (InterfaceWSInterrogeable interfaceWS : listeWs) {
                //
                try {
                    interfaceWS.setParams(sourcesResultSku, optionP, optionS, optionDataS, clientID, nbMPNs, "sku");
                    interfaceWS.RecuperationSKUs(sourcesResultSku);
                    //
                    interfaceWS.enregistreCredit(clientID, tabSku.length);
                    //
                } catch (RuntimeException e) {
                    Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, e);
                }
                //
            }
        }

        //
        // Ajoute le nombre de requête consommé
        UtilisationClientDB.requeteSuppCredit(clientID, tabSku.length);
        //
        
            for (SearchResult resultParcours : sourcesResultSku) {
                //
                for ( Source sourceParcours : resultParcours.getTabSource() ) {
                    //
                    sourceParcours.initPrediction(resultParcours.getQtyPrediction());
                    //
                }
                //
            }
            
        //serialisation des résultats
        String resultats = Json.ToJsonArrSearchResult(sourcesResultSku, optionP, optionS, optionDataS);
        return resultats;
        //
    }

    /**
     * 
     * @param listeDesc liste des descriptions
     * @param key       clef de l'utilisateur
     * @param cusKey    clef client
     * @param listWS    liste des WS à utiliser
     * @param option
     * @param optionSpec
     * @param optionDS
     * @param qtyPrediction quantité de la prediction
     * @param request
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws JSONException
     * @throws IOException
     * @throws ParseException
     * @throws Exception 
     */
    @GET
    @Produces("application/json")
    @Path("getDescriptions")
    public String getDescriptions(
            @QueryParam("tabDesc") final String listeDesc,
            @QueryParam("key") final String key, //1411:Octo/Farnell/Rs 2525:Octo/Farnell
            @DefaultValue("") @QueryParam("cusKey") final String cusKey,
            @DefaultValue("") @QueryParam("listWS") final String listWS,
            @DefaultValue("false") @QueryParam("option") final String option,
            @DefaultValue("false") @QueryParam("optionSpec") final String optionSpec,
            @DefaultValue("false") @QueryParam("optionDS") final String optionDS,
            @DefaultValue("") @QueryParam("qty") final String qtyPrediction,
            @Context HttpServletRequest request) throws SQLException, ClassNotFoundException, JSONException, IOException, ParseException, Exception {
        
        
        //verification que la key est la bonne et donc que le client existe
        int clientID = ClientDB.requeteVerifKeyClient(key, cusKey);

        //Si le client n'existe pas
        if (clientID == 0) {
            return "Erreur sur la clef";
        }
        //
        ip_listDB.insertLog(request, clientID, "getDescriptions");
        //
        //Transformation de la liste de MPNs en tableau avec le séparateur "|#|"
        String[] tabDesc = listeDesc.split("\\|\\|");
        String[] tabQty = qtyPrediction.split("\\|\\|");
        //
        //on crée le tableau (futur Json) avec chaque MPN
        ArrayList<SearchResult> sourcesResultDesc = new ArrayList<SearchResult>();
        //nb de MPNs demandés
        int nbDescs = tabDesc.length;
        for (int i = 0; i < nbDescs; i++) {
            if (tabDesc[i] != null) {
                SearchResult sourcesResultat = new SearchResult();
                String Desc = tabDesc[i];
                sourcesResultat.setDescOriginal(Desc);
                
                // Ajout de la quantité pour le calcul du prix prévisionel
                if (i < tabQty.length && tabQty[i] != null && tabQty[i].isEmpty() == false ) {
                    //                              
                    sourcesResultat.setQtyPrediction(Double.valueOf(tabQty[i]));
                    //
                }
                
                sourcesResultDesc.add(sourcesResultat);
            }
        }

        //Si le client existe :
        //optionParam vaut true si option==true sinon vaut false
        boolean optionP = "true".equals(option);
        boolean optionS = "true".equals(optionSpec);
        boolean optionDataS = "true".equals(optionDS);
        //
        //on vérifie pour quelles APIs, le client a les droits et on fait une liste de ces APIs
        ArrayList<InterfaceWSInterrogeable> listeWs = WsClientDB.creationListeWs(clientID, tabDesc.length,listWS);
 
        //Contre performant d'utiliser les threads si un seul WebService est à appeler
        // Pour le moment il ne devrait y avoir que octopart...
        if (listeWs.size() > 1) {
            List<Future<ArrayList<SearchResult>>> futures = new ArrayList<Future<ArrayList<SearchResult>>>();
            ExecutorService executor = Executors.newFixedThreadPool(listeWs.size());
            //créé un thread pour chaque API
            for (InterfaceWSInterrogeable listeW : listeWs) {
                listeW.setParams(sourcesResultDesc, optionP, optionS, optionDataS, clientID, nbDescs, "desc");
                Future<ArrayList<SearchResult>> future = executor.submit((Callable<ArrayList<SearchResult>>) listeW);
                futures.add(future);
            }
            //recuperation des resultats
            for (Future<ArrayList<SearchResult>> f : futures) {
                try {
                    //interromp l'action si aucun résultat n'est revenu après une minute
                    ArrayList<SearchResult> renvoie = f.get();
                    if (renvoie != null) {
                        for (int i = 0; i < renvoie.size(); i++) {
                            sourcesResultDesc.get(i).addTabSource(renvoie.get(i).getTabSource());
                        }
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            executor.shutdown();
        } else {
            for (InterfaceWSInterrogeable interfaceWS : listeWs) {
                //
                try {
                    interfaceWS.setParams(sourcesResultDesc, optionP, optionS, optionDataS, clientID, nbDescs, "desc");
                    interfaceWS.RecuperationDesc(sourcesResultDesc);
                    //
                    interfaceWS.enregistreCredit(clientID, tabDesc.length);
                    //
                } catch (RuntimeException e) {
                    Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, e);
                }
                //
            }
        }

        //
        // Ajoute le nombre de requête consommé
        UtilisationClientDB.requeteSuppCredit(clientID, tabDesc.length);
        //
        for (SearchResult resultParcours : sourcesResultDesc) {
                //
                for ( Source sourceParcours : resultParcours.getTabSource() ) {
                    //
                    sourceParcours.initPrediction(resultParcours.getQtyPrediction());
                    //
                }
                //
            }
        
        //serialisation des résultats
        String resultats = Json.ToJsonArrSearchResult(sourcesResultDesc, optionP, optionS, optionDataS);
        return resultats;
        //
    }
    
    /**
     * Permet de compléter le tabSourcesListe passé en paramètre avec les
     * résultats des mpns de sourcesListe.
     *
     * @param listeWs liste des Services Web à interroger.
     * @param sourcesListe tableau des résultats (dans l'optique de le
     * sérialiser puis envoyer au logiciel).
     * @param clientID l'identifiant du client effectuant la requête.
     * @param optionPrix boolean indiquant le choix du client sur l'affichage du
     * prix.
     * @param optionSpec boolean indiquand si on agrége les spécifications
     * @param optionDS
     * @param nbReqEffectuees nombre de MPNs interrogés.
     * @return tabSourcesListe.
     * @throws SQLException : exception levée suite à une erreur SQL (connexion
     * à la base/ mauvaise requête).
     * @throws ClassNotFoundException : exception levée quand une classe n’a pas
     * été trouvée.
     */
    public ArrayList<SearchResult> getTableauComplet(ArrayList<InterfaceWSInterrogeable> listeWs,
            ArrayList<SearchResult> sourcesListe, int clientID, boolean optionPrix, boolean optionSpec, boolean optionDS, int nbReqEffectuees)
            throws SQLException, ClassNotFoundException {
        //Contre performant d'utiliser les threads si un seul WebService est appelé
        if (listeWs.size() > 1) {
            List<Future<ArrayList<SearchResult>>> futures = new ArrayList<Future<ArrayList<SearchResult>>>();
            ExecutorService executor = Executors.newFixedThreadPool(listeWs.size());
            //créé un thread pour chaque API
            for (InterfaceWSInterrogeable listeW : listeWs) {
                listeW.setParams(sourcesListe, optionPrix, optionSpec, optionDS, clientID, nbReqEffectuees, "mpn");
                Future<ArrayList<SearchResult>> future = executor.submit((Callable<ArrayList<SearchResult>>) listeW);
                futures.add(future);
            }
            //recuperation des resultats
            for (Future<ArrayList<SearchResult>> f : futures) {
                try {
                    ArrayList<SearchResult> renvoie = f.get();
                    if (renvoie != null) {
                        for (int i = 0; i < renvoie.size(); i++) {
                            sourcesListe.get(i).addTabSource(renvoie.get(i).getTabSource());
                        }
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            executor.shutdown();
        } else {
            for (InterfaceWSInterrogeable listeW : listeWs) {
                try {
                    listeW.setParams(sourcesListe, optionPrix, optionSpec, optionDS, clientID, nbReqEffectuees, "mpn");
                    listeW.RecuperationMPNs(sourcesListe);
                    //
                    // comptage specifique
                    listeW.enregistreCredit(clientID, nbReqEffectuees);
                    //
                    //modification du rang Levenshtein
                    for (SearchResult sourcesListe1 : sourcesListe) {
                        sourcesListe1.calculLevenshtein();
                    }
                } catch (RuntimeException e) {
                    Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
        ///////////////////////////////
        //RETIRER LES CREDITS
        //////////////////////////////
        //On enleve le nombre de requetes effectuées au crédit restant (un credit pour chaque MPN questionné )
        UtilisationClientDB.requeteSuppCredit(clientID, nbReqEffectuees);

        return sourcesListe;
    }

    /**
     * Permet de définir à quel WS le client a les droits et leurs priorités.
     *
     * @param key : identifiant du client
     * @param cusKey
     * @return : Renvoie un Json formé de couples Ws-Prio. Si le WS n'est pas
     * dans le Json alors le client n'a pas les droits (absent de la BD OU
     * interroParDefaut égal à faux). Les prios de zéro sont incluses dans le
     * JSON.
     * @throws java.lang.ClassNotFoundException : exception levée quand une
     * classe n’a pas été trouvée.
     * @throws java.sql.SQLException : exception levée suite à une erreur SQL
     * (connexion à la base/ mauvaise requête).
     */
    @GET
    @Produces("application/json")
    @Path("getWsDroit")
    public String getWsDroit(
            @QueryParam("key") final String key,
            @DefaultValue("") @QueryParam("cusKey") final String cusKey
    ) throws ClassNotFoundException, SQLException,JSONException {
        //création de la liste de tous les WS dont le client est autorisé à consulter avec leurs priorités
        ArrayList<WsPrioClass> wsPrioListe = new ArrayList<WsPrioClass>();

        //verification que la key est la bonne et donc que le client existe
        int clientID = ClientDB.requeteVerifKeyClient(key, cusKey);

        //Si le client existe
        if (clientID != 0) {

            //récupération des Ids de tous les SWs
            ArrayList<Integer> tabSwId = WebserviceDB.requeteRecupTousLesIdsWs();

            for (int i = 0; i < tabSwId.size(); i++) {
                int wsID = tabSwId.get(i);

                //Verifie que le client a les droits pour chacun des SW existants (si le WS est lié à l'id du client)
                boolean droitClient = WsClientDB.requeteVerifDroitSW(clientID, wsID);

                //Si le client a les droits, vérifie que l'interroParDefaut de cet idWS est égal à vrai
                if (droitClient == true) {
                    boolean interroParDefautClient = WsClientDB.requeteVerifInterroSW(clientID, wsID);
                    WsPrioClass wsPrio = new WsPrioClass();
                    wsPrio.setWsId(wsID);
                    wsPrio.setInterroParDefaut(interroParDefautClient);
                    if (interroParDefautClient ){
                        //
                        // test du service
                        ArrayList<InterfaceWSInterrogeable> listeWs = WsClientDB.creationListeWs(clientID,0, String.valueOf(wsID));
                        if (listeWs.size() > 0  ){
                            InterfaceWSInterrogeable intWs = listeWs.get(0);
                            wsPrio.setErrorDetected(intWs.getErrorStatus());
                        }

                    }
                    wsPrioListe.add(wsPrio);
                }
            }
        }
        //Sérialise l'ensemble des couples wsId - prioWs
        //si JSON vide = client inexistant OU n'ayant aucun droit
        String wsPrioJson = Json.ToJsonWsPrioListe(wsPrioListe);
        return wsPrioJson;
    }

    /**
     * Permet de définir combien de requêtes restantes le client possède.
     *
     * @param key : identifiant du client
     * @param cusKey
     * @return : un nombre sous forme de String correspondant au nombre de
     * requetes restantes.
     * @throws java.lang.ClassNotFoundException : exception levée quand une
     * classe n’a pas été trouvée.
     * @throws java.sql.SQLException : exception levée suite à une erreur SQL
     * (connexion à la base/ mauvaise requête).
     */
    @GET
    @Produces("application/json")
    @Path("getNbReqRestantes")
    public String getNbReqRestantes(
            @QueryParam("key") final String key,
            @DefaultValue("") @QueryParam("cusKey") final String cusKey
    ) throws ClassNotFoundException, SQLException {
        String resultat = "";
        int nbReqRestantes = 0;

        //verification que la key est la bonne et donc que le client existe
        int clientID = ClientDB.requeteVerifKeyClient(key, cusKey);

        //Si le client n'existe pas
        if (clientID == 0) {
            nbReqRestantes = -1;
        } else {
            nbReqRestantes = UtilisationClientDB.requeteNbCreditRestant(clientID);
        }

        resultat += nbReqRestantes;
        return resultat;
    }

    /**
     * Permet de définir à quel WS le client a les droits et leurs priorités.
     *
     * @param key : l'identifiant du client.
     * @param swID l'identifiant du webservice.
     * @param interroParDefautParamètre : boolean indiquant quelle sera la
     * nouvelle valeur de l'attribut "interroParDefaut".
     * @param cusKey
     * @return un boolean indiquant si le changement a été effectué (true) ou si
     * celui-ci a rencontré une erreur/problème (false).
     * @throws java.lang.ClassNotFoundException : exception levée quand une
     * classe n’a pas été trouvée.
     * @throws java.sql.SQLException : exception levée suite à une erreur SQL
     * (connexion à la base/ mauvaise requête).
     */
    @GET
    @Produces("application/json")
    @Path("modifInterroParDefaut")
    public String modifInterroParDefaut(
            @QueryParam("key") final String key,
            @QueryParam("swID") final int swID,
            @QueryParam("interroDefaut") final boolean interroParDefautParamètre,
            @DefaultValue("") @QueryParam("cusKey") final String cusKey) throws ClassNotFoundException, SQLException {
        //booléen indiquant si un changement a été effectué
        boolean changement = false;

        //verification que la key est la bonne et donc que le client existe
        int clientID = ClientDB.requeteVerifKeyClient(key, cusKey);

        //Si le client existe
        if (clientID != 0) {
            //Vérifie que le couple clientID - swID existe
            boolean droitWs = WsClientDB.requeteVerifDroitSW(clientID, swID);

            // si le couple existe, récupèrer son interroParDéfaut initial
            if (droitWs == true) {
                boolean interroParDefautInitial = WsClientDB.requeteVerifInterroSW(clientID, swID);

                //Si l'interroParDéfaut initial est différent de l'interroParDefaut passé en paramètre, on le change
                if (interroParDefautInitial != interroParDefautParamètre) {
                    changement = WsClientDB.requeteModifInterroParDefaut(interroParDefautParamètre, clientID, swID);
                } else {
                    changement = true; //optimisation pour BuyManager
                }
            }
        }
        //Transformation du booléen en chaine de caractères avant le renvoi
        String reponse = "";
        reponse += changement;
        return reponse;
    }

    /**
     * Permet de modifier la priorité d'un service Web donné pour un client
     * donné.
     *
     * @param key l'identifiant du client.
     * @param customerKey
     * @return un boolean valant true si la modification a été effectuée, false
     * sinon (cas: client inexistant, ou n'ayant aucun droit ou priorité
     * identique à l'originale).
     * @throws ClassNotFoundException : exception levée quand une classe n’a pas
     * été trouvée.
     * @throws SQLException : exception levée suite à une erreur SQL (connexion
     * à la base/ mauvaise requête).
     * @throws org.codehaus.jettison.json.JSONException
     * @throws java.text.ParseException
     * @throws java.io.IOException
     */
    @GET
    @Produces("application/json")
    @Path("getClientInfo")
    public String getClientInfo(
            @QueryParam("key") final String key,
            @QueryParam("cusKey") final String customerKey
    ) throws ClassNotFoundException, SQLException, JSONException, ParseException, IOException {
        //
        ClientDB clientinfo = ClientDB.getCustomerVersion(key, customerKey);
        //
        if (clientinfo.getClientId() > 0) {
            
            ArrayList<InterfaceWSInterrogeable> listeWs = WsClientDB.creationListeWs(clientinfo.getClientId(),1,"");
            //
            for (int i = 0; i < listeWs.size(); i++) {
                WsClientDB Service = (WsClientDB) listeWs.get(i); 
                if ( Service.getCustomerPriceAvailable() ) {
                    Integer wsId = Service.getWsId();
                    //
                    clientinfo.setPrixClientDispo(wsId);
                }

            } 
        }
        
        
        // vide les clefs => pour eviter les transites...
        clientinfo.setKey("");
        clientinfo.setCustomer_key("");
        //Sérialise le client
        //si JSON vide = client inexistant OU n'ayant aucun droit
        String wsPrioJson = Json.ToJsonClientDB(clientinfo);
        //
        return wsPrioJson;
    }

    @GET
    @Produces("application/json")
    @Path("getTMENonce")
    public String getTMENonce() throws ClassNotFoundException, SQLException, JSONException, ParseException, IOException {
        //
        TME tme = new TME();
        return tme.getNonce();
       //
    }
    
    @GET
    @Produces("application/json")
    @Path("getTMEPrivateKey")
    public String getTMEPrivateKey(
            @QueryParam("IDClient") final int idClient,
            @QueryParam("tempKey") final String tempKey,
            @QueryParam("Nonce") final String nonce
    ) throws ClassNotFoundException, SQLException, JSONException, ParseException, IOException {
        //
        TME tme = new TME();
        return tme.getSaveCustumerKey(idClient, tempKey, nonce);
       //
    }
    
    @GET
    @Produces("application/json")
    @Path("getDigikeyAuthURL")
    public String getDigikeyAuthURL(){
        Digikey digi = new Digikey();
        return digi.getAuthorizationUrlToken();
    }
    
    @GET
    @Produces("application/json")
    @Path("setDigikeyAuthToken")
    public String setDigikeyAuthToken(
           @QueryParam("key") final String key,
            @QueryParam("cusKey") final String customerKey,
            @QueryParam("tempKey") final String tempKey ) 
            throws ClassNotFoundException, SQLException, JSONException, ParseException, IOException {
           
        //
        ClientDB clientinfo = ClientDB.getCustomerVersion(key, customerKey);
        //
        
        System.out.println("============================getDigikeyAuthURL============================");
        Digikey digi = new Digikey();
        return digi.getAccessToken(clientinfo.getClientId(), tempKey);
        //
    }
    
        @GET
    @Produces("application/json")
    @Path("setDigikeyrenewToken")
    public String setDigikeyrenewToken(
           @QueryParam("key") final String key,
           @QueryParam("cusKey") final String customerKey) 
           throws ClassNotFoundException, SQLException, JSONException, ParseException, IOException {
           
        //
        ClientDB clientinfo = ClientDB.getCustomerVersion(key, customerKey);
        //
        Digikey digi =  Digikey.getWsDigikeyFromWebserviceID(clientinfo.getClientId());
        if ( digi != null  ){
            return digi.getRenewAccessToken();
        }else{
            return "error";
        }
        //
    }
    
    /**
     * renvoie la liste des masters fab/four stockés dans le webgate
     *
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws JSONException
     * @throws ParseException
     * @throws IOException
     */
    @GET
    @Produces("application/json")
    @Path("getMasterList")
    public String getMasterList() throws ClassNotFoundException, SQLException, JSONException, ParseException, IOException {
        ArrayList<manufacturerMasterList> arrManufacturer = manufacturerMasterList.getAllSql();
        //
        return Json.ToJsonArrManufacturerList(arrManufacturer);
    }
    
    public static ArrayList<InterfaceWSInterrogeable> getAccessToken(int clientID,String listWS) throws SQLException
    {
        ArrayList<InterfaceWSInterrogeable> liste = new ArrayList<InterfaceWSInterrogeable>();
        String requete;
        
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
        
        ResultSet reponse = ConnexionDB.ExecuteQuery(requete);

        //Boucle sur la base de données
        while (reponse.next()) {
            WsClientDB wsClient;
            int wsID = reponse.getInt("wsID");
            //
            switch (wsID) {
                case 2:
                    wsClient = new Farnell();
                    liste.add((Farnell) wsClient);
                    break;
                case 3:
                    wsClient = new Rs();
                    liste.add((Rs) wsClient);
                    break;
                case 4:
                    wsClient = new TME();
                    liste.add((TME) wsClient);
                    break;
                case 5:
                    wsClient = new Mouser();
                    liste.add((Mouser) wsClient);
                    break;
                case 6:
                    wsClient = new Digikey();
                    liste.add((Digikey) wsClient);
                    break;
                case 7 :
                    wsClient = new Arrow();
                    liste.add((Arrow) wsClient);
                    break;
                case 8 :
                    wsClient = new MyArrow();
                    liste.add((MyArrow) wsClient);
                    break;
                case 9 :
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
        }
        return liste;
    }
     
    
}


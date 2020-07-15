/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WS;

import DB.ConnexionDB;
import DB.WsClientDB;
import POJO.Datasheet;
import POJO.Prix;
import POJO.SearchResult;
import POJO.Source;
import POJO.Specs;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.ProcessingException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.uri.UriComponent;

/**
 * Description : Permet d'interroger l'API TME
 *
 * @author François DUPONT
 */
public class TME extends WsClientDB implements InterfaceWSInterrogeable, Callable<ArrayList<SearchResult>> {

    // Login : 
    // support@pertilience.com
    //
    // Mdp :
    // Buymanager2017
    //
    // Anonymous key: 
    // 791c313858ef136cc699a1c8332e6ec00c5c16c8b1e98
    //
    // Application secret :
    // 
    
    
    
    /**
     * ***********************
     * ATTRIBUTS **********************
     */
    private static final String urlAPI = "https://api.tme.eu/";
    //clé de cryptage de la requête
    //OLD ---- >   private static final String app_secret = "282a5b9c943e5e20db16";
     private static final String app_secret = "2af28c63db9e0ff6eb58";

    //anonymous token == Clé générique
    //OLD ---- >   private static final String tokenGenerique = "613bd53963d27e0e759747ef97115a19482e9d0b10085";
    private static final String tokenGenerique = "791c313858ef136cc699a1c8332e6ec00c5c16c8b1e98";
    //token utilisé pour les requêtes après l'initialisation des clés
    private String tokenToUse = "";
    //clé privée de test
    // private static final String private_key = "41c434be5d6103fbd590aa087440bf9b272ceb6325b44eba95";

    private static final String currency = "EUR";

    private static final String language = "EN";

    private String country = "FR";

    // type de requête faîte à l'API
    private String action;

    /**
     * ***********************
     * CONSTRUCTEUR **********************
     */
    public TME() {
    }

    /**
     * ***********************
     * SETTERS **********************
     */
    private void setAction(String action) {
        this.action = action;
    }

    /**
     * ***********************
     * GETTERS **********************
     */
    private String getAction() {
        return this.action;
    }

    /**
     * Interroge l'API TME selon le choix
     *
     * @param mpns - liste de mpns à interroger
     * @param sourcesListe - liste dans laquelle les résulats seront stockés
     */
    private void interroTME(ArrayList<String> mpnsOuSkus, ArrayList<SearchResult> sourcesListe) {
        ArrayList<ArrayList<Source>> objetsSources = new ArrayList<ArrayList<Source>>();
        initKey();
        procedurePremiereInterrogation(mpnsOuSkus, objetsSources);
        //Chez TME la spécification du poids se trouve dans la première requête
        if (optionPrix) {            
            procedureAuxiliaire(objetsSources, "prix",null);
        }
        if (optionDS) {
            procedureAuxiliaire(objetsSources, "ds",null);
        }
        //Chaque index de sourcesListe correspond à un mpn demander avec les maps trouvés.
        //De même pour objetsSources.
        for (int i = 0; i < objetsSources.size(); i++) {
            sourcesListe.get(i).addTabSource(objetsSources.get(i));
        }
    }

    /**
     * Procedure permettant de boucler sur les requêtes à une entrée
     *
     * @param objetsSources - Liste à remplir
     * @param mpnsOuSkus - La liste a interroger séquentiellement
     */
    private void procedurePremiereInterrogation(ArrayList<String> mpnsOuSkus, ArrayList<ArrayList<Source>> objetsSources) {
        ClientConfig configuration = new ClientConfig();
        configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, 10000);
        configuration = configuration.property(ClientProperties.READ_TIMEOUT, 10000);
        Client client = ClientBuilder.newClient(configuration);
        Form form = new Form();
        String signature_base = null, reponseRequete = null;
        Response reponse = null;
        WebTarget webTarget;
        if ("mpn".equals(this.typeRequete)) {
            this.setAction("Products/Search.json");
            webTarget = client.target(urlAPI).path(this.getAction());
            for (String mpn : mpnsOuSkus) {
                uneInterroTME(mpn, objetsSources, signature_base, form, reponse, webTarget, reponseRequete);
            }
        } else if ("sku".equals(this.typeRequete)) {
            procedureAuxiliaire(objetsSources, "sku", mpnsOuSkus);
        }

    }

    /**
     * Procedure de recuperation polyvalente pour les requêtes avec 1 entrée
     *
     * @param objetsSources - Liste à remplir
     * @param mpnOuSku - Le numéro sur lequel la recherche est basée
     */
    private void uneInterroTME(String mpnOuSku, ArrayList<ArrayList<Source>> objetsSources,
            String signature_base, Form form, Response reponse, WebTarget webTarget, String reponseRequete) {
        try {
            signature_base = initRequete(form, mpnOuSku, null);
            form.param("ApiSignature", genererSignature(signature_base));
            reponse = webTarget.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                                    .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
            reponseRequete = reponse.readEntity(String.class);
            if (reponse.getStatus() == 200) {
                objetsSources.addAll(genererObjetsSources(reponseRequete));
            }else{
                // log l'erreur
                objetsSources.addAll( genererObjetsSources(""));
                Logger.getLogger(TME.class.getName()).warning("Réponse html : "+reponse.getStatus()); 
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex){
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProcessingException ex){
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Procedure de recuperation polyvalente pour les requêtes avec 50 entrées,
     * à paramètrer avec ce qu'on l'on souhaite rechercher
     *
     * @param objetsSources - Liste à remplir
     * @param type - Le type de requête à effectuer
     */
    private void procedureAuxiliaire(ArrayList<ArrayList<Source>> objetsSources, String type, ArrayList<String> skus) {
        ArrayList<ArrayList<String>> tranchesMpns = new ArrayList<ArrayList<String>>();
        if (type.equals("prix")) {
            this.setAction("Products/GetPricesAndStocks.json");
            //Initialisation des tranches de 50 pour les requêtes sur les prix      
            initTranches(tranchesMpns, objetsSources);
        } else if (type.equals("ds")) {
            this.setAction("Products/GetProductsFiles.json");
            //Initialisation des tranches de 50 pour les requêtes datasheets           
            initTranches(tranchesMpns, objetsSources);
        } else if (type.equals("sku")) {
            this.setAction("Products/GetProducts.json");
            //Initialisation des tranches de 50 pour les skus           
            initTranchesSKU(tranchesMpns, skus);
        }
        ClientConfig configuration = new ClientConfig();
        configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, 10000);
        configuration = configuration.property(ClientProperties.READ_TIMEOUT, 10000);
        Client client = ClientBuilder.newClient(configuration);
        WebTarget webTarget;
        webTarget = client.target(urlAPI).path(this.getAction());
        Form form = new Form();
        String signature_base, reponseAuxi;
        Response reponse;

        //Deuxième interrogation de l'API sur les prix avec mapping des données
        int[] traceurs = {0, 0};
        for (ArrayList<String> tranchesMpn : tranchesMpns) {
            try {
                signature_base = initRequete(form, null, tranchesMpn);
                form.param("ApiSignature", genererSignature(signature_base));
                reponse = webTarget.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                                    .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
                reponseAuxi = reponse.readEntity(String.class);
                if (reponse.getStatus() == 200) {
                    if (type.equals("prix")) {
                        mapping(objetsSources, reponseAuxi, traceurs, "prix");
                    } else if (type.equals("ds")) {
                        mapping(objetsSources, reponseAuxi, traceurs, "ds");
                    } else if (type.equals("sku")){
                        objetsSources.addAll(genererObjetsSources(reponseAuxi));
                    }
                }
                if (tranchesMpns.size() > 1) {
                    form = new Form();
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
            }  catch (IllegalStateException ex){
                Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ProcessingException ex){
                Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Initialise les paramètres de la requête et retourne une chaine de
     * caractères, qui une fois encrypter, sera la signature
     *
     * @param signature_base La chaine à crypter
     * @param unMon - Le mpn a envoyer en paramère pour products/search
     * @param mpns - Liste de mpns pour la seconde requete
     * @return signature La signature de la requête
     */
    private String initRequete(Form form, String unMpn, ArrayList<String> mpns) throws UnsupportedEncodingException {
        String urlEncode = URLEncoder.encode(urlAPI.concat(this.getAction()),"UTF-8");
        String signature_base = "";

        //Structure de données permettant de stocker plusieurs valeurs pour une seule clé
        String nomListParam;
        String[] val;
        int cpt = 0;
        HashMap<String, String[]> mapParametres = new HashMap<String, String[]>();
        if ("Products/Search.json".equals(this.action)) {//Pour les paramètres en string    
            nomListParam = "SearchPlain";
            val = new String[]{"true"};
            mapParametres.put("SearchWithStock", val);
            String search = unMpn;
            val = new String[]{search};
            mapParametres.put(nomListParam, val);
        } else if ("Products/GetPricesAndStocks.json".equals(this.action) || "Products/GetProductsFiles.json".equals(this.action)
                || "Products/GetProducts.json".equals(this.action)) {//Pour les parametres en array
            val = new String[mpns.size()];
            nomListParam = "SymbolList";
            //Pour un array
            for (String mpn : mpns) {
                val[cpt] = mpn;
                cpt++;
            }
            mapParametres.put(nomListParam, val);
        }
        //Cas générique, car ces paramètres sont inutiles lors des requêtes avec des clés privées clients
        if (tokenToUse.length() == 45) {
            val = new String[]{country};
            mapParametres.put("Country", val);
            val = new String[]{currency};
            mapParametres.put("Currency", val);
        }
        val = new String[]{language};
        mapParametres.put("Language", val);
        val = new String[]{tokenToUse};
        mapParametres.put("Token", val);
        //Permet d'ordonné selon l'ordre alphabétique les paramètres comme le demande TME
        Map<String, String[]> sortMapParametres = new TreeMap<String, String[]>(mapParametres);
        cpt = 0;
        int cpt2 = 0;
        //ajoute les paramètres, la requete construit également la signature à encrypter
        for (Map.Entry<String, String[]> entrerMap : sortMapParametres.entrySet()) {            
            val = entrerMap.getValue();
            if ( val.length > 1) {
                for (String valTempo : val) {
                    //
                    form.param(entrerMap.getKey() + "[" + cpt2 + "]", valTempo);
                    //
                    valTempo = UriComponent.encode(valTempo.trim(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
                    //
                    if (cpt != mapParametres.size() - 1) {
                         // crochet encode avec %5B et %5D a la mano
                        signature_base = signature_base + entrerMap.getKey() + "%5B" + cpt2 + "%5D=" + valTempo + "&";
                    } else {
                         // crochet encode avec %5B et %5D a la mano
                        signature_base = signature_base + entrerMap.getKey() + "%5B" + cpt2 + "%5D=" + valTempo;
                    }
                    cpt2++;
                }
                cpt2 = 0;
            } else if(val.length > 0 && val[0] != null) {
                if (entrerMap.getKey().equals("SymbolList")) {
                    //
                    
                    form.param(entrerMap.getKey() + "[0]", val[0]);
                    
                    val[0] = UriComponent.encode(val[0], UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
                    
                    if (cpt != mapParametres.size() - 1) {
                        // crochet encode avec %5B et %5D a la mano
                        signature_base = signature_base + entrerMap.getKey() + "%5B0%5D=" + val[0] + "&";
                    } else {
                         // crochet encode avec %5B et %5D a la mano
                        signature_base = signature_base + entrerMap.getKey() + "%5B0%5D=" + val[0];
                    }
                } else {
                    //
                    form.param(entrerMap.getKey(), val[0]);
                    //
                    val[0] = UriComponent.encode(val[0], UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
                    //
                    if (cpt != mapParametres.size() - 1) {
                        signature_base = signature_base + entrerMap.getKey() + "=" +val[0]+ "&";
                    } else {
                        signature_base = signature_base + entrerMap.getKey() + "=" + val[0];
                    }
                }
            }
            cpt++;
        }
        //
        signature_base = "POST&"+urlEncode+"&"+URLEncoder.encode(signature_base,"UTF-8");
        //
        return signature_base;
    }

    /**
     * Genère la signature de la requête à envoyer dans le paramètre
     * "ApiSignature"
     *
     * @param signature_base La chaine à crypter
     * @return signature - La signature de la requête
     */
    private String genererSignature(String signature_base) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(app_secret.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
            byte[] result = mac.doFinal(signature_base.getBytes());
            String signature = Base64.encodeBase64String(result);
            return signature;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidKeyException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Initialise les tranches pour les requêtes à 50 entrées
     *
     * @param tranchesMpns - Les arraylist de mpns à interorger
     * @param objectsSources - Pour remplir trancheMpns
     */
    private void initTranches(ArrayList<ArrayList<String>> tranchesMpns, ArrayList<ArrayList<Source>> objetsSources) {
        int cpt = 1;
        ArrayList<String> uneTranche = new ArrayList<String>();
        for (int i = 0; i < objetsSources.size(); i++) {
            for (int j = 0; j < objetsSources.get(i).size(); j++) {
                String Uid = objetsSources.get(i).get(j).getUid();
                // on fait attention a ne pas ajouter d'uid null
                if (Uid != null ){
                    uneTranche.add(Uid);
                    if (cpt == 50) {
                        cpt = 0;
                        tranchesMpns.add(uneTranche);
                        uneTranche = new ArrayList<String>();
                    }
                    cpt++;
                }
            }
        }
        if (cpt < 50) {
            tranchesMpns.add(uneTranche);
        }
    }

    /**
     * Initialise les tranches pour les requêtes à 50 entrées
     *
     * @param tranchesMpns - Les arraylist de mpns à interorger
     * @param objectsSources - Pour remplir trancheMpns
     */
    private void initTranchesSKU(ArrayList<ArrayList<String>> tranchesSkus, ArrayList<String> skus) {
        int cpt = 1;
        ArrayList<String> uneTranche = new ArrayList<String>();
        for (String sku : skus) {
            uneTranche.add(sku);
            if (cpt == 50) {
                cpt = 0;
                tranchesSkus.add(uneTranche);
                uneTranche = new ArrayList<String>();
            }
            cpt++;
        }
        if (cpt < 50) {
            tranchesSkus.add(uneTranche);
        }
    }

    /**
     * ***
     * Convertit le résulat de l'API TME en plusieurs objets sources avec
     * mapping des données
     *
     * @param signature_base La chaine à crypter
     * @return signature La signature de la requête
     */
    private ArrayList<ArrayList<Source>> genererObjetsSources(String resultatRequete) {
        try {
            ArrayList<ArrayList<Source>> listeProduits = new ArrayList<ArrayList<Source>>(); // Liste des produits de chaque requete
            ArrayList<Source> produits = new ArrayList<Source>(); // Liste des produits d'un mpn
            
            if (resultatRequete != "" ){ // si la requête est vide crée un produit vide
                String[] mpqEtMoq = new String[2];
                JSONObject jsonObject = new JSONObject(resultatRequete);
                JSONArray listeSymbol = jsonObject.getJSONObject("Data").getJSONArray("ProductList");
                //pour plusieurs mpns, créé un object contenant les mpns trouvé correspondant au mpn demandé
                for (int i = 0; i < listeSymbol.length(); i++) {
                    Source produit = new Source();
                    produit.setUid(listeSymbol.getJSONObject(i).getString("Symbol"));
                    produit.setNomFabricant(listeSymbol.getJSONObject(i).getString("Producer"));
                    produit.setNomProduit(listeSymbol.getJSONObject(i).getString("Description"));
                    produit.setUrlWs("http:" + listeSymbol.getJSONObject(i).getString("ProductInformationPage"));
                    produit.setMpn(listeSymbol.getJSONObject(i).getString("OriginalSymbol"));
                    produit.setNumberPackagingTempo(listeSymbol.getJSONObject(i).getString("SuppliedAmount"));
                    produit.setOrigine("TME");
                    mpqEtMoq[0] = listeSymbol.getJSONObject(i).getString("Multiples");
                    mpqEtMoq[1] = listeSymbol.getJSONObject(i).getString("MinAmount");
                    produit.setMpqEtMoq(mpqEtMoq);
                    if (optionSpec) {
                        Specs spec = new Specs();
                        spec.setKey("Weight");
                        spec.setValue(listeSymbol.getJSONObject(i).getString("Weight"));
                        spec.setUnit("g");
                        ArrayList<Specs> specs = new ArrayList<Specs>();
                        specs.add(spec);
                        produit.setListeSpecs(specs);
                    }
                    produits.add(produit); //Pour un mpn, ajoute les mpns trouvés correspondants  
                }
            }
            listeProduits.add(produits);
            return listeProduits;
        } catch (JSONException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * ***
     * Mapping des données selon le paramètre
     *
     * @param objetsSources - La liste des mpns à interogger
     * @param reponse - La réponse de TME c
     * @param traceurs - Permet de ne pas reparcourrir l'integralité de la
     */
    private void mapping(ArrayList<ArrayList<Source>> objetsSources, String reponse, int[] traceurs, String type) {
        try {
            JSONObject jsonObject = new JSONObject(reponse);
            JSONArray listeSymbol = jsonObject.getJSONObject("Data").getJSONArray("ProductList");
            JSONArray liste = null;
            ArrayList<Prix> prix = null;
            int trace = 0;
            // Les traceurs permettent de savoir où reprendre si plus d'une seule requête sur les prix ont été faîtes
            int k = traceurs[0];
            int n = traceurs[1];
            while (k < objetsSources.size() && trace < listeSymbol.length()) {
                while (n < objetsSources.get(k).size() && trace < listeSymbol.length()) {
                    //trouve la bonne ligne a assembler
                    while (trace < listeSymbol.length() && !listeSymbol.getJSONObject(trace).getString("Symbol").equals(objetsSources.get(k).get(n).getUid())) {
                        trace++;
                    }
                    if (trace < listeSymbol.length() && listeSymbol.getJSONObject(trace).getString("Symbol").equals(objetsSources.get(k).get(n).getUid())) {
                        if (type.equals("prix")) {
                            mappingPrix(objetsSources, prix, liste, listeSymbol, trace, k, n);
                        } else if (type.equals("ds")) {
                            mappingDatasheet(objetsSources, liste, listeSymbol, trace, k, n);
                        }
                    }
                    trace = 0;
                    n++;
                }
                if (trace == 50) {
                    traceurs[0] = k;
                    traceurs[1] = n;
                }
                n = 0;
                trace = 0;
                k++;
            }
        } catch (JSONException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * ***
     * Mapping des prix
     *
     */
    private void mappingPrix(ArrayList<ArrayList<Source>> objetsSources, ArrayList<Prix> prix, JSONArray liste, JSONArray listeSymbol, int trace, int k, int n) {
        try {
            prix = new ArrayList<Prix>();
            liste = listeSymbol.getJSONObject(trace).getJSONArray("PriceList");
            String packaging = listeSymbol.getJSONObject(trace).getString("Unit");
            for (int q = 0; q < liste.length(); q++) {
                Prix p = new Prix();
                p.setQuantite(Integer.parseInt(liste.getJSONObject(q).getString("Amount")));
                p.setPrix(Double.parseDouble(liste.getJSONObject(q).getString("PriceValue")));
                p.setDevise("EUR");
                p.setStock(Integer.parseInt(listeSymbol.getJSONObject(trace).getString("Amount")));
                //String packaging = objetsSources.get(k).get(n).getNumberPackagingTempo() + " " + listeSymbol.getJSONObject(trace).getString("Unit");
                //p.setPackaging(packaging);
                p.setMpq(Integer.parseInt(objetsSources.get(k).get(n).getMpqEtMoq()[0]));
                p.setMoq(p.getQuantite());
                p.setFournisseur("TME (direct)");
                p.setDistribValide(true);
                p.setSku(objetsSources.get(k).get(n).getUid());
                p.setStockRegions("");
                p.setPackaging(packaging);
                prix.add(p);
            }
            objetsSources.get(k).get(n).setListePrix(prix);
            objetsSources.get(k).get(n).setStock(Integer.parseInt(listeSymbol.getJSONObject(trace).getString("Amount")));
        } catch (JSONException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * ***
     * Mapping des datasheets
     *
     */
    private void mappingDatasheet(ArrayList<ArrayList<Source>> objetsSources, JSONArray listeDS, JSONArray listeSymbol, int trace, int k, int n) {
        try {
            listeDS = listeSymbol.getJSONObject(trace).getJSONObject("Files").getJSONArray("DocumentList");
            ArrayList<Datasheet> datasheet = new ArrayList<Datasheet>();
            for (int q = 0; q < listeDS.length(); q++) {
                Datasheet ds = new Datasheet();
                ds.setUrl("http:" + listeDS.getJSONObject(q).getString("DocumentUrl"));
                datasheet.add(ds);
            }
            objetsSources.get(k).get(n).setDatasheet(datasheet);
        } catch (JSONException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * ***
     * Méthode principale implémenter pour la récupération des données sur des
     * mpns
     *
     * @param sourcesListe - La liste des mpns à interroger
     */
    @Override
    public void RecuperationMPNs(ArrayList<SearchResult> sourcesListe) {
        ArrayList<String> mpns = new ArrayList<String>();
        for (SearchResult sL : sourcesListe) {
            mpns.add(sL.getMpnOriginal());
        }        
        this.interroTME(mpns, sourcesListe);
    }

    /**
     * ***
     * Méthode principale implémenter pour la récupération des données sur des
     * skus
     *
     * @param sourcesListe - La liste des skus à interroger
     */
    @Override
    public void RecuperationSKUs(ArrayList<SearchResult> sourcesListe) {
        ArrayList<String> skus = new ArrayList<String>();
        for (SearchResult sL : sourcesListe) {
            skus.add(sL.getSkuOriginal());
        }
        this.interroTME(skus, sourcesListe);
    }

    @Override
    public void enregistreCredit(int clientID, int nbCredit) {
    }

    /**
     * ***
     * Méthode appelée lors du lancement du thread
     *
     * @throws java.lang.Exception
     * @return duplicateResults - POur chaque mpn a intéroger, la listes des
     * mpns avec leurs caractéristiques trouvées.
     */
    @Override
    public ArrayList<SearchResult> call() throws Exception {
        if (this.typeRequete.equals("mpn")) {
            this.RecuperationMPNs(this.duplicateResults);
            for (SearchResult duplicateResult : duplicateResults) {
                duplicateResult.calculLevenshtein();
            }
        } else if (this.typeRequete.equals("sku")) {
            this.RecuperationSKUs(this.duplicateResults);
        }
        this.enregistreCredit(this.clientID, this.nbCredit);
        System.out.println("TME terminé");
        return duplicateResults;
    }

    /**
     * Détermine si on utilise une clé générique ou une clé client
     */
    private void initKey() {
        String privateKey = this.getKey();
        if (privateKey.isEmpty()) {
            //Cas générique
            this.tokenToUse = TME.tokenGenerique;
           //
        } else {
            //Cas où le client à une clé privée
            this.tokenToUse = privateKey;
        }
    }

    /**
     * Initialise la requête afin de générer une clé client
     */
    private String initRequeteNonce(Form form) {
        try {
            String urlEncode = URLEncoder.encode(urlAPI.concat(this.getAction()), "UTF-8");
            //signature de la requêtre prête à être hashé
            String signature_base = "POST&".concat(urlEncode).concat("&");
            
            form.param("Token", tokenToUse);
            signature_base = signature_base + URLEncoder.encode("Token=" + tokenToUse , "UTF-8");
            signature_base = signature_base.replace("+", "%20");
            signature_base = signature_base.replace("%7E", "~");
            return signature_base;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
        /**
     * Initialise la requête afin de générer une clé client
     */
    private String initRequeteCustumerKey(Form form,String TemporaryKey,String nonce) {
        try {
            String urlEncode = URLEncoder.encode(urlAPI.concat(this.getAction()), "UTF-8");
            //signature de la requêtre prête à être hashé
            String signature_base = "POST&".concat(urlEncode).concat("&");
            //      
            form.param("Nonce", nonce);
            signature_base = signature_base + URLEncoder.encode("Nonce" + "=" + nonce + "&", "UTF-8");
            //
            form.param("TempToken", TemporaryKey);
            signature_base = signature_base + URLEncoder.encode("TempToken" + "=" + TemporaryKey + "&", "UTF-8");
            //
            form.param("Token", tokenToUse);
            signature_base = signature_base + URLEncoder.encode("Token" + "=" + tokenToUse , "UTF-8");
            //
            signature_base = signature_base.replace("+", "%20");
            signature_base = signature_base.replace("%7E", "~");
            return signature_base;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    @Override
    public void RecuperationDesc(ArrayList<SearchResult> arrSearchResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getNonce() {
                
        this.tokenToUse = TME.tokenGenerique;
        this.setAction("Auth/GetNonce.json");
        ClientConfig configuration = new ClientConfig();
        configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, 10000);
        configuration = configuration.property(ClientProperties.READ_TIMEOUT, 10000);
        Client client = ClientBuilder.newClient(configuration);
        WebTarget webTarget;
        webTarget = client.target(urlAPI).path(this.getAction());
        Form form = new Form();
        String signature_base, responsePrivateKey; 
        Response reponse;
        signature_base = initRequeteNonce(form);
        form.param("ApiSignature", genererSignature(signature_base));
        reponse = webTarget.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        responsePrivateKey = reponse.readEntity(String.class);
        JSONObject rep;
        String Nonce = "";
        System.out.println("WS.TME.getNonce()"+ responsePrivateKey);
        try {
            rep = new JSONObject(responsePrivateKey);
            Nonce = rep.getJSONObject("Data").getString("Nonce");
        } catch (JSONException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Nonce;
    }
    
    public String getSaveCustumerKey(int idClient, String temporaryKey, String nonce) {
        //
        this.tokenToUse = TME.tokenGenerique;
        this.setAction("Auth/Init.json");
        ClientConfig configuration = new ClientConfig();
        configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, 10000);
        configuration = configuration.property(ClientProperties.READ_TIMEOUT, 10000);
        Client client = ClientBuilder.newClient(configuration);
        WebTarget webTarget;
        webTarget = client.target(urlAPI).path(this.getAction());
        Form form = new Form();
        String signature_base, responsePrivateKey; 
        Response reponse;
        signature_base = initRequeteCustumerKey(form,temporaryKey,nonce);
        form.param("ApiSignature", genererSignature(signature_base));
        reponse = webTarget.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        responsePrivateKey = reponse.readEntity(String.class);
        JSONObject rep;
        //
        String CustumerKey = "";
        //
        try {
            rep = new JSONObject(responsePrivateKey);
            CustumerKey = rep.getJSONObject("Data").getString("Token");
        } catch (JSONException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, null, ex);
        }
        //
        // Enregistre la cle client
        requeteSavePrivateKeyTME(CustumerKey, idClient);
        
        return "OK";
    }

    @Override
    public boolean getErrorStatus() {
      return false;
    }
    
    
    /**
     * Sauvegarde en DB la clé client TME qui vient d'être générée
     *
     * @param key
     * @param clientID
     */
    public void requeteSavePrivateKeyTME(String key, int clientID) {
        try {
            ConnexionDB.ExecuteUpdate("UPDATE ws_client SET `key`='" + key + "' WHERE `clientID`='" + clientID + "' and`wsID`='" + tmeWsId + "'");
        } catch (SQLException ex) {
            Logger.getLogger(WsClientDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getNameWS() {
        return "TME";
    }
    
}

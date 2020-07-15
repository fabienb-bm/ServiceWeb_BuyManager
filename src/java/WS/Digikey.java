/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WS;

import DB.ConnexionDB;
import DB.WsClientDB;
import static DB.WsClientDB.digikeyWsId;
import POJO.Datasheet;
import POJO.Prix;
import POJO.SearchResult;
import POJO.Source;
import POJO.Specs;
import WS.DigikeyOAuthApi.DigikeyOAuth2Service;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import java.util.concurrent.Callable;
import org.apache.tomcat.util.codec.binary.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.uri.UriComponent;


/**
 * Classe permettant d'interroger l'API Digi-Key
 * @author François DUPONT
 */
public class Digikey extends WsClientDB implements InterfaceWSInterrogeable, Callable<ArrayList<SearchResult>> {
    
    private static final int nbPoolsThreads = 6;
    
    //////
    // Compte Digikey : https://api-portal.digikey.com
    //    login : loic.maisonnasse@pertilience.com
    //    mdp : Lme198112
    //
    /// information du client rest 
    /// private key : dhY77OJLpDLyywcX
    /// Client ID : 0JBmPbpn8GGFJRMkWkNc34iqMRiUzQyy
    // code ... IuQPaR46DnwpYMRdVcaRTAZkFOsncOypTBfUkQAD ???
    /// OAuth Redirection URL :https://accountRQT.buymanager.biz/
    //
   // '{"access_token":"lSuPLv7fOoMFSPI5EqXfToKdI8qA","refresh_token":"J5s4TUsKo7prwPwGN4TRQBSVob0FQgbW8xgTsbqJHl","token_type":"Bearer","expires_in":604799}
   // '{"access_token":"D8Gpvq2s9aSLXhE9NMwnlN7fOnXe","refresh_token":"v0C9CpHgsoMxI2UyGb6SqdKUDsJHSny0pb0JCYPmIG","token_type":"Bearer","expires_in":604799}
    
    // clef priver de l'application
    private static final String privateKey = "dhY77OJLpDLyywcX";
    // clef privée de l'application buymanager test
    //private static final String privateKey = "WLZfYbGyN2HIejtH";
    // identifinat du client chez digikey pour l'appli BM
    private static final String DigikeyClientID = "0JBmPbpn8GGFJRMkWkNc34iqMRiUzQyy";
    // Product app test
    //private static final String DigikeyClientID = "e5Pp02xsQBYSAl9v2BW7Z8E0RGCZReO8";
    //
    // redirection définie dans le service BM
    private static final String oAuthRedirectionURL ="https://accountRQT.buymanager.biz/oAuthLP.php";
    //
    // token que j'ai recup la premiere fois (token du compte LME)
    private static final String access_tokenBM = "lSuPLv7fOoMFSPI5EqXfToKdI8qA";
    
    private static final int MPNSEARCH = 1;
    private static final int SKUSEARCH = 2;        
    private static final int TEXTSEARCH = 3;
        /**
     * ***********************
     * Constructeurs
     *
     * @return String **********************
     */
    @Override
    public String getKey() {
        String mykey = super.getKey(); //To change body of generated methods, choose Tools | Templates.
        /*byte[] decodePassword =  Base64.decodeBase64(super.getKey());
        mykey = new String(decodePassword);*/
        
        if (mykey.isEmpty()) {
            mykey = access_tokenBM;
        }
        return mykey;
    }
    
  
    
    /**
     * Country == X-DIGIKEY-Locale-Site
     * @return
     */
    @Override
    public String getCountry() {
        String myCountry = super.getCountry();
        //
        if ( myCountry.isEmpty()){
            myCountry = "fr"; // force le fr si pas de country
        }else{
            if (myCountry.contains("|")) {
               String[] data =  myCountry.split("\\|");
               myCountry = data[0];
            }
        }
        return myCountry;
    }
    
    /**
     * Country == X-DIGIKEY-Locale-Site | X-DIGIKEY-Locale-ShipToCountry
     * @return 
     */
    public String getShipToCountry() {
        String myCountry = super.getCountry();
        String myShipToCountry = "fr";
        //
        if ( ! myCountry.isEmpty()){

            if (myCountry.contains("|")) {
               String[] data =  myCountry.split("\\|");
               myShipToCountry = data[1];
            }else{
               myShipToCountry = myCountry;
            }
        }
        return myShipToCountry;
    }
    
    
  // on met la devise dans magasin
    /**
     * 
     * @return 
     */
    public String getDevise() {
        String myDevise = this.getMagasin();
        if ( myDevise.isEmpty() ){
            myDevise = "eur";
        }
        return myDevise;
    }
   

    @Override
    public void RecuperationMPNs(ArrayList<SearchResult> arrSearchResult) {
        procedureThreads(arrSearchResult);
    }

    @Override
    public void RecuperationSKUs(ArrayList<SearchResult> arrSearchResult) {
        procedureThreads(arrSearchResult);
    }
    
    @Override
    public void RecuperationDesc(ArrayList<SearchResult> arrSearchResult) {
        procedureThreads(arrSearchResult);
    }
    

    /**
     * sucharge pour enregistrer la conso sur un service particulier
     * @param clientID
     * @param nbCredit 
     */
    @Override
    public void enregistreCredit(int clientID, int nbCredit) {
        // nothing here
    }

    /**
     * implemetation du call pour le thread entre service 
     * @return
     * @throws Exception 
     */
    @Override
    public ArrayList<SearchResult> call() throws Exception {
        //
        if (this.typeRequete.equals("mpn")) {
            this.RecuperationMPNs(this.duplicateResults);
            for (SearchResult duplicateResult : duplicateResults) {
                duplicateResult.calculLevenshtein();
            }
        } else if (this.typeRequete.equals("sku")) {
            this.RecuperationSKUs(this.duplicateResults);
        } else if (this.typeRequete.equals("desc")) {
            this.RecuperationDesc(this.duplicateResults);
        }
        this.enregistreCredit(this.clientID, this.nbCredit);
        System.out.println("Digikey V3 terminé");
        return duplicateResults;
        //
    }



    /**
     * 
     * @return url du token
     */
    public String getAuthorizationUrlToken() {
        
        OAuthService service =  new ServiceBuilder()
                                        .provider(DigikeyOAuthApi.class)
                                        .apiKey(DigikeyClientID)
                                        .apiSecret(privateKey)
                                        .callback(oAuthRedirectionURL)
                                        .build();
        

        
        Verifier verifier = null;
        Token accessToken = null; 
        String authorizationUrl = service.getAuthorizationUrl(null);
        //
        return authorizationUrl;
        //
    }
    
    /**
     * 
     * @param wsBMclientID identifiant du client dans le BD
     * @param inToken oken fournit par le client 
     * @return 
     */
    public String getAccessToken(int wsBMclientID,String inToken) {
        
        OAuthService service =  new ServiceBuilder()
                                        .provider(DigikeyOAuthApi.class)
                                        .apiKey(DigikeyClientID)
                                        .apiSecret(privateKey)
                                        .callback(oAuthRedirectionURL)
                                        .build();
        //
        Token emptyToken = null;
        //
        Verifier verifier = new Verifier(inToken);
        //
        try {
            Token accessToken  = service.getAccessToken(emptyToken, verifier);
            String sAccessToken = accessToken.getToken();
            String sRenewToken = accessToken.getSecret();
            this.requeteSavePrivateKeyTOKEN(sAccessToken,sRenewToken,wsBMclientID);
            //
            return sAccessToken;
        } catch ( Exception e) {
            return "ERROR :" + e.toString();
        }
        //
    }
    
        /**
     * 
     * @return 
     */
    public String getRenewAccessToken() {
        
        OAuthService service =  new ServiceBuilder()
                                        .provider(DigikeyOAuthApi.class)
                                        .apiKey(DigikeyClientID)
                                        .apiSecret(privateKey)
                                        .callback(oAuthRedirectionURL)
                                        .build();
        
        DigikeyOAuth2Service digiserv = (DigikeyOAuth2Service )  service;    
        //
        try{
            Token accessToken = digiserv.getRenewToken(this.getKey(),this.getPassword());
                        
            String sAccessToken = accessToken.getToken();
            String sRenewToken = accessToken.getSecret();

            if ( ! sAccessToken.isEmpty() && ! sRenewToken.isEmpty() ){
                //
                this.requeteSavePrivateKeyTOKEN(sAccessToken,sRenewToken,this.getClientId());
                //
                return sAccessToken;
                //
            }else{            
               return "";
            }
        }catch(org.scribe.exceptions.OAuthException oathex){
            //
            Logger.getLogger(WsClientDB.class.getName()).log(Level.WARNING, null, oathex);
            return "";
            //
        }

    }
    
    
    /**
     * Recuperation du token client est sauvegarde dans la base
     * Attention non totalement testé
     *
     * @param key token d'acces 
     * @param clientID client dans lequel on va enregistrer l'element
     */
    public void requeteSavePrivateKeyTOKEN(String key,String renew, int clientID) {
        try {
           String query =" UPDATE ws_client SET `key`='" + key + "', `password`='"+ renew +"' WHERE `clientID`='" + clientID + "' and`wsID`='" + digikeyWsId + "'";
           ConnexionDB.ExecuteUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(WsClientDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   
    /**
     * 
     * @param mySearchResult
     * @return liste de source du thread
     */
    @Override
    protected Callable<ArrayList<Source>> getTMT(SearchResult mySearchResult){
        Digikey.TraitementMultihtreads tmt = new Digikey.TraitementMultihtreads();
        //
        if (this.getTypeRequete().equals("mpn")) {
            tmt.setNumeroATraiter(mySearchResult.getMpnOriginal());
        } else if (this.getTypeRequete().equals("sku")) {
            tmt.setNumeroATraiter(mySearchResult.getSkuOriginal());
        } else{
            tmt.setNumeroATraiter(mySearchResult.getDescOriginal()); 
        }
        return tmt;
    }
        

    
 
    
        /**
     * ***
     * Classe permettant d'executer une requête dans un thread
     */
    class TraitementMultihtreads implements Callable<ArrayList<Source>> {

        private String number = "";

        public void setNumeroATraiter(String number) {
            this.number = number;
        }

        @Override
        public ArrayList<Source> call() throws Exception {
            String requete = getTypeRequete();
            if (requete.equals("mpn")) {
                return interrogationUnMPN(number);
            } else if (requete.equals("sku")) {
                // pour moi MPN et SKU sont la même fonction
                return interrogationUnSKU(number);
            } else if (requete.equals("desc")) {
                // pour moi MPN et SKU sont la même fonction
                return interrogationUneDESC(number);
            }
            return null;
        }

    }
    
    
        /**
     * Permet la récupération des informations sous format JSON pour les parser
     * et les intégrer dans un tableau d'articles à partir d'un seul SKU
     *
     * @param descr La description recherché.
     * l'option des prix.
     * @return : Tableau de Produits correspondant au résultat parsé de la
     * requête et inclus dans des objets de type Source.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     * @see InterfaceWSInterrogeable#RecuperationArticle(java.lang.String, int,
     * boolean)
     */
    public ArrayList<Source> interrogationUneDESC(String descr) throws JSONException {
        //
        if ( descr.isEmpty()){
            return null;
        }else{
            //on recupére un String correspondant au JSON résultant de la requete     
             String resultatJson = this.interroDigikey(descr,TEXTSEARCH,this.getKey());
            //
            // si le resultat n'est pas vide ou différents de -1 erreur de connexion
            if (!resultatJson.isEmpty() && !resultatJson.equals("-1") ) {
                ArrayList<Source> produits = this.JsonToArrSource(resultatJson, descr);
                return produits;
            }else{
                return null;
            }
        }
    }
    
    /**
     * Permet la récupération des informations sous format JSON pour les parser
     * et les intégrer dans un tableau d'articles à partir d'un seul SKU
     *
     * @param sku Le SKU recherché.
     * l'option des prix.
     * @return : Tableau de Produits correspondant au résultat parsé de la
     * requête et inclus dans des objets de type Source.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     * @see InterfaceWSInterrogeable#RecuperationArticle(java.lang.String, int,
     * boolean)
     */
    public ArrayList<Source> interrogationUnSKU(String sku) throws JSONException {
        //
        if ( sku.isEmpty()){
            return null;
        }else{
            //on recupére un String correspondant au JSON résultant de la requete     
             String resultatJson = this.interroDigikey(sku,SKUSEARCH,this.getKey());
            //
            // si le resultat n'est pas vide ou différents de -1 erreur de connexion
            if (!resultatJson.isEmpty() && !resultatJson.equals("-1") ) {
                ArrayList<Source> produits = this.JsonToArrSourceSKU(resultatJson, sku);
                return produits;
            }else{
                return null;
            }
        }
    }
    
    
     /**
     * Permet la récupération des informations sous format JSON pour les parser
     * et les intégrer dans un tableau d'articles à partir d'un seul MPN
     *
     * @param mpn Le MPN recherché.
     * l'option des prix.
     * @return : Tableau de Produits correspondant au résultat parsé de la
     * requête et inclus dans des objets de type Source.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     * @see InterfaceWSInterrogeable#RecuperationArticle(java.lang.String, int,
     * boolean)
     */
    public ArrayList<Source> interrogationUnMPN(String mpn) throws JSONException {
        //
        if ( mpn.isEmpty()){
            return null;
        }else{
            //on recupére un String correspondant au JSON résultant de la requete     
             String resultatJson = this.interroDigikey(mpn,MPNSEARCH,this.getKey());

            // si le resultat n'est pas vide ou différents de -1 erreur de connexion
            if (!resultatJson.isEmpty() && !resultatJson.equals("-1") ) {
                ArrayList<Source> produits = this.JsonToArrSource(resultatJson, mpn);
                return produits;
            }else{
                return null;
            }
        }
    }
    

   
    /**
     * 
     * @return renvoie vrai si la login est remplis
     */
     @Override
    public boolean getCustomerPriceAvailable(){
          String myKey = this.getKey();
        return ! myKey.isEmpty();
    }
    
    public String getStatusCodeMessage(int statusCode,String mpn,int mode)
    {
        String StatusMessageCode = "";
        
        switch(statusCode)
        {
            case 401:
                    // token invalide
                    // le risque c'est q'un bloc entier parte en cacahouette lors du premier renew...
                    String newToken =  getRenewAccessToken();

                    if ( ! newToken.isEmpty() ){
                        // appel recursif si nouvel MPN
                        // anien code
                        //returnValue =  interroDigikey(mpn,mode);
                        StatusMessageCode =  interroDigikey(mpn,mode,newToken);
                    }else{
                        // renvoie -1 erreur de connexion
                        //Logger.getLogger(Digikey.class.getName()).log(Level.INFO,"Token renew error "+response.code()+ " : "+ response.body().string(),response);
                        StatusMessageCode = "{ErrorStatus : 401 , ErrorMessage : Unauthorized !}";
                    }       
                        
                    break;
            case 400:
                    StatusMessageCode = "{ERROR : 400 Bad request !}";
                    break;
            case 404:
                    StatusMessageCode = "{ERROR : 404 Not found !}";
                    break;
            case 502:
                    StatusMessageCode = "{ERROR : 502 Bad gateway ou proxy error}";
                    break;
            case 500:
                    StatusMessageCode = "{ERROR : 500 Internal server error !}";
                    break;
            default:
                    StatusMessageCode = "{ErrorStatus : jsonVide , ErrorMessage : products not found !}";
                    break;
        }
        return StatusMessageCode ;
    }
    
    public String interroDigikey(String mpn, int mode, String accessToken ) {
     
        // OkHttpClient from http://square.github.io/okhttp/
        // https://github.com/square/okhttp/wiki/HTTPS
        
       OkHttpClient client = new OkHttpClient();
       
        
        //
        //MediaType mediaType = MediaType.parse("application/octet-stream");
        //
        MediaType mediaType = MediaType.parse("application/json");    
        //RequestBody body = RequestBody.create(mediaType, "{  \"PartNumber\": \""+mpn+"\",  \"Quantity\": \"10\",  \"PartPreference\": \"\"}");
        
        RequestBody body ;
        Request request ;
        Request.Builder reqBuilder ;
        
        //String myKey =  accessToken; //this.getKey();
        String myLogin = this.getLogin();
        
        if ( mode == MPNSEARCH){
            // BASIC API : Keyword Search pour récupérer l'ensemble des "Digikey reference" du MPN (1 référence par packaging)
            body = RequestBody.create(mediaType,"{ \"Keywords\": \""+mpn+"\",\n" +
                                                "  \"RecordCount\": 30,\n" +
                                                "  \"RecordStartPosition\": 0\n" +
                                                "}");
            
            reqBuilder = new Request.Builder()
                //.url("https://api.digikey.com/services/partsearch/v2/keywordsearch")
                .url("https://api.digikey.com/Search/v3/Products/Keyword")
                .post(body)
                .addHeader("X-DIGIKEY-Client-Id", DigikeyClientID)
                .addHeader("Authorization","Bearer "+accessToken)
                .addHeader("X-DIGIKEY-Locale-Site",this.getCountry())
                .addHeader("X-DIGIKEY-Locale-Language",this.getCountry())
                .addHeader("X-DIGIKEY-Locale-Currency",this.getDevise())
                .addHeader("X-DIGIKEY-Locale-ShipToCountry",this.getShipToCountry());

            if ( ! myLogin.isEmpty() ){        
               reqBuilder.addHeader("X-DIGIKEY-Customer-Id",myLogin);
            }
            //
            request = reqBuilder.build();

        }else if (mode == SKUSEARCH){

            mpn = mpn.replaceAll("#","%23");
            mpn = mpn.replaceAll("/","%2F");
            //body = RequestBody.create(mediaType,"{\"Part\": \""+mpn+"\"}");
        // BASIC API
            reqBuilder = new Request.Builder()
                //.url("https://api.digikey.com/services/partsearch/v2/partdetails")
                .url("https://api.digikey.com/Search/v3/Products/"+mpn)
                //.post(body)
                
                //.addHeader("accept", "application/json")
                .addHeader("X-DIGIKEY-Client-Id", DigikeyClientID)
                .addHeader("Authorization","Bearer "+accessToken)
                //.addHeader("content-type", "application/json")
                .addHeader("X-DIGIKEY-Locale-Site",this.getCountry())
                .addHeader("X-DIGIKEY-Locale-Language",this.getCountry())
                .addHeader("X-DIGIKEY-Locale-Currency",this.getDevise())
                .addHeader("X-DIGIKEY-Locale-ShipToCountry",this.getShipToCountry());
   
            //
            if ( ! myLogin.isEmpty() ){        
               reqBuilder.addHeader("X-DIGIKEY-Customer-Id",myLogin);
            }

            request = reqBuilder.get().build();
        
        }else{ 
            String myText = mpn ; //UriComponent.encode(mpn, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
            body = RequestBody.create(mediaType, "{ \"Keywords\": \""+myText+"\", \"RecordCount\":30, \"RecordStartPosition\":0 }");
        
            reqBuilder = new Request.Builder()
                .url("https://api.digikey.com/Search/v3/Products/Keyword")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("authorization","Bearer "+accessToken)
                .addHeader("x-ibm-client-id", DigikeyClientID)
                .addHeader("content-type", "application/json")
                .addHeader("X-DIGIKEY-Locale-Site",this.getCountry())
                .addHeader("X-DIGIKEY-Locale-Language",this.getCountry())
                .addHeader("X-DIGIKEY-Locale-Currency",this.getDevise())
                .addHeader("X-DIGIKEY-Locale-ShipToCountry",this.getShipToCountry());
            //
            //
            if ( ! myLogin.isEmpty() ){        
               reqBuilder.addHeader("X-DIGIKEY-Customer-Id",myLogin);
            }
            //
            request = reqBuilder.build();
        }
        
        //
        try {
            Response response = client.newCall(request).execute();
            if ( response.isSuccessful() ) {
                return response.body().string();             
            }else{
                String returnValue = this.getStatusCodeMessage(response.code(), mpn, mode);
                return returnValue;
            }
           
        } catch (IOException ex) {
            Logger.getLogger(Digikey.class.getName()).log(Level.SEVERE, null, ex);
            return "-1";
        }
        // 
     }
    
    /**
     * transforme le json recu depuis digikey pour le mode SKU prix négocié en notre format 
     * @param resultatJson
     * @param sku
     * @return 
     */
    private ArrayList<Source> JsonToArrSourceSKU(String resultatJson, String sku){
        //On crée une nouvelle liste d'articles
        ArrayList<Source> produits = new ArrayList<Source>();

        //vérification que le Json n'est pas vide
        if (resultatJson != null && !resultatJson.isEmpty()) {
            try {
                //on recupere le JSON complet
                JSONObject jsonObject = new JSONObject(resultatJson);
                // On récupère le tableau d'objets spécifique à l'entrée "products"
                //JSONObject objPart = jsonObject.getJSONObject("Products");

//                //Pour chacune des entrées de "products":
//                for (int i = 0; i < parts.length(); i++) {

//                    //on crée un objet de chaque "products"
//                    JSONObject objPart = parts.getJSONObject(i);

                    //On crée un nouvel article pour chaque "products"
                    Source produit = new Source();

                    produit.setMpn(jsonObject.getString("ManufacturerPartNumber"));
                    produit.setNomProduit(jsonObject.getString("ProductDescription"));
                    
                    JSONObject objManufacturer = jsonObject.getJSONObject("Manufacturer");
                    
                    produit.setNomFabricant(objManufacturer.getString("Value"));
                    produit.setUid(jsonObject.getString("DigiKeyPartNumber"));
                    produit.setOrigine("Digikey");
                    produit.setUrlWs(jsonObject.getString("ProductUrl"));
                    //
                    produit.setStock(jsonObject.getInt("QuantityAvailable"));
                    
                    //
                    String packaging = "";
                    // si on a un package type on le prend
                    if (jsonObject.has("PackageType") ){
                        packaging = jsonObject.getString("PackageType");
                    }
                    // si on a le packaging type c'est mieux
                    if (jsonObject.has("Packaging") ){
                        JSONObject myPackaging = jsonObject.getJSONObject("Packaging");
                        packaging = myPackaging.getString("Value");
                    }

                    Integer leadweeksint = 0;
                    if (jsonObject.has("ManufacturerLeadWeeks")){
                        String[] leadweeks = jsonObject.getString("ManufacturerLeadWeeks").split(" ");
                        if (leadweeks.length > 0) {
                            if (leadweeks[0].matches("[+-]?\\d*(\\.\\d+)?" )){
                                if (leadweeks[0].length() > 0) {
                                    leadweeksint = Integer.parseInt(leadweeks[0]);
                                }
                                
                            }
                        }                                
                    }
                    
                    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                    Date today = Calendar.getInstance().getTime(); 
                    String strLastUpdate =  df.format(today);
                    //Création du tableau prixTab qui est un attribut de Source
                    ArrayList<Prix> prixList = new ArrayList<Prix>();
                    
                    //
                    if (optionPrix  && jsonObject.has("StandardPricing") ) {
                        JSONArray prices = jsonObject.getJSONArray("StandardPricing");
                        //
                        //                        
                        for (int itPrices = 0; itPrices < prices.length(); itPrices++) {
                            JSONObject objPrice = prices.getJSONObject(itPrices);
                            //
                            Prix myPrice = new Prix();
                            myPrice.setMoq(objPrice.getInt("BreakQuantity"));
                            //myPrice.setMpq(objPart.getInt("StandardPackage")); --> non applicable pour les prix standards
                            myPrice.setQuantite(objPrice.getInt("BreakQuantity"));
                            myPrice.setPrix(objPrice.getDouble("UnitPrice"));
                            myPrice.setDevise(this.getDevise().toUpperCase());
                            myPrice.setPackaging(packaging);
                            myPrice.setLeadDays(leadweeksint * 7);
                            myPrice.setFournisseur("Digikey (direct Public)");
                            myPrice.setSku(jsonObject.getString("DigiKeyPartNumber")); 
                            myPrice.setStock(jsonObject.getInt("QuantityAvailable"));
                            myPrice.setStockRegions("");
                            //myPrice.setLastUpdate(strLastUpdate);
                            // on peut avoir des prix à 0 
                            if (myPrice.getPrix() > 0 ) {
                                prixList.add(myPrice);
                            }
                        }
                        
                    }
                    if (optionPrix  && jsonObject.has("MyPricing") ) {
                        JSONArray MyPrices = jsonObject.getJSONArray("MyPricing");
                        //
                        //                        
                        for (int itPrices = 0; itPrices < MyPrices.length(); itPrices++) {
                            JSONObject objPrice = MyPrices.getJSONObject(itPrices);
                            //
                            Prix myPrice = new Prix();
                            myPrice.setMoq(objPrice.getInt("BreakQuantity"));
                            myPrice.setMpq(jsonObject.getInt("StandardPackage"));
                            myPrice.setLeadDays(leadweeksint * 7);
                            myPrice.setQuantite(objPrice.getInt("BreakQuantity"));
                            myPrice.setPrix(objPrice.getDouble("UnitPrice"));
                            myPrice.setDevise(this.getDevise().toUpperCase());
                            myPrice.setPackaging(packaging);
                            myPrice.setFournisseur("Digikey (direct Nego)");
                            myPrice.setSku(jsonObject.getString("DigiKeyPartNumber")); 
                            myPrice.setStock(jsonObject.getInt("QuantityAvailable"));
                            myPrice.setStockRegions("");
                            //myPrice.setLastUpdate(strLastUpdate);
                            // on peut avoir des prix à 0 
                            if (myPrice.getPrix() > 0 ) {
                                prixList.add(myPrice);
                            }
                        }
                        
                    }
                    if (optionPrix) {
                        produit.setListePrix(prixList);
                    }
                    
                    if (optionSpec && jsonObject.has("Parameters") ) {
                        JSONArray listSpec = jsonObject.getJSONArray("Parameters");
                        ArrayList<Specs> specsItem = new ArrayList<Specs>();
                        for (int itListSpec = 0; itListSpec < listSpec.length(); itListSpec++) {
                            Specs uneSpec = new Specs();
                            JSONObject uneSpecJson = listSpec.getJSONObject(itListSpec);
                            //
                            uneSpec.setKey(uneSpecJson.getString("Parameter").trim());
                            uneSpec.setValue(uneSpecJson.getString("Value").trim());
                            //
                            specsItem.add(uneSpec);
                        }
                        //------------
                        // autre infos 
                        //------------
                        if ( jsonObject.has("RoHSStatus")){
                            Specs uneSpec = new Specs();
                            uneSpec.setROHS(jsonObject.getString("RoHSStatus"));
                            specsItem.add(uneSpec);
                        }
                        //
                        String sCycle = "";
                        if ( jsonObject.has("Obsolete")){
                            boolean bObso = jsonObject.getBoolean("Obsolete");
                            if (bObso ){
                                sCycle = "Obsolete ";
                            }
                        }
                        if (jsonObject.has("ProductStatus") ){
                            sCycle = sCycle.concat(jsonObject.getString("ProductStatus"));
                        }
                        
                        if (sCycle.equals("") == false) {
                            Specs uneSpec = new Specs();
                            uneSpec.setLifeCycle(sCycle);
                            specsItem.add(uneSpec);
                        }
                        //
                        produit.setListeSpecs(specsItem);
                    }
                    
                    if (optionDS && jsonObject.has("PrimaryDatasheet")) {
                        
                        String myDataSheetString = "";
                        ArrayList<Datasheet> datasheet = new ArrayList<Datasheet>();
                        
                        try{
                            //
                            // test en mode chaine
                            myDataSheetString = jsonObject.getString("PrimaryDatasheet");
                        } catch (JSONException ex) {
                            //
                            // inon on teste en mode Object
                            JSONObject myDataSheet = jsonObject.getJSONObject("PrimaryDatasheet");
                            myDataSheetString = myDataSheet.getString("urlField");
                            //
                        }
                        
                        // une seule datasheet !!!!
                        Datasheet ds = new Datasheet();
                        ds.setUrl(myDataSheetString);
                        datasheet.add(ds);
                        //
                        produit.setDatasheet(datasheet);
                    }
                    //
                    produit.setRangOrigine(1);
                    // On ajoute le produit à la liste
                    produits.add(produit);
//                }
            } catch (JSONException ex) {
                Logger.getLogger(Digikey.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return produits;
    }
    
    /**
     * transforme le json recu depuis digikey en notre format 
     * @param resultatJson
     * @param mpn
     * @return 
     */
    private ArrayList<Source> JsonToArrSource(String resultatJson, String mpn) {
        //On crée une nouvelle liste d'articles
        ArrayList<Source> produits = new ArrayList<Source>();

        //vérification que le Json n'est pas vide
        if (resultatJson != null && !resultatJson.isEmpty()) {
            try {
                //on recupere le JSON complet
                JSONObject jsonObject = new JSONObject(resultatJson);
                // On récupère le tableau d'objets spécifique à l'entrée "products"

                String  errorStatus = "";
                String  errorMessage = "";
                
                if(jsonObject.has("ErrorStatus"))
                {
                    errorStatus = jsonObject.getString("ErrorStatus");
                }
                                
                if(jsonObject.has("ErrorMessage"))
                {
                    errorMessage = jsonObject.getString("ErrorMessage");
                }               
                
                if(errorStatus != "" && errorMessage != "")
                {
                    //On crée un nouvel article pour chaque "products"
                    Source produit = new Source();
                    produit.setErrorStatus(errorStatus);
                    produit.setErrorMessage(errorMessage);
                    produit.setOrigine("Digikey");
                    
                    produits.add(produit);
                }
                else
                {
                    JSONArray parts = jsonObject.getJSONArray("Products");

                    //Pour chacune des entrées de "products":
                    for (int i = 0; i < parts.length(); i++) {

                        //on crée un objet de chaque "products"
                        JSONObject objPart = parts.getJSONObject(i);

                        //On crée un nouvel article pour chaque "products"
                        Source produit = new Source();
                        errorStatus = "200";
                        errorMessage = "Success !";
                        produit.setErrorStatus(errorStatus);
                        produit.setErrorMessage(errorMessage);
                        produit.setMpn(objPart.getString("ManufacturerPartNumber"));
                        produit.setNomProduit(objPart.getString("ProductDescription"));

                        JSONObject objManufacturer = objPart.getJSONObject("Manufacturer");

                        produit.setNomFabricant(objManufacturer.getString("Value"));
                        produit.setUid(objPart.getString("DigiKeyPartNumber"));
                        produit.setOrigine("Digikey");
                        produit.setUrlWs(objPart.getString("ProductUrl"));
                        //
                        produit.setStock(objPart.getInt("QuantityAvailable"));

                        //
                        String packaging = "";
                        // si on a un package type on le prend
                        if (objPart.has("PackageType") ){
                            packaging = objPart.getString("PackageType");
                        }
                        // si on a le packaging type c'est mieux
                        if (objPart.has("Packaging") ){
                            JSONObject myPackaging = objPart.getJSONObject("Packaging");
                            packaging = myPackaging.getString("Value");
                        }

                        Integer leadweeksint = 0;
                        if (objPart.has("ManufacturerLeadWeeks")){
                            String strleadweeks = objPart.getString("ManufacturerLeadWeeks");
                            String[] leadweeks = strleadweeks.split(" ");
                            if (leadweeks.length > 0) {
                                if (leadweeks[0].matches("[+-]?\\d*(\\.\\d+)?" )){
                                    if (leadweeks[0].length() > 0) {
                                        leadweeksint = Integer.parseInt(leadweeks[0]);
                                    }
                                }
                            }                                
                        }

                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                        Date today = Calendar.getInstance().getTime(); 
                        String strLastUpdate =  df.format(today);

                        //Création du tableau prixTab qui est un attribut de Source
                        ArrayList<Prix> prixList = new ArrayList<Prix>();

                        //
                        if (objPart.has("StandardPricing") ) {
                            JSONArray prices = objPart.getJSONArray("StandardPricing");
                            //
                            //                        
                            for (int itPrices = 0; itPrices < prices.length(); itPrices++) {
                                JSONObject objPrice = prices.getJSONObject(itPrices);
                                //
                                Prix myPrice = new Prix();
                                myPrice.setMoq(objPrice.getInt("BreakQuantity"));
                                myPrice.setQuantite(objPrice.getInt("BreakQuantity"));
                                myPrice.setLeadDays(leadweeksint * 7);
                                myPrice.setPrix(objPrice.getDouble("UnitPrice"));
                                myPrice.setDevise(this.getDevise().toUpperCase());
                                myPrice.setPackaging(packaging);
                                myPrice.setFournisseur("Digikey (direct Public)");
                                myPrice.setSku(objPart.getString("DigiKeyPartNumber")); 
                                myPrice.setStock(objPart.getInt("QuantityAvailable"));
                                myPrice.setStockRegions("");
                                //myPrice.setLastUpdate(strLastUpdate);
                                // on peut avoir des prix à 0 
                                if (myPrice.getPrix() > 0 ) {
                                    prixList.add(myPrice);
                                }
                            }
                            produit.setListePrix(prixList);
                        }

                        if (optionSpec && objPart.has("Parameters") ) {
                            JSONArray listSpec = objPart.getJSONArray("Parameters");
                            ArrayList<Specs> specsItem = new ArrayList<Specs>();
                            for (int itListSpec = 0; itListSpec < listSpec.length(); itListSpec++) {
                                Specs uneSpec = new Specs();
                                JSONObject uneSpecJson = listSpec.getJSONObject(itListSpec);
                                //
                                uneSpec.setKey(uneSpecJson.getString("Parameter").trim());
                                uneSpec.setValue(uneSpecJson.getString("Value").trim());
                                //
                                specsItem.add(uneSpec);
                            }
                            //------------
                            // autre infos 
                            //------------
                            if ( objPart.has("RoHSStatus")){
                                Specs uneSpec = new Specs();
                                uneSpec.setROHS(objPart.getString("RoHSStatus"));
                                specsItem.add(uneSpec);
                            }
                            //
                            String sCycle = "";
                            if ( objPart.has("Obsolete")){
                                boolean bObso = objPart.getBoolean("Obsolete");
                                if (bObso ){
                                    sCycle = "Obsolete ";
                                }
                            }
                            if (objPart.has("ProductStatus") ){
                                sCycle = sCycle.concat(objPart.getString("ProductStatus"));
                            }

                            if (sCycle.equals("") == false) {
                                Specs uneSpec = new Specs();
                                uneSpec.setLifeCycle(sCycle);
                                specsItem.add(uneSpec);
                            }
                            //
                            produit.setListeSpecs(specsItem);
                        }

                        if (optionDS && objPart.has("PrimaryDatasheet")) {

                            String myDataSheetString = "";
                            ArrayList<Datasheet> datasheet = new ArrayList<Datasheet>();

                            try{
                                //
                                // test en mode chaine
                                myDataSheetString = objPart.getString("PrimaryDatasheet");
                            } catch (JSONException ex) {
                                //
                                // inon on teste en mode Object
                                JSONObject myDataSheet = objPart.getJSONObject("PrimaryDatasheet");
                                myDataSheetString = myDataSheet.getString("urlField");
                                //
                            }

                            // une seule datasheet !!!!
                            Datasheet ds = new Datasheet();
                            ds.setUrl(myDataSheetString);
                            datasheet.add(ds);
                            //
                            produit.setDatasheet(datasheet);
                        }
                        //
                        produit.setRangOrigine(i + 1);
                        // On ajoute le produit à la liste
                        produits.add(produit);
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(Digikey.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return produits;
    }

    
    
    /**
     * Permet la creation d'une liste de webservice à interroger suivant les
     * droits du client.
     *
     * @param clientID l'identifiant du client.
     * @return liste la liste des WebService autorisées pour le client.
     * @throws ClassNotFoundException : exception levée quand une classe n’a pas
     * été trouvée.
     * @throws SQLException : exception levée suite à une erreur SQL (connexion
     * à la base/ mauvaise requête).
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     */
    public static Digikey getWsDigikeyFromWebserviceID(int clientID)
            throws ClassNotFoundException, SQLException, JSONException {
        InterfaceWSInterrogeable wsIntClient = null;
        //
        String requete = "  SELECT ws_client.* "
                + " FROM ws_client "
                + " WHERE `clientID`='" + clientID + "' "
                + "         AND `wsID`='" + digikeyWsId + "' ";

        ResultSet reponse = ConnexionDB.ExecuteQuery(requete);

        //Boucle sur la base de données
        while (reponse.next()) {
            wsIntClient = extractClientFromResultSet(reponse);
        }
        return (Digikey) wsIntClient;
    }
    
    @Override
    public boolean getErrorStatus() {
       // on renouvelle le token d'acces pour savoir si le service est OK
       // un peu
       String resultatJson = this.interroDigikey("bav99",MPNSEARCH,this.getKey());
       //
       // -1 erreur de connexion ou de clef 
       return resultatJson.equals("-1");
       //
    }
    
    @Override
    public String getNameWS()
    {
        return "Digikey";
    }
    
}

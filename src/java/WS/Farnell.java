package WS;

import DB.WsClientDB;
import POJO.Datasheet;
import POJO.Prix;
import POJO.SearchResult;
import com.essai3.ServiceResource;
import POJO.Source;
import POJO.Specs;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.tomcat.util.codec.binary.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;


/**
 * Nom de classe : Farnell
 * <br>
 * Description : Classe permettant l'interrogation du service Web Farnell.
 * <br>
 * URL :  http://partner.element14.com/
 * login : loic.maisonnasse
 * pass : Buymanager2014
 * 
 * Date de la dernière modification : 22/07/2014
 * 
* @author Stagiaire (Florence Giraud)
 */
public class Farnell extends WsClientDB implements InterfaceWSInterrogeable, Callable<ArrayList<SearchResult>> {

    static final String rsFarnellKey = "83vna3kmk7yaa2vqhspawxjx";

    static final String sFarnelFunctionMpn = "searchByManufacturerPartNumber";

    static final String sFarnelFunctionSku = "searchByPremierFarnellPartNumber";

    private static final int nbPoolsThreads = 6;

    /**
     * fonction de cryptage pour construire la clef farnell
     *
     * @param msg
     * @param keyString
     * @return
     */
    private static String hmacDigest(String message, String keyString) {

        String algo = "HmacSHA1";
        SecretKeySpec key = new SecretKeySpec(keyString.getBytes(), algo);
        try {
            Mac mac = Mac.getInstance(algo);
            mac.init(key);

            String hash = Base64.encodeBase64String(mac.doFinal(message.getBytes()));
            return hash;

        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            Logger.getLogger(Farnell.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    private static String getFarnellTimeStamp() {

        final Date currentTime = new Date();

        final SimpleDateFormat sdf
                = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        // Give it to me in GMT time.
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        //
        return sdf.format(currentTime);
    }

    private static String getFarnellSignature(String timeStamp, String operation, String password) {

        String signature = operation.concat(timeStamp);
        return hmacDigest(signature, password);
    }

    private String getDevise() {
        String myDevise = "USD";
        String myMagasin = this.getMagasin();
        //
        if (myMagasin.equals("fr.farnell.com") || myMagasin.equals("es.farnell.com") || myMagasin.equals("de.farnell.com")) {
            myDevise = "EUR";
        } else if (myMagasin.equals("tw.element14.com")) { // taiwan
            myDevise = "NTD";
        } else if (myMagasin.equals("ch.farnell.com")) { // suisses
            myDevise = "CHF";
        } else if (myMagasin.equals("uk.farnell.com")) { // UK
            myDevise = "GBP";
        } else if (myMagasin.equals("cn.element14.com")) { //chine
            myDevise = "CNY";
        } else if (myMagasin.equals("my.element14.com")) { // malaysie
            myDevise = "MYR";
        }
        //
        return myDevise;
    }

    /**
     * ***********************
     * Constructeurs
     *
     * @return String **********************
     */
    @Override
    public String getKey() {
        String mykey = super.getKey(); //To change body of generated methods, choose Tools | Templates.
        if (mykey.isEmpty()) {
            mykey = rsFarnellKey;
        }
        return mykey;
    }

    /**
     * Constructeur vide de Farnell
     */
    public Farnell() {
    }

    /**
     * ***********************
     * METHODES PUBLIQUES **********************
     */
    /**
     * Permet l'interrogation de l'API Farnell et retourne une réponse sous
     * format JSON/String.
     *
     * @param mySearch
     * @param myFunction
     * @return Un JSON de l'ensemble des résultats de la requête.
     */
    private String InterroFarnell(String mySearch, String myFunction) {

        //Exemple d'URL questionnée
        //https://api.element14.com/catalog/products?term=manuPartNum%3A%2A339L%2A&storeInfo.id=fr.farnell.com&resultsSettings.offset=0&resultsSettings.numberOfResults=10&resultsSettings.responseGroup=medium&callInfo.responseDataFormat=JSON&callInfo.apiKey=83vna3kmk7yaa2vqhspawxjx
        //
        //création du nouveau Client de requête
        Client client = ClientBuilder.newClient(new ClientConfig());
        //
        WebTarget webTarget;
        //
        if (this.getKey().isEmpty() || this.getLogin().isEmpty()) {

            //Declaration de l'URL permettant la recherche
            webTarget = client.target("https://api.element14.com/catalog/")
                    .path("/products")
                    .queryParam("term", mySearch) // ex pour MPN -> "manuPartNum%3A%2A"+mpn+"%2A"
                    .queryParam("storeInfo.id", this.getMagasin())
                    .queryParam("resultsSettings.offset", "0")
                    .queryParam("resultsSettings.numberOfResults", "10")
                    .queryParam("resultsSettings.responseGroup", "large")
                    .queryParam("callInfo.responseDataFormat", "JSON")
                    .queryParam("callInfo.apiKey", rsFarnellKey)
                    .property(ClientProperties.READ_TIMEOUT,20000)     // timeOUT à 20 s comme pour RS  pour les jour ou ca rame
                    .property(ClientProperties.CONNECT_TIMEOUT,20000);
        } else {

            String customerId = this.getLogin();
            String key = this.getKey();
            String timestampFarnell = Farnell.getFarnellTimeStamp();
            String functionFarnell = myFunction;
            String signatureFarnell = Farnell.getFarnellSignature(timestampFarnell, functionFarnell, this.getPassword());

            //Declaration de l'URL permettant la recherche
            webTarget = client.target("https://api.element14.com/catalog/")
                    .path("/products")
                    .queryParam("term", mySearch)
                    .queryParam("storeInfo.id",this.getMagasin())
                    .queryParam("resultsSettings.offset", "0")
                    .queryParam("resultsSettings.numberOfResults", "10")
                    //callInfo.omitXmlSchema
                    .queryParam("resultsSettings.responseGroup", "large")
                    .queryParam("callInfo.omitXmlSchema", "false")
                    .queryParam("callInfo.responseDataFormat", "JSON")
                    .queryParam("callInfo.apiKey", rsFarnellKey)
                    //.queryParam("userInfo.customerId", customerId)//)
                    //.queryParam("userInfo.timestamp", timestampFarnell)
                    .queryParam("userInfo.signature", signatureFarnell)     //SyST1ecHStE
                    .queryParam("userInfo.timestamp", timestampFarnell)
                    .queryParam("userInfo.customerId", customerId)          // 34768324
                    .property(ClientProperties.READ_TIMEOUT,20000)          // timeOUT à 20 s comme pour RS  pour les jour ou ca rame
                    .property(ClientProperties.CONNECT_TIMEOUT,20000);  
        }

        try {
            //Activation de l'URL permettant la recherche
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN_TYPE);
            Response rep = invocationBuilder.get(Response.class);
            if (rep.getStatus() == 200) {
                String reponse = rep.readEntity(String.class);
                return reponse;
            }else{
                Logger.getLogger(ServiceResource.class.getName()).log(Level.WARNING,String.valueOf( rep.getStatus()));
            }
        } catch (RuntimeException e) {
            Logger.getLogger(ServiceResource.class.getName()).log(Level.WARNING, null, e);
        }
        return "";
    }

    /**
     * Permet la récupération des informations sous format JSON pour les parser
     * et les intégrer dans un tableau d'articles à partir d'un seul MPN
     *
     * @param mpn Le MPN recherché.
     * @param clientID l'identifiant du client.
     * @param optionPrix boolean décrétant si le client souhaite obtenir ou pas
     * l'option des prix.
     * @return : Tableau de Produits correspondant au résultat parsé de la
     * requête et inclus dans des objets de type Source.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     * @see InterfaceWSInterrogeable#RecuperationArticle(java.lang.String, int,
     * boolean)
     */
    private ArrayList<Source> RecuperationMPN(String mpn) throws JSONException {
        //
        if ( mpn.isEmpty()){
            return null;
        }else{
            //on recupére un String correspondant au JSON résultant de la requete     
            String mySearch = "manuPartNum%3A%2A" + mpn + "%2A";
            String resultatJson = this.InterroFarnell(mySearch, sFarnelFunctionMpn);
            //
            if (!resultatJson.isEmpty()) {
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
        return !getKey().isEmpty() && !getPassword().isEmpty();
    }
    

    /**
     * Permet la récupération des informations sous format JSON pour les parser
     * et les intégrer dans un tableau d'articles à partir d'un seul MPN
     *
     * @param mpn Le MPN recherché.
     * @param optionPrix boolean décrétant si le client souhaite obtenir ou pas
     * l'option des prix.
     * @return : Tableau de Produits correspondant au résultat parsé de la
     * requête et inclus dans des objets de type Source.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     * @see InterfaceWSInterrogeable#RecuperationArticle(java.lang.String, int,
     * boolean)
     */
    private ArrayList<Source> RecuperationSKU(String sku) throws JSONException {
        //
        //on recupére un String correspondant au JSON résultant de la requete     
        String mySearch = "id%3A" + sku + "";
        String resultatJson = this.InterroFarnell(mySearch, sFarnelFunctionSku);
        //
        ArrayList<Source> produits = this.JsonToArrSource(resultatJson, "");
        //
        return produits;
    }

    /**
     * Permet de remplir le sourcesListe (Tableau représentant le futur Json )
     * fourni en paramètre en récupérant les articles. Initialise également les
     * pools de threads
     *
     * @param arrSearchResult tableau a remplir avec les résultats de l'API
     */
    @Override
    public void RecuperationMPNs(ArrayList<SearchResult> arrSearchResult) {
        procedureThreads(arrSearchResult);
    }

    /**
     * Permet de récupérer les prix si l'option est demandée par le client.
     *
     * @param objProducts objet Json contenant certaines informations
     * nécessaires.
     * @param quantite quantité disponible pour le produit.
     * @param prixList tableau des prix qui sera renvoyé complété.
     * @param objStock objet Json contenant certaines informations nécessaires.
     * @return prixList : tableau des prix complété.
     * @throws org.codehaus.jettison.json.JSONException : exception levée suite
     * à une erreur sur le JSON (mauvais format/objet non trouvé).
     */
    public ArrayList<Prix> recuperationPrix(JSONObject objProducts, int quantite,
            ArrayList<Prix> prixList, JSONObject objStock,int statut) throws JSONException {
        // On récupère le tableau d'objets spécifique à l'entrée "prices"
        JSONArray prices = objProducts.getJSONArray("prices");
        //
        int packSize = objProducts.getInt("packSize");
        //
        for (int j = 0; j < prices.length(); j++) {
            //on crée un objet de chaque "prices"
            JSONObject objPrices = prices.getJSONObject(j);
            //
            //
            Prix prixTab = new Prix();
            prixTab.setFournisseur("Farnell (direct)");
            //
            //si aucune quantité, cela déclare 0 (quantité est une variable déclarée dans le bloc précédent)
            quantite = objPrices.getInt("from");
            prixTab.setQuantite(quantite);
            prixTab.setDevise(this.getDevise());
            prixTab.setPrix(objPrices.getDouble("cost"));

            prixTab.setDistribValide(true);
            if(statut == -3){
                prixTab.setLeadDays(9999);
            }else{
                prixTab.setLeadDays(objStock.getInt("leastLeadTime"));
            }
            
            String sku = objProducts.getString("sku");
            prixTab.setSku(sku);
            //
            // surcharge pour les condionnement de production
            if(prixTab.getSku().endsWith("RL")){
                prixTab.setPackaging("(P)"+objProducts.getString("unitOfMeasure"));   
            }else{
                prixTab.setPackaging(objProducts.getString("unitOfMeasure"));   
            }
            
            prixTab.setStock(objStock.getInt("level"));
            // ici le moq doit correspondre à celui du prix -> donc egal à la quantité du from
            prixTab.setMoq(quantite);
            // le MPQ est le translatedMinimumOrderQuality, ce qui correspond à la tranche des quantités
            prixTab.setMpq(objProducts.getInt("translatedMinimumOrderQuality"));
            
            //
            //on crée un objet de chaque "prices"
            //
            String myStockRegion = "";
            if(objStock.has("breakdown"))
            {
                JSONArray arrObjBreakDown = objStock.getJSONArray("breakdown");
                for (int nLigne = 0; nLigne < arrObjBreakDown.length(); nLigne++) {
                    //on crée un objet de chaque "prices"
                    JSONObject ObjBreakDown = arrObjBreakDown.getJSONObject(nLigne);
                    int level = ObjBreakDown.getInt("inv");
                    if (level > 0) {
                        if (!myStockRegion.isEmpty()) {
                            myStockRegion = myStockRegion.concat("|");
                        }
                        myStockRegion = myStockRegion.concat(ObjBreakDown.getString("region"));
                    }
                }
            }

            prixTab.setStockRegions(myStockRegion);
            //
            // applique le packaging pour repasser en unité
            if (packSize > 1) {
                //
                prixTab.applyPackSize(packSize);
                //
            }

            prixList.add(prixTab);
        }
        return prixList;
    }

    @Override
    public void RecuperationSKUs(ArrayList<SearchResult> sourcesListe) {
        procedureThreads(sourcesListe);
    }

    private ArrayList<Source> JsonToArrSource(String resultatJson, String mpn) {
        //On crée une nouvelle liste d'articles
        ArrayList<Source> produits = new ArrayList<Source>();

        //vérification que le Json n'est pas vide
        if (resultatJson != null && !resultatJson.isEmpty()) {
            try {
                //on recupere le JSON complet
                JSONObject jsonObject = new JSONObject(resultatJson);

                //on crée un objet correspondant au JSON un niveau en-dessous
                JSONObject objManu;

                if (jsonObject.has("manufacturerPartNumberSearchReturn")) {
                    objManu = jsonObject.getJSONObject("manufacturerPartNumberSearchReturn");
                } else {
                    objManu = jsonObject.getJSONObject("premierFarnellPartNumberReturn");
                }

                // On récupère le tableau d'objets spécifique à l'entrée "products"
                
                
                JSONArray products = objManu.getJSONArray("products");

                //Pour chacune des entrées de "products":
                for (int i = 0; i < products.length(); i++) {
                    int quantite = 0;

                    //on crée un objet de chaque "products"
                    JSONObject objProducts = products.getJSONObject(i);

                    //On crée un nouvel article pour chaque "products"
                    Source produit = new Source();

                    produit.setMpn(objProducts.getString("translatedManufacturerPartNumber"));
                    String sName = objProducts.getString("displayName");
                    // supprime le fabricnt et la référence avant le nom du produit
                    sName = sName.replaceFirst("^.*?\\s\\-\\s.*?\\s\\-\\s", "");
                    //
                    produit.setNomProduit(sName);
                    produit.setNomFabricant(objProducts.getString("brandName"));

                    // l'id n'est pas présent en mode "client"
                    try {
                        String idFarnell = objProducts.getString("id");
                        produit.setUid(idFarnell);
                    } catch (JSONException e) {
                        // ceci n'est pas grave...
                        // car l'ID n'est pas present dans le mode client
                    }
                    //
                    produit.setOrigine("Farnell");

                    //on crée un objet de chaque "stock"
                    JSONObject objStock = objProducts.getJSONObject("stock");
                    int statut = Integer.parseInt(objStock.getString("status"));
                    
                    int packSize = objProducts.getInt("packSize");
                    // chez farnell, le stock doit etre mis a jour avec le pack size
                    produit.setStock(objStock.getInt("level") * packSize);

                    //Création du tableau prixTab qui est un attribut de Source
                    ArrayList<Prix> prixList = new ArrayList<Prix>();
                    if (optionPrix) {
                        prixList = this.recuperationPrix(objProducts, quantite, prixList, objStock, statut);
                    }

                    if (optionSpec && objProducts.has("attributes")) {
                        JSONArray listSpec = objProducts.getJSONArray("attributes");
                        // detclaration du tableau de spec
                        ArrayList<Specs> specsItem = new ArrayList<Specs>();
                        //
                        for (int k = 0; k < listSpec.length(); k++) {
                            Specs uneSpec = new Specs();
                            JSONObject uneSpecJson = listSpec.getJSONObject(k);
                            uneSpec.setKey(uneSpecJson.getString("attributeLabel").replace("é", "e").replace("è", "e").replace("î", "i").replace("°", "").replace("à", "a").replace("ê", "e").replaceAll("[^\\x00-\\x7F]", " ").trim());
                            uneSpec.setValue(uneSpecJson.getString("attributeValue").replace("é", "e").replace("è", "e").replace("î", "i").replace("°", "").replace("ê", "e").replaceAll("[^\\x00-\\x7F]", " ").trim());
                            if (uneSpecJson.has("attributeUnit")) {
                                uneSpec.setUnit(uneSpecJson.getString("attributeUnit").replace("°", "").replaceAll("[^\\x00-\\x7F]", " ").trim());
                            }
                            specsItem.add(uneSpec);
                        }
                        //
                        // ajout de ROHS
                        if ( objProducts.has("rohsStatusCode")){
                            Specs uneSpec = new Specs();
                            uneSpec.setROHS(objProducts.getString("rohsStatusCode"));
                            specsItem.add(uneSpec);
                        }
                        //
                        produit.setListeSpecs(specsItem);
                    }
                    if (optionDS && objProducts.has("datasheets")) {
                        ArrayList<Datasheet> datasheet = new ArrayList<Datasheet>();
                        JSONArray farnellDS = objProducts.getJSONArray("datasheets");
                        for (int k = 0; k < farnellDS.length(); k++) {
                            Datasheet ds = new Datasheet();
                            ds.setUrl(farnellDS.getJSONObject(k).getString("url"));
                            datasheet.add(ds);
                        }
                        produit.setDatasheet(datasheet);
                    }

                    produit.setRangOrigine(i + 1);
                    produit.setListePrix(prixList);
                    produit.setUrlWs("http://"+this.getMagasin()+"/"+objProducts.getString("sku"));
                    // On ajoute le produit à la liste
                    produits.add(produit);
                }
            } catch (JSONException ex) {
                Logger.getLogger(Farnell.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return produits;
    }

    @Override
    public void enregistreCredit(int clientID, int nbCredit) {

    }

    /**
     * ***
     * Méthode appelée lors du lancement du thread - externe
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
        System.out.println("Farnell terminé");
        return duplicateResults;
    }


    @Override
    public void RecuperationDesc(ArrayList<SearchResult> arrSearchResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getErrorStatus() {
       String mySearch = "id%3A" + "2112753" + "";
       /// on test une fonction SKu car devrait etre plus rapide
       String resultatJson = this.InterroFarnell(mySearch, sFarnelFunctionSku);
       // si vide alors on a une erreur
       return resultatJson.isEmpty();
    }
    
   /**
     * Retourne le type de requête
     * @return Integer
     */
    @Override
    protected int getNbThread() {
        return nbPoolsThreads;
    }

    @Override
    public String getNameWS() {
        return "Farnell";
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
                return RecuperationMPN(number);
            } else if (requete.equals("sku")) {
                return RecuperationSKU(number);
            }
            return null;
        }

    }
    
    
    
            /**
     * 
     * @param mySearchResult
     * @return Callable<ArrayList<Source>>
     */
    @Override
    protected Callable<ArrayList<Source>> getTMT(SearchResult mySearchResult){
        Farnell.TraitementMultihtreads tmt = new Farnell.TraitementMultihtreads();
        //
        if (this.getTypeRequete().equals("mpn")) {
            tmt.setNumeroATraiter(mySearchResult.getMpnOriginal());
        } else {
            tmt.setNumeroATraiter(mySearchResult.getSkuOriginal());
        }
        return tmt;
    }

}

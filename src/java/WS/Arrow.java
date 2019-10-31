package WS;

import DB.WsClientDB;
import POJO.Prix;
import POJO.SearchResult;
import POJO.Source;
import POJO.Specs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.tomcat.util.codec.binary.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Nom de classe : Arrow
 * 
 * http://developers.arrow.com/api/index.php/site/page?view=gettingStarted
 * 
 * Description : Classe permettant l'interrogation du service Web Farnell.
 * <br>
 * Date de la dernière modification : 22/07/2014
 * 
* @author Stagiaire (Florence Giraud)
 */
public class Arrow extends WsClientDB implements InterfaceWSInterrogeable, Callable<ArrayList<SearchResult>> {

    //
    //default key if no customer key
    static final String ARROWKEY = "051e8765ee88b0027ce2fa57646fea2789e121d05c36a9b9e1a7db7625bc3a6a";
    // login defaut si pas de clef client
    static final String ARROWLOGIN = "buymanager";
    //
    static final String SERVICETOKEN = "http://api.arrow.com/itemservice/v3/en/search/token?";

    private static final int nbPoolsThreads = 6;
    
    /**
     * ***********************
     * Constructeurs
     *
     * @return String **********************
     */
    @Override
    public String getKey() {
        String mykey = ""; //To change body of generated methods, choose Tools | Templates.
        if(!super.getKey().isEmpty())
        {
            byte[] decodePassword =  Base64.decodeBase64(super.getKey());
            mykey = new String(decodePassword);
        }
        if (mykey.isEmpty()) {
            mykey = ARROWKEY;
        }
        return mykey;
    }
    
    /**
     *
     * @return string 
     */
    @Override
    public String getLogin() {
        String myLogin = super.getLogin(); //To change body of generated methods, choose Tools | Templates.
        if (myLogin.isEmpty()) {
            myLogin = ARROWLOGIN;
        }
        return myLogin;
    }
    

    /**
     * Constructeur vide de Farnell
     */
    public Arrow() {
        //
        // Met a vide le pays avant chargement du pays Vis-à-vis de la BD
        super.setEmptyCountry();
        //
    }
    
    
    /**
     * renvoie la chaine avec les identifiants - pour get
     * @return chaine
     */
    public String getUrlAuthentification(){
        //
        return "login="+this.getLogin()+"&apikey="+this.getKey();        
        //
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
    private String InterroArrow(String mySearch) {

        OkHttpClient client = new OkHttpClient();
        //
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");    
        //
        String totalUrl = SERVICETOKEN;
        totalUrl = totalUrl.concat(this.getUrlAuthentification());
        //
        totalUrl = totalUrl.concat("&search_token="+mySearch);
        //
        Request request = new Request.Builder()
                    .url(totalUrl)
                    .build();
         //
        try {
            //
            okhttp3.Response response = client.newCall(request).execute();
            //
            if ( response.isSuccessful() ) {
                return response.body().string();
            }else{
                //
                Logger.getLogger(Arrow.class.getName())
                      .log(Level.WARNING,"Erreur HTTP "+response.code()+ " : "+ response.body().string(),response);
                //
                return "";
            }
           
        } catch (IOException ex) {
            Logger.getLogger(Arrow.class.getName()).log(Level.SEVERE, null, ex);
            return "-1";
        }
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
            String resultatJson = this.InterroArrow(mpn);
            //
            if (!resultatJson.isEmpty()) {
                ArrayList<Source> produits = this.JsonToArrSource(resultatJson, mpn);
                return produits;
            }else{
                return null;
            }
            //
        }
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
        //String mySearch = "id%3A" + sku + "";
        //String resultatJson = this.InterroArrow(mySearch);
        //
        ArrayList<Source> produits = new ArrayList<>(); //= this.JsonToArrSource(resultatJson, "");
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
    
    
    @Override
    public void RecuperationSKUs(ArrayList<SearchResult> sourcesListe) {
        procedureThreads(sourcesListe);
    }

    /**
     * Permet de récupérer les prix si l'option est demandée par le client.
     *
     * @param objInvOrg : partie du json correpondant au prix
     * @param produit : produit a completer avec les prix et le stock
     * @return prixList : tableau des prix complété.
     * @throws org.codehaus.jettison.json.JSONException : exception levée suite
     * à une erreur sur le JSON (mauvais format/objet non trouvé).
     */
    public Source recuperationPrix(JSONObject objInvOrg,Source produit) throws JSONException {
        //
        // On récupère le tableau d'objets spécifique à l'entrée "prices"
        JSONArray sourceArea = objInvOrg.getJSONArray("sources");
        //
        ArrayList<Prix> prixList = new ArrayList<Prix>();
        //
        int nStockTotal = 0;
        //
        for (int jArea = 0; jArea < sourceArea.length(); jArea++) {
            //on crée un objet de chaque "prices"
            JSONObject objSourceArea = sourceArea.getJSONObject(jArea);
            //
            String sDevise = objSourceArea.getString("currency");
            String sRegion = objSourceArea.getString("sourceCd");
            String sRegionName  = objSourceArea.getString("displayName");
            //
            JSONArray arrSourceParts = objSourceArea.getJSONArray("sourceParts");
            //
            String filtreRegion = this.getCountry();
            if ( filtreRegion.isEmpty() || filtreRegion.equals(sRegion) ) {
                //
                for (int jPart = 0; jPart < arrSourceParts.length(); jPart++) {
                    //
                    //recup des infos communes a tout les prix 
                    JSONObject mySourcePart = arrSourceParts.getJSONObject(jPart);
                    int mpq         = mySourcePart.getInt("packSize");
                    int moq         = mySourcePart.getInt("minimumOrderQuantity");
                    int leadTime    = mySourcePart.getInt("mfrLeadTime");
                    leadTime = leadTime * 7; // Leadtime Arrow en weeks et on renvoi des jours
                    String sku      = mySourcePart.getString("sourcePartId");

                    int stock = 0;

                    JSONArray arrAvailability = mySourcePart.getJSONArray("Availability");
                    if ( arrAvailability.length() > 0 ) {
                        JSONObject myAvailability = arrAvailability.getJSONObject (0);
                        //
                        stock = myAvailability.getInt("fohQty");
                        //
                    }
                    //
                    nStockTotal +=stock;
                        
                    // --- //
                    // recup des prix lient si dispo: 
                    JSONArray arrCusPrice  = mySourcePart.getJSONArray("customerSpecificPricing");
                    // --- //
                    if (mySourcePart.has("Prices")){
                        JSONObject prices = mySourcePart.getJSONObject("Prices");
                        // si il existe des prix client on prend ces prix sinon, on prend les prix par défaut
                        if ( arrCusPrice.length() > 0 ) {
                            //
                            //a voir plu tard il faut chopper le prix du client ? un seul ou plusieurs ????

                            //
                        }
                        //
                        JSONArray arrResalePrice = prices.getJSONArray("resaleList");

                        for (int jPrice = 0; jPrice < arrResalePrice.length(); jPrice++) {
                            //
                            JSONObject myResalePrice = arrResalePrice.getJSONObject(jPrice);
                            //
                            //
                            Prix prixTab = new Prix();
                            prixTab.setFournisseur("Arrow (direct)");
                            prixTab.setDistribValide(true);
                            //
                            prixTab.setDevise(sDevise);
                            
                            prixTab.setLeadDays(leadTime);
                            prixTab.setSku(sku);
                            
                            //
                            prixTab.setMoq(myResalePrice.getInt("minQty"));
                            prixTab.setQuantite(myResalePrice.getInt("minQty"));
                            //
                            prixTab.setMpq(mpq);
                            prixTab.setPackaging(""); /// mettre quel que chose car arrow c'est pas de l
                            prixTab.setStock(stock);
                           
                            //
                            prixTab.setStockRegions(sRegionName);
                            //
                            double prix = myResalePrice.getDouble("price");
                            prixTab.setPrix(prix);

                            prixList.add(prixTab);
                            //
                        }
                    }
                }
                //
            }
            
        }
        // ajoute le stocke et les prix au produit
        produit.setStock(nStockTotal);
        produit.setListePrix(prixList);
        //
        return produit;
        //
    }


    private ArrayList<Source> JsonToArrSource(String resultatJson, String mpn) {
        //On crée une nouvelle liste d'articles
        ArrayList<Source> produits = new ArrayList<Source>();

        //vérification que le Json n'est pas vide
        if (resultatJson != null && !resultatJson.isEmpty()) {
            //
            try {
                //on recupere le JSON complet
                JSONObject objProducts = new JSONObject(resultatJson);
                JSONObject itemserviceresult =objProducts.getJSONObject("itemserviceresult");
                //
                // On récupère le tableau d'objets spécifique à l'entrée "prices"
                JSONArray data  = itemserviceresult.getJSONArray("data");
                //
                if (data != null   && data.length()> 0 ){
                    //
                    if (! data.isNull(0) ) {
                        //
                        JSONObject objOneResult = data.getJSONObject(0);
                        JSONArray partList = objOneResult.getJSONArray("PartList");
                        //
                        for (int j = 0; j < partList.length(); j++) {
                            //
                            JSONObject objPart = partList.getJSONObject(j);
                            //
                            //On crée un nouvel article pour chaque "products"
                            Source produit = new Source();
                            //
                            produit.setMpn(objPart.getString("partNum"));
                            produit.setNomProduit(objPart.getString("desc"));
                            //
                            JSONObject objManuf = objPart.getJSONObject("manufacturer");
                            produit.setNomFabricant(objManuf.getString("mfrName"));
                            //
                            produit.setUid(Integer.toString(objPart.getInt("itemId")));
                            //
                            produit.setOrigine("Arrow");


                            //Création du tableau prixTab qui est un attribut de Source

                            if (optionPrix) {
                                //
                                Integer nStockTotal =0;
                                ///
                                JSONObject invOrg = objPart.getJSONObject("InvOrg");
                                //
                                this.recuperationPrix(invOrg,produit);

                                //
                            }

                            if (optionSpec && objPart.has("EnvData")) {
                                //
                                JSONObject dataEnv = objPart.getJSONObject("EnvData");
                                //
                                JSONArray listSpec = dataEnv.getJSONArray("compliance");
                                //
                                String sComplianceROHS ="";
                                ArrayList<Specs> specsItem = new ArrayList<Specs>();
                                for (int k = 0; k < listSpec.length(); k++) {
                                    //
                                    JSONObject uneSpecJson = listSpec.getJSONObject(k);
                                    //
                                    String sDisplayLabel = uneSpecJson.getString("displayLabel").trim();
                                    String sDisplayValue = uneSpecJson.getString("displayValue").trim();
                                    //
                                    if( sDisplayLabel.equals("eurohs")){
                                        sComplianceROHS = sComplianceROHS.concat(sDisplayValue+" (EU) ");
                                    }else if ( sDisplayLabel.equals("cnrohs")){
                                        sComplianceROHS = sComplianceROHS.concat(sDisplayValue+" (CN) ");
                                    }else{
                                        //
                                        Specs uneSpec = new Specs();
                                        uneSpec.setKey(sDisplayLabel);
                                        uneSpec.setValue(sDisplayValue);
                                        //
                                        specsItem.add(uneSpec); 
                                    }
                                    //
                                }
                                if (!sComplianceROHS.isEmpty()){
                                    //
                                    Specs uneSpec = new Specs();
                                    uneSpec.setROHS(sComplianceROHS);
                                    specsItem.add(uneSpec);
                                    //
                                }
                                
                                produit.setListeSpecs(specsItem);
                            }
                            //
    //                        if (optionDS && objProducts.has("resources")) {
    //                            ArrayList<Datasheet> datasheet = new ArrayList<Datasheet>();
    //                            JSONArray farnellDS = objProducts.getJSONArray("resources");
    //                            for (int k = 0; k < farnellDS.length(); k++) {
    //                                Datasheet ds = new Datasheet();
    //                                ds.setUrl(farnellDS.getJSONObject(k).getString("url"));
    //                                datasheet.add(ds);
    //                            }
    //                            produit.setDatasheet(datasheet);
    //                        }
    //
                            //
                            produit.setRangOrigine(j + 1);

                            //
                            // produit.setUrlWs("http://"+this.getMagasin()+"/"+objProducts.getString("sku"));
                            // On ajoute le produit à la liste
                            produits.add(produit);
                        }
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(Arrow.class.getName()).log(Level.SEVERE, null, ex);
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
            //this.RecuperationSKUs(this.duplicateResults);
        }
        this.enregistreCredit(this.clientID, this.nbCredit);
        System.out.println("Arrow terminé");
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
       String resultatJson = this.InterroArrow(mySearch);
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
        Arrow.TraitementMultihtreads tmt = new Arrow.TraitementMultihtreads();
        //
        if (this.getTypeRequete().equals("mpn")) {
            tmt.setNumeroATraiter(mySearchResult.getMpnOriginal());
        } else {
            tmt.setNumeroATraiter(mySearchResult.getSkuOriginal());
        }
        return tmt;
    }

}

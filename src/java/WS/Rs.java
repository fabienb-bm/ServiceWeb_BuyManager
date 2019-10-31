package WS;

import DB.WsClientDB;
import POJO.Datasheet;
import POJO.Prix;
import POJO.SearchResult;
import POJO.Source;
import POJO.Specs;
import com.essai3.ServiceResource;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
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
import org.apache.tomcat.util.codec.binary.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Nom de classe : Rs
 * <br>
 * Description : Classe permettant l'interrogation du service Web Rs.
 * <br>
 * 
 * RS web service
        https://api.webservices-rs.com
        log :PERTILIENCE
        pass : PERTILIENCEDEMO
 * 
 * Date de la dernière modification : 22/07/2014
 * 
* @author Stagiaire (Florence Giraud)
 */
public class Rs extends WsClientDB implements InterfaceWSInterrogeable, Callable<ArrayList<SearchResult>> {

    private static final int nbPoolsThreads = 6;

    /**
     * Liste des URL utilisé interroger sur le web service
     */
    static final String rsUrlsearchMPN = "https://api.webservices-rs.com/api/nodes/search"; //url de recherche de base du search
    static final String rsUrlGetSku = "https://api.webservices-rs.com/api/productv2/get";
    //
    /**
     * famille de composant interrogé
     */
    static final String parentFR = "2fe97f54-a64c-4828-9d30-0b7020bd136c"; // Famille mère langue FR
    static final String parentIT = "561f755c-5fcb-4b11-b06f-2566e1fd4dd1"; // Famille mère langue IT
    static final String parentGER = "b2be4465-cda2-442c-810b-37b29cf26389"; // Famille mère langue GER
    static final String parentEN = "8cfacf54-55c3-4c64-add0-381eb5a56c14"; // Famille mère langue EN
    static final String parentESP = "f49a33b9-cb4c-41cb-bd67-3bd786ab418a"; // Famille mère langue ESP

    //
    /**
     * paramètres de recherche
     */
    static final String pageIndex = "0";
    static final String rowLimit = "10";

    /**
     * clef par defaut => clef de pertilience : log :PERTILIENCE pass :
     * PERTILIENCEDEMO
     */
    static final String rsGenericKey = "06598e3a-73cb-4a7e-a15e-066a0c88cbb3"; //systech -> 9e82977f-af26-43ca-b055-1befea880251

    /**
     * ***********************
     * Constructeurs **********************
     */
    public Rs() {
    }

    /**
     *
     * @return
     */
    @Override
    public String getKey() {
        String mykey = ""; //To change body of generated methods, choose Tools | Templates.
        if(!super.getKey().toString().isEmpty())
        {
            byte[] decodePassword =  Base64.decodeBase64(super.getKey().toString());
            mykey = new String(decodePassword);
        }
        if (mykey.isEmpty()) {
            mykey = rsGenericKey;
        }
        return mykey;
    }

    /**
     * ***********************
     * METHODES PUBLIQUES **********************
     */
    private String InterroRs(String stUrl, String paramString) throws IOException, ParseException {

        URL url = new URL(stUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");

        //Paramètres de la connexion
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setReadTimeout(20000); // au dela de 20 s on considere que RS est HS

        try{
            //Envoi de la requête
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(paramString);
            wr.flush();
            wr.close();


            // en cas d'erreur HTTP (réponse différente de 200 - OK)
            // en cas de 500 on renvoie egalement 500 - 
            if (connection.getResponseCode() == 400 || connection.getResponseCode() == 500) {
                //
                // peut etre une erreur de RS notamment à la recuperation de SKu si le SKU n'existe pas 
                Logger.getLogger(Rs.class.getName()).log(Level.INFO, connection.getResponseMessage());
                // renvoie vide
                return "";
            } else if (connection.getResponseCode() != 200) {
                //
                throw new RuntimeException("Code erreur HTTP: " + connection.getResponseCode() + " " + connection.getResponseMessage());
                //
            } else {

                //Recupération de la réponse
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                StringBuilder response = new StringBuilder();

                //Lecture de chaque ligne
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }

                //Fermeture du buffer
                rd.close();
                //Renvoi des résultats
                return response.toString();
            }
            //
        }catch(SocketTimeoutException e){
            // cas de plantage à 20 secondes
            Logger.getLogger(Rs.class.getName()).log(Level.WARNING, connection.getResponseMessage());
            return "";
        }
    }

    /**
     * ***
     * Permet d'interroger l'API de Rs via une requête HTTP (méthode POST) et
     * renvoie un JSON sous forme de string.
     *
     * @param mpnRef Le MPN recherché.
     * @return results : Un JSON de l'ensemble des résultats de la requête.
     * @throws IOException : exception levée suite à un problème sur une entrée
     * ou une sortie (mauvais format).
     * @throws ParseException : exception levée suite à une erreur survenue
     * lorsque du « parse » du JSON (problème de sérialisation).
     */
    private String InterroRsMpn(String mpnRef) throws IOException, ParseException {

        String apikey = this.getKey(); // clef du client //9e82977f-af26-43ca-b055-1befea880251
        String parent = this.getParent();
      
        String paramString = "SessionKey=&PageIndex=" + pageIndex + "&RowLimit=" + rowLimit + "&MPN=" + mpnRef + "&Parent=" + parent + "&ApiKey=" + apikey;

        return InterroRs(rsUrlsearchMPN, paramString);

    }

    /**
     * ***
     * Permet d'interroger l'API de Rs via une requête HTTP (méthode POST) et
     * renvoie un JSON sous forme de string.
     *
     * @param sku Le SKU recherché.
     * @return results : Un JSON de l'ensemble des résultats de la requête.
     * @throws IOException : exception levée suite à un problème sur une entrée
     * ou une sortie (mauvais format).
     * @throws ParseException : exception levée suite à une erreur survenue
     * lorsque du « parse » du JSON (problème de sérialisation).
     */
    private String InterroRsSKU(String sku) throws IOException, ParseException {

        String apikey = this.getKey(); // clef du client //9e82977f-af26-43ca-b055-1befea880251
        String parent = this.getParent();
        
        String paramString = "SessionKey=&Sku=" + sku + "&Node=" + parent + "&ApiKey=" + apikey;

        return InterroRs(rsUrlGetSku, paramString);

    }

    /**
     * 
     * @return renvoie vrai si la login est remplis
     */
     @Override
    public boolean getCustomerPriceAvailable(){
        return getKey() != rsGenericKey;
    }
    
    
    private Source jsonProductToSource(JSONObject objProducts, String ref, int rang) throws JSONException {

        //On crée un nouvel article 
        Source produit = new Source();

        produit.setMpn(objProducts.getString("ManufacturerReference"));
        produit.setNomProduit(objProducts.getString("Title"));
        produit.setNomFabricant(objProducts.getString("Manufacturer"));
        produit.setUid(objProducts.getString("Id"));
        produit.setUrlWs(objProducts.getString("Url"));
        produit.setOrigine("RS");

        // test le string car null etst pas objet...    
        String stockString = (objProducts.getString("Stock"));

        int stockInt = 0;
        //
        // si le stock pointe sur null, il vaut zéro
        String sCycleLife = "";
        if ( ! "null".equals(stockString)) {
            //
            //{"Status":"DISCONTINUED","Quantity":0,"PartialStockQuantity":0,"FutureStockDate":null}
            // OU "Stock": {
            //        "SkuNumber": "126622",
            //        "Referral": "7980818",
            //        "Total": 60,
            //        "Discontinuation": {
            //            "Code": "D2",
            //            "Date": "2018-09-21"
            //        },
            //        "Warehouses": [
            //        ]

            
            //
            JSONObject stockObj = (objProducts.getJSONObject("Stock"));
            //
            if (stockObj.has("Discontinuation") && ! stockObj.isNull("Discontinuation")) {
                //
                JSONObject disContObj = stockObj.getJSONObject("Discontinuation");
                sCycleLife = sCycleLife.concat("Discontinuation ");
                sCycleLife = sCycleLife.concat(disContObj.getString("Code")+" ");
                sCycleLife = sCycleLife.concat(disContObj.getString("Date"));   
                //
            }
            
            
            if (stockObj.has("Quantity")) {
                //
                stockInt = stockObj.getInt("Quantity");
                //
            }else if (stockObj.has("Total")) {
                //
                stockInt = stockObj.getInt("Total");
            }
            //            
        }
        //
        int quantityOrderMultiplier = objProducts.getInt("QuantityOrderMultiplier");
        int packaging = 0;
        try {
            packaging = Integer.valueOf(objProducts.getString("PackSize"));
        } catch (NumberFormatException e) {
            // nothing
        }
        //
        if (quantityOrderMultiplier > 0 && quantityOrderMultiplier != packaging) {
            //
            produit.setStock(stockInt * packaging / quantityOrderMultiplier);
            //
        } else {
            produit.setStock(stockInt);
        }
        //

        produit.setRangOrigine(rang + 1);

        //Liste des prix
        //Création du tableau de prixTab qui est un attribut de Source
        ArrayList<Prix> prixList = new ArrayList<Prix>();
        //si on prend l'option d'afficher les prix
        if (optionPrix) {
            prixList = Rs.recuperationPrix(objProducts, prixList);
        }
        if (optionSpec && objProducts.has("Attributes")) {
            JSONArray listSpec = objProducts.getJSONArray("Attributes");
            ArrayList<Specs> specsItem = new ArrayList<Specs>();
            for (int k = 0; k < listSpec.length(); k++) {
                Specs uneSpec = new Specs();
                JSONObject uneSpecJson = listSpec.getJSONObject(k);
                uneSpec.setKey(uneSpecJson.getString("Title").replace("é", "e").replace("è", "e").replace("î", "i").replace("°", "").replace("à", "a").replace("ê", "e").replaceAll("[^\\x00-\\x7F]", " ").trim());
                uneSpec.setValue(uneSpecJson.getString("Value").replace("é", "e").replace("è", "e").replace("î", "i").replace("°", "").replace("à", "a").replace("ê", "e").replaceAll("[^\\x00-\\x7F]", " ").trim());
                specsItem.add(uneSpec);
            }
            produit.setListeSpecs(specsItem);
            //Autres specs 
            if(objProducts.has("RoHSStatus")){
                Specs uneSpec = new Specs();
                uneSpec.setROHS(objProducts.getString("RoHSStatus"));
                specsItem.add(uneSpec);
            }
            // ajout cycle de vie
            if (! sCycleLife.isEmpty()){
                Specs uneSpec = new Specs();
                uneSpec.setLifeCycle(sCycleLife);
                specsItem.add(uneSpec);
            }
        }
        if (optionDS && objProducts.has("Documents")) {
            ArrayList<Datasheet> datasheet = new ArrayList<Datasheet>();
            JSONArray rsDS = objProducts.getJSONArray("Documents");
            for (int i = 0; i < rsDS.length(); i++) {
                Datasheet ds = new Datasheet();
                ds.setUrl(rsDS.getJSONObject(i).getString("Link"));
                datasheet.add(ds);
            }
            produit.setDatasheet(datasheet);
        }
        //si optionParam=true, ListePrix est remplie, si optionParam=false, ListePrix est initialisée à null
        produit.setListePrix(prixList);
        return produit;
    }

    /**
     * Permet, après connection, de récupérer le JSON sous forme de string puis
     * de le parser et de l'intégrer dans un tableau de Source.
     *
     * @param ref Le MPN recherché.
     * @param clientID l'identifiant du client.
     * @return produits : Tableau de Source correspondant au résultat de la
     * requête parsé et inclus dans des objets de type Source.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     */
    private ArrayList<Source> RecuperationArticleFromMPN(String ref) throws JSONException {
        try {
            // si la référence ets vide ce n'est pas la peine de constinuer
            if (ref.isEmpty()) {
                //
                return null;
            }else{
                //on recupere un string correspondant au résultat de la requete           
                String resultat = this.InterroRsMpn(ref);
                // si pas de résultat on ne traite pas
                if (!resultat.isEmpty()) {
                    //On crée une nouvelle liste d'articles
                    ArrayList<Source> produits = new ArrayList<Source>();

                    //on recupere le JSON complet
                    JSONObject jsonObject = new JSONObject(resultat);

                    JSONArray products = jsonObject.getJSONArray("Products");

                    for (int i = 0; i < products.length(); i++) {
                        //on crée un objet de chaque "product"
                        JSONObject objProducts = products.getJSONObject(i);
                        Source produit = this.jsonProductToSource(objProducts, ref, i);
                        //On ajoute le produit à la liste
                        produits.add(produit);
                    }
                    return produits;
                } else {
                    return null;
                }
            }
        } catch (JSONException e) {
            Logger.getLogger(Rs.class.getName()).log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        } catch (IOException ex) {
            Logger.getLogger(Rs.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (ParseException ex) {
            Logger.getLogger(Rs.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Permet de remplir un tableau de SearchResult avec les résultats de la
     * requête. Initialise également les pools de threads
     *
     * THREADS
     */
    @Override
    public void RecuperationMPNs(ArrayList<SearchResult> arrSearchResult) {
        procedureThreads(arrSearchResult);
    }

    /**
     * Permet de récupérer les prix des articles.
     *
     * @param objProducts objet Json contenant les prix, à parser.
     * @param prixList liste de prix à retourner complétée.
     * @return prixListe: liste de prix complétée.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     */
    private static ArrayList<Prix> recuperationPrix(JSONObject objProducts, ArrayList<Prix> prixList) throws JSONException {
        String lastUpdate = objProducts.getString("TimeLastModified").substring(0, 10);
        JSONArray prices = objProducts.getJSONArray("Prices");

        for (int i = 0; i < prices.length(); i++) {
            //Recuperation des prixTab;
            Prix prixTab = new Prix();

            // On récupère le tableau d'objets spécifique à chaque entrée de "Prices"
            JSONObject objPrix = prices.getJSONObject(i);
            prixTab.setLastUpdate(lastUpdate);
            prixTab.setQuantite(objPrix.getInt("QuantityFrom"));
            //
            prixTab.setPrix(objPrix.getDouble("PublicPrice"));
            //
            
            
            // test le string car null n'est pas objet...    
            String stockString = (objProducts.getString("Stock"));
            int stockInt =  0;
            int delais = 0;
            String sRegionStock = "" ;
            // si le stock pointe sur null, il vaut zéro
            if (! "null".equals(stockString)) {
                //{"Status":"DISCONTINUED","Quantity":0,"PartialStockQuantity":0,"FutureStockDate":null}
                JSONObject stockObj = (objProducts.getJSONObject("Stock"));
                
                if (stockObj.has("Quantity")) {
                    //
                    stockInt = stockObj.getInt("Quantity");
                    //
                }else if (stockObj.has("Total")) {
                    //
                    stockInt = stockObj.getInt("Total");
                    //
                    JSONArray stockRegions = stockObj.getJSONArray("Warehouses");
                    for (int itStk = 0; itStk < stockRegions.length(); itStk++) {
                        //
                        JSONObject uneRegion = stockRegions.getJSONObject(itStk);
                        String sCodeRegion = uneRegion.getString("Code");
                        //
                        sRegionStock = sRegionStock.concat(" " +sCodeRegion);
                        //
                        int intLeadTime = uneRegion.getInt("LeadTime");
                        delais = Integer.max(delais,intLeadTime);
                        //
                    }
                    //
                }
            }
            //
            prixTab.setStock(stockInt);
            prixTab.setStockRegions(sRegionStock);
            prixTab.setLeadDays(delais);
            //
            prixTab.setDevise(objPrix.getString("CurrencySymbol"));
            prixTab.setSku(objProducts.getString("SkuNumber"));
            prixTab.setFournisseur("RS (direct Public)");
            prixTab.setDistribValide(true);
            
             //
            // surcharge pour les condionnement de production
            if(prixTab.getSku().endsWith("P")){
                prixTab.setPackaging("(P)"+objProducts.getString("SalesUnitDescription"));   
            }else{
                prixTab.setPackaging(objProducts.getString("SalesUnitDescription")); 
            }
           
            
            
            // le MOQ est un recopie de la quantité
            prixTab.setMoq(prixTab.getQuantite());

            int quantityOrderMultiplier = objProducts.getInt("QuantityOrderMultiplier");

            int packaging = 0;

            try {
                packaging = Integer.valueOf(objProducts.getString("PackSize"));
            } catch (NumberFormatException e) {
                // nothing
            }

            //
            if (quantityOrderMultiplier > 0 && quantityOrderMultiplier != packaging) {
                //
                prixTab.applyPackSize(packaging / quantityOrderMultiplier);
                //
            }
            else {
                  prixTab.setMpq(packaging);
            }
            //
            
            //
            prixTab.setStockRegions("FR");
            //
            prixList.add(prixTab);
            //
            
            try {
                Prix prixNeg = (Prix) prixTab.clone();
                Double CustomerPrice = objPrix.getDouble("CustomerPrice");
                prixNeg.setPrix(CustomerPrice);
                if (quantityOrderMultiplier > 0 && quantityOrderMultiplier != packaging) {
                    //
                    float coeff = (packaging / quantityOrderMultiplier);
                    if (coeff > 0) {
                        prixNeg.setPrix(CustomerPrice/coeff);
                    }
                     //
                    //prixNeg.applyPackSize(packaging / quantityOrderMultiplier); ne pas appliqué car les valeurs sont préinit avant clone
                    //
                }
                else {
                    prixNeg.setMpq(packaging);
                }
                
                prixNeg.setFournisseur("RS (direct Nego)");
                prixList.add(prixNeg);
               
            } catch (JSONException e) {
                // pas sur que le prix client soit toujours dispo // securité...
            }
        }
        return prixList;
    }

    /**
     * Permet, après connection, de récupérer le JSON sous forme de string puis
     * de le parser et de l'intégrer dans un tableau de Source.
     *
     * @param ref Le MPN recherché.
     * @param clientID l'identifiant du client.
     * @return produits : Tableau de Source correspondant au résultat de la
     * requête parsé et inclus dans des objets de type Source.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     */
    private ArrayList<Source> RecuperationArticleFromSKU(String sku) throws JSONException {
        try {
            //on recupere un string correspondant au résultat de la requete           
            String resultat = this.InterroRsSKU(sku);
            ArrayList<Source> produits = new ArrayList<Source>();
            if (!resultat.isEmpty()) {
                //on recupere le JSON complet
                JSONObject jsonObject = new JSONObject(resultat);

                //on crée un objet de chaque "product"
                JSONObject objProducts = jsonObject.getJSONObject("Product");

                //On crée un nouvel article 
                Source produit = this.jsonProductToSource(objProducts, "", 0);

                //On ajoute le produit à la liste
                produits.add(produit);
                return produits;
            } else {
                return null;
            }

        } catch (JSONException | IOException | ParseException e) {
            Logger.getLogger(Rs.class.getName()).log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void RecuperationSKUs(ArrayList<SearchResult> sourcesListe) {
        procedureThreads(sourcesListe);
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
        System.out.println("RS terminé");
        return duplicateResults;
    }

    private String getParent() {
        String country = super.getCountry();
        if (country.equals("FR")) {
            return parentFR;
        } else if (country.equals("EN")) {
            return parentEN;
        } else if (country.equals("IT")) {
            return parentIT;
        } else if (country.equals("GER")) {
            return parentGER;
        } else if (country.equals("ESP")) {
            return parentESP;
        } else {
            return parentFR;
        }

    }

    @Override
    public void RecuperationDesc(ArrayList<SearchResult> arrSearchResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getErrorStatus() {
        //
        String resultat;
        //
        try {
            resultat = this.InterroRsMpn("ABCD");
        } catch (IOException ex) {
            return true;
        } catch (ParseException ex) {
            return true;
        }
        // si pas de résultat on ne traite pas
        return resultat.isEmpty();
        //
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
                return RecuperationArticleFromMPN(number);
            } else if (requete.equals("sku")) {
                return RecuperationArticleFromSKU(number);
            }
            return null;
        }

    }

    /*
     * @param mySearchResult
     * @return Callable<ArrayList<Source>>
     */
    @Override
    protected Callable<ArrayList<Source>> getTMT(SearchResult mySearchResult){
        Rs.TraitementMultihtreads tmt = new Rs.TraitementMultihtreads();
        //
        if (this.getTypeRequete().equals("mpn")) {
            tmt.setNumeroATraiter(mySearchResult.getMpnOriginal());
        } else {
            tmt.setNumeroATraiter(mySearchResult.getSkuOriginal());
        }
        return tmt;
    }
    
}

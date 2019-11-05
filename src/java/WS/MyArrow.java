package WS;

import DB.WsClientDB;
import POJO.Prix;
import POJO.SearchResult;
import POJO.Source;
import POJO.Specs;
import static com.google.common.io.BaseEncoding.base64;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Integer.parseInt;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import org.apache.tomcat.util.codec.binary.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
//
///**
// * Nom de classe : MyArrow
// * Description : Classe permettant l'interrogation du service MyArrow.
// * 
// * @author Alternant (Lagarrigue Thibault)
// */
public class MyArrow extends WsClientDB implements InterfaceWSInterrogeable, Callable<ArrayList<SearchResult>> {

//===============================================================================   my arrow   ===============================================================================================

    private static final int nbPoolsThreads = 6;
    static String MYARROWKEY = "";
        /**
     * ***********************
     * Constructeurs
     *
     * @return String **********************
     */
    @Override
    public String getKey() {
        String mykey = super.getKey(); //To change body of generated methods, choose Tools | Templates.
        return mykey;
    }
    
    @Override
    public String getLogin()
    {
        String login = "";
        if(!super.getLogin().isEmpty())
        {
            login = "&client_id="+super.getLogin();
        }
        
        return login;
    }
    
    @Override
    public String getPassword()
    {
        String password = "&client_secret="+super.getPassword().toString();
        //if(!super.getPassword().toString().isEmpty())
        //{
            //byte[] decodePassword =  Base64.decodeBase64(super.getPassword().toString());
            //password = "&client_secret="+new String(decodePassword);
        //}
        return password;
    }
    
    public String getAuthentification()
    {
        String authentification = this.getLogin()+this.getPassword();
        System.out.println("=================================");
        System.out.println(authentification);
        return authentification;
    }
    
    public String getDevise() {
        String myDevise = this.getMagasin();
        return myDevise;
    }
        
    /**
     * Constructeur vide de MyArrow
     */
    public MyArrow(){
        

    }
    
    
    public String getJSONDATA(String MyKey,String mySearch,String ClientID) throws IOException
    {
        //Represents a mutable string
        StringBuilder sb2 = new StringBuilder();
        
        String search = "currency="+this.getDevise()+"&limit=1000000000&search="+mySearch.toLowerCase();
        
        // Create url with current search => Currency, limit and part name
        URL url2 = new URL("https://my.arrow.com/api/priceandavail/search?"+search);

        // open connection
        HttpURLConnection urlConnGetData = (HttpURLConnection) url2.openConnection();

        // Curl request with different property
        urlConnGetData.setRequestProperty("X-Requested-With","Curl");
        urlConnGetData.setRequestProperty("accept", "application/json");
        urlConnGetData.setRequestProperty("authorization", "Bearer "+ MyKey);
        urlConnGetData.setRequestProperty("Content-Type", "application/json");
        urlConnGetData.setRequestProperty("cache-control", "no-cache");
        urlConnGetData.setRequestProperty("client_id", ClientID);
        //Start request
        urlConnGetData.connect();

        BufferedReader br2 = new BufferedReader(new InputStreamReader(urlConnGetData.getInputStream()));

        String line2 = "";

        // Rewrite result 
        while((line2 = br2.readLine())!=null)
        {
            sb2.append(line2 + "\n");
        }    

        // and return result (its json from MyArrow api)
        return sb2.toString();  
    }
    /**
     * ***********************
     * METHODES PUBLIQUES **********************
     */
    /**
     * Permet l'interrogation de l'API MyArrow et retourne une réponse sous
     * format JSON/String.
     *
     * @param mySearch
     * @param myFunction
     * @return Un JSON de l'ensemble des résultats de la requête.
     */
    private String InterroMyArrow(String mySearch) throws MalformedURLException, IOException, JSONException{

        String myKey = "";
        String json = "";
        //Represents a mutable string
        StringBuilder sb = new StringBuilder();
        //Get client login and password from Account RQT
        String ClientID = this.getLogin();
        String ClientSecret = this.getPassword();
        if(!ClientID.isEmpty() && !ClientSecret.isEmpty())
        {
            // Create url to create a authentification token
            URL url = new URL("https://my.arrow.com/api/security/oauth/token?grant_type=client_credentials"+this.getAuthentification());
            HttpURLConnection urlConnGetToken = (HttpURLConnection) url.openConnection();
            // Request curl 
            urlConnGetToken.setRequestProperty("X-Requested-With","Curl");
            // Method post
            urlConnGetToken.setRequestMethod("POST");
            // Get ClientId in Account RQT
            urlConnGetToken.setRequestProperty("client_id", ClientID); 
            // Start
            urlConnGetToken.connect();

            // Catch the response
            try{
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnGetToken.getInputStream()));

                String line = "";

                // Check and rewrite response
                while((line = br.readLine())!=null)
                {
                    sb.append(line + "\n");
                }  

                // Get and create JsonObject from sb 
                JSONObject objProducts = new JSONObject(sb.toString());  
                // Get access token value
                String MyKey = objProducts.getString("access_token");
                // Disconnect
                urlConnGetToken.disconnect();

                // send my key, my search(part name in buymanager), and client ID
                 json = this.getJSONDATA(MyKey,mySearch,ClientID);
            }catch(IOException iex)
            {
                json = "";
            }
            
        }
        // Return json MyArrow from part name search
        return json;
         
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
    private ArrayList<Source> RecuperationMPN(String mpn) throws JSONException, IOException {
        //
        if ( mpn.isEmpty()){

            return null;
        }else{
            //on recupére un String correspondant au JSON résultant de la requete     
            String resultatJson = this.InterroMyArrow(mpn);
            //
            if (!resultatJson.isEmpty() && resultatJson != null) {
                
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

    public ArrayList<Prix> recuperationPrix(JSONObject objPrice,ArrayList prixListMyArrow) throws JSONException {
        
        JSONArray objpriceTier = objPrice.getJSONArray("pricingTier");

        //loop in all price from current response
            for(int priceIndex = 0; priceIndex < objpriceTier.length(); priceIndex++)
            {
                JSONObject objpriceCurrent = objpriceTier.getJSONObject(priceIndex);
                
                Prix prixTab = new Prix(); 
                if(objPrice.has("currency"))
                {
                    prixTab.setDevise(objPrice.getString("currency"));
                }
                if(objPrice.has("partNumber"))
                {
                    prixTab.setSku(objPrice.getString("partNumber"));
                }
                if(objPrice.has("minOrderQuantity"))
                {
                    prixTab.setMoq(objPrice.getInt("minOrderQuantity"));
                }
                if(objPrice.has("supplier"))
                {
                    prixTab.setFournisseur(objPrice.getString("supplier"));
                }            
                if(objPrice.has("pkg"))
                {
                    prixTab.setPackaging(objPrice.getString("pkg"));
                }
                if(objPrice.has("leadTime"))
                {
                     JSONObject objLeadTime = objPrice.getJSONObject("leadTime");

                     if(objLeadTime.has("arrowLeadTime"))
                     {
                         prixTab.setLeadDays(objLeadTime.getInt("arrowLeadTime") * 7);
                     }     
                    /* if(objLeadTime.has("supplierLeadTimeDate"))
                     {
                         prixTab.setLastUpdate(objLeadTime.getString("supplierLeadTimeDate"));
                     }*/
                }
                if(objpriceCurrent.has("minQuantity"))
                {
                     prixTab.setQuantite(parseInt(objpriceCurrent.getString("minQuantity")));
                }
                if(objpriceCurrent.has("resalePrice"))
                {
                     prixTab.setPrix(Double.parseDouble(objpriceCurrent.getString("resalePrice")));
                } 
                if(objPrice.has("fohQuantity"))
                {
                    prixTab.setStock(parseInt(objPrice.getString("fohQuantity")));
                }
                if(objPrice.has("warehouseCode"))
                {
                    prixTab.setStockRegions(objPrice.getString("warehouseCode"));
                }
                if(objPrice.has("multOrderQuantity"))
                {
                    prixTab.setMpq(objPrice.getInt("multOrderQuantity"));
                }
                
                prixListMyArrow.add(prixTab);
            }
            
        return prixListMyArrow;
    }
    
   public Source MPNData(Source produit,JSONObject objResponse, ArrayList prixListMyArrow,ArrayList specsItem) throws JSONException
    {
        if(objResponse.has("description"))
        {
             produit.setNomProduit(objResponse.getString("description"));
        }
        if(objResponse.has("arwPartNum"))
        {
            JSONObject arrPartNum = objResponse.getJSONObject("arwPartNum");
            
            if(arrPartNum.has("name") && !arrPartNum.getString("name").isEmpty())
            {
                produit.setMpn(arrPartNum.getString("name"));
            }
        }
        if(objResponse.has("fohQuantity"))
        {
            produit.setStock(parseInt(objResponse.getString("fohQuantity")));
        }
        if(objResponse.has("manufacturer"))
        {
             produit.setNomFabricant(objResponse.getString("manufacturer"));
        }
        //Add new price in our current list price 
        this.recuperationPrix(objResponse,prixListMyArrow);
        produit.setListePrix(prixListMyArrow);
        produit.setOrigine("MyArrow");

        return produit;
    }
    
     public Source CurrentROHs(JSONObject objResponse,ArrayList<Specs> specsItem,Source produit,ArrayList rohs,String sRohs) throws JSONException
    {
        //verify if we have EU rohs data
        if(!objResponse.getString("euRohs").isEmpty() && objResponse.has("euRohs"))
        {
            //verify if we have china Rohs data
             if(objResponse.has("chinaRohs") && !objResponse.getString("chinaRohs").isEmpty() && !rohs.contains("euRohs: "+objResponse.getString("euRohs").toLowerCase()+ "/ chinaRohs: "+objResponse.getString("chinaRohs").toLowerCase()))
             {
                 //Add eu and china rohs found in our list rohs
                 sRohs = "euRohs: "+objResponse.getString("euRohs").toLowerCase()+ "/ chinaRohs: "+objResponse.getString("chinaRohs").toLowerCase();
                 rohs.add(sRohs);
             }
             else
             {
                 //Add eu rohs found in our list rohs
                 if(!rohs.contains("euRohs: "+objResponse.getString("euRohs").toLowerCase()))
                 {
                     sRohs = "euRohs: "+objResponse.getString("euRohs").toLowerCase();
                     rohs.add(sRohs);
                 }
             }
        }
        else
        {
            //Add china rohs found in our list rohs
            if(!rohs.contains("chinaRohs: "+objResponse.getString("chinaRohs").toLowerCase()))
            {
             sRohs = "chinaRohs: "+objResponse.getString("chinaRohs").toLowerCase();
             rohs.add(sRohs);
            }
        }

       // If we have found a same part and sRohs isn't empty
       if(!sRohs.isEmpty() && specsItem.size()>0)
       {
            Specs specs = new Specs();
            //Catch our list from current produit and we add the current rohs value 
            specs.setROHS(specsItem.get(0).getValue() + ", "+ sRohs);
            //Clear old list
            specsItem.clear();                        
            //Add new list with new value
            specsItem.add(specs);
       }else
       {
           //If first part name found
            if(!sRohs.isEmpty())
           {
                Specs specs = new Specs();
                //Add value in our specs and in our specs list
                specs.setROHS(sRohs);
                //Add new list with new value
                specsItem.add(specs);
                //Add in our produit new list
           }
       }
       return produit;            
    };
        
    private ArrayList<Source> JsonToArrSource(String resultatJson, String mpn) throws JSONException {
        //Table 
        ArrayList<Source> produits = new ArrayList<Source>();
            
        //verify if our json exist
        if(!resultatJson.isEmpty() && resultatJson != null)
        {
            // Catch response from Findchips API in JsonObject 
            JSONObject objProducts = new JSONObject(resultatJson);
            // Catch all the responses from the jsonobject structure in a JSONarray
            JSONArray response = objProducts.getJSONArray("pricingResponse");  
            int StockTotalMPN = 0 ;
            String sRohs = "";
            Specs specs = null;
            Source produit = null;       
            //Table 
            ArrayList rohs = new ArrayList();
            ArrayList tabMPN = new ArrayList();
            ArrayList<Prix> prixListMyArrow = new ArrayList<Prix>();
            ArrayList<Specs> specsItem = new ArrayList<Specs>();       
            //
            int indice = 0;

           //loop in all responses from FindChips API
           for(int indexResponse = 0; indexResponse < response.length(); indexResponse++)
           {
                //get JSONObject according to the current iResponse
                JSONObject objResponse = response.getJSONObject(indexResponse);   
                //Get Current part to our parts response
                JSONObject currentobjectMPN = objResponse.getJSONObject("arwPartNum");     
                String currentMPN = currentobjectMPN.getString("name");
                //check if current part exists in our table
                if(tabMPN.contains(currentMPN))
                {
                    //loop in all MPn in our taMPN
                    for(int indextabMPN = 0; indextabMPN < tabMPN.size(); indextabMPN++)
                    {
                        //try to find indice of current mpn
                        if(tabMPN.get(indextabMPN).equals(currentMPN))
                        {              
                            //Get our produit
                             produit = produits.get(indextabMPN); 
                             //Get price list from produit
                             prixListMyArrow = produit.getListePrix();
                             //Get specs list from produit
                             specsItem = produit.getListeSpecs();
                             //Get rohs added in our list
                             rohs = produit.getRohsProduit();
                             //Add new price to our current list price 
                             this.recuperationPrix(objResponse,prixListMyArrow);  
                             //Add new spec to our current list specs     
                             this.CurrentROHs(objResponse,specsItem,produit,rohs,sRohs);                            
                             //Check if price list is empty. Can't add stock when price list is empty
                             JSONArray sourceAreaPrice = objResponse.getJSONArray("pricingTier");
                             if(sourceAreaPrice.length() > 0)
                             {
                                 if(objResponse.has("fohQuantity"))
                                 {
                                     //we add to our stock the current stock
                                     StockTotalMPN = produit.getStock() + objResponse.getInt("fohQuantity");
                                     //Set result
                                     produit.setStock(StockTotalMPN);
                                 }
                             }                       
                        }
                    }                              
                }
                else
                {                  
                    //Create new produit
                    produit = new Source(); 
                    //Clear our price list
                    prixListMyArrow = new ArrayList<Prix>();
                    //Clear our specs list
                    specsItem = new ArrayList<Specs>();
                    //Clear our specs list
                    rohs = new ArrayList();
                    //Get mpn / manucfaturer / description of our new prouit
                    this.MPNData(produit,objResponse,prixListMyArrow,specsItem); 
                    // Get Rohs current
                    this.CurrentROHs(objResponse,specsItem,produit,rohs,sRohs);
                    //Add in our produit new list
                    produit.setRohsProduit(rohs);
                    produit.setListeSpecs(specsItem);
                    //Check if price list is empty. Can't add stock when price list is empty
                    JSONArray sourceAreaPrice = objResponse.getJSONArray("pricingTier");
                    if(sourceAreaPrice.length() > 0)
                    {
                         //Set stock to our current mpn in our produit
                        if(objResponse.has("fohQuantity"))
                        {
                             StockTotalMPN = objResponse.getInt("fohQuantity");
                             produit.setStock(StockTotalMPN);
                        }
                    }
                    //Add our produit to our produits list
                    produits.add(produit);
                    //Add new MPN found in our table mpn
                    tabMPN.add(currentMPN);
                }   
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
        System.out.println("MyArrow terminé");
        return duplicateResults;
    }


    @Override
    public void RecuperationDesc(ArrayList<SearchResult> arrSearchResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getErrorStatus() {
       String mySearch = "sm6t36ca";
       /// on test une fonction SKu car devrait etre plus rapide
       String resultatJson = null;
        try {
            resultatJson = this.InterroMyArrow(mySearch);
        } catch (IOException ex) {
            Logger.getLogger(MyArrow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(MyArrow.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        MyArrow.TraitementMultihtreads tmt = new MyArrow.TraitementMultihtreads();
        //
        if (this.getTypeRequete().equals("mpn")) {
            tmt.setNumeroATraiter(mySearchResult.getMpnOriginal());
        } else {
            tmt.setNumeroATraiter(mySearchResult.getSkuOriginal());
        }
        return tmt;
    }

}

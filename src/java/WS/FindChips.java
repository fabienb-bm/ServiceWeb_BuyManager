//Current folder for this file
package WS;
//Project class
import DB.WsClientDB;
import POJO.Prix;
import POJO.SearchResult;
import POJO.Source;
import POJO.Specs;
//Java library 
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
//Http request library
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.tomcat.util.codec.binary.Base64;
//Json library
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Nom de classe : FindChips
 * Description : Classe permettant l'interrogation du service FindChips.
 * 
 * @author Alternant (Lagarrigue Thibault)
 */
public class FindChips extends WsClientDB implements InterfaceWSInterrogeable, Callable<ArrayList<SearchResult>> {

    //Default BM key for test (not used for the customer)
    static final String FINDCHIPSKEY = "m79wo9S4nrsxG";
    //Link Findchips API
    static final String SERVICETOKEN = "http://api.findchips.com/v1/search?";

    private static final int nbPoolsThreads = 6;
    
    /**
     * ***********************
     * Constructeurs
     *
     * @return String **********************
     */
    @Override
    public String getKey() {
        //Search your Findchips Key due to your clientID and wsID (Webservice)
        String mykey = "";
        if(!super.getKey().toString().isEmpty())
        {
            byte[] decodeKey =  Base64.decodeBase64(super.getKey().toString());
            mykey = new String(decodeKey);
        }
        return mykey;
    }
    /**
     * Class constructor 
     */
    public FindChips() {

    }

    /**
     * Call FindChips API
     * @param mySearch
     * @param myFunction
     * @return Json response to findchips request about mpn
     */
    private String InterroFindChips(String mySearch) {

        OkHttpClient client = new OkHttpClient();
        //
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");    
        //Build url for call findchips api
        String totalUrl = SERVICETOKEN;
        totalUrl = totalUrl.concat("&apiKey="+this.getKey()+"&part="+mySearch);       
        Request request = new Request.Builder()
                    .url(totalUrl)
                    .build();     
        // try to execute the request
        // if succesfull => return json response
        // else return null and create log error
        try {
            okhttp3.Response response = client.newCall(request).execute();
            if ( response.isSuccessful() ) {
                return response.body().string();
            }else{
                //
                Logger.getLogger(FindChips.class.getName())
                      .log(Level.WARNING,"Erreur HTTP "+response.code()+ " : "+ response.body().string(),response);
                return "";
            }     
        } catch (IOException ex) {
            Logger.getLogger(FindChips.class.getName()).log(Level.SEVERE, null, ex);
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
            String resultatJson = this.InterroFindChips(mpn);
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
        //String resultatJson = this.InterroFindChips(sku);
        //
        ArrayList<Source> produits = new ArrayList();//this.JsonToArrSource(resultatJson, sku);
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
    public ArrayList<Prix> recuperationPrix(JSONObject objDistributor,JSONObject objPart,ArrayList prixListFindchips) throws JSONException {

        JSONArray sourceAreaPrice = objPart.getJSONArray("price");

        String elementSupp[] = {"</br>", "<br/>", "<b>" , "</b>", "<span style=white-space:nowrap;>", "</span>",  "<span style="+'"'+"white-space:nowrap;"+'"'+">"};

            for(int JAreaPartsPrice = 0; JAreaPartsPrice < sourceAreaPrice.length(); JAreaPartsPrice++)
            {
                JSONObject objPartPrice = sourceAreaPrice.getJSONObject(JAreaPartsPrice);
                
                Prix prixTab = new Prix();

                if(objDistributor.has("name"))
                {
                    prixTab.setFournisseur(objDistributor.getString("name"));
                }

                if(objPart.has("lastUpdated"))
                {
                    prixTab.setLastUpdate(objPart.getString("lastUpdated"));
                }

                if(objPart.has("minimumQuantity"))
                {
                    prixTab.setMoq(objPart.getInt("minimumQuantity"));
                }

                if(objPart.has("packageMultiple"))
                {
                    prixTab.setMpq(objPart.getInt("packageMultiple"));
                }

                if(objPart.has("distributorItemNo"))
                {
                    prixTab.setSku(objPart.getString("distributorItemNo"));     
                }

                if(objPart.has("stock"))
                {
                    prixTab.setStock(objPart.getInt("stock"));                 
                }

                if(objPart.has("packageType"))
                {
                   prixTab.setPackaging(objPart.getString("packageType"));
                }

                if(objPart.has("stockIndicator"))
                {

                    String Indicator = objPart.getString("stockIndicator");

                    for(int listElement = 0; listElement < elementSupp.length ; listElement++)
                    {

                        Indicator = Indicator.replace(elementSupp[listElement], " ");
                        Indicator = Indicator.trim();
                    }

                    prixTab.setStockRegions(Indicator);
                }
                
                if(objPart.has("leadTime"))
                {               

                    // leatime = "1 week, 2 days" par exemple et on split par les virgules
                    String leadTimeStr = objPart.getString("leadTime");

                    if (leadTimeStr.indexOf(',')!=-1)
                        {

                          String[] leadTime = leadTimeStr.split(",");

                          // "1 week"
                          String weeks = leadTime[0];

                         // "2 days"
                          String days = leadTime[1];

                          //split ces resultats par les espaces
                          String[] WeekNumber = weeks.split(" ");
                          String[] DaysNumber = days.split(" ");

                          int WeekNumberInt = Integer.parseInt(WeekNumber[0]);
                          int DaysNumberInt = Integer.parseInt(DaysNumber[1]);

                          int LeadTime = (WeekNumberInt * 7) + DaysNumberInt;
                          prixTab.setLeadDays(LeadTime);
                        }
                    else
                        {
                          // "1 week"
                          String[] WeekNumber = leadTimeStr.split(" ");

                          String weeks = WeekNumber[1].toLowerCase();

                          if(weeks.indexOf("weeks")!=-1)
                          {
                              int WeekNumberInt = Integer.parseInt(WeekNumber[0]);
                              //Calendar days so mulplication by 7
                              int LeadTime = (WeekNumberInt * 7);
                              prixTab.setLeadDays(LeadTime);
                          }
                          else
                          {
                              int DayNumberInt = Integer.parseInt(WeekNumber[0]);
                              int LeadTime = DayNumberInt;
                              prixTab.setLeadDays(LeadTime);
                          }

                        }     
                }
                else
                {
                    prixTab.setLeadDays(0);
                }

                if(objPartPrice.has("price"))
                {
                    prixTab.setPrix(objPartPrice.getDouble("price"));                 
                }
                
                if(objPartPrice.has("quantity"))
                {
                    prixTab.setQuantite(objPartPrice.getInt("quantity"));
                }
                
                if(objPartPrice.has("currency"))
                {
                    prixTab.setDevise(objPartPrice.getString("currency"));
                }

                prixListFindchips.add(prixTab);
            }
 
        
         
        return prixListFindchips ;
        
    }

    private ArrayList<Source> JsonToArrSource(String resultatJson, String mpn) throws JSONException {
        // Catch response from Findchips API in JsonObject 
        JSONObject objProducts = new JSONObject(resultatJson);
        // Catch all the responses from the jsonobject structure in a JSONarray
        JSONArray response = objProducts.getJSONArray("response"); 
        //Init var
        int StockTotalMPN = 0 ;
        int indice = 0;
        String currentMPN = "";
        String sRohs = "";
        Source produit = null;       
        //Init tables 
        ArrayList<Source> produits = new ArrayList<Source>();
        ArrayList tabRohs = new ArrayList();
        ArrayList tabMPN = new ArrayList();
        ArrayList<Prix> prixListFindchips = new ArrayList<Prix>();
        ArrayList<Specs> specsItem = new ArrayList<Specs>();      
        
       //loop in all responses from FindChips API
       for(int indexResponse = 0; indexResponse < response.length(); indexResponse++)
       {
           //get JSONObject according to the current iResponse
           JSONObject objResponse = response.getJSONObject(indexResponse);   
           //get Supplier 
           JSONObject objDistributor = objResponse.getJSONObject("distributor");
           //get JSONArray to parts according to the current object
           JSONArray parts = objResponse.getJSONArray("parts");           
           //loop in all parts in current JSONArray
           for(int indexParts = 0; indexParts < parts.length(); indexParts++)
           {
               //get JSONObject according to the current iParts
               JSONObject objPart = parts.getJSONObject(indexParts);
               //Get Current part to our parts response
               currentMPN = objPart.getString("part");             
               //check if current part exists in our table
               if(tabMPN.contains(currentMPN))
               {
                   //loop in all MPn in our taMPN
                   for(int indexTabMPN = 0; indexTabMPN < tabMPN.size(); indexTabMPN++)
                   {
                       //try to find indice of current mpn
                       if(tabMPN.get(indexTabMPN).equals(currentMPN))
                       {              
                           //Get our produit
                            produit = produits.get(indexTabMPN); 
                            //Get price list from produit
                            prixListFindchips = produit.getListePrix();
                            //Get specs list from produit
                            specsItem = produit.getListeSpecs();
                            //Get rohs added in our list
                            tabRohs = produit.getRohsProduit();
                            //Add new price to our current list price 
                            this.recuperationPrix(objDistributor,objPart,prixListFindchips);  
                            //Add new spec to our current list specs     
                            this.CurrentROHs(objPart,specsItem,produit,tabRohs,sRohs); 
                            //Check if price list is empty. Can't add stock when price list is empty
                            JSONArray sourceAreaPrice = objPart.getJSONArray("price");
                            if(sourceAreaPrice.length() > 0)
                            {
                                if(objPart.has("stock"))
                                {
                                    //we add to our stock the current stock
                                    StockTotalMPN = produit.getStock() + objPart.getInt("stock");
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
                   //Create new price list
                   prixListFindchips = new ArrayList<Prix>();
                   //Create new specs list
                   specsItem = new ArrayList<Specs>();
                   //Create new specs list
                   tabRohs = new ArrayList();
                   //Get mpn / manucfaturer / description of our new prouit
                   this.MPNData(produit,objPart,objDistributor,prixListFindchips,specsItem); 
                   // Get Rohs current
                   this.CurrentROHs(objPart,specsItem,produit,tabRohs,sRohs);                  
                   produit.setRohsProduit(tabRohs);
                   produit.setListeSpecs(specsItem);
                   //Check if price list is empty. Can't add stock when price list is empty
                   JSONArray sourceAreaPrice = objPart.getJSONArray("price");
                   if(sourceAreaPrice.length() > 0)
                   {
                        //Set stock to our current mpn in our produit
                       if(objPart.has("stock"))
                       {
                            StockTotalMPN = objPart.getInt("stock");
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
    };

    public Source CurrentROHs(JSONObject objPart,ArrayList<Specs> specsItem,Source produit,ArrayList rohs,String sRohs) throws JSONException
    {
        //verify if we have rohs data
        if(objPart.has("rohs"))
        {
            //Get value
            JSONObject objRohs = objPart.getJSONObject("rohs");
             //verify if this value isn't empty and if she exists
            if(objRohs.has("DEFAULT") && !objRohs.getString("DEFAULT").isEmpty())
            {
                 //verify if objRohs.getString("pbFree") doesn't exist in our list and isn't empty
                 if(objPart.has("pbFree") && !objPart.getString("pbFree").isEmpty() && !rohs.contains(objRohs.getString("DEFAULT").toLowerCase()+" / pbFree: "+objPart.getString("pbFree").toLowerCase()))
                 {
                     //Add in our rohs list
                     sRohs = objRohs.getString("DEFAULT").toLowerCase()+" / pbFree: "+objPart.getString("pbFree").toLowerCase();
                     rohs.add(sRohs);
                 }
                 else
                 {
                     //verify if objRohs.getString("DEFAULT") doesn't exist in our list
                     if(!rohs.contains(objRohs.getString("DEFAULT").toLowerCase()))
                     {
                         //Add in our rohs list
                         sRohs = objRohs.getString("DEFAULT").toLowerCase();
                         rohs.add(sRohs);
                     }
                 }                
            }
            else
            {
                if(objPart.has("pbFree"))
                {
                    if(!objPart.getString("pbFree").isEmpty() && !rohs.contains(objRohs.getString("pbFree").toLowerCase()))
                    {
                        //Add in our rohs list
                        sRohs = objRohs.getString("pbFree").toLowerCase();
                        rohs.add(sRohs);  
                    }
                }
            }
        }
        else
        {
            //verify if objRohsSizeNotNull.getString("pbFree") doesn't exist in our list and isn't empty
            if(objPart.has("pbFree") && !objPart.getString("pbFree").isEmpty() && !rohs.contains(objPart.getString("pbFree").toLowerCase()))
            {
                //Add in our rohs list
                sRohs = objPart.getString("pbFree").toLowerCase();
                rohs.add(sRohs);
            }
        }

       
       if(!sRohs.isEmpty() && specsItem.size()>0)
       {
            Specs specs = new Specs();
            //Catch our list from current produit and we add the next value 
            specs.setROHS(specsItem.get(0).getValue() + ", "+ sRohs);
            //Clear old list
            specsItem.clear();                        
            //Add new list with new value
            specsItem.add(specs);
       }else
       {
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
    
    public Source MPNData(Source produit,JSONObject objPart,JSONObject objDistributor, ArrayList prixListFindchips,ArrayList specsItem) throws JSONException
    {
        if(objPart.has("part"))
        {
            produit.setMpn(objPart.getString("part"));
        }
        if(objPart.has("description"))
        {                      
            produit.setNomProduit(objPart.getString("description"));
        }

        if(objPart.has("manufacturer"))
        {
            produit.setNomFabricant(objPart.getString("manufacturer"));
        }         
        this.recuperationPrix(objDistributor,objPart,prixListFindchips);
        produit.setListePrix(prixListFindchips);
        produit.setOrigine("FindChips");
        
        return produit;
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
            
            //isn't available yet
            //this.RecuperationMPNs(this.duplicateResults);
        }
        
        // if this.enregistreCredit = 0 => do nothing
        this.enregistreCredit(this.clientID, this.nbCredit);
        System.out.println("FindChips terminé");
        return duplicateResults;

    }


    @Override
    public void RecuperationDesc(ArrayList<SearchResult> arrSearchResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getErrorStatus() {
       String mySearch = "SM6T36CA";
       /// on test une fonction SKu car devrait etre plus rapide
       String resultatJson = this.InterroFindChips(mySearch);
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
        FindChips.TraitementMultihtreads tmt = new FindChips.TraitementMultihtreads();
        //
        if (this.getTypeRequete().equals("mpn")) {
            tmt.setNumeroATraiter(mySearchResult.getMpnOriginal());
        } else {
            tmt.setNumeroATraiter(mySearchResult.getSkuOriginal());
        }
        return tmt;
    }

}

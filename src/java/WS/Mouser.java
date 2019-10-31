/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WS;

import DB.WsClientDB;
import POJO.Datasheet;
import POJO.Prix;
import POJO.SearchResult;
import POJO.Source;
import POJO.Specs;
import com.essai3.ServiceResource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.json.JSONException;
import org.json.XML;
import java.util.Arrays;
import org.apache.tomcat.util.codec.binary.Base64;
/**
 * Classe permettant d'interroger l'API MOUSER
 * 
 * log      : loic.maisonnasse
 * mdp      : buymanager2014 
 * 
 * Compte secondaire 
 * log : buymanager
 * mdp : pertilience
 * 
 * @author François DUPONT
 */
public class Mouser extends WsClientDB implements InterfaceWSInterrogeable, Callable<ArrayList<SearchResult>> {

    //clé d'utilisation de l'API == clé générique
    private static final String key = "2ab57871-4d43-4e47-be19-6ef7d56bd0e7";
    private static final int nbPoolsThreads = 6;
    
    private String getMyKey(){
        String cle = "";
        
        if(!super.getKey().isEmpty())
        {
            byte[] decodePassword =  Base64.decodeBase64(super.getKey());
            cle = new String(decodePassword);
        }
        if(cle.equals("")){
            cle = key;
        }
        return cle;
    }

    /**
     * Procedure principale pour interroger l'API
     *
     * @param mpns - liste de mpns à interroger
     * @param sourcesListe - liste dans laquelle les résulats seront stockés
     */
    private ArrayList<Source> procedureMPN(String mpn,String cle) throws JSONException {
       // try {
            if (mpn.isEmpty()){
                // si le Mpn est vide renvoie un tableau vide
                return new ArrayList<Source>();
            }else{
                //
                // je poense que si on fait trop de requete par second mouser nous met en attente
                // plutôt que d'attendre 10 seconde on met 5 seconds, comme ca on attend moins...
                //
                OkHttpClient client = new OkHttpClient.Builder()
                                                .connectTimeout(10, TimeUnit.SECONDS)
                                                .writeTimeout(10, TimeUnit.SECONDS)
                                                .readTimeout(5, TimeUnit.SECONDS) // tentative car timeout quand trop de tentative 
                                                .build();
//                
//                //Pour chaque mpn recherché par l'utilisateur
//                URL wsdlLocation = new URL("http://www.mouser.fr/service/searchapi.asmx?WSDL");
//                QName searchAPI = new QName("http://api.mouser.com/service", "SearchAPI");
//
//                //
//                // Attention actuellement le code iciu ne focntionne qu'avec le JDK 7 et pas le JDK 8
//                // cela tombe bien on utilise le 1.7 sur amazon => il faudra quand même chercher pourquoi un jour
//                SearchAPI api = new SearchAPI(wsdlLocation, searchAPI);
//                api.addPort(searchAPI, SOAPBinding.SOAP12HTTP_BINDING, "http://api.mouser.com/service/SearchAPI");
//
//                QName search = new QName("http://api.mouser.com/service", "SearchAPISoap12");
//                Dispatch<SOAPMessage> disp = api.createDispatch(search, SOAPMessage.class, Service.Mode.MESSAGE);

                String xml = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:ser=\"http://api.mouser.com/service\">\n"
                        + "   <soap:Header>\n"
                        + "      <ser:MouserHeader>\n"
                        + "         <ser:AccountInfo>\n"
                        + "            <ser:PartnerID>" + cle + "</ser:PartnerID>\n"
                        + "         </ser:AccountInfo>\n"
                        + "      </ser:MouserHeader>\n"
                        + "   </soap:Header>\n"
                        + "   <soap:Body>\n"
                        + "      <ser:SearchByPartNumber>\n"
                        + "         <ser:mouserPartNumber>" + mpn + "</ser:mouserPartNumber>\n"
                        + "      </ser:SearchByPartNumber>\n"
                        + "   </soap:Body>\n"
                        + "</soap:Envelope>";

                MediaType mediaType = MediaType.parse("application/soap+xml");    
                RequestBody body = RequestBody.create(mediaType,xml );
                
                Request request = new Request.Builder()
                    .url("http://api.mouser.com/service/searchapi.asmx?WSDL")
                    .post(body)
                    .addHeader("content-type", "application/soap+xml")
                    .build();

                try {
                    Response response = client.newCall(request).execute();
                    if ( response.isSuccessful() ) {
                        String Result = response.body().string();
                        org.json.JSONObject resultat = XML.toJSONObject(Result);
                        return genererObjetsSources(resultat.toString());
                    }else{
                        //
                        String returnValue = "";
                        switch (response.code() ) {
                            case 401 :
                            case 404 :
                            case 400 :
                                Logger.getLogger(Mouser.class.getName()).log(Level.INFO,"Erreur HTTP "+response.code()+ " : "+ response.body().string(),response);
                                break;
                            default :
                                Logger.getLogger(Mouser.class.getName()).log(Level.WARNING,"Erreur HTTP "+response.code()+ " : "+ response.body().string(),response);
                                break;
                        }
                         return null;
                    }

                } catch (IOException ex) {
                    Logger.getLogger(Mouser.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }    
     
    }

    private ArrayList<Source> procedureSKU(String mpn) {
        return null;
    }

    /**
     * ***
     * Convertit le résulat de l'API Mouser en plusieurs objets sources avec
     * mapping des données
     *
     */
    private ArrayList<Source> genererObjetsSources(String resultatRequete) {
        try {
            ArrayList<Source> produits = new ArrayList<Source>(); // Liste des produits d'un mpn
            String[] mpqEtMoq = new String[2];
            JSONObject jsonObject = new JSONObject(resultatRequete);
            int nbResults = Integer.parseInt(jsonObject.getJSONObject("soap:Envelope")
                    .getJSONObject("soap:Body")
                    .getJSONObject("SearchByPartNumberResponse")
                    .getJSONObject("SearchByPartNumberResult")
                    .getString("NumberOfResult"));
            if (nbResults > 0) {
                JSONArray mouserPart;
                //Cas où la requpete n'a qu'un seul produit, le type change
                JSONObject objParts = jsonObject.getJSONObject("soap:Envelope")
                            .getJSONObject("soap:Body")
                            .getJSONObject("SearchByPartNumberResponse")
                            .getJSONObject("SearchByPartNumberResult")
                            .getJSONObject("Parts");
                    
                if (nbResults == 1) {
                    //
                    try{
                        // In a tru car des fois il y a 1 seul resultat mais un tableau quand même
                        JSONObject mouserPartTransition = objParts.getJSONObject("MouserPart");
                        //
                        JSONArray mouserPartArray = new JSONArray();
                        mouserPartArray.put(mouserPartTransition);
                        mouserPart = mouserPartArray;
                        //
                    }catch (org.codehaus.jettison.json.JSONException ex ){
                        // try to read as a table
                        mouserPart = objParts.getJSONArray("MouserPart");
                    }
                    
                    //Cas général
                } else {
                    mouserPart = objParts.getJSONArray("MouserPart");
                }
                //pour plusieurs mpns, créé un object contenant les mpns trouvé correspondant au mpn demandé
                for (int i = 0; i < mouserPart.length(); i++) {
                    Source produit = new Source();
                    JSONObject jsonMouserObject =mouserPart.getJSONObject(i);
                    
                    produit.setUid(jsonMouserObject.getString("MouserPartNumber"));
                    produit.setMpn(jsonMouserObject.getString("ManufacturerPartNumber"));
                    produit.setNomProduit(jsonMouserObject.getString("Description").replaceAll("[^\\x00-\\x7F]", "e").trim());
                    produit.setNomFabricant(jsonMouserObject.getString("Manufacturer"));
                    produit.setUrlWs(jsonMouserObject.getString("ProductDetailUrl"));
                    produit.setOrigine("Mouser");
                    
                    boolean bReeling = jsonMouserObject.getBoolean("Reeling");
                       
                    String[] stock = null;
                    if(jsonMouserObject.has("Availability"))
                    {
                        stock = jsonMouserObject.getString("Availability").split(" ");
                        //Ne prend la valeur que si le produit est en stock
                        if (stock.length > 2 && stock[2].equals("stock")) {
                            produit.setStock(Integer.parseInt(stock[0]));
                        } 
                    }

                    if (optionPrix) {
                        String myMPQ = jsonMouserObject.getString("Mult");
                        // ici le min n'est pas juste
                        String[] lt = jsonMouserObject.getString("LeadTime").split(" ");
                        String leadTime = lt[0];
                        JSONArray listePrix;
                        //
                        // PriceBreaks n'existe pas tjrs 
                        if ( ! jsonMouserObject.getString("PriceBreaks").isEmpty() ) {
                            
                            Object lesPrix = jsonMouserObject.getJSONObject("PriceBreaks").get("Pricebreaks");
                            //
                            //Cas où le produit n'a qu'un seul prix, le type change
                            if (lesPrix.getClass().isAssignableFrom(JSONObject.class)) {
                                JSONArray jsonArray = new JSONArray();
                                jsonArray.put(lesPrix);
                                listePrix = jsonArray;
                            } else {
                                listePrix = jsonMouserObject.getJSONObject("PriceBreaks").getJSONArray("Pricebreaks");
                            }
                            //
                            // CAS VU :
                            // - Si pas de stock alors egale ["none"] sinon ["2356",in,Stock]
                            // - On test donc si la premiere valeur est uniquement numérique
                            Class classStock = stock.getClass();
                            boolean isStockArray = classStock.isArray();
                           // boolean hasStock = stock[0].replaceAll("[0-9.,]", "").equals("");
                            if (stock.length > 1 && isStockArray ) {
                                mappingDesPrix(produit, listePrix, leadTime, myMPQ, stock[0], jsonMouserObject.getString("MouserPartNumber"),bReeling,Arrays.toString(stock).replaceAll("[\\[\\],]", ""));
                            } else {
                                mappingDesPrix(produit, listePrix, leadTime, myMPQ, "0", jsonMouserObject.getString("MouserPartNumber"),bReeling,Arrays.toString(stock).replaceAll("[\\[\\],]", ""));
                            }
                        }
                    }
                    if (optionDS) {
                        ArrayList<Datasheet> datasheet = new ArrayList<Datasheet>();
                        Datasheet ds = new Datasheet();
                        ds.setUrl(jsonMouserObject.getString("DataSheetUrl"));
                        datasheet.add(ds);
                        produit.setDatasheet(datasheet);
                    }
                    
                    if (optionSpec){
                        ArrayList<Specs> specsItem = new ArrayList<Specs>();
                        if ( jsonMouserObject.has("ROHSStatus")){
                            Specs uneSpec = new Specs();
                            uneSpec.setROHS(jsonMouserObject.getString("ROHSStatus"));
                            specsItem.add(uneSpec);
                        }
                        
                        if ( jsonMouserObject.has("LifecycleStatus")){
                            Specs uneSpec = new Specs();
                            uneSpec.setLifeCycle(jsonMouserObject.getString("LifecycleStatus"));
                            specsItem.add(uneSpec);
                        }
                        
                        //
                        produit.setListeSpecs(specsItem);
                    }
                    produits.add(produit);
                }
                return produits;
            }
        } catch (org.codehaus.jettison.json.JSONException ex) {
            Logger.getLogger(TME.class.getName()).log(Level.SEVERE, "error on result :"+ resultatRequete, ex);
        }
        return null;
    }

    /**
     * ***
     * Affecte pour un mpn ses prix correspondants
     *
     */
    private void mappingDesPrix(Source source, JSONArray listePrix, String leadTime, String mpq, String stock, String sku,boolean bReeling, String stockComment) {
        ArrayList<Prix> prix = new ArrayList<Prix>();
        for (int i = 0; i < listePrix.length(); i++) {
            try {
                Prix p = new Prix();
                p.setDevise(listePrix.getJSONObject(i).getString("Currency"));
                p.setQuantite(Integer.parseInt(listePrix.getJSONObject(i).getString("Quantity")));
                String[] lePrix = listePrix.getJSONObject(i).getString("Price").split(" ");
                p.setPrix(Double.parseDouble(lePrix[0].replace(",", ".").replaceAll("[^\\d\\.]", "")));
                p.setLeadDays(Integer.parseInt(leadTime));
                p.setMpq(Integer.parseInt(mpq));
                //
                // Dans le MOQ on copie la quantité dans le MOQ 
                //  le MOQ est ici au niveau de l'article par du prix  
                p.setMoq(p.getQuantite());
                p.setFournisseur("Mouser (direct)");
                if  (stockComment.contains("Stock")== false &&  stockComment.contains("stock") == false  ) {
                  p.setStock(0);
                } else {
                   p.setStock(Integer.parseInt(stock)); //   
                };
                p.setSku(sku);
                p.setStockRegions(stockComment);
                p.setDistribValide(true);

                if (bReeling) {
                    if ( p.getMpq() == 1 ){
                        p.setPackaging("CT,Reel");
                    }else{
                        p.setPackaging("Reel");   
                    }
                }
                
                prix.add(p);
            } catch (org.codehaus.jettison.json.JSONException ex) {
                Logger.getLogger(Mouser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        source.setListePrix(prix);
    }

    /**
     * ***
     * Méthode implémentée permettant d'executer l'interrogation de tous les
     * mpns
     *
     * @param sourcesListe - L'array a remplir avec les résulats de l'API
     */
    @Override
    public void RecuperationMPNs(ArrayList<SearchResult> sourcesListe) {
        procedureThreads(sourcesListe);
    }

    /**
     * ***
     * Méthode implémentée permettant d'executer l'interrogation de tous les
     * skus
     *
     * @param sourcesListe - L'array a remplir avec les résulats de l'API
     */
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
        System.out.println("Mouser terminé");
        return duplicateResults;
    }

    @Override
    public void RecuperationDesc(ArrayList<SearchResult> arrSearchResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getErrorStatus() {
      return false;
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
            String cle = getMyKey();
            if (requete.equals("mpn")) {
                return procedureMPN(number,cle);
            } else if (requete.equals("sku")) {
                return procedureMPN(number,cle);
            }
            return null;
        }

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
     * 
     * @param mySearchResult
     * @return Callable<ArrayList<Source>>
     */
    @Override
    protected Callable<ArrayList<Source>> getTMT(SearchResult mySearchResult){
        Mouser.TraitementMultihtreads tmt = new Mouser.TraitementMultihtreads();
        //
        if (this.getTypeRequete().equals("mpn")) {
            tmt.setNumeroATraiter(mySearchResult.getMpnOriginal());
        } else {
            tmt.setNumeroATraiter(mySearchResult.getSkuOriginal());
        }
        return tmt;
    }
    
}

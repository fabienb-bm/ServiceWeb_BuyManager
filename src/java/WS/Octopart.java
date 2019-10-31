package WS;

import DB.WsClientDB;
import DB.UtilisationClientDB;
import JSON.Json;
import POJO.Datasheet;
import POJO.Prix;
import POJO.SearchResult;
import POJO.Source;
import POJO.Specs;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.tomcat.util.codec.binary.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.uri.UriComponent;

/**
 * Nom de classe : Octopart
 * <br>
 * Description : Classe permettant l'interrogation du service Web Octopart.
 * 
 * octopart
        dev :
            support@pertilience.Com
            21fa6285
            mdp : pertilience
        client :
            sales@pertilience.com 
            57ee13b3 	
            mdp : RQT2015

 * 
 * 
 * 
 * 
 * 
 * Date de la dernière modification : 22/07/2014
 * 
* @author Stagiaire (Florence Giraud)
 */
public class Octopart extends WsClientDB implements InterfaceWSInterrogeable, Callable<ArrayList<SearchResult>> {

    static final String octopartGenericKey = "21fa6285";

    /**
     * Constructeur vide de la classe Octopart.
     */
    public Octopart() {
    }

    /**
     *
     * @return
     */
    @Override
    public String getKey() {
        String mykey = ""; //To change body of generated methods, choose Tools | Templates.
        byte[] decodePassword =  Base64.decodeBase64(super.getKey());
        mykey = new String(decodePassword);
        if (mykey.isEmpty()) {
            mykey = octopartGenericKey;
        }
        return mykey;
    }

    /**
     * ***********************
     * METHODES PUBLIQUES **********************
     */
    /**
     * Permet de remplir un tableau de SearchResult avec les résultats de la
     * requête.
     *
     * @param arrSearchResult le tableau de SearchResult à remplir.
     */
    @Override
    public void RecuperationMPNs(ArrayList<SearchResult> arrSearchResult) {
        //Appel de la fonction permettant de récupérer un tableau de "tranche" de MPNs
        ArrayList<String[]> listeTabMpn = recuperationTableauMpn(arrSearchResult);

        ArrayList<ArrayList<Source>> listeResults = new ArrayList<ArrayList<Source>>();

        //Pour chaque tranche de 20 MPNs, on soumet une requête à l'API d'Octopart  
        for (int h = 0; h < listeTabMpn.size(); h++) {
            //tableauMpn: une tranche de 20 ou moins de MPNs (tranche crée par la fonction recuperationTableauMpn)
            String[] tableauMpn = listeTabMpn.get(h);

            //transformation du tableau en ArrayList (pour pouvoir le sérialiser)
            ArrayList<String> tableauMpnList = new ArrayList<String>();
            for (int i = 0; i < tableauMpn.length; i++) {
                if (tableauMpn[i] != null) {
                    tableauMpnList.add(tableauMpn[i]);
                }
            }

            String listeMpnJson = null;
            try {
                //transformation de la liste en JSon
                listeMpnJson = Json.ToJsonTabMpn(tableauMpnList);

                //Recuperation des résultats
                //l'ordre est conservé par les reponses octopart 
                ArrayList<ArrayList<Source>> listeSource = this.jsonToArrSources(listeMpnJson,false);
                listeResults.addAll(listeSource);
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
        }
        //
        //Chaque résultat obtenu suite à la requête est placé dans le tableau sourcesListe (futur JSON)
        for (int i = 0; i < listeResults.size(); i++) {
            ArrayList<Source> produits = listeResults.get(i);
            if (produits != null) {
                arrSearchResult.get(i).addTabSource(produits);
            }
        }
    }

    /**
     * Permet de récupérer un tableau de "tranches" de MPNs à partir d'un
     * tableau de SearchResult.
     *
     * @param sourcesListe le tableau de SearchResult (futur Json).
     * @return un tableau de MPNs.
     */
    public ArrayList<String[]> recuperationTableauMpn(ArrayList<SearchResult> sourcesListe) {

        //Transformation de la liste de MPNs en tableau avec le séparateur ","
        ArrayList<String> tabMpn = new ArrayList<String>();

        //Pour chaque ligne correspondant à un mpn différent
        for (int i = 0; i < sourcesListe.size(); i++) {
            String mpn = sourcesListe.get(i).getMpnOriginal();
            tabMpn.add(mpn);
        }

        //taille de la tranche
        int tailleMaxi = 20;
        //taille du tableau à trancher
        int tailleTabMpn = tabMpn.size();

        //Tableau contenant toutes les tranches
        ArrayList<String[]> listeTabMpn = new ArrayList<String[]>();

        for (int j = 0; j < tailleTabMpn; j = j + tailleMaxi) {
            String[] tab = new String[tailleMaxi];

            if (tailleTabMpn < j + tailleMaxi) {
                tailleMaxi = tailleTabMpn - j;
            }
            //
            System.arraycopy(tabMpn.toArray(), j, tab, 0, tailleMaxi);
            listeTabMpn.add(tab);
        }
        return listeTabMpn;
    }

    /**
     * Permet de lancer une requête avec plusieurs MPNs.
     *
     * @param listeMpnJson la requete avec les MPNs. affichés.
     * @param DescriptionSearch
     * @return un tableau de tableau de Sources.
     */
    public ArrayList<ArrayList<Source>> jsonToArrSources(String listeMpnJson,boolean DescriptionSearch) {
        try {
            //on recupere un string correspondant au résultat de la requete           
            String jsonRecup;
                    
            if (DescriptionSearch == false) {
                jsonRecup = this.InterroOctoMulti(listeMpnJson);
            }else {
                jsonRecup = this.SearchDesc(listeMpnJson);
            }           
            //
            ArrayList<ArrayList<Source>> listeSources = Recuperation(jsonRecup,DescriptionSearch);
            return listeSources;

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    //Exemple d'une URL de requête  
    //http://octopart.com/api/parts/match?apikey=24ae159c&pretty_print=true&queries=[{"mpn":"bav99","limit":10},{"mpn":"339L","limit":10},{"mpn":"338T","limit":10}] ==> marche pas
    //http://octopart.com/api/v3/parts/match?apikey=21fa6285&pretty_print=true&queries=[{%22sku%22:%22sm6t36ca%22,%22limit%22:10}] ==> celle qui marche
    /**
     * Permet l'interrogation de l'API Octopart et retourne une réponse sous
     * JSON.
     *
     * @param listeMpnJson Les MPNs recherchés sous format Json pour la requête.
     * @return reponse : Un JSON de l'ensemble des résultats de la requête.
     */
    private String InterroOctoMulti(String listeMpnJson) {
        //création du nouveau client 
        Client client = ClientBuilder.newClient(new ClientConfig());

        //Declaration de l'URL permettant la recherche
        WebTarget webTarget = client.target("http://octopart.com/api")
                .path("/v3/parts/match")
                .queryParam("apikey", this.getKey())
                .queryParam("country", this.getCountry())
                .queryParam("include[]", "short_description")
                .queryParam("include[]", "specs")
                .queryParam("include[]", "category_uids")
                .queryParam("include[]", "datasheets")
                .queryParam("queries", UriComponent.encode(listeMpnJson, UriComponent.Type.QUERY_PARAM));
        //System.out.println(listeMpnJson);
        //Activation de l'URL permettant la recherche
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN_TYPE);
        String reponse = invocationBuilder.get(String.class);
        return reponse;
    }
    
        //Exemple d'une URL de requête  
    //http://octopart.com/api/parts/search?apikey=24ae159c&pretty_print=true&q=diode zener&limit=20
    /**
     * Permet l'interrogation de l'API Octopart et retourne une réponse sous
     * JSON.
     *
     * @param description La description à rechercher
     * @return reponse : Un JSON de l'ensemble des résultats de la requête.
     */
    private String SearchDesc(String description) {
        //création du nouveau client 
        Client client = ClientBuilder.newClient(new ClientConfig());

        //Declaration de l'URL permettant la recherche
        WebTarget webTarget = client.target("http://octopart.com/api")
                .path("/v3/parts/search")
                .queryParam("apikey", this.getKey())
                .queryParam("country", this.getCountry())
                .queryParam("include[]", "short_description")
                .queryParam("include[]", "specs")
                .queryParam("include[]", "category_uids")
                .queryParam("include[]", "datasheets")
                .queryParam("q", UriComponent.encode(description, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
                .queryParam("limit", 20);
        //System.out.println(listeMpnJson);
        //Activation de l'URL permettant la recherche
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN_TYPE);
        String reponse = invocationBuilder.get(String.class);
        return reponse;
    }
    
    
    /**
     * Méthode générale permettant de récupérer les informations depuis le
     * résultat de la requête et de les placer dans les objets de type Source.
     *
     * @param jsonRecup le JSON résultant de la requête.
     * @param ref la requête contenant les MPNs (Paramètre d'interface NON
     * utilisé ici).
     * @param optionPrix boolean valant true si les prix doivent être visibles,
     * false sinon.
     * @return un tableau de tableau de Sources.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     */
    private ArrayList<ArrayList<Source>> Recuperation(String jsonRecup,boolean DescSearch) throws JSONException {
        //on recupere le JSON complet
        JSONObject jsonObject = new JSONObject(jsonRecup);// On récupère le tableau d'objets spécifique à l'entrée "results"
        JSONArray results = jsonObject.getJSONArray("results");

        //On crée un tableau de tableau de sources
        ArrayList<ArrayList<Source>> listeProduits = new ArrayList<ArrayList<Source>>();

        //Pour chacune des entrées de "results":
        for (int i = 0; i < results.length(); i++) {
            //on crée un objet de chaque "results"
            JSONObject objResults = results.getJSONObject(i);

            // On récupère le tableau d'objets spécifique à l'entrée "items"
            // Doit correspondre a la classe part chez octopart
            JSONArray items = new JSONArray();
            
                    
            if (DescSearch == true) {
               JSONObject item = objResults.getJSONObject("item");
               items.put(0, item);
            }else {
                items = objResults.getJSONArray("items");
            }
                

            //On crée une nouvelle liste d'articles
            ArrayList<Source> produits = new ArrayList<Source>();
            //
            // Dans le cas d'une recherche libellé, un item = un "hit", et le membre hits n'existe pas dans la structure
            //DU coup en mode desc, on considere que hits = 1
            int hits;
            if (DescSearch == true) {
                hits = 1;
            }else {
                hits = objResults.getInt("hits");
            }
                
            if (hits != 0) {
                //Pour chacune des entrées de "items":
                for (int j = 0; j < items.length(); j++) {
                    //Definition des attributs
                    String prixString = null;
                    int quantite = 0;

                    //on crée un objet de chaque "items"
                    JSONObject objItems = items.getJSONObject(j);

                    //On crée un nouvel article pour chaque "items"
                    Source produit = new Source();

                    produit.setMpn(objItems.getString("mpn"));
                    produit.setUrlWs(objItems.getString("octopart_url"));
                    produit.setUid(objItems.getString("uid"));
                    //
                    // reccupere la description
                    if (objItems.has("short_description")) {
                        produit.setNomProduit(objItems.getString("short_description"));
                    }

                    //on crée un objet de chaque "manufacturer"
                    JSONObject objManu = objItems.getJSONObject("manufacturer");

                    produit.setNomFabricant(objManu.getString("name"));
                    produit.setUrlFabricant(objManu.getString("homepage_url"));
                    produit.setOrigine("Octopart");
                    // DATASHEETS INTIALISATION
                    if (optionDS && objItems.has("datasheets")) {
                        ArrayList<Datasheet> datasheet = new ArrayList<Datasheet>();
                        JSONArray octoDS = objItems.getJSONArray("datasheets");
                        for (int k = 0; k <octoDS.length(); k++) {
                            Datasheet ds = new Datasheet();
                            ds.setUrl(octoDS.getJSONObject(k).getString("url"));
                            datasheet.add(ds);
                        }
                        produit.setDatasheet(datasheet);
                    }

                    //LES PRIX + LES STOCKS
                    // On récupère le tableau d'objets spécifique à l'entrée "offers"
                    JSONArray offers = objItems.getJSONArray("offers");
                    //Création du tableau de prixTab qui est un attribut de Source
                    ArrayList<Prix> prixListe = new ArrayList<Prix>();
                    //Pour chaque offre
                    for (int k = 0; k < offers.length(); k++) {
                        //on crée un objet de chaque "offers"
                        JSONObject objOffers = offers.getJSONObject(k);
                        //La quantite disponible s'ajoute
                        quantite += objOffers.getInt("in_stock_quantity");
                        //on crée un objet "vendeur"
                        JSONObject objSeller = objOffers.getJSONObject("seller");
                        String fournisseur = objSeller.getString("name");

                        //on récupère diverses valeurs
                        String sku = objOffers.getString("sku");
                        boolean distribValide = objOffers.getBoolean("is_authorized");
                        String lastUpdate = objOffers.getString("last_updated").substring(0,10);

                        if (optionPrix) {
                            //appel de la fonction permettant de récupération des prix
                            prixListe = Octopart.recuperationPrix(objOffers, fournisseur, sku, distribValide, prixString, prixListe, lastUpdate);
                        }
                    }
                    if (optionSpec) {
                        JSONObject specs = objItems.getJSONObject("specs");
                        JSONObject uneSpec;
                        Iterator<?> keys = specs.keys();
                        ArrayList<Specs> specsItem = new ArrayList<Specs>();
                        while (keys.hasNext()) {
                            uneSpec = (JSONObject) specs.get((String) keys.next());
                            Specs spec = new Specs();
                            
                            spec.setKey(uneSpec.getJSONObject("metadata").getString("key"));
                            
                            if (!uneSpec.getJSONArray("value").isNull(0)) {
                                spec.setValue(uneSpec.getJSONArray("value").getString(0));
                            }
                            if (!uneSpec.getJSONObject("metadata").isNull("unit")) {
                                spec.setUnit(uneSpec.getJSONObject("metadata").getJSONObject("unit").getString("symbol"));
                            }
                            specsItem.add(spec);
                        }
                        produit.setListeSpecs(specsItem);
                    }
                    produit.setStock(quantite);
                    produit.setRangOrigine(j + 1);
                    produit.setListePrix(prixListe);
                    // On ajoute le produit à la liste
                    produits.add(produit);
                }
            } else {
                produits = null;
            }
            listeProduits.add(produits);
        }
        return listeProduits;
    }

    /**
     * Permt la récupération des informations sur les prix présent dans le JSON
     * de base.
     *
     * @param objOffers objet JSON contenant les prix, à parser.
     * @param fournisseur le nom du fournisseur de l'article.
     * @param sku le sku de l'article.
     * @param distribValide boolean indiquant si l'API a validé le distributeur
     * (valable pour octopart, true par défaut pour Farnell/Rs)
     * @param prixString prix de l'article sous forme de String.
     * @param prixList liste des prix (qui sera retourné).
     * @param lastUpdate - Dernière maj de l'offre
     * @return la liste des prix (prixList) complétée.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     */
    public static ArrayList<Prix> recuperationPrix(JSONObject objOffers, String fournisseur, String sku,
            boolean distribValide, String prixString, ArrayList<Prix> prixList, String lastUpdate) throws JSONException {
        //on crée un objet de chaque "prices"
        JSONObject objPrices = objOffers.getJSONObject("prices");

        if (objPrices.length() != 0) {
            JSONArray nomsDevise = objPrices.names();

            for (int l = 0; l < nomsDevise.length(); l++) {
                String devise = nomsDevise.getString(l);

                // On récupère le tableau d'objets spécifique à l'entrée de la devise choisie
                JSONArray devi = objPrices.getJSONArray(devise);

                for (int m = 0; m < devi.length(); m++) {
                    // On récupère le tableau d'objets spécifique à chaque entrée de "devi"
                    JSONArray prix = devi.getJSONArray(m);

                    Prix prixTab = new Prix();
                    prixTab.setLastUpdate(lastUpdate);
                   
                    //
                    //if (!objOffers.isNull("moq")) {
                    //    int moq = objOffers.getInt("moq");
                    //     prixTab.setMoq(moq);
                    //
                    //}
                    
                    //
                    if (!objOffers.isNull("order_multiple")) {
                        int mpq = objOffers.getInt("order_multiple");
                        prixTab.setMpq(mpq);
                    }
                    //
                    prixTab.setFournisseur(fournisseur);
                    prixTab.setDevise(devise);
                    prixTab.setSku(sku);
                    //
                    prixTab.setStock(objOffers.getInt("in_stock_quantity"));
                    //
                    String region = objOffers.getString("eligible_region");

                    if (!region.equals("null")) {
                        prixTab.setStockRegions(region);
                    } else {
                        prixTab.setStockRegions("");
                    }
                    //
                    //si factory_lead_days n'est pas null
                    if (!objOffers.isNull("factory_lead_days")) {
                        int leadDays = objOffers.getInt("factory_lead_days");
                        prixTab.setLeadDays(leadDays);
                    }

                    prixTab.setDistribValide(distribValide);

                    //recuperation à chaque tour du couple prix-quantité
                    prixString = prix.getString(1);
                    int quantitePrix = prix.getInt(0);

                    //conversion en double du prix recupéré en String
                    double prixDouble = Double.parseDouble(prixString);

                    prixTab.setPrix(prixDouble);
                    prixTab.setQuantite(quantitePrix);
                    prixTab.setMoq(quantitePrix); //copie le MOQ a l'identique du prix

                    prixTab.setPackaging(objOffers.getString("packaging"));

                    prixList.add(prixTab);
                }
            }
        }
        return prixList;
    }

    @Override
    public void RecuperationSKUs(ArrayList<SearchResult> arrSearchResult) {

        //
        //
        // tableau contenat les SKU à interroger
        ArrayList<String> tableauReqSKU = new ArrayList<String>();
        //
        ArrayList<ArrayList<Source>> listeResults = new ArrayList<ArrayList<Source>>();
        //
        //
        for (int i = 0; i < arrSearchResult.size(); i++) {

            String sku = arrSearchResult.get(i).getSkuOriginal();
            tableauReqSKU.add(sku);

            if (tableauReqSKU.size() == 20) {

                // lors que l'on attein 20 on lance une recherche
                String listeMpnJson = Json.ToJsonTabSku(tableauReqSKU);
                //
                //Recuperation des résultats
                ArrayList<ArrayList<Source>> listeSource = this.jsonToArrSources(listeMpnJson,false);
                //
                listeResults.addAll(listeSource);
                //
                tableauReqSKU.clear();
            }
        }

        // traite les derniers résultats
        if (tableauReqSKU.size() > 0) {
            //
            String listeMpnJson = Json.ToJsonTabSku(tableauReqSKU);
            //
            //Recuperation des résultats // conserve l'ordre
            ArrayList<ArrayList<Source>> listeSource = this.jsonToArrSources(listeMpnJson,false);
            //
            listeResults.addAll(listeSource);
            //
        }
        //
        //
        //Chaque résultat obtenu suite à la requête est placé dans le tableau sourcesListe (futur JSON)
        // au final listeResults et sourcesListe sont sur les mêmes indices
        for (int i = 0; i < listeResults.size(); i++) {
            ArrayList<Source> produits = listeResults.get(i);
            if (produits != null) {
                arrSearchResult.get(i).addTabSource(produits);
            }
        }
        //
    }

    @Override
    public void enregistreCredit(int clientID, int nbCredit) {
        try {
            UtilisationClientDB.requeteAjouteREQOcto(clientID, nbCredit);
        } catch (SQLException ex) {
            Logger.getLogger(Octopart.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        } else if (this.typeRequete.equals("desc")) {
            this.RecuperationDesc(this.duplicateResults);
        }
        this.enregistreCredit(this.clientID, this.nbCredit);
        System.out.println("Octopart terminé");
        return duplicateResults;
    }

    @Override
    public void RecuperationDesc(ArrayList<SearchResult> arrSearchResult) {
           //
        //
        // tableau contenat les SKU à interroger
        //
        ArrayList<ArrayList<Source>> listeResults = new ArrayList<ArrayList<Source>>();
        //
        //
        for (int i = 0; i < arrSearchResult.size(); i++) {

            String Desc = arrSearchResult.get(i).getDescOriginal();

            //Recuperation des résultats
            ArrayList<ArrayList<Source>> listeSource = this.jsonToArrSources(Desc,true);
            //
            // parcours du tableau de tableau (un item trouvé dans le second tableau source ....
            for (int indiceTabTabSource = 0; indiceTabTabSource < listeSource.size();indiceTabTabSource++) {
                
                ArrayList<Source> itemList = listeSource.get(indiceTabTabSource); // récupération du tableau de source ne contenant qu'un item
                Source item = itemList.get(0); // Récupération de l'item
                arrSearchResult.get(i).addTabSource(item); // Ajout de l'item
            }
            //
        }
        //

        //
    }

    @Override
    public boolean getErrorStatus() {
      return false;
    }

}

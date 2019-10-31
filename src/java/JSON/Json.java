package JSON;

import DB.ClientDB;
import POJO.SearchResult;
import POJO.Source;
import com.essai3.WsPrioClass;
import com.essai3.manufacturerMasterList;
import flexjson.JSONSerializer;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;

/**
 * Nom de classe : Json
 * <br>
 * Description : Classe permettant l'envoi des resultats au logiciel sous un
 * format Json.
 * <br>
 * Date de la dernière modification : 07/08/2014
 * 
* @author Stagiaire (Florence Giraud)
 */
public class Json {

    /**
     * ***********************
     * METHODES PUBLIQUES
    ***********************
     */
    /**
     * Permet de sérialiser un tableau de SourcesResults afin d'obtenir un JSON.
     *
     * @param sourcesListe un tableau de SourcesResults (composée d'un MPN et
     * d'un tableau de résultats).
     * @param optionPrix boolean relatant l'accès ou non aux prix.
     * @param optionSpec boolean reletant l'accès aux spécifications
     * @param optionDS boolean reletant l'accès aux datasheets
     * @return un JSON des résultats.
     * @throws JSONException : exception levée suite à une erreur sur le JSON
     * (mauvais format/objet non trouvé).
     * @throws IOException : exception levée suite à un problème sur une entrée
     * ou une sortie (mauvais format).
     * @throws ParseException : exception levée suite à une erreur survenue
     * lorsque du « parse » du JSON (problème de sérialisation).
     */
    public static String ToJsonArrSearchResult(ArrayList<SearchResult> sourcesListe, boolean optionPrix, boolean optionSpec, boolean optionDS) throws JSONException, IOException, ParseException {
        //on exclut les attributs temporaire utilisé pour le mapping des donneés dans TME
        //pas de besoin d'exclure mpqEtMoq car par défaut les tableaux ne sont pas sérializé  
        JSONSerializer serializer = new JSONSerializer();
        String prix="",spec="",DS="",prediction="";
        if(optionPrix){
            //
            prix = "tabSource.listePrix";
            prediction = "tabSource.listePrediction";
            //
        }
        if(optionSpec){
            spec = "tabSource.listeSpecs";
        }
        if(optionDS){
            DS = "tabSource.datasheet";
        }
        return serializer.rootName("ws").include(prix,spec,DS,prediction).exclude("*.class","tabSource.numberPackagingTempo").serialize(sourcesListe);
    }

    /**
     * Permet de sérialiser un tableau de MPN afin d'obtenir un JSON.
     *
     * @param tabMpn un tableau de String composé de MPNs.
     * @return un JSON composé d'un tableau de couple MPN + Limit.
     * @throws org.codehaus.jettison.json.JSONException : exception levée suite
     * à une erreur sur le JSON (mauvais format/objet non trouvé).
     */
    public static String ToJsonTabMpn(ArrayList<String> tabMpn) throws JSONException {
        //on crée le tableau vide contenant une classe MpnClass (les atrtributs sont un couple MPN-limit)
        ArrayList<MpnClass> mpnListe = new ArrayList<MpnClass>();

        //création des objets de la classe MpnClass
        for (int i = 0; i < tabMpn.size(); i++) {
            MpnClass mpnArray = new MpnClass();
            String mpn = "*" + tabMpn.get(i) + "*";
            mpnArray.setMpn(mpn);
            mpnArray.setLimit(20); //LME : passage à 20 plutot que 10
            mpnListe.add(mpnArray);
        }

        //Methode FLexJson ici est impossible car elle rend un JSON classé par ordre alphabétique
        //Utilisation ici de la méthode JACKSON
        String serialized = null;
        try {
            serialized = new ObjectMapper().writeValueAsString(mpnListe);
        } catch (IOException ex) {
            Logger.getLogger(Json.class.getName()).log(Level.SEVERE, null, ex);
        }
        return serialized;
    }

    /**
     * Permet de sérialiser un tableau de MPN afin d'obtenir un JSON.
     *
     * @param tabSku
     * @return un JSON composé d'un tableau de couple MPN + Limit.
     */
    public static String ToJsonTabSku(ArrayList<String> tabSku) {
        //on crée le tableau vide contenant une classe MpnClass (les atrtributs sont un couple MPN-limit)
        ArrayList<MpnClass> mySkuListe = new ArrayList<MpnClass>();

        //création des objets de la classe MpnClass
        for (int i = 0; i < tabSku.size(); i++) {
            MpnClass skuArray = new MpnClass();
            String sku = "" + tabSku.get(i) + "";
            skuArray.setSku(sku);
            skuArray.setLimit(5);
            mySkuListe.add(skuArray);
        }

        //Methode FLexJson ici est impossible car elle rend un JSON classé par ordre alphabétique
        //Utilisation ici de la méthode JACKSON
        String serialized = null;
        try {
            serialized = new ObjectMapper().writeValueAsString(mySkuListe);
        } catch (IOException ex) {
            Logger.getLogger(Json.class.getName()).log(Level.SEVERE, null, ex);
        }
        return serialized;
    }

    
    /**
     * Permet de sérialiser une liste de WsPrioClass afin d'obtenir un JSON.
     *
     * @param wsPrioListe une liste de WsPrioClass (composée d'un identifiant de
     * WS et de sa priorité).
     * @return un JSON des résultats.
     */
    public static String ToJsonWsPrioListe(ArrayList<WsPrioClass> wsPrioListe) {
        // METHODE JACKSON pour éviter la réorganisation -> SERIALISATION

        String serialized = null;
        try {
            serialized = new ObjectMapper().writeValueAsString(wsPrioListe);
        } catch (IOException ex) {
            Logger.getLogger(Json.class.getName()).log(Level.SEVERE, null, ex);
        }
        return serialized;

        //JSONSerializer serializer = new JSONSerializer();
        //return serializer.rootName("wsDroitPrio").exclude("*.class").serialize(wsPrioListe);
    }

    /**
     *
     * @param arrSource
     * @param optionPrix
     * @return
     * @throws JSONException
     * @throws IOException
     * @throws ParseException
     */
    public static String ToJsonArrSource(ArrayList<Source> arrSource, boolean optionPrix) throws JSONException, IOException, ParseException {
        // METHODE JACKSON pour éviter la réorganisation -> SERIALISATION
        // METHODE FLEXJSON -> SERIALISATION
        JSONSerializer serializer = new JSONSerializer();
        // si le client souhaite afficher le tableau des prix, on le sérialise
        if (optionPrix) {
            return serializer.include("listePrix").exclude("*.class").serialize(arrSource);
        } else {
            return serializer.exclude("*.class").serialize(arrSource);
        }

    }

    /**
     * Serialise la classe client
     *
     * @param client
     * @return String
     * @throws JSONException
     * @throws IOException
     * @throws ParseException
     */
    public static String ToJsonClientDB(ClientDB client) throws JSONException, IOException, ParseException {
         // METHODE JACKSON pour éviter la réorganisation -> SERIALISATION
        // METHODE FLEXJSON -> SERIALISATION
        JSONSerializer serializer = new JSONSerializer();
        return serializer.exclude("*.class").include("arrCustomerPrice").serialize(client);

    }

    public static String ToJsonArrManufacturerList(ArrayList<manufacturerMasterList> arrManufacturer) throws JSONException, IOException, ParseException {
         // METHODE JACKSON pour éviter la réorganisation -> SERIALISATION
        // METHODE FLEXJSON -> SERIALISATION
        JSONSerializer serializer = new JSONSerializer();
        return serializer.exclude("*.class").serialize(arrManufacturer);
    }

}

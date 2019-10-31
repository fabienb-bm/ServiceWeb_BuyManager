package WS;

import POJO.SearchResult;
import java.util.ArrayList;

/**
* Nom de classe : InterfaceWSInterrogeable
* <br>
* Description : Interface implémentant les 3 APIs.
* <br>
* Date de la dernière modification : 22/07/2014
* 
* @author Stagiaire (Florence Giraud)
*/
public interface InterfaceWSInterrogeable{
    
    /**
    * Méthode abstraite permettant de remplir un tableau de SourcesResults avec les résultats de la requête.           
    * @param arrSearchResult tableau in out avec la liste des resultats
    * @see Octopart#RecuperationSources(java.util.ArrayList, int, boolean) 
    * @see Farnell#RecuperationSources(java.util.ArrayList, int, boolean) 
    * @see Rs#RecuperationSources(java.util.ArrayList, int, boolean) 
    */
    void RecuperationMPNs(ArrayList<SearchResult> arrSearchResult);
    
    
    /**
     * 
     * @param arrSearchResult tableau in out avec la liste des resultats
     */
    void RecuperationSKUs(ArrayList<SearchResult> arrSearchResult);

    /**
     * 
     * @param clientID 
     * @param nbCredit 
     */
    public void enregistreCredit(int clientID,int nbCredit);

    public void setParams(ArrayList<SearchResult> sourcesResultSku, boolean optionP, boolean optionS, boolean optionDataS, int clientID, int nbMPNs, String sku);  
    
    void RecuperationDesc(ArrayList<SearchResult> arrSearchResult);
    
    /**
     * Return vrai si une erreur est detecté
     */
    public boolean getErrorStatus();
    
}

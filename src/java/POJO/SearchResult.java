package POJO;

import java.util.ArrayList;

/**
* Nom de classe : SearchResult
 <br>
* Description : Classe permettant le formatage des resultats.
* <br>
* Date de dernière modification : 22/07/2014
* 
* @author Stagiaire (Florence Giraud)
*/
public class SearchResult {
    
    
    /*********************
    * ATTRIBUTS
    **********************/
    
    /**
    * MPN original : MPN unique demandé par le client.
    * @see SourcesResult#getMpnOriginal() 
    * @see SourcesResult#setMpnOriginal(java.lang.String) 
    */
    private String mpnOriginal;
    
    /**
    * Sku original : sku unique demandé par le client.
    * @see SourcesResult#getMpnOriginal() 
    * @see SourcesResult#setMpnOriginal(java.lang.String) 
    */
    private String skuOriginal;

    /**
     * 
     */
    private String descOriginal;

    /**
     *
     * @param descOriginal
     */
    public void setDescOriginal(String descOriginal) {
        this.descOriginal = descOriginal;
    }

    /**
     *
     * @return
     */
    public String getDescOriginal() {
        return descOriginal;
    }
    
    /**
    * Tableau regroupant tous les résultats obtenus suite à une requête.
    * @see SourcesResult#getTabSource() 
    * @see SourcesResult#setTabSource(java.util.ArrayList) 
    */
    private ArrayList<Source> tabSource;
    
    private double qtyPrediction;

    public double getQtyPrediction() {
        return qtyPrediction;
    }

    public void setQtyPrediction(double qtyPrediction) {
        this.qtyPrediction = qtyPrediction;
    }
    
    /*************************
    * Constructeurs
    ************************/
    
    
    /**
    * Constructeur de SourcesResult vide.
    */
    public SearchResult() {
        tabSource = new ArrayList<Source>();
    }

    /**
     *
     * @param skuOriginal
     */
    public void setSkuOriginal(String skuOriginal) {
        this.skuOriginal = skuOriginal;
    }
    /*************************
    * GETTERS
    ************************/
        
    /**
     * GETTERS
     * @return
     */
    public String getSkuOriginal() {
        return skuOriginal;
    }
    


    /***
    * Retourne la valeur de l'attribut "mpnOriginal".
    * @return mpnOriginal : le mpn original demandé par le client.
    */
    public String getMpnOriginal() {
        return mpnOriginal;
    }
    
    /**
    * Retourne la valeur de l'attribut "tabSource".
    * @return tabsource : le tableau contenant les resultats de la requete.
    */
    public ArrayList<Source> getTabSource() {
        return tabSource;
    }
    
    
    /*************************
    * SETTERS
    ************************/

    /**
    * Met à jour le mpn original demandé par le client.
    * @param mpnOriginal le nouveau mpn original.
    */
    public void setMpnOriginal(String mpnOriginal) {
        this.mpnOriginal = mpnOriginal;
    }
    
    /**
    * Met à jour le tableau de résultats de requete.
    * @param tabSource le nouveau tableau de resultats.
    */
    public void setTabSource(ArrayList<Source> tabSource) {
        this.tabSource = tabSource;
    }

    
    /*************************
    * Methodes Publiques
    ************************/
    
    /**
    * Permet d'ajouter une source seulement
    * @param sourceToAdd le résultat à ajouter au tableau
    */
    public void addTabSource(Source sourceToAdd ){
        tabSource.add(sourceToAdd);
    }
    
    /**
    * Permet d'ajouter un tableau de résultats supplémentaires à la suite du tableau existant.
    * @param tabSourceRajout le tableau de résultats à rajouter dans le tableau existant.
    */
    public void addTabSource(ArrayList<Source> tabSourceRajout ){
        tabSource.addAll(tabSourceRajout);
    }
    
    /**
     *
     */
    public void calculLevenshtein(){
        //
        //si mpn et mpnOriginal sont différents alors on a eu une troncature
        //il est donc nécessaire de recalculer le Levensthein
        ArrayList<Source> produitListe = this.getTabSource();
        for (int k=0; k<produitListe.size(); k++){
            String mpnTrouve = produitListe.get(k).getMpn();
            //calcul du Levenshtein
            int rangLeven = LevenshteinDistance.LevenshteinDistanceAlgo(mpnOriginal, mpnTrouve);
            //modification dans les produits
            produitListe.get(k).setRangLevensthein(rangLeven);
        } 
    }
    
    
}

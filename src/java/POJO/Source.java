package POJO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
* Nom de classe : Source
* <br>
* Description : Classe regroupant les attributs des produits que l'on souhaite récupérer.
* <br>
* Date de la dernière modification : 22/07/2014
* 
* @author Stagiaire (Florence Giraud)
*/
public class Source {
    
    
    /*************************
    * Attributs
    ************************/
    
    
    /**
    * Manufacturer part number : numéro unique des fabriquants (cible de la recherche).
    * @see Source#getMpn() 
    * @see Source#setMpn(java.lang.String) 
    */
    private String mpn; 
    
    /**
    * Nom du produit recherché.
    * @see Source#getNomProduit() 
    * @see Source#setNomProduit(java.lang.String) 
    */
    private String nomProduit;
    
    /**
    * Nom du fabriquant lié au produit recherché.
    * @see Source#getnomFabricant() 
    * @see Source#setNomFabricant(java.lang.String)  
    */
    private String nomFabricant;
    
    /**
    * Lien du site du fabricant.
    * @see Source#getUrlFabricant() 
    * @see Source#setUrlFabricant(java.lang.String) 
    */
    private String urlFabricant;
    
    /**
    * Lien du site du WebService interrogé renvoyant le produit recherché.
    * @see Source#getUrlWs() 
    * @see Source#setUrlWs(java.lang.String) 
    */
    private String urlWs;
    
    /**
    * Numéro du produit spécifique au WebService interrogé.
    * @see Source#getUID() 
    * @see Source#setUID(java.lang.String)  
    */
    private String uid;
    
    /**
    * Origine de l'objet suivant l'API interrogée.
    * @see Source#getOrigine() 
    * @see Source#setOrigine(java.lang.String) 
    */
    private String origine;
    
    /**
    * Quantité disponible au total.
    * @see Source#getStock() 
    * @see Source#setStock(java.lang.String) 
    */
    private int stock;
    
    /**
    * Tableau comprenant une liste de fournisseur, quantité (borne inférieure), prix et devise.
    * @see Source#getListePrix() 
    * @see Source#setListePrix(java.util.ArrayList) 
    */
    private ArrayList<Prix> listePrix;
   
    
    private ArrayList<Prediction> listePrediction;

    public ArrayList<Prediction> getListePrediction() {
        return listePrediction;
    }

    public void setListePrediction(ArrayList<Prediction> listePrediction) {
        this.listePrediction = listePrediction;
    }
    
    
    /**
    * Position de l'article dans le JSON.
    * @see Source#getRangOrigine() 
    * @see Source#setOrigine(java.lang.String) 
    */
    private int rangOrigine;

    /**
    * Resultat de l'application de l'algorithme de la distance de Levenshtein entre le mpn recherché et le mpn trouvé.
    * @see Source#getRangLevensthein() 
    * @see Source#setRangLevensthein(int) 
    */
    private int rangLevensthein;
        
    /**
    * Sert à mapper avec les prix pour TME
    */
    private String[] mpqEtMoqTemporaire = new String[2];
    
    /**
    * Sert à mapper avec le packaging pour TME
    */
    private transient String numberPackagingTempo;
    
    /**
    * Tableau comprenant une liste de spécifications
    * @see Source#getListePrix() 
    * @see Source#setListePrix(java.util.ArrayList) 
    */
    private ArrayList<Specs> listeSpecs;
    
    /**
    * Tableau comprenant une liste de spécifications
    * @see Source#getListePrix() 
    * @see Source#setListePrix(java.util.ArrayList) 
    */
    private ArrayList rohsProduit;
    
    /**
    * La ou les datasheets du produit, si disponible
    */
    private ArrayList<Datasheet> datasheet;
    
    private String errorStatus;
    
    private String errorMessage;
    
    /*************************
    * Constructeurs
    ************************/
    
    
    /**
    * Constructeur de Source vide.
    */
    public Source() { 
        //
        listePrix = new ArrayList<Prix>();
        listePrediction = new ArrayList<Prediction>();
        datasheet = new ArrayList<Datasheet>();
        listeSpecs = new ArrayList<Specs> ();
        rohsProduit = new ArrayList();
        //
    }
    
    
    /*************************
    * Getters
    ************************/
    
    
    /**
    * Retourne la valeur de l'attribut "mpn".
    * @return mpn : la valeur du mpn sous forme de String.
    */
    public String getMpn(){
        return this.mpn;
    }
    
    /**
    * Retourne la valeur de l'attribut "nomProduit".
    * @return nomProduit : l'intitule du produit recherché sous forme de String.
    */
    public String getNomProduit(){
        return this.nomProduit;
    } 
   
    /**
    * Retourne la valeur de l'attribut "nomFabricant".
    * @return nomFabricant : le nom du fabricant du produit sous forme de String.
    */
    public String getNomFabricant(){
        return this.nomFabricant;
    }
   
    /**
    * Retourne la valeur de l'attribut l'a"urlFabricant".
    * @return urlFabricant : l'url du fabricant du produit sous forme de String.
    */
    public String getUrlFabricant(){
        return this.urlFabricant;
    }
   
    /**
    * Retourne la valeur de l'attribut "urlWs".
    * @return urlWs : l'url du webservice sous forme de String.
    */
    public String getUrlWs(){
        return this.urlWs;
    }
   
   /**
    * Retourne la valeur de l'attribut "UID".
    * @return uid : la valeur du numéro unique sous forme de String.
    */
    public String getUid(){
        return this.uid;
    }
   
   /**
    * Retourne la valeur de l'attribut "origine".
    * @return origine : l'API ayant donné cette source.
    */
    public String getOrigine(){
        return this.origine;
    }
   
   /**
    * Retourne la valeur de l'attribut "stock".
    * @return stock : la quantite disponible pour cette source.
    */
    public int getStock(){
        return this.stock;
    }
   
    /**
    * Retourne la valeur de l'attribut "listePrix".
    * @return prixTab : le tableau contenant le fournisseur, la quantité (borne inférieure), le prix et la devise.
    */
    public ArrayList<Prix> getListePrix() {
        return listePrix;
    }
    
    /**
    * Retourne la valeur de l'attribut"rangOrigine".
    * @return rangOrigine : la position à laquelle se situe le produit.
    */
    public int getRangOrigine() {
        return rangOrigine;
    }
    
    /**
    * Retourne la valeur de l'attribut "rangLevensthein".
    * @return rangLevensthein : la valeur du résultat de l'algorithme de distance de Levenshtein entre le mpn recherché et le mpn trouvé.
    */
    public int getRangLevensthein() {
        return rangLevensthein;
    }
    
    public String[] getMpqEtMoq(){
        return this.mpqEtMoqTemporaire;
    }
    
 
    /*************************
    * Setters
    ************************/    
    
    /**
    * Met à jour le mpn du produit.
    * @param mpn le nouveau mpn du produit.
    */
    public void setMpn(String mpn){
        this.mpn=mpn;
    }
    
    /**
    * Met à jour le nom du produit.
    * @param nomProduit le nouveau nom du produit.
    */
    public void setNomProduit(String nomProduit){
        this.nomProduit=nomProduit;
    }
    
    /**
    * Met à jour le nom du fabricant du produit.
    * @param nomFabricant le nouveau nom du fabricant.
    */
   public void setNomFabricant(String nomFabricant){
        this.nomFabricant=nomFabricant;
    }
   
    /**
    * Met à jour l'url du fabricant du produit.
    * @param urlFabricant la nouvelle url du fabricant. 
    */
    public void setUrlFabricant(String urlFabricant){
        this.urlFabricant=urlFabricant;
    }
   
    /**
    * Met à jour l'url du service web du produit.
    * @param urlWs la nouvelle url du service web.
    */
    public void setUrlWs(String urlWs){
        this.urlWs=urlWs;
    }
   
    /**
    * Met à jour l'uid du produit.
    * @param uid le nouveau uid du produit.
    */
    public void setUid(String uid){
       this.uid=uid;
    } 
    
    /**
    * Met à jour l'origine du produit.
    * @param origine la nouvelle origine de la source.
    */
    public void setOrigine(String origine){
       this.origine=origine;
    } 
    
    /**
    * Met à jour la quantité disponible du produit.
    * @param stock la nouvelle quantité disponible de la source.
    */
    public void setStock(int stock){
       this.stock=stock;
    } 
    
    /**
    * Met à jour la valeur du tableau de prix.
    * @param listePrix le nouveau tableau de fournisseurs, quantité, prix et devise.
    */
    public void setListePrix(ArrayList<Prix> listePrix) {
        this.listePrix = listePrix;
    }
     
    /**
    * Met à jour le rang d'origine de l'article;
    * @param rangOrigine la nouvelle position à laquelle est le produit.
    */
    public void setRangOrigine(int rangOrigine) {
        this.rangOrigine = rangOrigine;
    }

    /**
    * Met à jour la resultat de la distance de Levenshtein entre le mpn recherché et le mpn trouvé.
    * @param rangLevensthein la nouvelle valeur de la distance de Levenshtein.
    */
    public void setRangLevensthein(int rangLevensthein) {
        this.rangLevensthein = rangLevensthein;
    }
    
    public void setMpqEtMoq(String[] valeurs){
        this.mpqEtMoqTemporaire = valeurs;
    }

    /**
     * @return the listeSpecs
     */
    public ArrayList<Specs> getListeSpecs() {
        return listeSpecs;
    }

    /**
     * @param listeSpecs the listeSpecs to set
     */
    public void setListeSpecs(ArrayList<Specs> listeSpecs) {
        this.listeSpecs = listeSpecs;
    }

     /**
     * @return the rohs
     */
    public ArrayList getRohsProduit() {
        return rohsProduit;
    }

    /**
     * @param rohs the rohs to set
     */
    public void setRohsProduit(ArrayList rohsProduit) {
        this.rohsProduit = rohsProduit;
    }
    
     /**
     * @return the error
     */
    
    public String getErrorStatus()
    {
        return errorStatus;
    }
    
    /**
     * @param error the error to set
     */
    public void setErrorStatus(String errorStatus)
    {
        this.errorStatus = errorStatus;
    }
         /**
     * @return the error
     */
    
    public String getErrorMessage()
    {
        return errorMessage;
    }
    
    /**
     * @param error the error to set
     */
    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
    /**
     * @return the numberPackagingTempo
     */
    public String getNumberPackagingTempo() {
        return numberPackagingTempo;
    }

    /**
     * @param numberPackagingTempo the numberPackagingTempo to set
     */
    public void setNumberPackagingTempo(String numberPackagingTempo) {
        this.numberPackagingTempo = numberPackagingTempo;
    }

    /**
     * @return the datasheet
     */
    public ArrayList<Datasheet> getDatasheet() {
        return datasheet;
    }

    /**
     * @param datasheet the datasheet to set
     */
    public void setDatasheet(ArrayList<Datasheet> datasheet) {
        this.datasheet = datasheet;
    }

    public void initPrediction(double Qty) {
        //
        if (this.listePrix.isEmpty() == false) {
            
            HashMap<String,Prediction> SupplierPrediction = new HashMap();
            Prediction SupplierPrice;
                        
            for (Prix price : this.listePrix) {
                //
                if (SupplierPrediction.containsKey(price.getFournisseur()) == false ) {
                    //
                    // 1er prix pour le fournisseur, on créer un objet prediction
                    SupplierPrice = new Prediction();
                    SupplierPrice.setSupplier(price.getFournisseur());
                    //
                    SupplierPrediction.put(price.getFournisseur(), SupplierPrice);
                    //
                } else {
                    //
                    SupplierPrice = SupplierPrediction.get(price.getFournisseur());
                    //
                }
                SupplierPrice.addPrice(price);
                                
            }
            
            for ( Entry <String,Prediction> entry : SupplierPrediction.entrySet() ) {
                //
                SupplierPrice = entry.getValue();
                SupplierPrice.calculPrediction(Qty);
                //
                this.listePrediction.add(SupplierPrice);
            }
        }
        
    }

    public void add(Specs uneSpec) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

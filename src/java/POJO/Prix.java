package POJO;

/**
* Nom de classe : PrixClass
* <br>
* Description : Classe représentant un tableau de prix contenant comme attribut le fournisseur, la quantité, le prix et la devise
* <br>
* Date de la dernière modification : 22/07/2014
* 
* @author Stagiaire (Florence Giraud)s
*/
public class Prix implements Cloneable {
    
    
    /*************************
    * Attributs
    ************************/
    
    
    /**
    * Nom du fournisseur de l'article.
    * @see PrixClass#getFournisseur() 
    * @see PrixClass#setFournisseur(java.lang.String) 
    */
    private String fournisseur;

    /**
    * Quantité offerte (borne inférieure).
    * @see PrixClass#getQuantite() 
    * @see PrixClass#setQuantite(int) 
    */
    private int quantite;
    
    /**
    * Prix de l'offre, lié à la quantité.
    * @see PrixClass#getPrix() 
    * @see PrixClass#setPrix(double) 
    */
    private double prix; 
    
    /**
    * Devise à laquelle correspond le prix.
    * @see PrixClass#setDevise(java.lang.String) 
    * @see PrixClass#getDevise() 
    */
    private String devise;
    
    /**
    * Numéro du produit chez le distributeur.
    * @see PrixClass#getSku() 
    * @see PrixClass#setSku(java.lang.String) 
    */
    private String sku;

    /**
    * Temps de réapprovisionnement.
    * "Number of days to acquire parts from factory"
    * @see PrixClass#getLeadDays() 
    * @see PrixClass#setLeadDays(int) 
    */
    private int leadDays;
    
    /**
    * Validité du distributeur suivant l'avis du fabriquant.
    * @see PrixClass#setDistribValide(boolean) 
    * @see PrixClass#isDistribValide() 
    */
    private boolean distribValide;
    

    /**
     * Stocke associé à la source
     */
     private int stock;

     /**
     * Texte sur les reserve apporté au prix
     */
    private String stockRegions; 
     
    /**
     * mpq correspondant au packaging quand celui-ci a du sens
     */
    private int mpq;
    
    /**
     * moq correspondant à la quantité minimun d'achat
     */
    private int moq;
    
    /**
     * Correspond à la dernière mise à jour du prix
     */
    private String lastUpdate;
     
    /**
     * renvoie le Stock de la source correspondante au prix
     * @return int
     */
    public int getStock() {
        return stock;
    }

    /**
     * Affecte le stock de la source correspond à ce prix
     * @param stock 
     */
    public void setStock(int stock) {
        this.stock = stock;
    }

    
    
    /**
     * chaine decrivant le packing 
     */
    private String packaging = "";

    /**
     * 
     * @return 
     */
    public String getPackaging() {
        return packaging;
    }
      
    public Object clone() {
        try
        { 
          return super.clone();
        } catch (CloneNotSupportedException x) 
        {
            return null; 
        }
           
    }

    /**
     * @param packaging 
     */
    public void setPackaging(String packaging) {
        if(packaging!= null ){
            //
            // si le packaging est null affecte vide
            if ( packaging.equals("null") ){
                this.packaging = "";
            }else{
                this.packaging = packaging;
            }
            //
        }
    }
    
    /*************************
    * Constructeurs
    ************************/
    
    
    /**
    * Constructeur vide de la classe PrixClass.
    */
     public Prix() {
    }
     
     
    /*************************
    * Getters
    ************************/
    
     
    /**
    * Retourne la valeur de l'attribut "fournisseur".
    * @return fournisseur : le nom du fournisseur de l'article.
    */
    public String getFournisseur() {
        return fournisseur;
    }

    /**
    * Retourne la valeur de l'attribut "quantite".
    * @return quantite : la borne inférieur de la quantité offerte dans l'offre.
    */
    public int getQuantite() {
        return quantite;
    }

    /**
    * Retourne la valeur de l'attribut "prix".
    * @return prix : le prix lié à la quantité dans l'offre.
    */
    public double getPrix() {
        return prix;
    }

    /**
    * Retourne la valeur de l'attribut "devise".
    * @return devise : la devise à laquelle correspond le prix.
    */
    public String getDevise() {
        return devise;
    }
    
    /**
    * Retourne la valeur de l'attribut "sku".
    * @return sku : le numéro donné au produit par le distributeur.
    */
    public String getSku() {
        return sku;
    }

    /**
    * Retourne la valeur de l'attribut "leadDays".
    * @return leadDays : le temps d'approvisionnement.
    */
    public int getLeadDays() {
        return leadDays;
    }

    /**
    * Retourne la valeur du boolean "distribValide".
    * @return true ou false : selon si le distributeur est officiel ou non.
    */
    public boolean isDistribValide() {
        return distribValide;
    }
    
    /**
     * 
     * @param packSize
     */
    public void applyPackSize(int packSize) {
        if (packSize > 0){
            //
            this.quantite   = quantite * packSize;
            this.moq        = moq * packSize;
            this.stock      = stock * packSize;
            this.prix       = prix / packSize; 
            if ( this.mpq > 0 ){
                this.mpq        = this.mpq * packSize;
            }else{
                this.mpq        = packSize;
            }
            
            //
        }
    }
         
       /**
     * 
     * @return 
     */
    public String getStockRegions() {
        return stockRegions;
    }
    
       /**
     * 
     * @return 
     */
    public int getMoq() {
        return moq;
    }
    
      /**
     * 
     * @return 
     */
    public int getMpq() {
        return mpq;
    }


    
    /*************************
    * Setters
    ************************/
   
     /**
     * 
     * @param stockRegions 
     */
    public void setStockRegions(String stockRegions) {
        this.stockRegions = stockRegions;
    }
    
    
    /**
    * Met à jour le nom du fournisseur correspondant à l'offre.
    * @param fournisseur : le nouveau nom du fournisseur.
    */
    public void setFournisseur(String fournisseur) {
        this.fournisseur = fournisseur;
    }

    /**
    * Met à jour la quantité (borne inférieure) correspondant à l'offre.
    * @param quantite : la nouvelle borne inférieur de la quantité offerte.
    */
    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    /**
    * Met à jour le prix correspondant à l'offre.
    * @param prix : le nouveau prix de l'offre.
    */
    public void setPrix(double prix) {
        this.prix = prix;
    }

    /**
    * Met à jour la devise à laquelle correspond le prix.
    * @param devise : la nouvelle devise de l'offre.
    */
    public void setDevise(String devise) {
        if(devise.equals("€") ){
           this.devise = "EUR";
        }else if(devise.equals("£") ){
           this.devise = "GBP";   
        }else if(devise.equals("$") ){
           this.devise = "USD";   
        }else{
           this.devise = devise;
        }
    }
    
    /**
    * Met à jour le numéro du produit donné par le fabriquant.
    * @param sku : le nouveau numéro du produit donné par le distributeur.
    */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
    * Met à jour le nombre de jours pour l'approvisionnement.
    * @param leadDays : le nouveau temps d'approvisionnement.
    */
    public void setLeadDays(int leadDays) {
        this.leadDays = leadDays;
    }

    /**
    * Met à jour la validité du distributeur.
    * @param distribValide : le boolean true/false indiquant si le distributeur est officiel ou non.
    */
    public void setDistribValide(boolean distribValide) {
        this.distribValide = distribValide;
    }

    /**
    * 
    * @param
    */
    public void setMpq(int mpq) {
        this.mpq = mpq;
    }
    
    /**
    * 
    * @param
    */
    public void setMoq(int moq) {
        this.moq = moq;
    }

    /**
     * @return the lastUpdate
     */
    public String getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param lastUpdate the lastUpdate to set
     */
    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}

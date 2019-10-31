package JSON;

/**
* Nom de classe : MpnClass
* <br>
* Description : Classe représentant un couple Mpn et une limite permettant la création d'un JSON pour l'interrogation multiple d'Octopart.
* <br>
* Date de la dernière modification : 22/07/2014
* 
* @author Stagiaire (Florence Giraud)
*/

public class MpnClass {
  
    
    /*************************
    * Attributs
    ************************/
    
    
    /**
    * Manufacturer part number : numéro unique des fabriquants (cible de la recherche).
    * @see MpnClass#getMpn() 
    * @see MpnClass#setMpn(java.lang.String) 
    */
    private String mpn = "";

    /**
    * Stock unique identifier : numéro unique des fournisseurs (cible de la recherche de type SKU).
    * @see MpnClass#getSku() 
    * @see MpnClass#setSku(java.lang.String) 
    */
    private String sku = "";


    /**
    * Nombre de résultats limites autorisés.
    * @see MpnClass#getLimit() 
    * @see MpnClass#setLimit(int) 
    */
    private int limit;
    
    /*************************
    * Constructeurs
    ************************/
    
    
    /**
    * Constructeur vide de MpnClass.
    */
    public MpnClass() {
    }

    
    /*************************
    * Getters
    ************************/
    
    
    /**
    * Retourne la valeur de l'attibut "mpn".
    * @return mpn : la valeur du mpn sous forme de String.
    */
    public String getMpn() {
        return mpn;
    }

    /**
    * Retourne la valeur de l'attribut "limit".
    * @return limit : le nombre de résultats maximum.
    */
    public int getLimit() {
        return limit;
    }
    
    
    /*************************
    * Setters
    ************************/
    
    
    /**
    * Met à jour le mpn.
    * @param mpn la nouvelle valeur du "mpn".
    */
    public void setMpn(String mpn) {
        this.mpn = mpn;
    }

    /**
    * Met à jour la limit.
    * @param limit : la nouvelle valeur de "limit".
    */
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    /**
     *
     * @return
     */
    public String getSku() {
        return sku;
    }

    /**
     *
     * @param sku
     */
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    
}



package com.essai3;

/**
* Nom de classe : WsPrioClass
* <br>
* Description : Classe permettant de définir le JSON de droit et prio des WS
* <br>
* Date de la dernière modification : 06/08/2014
* 
* @author Stagiaire (Florence Giraud)
*/
public class WsPrioClass {
    
    /**
    * Identifiant du Service Web.
    * @see WsPrioClass#getWsId() 
    * @see WsPrioClass#setWsId(int) 
    */
    private int wsId;
    
    /**
    * Interrogation Par défaut lié au service web.
    * @see WsPrioClass#getInterroParDefaut() 
    * @see WsPrioClass#setInterroParDefaut(boolean) 
    */
    private boolean interroParDefaut;
    
    /**
     * donne une information sur l'etat du service
     */
    private boolean errorDetected = false;
    
    /*******************
    * Constructeur
    *********************/
    
    /**
    * Constructeur vide de la class WsPrioClass.
    */
    public WsPrioClass() {
    }


    /***********************
     *      GETTERS
     ************************/

    public boolean isErrorDetected() {
        return errorDetected;
    }
    /**
     * Retourne la valeur de l'attribut "getWsId".
     * @return wsId: identifiant du service web.
     */
    public int getWsId() {
        return wsId;
    }

    /**
    * Retourne la valeur de l'attribut "interroParDefaut".
    * @return interroParDefaut : la valeur de l'interrogation par défaut du SW.
    */
    public boolean getInterroParDefaut(){
        return interroParDefaut;
    }
    
    
    /***********************
    *     SETTERS
    ************************/
    
    public void setErrorDetected(boolean errorDetected) {
        this.errorDetected = errorDetected;
    }
    /**
    * Met à jour l'attribut "wsId".
    * @param wsId : l'identifiant du service web.
    */
    public void setWsId(int wsId) {
        this.wsId = wsId;
    }

    /**
    * Met à jour l'attribut "interroParDefaut".
    * @param interroParDefaut : la valeur de l'interrogation par défaut.
    */
    public void setInterroParDefaut(boolean interroParDefaut){
        this.interroParDefaut=interroParDefaut;
    }
}

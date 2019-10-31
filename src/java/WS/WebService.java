/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WS;

import POJO.SearchResult;
import POJO.Source;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Permet de factoriser certaines méthodes
 * @author dupont
 */
public abstract class WebService{
    /**
     * ***
     * Affecte les paramètres pour effectuer une requete
     *
     * @param sourcesListe - La liste des mpns à interroger et à dupliquer
     * @param optionPrix - Paramètre du prix
     * @param optionSpec - Paramètre des spécifications
     * @param optionDS - Paramètre de la datasheet
     * @param clientID - ID du client
     * @param nbCredit - Nombre de crédits
     * @param typeRequete - Permet de savoir si l'on veut faire une requête MPN
     * ou SKU
     */
    public void setParams(ArrayList<SearchResult> sourcesListe, boolean optionPrix, boolean optionSpec, boolean optionDS, int clientID, int nbCredit, String typeRequete) {
        duplicateResults = new ArrayList<SearchResult>();
        //Boucle pour bien dupliquer et non référencer vers les mêmes objets (essentiellement pour les threads)
        if (typeRequete.equals("mpn")) {
            for (SearchResult sr : sourcesListe) {
                SearchResult sourcesResultat = new SearchResult();
                sourcesResultat.setMpnOriginal(sr.getMpnOriginal());
                duplicateResults.add(sourcesResultat);
            }
        } else if (typeRequete.equals("sku")) {
            for (SearchResult sr : sourcesListe) {
                SearchResult sourcesResultat = new SearchResult();
                sourcesResultat.setSkuOriginal(sr.getSkuOriginal());
                duplicateResults.add(sourcesResultat);
            }
        } else if (typeRequete.equals("desc")){
            for (SearchResult sr : sourcesListe) {
                SearchResult sourcesResultat = new SearchResult();
                sourcesResultat.setDescOriginal(sr.getDescOriginal());
                duplicateResults.add(sourcesResultat);
            }
        }
        this.optionPrix = optionPrix;
        this.optionSpec = optionSpec;
        this.optionDS = optionDS;
        this.clientID = clientID;
        this.nbCredit = nbCredit;
        this.typeRequete = typeRequete;
    }
    /**
     * ***
     * Stockage des paramètres utilisés par tous les WS
     */
    protected ArrayList<SearchResult> duplicateResults;
    protected boolean optionPrix;
    protected boolean optionSpec;
    protected boolean optionDS;
    protected int clientID;
    protected int nbCredit;
    protected String typeRequete;
    
        /**
     * ***
     * Retourne le type de requête
     */
    protected String getTypeRequete() {
        return this.typeRequete;
    }
    
    /**
     * Retourne le type de requête
     * @return Integer
     */
    protected int getNbThread() {
        return 5 ;
    }
    
    
    /**
     * *** //mettre en heritage !!!!!!!
     * Initialise le pool de threads
     */
    protected void procedureThreads(ArrayList<SearchResult> sourcesListe) {
        int k = 0;
        ArrayList<ArrayList<SearchResult>> listePool = new ArrayList<ArrayList<SearchResult>>();
        ArrayList<SearchResult> poolTempo = null;
        //Création de pool de 2 mpns
        while (k < sourcesListe.size()) {
            //Regler la valeur du modulo pour choisir le nombre de threads dans un pool
            if (k % this.getNbThread() == 0) {
                ArrayList<SearchResult> pool = new ArrayList<SearchResult>();
                listePool.add(pool);
                poolTempo = pool;
                SearchResult sr = new SearchResult();
                //
                if (this.getTypeRequete().equals("mpn")) {
                    sr.setMpnOriginal(sourcesListe.get(k).getMpnOriginal());
                } else if  (this.getTypeRequete().equals("sku")) {
                    sr.setSkuOriginal(sourcesListe.get(k).getSkuOriginal());
                } else{
                    sr.setDescOriginal(sourcesListe.get(k).getDescOriginal());
                }
                //
                poolTempo.add(sr);
            } else {
                SearchResult sr = new SearchResult();
                if (this.getTypeRequete().equals("mpn")) {
                    sr.setMpnOriginal(sourcesListe.get(k).getMpnOriginal());
                } else if  (this.getTypeRequete().equals("sku")) {
                    sr.setSkuOriginal(sourcesListe.get(k).getSkuOriginal());
                } else{
                    sr.setDescOriginal(sourcesListe.get(k).getDescOriginal());
                }
                poolTempo.add(sr);
            }
            k++;
        }
        poolTempo = null; // Mettre à null evite de dupliquer l'information
        int pos = 0;
        //Envoie des requêtes à RS par pool
        for (ArrayList<SearchResult> poolActif : listePool) {
            executionThreads(poolActif);
            for (SearchResult sr : poolActif) {
                sourcesListe.get(pos).setTabSource(sr.getTabSource());
                pos++;
            }
        }
    }
    
    /**
     * 
     * @return Callable<ArrayList<Source>>
     */
    protected Callable<ArrayList<Source>> getTMT(SearchResult mySearchResult){
        return null;
    }

    
        /**
     * ***
     * Méthode qui execute un pool de thread
     *
     * @param poolActif - Le pool de threads à executer
     */
    protected void executionThreads(ArrayList<SearchResult> poolActif) {
        //
        List<Future<ArrayList<Source>>> futures = new ArrayList<Future<ArrayList<Source>>>();
        ExecutorService executor = Executors.newFixedThreadPool(poolActif.size());

        for (int i = 0; i < poolActif.size(); i++) {
            //
            Callable<ArrayList<Source>> tmt = this.getTMT(poolActif.get(i));
            //
            Future<ArrayList<Source>> future = executor.submit(tmt);
            futures.add(future);
        }
        int cpt = 0;
        for (Future<ArrayList<Source>> f : futures) {
            try {
                ArrayList<Source> renvoie = f.get();
                if (renvoie != null) {
                    poolActif.get(cpt).addTabSource(renvoie);
                }
            } catch (ExecutionException | InterruptedException ex) {
                Logger.getLogger(WebService.class.getName()).log(Level.SEVERE, null, ex);
            }
            cpt++;
        }
        executor.shutdown();
    }
    
    

}

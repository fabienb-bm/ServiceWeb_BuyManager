/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.essai3;

import DB.WsClientDB;
import POJO.SearchResult;
import WS.InterfaceWSInterrogeable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dupont
 */
public class MonoThread {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test des Threads
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testThreadsWithout() throws Exception {
        Long start = System.currentTimeMillis();
        System.out.println("SANS THREAD");
        String mpn = "PCF";
        ArrayList<SearchResult> sources = new ArrayList<SearchResult>();
        for(int i = 0;i<10;i++){
            SearchResult sr = new SearchResult();
            sr.setMpnOriginal(mpn);
            sources.add(sr);
        }/*
        SearchResult sr = new SearchResult();
        sr.setMpnOriginal(mpn);
        sources.add(sr);

        SearchResult sr2 = new SearchResult();
        sr2.setMpnOriginal("2450AT42A100E");
        sources.add(sr2);
        SearchResult sr3 = new SearchResult();
        sr3.setMpnOriginal("C0603X5R1A104K");
        sources.add(sr3);
        SearchResult sr4 = new SearchResult();
        sr4.setMpnOriginal("DRV8833RTYT");
        sources.add(sr4);
        SearchResult sr5 = new SearchResult();
        sr5.setMpnOriginal("POLOLU");
        sources.add(sr5);
        SearchResult sr6 = new SearchResult();
        sr6.setMpnOriginal("FA-238");
        sources.add(sr6);
         SearchResult sr7 = new SearchResult();
         sr7.setMpnOriginal("BAS");
         sources.add(sr7);
         SearchResult sr8 = new SearchResult();
         sr8.setMpnOriginal("OP");
         sources.add(sr8);
         SearchResult sr9 = new SearchResult();
         sr9.setMpnOriginal("ECP");
         sources.add(sr9);
         SearchResult sr10 = new SearchResult();
         sr10.setMpnOriginal("PCF");
         sources.add(sr10);*/

        System.out.println("Recherche sur " + sources.size() + " mpns");
        ArrayList<InterfaceWSInterrogeable> listeWs = WsClientDB.creationListeWs(1, 1, "");
        System.out.println("----------------------------------------");
        for (InterfaceWSInterrogeable listeW : listeWs) {
            System.out.println("execution de " + listeW.getClass().getCanonicalName());
            try {
                listeW.setParams(sources, true, true, true, 1, 0, "mpn");
                listeW.RecuperationMPNs(sources);
                //
                // comptage specifique
                listeW.enregistreCredit(1, 0);
                //
                //modification du rang Levenshtein
                for (SearchResult sourcesListe1 : sources) {
                    sourcesListe1.calculLevenshtein();
                }
            } catch (RuntimeException e) {
                Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        Long end = System.currentTimeMillis();
        System.out.println("----------------------------------------");
        System.out.println("La plateforme a mis : " + (end - start) + " ms à réponde");
        System.out.println("----------------------------------------");
        System.out.println("----------------------------------------");
        System.out.println("----------------------------------------");
        System.out.println("----------------------------------------");
        System.out.println("----------------------------------------");
        System.out.println("----------------------------------------");
        System.out.println("----------------------------------------");

    }
}

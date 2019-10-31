/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.essai3;

import DB.WsClientDB;
import POJO.SearchResult;
import WS.InterfaceWSInterrogeable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dupont
 */
public class PlusieursThreadsClient {

    public PlusieursThreadsClient() {
    }

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
    public void testThreads() throws Exception {
        Long start = System.currentTimeMillis();
        ///System.out.println("AVEC THREADS");

        List<Future<ArrayList<SearchResult>>> futures = new ArrayList<Future<ArrayList<SearchResult>>>();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        String mpn = "BAV99";
        ArrayList<SearchResult> sources = new ArrayList<SearchResult>();
        for(int i = 0;i<6;i++){
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
        sources.add(sr6);/*
         SearchResult sr7 = new SearchResult();
         sr7.setMpnOriginal("BAS");
         sources.add(sr7);
         SearchResult sr8 = new SearchResult();
         sr8.setMpnOriginal("OP");
         sources.add(sr8);
         SearchResult sr9 = new SearchResult();
         sr9.setMpnOriginal("ECP");
         sources.add(sr9);*/

        //System.out.println("Recherche sur "+sources.size()+" mpns");
        ArrayList<InterfaceWSInterrogeable> listeWs = WsClientDB.creationListeWs(1, 1,"");
//        System.out.println("----------------------------------------");
//        System.out.println("Phase d'execution des threads");
        long time;
        SimpleDateFormat sdf;
        Date resultdate;
        for (InterfaceWSInterrogeable listeW : listeWs) {
            listeW.setParams(sources, true, true, true, 1, 0, "mpn");
            time = System.currentTimeMillis();
            sdf = new SimpleDateFormat("HH:mm ss:SS");
            resultdate = new Date(time);
            //System.out.println("execution de " + listeW.getClass().getCanonicalName() + " à " + sdf.format(resultdate));
            Future<ArrayList<SearchResult>> future = executor.submit((Callable<ArrayList<SearchResult>>) listeW);
            futures.add(future);
        }
        //System.out.println("----------------------------------------");

        // System.out.println("Phase de récupérations des résultats");
        for (Future<ArrayList<SearchResult>> f : futures) {
            try {
                //System.out.println("en attente de réponse à "+sdf.format(resultdate));
                ArrayList<SearchResult> renvoie = f.get();
                time = System.currentTimeMillis();
                sdf = new SimpleDateFormat("HH:mm ss:SS");
                resultdate = new Date(time);
                //System.out.println(renvoie.get(0).getTabSource().size() + " resultats reçus" + " à " + sdf.format(resultdate));
                for (int i = 0; i < renvoie.size(); i++) {
                    sources.get(i).addTabSource(renvoie.get(i).getTabSource());
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ServiceResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        assertTrue(sources.get(0).getTabSource().size() > 0);
        executor.shutdown();
        Long end = System.currentTimeMillis();
        /*
         System.out.println("----------------------------------------");
         System.out.println("La plateforme a mis : " + (end - start) + " ms à réponde");
         System.out.println("----------------------------------------");
         System.out.println("----------------------------------------");
         System.out.println("----------------------------------------");
         System.out.println("----------------------------------------");
         System.out.println("----------------------------------------");
         System.out.println("----------------------------------------");
         System.out.println("----------------------------------------");*/

    }

    /**
     * Test des Threads en client multiple
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testClientsMultiples() throws Exception {
        int tours = 5;
        Long start = System.currentTimeMillis();
        for (int k = 0; k < tours; k++) {
            Long vagueStart = System.currentTimeMillis();
            int nb = 3;//compter n-1
            List<Runnable> runnables = new ArrayList<Runnable>();
            for (int i = 0; i < nb; i++) {
                runnables.add(new simuClient());
            }
            ExecutorService execute = Executors.newFixedThreadPool(nb);
            for (Runnable r : runnables) {
                execute.execute(r);
            }
            execute.shutdown();//Attends la fin de tous les threads avant de finir le programme
            while (!execute.isTerminated()) {
            }
            Long vagueEnd = System.currentTimeMillis();
            System.out.println("---------------->Vague " + (k + 1) + " | " + (vagueEnd - vagueStart) + " ms");
        }
        Long end = System.currentTimeMillis();
        System.out.println("La plateforme a mis : " + (end - start) + " ms à réponde pour un total de : " + (tours * 6) + " mpns");
    }

    public class simuClient implements Runnable {

        @Override
        public void run() {
            try {
                testThreads();
            } catch (Exception ex) {
                Logger.getLogger(PlusieursThreadsClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

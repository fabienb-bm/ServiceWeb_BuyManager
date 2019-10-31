/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WS;

import POJO.SearchResult;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dupont
 */
public class TMETest {
    
    public TMETest() {
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
     * Test of RecuperationMPNs method, of class TME.
     */
    @Test
    public void testRecuperationMPNs() {
        System.out.println("RecuperationMPNs");
        ArrayList<SearchResult> sourcesListe = new ArrayList<SearchResult>();
        
        SearchResult SearchResult  = new SearchResult();
        SearchResult.setMpnOriginal("M4102%208");
        sourcesListe.add(SearchResult);
        SearchResult SearchResult2  = new SearchResult();
        SearchResult.setMpnOriginal("BAV99");
        sourcesListe.add(SearchResult2);
        TME instance = new TME();
        instance.setParams(sourcesListe, true, true, true, 1, 0, "mpn");
        instance.RecuperationMPNs(sourcesListe);
        assertTrue(sourcesListe.size() == 2);
        assertTrue(sourcesListe.get(1).getTabSource().isEmpty());
        assertTrue(sourcesListe.get(0).getTabSource().size() > 0);
    }

        /**
     * Test of RecuperationMPNs method, of class TME.
     */
    @Test
    public void testRecuperationMPN_ponctuation() {
        System.out.println("RecuperationMPNs");
        ArrayList<SearchResult> sourcesListe = new ArrayList<SearchResult>();
        
        SearchResult SearchResult  = new SearchResult();
        SearchResult.setMpnOriginal("BAV 99");
        sourcesListe.add(SearchResult);
        
        SearchResult SearchResult2  = new SearchResult();
        SearchResult2.setMpnOriginal("BAV99 215");
        sourcesListe.add(SearchResult2);
        
        TME instance = new TME();
        instance.setParams(sourcesListe, true, true, true, 1, 0, "mpn");
        instance.RecuperationMPNs(sourcesListe);
        //
        assertTrue(sourcesListe.size() == 2);
        assertTrue(sourcesListe.get(1).getTabSource().size() > 0);
        assertTrue(sourcesListe.get(0).getTabSource().size() > 0);
        //
    }
    
//    /**
//     * Test of RecuperationSKUs method, of class TME.
//     */
//    @Test
//    public void testRecuperationSKUs() {
//        System.out.println("RecuperationSKUs");
//        ArrayList<SearchResult> sourcesListe = null;
//        TME instance = new TME();
//        instance.RecuperationSKUs(sourcesListe);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of enregistreCredit method, of class TME.
//     */
//    @Test
//    public void testEnregistreCredit() {
//        System.out.println("enregistreCredit");
//        int clientID = 0;
//        int nbCredit = 0;
//        TME instance = new TME();
//        instance.enregistreCredit(clientID, nbCredit);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of call method, of class TME.
//     */
//    @Test
//    public void testCall() throws Exception {
//        System.out.println("call");
//        TME instance = new TME();
//        ArrayList<SearchResult> expResult = null;
//        ArrayList<SearchResult> result = instance.call();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}

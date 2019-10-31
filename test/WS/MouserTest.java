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
 * @author LME
 */
public class MouserTest {
    
    public MouserTest() {
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
     * Test of RecuperationMPNs method, of class Mouser.
     */
    @Test
    public void testRecuperationMPNs() {
        System.out.println("RecuperationMPNs");
        
        ArrayList<SearchResult> arrSearchResult = new ArrayList<SearchResult>() ;
        SearchResult SearchResult  = new SearchResult();
        SearchResult.setMpnOriginal("863 MMSZ5242BT1G");
        arrSearchResult.add(SearchResult);
        
        Mouser instance = new Mouser();
        // TODO review the generated test code and remove the default call to fail.
        instance.setParams(arrSearchResult, true, true, true, 1, 0, "mpn");
        instance.RecuperationMPNs(arrSearchResult);
        //
        
        //
    }

    /**
     * Test of RecuperationSKUs method, of class Mouser.
     */
    @Test
    public void testRecuperationSKUs() {
        System.out.println("RecuperationSKUs");
        ArrayList<SearchResult> sourcesListe = null;

     
    }

   
    
}

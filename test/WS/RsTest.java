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
public class RsTest {
    
    public RsTest() {
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
     * Test of RecuperationMPNs method, of class Rs.
     */
    @Test
    public void testRecuperationMPNs() {
        System.out.println("RecuperationMPNs");
        //
        ArrayList<SearchResult> arrSearchResult = new ArrayList<SearchResult>() ;
        SearchResult SearchResult  = new SearchResult();
        SearchResult.setMpnOriginal("bav99");
        arrSearchResult.add(SearchResult);
        Rs instance = new Rs();
        //
        instance.setParams(arrSearchResult, true, true, true, 1, 0, "mpn");
        instance.RecuperationMPNs(arrSearchResult);
        // TODO review the generated test code and remove the default call to fail.
        assertTrue(SearchResult.getTabSource().size() > 0);
    }

    /**
     * Test of RecuperationSKUs method, of class Rs.
     */
    @Test
    public void testRecuperationSKUs() {
        System.out.println("RecuperationSKUs");
        //
        ArrayList<SearchResult> arrSearchResult = new ArrayList<SearchResult>() ;
        SearchResult SearchResult  = new SearchResult();
        SearchResult.setSkuOriginal("7384920");
        arrSearchResult.add(SearchResult);
        Rs instance = new Rs();
        //
        instance.setParams(arrSearchResult, true, true, true, 1, 0, "sku");
        instance.RecuperationSKUs(arrSearchResult);
        // TODO review the generated test code and remove the default call to fail.
        assertTrue(SearchResult.getTabSource().size() > 0);
    }
    
}

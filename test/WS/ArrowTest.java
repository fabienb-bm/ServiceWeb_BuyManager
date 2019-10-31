
/**
 * @copyright pertilience 205
 */
package WS;

import POJO.SearchResult;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Maisonnass
 */


public class ArrowTest {
    
    public ArrowTest() {
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
     * Test of RecuperationMPNs method, of class Farnell.
     */
    @Test
    public void testRecuperationMPNs() {
        System.out.println("RecuperationMPNs");
        
         System.out.println("RecuperationMPNs");
        
        ArrayList<SearchResult> arrSearchResult = new ArrayList<SearchResult>() ;
        SearchResult SearchResult  = new SearchResult();
        SearchResult.setMpnOriginal("bav99");
        
        Arrow instance = new Arrow();
        instance.setParams(arrSearchResult, true, true, true, 1, 0, "mpn");
        instance.RecuperationMPNs(arrSearchResult);
        
    }


}

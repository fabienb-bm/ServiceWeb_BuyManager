/**
 * @copyright pertilience
 **/

package WS;

import POJO.SearchResult;
import POJO.Source;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import org.codehaus.jettison.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Maisonnass
 */


public class DigikeyTest {
    
    public DigikeyTest() {
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
        //
        ArrayList<SearchResult> arrSearchResult = new ArrayList<SearchResult>() ;
        SearchResult SearchResult  = new SearchResult();
        SearchResult.setMpnOriginal("BAV99LT1G");
        //
        Digikey instance = new Digikey();
        instance.setParams(arrSearchResult, true, true, true, 1, 0, "mpn");
        instance.RecuperationMPNs(arrSearchResult);
        //
    }   
    
}


package WS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;


/**
 * This web service provides a service-oriented API to Digi-Key part information for authorized partners.
 * 
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "SearchService", targetNamespace = "http://services.digikey.com/SearchWS", wsdlLocation = "http://servicestest.digikey.com/search/search.asmx?wsdl")
public class SearchService
    extends Service
{

    private final static URL SEARCHSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(SearchService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = SearchService.class.getResource(".");
            url = new URL(baseUrl, "http://servicestest.digikey.com/search/search.asmx?wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'http://servicestest.digikey.com/search/search.asmx?wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        SEARCHSERVICE_WSDL_LOCATION = url;
    }

    public SearchService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SearchService() {
        super(SEARCHSERVICE_WSDL_LOCATION, new QName("http://services.digikey.com/SearchWS", "SearchService"));
    }

}

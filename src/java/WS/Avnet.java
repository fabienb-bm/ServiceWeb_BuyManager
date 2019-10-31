/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WS;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;

/**
 *
 * @author dupont
 */
public class Avnet {

    public String interroAvnet() {
        Client client = ClientBuilder.newClient(new ClientConfig());

        WebTarget webTarget = client.target("http://avnetexpress.avnet.com/part/c/")
                .path("/part/c/BAV99")
                .queryParam("r", "EMEA")
                .queryParam("l", "-2")
                .queryParam("c", "EUR");

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN_TYPE);
        String reponse = invocationBuilder.get(String.class);
        return reponse;
    }

}

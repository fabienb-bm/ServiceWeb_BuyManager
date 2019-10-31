/**
 * Nom de classe : ApplicationConfig
 * <br>
 * Description : 
 * <br>
 * Date de dernière modification : 10/06/2014
 * 
 * @author Stagiaire
 */

package com.essai3;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author LME
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    /**
     *
     * @return
     */
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.essai3.ServiceResource.class);
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.essai3;

import javax.servlet.http.HttpServletRequest;
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
public class ServiceResourceTest {

    public ServiceResourceTest() {
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
     * Test of getMPNs method, of class ServiceResource.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMPNs() throws Exception {

        System.out.println("getMPNs");
        String listeMpn = "bav99";
        String key = "1411";
        String cusKey = "loic";
        String optionPrix = "true";
        String optionSpec = "true";
        HttpServletRequest request = null;
        ServiceResource instance = new ServiceResource();
        
        String result = instance.getMPNs(listeMpn, key, cusKey,"", optionPrix, optionSpec, "true","",request);
        assertTrue(!result.isEmpty());
        assertTrue(result.contains("\"mpnOriginal\":\"bav99\""));

        System.out.println("getMPNs");
        listeMpn = "";
        key = "1411";
        cusKey = "loic";
        result = instance.getMPNs(listeMpn, key, cusKey,"", optionPrix, optionSpec,"true","", request);
        assertTrue(result.contains("ws"));
        assertTrue(result.contains("\"tabSource\":[]"));

    }

    /**
     * Test of getSKUs method, of class ServiceResource.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetSKUs() throws Exception {
        System.out.println("getSKUs");
        String listeSku = "2112753";
        String key = "1411";
        String cusKey = "loic";
        int serviceId = 0;
        String optionPrix = "true";
        String optionSpec = "true";
        HttpServletRequest request = null;
        ServiceResource instance = new ServiceResource();
        String result = instance.getSKUs(listeSku, key, cusKey,"", serviceId, optionPrix, optionSpec, "true","",request);
        assertTrue(!result.isEmpty());

    }
    
    
    /**
     * Test of getSKUs method, of class ServiceResource.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetDescriptions() throws Exception {
        System.out.println("getSKUs");
        String description = "diode zener";
        String key = "1411";
        String cusKey = "loic";
        String optionPrix = "true";
        String optionSpec = "true";
        HttpServletRequest request = null;
        ServiceResource instance = new ServiceResource();
        String result = instance.getDescriptions(description, key, cusKey,"", optionPrix, optionSpec, "true","",request);
        //
        assertTrue(!result.isEmpty());

    }
    

    /**
     * Test of getNbReqRestantes method, of class ServiceResource.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetNbReqRestantes() throws Exception {
        System.out.println("getNbReqRestantes");
        String key = "1411";
        String cusKey = "";
        ServiceResource instance = new ServiceResource();
        String result = instance.getNbReqRestantes(key, cusKey);
        assertTrue(Integer.valueOf(result) > 0);
    }

    /**
     * Test of getClientInfo method, of class ServiceResource.
     */
    @Test
    public void testGetClientInfo() throws Exception {
        System.out.println("getClientInfo");
        String key = "1411";
        String customerKey = "";
        ServiceResource instance = new ServiceResource();
        String result = instance.getClientInfo(key, customerKey);
        assertTrue(!result.isEmpty());
    }

    

    

}

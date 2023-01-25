/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalthinking;


import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author sotobotero
 */
@RestController
public class HelloResource {
    
    @GetMapping("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Spring boot";
    }
    
}

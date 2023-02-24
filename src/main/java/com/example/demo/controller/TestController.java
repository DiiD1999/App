package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author DiiD
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<Object> Get(){
        return new ResponseEntity<>("test", HttpStatus.OK);
    }
}

package com.spring.nahong.service.serviceuser.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class UserController {

    @Value("${server.port}")
    String port;

    @RequestMapping("/say")
    public String say(@RequestParam("name") String name) {
        return "Hi, my name is " + name + ", port: " + port;
    }
}

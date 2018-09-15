package com.spring.nahong.client.feign.clientfeign.controller;

import com.spring.nahong.client.feign.clientfeign.feign.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("/user/hi")
    public String say(@RequestParam("name") String name) {
        return userService.say(name);
    }
}

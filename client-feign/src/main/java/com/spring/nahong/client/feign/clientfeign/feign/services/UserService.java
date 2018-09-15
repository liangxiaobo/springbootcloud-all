package com.spring.nahong.client.feign.clientfeign.feign.services;

import com.spring.nahong.client.feign.clientfeign.feign.api.UserClientFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserClientFeign userClientFeign;

    public String say(String name) {
        return userClientFeign.sayFromClient(name);
    }
}

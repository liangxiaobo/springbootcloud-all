package com.spring.nahong.client.feign.clientfeign.hystrix;

import com.spring.nahong.client.feign.clientfeign.feign.api.UserClientFeign;
import org.springframework.stereotype.Component;

@Component
public class UserHystrix implements UserClientFeign {

    @Override
    public String sayFromClient(String name) {
        return "hi , sorry error!";
    }
}

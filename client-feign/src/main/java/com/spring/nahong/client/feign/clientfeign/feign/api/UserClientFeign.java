package com.spring.nahong.client.feign.clientfeign.feign.api;

import com.spring.nahong.client.feign.clientfeign.feign.config.FeignConfig;
import com.spring.nahong.client.feign.clientfeign.hystrix.UserHystrix;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "service-user", configuration = FeignConfig.class, fallback = UserHystrix.class)
public interface UserClientFeign {
    @GetMapping("/user/say")
    String sayFromClient(@RequestParam("name") String name);
}


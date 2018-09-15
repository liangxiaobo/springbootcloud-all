package com.spring.nahong.ribbon.clientorderribbon.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderRibbonService {
    @Autowired
    RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "orderInfoError")
    public String orderInfo(String orderId) {
        return restTemplate.getForObject("http://service-order:8764/order/info?order_id="+orderId, String.class);
    }

    public String orderInfoError(String orderId) {
        return "sorry, error ribbon hystrix!";
    }
}

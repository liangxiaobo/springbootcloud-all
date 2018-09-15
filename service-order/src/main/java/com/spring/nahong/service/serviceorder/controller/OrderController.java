package com.spring.nahong.service.serviceorder.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Value("${server.port}")
    String port;

    @RequestMapping("info")
    public String orderInfo(@RequestParam("order_id") String orderId) {
        return "order id is "+ orderId+ ", port: " + port;
    }
}

package com.spring.nahong.ribbon.clientorderribbon.controller;

import com.spring.nahong.ribbon.clientorderribbon.service.OrderRibbonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    @Autowired
    OrderRibbonService orderRibbonService;
    @GetMapping("/order/info")
    public String orderInfo(@RequestParam("id") String orderId) {
        return orderRibbonService.orderInfo(orderId);
    }
}

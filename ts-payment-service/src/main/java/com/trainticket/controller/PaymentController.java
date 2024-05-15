package com.trainticket.controller;

import com.trainticket.entity.Payment;

















import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.trainticket.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author Chenjie
 * @date 2017/4/7
 */
@RestController
@RequestMapping("/api/v1/paymentservice")
public class PaymentController { 
    private static final Logger logger = LogManager.getLogger(PaymentController.class);





















    @Autowired
    PaymentService service;

    @GetMapping(path = "/welcome")
    public String home() {
        return "Welcome to [ Payment Service ] !";
    }

    @PostMapping(path = "/payment")
    public HttpEntity pay(@RequestBody Payment info, @RequestHeader HttpHeaders headers) {
        return ok(service.pay(info, headers));
    }

    @PostMapping(path = "/payment/money")
    public HttpEntity addMoney(@RequestBody Payment info, @RequestHeader HttpHeaders headers) {
        return ok(service.addMoney(info, headers));
    }

    @GetMapping(path = "/payment")
    public HttpEntity query(@RequestHeader HttpHeaders headers) {
        return ok(service.query(headers));
    }
}

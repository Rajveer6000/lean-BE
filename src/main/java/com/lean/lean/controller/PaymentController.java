package com.lean.lean.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.IntentDto;
import com.lean.lean.dto.WebHookRequestDto;
import com.lean.lean.repository.LeanUserRepository;
import com.lean.lean.repository.UserRepository;
import com.lean.lean.service.PaymentService;
import com.lean.lean.service.webhook.WebhookService;
import com.lean.lean.util.LeanApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private WebhookService webhookService;

    @Autowired
    private PaymentService paymentService;


    @PostMapping("/create-payment-intent")
    public ResponseEntity<Object> createPaymentIntent(@RequestBody IntentDto intentDto) {
        Object response = paymentService.createPaymentIntent(intentDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment-source")
    public ResponseEntity<Object> getPaymentSource(@RequestParam Long userId, @RequestParam String paymentSourceId) {
        Object response = paymentService.getPaymentSource(userId, paymentSourceId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody WebHookRequestDto webhookPayloadDto) {
        webhookService.processWebhook(webhookPayloadDto);
        return ResponseEntity.ok("Webhook processed successfully");
    }



}
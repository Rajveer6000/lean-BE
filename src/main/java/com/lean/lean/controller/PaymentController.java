package com.lean.lean.controller;

import com.lean.lean.dto.WebhookPayloadDto;
import com.lean.lean.service.webhook.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private WebhookService webhookService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody WebhookPayloadDto webhookPayloadDto) {
        webhookService.processWebhook(webhookPayloadDto);
        return ResponseEntity.ok("Webhook processed successfully");
    }


}
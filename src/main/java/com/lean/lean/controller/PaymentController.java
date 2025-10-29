package com.lean.lean.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dto.WebHookRequestDto;
import com.lean.lean.service.webhook.WebhookService;
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

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody WebHookRequestDto webhookPayloadDto) {
        log.info("Received webhook payload: {}", webhookPayloadDto);
        webhookService.processWebhook(webhookPayloadDto);
        try {
            String json = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(webhookPayloadDto);
            System.out.println("Received webhook payload:\n" + json); // or log.info(...)
        } catch (Exception e) {
            System.out.println("Failed to serialize webhook payload: " + e.getMessage());
        }
        return ResponseEntity.ok("Webhook processed successfully");
    }



}
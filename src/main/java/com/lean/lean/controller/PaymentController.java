package com.lean.lean.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.IntentDto;
import com.lean.lean.dto.WebHookRequestDto;
import com.lean.lean.repository.LeanUserRepository;
import com.lean.lean.repository.UserRepository;
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
    private LeanApiUtil leanApiUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeanUserRepository leanUserRepository;


    @PostMapping("/create-payment-intent")
    public ResponseEntity<Object> createPaymentIntent(@RequestBody IntentDto intentDto) {
        log.info("Creating payment intent for amount: {}", intentDto);
        User user = userRepository.findById(intentDto.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(user.getId());
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        String accessToken = leanApiUtil.getAccessToken();
        Object response = leanApiUtil.createPaymentIntent(intentDto,accessToken,leanUser.getLeanUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment-source")
    public ResponseEntity<Object> getPaymentSource(
            @RequestParam Long userId,
            @RequestParam String paymentSourceId
    ) {
        log.info("Fetching payment source for userId: {}, paymentSourceId: {}", userId, paymentSourceId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LeanUser leanUser = leanUserRepository.findFirstByUserId(user.getId());
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        String accessToken = leanApiUtil.getAccessToken();
        Object response = leanApiUtil.getPaymentSource(accessToken,leanUser.getLeanUserId(),paymentSourceId);
        return ResponseEntity.ok(response);
    }

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
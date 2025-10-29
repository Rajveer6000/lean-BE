package com.lean.lean.service.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dao.LeanWebhookLog;
import com.lean.lean.dto.WebHookRequestDto;
import com.lean.lean.repository.LeanWebhookLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Slf4j
@Service
public class WebhookServiceImpl implements WebhookService {

    @Autowired
    private LeanWebhookLogRepository leanWebhookLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Transactional
    public LeanWebhookLog processWebhook(WebHookRequestDto webhookPayloadDto) {
        LeanWebhookLog logRow = new LeanWebhookLog();
        logRow.setTimestamp(webhookPayloadDto.getTimestamp());
        logRow.setPayload(webhookPayloadDto.getPayload());
        logRow.setType(webhookPayloadDto.getType());
        logRow.setEventId(webhookPayloadDto.getEvent_id());
        logRow.setMessage(webhookPayloadDto.getMessage());
        logRow.setCreatedAt(LocalDateTime.now());
        logRow.setUpdatedAt(LocalDateTime.now());
        logRow = leanWebhookLogRepository.save(logRow);

//        Integer statusCode;
//        String errMsg = null;
//
        try {
//                Process the webhook payload here according to it type and content
            log.info("Processing webhook: {}", logRow.getId());
        } catch (Exception e) {
            log.error("Error processing webhook {}: {}", logRow.getId(), e.getMessage());
        }
        return leanWebhookLogRepository.findById(logRow.getId()).orElse(logRow);
    }
}
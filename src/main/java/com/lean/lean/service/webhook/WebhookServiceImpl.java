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
//        try {
            // -------------------------------------------
            // TODO: domain logic per event type
            // switch (logRow.getEventType()) {
            //   case "entity.created": handleEntityCreated(webhookPayloadDto); break;
            //   // ...
            // }
            // Optionally set a structured 'response' object for observability:
            // logRow.setResponse(Map.of("message", "processed", "at", Instant.now().toString()));
            // leanWebhookLogRepository.save(logRow);
            // -------------------------------------------
//            statusCode = 200;
//        } catch (Exception ex) {
//            statusCode = 500;
//            errMsg = truncate(safeMsg(ex), 2000); // keep within column limit
//            log.error("Webhook processing failed: {}", errMsg, ex);
//        }
        return leanWebhookLogRepository.findById(logRow.getId()).orElse(logRow);
    }
}
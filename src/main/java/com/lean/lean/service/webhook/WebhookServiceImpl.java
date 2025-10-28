package com.lean.lean.service.webhook;

import com.lean.lean.dao.LeanWebhookLog;
import com.lean.lean.dto.WebhookPayloadDto;
import com.lean.lean.repository.LeanWebhookLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WebhookServiceImpl implements WebhookService {

    @Autowired
    private LeanWebhookLogRepository leanWebhookLogRepository;

    @Override
    public LeanWebhookLog processWebhook(WebhookPayloadDto webhookPayloadDto) {
        LeanWebhookLog webhookLog = new LeanWebhookLog();
        webhookLog.setEventName(webhookPayloadDto.getEventName());
        webhookLog.setWebhookPayload(webhookPayloadDto.getPayload());
        webhookLog.setStatusCode(200);  // assuming successful webhook
        webhookLog.setErrorMessage(""); // assuming no error
        webhookLog.setCreatedAt(LocalDateTime.now());
        webhookLog.setProcessedAt(LocalDateTime.now());
        return leanWebhookLogRepository.save(webhookLog);
    }
}
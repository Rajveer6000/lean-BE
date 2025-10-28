package com.lean.lean.service.webhook;

import com.lean.lean.dao.LeanWebhookLog;
import com.lean.lean.dto.WebhookPayloadDto;

public interface WebhookService  {
    LeanWebhookLog processWebhook(WebhookPayloadDto webhookPayloadDto);
}
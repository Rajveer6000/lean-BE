package com.lean.lean.service.webhook;

import com.lean.lean.dao.LeanWebhookLog;
import com.lean.lean.dto.WebHookRequestDto;

public interface WebhookService  {
    LeanWebhookLog processWebhook(WebHookRequestDto webhookPayloadDto);
}
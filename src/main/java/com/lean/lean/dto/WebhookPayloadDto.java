package com.lean.lean.dto;

import lombok.Data;

@Data
public class WebhookPayloadDto {
    private String eventName;
    private String payload;
}
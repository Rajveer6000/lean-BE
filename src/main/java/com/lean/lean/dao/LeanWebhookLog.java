package com.lean.lean.dao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
@Entity
@Data
@NoArgsConstructor
public class LeanWebhookLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventName;
    private String webhookPayload;
    private int statusCode;
    private String errorMessage;

    private Timestamp createdAt;
    private Timestamp processedAt;
}
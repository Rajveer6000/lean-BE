package com.lean.lean.dao;
import jakarta.persistence.*;
import lombok.Cleanup;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "lean_webhook_logs")
public class LeanWebhookLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "EventName")
    private String eventName;
    @Column(name = "WebhookPayload")
    private String webhookPayload;
    @Column(name = "StatusCode")
    private int statusCode;
    @Column(name = "ErrorMessage")
    private String errorMessage;
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    @Column(name = "ProcessedAt")
    private LocalDateTime processedAt;
}
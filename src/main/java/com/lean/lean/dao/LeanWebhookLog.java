package com.lean.lean.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "lean_webhook_logs")
public class LeanWebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type")
    private String type;

    @Column(name = "message")
    private String message;

    @Convert(converter = JsonAttributeConverter.class)
    @Column(name = "payload", columnDefinition = "JSON")
    private Object payload;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

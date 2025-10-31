package com.lean.lean.dao;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "lean_payment_sources")
public class LeanPaymentSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "payment_source_id", nullable = false, unique = true)
    private String paymentSourceId;

    @Column(name = "lean_user_id", nullable = false)
    private String leanUserId;

    @Column(name = "bank_identifier", nullable = false)
    private String bankIdentifier;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "status")
    private String status;

    @Column(name = "last_refreshed_at")
    private LocalDateTime lastRefreshedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
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

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "account_number_masked")
    private String accountNumberMasked;

    @Column(name = "iban", unique = true)
    private String iban;

    @Column(name = "currency", length = 3)
    private String currency = "AED";

    @Column(name = "balance")
    private Double balance;

    @Column(name = "status")
    private String status = "ACTIVE";

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "last_refreshed_at")
    private LocalDateTime lastRefreshedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
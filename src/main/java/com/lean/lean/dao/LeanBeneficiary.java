package com.lean.lean.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "lean_beneficiaries")
public class LeanBeneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "beneficiary_id", nullable = false, unique = true)
    private String beneficiaryId;

    @Column(name = "lean_user_id", nullable = false)
    private String leanUserId;

    @Column(name = "payment_source_id", nullable = false)
    private String paymentSourceId;

    @Column(name = "payment_destination_id", nullable = false)
    private String paymentDestinationId;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
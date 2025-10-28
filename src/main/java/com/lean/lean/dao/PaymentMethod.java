package com.lean.lean.dao;
import com.lean.lean.enums.PaymentMethodType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "payment_methods")
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "LeanUserId", referencedColumnName = "id", nullable = false)
    private LeanUser leanUser;
    @Column(name = "CardNumber")
    private String cardNumber;
    @Column(name = "CardHolderName")
    private String cardHolderName;
    @Column(name = "ExpirationDate")
    private String expirationDate;
    @Column(name = "Cvv")
    private String cvv;
    @Column(name = "PaymentMethodType")
    private PaymentMethodType paymentMethodType;
    @Column(name = "IsPrimary")
    private Boolean isPrimary;
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
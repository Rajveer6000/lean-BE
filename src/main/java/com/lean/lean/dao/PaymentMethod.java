package com.lean.lean.dao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
@Entity
@Data
@NoArgsConstructor
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lean_user_id", referencedColumnName = "id", nullable = false)
    private LeanUser leanUser;

    private String cardNumber;
    private String cardHolderName;
    private String expirationDate;
    private String cvv;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType paymentMethodType;

    private Boolean isPrimary;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    public enum PaymentMethodType {
        CREDITCARD, DEBITCARD, WALLET
    }
}
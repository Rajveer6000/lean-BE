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
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "leanuserid", referencedColumnName = "id", nullable = false)
    private LeanUser leanUser;
    @Column(name = "cardnumber")
    private String cardNumber;
    @Column(name = "cardholdername")
    private String cardHolderName;
    @Column(name = "expirationdate")
    private String expirationDate;
    @Column(name = "cvv")
    private String cvv;
    @Column(name = "paymentmethodtype")
    private PaymentMethodType paymentMethodType;
    @Column(name = "isprimary")
    private Boolean isPrimary;
    @Column(name = "createdat")
    private LocalDateTime createdAt;
    @Column(name = "updatedat")
    private LocalDateTime updatedAt;
}

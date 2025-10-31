package com.lean.lean.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lean_destinations")
public class LeanDestinations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "name")
    private String name;

    @Column(name = "bank_identifier")
    private String bankIdentifier;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "swift_code")
    private String swiftCode;

    @Column(name = "iban")
    private String iban;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "status")
    private Boolean status =true;
}

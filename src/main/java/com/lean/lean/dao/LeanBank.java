package com.lean.lean.dao;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lean_bank")
public class LeanBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "logo", columnDefinition = "json")
    private String logo;

    @Column(name = "main_color")
    private String mainColor;

    @Column(name = "background_color")
    private String backgroundColor;

    @Column(name = "active_payments")
    private Boolean isActivePayments;

    @Column(name = "active_data")
    private Boolean isActiveData;

    @Column(name = "enabled_payments")
    private Boolean isEnabledPayments;

    @Column(name = "enabled_data")
    private Boolean isEnabledData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
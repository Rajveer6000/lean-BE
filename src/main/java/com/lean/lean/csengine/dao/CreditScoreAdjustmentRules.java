package com.lean.lean.csengine.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "CreditScoreAdjustmentRules", indexes = @Index(name = "creditscoreadjustmentrules_engineconfigid_index", columnList = "EngineConfigID"))
public class CreditScoreAdjustmentRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "EngineConfigID", referencedColumnName = "Id", nullable = false)
    private EngineConfig engineConfig;

    @Column(name = "Key", nullable = false, length = 255)
    private String key;

    @Column(name = "VendorKey", nullable = false, length = 255)
    private String vendorKey;

    @Column(name = "DisplayName", nullable = false, length = 255)
    private String displayName;

    @Column(name = "Points", nullable = false, precision = 8, scale = 2)
    private BigDecimal points = BigDecimal.ZERO;

    @Column(name = "DisplayOrder")
    private Integer displayOrder;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CreatedBy", length = 255)
    private String createdBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "UpdatedBy", length = 255)
    private String updatedBy;
}

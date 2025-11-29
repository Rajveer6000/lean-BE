package com.lean.lean.csengine.dao;

import com.lean.lean.csengine.enums.EngineConfigStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "RiskTierConfig", uniqueConstraints = @UniqueConstraint(columnNames = { "EngineConfigId",
        "RiskTier" }), indexes = @Index(name = "risktierconfig_engineconfigid_index", columnList = "EngineConfigId"))
public class RiskTierConfig {

    @Id
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "EngineConfigId", referencedColumnName = "Id", nullable = false)
    private EngineConfig engineConfig;

    @Column(name = "RiskTier", nullable = false, length = 255)
    private String riskTier;

    @Column(name = "MinScore", nullable = false, precision = 5, scale = 2)
    private BigDecimal minScore;

    @Column(name = "MaxScore", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "RiskLevel", nullable = false, length = 50)
    private String riskLevel;

    @Column(name = "RentLimitPercentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal rentLimitPercentage;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "Status", nullable = false)
    private EngineConfigStatus status = EngineConfigStatus.ACTIVE;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CreatedBy", length = 255)
    private String createdBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "UpdatedBy", length = 255)
    private String updatedBy;
}

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
@Table(name = "EngineConfig")
public class EngineConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "MinScore", nullable = false, precision = 5, scale = 2)
    private BigDecimal minScore;

    @Column(name = "MaxScore", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore = BigDecimal.valueOf(10);

    @Column(name = "ScoreCap", nullable = false, precision = 5, scale = 2)
    private BigDecimal scoreCap = BigDecimal.valueOf(10);

    @Column(name = "TotalWeightage", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalWeightage;

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

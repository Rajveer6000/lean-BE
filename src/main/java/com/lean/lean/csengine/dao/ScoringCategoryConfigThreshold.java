package com.lean.lean.csengine.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "ScoringCategoryConfigThreshold", indexes = @Index(name = "scoringcategoryconfigthreshold_categoryconfigmasterid_index", columnList = "CategoryConfigMasterID"))
public class ScoringCategoryConfigThreshold {

    @Id
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CategoryConfigMasterID", referencedColumnName = "Id", nullable = false)
    private ScoringCategoryConfigMaster categoryConfigMaster;

    @Column(name = "MinScore", precision = 10, scale = 4)
    private BigDecimal minScore;

    @Column(name = "MaxScore", precision = 10, scale = 4)
    private BigDecimal maxScore;

    @Column(name = "ScoreValue", nullable = false, precision = 5, scale = 2)
    private BigDecimal scoreValue;

    @Column(name = "ScoreValueType", nullable = false, length = 255)
    private String scoreValueType;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CreatedBy", length = 255)
    private String createdBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "UpdatedBy", length = 255)
    private String updatedBy;
}

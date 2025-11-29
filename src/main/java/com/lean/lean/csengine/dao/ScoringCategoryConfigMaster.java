package com.lean.lean.csengine.dao;

import com.lean.lean.csengine.enums.EngineConfigStatus;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "ScoringCategoryConfigMaster")
public class ScoringCategoryConfigMaster {

    @Id
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "EngineConfigID", referencedColumnName = "Id", nullable = false)
    private EngineConfig engineConfig;

    @ManyToOne
    @JoinColumn(name = "MainCategoryID", referencedColumnName = "Id", nullable = false)
    private ScoringMainCategory mainCategory;

    @ManyToOne
    @JoinColumn(name = "CategoryTypeID", referencedColumnName = "Id", nullable = false)
    private ScoringCategoryType categoryType;

    @Column(name = "Weightage", nullable = false, precision = 8, scale = 2)
    private BigDecimal weightage;

    @Column(name = "MinScore", nullable = false, precision = 10, scale = 4)
    private BigDecimal minScore;

    @Column(name = "MaxScore", nullable = false, precision = 10, scale = 4)
    private BigDecimal maxScore;

    @Type(JsonBinaryType.class)
    @Column(name = "CalculationFormula", nullable = false, columnDefinition = "jsonb")
    private Object calculationFormula;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "Status", nullable = false)
    private EngineConfigStatus status = EngineConfigStatus.ACTIVE;

    @Column(name = "CreatedBy", nullable = false)
    private Long createdBy;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedBy", nullable = false)
    private Long updatedBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}

package com.lean.lean.csengine.dao;

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
@Table(name = "CategoryCalculations")
public class CategoryCalculations {

    @Id
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "UserCalculationMasterId", referencedColumnName = "Id", nullable = false)
    private CreditScoreUserCalculationMaster userCalculationMaster;

    @ManyToOne
    @JoinColumn(name = "MainCategoryId", referencedColumnName = "Id", nullable = false)
    private ScoringMainCategory mainCategory;

    @Type(JsonBinaryType.class)
    @Column(name = "InputValues", nullable = false, columnDefinition = "jsonb")
    private Object inputValues;

    @Column(name = "Score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CreatedBy", length = 255)
    private String createdBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "UpdatedBy", length = 255)
    private String updatedBy;
}

package com.lean.lean.csengine.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "ScoringMainCategory", uniqueConstraints = @UniqueConstraint(columnNames = { "EngineConfigID",
        "Name" }), indexes = @Index(name = "scoringmaincategory_engineconfigid_index", columnList = "EngineConfigID"))
public class ScoringMainCategory {

    @Id
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "EngineConfigID", referencedColumnName = "Id", nullable = false)
    private EngineConfig engineConfig;

    @Column(name = "Name", nullable = false, length = 255)
    private String name;

    @Column(name = "DisplayName", nullable = false, length = 255)
    private String displayName;

    @Column(name = "Weightage", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightage;

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

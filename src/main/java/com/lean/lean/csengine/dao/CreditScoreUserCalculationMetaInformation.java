package com.lean.lean.csengine.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "CreditScoreUserCalculationMetaInformation", indexes = {
        @Index(name = "creditscoreusercalculationmetainformation_userid_index", columnList = "UserId"),
        @Index(name = "creditscoreusercalculationmetainformation_usercalculationmasterid_index", columnList = "UserCalculationMasterId")
})
public class CreditScoreUserCalculationMetaInformation {

    @Id
    @Column(name = "Id")
    private Long id;

    @Column(name = "UserId", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "ConfigId", referencedColumnName = "Id", nullable = false)
    private EngineConfig config;

    @ManyToOne
    @JoinColumn(name = "UserCalculationMasterId", referencedColumnName = "Id", nullable = false)
    private CreditScoreUserCalculationMaster userCalculationMaster;

    @Column(name = "Source", nullable = false, length = 50)
    private String source;

    @Column(name = "KeyName", nullable = false, length = 255)
    private String keyName;

    @Column(name = "Value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CreatedBy")
    private Long createdBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "UpdatedBy", length = 255)
    private String updatedBy;
}

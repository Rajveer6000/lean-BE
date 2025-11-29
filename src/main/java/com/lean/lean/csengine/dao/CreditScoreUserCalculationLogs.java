package com.lean.lean.csengine.dao;

import com.lean.lean.csengine.enums.EngineConfigStatus;
import com.lean.lean.csengine.enums.LogLevel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "CreditScoreUserCalculationLogs")
public class CreditScoreUserCalculationLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "UserCalculationMasterId", referencedColumnName = "Id")
    private CreditScoreUserCalculationMaster userCalculationMaster;

    @Column(name = "UserId")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "ConfigId", referencedColumnName = "Id")
    private EngineConfig config;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "Status", nullable = false)
    private EngineConfigStatus status = EngineConfigStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "LogLevel", length = 20)
    private LogLevel logLevel;

    @Column(name = "LogMessage", nullable = false, columnDefinition = "TEXT")
    private String logMessage;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CreatedBy", length = 255)
    private String createdBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "UpdatedBy", length = 255)
    private String updatedBy;
}

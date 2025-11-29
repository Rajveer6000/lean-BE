package com.lean.lean.csengine.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "ScoringCategoryType")
public class ScoringCategoryType {

    @Id
    @Column(name = "Id")
    private Long id;

    @Column(name = "Name", nullable = false, length = 255)
    private String name;

    @Column(name = "DisplayName", nullable = false, length = 255)
    private String displayName;

    @Column(name = "CreatedBy", nullable = false)
    private Long createdBy;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedBy", nullable = false)
    private Long updatedBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}

package com.lean.lean.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "lean_user")
public class LeanUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userid", referencedColumnName = "id", nullable = false)
    private User user;
    @Column(name = "leanuserid")
    private String leanUserId;
    @Column(name = "leantoken")
    private String leanToken;
    @Column(name = "createdat")
    private LocalDateTime createdAt;
    @Column(name = "updatedat")
    private LocalDateTime updatedAt;
}
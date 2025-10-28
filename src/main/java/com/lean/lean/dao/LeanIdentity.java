package com.lean.lean.dao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
@Entity
@Data
@NoArgsConstructor
public class LeanIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lean_user_id", referencedColumnName = "id", nullable = false)
    private LeanUser leanUser;

    private String fullName;
    private String mobileNumber;

    private String gender;

    private String nationalIdentityNumber;
    private java.sql.Date birthDate;
    private String emailAddress;
    private String address;

    private Timestamp createdAt;
    private Timestamp updatedAt;
}
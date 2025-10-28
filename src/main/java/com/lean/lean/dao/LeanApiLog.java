package com.lean.lean.dao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
@Entity
@Data
@NoArgsConstructor
public class LeanApiLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String endpoint;
    private String requestBody;
    private String responseBody;
    private int statusCode;
    private String errorMessage;

    private Timestamp createdAt;
}
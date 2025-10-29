package com.lean.lean.dao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "lean_api_logs")
public class LeanApiLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Endpoint")
    private String endpoint;
    @Column(name = "RequestBody")
    private String requestBody;
    @Column(name = "ResponseBody")
    private String responseBody;
    @Column(name = "StatusCode")
    private int statusCode;
    @Column(name = "ErrorMessage")
    private String errorMessage;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}
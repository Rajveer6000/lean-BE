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
    @Column(name = "id")
    private Long id;

    @Column(name = "endpoint")
    private String endpoint;
    @Column(name = "requestbody")
    private String requestBody;
    @Column(name = "responsebody")
    private String responseBody;
    @Column(name = "statuscode")
    private int statusCode;
    @Column(name = "errormessage")
    private String errorMessage;

    @Column(name = "createdat")
    private LocalDateTime createdAt;
}

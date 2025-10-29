package com.lean.lean.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dao.LeanApiLog;
import com.lean.lean.repository.LeanApiLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeanApiLogService {

    private final LeanApiLogRepository repo;
    private final ObjectMapper om = new ObjectMapper();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(LeanApiLog log) {
        repo.save(log);
    }

    public String toJson(Object body) {
        if (body == null) return null;
        try { return om.writeValueAsString(body); }
        catch (Exception e) { return "<json-serialize-error>"; }
    }

    public String maskSecrets(String s) {
        if (s == null) return null;
        String masked = s
                .replaceAll("(?i)(client_secret=)([^&\\s]+)", "$1******")
                .replaceAll("(?i)(client_secret\"\\s*:\\s*\")([^\"]+)", "$1******")
                .replaceAll("(?i)(access_token\"\\s*:\\s*\")([^\"]+)", "$1******")
                .replaceAll("(?i)(authorization\"?\\s*:\\s*\"?Bearer\\s+)([A-Za-z0-9._-]+)", "$1******")
                .replaceAll("(?i)(refresh_token\"\\s*:\\s*\")([^\"]+)", "$1******");
        int max = 10000;
        if (masked.length() > max) masked = masked.substring(0, max) + "...<truncated>";
        return masked;
    }
}

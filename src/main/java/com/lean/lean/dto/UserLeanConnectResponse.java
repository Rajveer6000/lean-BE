package com.lean.lean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLeanConnectResponse {
    private String leanUserId;
    private String leanAccessToken;
}
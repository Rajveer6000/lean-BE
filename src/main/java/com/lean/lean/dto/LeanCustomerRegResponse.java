package com.lean.lean.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeanCustomerRegResponse {
    private String customer_id;
    private String app_user_id;
}

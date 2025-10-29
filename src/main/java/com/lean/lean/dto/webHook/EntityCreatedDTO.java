package com.lean.lean.dto.webHook;

import com.lean.lean.enums.Permission;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityCreatedDTO {
    private String id;
    private String customerId;
    private String appUserId;
    private List<Permission> permissions;
    private BankDetails bankDetails;
}

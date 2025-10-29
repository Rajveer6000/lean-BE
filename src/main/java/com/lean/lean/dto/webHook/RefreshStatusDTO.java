package com.lean.lean.dto.webHook;


import com.lean.lean.enums.OverallStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshStatusDTO {

    private String entityId;
    private String refreshId;
    private String customerId;
    private OverallStatus status;
    private DataStatus dataStatus;
    private String appUserId;
}
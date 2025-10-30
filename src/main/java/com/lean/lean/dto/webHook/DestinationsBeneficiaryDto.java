package com.lean.lean.dto.webHook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DestinationsBeneficiaryDto {
    private String id;
    private String display_name;
    private String name;
    private String bank_identifier;
    private String address;
    private String city;
    private String country;
    private String account_number;
    private String swift_code;
    private String iban;
}

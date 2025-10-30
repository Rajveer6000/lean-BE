package com.lean.lean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddDestinationsBeneficiaryDto {
    private String displayName;
    private String name;
    private String bankIdentifier;
    private String address;
    private String city;
    private String country;
    private String accountNumber;
    private String swiftCode;
    private String iban;
}

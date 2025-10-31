package com.lean.lean.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDestinationDTO {
    @JsonProperty("account_number")
    private String accountNumber;
    private String country;
    private String address;
    private String city;
    @JsonProperty("swift_code")
    private String swiftCode;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("sort_code")
    private String sortCode;
    @JsonProperty("routing_number")
    private String routingNumber;
    @JsonProperty("default")
    private Boolean isDefault;
    @JsonProperty("currency_iso_code")
    private String currencyIsoCode;
    @JsonProperty("bank_identifier")
    private String bankIdentifier;
    private String iban;
    private String name;
    @JsonProperty("branch_address")
    private String branchAddress;
    @JsonProperty("transit_code")
    private String transitCode;
    private String id;
    private String ifsc;
    @JsonProperty("postal_code")
    private String postalCode;
    @JsonProperty("owner_type")
    private String ownerType;
    private String status;
}
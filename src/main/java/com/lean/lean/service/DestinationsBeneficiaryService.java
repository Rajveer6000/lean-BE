package com.lean.lean.service;


import com.lean.lean.dao.DestinationsBeneficiary;
import com.lean.lean.dto.AddDestinationsBeneficiaryDto;
import com.lean.lean.dto.webHook.DestinationsBeneficiaryDto;
import com.lean.lean.repository.DestinationsBeneficiaryRepository;
import com.lean.lean.util.LeanApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DestinationsBeneficiaryService {

    @Autowired
    private DestinationsBeneficiaryRepository beneficiaryRepository;

    @Autowired
    private LeanApiUtil leanApiUtil;

    public DestinationsBeneficiary createDestinationsBeneficiary(AddDestinationsBeneficiaryDto beneficiary) {
        DestinationsBeneficiary existingBeneficiary = beneficiaryRepository.findByIdentifierAndAccountNumber(
                beneficiary.getBankIdentifier(),
                beneficiary.getAccountNumber()
        );

        if (existingBeneficiary != null) {
            throw new RuntimeException("Beneficiary with bank identifier " + existingBeneficiary.getBankIdentifier()
                    + " and account number " + existingBeneficiary.getAccountNumber()
                    + " already exists.");
        }

        String accessToken = leanApiUtil.getAccessToken();
        log.info("Access Token obtained: {}", accessToken);
        DestinationsBeneficiaryDto destinationsBeneficiarydto = leanApiUtil.createDestinationsBeneficiary(beneficiary,accessToken);
        DestinationsBeneficiary destinationsBeneficiary = new DestinationsBeneficiary();
        destinationsBeneficiary.setUuid(destinationsBeneficiarydto.getId());
        destinationsBeneficiary.setDisplayName(destinationsBeneficiarydto.getDisplay_name());
        destinationsBeneficiary.setName(destinationsBeneficiarydto.getName());
        destinationsBeneficiary.setBankIdentifier(destinationsBeneficiarydto.getBank_identifier());
        destinationsBeneficiary.setAddress(destinationsBeneficiarydto.getAddress());
        destinationsBeneficiary.setCity(destinationsBeneficiarydto.getCity());
        destinationsBeneficiary.setCountry(destinationsBeneficiarydto.getCountry());
        destinationsBeneficiary.setAccountNumber(destinationsBeneficiarydto.getAccount_number());
        destinationsBeneficiary.setSwiftCode(destinationsBeneficiarydto.getSwift_code());
        destinationsBeneficiary.setIban(destinationsBeneficiarydto.getIban());
        destinationsBeneficiary.setCreatedAt(LocalDateTime.now());
        destinationsBeneficiary.setUpdatedAt(LocalDateTime.now());
        destinationsBeneficiary.setCreatedBy("system");
        destinationsBeneficiary.setUpdatedBy("system");
        return beneficiaryRepository.save(destinationsBeneficiary);
    }

    public Optional<DestinationsBeneficiary> getDestinationsBeneficiary(Long id) {
        return beneficiaryRepository.findById(id);
    }

    public DestinationsBeneficiary updateDestinationsBeneficiary(Long id, DestinationsBeneficiary beneficiary) {
        if (!beneficiaryRepository.existsById(id)) {
            throw  new RuntimeException("Beneficiary with ID " + id + " not found.");
        }
        beneficiary.setId(id);
        return beneficiaryRepository.save(beneficiary);
    }

    public Object getAllBeneficiaries() {
        String accessToken = leanApiUtil.getAccessToken();
        log.info("accessToken: {}", accessToken);
        return leanApiUtil.getAllDestinationsBeneficiaries(accessToken);
    }
}
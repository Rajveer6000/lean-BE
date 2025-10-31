package com.lean.lean.controller;


import com.lean.lean.dao.DestinationsBeneficiary;
import com.lean.lean.dto.AddDestinationsBeneficiaryDto;
import com.lean.lean.service.DestinationsBeneficiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/beneficiaries")
public class DestinationsBeneficiaryController {

    @Autowired
    private DestinationsBeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<DestinationsBeneficiary> createBeneficiary(@RequestBody AddDestinationsBeneficiaryDto beneficiary) {
        DestinationsBeneficiary savedBeneficiary = beneficiaryService.createDestinationsBeneficiary(beneficiary);
        return new ResponseEntity<>(savedBeneficiary, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DestinationsBeneficiary> getBeneficiaryById(@PathVariable Long id) {
        Optional<DestinationsBeneficiary> beneficiaryOptional = beneficiaryService.getDestinationsBeneficiary(id);
        return beneficiaryOptional.map(destinationsBeneficiary -> new ResponseEntity<>(destinationsBeneficiary, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DestinationsBeneficiary> updateBeneficiary(@PathVariable Long id, @RequestBody DestinationsBeneficiary updatedBeneficiary) {
        DestinationsBeneficiary existingBeneficiaryOptional = beneficiaryService.updateDestinationsBeneficiary(id,updatedBeneficiary);
        return new ResponseEntity<>(existingBeneficiaryOptional, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Object> getAllBeneficiaries() {
        Object beneficiaries = beneficiaryService.getAllBeneficiaries();
        return new ResponseEntity<>(beneficiaries, HttpStatus.OK);
    }
}
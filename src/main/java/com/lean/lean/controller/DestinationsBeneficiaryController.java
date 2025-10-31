package com.lean.lean.controller;


import com.lean.lean.dao.LeanDestinations;
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
    public ResponseEntity<LeanDestinations> createBeneficiary(@RequestBody AddDestinationsBeneficiaryDto beneficiary) {
        LeanDestinations savedBeneficiary = beneficiaryService.createDestinationsBeneficiary(beneficiary);
        return new ResponseEntity<>(savedBeneficiary, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeanDestinations> getBeneficiaryById(@PathVariable Long id) {
        Optional<LeanDestinations> beneficiaryOptional = beneficiaryService.getDestinationsBeneficiary(id);
        return beneficiaryOptional.map(destinationsBeneficiary -> new ResponseEntity<>(destinationsBeneficiary, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeanDestinations> updateBeneficiary(@PathVariable Long id, @RequestBody LeanDestinations updatedBeneficiary) {
        LeanDestinations existingBeneficiaryOptional = beneficiaryService.updateDestinationsBeneficiary(id,updatedBeneficiary);
        return new ResponseEntity<>(existingBeneficiaryOptional, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Object> getAllBeneficiaries() {
        Object beneficiaries = beneficiaryService.getAllBeneficiaries();
        return new ResponseEntity<>(beneficiaries, HttpStatus.OK);
    }
}
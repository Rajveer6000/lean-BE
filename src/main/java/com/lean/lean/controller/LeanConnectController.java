package com.lean.lean.controller;

import com.lean.lean.dto.UserLeanConnectResponse;
import com.lean.lean.enums.PaymentIntentStatus;
import com.lean.lean.enums.ProofOfAddressDocumentType;
import com.lean.lean.service.UserLeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/lean")
public class LeanConnectController {

    @Autowired
    private UserLeanService userLeanService;

    @GetMapping("/connect")
    public UserLeanConnectResponse connectUser(
            @RequestParam Long userId) {
        return userLeanService.connectUser(userId);
    }

    @GetMapping("/UserDetails")
    public Object getLeanUserDetails(
            @RequestParam Long userId) {
        return userLeanService.getLeanUserDetails(userId);
    }

    @GetMapping("/userAccounts")
    public Object getUserAccounts(
            @RequestParam Long userId) {
        return userLeanService.getUserAccounts(userId);
    }

    @GetMapping("/accountBalances")
    public Object getAccountBalances(
            @RequestParam Long userId,
            @RequestParam String accountId) {
        return userLeanService.getAccountBalances(userId, accountId);
    }

    @PostMapping("/income")
    public Object getIncomeDetails(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false, defaultValue = "ALL") String incomeType) {
        return userLeanService.getIncomeDetails(userId, startDate, incomeType);
    }

    @GetMapping("/expenses")
    public Object getExpensesDetails(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return userLeanService.getExpensesDetails(userId, startDate);
    }

    @PostMapping("/name-verification")
    public Object getNameVerification(
            @RequestParam Long userId,
            @RequestParam String fullName) {
        return userLeanService.getNameVerification(userId, fullName);
    }

    @GetMapping("/account-transactions")
    public Object getUserTransactions(
            @RequestParam Long userId,
            @RequestParam String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return userLeanService.getUserTransactions(userId, accountId, fromDate, toDate);
    }

    @GetMapping("/get-payment-sources")
    public Object getuserPaymentSources(
            @RequestParam Long userId) {
        return userLeanService.getuserPaymentSources(userId);
    }

    @DeleteMapping("/payment-sources/{paymentSourceId}")
    public Object deletePaymentSource(
            @RequestParam Long userId,
            @PathVariable String paymentSourceId,
            @RequestParam(required = false) String reason) {
        return userLeanService.deletePaymentSource(userId, paymentSourceId, reason);
    }

    @GetMapping("/payments/{paymentId}")
    public Object getPaymentDetails(
            @RequestParam Long userId,
            @PathVariable String paymentId) {
        return userLeanService.getPaymentDetails(userId, paymentId);
    }

    @GetMapping("/payment-intents")
    public Object listPaymentIntents(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) PaymentIntentStatus status) {
        return userLeanService.listPaymentIntents(userId, page, size, from, to, status);
    }

    @GetMapping("/payment-intents/{paymentIntentId}")
    public Object getPaymentIntentDetails(
            @RequestParam Long userId,
            @PathVariable String paymentIntentId) {
        return userLeanService.getPaymentIntentDetails(userId, paymentIntentId);
    }

    @GetMapping("/customer-entities/{entityId}")
    public Object getCustomerEntityDetails(
            @RequestParam Long userId,
            @PathVariable String entityId) {
        return userLeanService.getCustomerEntityDetails(userId, entityId);
    }

    @DeleteMapping("/customer-entities/{entityId}")
    public Object deleteCustomerEntity(
            @RequestParam Long userId,
            @PathVariable String entityId,
            @RequestParam(required = false) String reason) {
        return userLeanService.deleteCustomerEntity(userId, entityId, reason);
    }

    @PostMapping(value = "/proof-of-address", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadProofOfAddress(
            @RequestParam Long userId,
            @RequestPart("document_type") ProofOfAddressDocumentType documentType,
            @RequestPart("full_name") String fullName,
            @RequestPart("reference_data") String referenceData,
            @RequestPart("file") MultipartFile file) {
        return userLeanService.uploadProofOfAddress(userId, documentType, fullName, referenceData, file);
    }
}

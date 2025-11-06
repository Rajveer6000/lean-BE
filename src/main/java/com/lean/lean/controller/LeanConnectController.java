package com.lean.lean.controller;

import com.lean.lean.dto.UserLeanConnectResponse;
import com.lean.lean.service.UserLeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

        return userLeanService.getUserTransactions(userId, accountId, fromDate, toDate);
    }

}
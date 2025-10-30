package com.lean.lean.controller;

import com.lean.lean.dto.UserLeanConnectResponse;
import com.lean.lean.service.UserLeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/lean")
public class LeanConnectController {

    @Autowired
    private UserLeanService userLeanService;

    @GetMapping("/connect")
    public UserLeanConnectResponse connectUser(@RequestParam Long userId) {
        return userLeanService.connectUser(userId);
    }

    @GetMapping("/UserDetails")
    public Object getLeanUserDetails(@RequestParam Long userId) {
        return userLeanService.getLeanUserDetails(userId);
    }

    @GetMapping("/userAccounts")
    public Object getUserAccounts(@RequestParam Long userId) {
        return userLeanService.getUserAccounts(userId);
    }

    @GetMapping("/accountBalances")
    public Object getAccountBalances(@RequestParam Long userId, @RequestParam String accountId) {
        return userLeanService.getAccountBalances(userId, accountId);
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
package com.lean.lean.controller;

import com.lean.lean.dto.UserLeanConnectResponse;
import com.lean.lean.service.UserLeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lean")
public class LeanConnectController {

    @Autowired
    private UserLeanService userLeanService;

    @GetMapping("/connect")
    public UserLeanConnectResponse connectUser(@RequestParam Long userId) {
        return userLeanService.connectUser(userId);
    }
}
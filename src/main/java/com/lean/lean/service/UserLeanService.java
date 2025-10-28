package com.lean.lean.service;

import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.UserLeanConnectResponse;
import com.lean.lean.repository.LeanUserRepository;
import com.lean.lean.repository.UserRepository;
import com.lean.lean.util.LeanApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserLeanService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeanUserRepository leanUserRepository;

    @Autowired
    private LeanApiUtil leanApiUtil;

    public UserLeanConnectResponse connectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + userId);
        }

        String accessToken = leanApiUtil.getAccessTokenForCustomer(leanUser.getLeanUserId());

        return new UserLeanConnectResponse(leanUser.getLeanUserId(), accessToken);
    }
}
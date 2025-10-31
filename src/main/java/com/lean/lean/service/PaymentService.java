package com.lean.lean.service;

import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.IntentDto;
import com.lean.lean.repository.LeanUserRepository;
import com.lean.lean.repository.UserRepository;
import com.lean.lean.util.LeanApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private LeanApiUtil leanApiUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeanUserRepository leanUserRepository;

    public Object createPaymentIntent(IntentDto intentDto) {
        log.info("Creating payment intent for amount: {}", intentDto);
        User user = userRepository.findById(intentDto.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(user.getId());
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        String accessToken = leanApiUtil.getAccessToken();
        return leanApiUtil.createPaymentIntent(intentDto,accessToken,leanUser.getLeanUserId());
    }

    public Object getPaymentSource(Long userId, String paymentSourceId) {
        log.info("Fetching payment source for userId: {}, paymentSourceId: {}", userId, paymentSourceId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LeanUser leanUser = leanUserRepository.findFirstByUserId(user.getId());
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        String accessToken = leanApiUtil.getAccessToken();
        return leanApiUtil.getPaymentSource(accessToken,leanUser.getLeanUserId(),paymentSourceId);
    }
}

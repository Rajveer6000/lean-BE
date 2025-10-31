package com.lean.lean.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dao.LeanPaymentIntent;
import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.IntentDto;
import com.lean.lean.dto.PaymentIntentDTO;
import com.lean.lean.dto.webHook.PaymentIntentCreatedDto;
import com.lean.lean.repository.LeanPaymentIntentRepository;
import com.lean.lean.repository.LeanUserRepository;
import com.lean.lean.repository.UserRepository;
import com.lean.lean.util.LeanApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private LeanApiUtil leanApiUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeanUserRepository leanUserRepository;

    @Autowired
    private LeanPaymentIntentRepository leanPaymentIntentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object createPaymentIntent(IntentDto intentDto) {
        log.info("Creating payment intent for amount: {}", intentDto);
        User user = userRepository.findById(intentDto.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(user.getId());
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        String accessToken = leanApiUtil.getAccessToken();
        Object intent = leanApiUtil.createPaymentIntent(intentDto,accessToken,leanUser.getLeanUserId());
        PaymentIntentDTO dto;
        try {
            if (intent instanceof String s) {
                dto = objectMapper.readValue(s, PaymentIntentDTO.class);
            } else {
                dto = objectMapper.readValue(objectMapper.writeValueAsString(intent), PaymentIntentDTO.class);
            }

            LeanPaymentIntent paymentIntent =
                    leanPaymentIntentRepository.findByPaymentIntentId(dto.getPaymentIntentId())
                            .orElseGet(LeanPaymentIntent::new);

            boolean isNew = (paymentIntent.getId() == null);
            paymentIntent.setPaymentIntentId(dto.getPaymentIntentId());
            paymentIntent.setLeanUserId(dto.getCustomerId());
            paymentIntent.setPaymentDestinationId(dto.getPaymentDestination().getId());
            paymentIntent.setBeneficiaryId(null);
            paymentIntent.setAmount(dto.getAmount());
            paymentIntent.setCurrency(dto.getCurrency());
            paymentIntent.setDescription(dto.getDescription());
            paymentIntent.setStatus(null);
            paymentIntent.setReferenceId(null);
            LocalDateTime now = LocalDateTime.now();
            paymentIntent.setUpdatedAt(now);
            if (isNew) {
                paymentIntent.setCreatedAt(now);
                paymentIntent.setInitiatedAt(now);
            }
            paymentIntent.setCompletedAt(null);
            paymentIntent.setFailedAt(null);
            paymentIntent.setExpiryAt(null);

            leanPaymentIntentRepository.save(paymentIntent);
            log.info("✅ LeanPaymentIntent [{}] saved successfully (new: {})",
                    dto.getPaymentIntentId(), isNew);
            return dto;
        } catch (Exception e) {
            log.error("Error parsing payment intent payload", e);
            throw new RuntimeException("Error parsing payment intent payload", e);
        }
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

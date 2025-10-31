package com.lean.lean.service.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dao.LeanBank;
import com.lean.lean.dao.LeanEntity;
import com.lean.lean.dao.LeanWebhookLog;
import com.lean.lean.dto.WebHookRequestDto;
import com.lean.lean.dto.webHook.BankAvailabilityDto;
import com.lean.lean.dto.webHook.BankDetails;
import com.lean.lean.dto.webHook.EntityCreatedDTO;
import com.lean.lean.enums.WebHookType;
import com.lean.lean.repository.LeanBankRepository;
import com.lean.lean.repository.LeanEntityRepository;
import com.lean.lean.repository.LeanWebhookLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class WebhookServiceImpl implements WebhookService {

    @Autowired
    private LeanWebhookLogRepository leanWebhookLogRepository;

    @Autowired
    private LeanBankRepository leanBankRepository;

    @Autowired
    private LeanEntityRepository leanEntityRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Transactional
    public LeanWebhookLog processWebhook(WebHookRequestDto webhookPayloadDto) {
        LeanWebhookLog logRow = new LeanWebhookLog();
        logRow.setTimestamp(webhookPayloadDto.getTimestamp());
        logRow.setPayload(webhookPayloadDto.getPayload());
        logRow.setType(webhookPayloadDto.getType());
        logRow.setEventId(webhookPayloadDto.getEvent_id());
        logRow.setMessage(webhookPayloadDto.getMessage());
        logRow.setCreatedAt(LocalDateTime.now());
        logRow.setUpdatedAt(LocalDateTime.now());
        logRow = leanWebhookLogRepository.save(logRow);

        try {
            WebHookType type = WebHookType.fromValue(webhookPayloadDto.getType());
            switch (type) {
                case ENTITY_CREATED -> {
                    handleEntityCreated(webhookPayloadDto.getPayload());
                }
                case BANK_AVAILABILITY_UPDATED -> {
                    handleBankAvailabilityUpdated(webhookPayloadDto.getPayload());
                }
                case PAYMENT_SOURCE_BENEFICIARY_CREATED ->{
                     handlePaymentSourceBeneficiaryCreated(webhookPayloadDto.getPayload());
                }
                case PAYMENT_SOURCE_CREATED -> {
                    handlePaymentSourceCreated(webhookPayloadDto.getPayload());
                }
            }
            log.info("Processing Complete: {}", logRow.getId());
        } catch (Exception e) {
            log.error("Error processing webhook {}: {}", logRow.getId(), e.getMessage());
        }
        return leanWebhookLogRepository.findById(logRow.getId()).orElse(logRow);
    }

    @Transactional
    public void handlePaymentSourceCreated(Object payload) {
        log.info("Handling payment_source.created with payload: {}", payload);
        // Implementation for handling payment_source.created webhook
        // Currently a placeholder as no specific instructions were provided
    }

    @Transactional
    public void handlePaymentSourceBeneficiaryCreated(Object payload) {
        log.info("Handling payment_source.beneficiary.created with payload: {}", payload);
        // Implementation for handling payment_source.beneficiary.created webhook
        // Currently a placeholder as no specific instructions were provided
    }

    @Transactional
    public void handleBankAvailabilityUpdated(Object payload) {
        log.info("Handling bank.availability.updated with payload: {}", payload);
        BankAvailabilityDto dto;
        try {
            if (payload instanceof String s) {
                dto = objectMapper.readValue(s, BankAvailabilityDto.class);
            } else {
                dto = objectMapper.readValue(objectMapper.writeValueAsString(payload), BankAvailabilityDto.class);
            }
            Optional<LeanBank> bank = leanBankRepository.findByIdentifier(dto.getIdentifier());
            if (bank.isEmpty()) {
                log.warn("Bank with identifier {} not found; skipping update.", dto.getIdentifier());
                return;
            }
            bank.get().setIsActiveData(dto.getAvailability().getActive().getData());
            bank.get().setIsActivePayments(dto.getAvailability().getActive().getPayments());
            bank.get().setIsEnabledData(dto.getAvailability().getEnabled().getData());
            bank.get().setIsEnabledPayments(dto.getAvailability().getEnabled().getPayments());
            bank.get().setUpdatedAt(LocalDateTime.now());
            leanBankRepository.save(bank.get());
            log.info("bank.availability.updated processed for bank.identifier={}", dto.getIdentifier());
        } catch (Exception e) {
            log.error("Failed to parse bank.availability.updated payload", e);
        }
    }

    @Transactional
    public void handleEntityCreated(Object payload) {
        log.info("Handling entity.created with payload: {}", payload);

        EntityCreatedDTO dto;
        try {
            if (payload instanceof String s) {
                dto = objectMapper.readValue(s, EntityCreatedDTO.class);
            } else {
                dto = objectMapper.readValue(objectMapper.writeValueAsString(payload), EntityCreatedDTO.class);
            }
        } catch (Exception e) {
            log.error("Failed to parse entity.created payload", e);
            return;
        }

        if (dto == null || dto.getId() == null || dto.getBankDetails() == null || dto.getBankDetails().getIdentifier() == null) {
            log.warn("Missing required fields (entity id / bank_details / bank identifier); skipping. dto={}", dto);
            return;
        }

        final LocalDateTime now = LocalDateTime.now();
        final BankDetails bd = dto.getBankDetails();

        LeanBank bank = leanBankRepository.findByIdentifier(bd.getIdentifier()).orElseGet(LeanBank::new);
        boolean newBank = (bank.getId() == null);
        bank.setIdentifier(bd.getIdentifier());
        bank.setName(bd.getName());
        bank.setLogo(bd.getLogo());
        bank.setMainColor(bd.getMainColor());
        bank.setBackgroundColor(bd.getBackgroundColor());
        if (newBank) bank.setCreatedAt(now);
        bank.setUpdatedAt(now);
        bank = leanBankRepository.save(bank);

        LeanEntity entity = leanEntityRepository.findByEntityId(dto.getId()).orElseGet(LeanEntity::new);
        boolean newEntity = (entity.getId() == null);
        entity.setEntityId(dto.getId());
        entity.setUserId(dto.getAppUserId());                 // app_user_id → user_id
        entity.setBankId(bank.getId());                 // storing bank.identifier as String (per your schema)
        try {
            entity.setPermissions(objectMapper.writeValueAsString(dto.getPermissions())); // JSON string
        } catch (Exception e) {
            entity.setPermissions(null);
        }
        if (newEntity) entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity = leanEntityRepository.save(entity);

        log.info("entity.created processed. bank.identifier={} (dbId={}), entity.entity_id={}",
                bank.getIdentifier(), bank.getId(), entity.getEntityId());
    }

}
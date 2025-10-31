package com.lean.lean.service.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dao.*;
import com.lean.lean.dto.WebHookRequestDto;
import com.lean.lean.dto.webHook.*;
import com.lean.lean.enums.WebHookType;
import com.lean.lean.repository.*;
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
    private LeanPaymentSourceRepository leanPaymentSourceRepository;

    @Autowired
    private LeanPaymentIntentRepository leanPaymentIntentRepository;

    @Autowired
    private LeanBeneficiaryRepository leanBeneficiaryRepository;

    @Autowired
    private LeanPaymentRepository leanPaymentRepository;

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
                case PAYMENT_SOURCE_CREATED -> {
                    handlePaymentSourceCreated(webhookPayloadDto.getPayload());
                }
                case PAYMENT_SOURCE_BENEFICIARY_CREATED ->{
                     handlePaymentSourceBeneficiaryCreated(webhookPayloadDto.getPayload());
                }
                case PAYMENT_SOURCE_BENEFICIARY_UPDATED -> {
                    handlePaymentSourceBeneficiaryUpdated(webhookPayloadDto.getPayload());
                }
                case PAYMENT_CREATED -> {
                    handlePaymentCreated(webhookPayloadDto.getPayload());
                }
                case PAYMENT_UPDATED -> {
                    handlePaymentUpdated(webhookPayloadDto.getPayload());
                }
                case PAYMENT_INTENT_CREATED -> {
                    handlePaymentIntentCreated(webhookPayloadDto.getPayload());
                }
            }
            log.info("Processing Complete: {}", logRow.getId());
        } catch (Exception e) {
            log.error("Error processing webhook {}: {}", logRow.getId(), e.getMessage());
        }
        try {
            String json = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(webhookPayloadDto);
            System.out.println("Received webhook payload:\n" + json);
        } catch (Exception e) {
            System.out.println("Failed to serialize webhook payload: " + e.getMessage());
        }
        return leanWebhookLogRepository.findById(logRow.getId()).orElse(logRow);
    }

    @Transactional
    public void handlePaymentIntentCreated(Object payload) {
        log.info("Handling payment_intent.created with payload: {}", payload);
        PaymentIntentCreatedDto dto;
        try {
            if (payload instanceof String s) {
                dto = objectMapper.readValue(s, PaymentIntentCreatedDto.class);
            } else {
                dto = objectMapper.readValue(objectMapper.writeValueAsString(payload), PaymentIntentCreatedDto.class);
            }
            LeanPaymentIntent paymentIntent = leanPaymentIntentRepository.findByPaymentIntentId(dto.getIntent_id()).orElseGet(LeanPaymentIntent::new);
            boolean isNew = (paymentIntent.getId() == null);
            paymentIntent.setPaymentIntentId(dto.getIntent_id());
            paymentIntent.setAmount(dto.getAmount());
            paymentIntent.setCurrency(dto.getCurrency());
            paymentIntent.setStatus(dto.getStatus());
            paymentIntent.setUpdatedAt(LocalDateTime.now());
            if (isNew) paymentIntent.setCreatedAt(LocalDateTime.now());
            if (isNew) paymentIntent.setInitiatedAt(LocalDateTime.now());
            leanPaymentIntentRepository.save(paymentIntent);
            log.info("payment_intent.created processed for payment_intent.id={}", dto.getIntent_id());
        } catch (Exception e) {
            log.error("Failed to parse payment_intent.created payload", e);
            return;
        }
    }

    @Transactional
    public void handlePaymentSourceCreated(Object payload) {
        log.info("Handling payment_source.created with payload: {}", payload);
        PaymentSourceCreated dto;
        try {
            if (payload instanceof String s) {
                dto = objectMapper.readValue(s, PaymentSourceCreated.class);
            } else {
                dto = objectMapper.readValue(objectMapper.writeValueAsString(payload), PaymentSourceCreated.class);
            }
            LeanPaymentSource paymentSource = leanPaymentSourceRepository.findByPaymentSourceId(dto.getId()).orElseGet(LeanPaymentSource::new);
            boolean isNew = (paymentSource.getId() == null);
            paymentSource.setPaymentSourceId(dto.getId());
            paymentSource.setLeanUserId(dto.getCustomer_id());
            paymentSource.setBankName(dto.getBank_name());
            paymentSource.setBankIdentifier(dto.getBank_identifier());
            paymentSource.setStatus(dto.getStatus());
            paymentSource.setUpdatedAt(LocalDateTime.now());
            paymentSource.setLastRefreshedAt(LocalDateTime.now());
            if (isNew) paymentSource.setCreatedAt(LocalDateTime.now());
            leanPaymentSourceRepository.save(paymentSource);
            log.info("payment_source.created processed for payment_source.id={}", dto.getId());
        } catch (Exception e) {
            log.error("Failed to parse payment_source.created payload", e);
            return;
        }
    }

    @Transactional
    public void handlePaymentSourceBeneficiaryCreated(Object payload) {
        log.info("Handling payment_source.beneficiary.created with payload: {}", payload);
        PaymentSourceBeneficiaryDto dto;
        try {
            if (payload instanceof String s) {
                dto = objectMapper.readValue(s, PaymentSourceBeneficiaryDto.class);
            } else {
                dto = objectMapper.readValue(objectMapper.writeValueAsString(payload), PaymentSourceBeneficiaryDto.class);
            }
            LeanBeneficiary beneficiary = leanBeneficiaryRepository.findByBeneficiaryId(dto.getId()).orElseGet(LeanBeneficiary::new);
            boolean isNew = (beneficiary.getId() == null);
            beneficiary.setBeneficiaryId(dto.getId());
            beneficiary.setLeanUserId(dto.getCustomer_id());
            beneficiary.setStatus(dto.getStatus());
            beneficiary.setPaymentSourceId(dto.getPayment_source_id());
            beneficiary.setPaymentDestinationId(dto.getPayment_destination_id());
            beneficiary.setPaymentSourceBankIdentifier(dto.getPayment_source_bank_identifier());
            beneficiary.setUpdatedAt(LocalDateTime.now());
            if (isNew) beneficiary.setCreatedAt(LocalDateTime.now());
            leanBeneficiaryRepository.save(beneficiary);
            log.info("payment_source.beneficiary.created processed for beneficiary.id={}", dto.getId());

        } catch (Exception e) {
            log.error("Failed to parse payment_source.beneficiary.created payload", e);
            return;
        }
    }

    @Transactional
    public void handlePaymentSourceBeneficiaryUpdated(Object payload) {
        log.info("Handling payment_source.beneficiary.updated with payload: {}", payload);
        PaymentSourceBeneficiaryDto dto;
        try {
            if (payload instanceof String s) {
                dto = objectMapper.readValue(s, PaymentSourceBeneficiaryDto.class);
            } else {
                dto = objectMapper.readValue(objectMapper.writeValueAsString(payload), PaymentSourceBeneficiaryDto.class);
            }
            LeanBeneficiary beneficiary = leanBeneficiaryRepository.findByBeneficiaryId(dto.getId()).orElseGet(LeanBeneficiary::new);
            boolean isNew = (beneficiary.getId() == null);
            beneficiary.setBeneficiaryId(dto.getId());
            beneficiary.setLeanUserId(dto.getCustomer_id());
            beneficiary.setStatus(dto.getStatus());
            beneficiary.setPaymentSourceId(dto.getPayment_source_id());
            beneficiary.setPaymentDestinationId(dto.getPayment_destination_id());
            beneficiary.setPaymentSourceBankIdentifier(dto.getPayment_source_bank_identifier());
            beneficiary.setUpdatedAt(LocalDateTime.now());
            if (isNew) beneficiary.setCreatedAt(LocalDateTime.now());
            leanBeneficiaryRepository.save(beneficiary);
            log.info("payment_source.beneficiary.updated processed for beneficiary.id={}", dto.getId());
        } catch (Exception e) {
            log.error("Failed to parse payment_source.beneficiary.created payload", e);
            return;
        }
    }

    @Transactional
    public void handlePaymentCreated(Object payload) {
        log.info("Handling payment.created with payload: {}", payload);
        PaymentCreatedDto dto;
        try {
            if (payload instanceof String s) {
                dto = objectMapper.readValue(s, PaymentCreatedDto.class);
            } else {
                dto = objectMapper.readValue(objectMapper.writeValueAsString(payload), PaymentCreatedDto.class);
            }
        } catch (Exception e) {
            log.error("Failed to parse payment.created payload", e);
            return;
        }
    }

    @Transactional
    public void handlePaymentUpdated(Object payload) {
        log.info("Handling payment.updated with payload: {}", payload);
        PaymentCreatedDto dto;
        try {
            if (payload instanceof String s) {
                dto = objectMapper.readValue(s, PaymentCreatedDto.class);
            } else {
                dto = objectMapper.readValue(objectMapper.writeValueAsString(payload), PaymentCreatedDto.class);
            }
        } catch (Exception e) {
            log.error("Failed to parse payment.created payload", e);
            return;
        }
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
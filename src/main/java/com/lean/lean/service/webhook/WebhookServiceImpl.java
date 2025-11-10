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

import java.math.BigDecimal;
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
        logRow.setPayload(objectMapper.valueToTree(webhookPayloadDto.getPayload()));
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
                case ENTITY_RECONNECTED -> {
                    handleEntityReconnected(webhookPayloadDto.getPayload());
                }
                case RESULTS_READY -> {
                    handleResultsReady(webhookPayloadDto.getPayload());
                }
                case PAYMENT_RECONCILIATION_UPDATED -> {
                    handlePaymentReconciliationUpdated(webhookPayloadDto.getPayload());
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
        try {
            PaymentIntentCreatedDto dto = convertPayload(payload, PaymentIntentCreatedDto.class);
            if (dto == null || dto.getIntent_id() == null) {
                log.warn("payment_intent.created payload missing intent_id. payload={}", payload);
                return;
            }
            LeanPaymentIntent paymentIntent = leanPaymentIntentRepository.findByPaymentIntentId(dto.getIntent_id())
                    .orElseGet(LeanPaymentIntent::new);
            boolean isNew = (paymentIntent.getId() == null);
            LocalDateTime now = LocalDateTime.now();
            paymentIntent.setPaymentIntentId(dto.getIntent_id());
            if (dto.getAmount() != null) {
                paymentIntent.setAmount(dto.getAmount());
            }
            if (dto.getCurrency() != null) {
                paymentIntent.setCurrency(dto.getCurrency());
            }
            if (dto.getCustomer_id() != null) {
                paymentIntent.setLeanUserId(dto.getCustomer_id());
            }
            if (dto.getPayment_destination_id() != null) {
                paymentIntent.setPaymentDestinationId(dto.getPayment_destination_id());
            }
            if (dto.getDescription() != null) {
                paymentIntent.setDescription(dto.getDescription());
            }
            updateIntentStatus(paymentIntent, dto.getStatus(), now);
            paymentIntent.setUpdatedAt(now);
            if (isNew && paymentIntent.getCreatedAt() == null) {
                paymentIntent.setCreatedAt(now);
            }
            if (isNew && paymentIntent.getInitiatedAt() == null) {
                paymentIntent.setInitiatedAt(now);
            }
            leanPaymentIntentRepository.save(paymentIntent);
            log.info("payment_intent.created processed for payment_intent.id={} (new={})", dto.getIntent_id(), isNew);
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
        try {
            PaymentCreatedDto dto = convertPayload(payload, PaymentCreatedDto.class);
            if (dto == null || dto.getId() == null) {
                log.warn("payment.created payload missing id. payload={}", payload);
                return;
            }
            LeanPayment payment = leanPaymentRepository.findByPaymentId(dto.getId()).orElseGet(LeanPayment::new);
            boolean isNew = (payment.getId() == null);
            if (isNew && !hasRequiredPaymentFields(dto.getIntent_id(), dto.getCustomer_id(), dto.getPayment_destination_id(),
                    dto.getStatus(), dto.getAmount())) {
                log.warn("payment.created payload missing mandatory fields for new payment. dto={}", dto);
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            payment.setPaymentId(dto.getId());
            if (dto.getIntent_id() != null) {
                payment.setPaymentIntentId(dto.getIntent_id());
            }
            if (dto.getCustomer_id() != null) {
                payment.setLeanUserId(dto.getCustomer_id());
            }
            if (dto.getPayment_destination_id() != null) {
                payment.setPaymentDestinationId(dto.getPayment_destination_id());
            }
            BigDecimal amount = dto.getAmount();
            if (amount != null) {
                payment.setAmount(amount);
            } else if (isNew && payment.getAmount() == null) {
                log.warn("payment.created payload missing amount for new payment. dto={}", dto);
                return;
            }
            if (dto.getCurrency() != null) {
                payment.setCurrency(dto.getCurrency());
            }
            if (dto.getBank_transaction_reference() != null) {
                payment.setBankReference(dto.getBank_transaction_reference());
            }
            updatePaymentStatus(payment, dto.getStatus(), now);
            if (hasText(dto.getStatus_additional_info())) {
                payment.setFailureReason(dto.getStatus_additional_info());
            }
            if (isNew && payment.getInitiatedAt() == null) {
                payment.setInitiatedAt(now);
            }
            if (isNew && payment.getCreatedAt() == null) {
                payment.setCreatedAt(now);
            }
            payment.setUpdatedAt(now);
            leanPaymentRepository.save(payment);
            updateIntentFromPayment(dto.getIntent_id(), dto.getStatus(), now);
            log.info("payment.created processed for payment.id={} (new={})", dto.getId(), isNew);
        } catch (Exception e) {
            log.error("Failed to parse payment.created payload", e);
            return;
        }
    }

    @Transactional
    public void handlePaymentUpdated(Object payload) {
        log.info("Handling payment.updated with payload: {}", payload);
        try {
            PaymentUpdatedDto dto = convertPayload(payload, PaymentUpdatedDto.class);
            if (dto == null || dto.getId() == null) {
                log.warn("payment.updated payload missing id. payload={}", payload);
                return;
            }
            LeanPayment payment = leanPaymentRepository.findByPaymentId(dto.getId()).orElseGet(LeanPayment::new);
            boolean isNew = (payment.getId() == null);
            String effectiveStatus = coalesceStatus(dto.getPost_initiation_status(), dto.getStatus());
            if (isNew && !hasRequiredPaymentFields(dto.getIntent_id(), dto.getCustomer_id(), dto.getPayment_destination_id(),
                    effectiveStatus, dto.getAmount())) {
                log.warn("payment.updated payload missing mandatory fields for new payment. dto={}", dto);
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            payment.setPaymentId(dto.getId());
            if (dto.getIntent_id() != null) {
                payment.setPaymentIntentId(dto.getIntent_id());
            }
            if (dto.getCustomer_id() != null) {
                payment.setLeanUserId(dto.getCustomer_id());
            }
            if (dto.getPayment_destination_id() != null) {
                payment.setPaymentDestinationId(dto.getPayment_destination_id());
            }
            BigDecimal amount = dto.getAmount();
            if (amount != null) {
                payment.setAmount(amount);
            } else if (isNew && payment.getAmount() == null) {
                log.warn("payment.updated payload missing amount for new payment. dto={}", dto);
                return;
            }
            if (dto.getCurrency() != null) {
                payment.setCurrency(dto.getCurrency());
            }
            if (dto.getBank_transaction_reference() != null) {
                payment.setBankReference(dto.getBank_transaction_reference());
            }
            updatePaymentStatus(payment, effectiveStatus, now);
            String failureReason = coalesceNonBlank(dto.getFailure_reason(), dto.getStatus_additional_info());
            if (hasText(failureReason)) {
                payment.setFailureReason(failureReason);
            }
            if (hasText(dto.getFailure_code())) {
                payment.setFailureCode(dto.getFailure_code());
            }
            if (isNew && payment.getInitiatedAt() == null) {
                payment.setInitiatedAt(now);
            }
            if (isNew && payment.getCreatedAt() == null) {
                payment.setCreatedAt(now);
            }
            payment.setUpdatedAt(now);
            leanPaymentRepository.save(payment);
            updateIntentFromPayment(dto.getIntent_id(), effectiveStatus, now);
            log.info("payment.updated processed for payment.id={} (new={})", dto.getId(), isNew);
        } catch (Exception e) {
            log.error("Failed to parse payment.updated payload", e);
            return;
        }
    }

    private void updateIntentFromPayment(String intentId, String status, LocalDateTime timestamp) {
        if (intentId == null || intentId.isBlank()) {
            return;
        }
        leanPaymentIntentRepository.findByPaymentIntentId(intentId)
                .ifPresent(intent -> {
                    updateIntentStatus(intent, status, timestamp);
                    intent.setUpdatedAt(timestamp);
                    leanPaymentIntentRepository.save(intent);
                });
    }

    private boolean hasRequiredPaymentFields(String intentId,
                                             String customerId,
                                             String destinationId,
                                             String status,
                                             BigDecimal amount) {
        return intentId != null && !intentId.isBlank()
                && customerId != null && !customerId.isBlank()
                && destinationId != null && !destinationId.isBlank()
                && status != null && !status.isBlank()
                && amount != null;
    }

    private void updatePaymentStatus(LeanPayment payment, String status, LocalDateTime now) {
        if (!hasText(status)) {
            return;
        }
        payment.setStatus(status);
    }

    private void updateIntentStatus(LeanPaymentIntent intent, String status, LocalDateTime now) {
        if (!hasText(status)) {
            return;
        }
        intent.setStatus(status);
    }

    private String coalesceStatus(String... statuses) {
        if (statuses == null) {
            return null;
        }
        for (String status : statuses) {
            if (hasText(status)) {
                return status;
            }
        }
        return null;
    }

    private String coalesceNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private <T> T convertPayload(Object payload, Class<T> targetType) throws Exception {
        if (payload == null) {
            return null;
        }
        if (targetType.isInstance(payload)) {
            return targetType.cast(payload);
        }
        if (payload instanceof String s) {
            return objectMapper.readValue(s, targetType);
        }
        return objectMapper.readValue(objectMapper.writeValueAsString(payload), targetType);
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
        processEntityLifecycleEvent(payload, "entity.created");
    }

    @Transactional
    public void handleEntityReconnected(Object payload) {
        processEntityLifecycleEvent(payload, "entity.reconnected");
    }

    @Transactional
    public void handleResultsReady(Object payload) {
        log.info("Handling results.ready with payload: {}", payload);
    }

    @Transactional
    public void handlePaymentReconciliationUpdated(Object payload) {
        log.info("Handling payment.reconciliation.updated with payload: {}", payload);
    }

    private void processEntityLifecycleEvent(Object payload, String eventName) {
        log.info("Handling {} with payload: {}", eventName, payload);

        EntityCreatedDTO dto;
        try {
            dto = convertPayload(payload, EntityCreatedDTO.class);
        } catch (Exception e) {
            log.error("Failed to parse {} payload", eventName, e);
            return;
        }

        if (dto == null || dto.getId() == null || dto.getBankDetails() == null || dto.getBankDetails().getIdentifier() == null) {
            log.warn("{} missing required fields (entity id / bank_details / bank identifier); skipping. dto={}", eventName, dto);
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
        if (newBank) {
            bank.setCreatedAt(now);
        }
        bank.setUpdatedAt(now);
        bank = leanBankRepository.save(bank);

        LeanEntity entity = leanEntityRepository.findByEntityId(dto.getId()).orElseGet(LeanEntity::new);
        boolean newEntity = (entity.getId() == null);
        entity.setEntityId(dto.getId());
        entity.setUserId(dto.getAppUserId());
        entity.setBankId(bank.getId());
        try {
            entity.setPermissions(objectMapper.valueToTree(dto.getPermissions()));
        } catch (Exception e) {
            entity.setPermissions(null);
        }
        if (newEntity) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);
        entity = leanEntityRepository.save(entity);

        log.info("{} processed. bank.identifier={} (dbId={}), entity.entity_id={}",
                eventName, bank.getIdentifier(), bank.getId(), entity.getEntityId());
    }

}

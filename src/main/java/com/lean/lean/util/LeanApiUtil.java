package com.lean.lean.util;


import com.lean.lean.dao.LeanApiLog;
import com.lean.lean.dao.User;
import com.lean.lean.dto.AddDestinationsBeneficiaryDto;
import com.lean.lean.dto.IntentDto;
import com.lean.lean.dto.LeanCustomerRegResponse;
import com.lean.lean.dto.webHook.DestinationsBeneficiaryDto;
import com.lean.lean.enums.PaymentIntentStatus;
import com.lean.lean.enums.ProofOfAddressDocumentType;
import com.lean.lean.service.LeanApiLogService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LeanApiUtil {
    @Autowired
    private  RestTemplate restTemplate;

    @Autowired
    private LeanApiLogService logService;

    @Value("${lean.auth.api-url}")
    private String authApiUrl;

    @Value("${lean.api-url}")
    private String apiUrl;

    @Value("${lean.client-secret-key}")
    private String clientSecret;

    @Value("${lean.client-id}")
    private String clientId;


    public String getAccessToken() {
        String url = authApiUrl + "/oauth2/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=client_credentials" +
                "&scope=api";

        ResponseEntity<String> resp = exchangeWithLog(url, HttpMethod.POST, headers, body, String.class);
        JSONObject responseBody = new JSONObject(resp.getBody());
        return responseBody.getString("access_token");
    }

    public LeanCustomerRegResponse createCustomerOnLean(User user, String accessToken) {
        String url = apiUrl + "/customers/v1";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = new JSONObject()
                .put("app_user_id", user.getId())
                .toString();

        ResponseEntity<LeanCustomerRegResponse> resp =
                exchangeWithLog(url, HttpMethod.POST, headers, body, LeanCustomerRegResponse.class);

        return resp.getBody();
    }

    public String getAccessTokenForCustomer(String customerId) {
        String url = authApiUrl + "/oauth2/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String scope = "customer." + customerId;
        String body = "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=client_credentials" +
                "&scope=" + scope;

        ResponseEntity<String> resp = exchangeWithLog(url, HttpMethod.POST, headers, body, String.class);
        JSONObject responseBody = new JSONObject(resp.getBody());
        return responseBody.getString("access_token");
    }

    public Object getLeanUserDetails(String entityId, String accessToken) {
        String url = apiUrl + "/data/v1/identity";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "*/*");
        headers.set("Content-Type", "application/json");

        String body = new JSONObject()
                .put("entity_id", entityId)
                .toString();
        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.POST, headers, body, String.class);
        JSONObject responseBody = new JSONObject(resp.getBody());
        return responseBody.toMap();
    }

    public Object getUserAccounts(String entityId, String accessToken) {
        String url = apiUrl + "/data/v1/accounts";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "*/*");
        headers.set("Content-Type", "application/json");

        String body = new JSONObject()
                .put("entity_id", entityId)
                .toString();
        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.POST, headers, body, String.class);
        JSONObject responseBody = new JSONObject(resp.getBody());
        return responseBody.toMap();
    }

    public Object getAccountBalances(String entityId, String accountId, String accessToken) {
        String url = apiUrl + "/data/v1/balance";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "*/*");
        headers.set("Content-Type", "application/json");

        String body = new JSONObject()
                .put("entity_id", entityId)
                .put("account_id", accountId)
                .toString();
        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.POST, headers, body, String.class);
        JSONObject responseBody = new JSONObject(resp.getBody());
        return responseBody.toMap();
    }

    public Object getAllDestinationsBeneficiaries(String accessToken) {
        String url = apiUrl + "/payments/v1/destinations";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "*/*");
        headers.set("Content-Type", "application/json");
        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.GET, headers, null, String.class);
        JSONArray responseArray = new JSONArray(resp.getBody());

        List<Map<String, Object>> destinations = new ArrayList<>();
        for (int i = 0; i < responseArray.length(); i++) {
            destinations.add(responseArray.getJSONObject(i).toMap());
        }
        return destinations;
    }

    public Object getUserTransactions(String entityId, String accountId, LocalDate fromDate, LocalDate toDate, String accessToken) {
        String url = apiUrl + "/data/v1/transactions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "*/*");
        headers.set("Content-Type", "application/json");

        JSONObject body = new JSONObject()
                .put("entity_id", entityId)
                .put("account_id", accountId)
                .put("insights", true);

        if (fromDate != null) {
            body.put("from_date", fromDate.toString());
        }
        if (toDate != null) {
            body.put("to_date", toDate.toString());
        }

        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.POST, headers, body.toString(), String.class);
        JSONObject responseBody = new JSONObject(resp.getBody());
        return responseBody.toMap();
    }

    public Object getIncomeInsights(String entityId,
                                    LocalDate startDate,
                                    String incomeType,
                                    String accessToken) {
        String url = apiUrl + "/insights/v2/income";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        JSONObject body = new JSONObject()
                .put("entity_id", entityId)
                .put("income_type", incomeType == null || incomeType.isBlank() ? "ALL" : incomeType)
                .put("async", false);
        if (startDate != null) {
            body.put("start_date", startDate.toString());
        }

        ResponseEntity<String> response =
                exchangeWithLog(url, HttpMethod.POST, headers, body.toString(), String.class);
        return parseJsonResponse(response.getBody());
    }

    public Object getExpensesInsights(String entityId,
                                      LocalDate startDate,
                                      String accessToken) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(apiUrl + "/insights/v2/expenses")
                .queryParam("entity_id", entityId)
                .queryParam("async", false);
        if (startDate != null) {
            builder.queryParam("start_date", startDate.toString());
        }
        String url = builder.build(true).toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response =
                exchangeWithLog(url, HttpMethod.GET, headers, null, String.class);
        return parseJsonResponse(response.getBody());
    }

    public Object verifyName(String entityId,
                             String fullName,
                             String accessToken) {
        String url = apiUrl + "/insights/v1/name-verification";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        JSONObject body = new JSONObject()
                .put("entity_id", entityId)
                .put("full_name", fullName)
                .put("async", false);

        ResponseEntity<String> response =
                exchangeWithLog(url, HttpMethod.POST, headers, body.toString(), String.class);
        return parseJsonResponse(response.getBody());
    }

    public DestinationsBeneficiaryDto createDestinationsBeneficiary(AddDestinationsBeneficiaryDto beneficiary, String accessToken) {
        String url = apiUrl + "/payments/v1/destinations";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        String body = new JSONObject()
                .put("display_name", beneficiary.getDisplayName())
                .put("name", beneficiary.getName())
                .put("bank_identifier", beneficiary.getBankIdentifier())
                .put("address", beneficiary.getAddress())
                .put("city", beneficiary.getCity())
                .put("country", beneficiary.getCountry())
                .put("account_number", beneficiary.getAccountNumber())
                .put("swift_code", beneficiary.getSwiftCode())
                .put("iban", beneficiary.getIban())
                .toString();

        ResponseEntity<DestinationsBeneficiaryDto> resp =
                exchangeWithLog(url, HttpMethod.POST, headers, body, DestinationsBeneficiaryDto.class);

        return resp.getBody();
    }

    public Object createPaymentIntent(IntentDto intentDto , String accessToken, String customerId) {
        String url = apiUrl + "/payments/v1/intents";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        String body = new JSONObject()
                .put("amount", intentDto.getAmount())
                .put("currency", intentDto.getCurrency())
                .put("payment_destination_id", intentDto.getPayment_destination_id())
                .put("customer_id", customerId)
                .put("description", intentDto.getDescription())
                .toString();

        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.POST, headers, body, String.class);
        JSONObject responseBody = new JSONObject(resp.getBody());
        return responseBody.toMap();
    }
    public Object getPaymentSources(String leanUserID, String accessToken) {
        String url = apiUrl + "/customers/v1/" + leanUserID + "/payment-sources";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "*/*");
        headers.set("Content-Type", "application/json");
        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.GET, headers, null, String.class);
        JSONArray responseArray = new JSONArray(resp.getBody());
        List<Map<String, Object>> paymentSources = new ArrayList<>();
        for (int i = 0; i < responseArray.length(); i++) {
            paymentSources.add(responseArray.getJSONObject(i).toMap());
        }
        return paymentSources;
    }

    public Object getPaymentSource(String accessToken, String leanUserId, String paymentSourceId) {
        String url = apiUrl + "/customers/v1/" + leanUserId + "/payment-sources/" + paymentSourceId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "*/*");
        headers.set("Content-Type", "application/json");
        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.GET, headers, null, String.class);
        JSONObject responseBody = new JSONObject(resp.getBody());
        return responseBody.toMap();
    }

    public Object deletePaymentSource(String leanUserId,
                                      String paymentSourceId,
                                      String reason,
                                      String accessToken) {
        String url = apiUrl + "/customers/v1/" + leanUserId + "/payment-sources/" + paymentSourceId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject().put("reason", reason);
        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.DELETE, headers, body.toString(), String.class);
        return parseJsonResponse(resp.getBody());
    }

    public Object getPaymentById(String paymentId, String accessToken) {
        String url = apiUrl + "/payments/v1/" + paymentId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.GET, headers, null, String.class);
        return parseJsonResponse(resp.getBody());
    }

    public Object listPaymentIntents(String accessToken,
                                     String customerId,
                                     Integer page,
                                     Integer size,
                                     LocalDate from,
                                     LocalDate to,
                                     PaymentIntentStatus status) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(apiUrl + "/payments/v1/intents");
        if (page != null) {
            builder.queryParam("page", page);
        }
        if (size != null) {
            builder.queryParam("size", size);
        }
        if (customerId != null) {
            builder.queryParam("customer_id", customerId);
        }
        if (from != null) {
            builder.queryParam("from", toStartOfDayInstant(from));
        }
        if (to != null) {
            builder.queryParam("to", toEndOfDayInstant(to));
        }
        if (status != null) {
            builder.queryParam("status", status.getValue());
        }

        String url = builder.build(true).toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.GET, headers, null, String.class);
        return parseJsonResponse(resp.getBody());
    }

    public Object getPaymentIntentById(String paymentIntentId, String accessToken) {
        String url = apiUrl + "/payments/v1/intents/" + paymentIntentId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.GET, headers, null, String.class);
        return parseJsonResponse(resp.getBody());
    }

    public Object getCustomerEntity(String accessToken, String customerId, String entityId) {
        String url = apiUrl + "/customers/v1/" + customerId + "/entities/" + entityId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.GET, headers, null, String.class);
        return parseJsonResponse(resp.getBody());
    }

    public Object deleteCustomerEntity(String accessToken,
                                       String customerId,
                                       String entityId,
                                       String reason) {
        String url = apiUrl + "/customers/v1/" + customerId + "/entities/" + entityId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject().put("reason", reason);
        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.DELETE, headers, body.toString(), String.class);
        return parseJsonResponse(resp.getBody());
    }

    public Object uploadProofOfAddress(String accessToken,
                                       String customerId,
                                       ProofOfAddressDocumentType documentType,
                                       String fullName,
                                       Map<String, Object> referenceData,
                                       MultipartFile file) {
        String url = apiUrl + "/kyc/v1/customers/" + customerId + "/proof-of-address";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("document_type", documentType.name());
        formData.add("full_name", fullName);

        HttpHeaders referenceHeaders = new HttpHeaders();
        referenceHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject referencePayload = new JSONObject(referenceData != null ? referenceData : Map.of());
        formData.add("reference_data", new HttpEntity<>(referencePayload.toString(), referenceHeaders));

        try {
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "proof-of-address";
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
            HttpHeaders fileHeaders = new HttpHeaders();
            MediaType mediaType = file.getContentType() != null
                    ? MediaType.parseMediaType(file.getContentType())
                    : MediaType.APPLICATION_OCTET_STREAM;
            fileHeaders.setContentType(mediaType);
            formData.add("file", new HttpEntity<>(resource, fileHeaders));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read proof-of-address file", e);
        }

        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.POST, headers, formData, String.class);
        return parseJsonResponse(resp.getBody());
    }
    private Object parseJsonResponse(String responseBody) {
        if (responseBody == null) {
            return Map.of();
        }
        String trimmed = responseBody.trim();
        if (trimmed.isEmpty()) {
            return Map.of();
        }
        if (trimmed.startsWith("{")) {
            return new JSONObject(trimmed).toMap();
        }
        if (trimmed.startsWith("[")) {
            return new JSONArray(trimmed).toList();
        }
        return trimmed;
    }

    // ---------- Centralized logging wrapper ----------

    private <T> ResponseEntity<T> exchangeWithLog(
            String url,
            HttpMethod method,
            HttpHeaders headers,
            Object requestBody,
            Class<T> responseType
    ) {
        String maskedReq = logService.maskSecrets(stringifyRequestBody(requestBody));
        LeanApiLog.LeanApiLogBuilder logBuilder = LeanApiLog.builder()
                .endpoint(url)
                .requestBody(maskedReq);
        log.info("Making request to Lean API: {} {}", method, url);
        log.info("Request Body: {}", maskedReq);
        log.info("Headers: {}", logService.maskSecrets(headers.toString()));
        log.info("Expected Response Type: {}", responseType.getSimpleName());
        try {
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

            String respString = null;
            if (response.getBody() instanceof String) {
                respString = (String) response.getBody();
            } else {
                respString = logService.toJson(response.getBody());
            }
            respString = logService.maskSecrets(respString);

            logBuilder
                    .statusCode(response.getStatusCodeValue())
                    .responseBody(respString)
                    .errorMessage(null);

            logService.save(logBuilder.build());
            return response;

        } catch (HttpStatusCodeException e) {
            String respBody = logService.maskSecrets(e.getResponseBodyAsString());
            logBuilder
                    .statusCode(e.getRawStatusCode())
                    .responseBody(respBody)
                    .errorMessage(e.getMessage());
            logService.save(logBuilder.build());
            throw e; // keep behavior same as before

        } catch (RestClientException e) {
            logBuilder
                    .statusCode(0) // network/serialization/etc.
                    .responseBody(null)
                    .errorMessage(e.getMessage());
            logService.save(logBuilder.build());
            throw e;
        } catch (Exception e) {
            logBuilder
                    .statusCode(0)
                    .responseBody(null)
                    .errorMessage("Unexpected: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            logService.save(logBuilder.build());
            throw e;
        }
    }

    private String stringifyRequestBody(Object requestBody) {
        if (requestBody == null) {
            return null;
        }
        if (requestBody instanceof String s) {
            return s;
        }
        if (requestBody instanceof MultiValueMap<?, ?> multiValueMap) {
            return multiValueMap.toString();
        }
        return logService.toJson(requestBody);
    }

    private String toStartOfDayInstant(LocalDate date) {
        Instant instant = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        return instant.toString();
    }

    private String toEndOfDayInstant(LocalDate date) {
        Instant instant = date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toInstant();
        return instant.toString();
    }



}

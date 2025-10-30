package com.lean.lean.util;


import com.lean.lean.dao.LeanApiLog;
import com.lean.lean.dao.User;
import com.lean.lean.dto.AddDestinationsBeneficiaryDto;
import com.lean.lean.dto.LeanCustomerRegResponse;
import com.lean.lean.dto.webHook.DestinationsBeneficiaryDto;
import com.lean.lean.service.LeanApiLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;

import java.time.LocalDate;

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

    public Object getUserTransactions(String entityId, String accountId, LocalDate fromDate, LocalDate toDate, String accessToken) {
        String url = apiUrl + "/v1/transactions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "*/*");
        headers.set("Content-Type", "application/json");
        String body = new JSONObject()
                .put("entity_id", entityId)
                .put("account_id", accountId)
                .put("from_date", fromDate.toString())
                .put("to_date", toDate.toString())
                .put("insights", true)
                .toString();
        ResponseEntity<String> resp =
                exchangeWithLog(url, HttpMethod.POST, headers, body, String.class);
        JSONObject responseBody = new JSONObject(resp.getBody());
        return responseBody.toMap();
    }
//public Object getUserTransactions(String entityId, String accountId, LocalDate fromDate, LocalDate toDate, String accessToken) {
//    String url = apiUrl + "/data/v1/transactions";
//    HttpHeaders headers = new HttpHeaders();
//    headers.set("Authorization", "Bearer " + accessToken);
//    headers.set("Accept", "*/*");
//    headers.set("Content-Type", "application/json");
//
//    JSONObject body = new JSONObject()
//            .put("entity_id", entityId)
//            .put("account_id", accountId)
//            .put("insights", true);
//
//    if (fromDate != null) {
//        body.put("from_date", fromDate.toString());
//    }
//    if (toDate != null) {
//        body.put("to_date", toDate.toString());
//    }
//
//    ResponseEntity<String> resp =
//            exchangeWithLog(url, HttpMethod.POST, headers, body.toString(), String.class);
//    JSONObject responseBody = new JSONObject(resp.getBody());
//    return responseBody.toMap();
//}
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
    // ---------- Centralized logging wrapper ----------

    private <T> ResponseEntity<T> exchangeWithLog(
            String url,
            HttpMethod method,
            HttpHeaders headers,
            String requestBody,
            Class<T> responseType
    ) {
        String maskedReq = logService.maskSecrets(requestBody);
        LeanApiLog.LeanApiLogBuilder logBuilder = LeanApiLog.builder()
                .endpoint(url)
                .requestBody(maskedReq);
        log.info("Making request to Lean API: {} {}", method, url);
        log.info("Request Body: {}", maskedReq);
        log.info("Headers: {}", logService.maskSecrets(headers.toString()));
        log.info("Expected Response Type: {}", responseType.getSimpleName());
        try {
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
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



}
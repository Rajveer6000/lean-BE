package com.lean.lean.util;


import com.lean.lean.dao.User;
import com.lean.lean.dto.LeanCustomerRegResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;

@Component
public class LeanApiUtil {

    @Value("${lean.auth.api-url}")
    private String authApiUrl;

    @Value("${lean.api-url}")
    private String apiUrl;

    @Value("${lean.client-secret-key}")
    private String clientSecret;

    @Value("${lean.client-id}")
    private String clientId;


    private final RestTemplate restTemplate = new RestTemplate();

    public String getAccessToken() {
        String url = authApiUrl + "/oauth2/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=client_credentials" +
                "&scope=api";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        JSONObject responseBody = new JSONObject(response.getBody());
        return responseBody.getString("access_token");
    }

    // Method to create a customer on Lean platform
    public LeanCustomerRegResponse createCustomerOnLean(User user, String accessToken) {
        String url = "https://sandbox.leantech.me/customers/v1";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the request body
        String body = new JSONObject()
                .put("app_user_id", user.getId())
                .toString();

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<LeanCustomerRegResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, entity, LeanCustomerRegResponse.class);
        return responseEntity.getBody();
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

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        JSONObject responseBody = new JSONObject(response.getBody());
        return responseBody.getString("access_token");
    }

}
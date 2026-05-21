package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/public/auth/gyalsung")
public class GyalsungApiController {

    @Value("${gyalsung-token-api}")
    private String gyalsungTokenUrl;

    @Value("${gyalsung-consumer-key}")
    private String consumerKey;

    @Value("${gyalsung-consumer-secret}")
    private String consumerSecret;

    @Value("${gyalsung-data-api}")
    private String gyalsungDataUrl;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/gyalsungDetails/{citizenshipNo}")
    public ResponseEntity<?> getGyalsungDetails(@PathVariable String citizenshipNo) {

        try {
            // 1. Generate token
            String accessToken = generateNewToken();

            // 2. Prepare headers for GET request
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // 3. Correct API format (QUERY PARAM, not path variable)
            String url = gyalsungDataUrl + "?cid=" + citizenshipNo;

            // 4. Call external API
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Object.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body(ex.getMessage());
        }
    }

    private String generateNewToken() {

        // Request body as per curl
        Map<String, String> requestBody = Map.of(
                "client_id", consumerKey,
                "client_secret", consumerSecret
        );

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(requestBody, headers);

        // Call token API
        ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(
                        gyalsungTokenUrl,
                        HttpMethod.POST,
                        request,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );

        // Extract token
        return (String) response.getBody().get("access_token");
    }
}
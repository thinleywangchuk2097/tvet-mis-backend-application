package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/public/gyalsung")
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
            //    .../getGyalsungDetails?cid=XXXXXXXXXXX  (no trailing "/" before "?")
            String base = gyalsungDataUrl.trim().replaceAll("/+$", "");
            String url = UriComponentsBuilder.fromUriString(base)
                    .queryParam("cid", citizenshipNo)
                    .toUriString();

            // 4. Call external API
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Object.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (HttpStatusCodeException e) {
            log.error("Gyalsung lookup failed for {}: {} {}", citizenshipNo, e.getStatusCode(),
                    e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", "Failed to retrieve Gyalsung details"));
        } catch (Exception e) {
            log.error("Gyalsung lookup failed for {}", citizenshipNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to retrieve Gyalsung details"));
        }
    }

	private String generateNewToken() {

		// Request body as per curl
		Map<String, String> requestBody = Map.of("client_id", consumerKey, "client_secret", consumerSecret);

		// Headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

		// Call token API
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(gyalsungTokenUrl.trim(), HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		// Extract token
		return (String) response.getBody().get("access_token");
	}
}
package com.moesd.tvet.mis.backend.application.controller;

import java.text.ParseException;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/auth/gyalsung")
public class GyalsungApiController {
	
	@Value("${gyalsung-token-api}")
	String gyalsungTokenUrl;

	@Value("${gyalsung-consumer-key}")
	String consumerKey;

	@Value("${gyalsung-consumer-secret}")
	String consumerSecret;

	@Value("${gyalsung-data-api}")
	String gyalsungDataUrl;

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/gyalsungDetails/{citizenshipNo}")
	public ResponseEntity<Object> getGyalsungDetails(@PathVariable String citizenshipNo) throws ParseException {

		// Step 1: Generate token (no model, no DB)
		String accessToken = generateNewToken();

		// Step 2: Prepare headers
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<String> request = new HttpEntity<>(headers);

		// Step 3: Call citizen API
		ResponseEntity<Object> response = restTemplate.exchange(gyalsungDataUrl + citizenshipNo, HttpMethod.GET, request,
				Object.class);

		return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
	}

	private String generateNewToken() {

		String authStringEnc = Base64.getEncoder().encodeToString((consumerKey + ":" + consumerSecret).getBytes());

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + authStringEnc);

		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				gyalsungTokenUrl + "?grant_type=client_credentials", HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		// Extract access_token directly from response
		return (String) response.getBody().get("access_token");
	}
}

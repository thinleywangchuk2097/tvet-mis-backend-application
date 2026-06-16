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
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/student")
public class BcseaStudentDetailsController {
	
	@Value("${bcsea-token-api}")
	String tokenUrl;

	@Value("${bcsea-consumer-key}")
	String consumerKey;

	@Value("${bcsea-consumer-secret}")
	String consumerSecret;

	@Value("${bcsea-student-api}")
	String studentUrl;

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/get-student-details/{citizenshipNo}")
	public ResponseEntity<Object> getCitizenDetails(@PathVariable String citizenshipNo) throws ParseException {

		// Step 1: Generate token (no model, no DB)
		String accessToken = generateNewToken();

		// Step 2: Prepare headers
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<String> request = new HttpEntity<>(headers);

		// Step 3: Call citizen API
		ResponseEntity<Object> response = restTemplate.exchange(studentUrl + citizenshipNo, HttpMethod.GET, request,
				Object.class);

		return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
	}

	private String generateNewToken() {

		String authStringEnc = Base64.getEncoder().encodeToString((consumerKey + ":" + consumerSecret).getBytes());

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + authStringEnc);

		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				tokenUrl + "?grant_type=client_credentials", HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		// Extract access_token directly from response
		return (String) response.getBody().get("access_token");
	}
}

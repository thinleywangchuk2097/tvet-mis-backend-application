package com.moesd.tvet.mis.backend.application.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.moesd.tvet.mis.backend.application.service.DatahubTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/auth/license")
public class IBLSAPIController {
	@Value("${datahub.ibls-license-api}")
	String LicenseUrl;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DatahubTokenService datahubTokenService;

	@GetMapping("/licenseDetails/{licenseNo}")
	public ResponseEntity<Object> getCitizenDetails(@PathVariable String licenseNo) throws ParseException {

		// Step 1: Generate token (no model, no DB)
		// Same DataHub SSO token as DatahubAPIController
		String accessToken = datahubTokenService.generateNewToken();

		// Step 2: Prepare headers
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<String> request = new HttpEntity<>(headers);

		// Step 3: Call citizen API
		ResponseEntity<Object> response = restTemplate.exchange(LicenseUrl + licenseNo, HttpMethod.GET, request,
				Object.class);

		return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
	}
}

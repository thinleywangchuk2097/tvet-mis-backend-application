package com.moesd.tvet.mis.backend.application.controller;

import java.text.ParseException;
import java.util.Base64;
import java.util.List;
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
import com.moesd.tvet.mis.backend.application.dto.PublicCitizenDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/datahub")
public class DatahubAPIController {

	@Value("${datahub.token-api}")
	String tokenApi;

	@Value("${datahub.consumer-key}")
	String consumerKey;

	@Value("${datahub.consumer-secret}")
	String consumerSecret;

	@Value("${datahub.citizen-api}")
	String citizenshipUrl;

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/citizendetails/{citizenshipNo}")
	public ResponseEntity<PublicCitizenDetailsResponse> getCitizenDetails(@PathVariable String citizenshipNo)
			throws ParseException {

		// Step 1: Generate token
		String accessToken = generateNewToken();

		// Step 2: Prepare headers
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<String> request = new HttpEntity<>(headers);

		// Step 3: Call citizen API
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(citizenshipUrl + citizenshipNo,
				HttpMethod.GET, request, new ParameterizedTypeReference<Map<String, Object>>() {
				});

		// Step 4: Extract ONLY the fields the public page needs
		PublicCitizenDetailsResponse safeResponse = mapToPublicDto(response.getBody());

		return new ResponseEntity<>(safeResponse, HttpStatus.OK);
	}

	/**
	 * Maps the upstream DataHub response to a minimal public DTO. Any field not
	 * explicitly copied here is NOT exposed to the caller.
	 */
	@SuppressWarnings("unchecked")
	private PublicCitizenDetailsResponse mapToPublicDto(Map<String, Object> upstreamBody) {
		PublicCitizenDetailsResponse dto = new PublicCitizenDetailsResponse();

		if (upstreamBody == null) {
			return dto;
		}

		Map<String, Object> citizenDetailsResponse = (Map<String, Object>) upstreamBody.get("citizenDetailsResponse");
		if (citizenDetailsResponse == null) {
			return dto;
		}

		List<Map<String, Object>> citizenDetailList = (List<Map<String, Object>>) citizenDetailsResponse
				.get("citizenDetail");
		if (citizenDetailList == null || citizenDetailList.isEmpty()) {
			return dto;
		}

		Map<String, Object> citizen = citizenDetailList.get(0);
		// Copy ONLY fields
		dto.setCid((String) citizen.get("cid"));
		dto.setFirstName((String) citizen.get("firstName"));
		dto.setMiddleName((String) citizen.get("middleName"));
		dto.setLastName((String) citizen.get("lastName"));
		dto.setFirstDzoName((String) citizen.get("firstDzoName"));
		dto.setMiddleDzoName((String) citizen.get("middleDzoName"));
		dto.setLastDzoName((String) citizen.get("lastDzoName"));
		dto.setDob((String) citizen.get("dob"));
		dto.setGender((String) citizen.get("gender"));
		return dto;
	}

	private String generateNewToken() {
		String authStringEnc = Base64.getEncoder().encodeToString((consumerKey + ":" + consumerSecret).getBytes());

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + authStringEnc);
		
		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				tokenApi + "?grant_type=client_credentials", HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		return (String) response.getBody().get("access_token");
	}
}
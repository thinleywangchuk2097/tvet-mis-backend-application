package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;
import com.moesd.tvet.mis.backend.application.repository.BhutanNDIRepository;
import com.moesd.tvet.mis.backend.application.service.BhutanNDIService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BhutanNDIServiceImpl implements BhutanNDIService {

	private final BhutanNDIRepository bhutanNDIRepository;

	@Value("${ndi.token-api-url}")
	private String tokenApiUrl;

	@Value("${ndi.client_id}")
	private String clientId;

	@Value("${ndi.client_secret}")
	private String clientSecret;

	@Value("${ndi.grant_type}")
	private String grantType;

	@Value("${ndi.create_proof_request_url}")
	private String createProofRequestUrl;

	@Value("${ndi.schema_name_url}")
	private String schemaNameUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	@Override
	public BhutanNDIToken getValidToken() {
		BhutanNDIToken existingToken = bhutanNDIRepository.findTopByOrderByExpirationDateDesc().orElse(null);

		if (existingToken != null && existingToken.getExpirationDate().isAfter(LocalDateTime.now())) {
			return existingToken;
		}

		Map<String, Object> tokenResponse = generateNewToken();

		String accessToken = (String) tokenResponse.get("access_token");
		Integer expiresIn = (Integer) tokenResponse.get("expires_in");
		String tokenType = (String) tokenResponse.get("token_type");

		BhutanNDIToken newToken = BhutanNDIToken.builder().accessToken(accessToken)
				.expirationDate(LocalDateTime.now().plusSeconds(expiresIn)).tokenType(tokenType).build();

		return bhutanNDIRepository.save(newToken);
	}

	private Map<String, Object> generateNewToken() {

		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("client_id", clientId);
		requestBody.put("client_secret", clientSecret);
		requestBody.put("grant_type", grantType);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(tokenApiUrl, HttpMethod.POST,
				requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
				});

		if (response.getBody() == null) {
			throw new RuntimeException("Failed to fetch NDI token");
		}

		return response.getBody();
	}

	@Override
	public Map<String, Object> createProofRequest(BhutanNDIToken token) {

		Map<String, Object> payload = new HashMap<>();
		payload.put("proofName", "Tvet-Mis System Foundational ID");

		List<Map<String, Object>> proofAttributes = new ArrayList<>();

		proofAttributes.add(Map.of("name", "ID Number", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));

		proofAttributes.add(Map.of("name", "Full Name", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));

		proofAttributes.add(Map.of("name", "Gender", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));

		proofAttributes
				.add(Map.of("name", "Date of Birth", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));

		proofAttributes
				.add(Map.of("name", "Contact Number", "restrictions", new ArrayList<>(), "selfAttestedAllowed", true));

		proofAttributes.add(Map.of("name", "Email", "restrictions", new ArrayList<>(), "selfAttestedAllowed", true));

		payload.put("proofAttributes", proofAttributes);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + token.getAccessToken());

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(createProofRequestUrl, HttpMethod.POST,
				requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
				});

		return response.getBody();
	}
}
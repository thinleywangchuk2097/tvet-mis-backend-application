package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.moesd.tvet.mis.backend.application.service.DatahubTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatahubTokenServiceImpl implements DatahubTokenService {

	private final RestTemplate restTemplate;

	@Value("${datahub.token-api}")
	private String tokenApi;

	@Value("${datahub.consumer-key}")
	private String consumerKey;

	@Value("${datahub.consumer-secret}")
	private String consumerSecret;

	@Override
	public String generateNewToken() {
		String authStringEnc = Base64.getEncoder()
				.encodeToString((consumerKey.trim() + ":" + consumerSecret.trim()).getBytes(StandardCharsets.UTF_8));

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + authStringEnc);

		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				tokenApi.trim() + "?grant_type=client_credentials", HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		Map<String, Object> body = response.getBody();
		if (body == null || body.get("access_token") == null) {
			throw new IllegalStateException("DataHub token API returned no access_token");
		}
		return (String) body.get("access_token");
	}
}

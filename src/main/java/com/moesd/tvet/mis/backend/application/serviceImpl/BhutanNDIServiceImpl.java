package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.moesd.tvet.mis.backend.application.dto.AuthenticationRequest;
import com.moesd.tvet.mis.backend.application.dto.UserRegisterRequest;
import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;
import com.moesd.tvet.mis.backend.application.repository.BhutanNDIRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import com.moesd.tvet.mis.backend.application.service.AuthenticationService;
import com.moesd.tvet.mis.backend.application.service.BhutanNDIService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BhutanNDIServiceImpl implements BhutanNDIService {

	private final BhutanNDIRepository bhutanNDIRepository;
	private final AuthenticationService authenticationService;
	private final UserRepository userRepository;

	@Value("${ndi.token_api_url}")
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

		payload.put("purpose", "login");
		payload.put("proofName", "Tvet Mis System Foundational ID");

		List<Map<String, Object>> proofAttributes = new ArrayList<>();

		proofAttributes.add(Map.of("name", "ID Number", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));

		proofAttributes.add(Map.of("name", "Full Name", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));

		proofAttributes.add(Map.of("name", "Gender", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));

		proofAttributes
				.add(Map.of("name", "Date of Birth", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));
		/*
		 * proofAttributes .add(Map.of("name", "Contact Number", "restrictions", new
		 * ArrayList<>(), "selfAttestedAllowed", true));
		 * 
		 * proofAttributes.add(Map.of("name", "Email", "restrictions", new
		 * ArrayList<>(), "selfAttestedAllowed", true));
		 */

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

	@Override
	public ResponseEntity<?> processNatsResponse(JsonNode payload) {
		try {
			JsonNode dataNode = payload.path("data");

			String type = dataNode.path("type").asText();

			if ("present-proof/rejected".equals(type)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("message", "User rejected proof request"));
			}

			JsonNode requestedPresentation = dataNode.path("requested_presentation");
			JsonNode revealedAttrs = requestedPresentation.path("revealed_attrs");

			String idNumber = revealedAttrs.path("ID Number").get(0).path("value").asText();
			String fullName = revealedAttrs.path("Full Name").get(0).path("value").asText();
			String gender = revealedAttrs.path("Gender").get(0).path("value").asText();
			String dateOfBirth = revealedAttrs.path("Date of Birth").get(0).path("value").asText();

			// Optional: split name
			String[] nameParts = fullName.split("\\s+");
			String firstName = nameParts.length > 0 ? nameParts[0] : "";
			String middleName = nameParts.length > 2
					? String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length - 1))
					: "";
			String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : "";

			// Build clean response
			Map<String, Object> response = new HashMap<>();
			response.put("idNumber", idNumber);
			response.put("fullName", fullName);
			response.put("firstName", firstName);
			response.put("middleName", middleName);
			response.put("lastName", lastName);
			response.put("gender", gender);
			response.put("dateOfBirth", dateOfBirth);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to process NATS response"));
		}
	}

	@Override
	public ResponseEntity<?> processAuthNatsResponse(JsonNode payload) {
		try {
			// Log the received pay load
			System.out.println("Received payload: " + payload.toString());

			// Extract the "data" node
			JsonNode dataNode = payload.path("data");
			String type = dataNode.path("type").asText();
			if ("present-proof/rejected".equals(type)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("The user has declined the request to share proof.");
			}
			// Extract fields from "data"
			JsonNode requestedPresentation = dataNode.path("requested_presentation");

			// Extract revealed attributes
			JsonNode revealedAttrs = requestedPresentation.path("revealed_attrs");
			String idNumber = revealedAttrs.path("ID Number").get(0).path("value").asText();
			String fullName = revealedAttrs.path("Full Name").get(0).path("value").asText();
			// String gender = revealedAttrs.path("Gender").get(0).path("value").asText();
			String dateOfBirth = revealedAttrs.path("Date of Birth").get(0).path("value").asText();

			// Split fullName into first, middle, and last name
			String[] nameParts = fullName.split("\\s+");
			String firstName = nameParts.length > 0 ? nameParts[0] : "";
			String middleName = nameParts.length > 2
					? String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length - 1))
					: "";
			String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : middleName;

			try {
				List<Tuple> user = userRepository.findNDIByUserId(idNumber, 1);
				// Check if user exists
				if (user != null && !user.isEmpty()) {
					AuthenticationRequest data = new AuthenticationRequest();
					data.setUsername(idNumber);
					return (authenticationService.authByBhutanNDI(data));
				} else {
					// Create a Set for roles
					Set<Integer> roles = new HashSet<>();
					roles.add(11);// training provider roleId
					roles.add(22);
					roles.add(28);
					roles.add(23);
					roles.add(9);
					roles.add(7);
					// Register a new user if not found
					UserRegisterRequest registerRequest = UserRegisterRequest.builder().userId(idNumber)
							.firstName(firstName).middleName(middleName).lastName(lastName)
							.password("password").locationId("14")
							.statusId("1").role(roles).doB(dateOfBirth).currentRole(11)
							.build();
					return (authenticationService.register(registerRequest));
				}

			} catch (RuntimeException e) {
				System.err.println("Error: " + e.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User has denied");
		}
	}
}
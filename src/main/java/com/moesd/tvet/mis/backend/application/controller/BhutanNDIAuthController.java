package com.moesd.tvet.mis.backend.application.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.moesd.tvet.mis.backend.application.configuration.JwtUtilService;
import com.moesd.tvet.mis.backend.application.dto.AuthenticationResponse;
import com.moesd.tvet.mis.backend.application.dto.UserRegisterRequest;
import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;
import com.moesd.tvet.mis.backend.application.model.Role;
import com.moesd.tvet.mis.backend.application.model.Token;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.repository.BhutanNDIRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleRepository;
import com.moesd.tvet.mis.backend.application.repository.TokenRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/ndi")
public class BhutanNDIAuthController {
	
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

	private final BhutanNDIRepository bhutanNDIRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final JwtUtilService jwtService;
	private final TokenRepository tokenRepository;

	@PostMapping("/get-token")
	public ResponseEntity<?> getNDIToken() {
		BhutanNDIToken existingToken = bhutanNDIRepository.findTopByOrderByExpirationDateDesc().orElse(null);
		if (existingToken != null && existingToken.getExpirationDate().isAfter(LocalDateTime.now())) {
			return ResponseEntity.ok(existingToken);
		}

		RestTemplate restTemplate = new RestTemplate();

		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("client_id", clientId);
		requestBody.put("client_secret", clientSecret);
		requestBody.put("grant_type", grantType);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

		// Correctly using ParameterizedTypeReference
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(tokenApiUrl, HttpMethod.POST,
				requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
				} // <-- this is correct
		);

		Map<String, Object> responseBody = response.getBody();
		if (responseBody == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch token");
		}

		String accessToken = (String) responseBody.get("access_token");
		Integer expiresIn = (Integer) responseBody.get("expires_in");
		String tokenType = (String) responseBody.get("token_type");

		LocalDateTime expirationDate = LocalDateTime.now().plusSeconds(expiresIn);

		BhutanNDIToken newToken = BhutanNDIToken.builder().accessToken(accessToken).expirationDate(expirationDate)
				.tokenType(tokenType).build();

		bhutanNDIRepository.save(newToken);

		return ResponseEntity.ok(newToken);
	}

	@PostMapping("/create-proof-request")
	public ResponseEntity<?> createProofRequests() {
		// Step 1: Retrieve the token
		ResponseEntity<?> tokenResponse = getNDIToken();
		if (tokenResponse.getStatusCode() != HttpStatus.OK) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve token");
		}
		BhutanNDIToken token = (BhutanNDIToken) tokenResponse.getBody();
		if (token == null || token.getAccessToken() == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid token");
		}
		// Step 2: Prepare the pay load
		Map<String, Object> payload = new HashMap<>();
		payload.put("proofName", "Tvet-Mis System Foundational ID");
		List<Map<String, Object>> proofAttributes = new ArrayList<>();
		// ID Number
		proofAttributes.add(Map.of("name", "ID Number", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));
		// Full Name
		proofAttributes.add(Map.of("name", "Full Name", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));
		// Gender
		proofAttributes.add(Map.of("name", "Gender", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));
		// Date of Birth
		proofAttributes
				.add(Map.of("name", "Date of Birth", "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));
		// Contact Number
		proofAttributes
				.add(Map.of("name", "Contact Number", "restrictions", new ArrayList<>(), "selfAttestedAllowed", true));
		// Email
		proofAttributes.add(Map.of("name", "Email", "restrictions", new ArrayList<>(), "selfAttestedAllowed", true));
		payload.put("proofAttributes", proofAttributes);

		// Step 3: Call the API
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + token.getAccessToken());

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(createProofRequestUrl, HttpMethod.POST,
					requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
					} // <-- type safe
			);

			if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
				return ResponseEntity.ok(response.getBody());
			} else {
				return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create proof request");
		}
	}

	@PostMapping("/nats-response-submit")
	public ResponseEntity<?> bhutanNDIWebhookResponse(@RequestBody JsonNode payload) {
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
			// String type = dataNode.path("type").asText();
			// String verificationResult = dataNode.path("verification_result").asText();
			JsonNode requestedPresentation = dataNode.path("requested_presentation");

			// Extract revealed attributes
			JsonNode revealedAttrs = requestedPresentation.path("revealed_attrs");
			String idNumber = revealedAttrs.path("ID Number").get(0).path("value").asText();
			String fullName = revealedAttrs.path("Full Name").get(0).path("value").asText();
			// String gender = revealedAttrs.path("Gender").get(0).path("value").asText();
			String contactNumber = revealedAttrs.path("Contact Number").get(0).path("value").asText();
			String email = revealedAttrs.path("Email").get(0).path("value").asText();
			// String dateOfBirth = revealedAttrs.path("Date of
			// Birth").get(0).path("value").asText();

			// Split fullName into first, middle, and last name
			String[] nameParts = fullName.split("\\s+");
			String firstName = nameParts.length > 0 ? nameParts[0] : "";
			String middleName = nameParts.length > 2
					? String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length - 1))
					: "";
			String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : middleName;
			// Create a Set for roles
			Set<Integer> roles = new HashSet<>();
			roles.add(2);
			roles.add(3);

			try {
				List<Tuple> user = userRepository.findNDIByUserId(idNumber, 1);
				AuthenticationResponse authResponse;
				// Check if user exists
				if (user != null && !user.isEmpty()) {
					String userId = "";
					for (Tuple tuple : user) {
						userId = tuple.get(0, String.class);
					}
					authResponse = authenticate(userId);
				} else {
					// Register a new user if not found
					UserRegisterRequest registerRequest = UserRegisterRequest.builder().userId(idNumber)
							.firstName(firstName).middleName(middleName).lastName(lastName).role(roles).emailId(email)
							.mobileNo(contactNumber).build();
					authResponse = register(registerRequest);
				}

				// Validate the authentication response
				if (authResponse != null && authResponse.getAccessToken() != null
						&& authResponse.getRefreshToken() != null) {
					return ResponseEntity.ok(authResponse);
				} else {
					System.err.println("Authentication failed: No tokens received");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body("Authentication failed: No tokens received");
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

	@Transactional
	public AuthenticationResponse register(UserRegisterRequest request) {
		Set<Integer> userRoles = request.getRole();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<Role> roles = new HashSet();

		userRoles.forEach(r -> {
			Role role = this.roleRepository.findById(r)
					.orElseThrow(() -> new RuntimeException("Error: Role with ID " + r + " is not found."));
			roles.add(role);
		});

		var user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
				.middleName(request.getMiddleName()).mobileNo(request.getMobileNo()).userId(request.getUserId())
			//	.role(roles)
				.emailId(request.getEmailId()).statusId("1").password("").build();

		var savedUser = userRepository.save(user);
		if (savedUser != null) {
			var jwtToken = jwtService.generateToken(user);
			var refreshToken = jwtService.generateRefreshToken(user);
			saveUserToken(savedUser, jwtToken);
			return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
		}
		return null;
	}

	public AuthenticationResponse authenticate(String userId) {
		var user = userRepository.findByUsername(userId, 1).orElseThrow();
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		revokeAllUserTokens(user);
		saveUserToken(user, jwtToken);
		return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
	}

	private void revokeAllUserTokens(User user) {
		var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getUsername());
		if (validUserTokens.isEmpty())
			return;
		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});
		tokenRepository.saveAll(validUserTokens);
	}

	private void saveUserToken(User user, String jwtToken) {
		var token = Token.builder().user(user).token(jwtToken).tokenType("BEARER").expired(false).revoked(false)
				.build();
		tokenRepository.save(token);
	}

}

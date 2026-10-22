package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.moesd.tvet.mis.backend.application.dto.AuthenticationRequest;
import com.moesd.tvet.mis.backend.application.dto.UserRegisterRequest;
import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;
import com.moesd.tvet.mis.backend.application.repository.BhutanNDIRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import com.moesd.tvet.mis.backend.application.service.AuthenticationService;
import com.moesd.tvet.mis.backend.application.service.BhutanNDIService;
import io.nats.client.AuthHandler;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.NKey;
import io.nats.client.Nats;
import io.nats.client.Options;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class BhutanNDIServiceImpl implements BhutanNDIService {

	private final BhutanNDIRepository bhutanNDIRepository;
	private final AuthenticationService authenticationService;
	private final UserRepository userRepository;

	@Value("${ndi.token-api-url}")
	private String tokenApiUrl;

	@Value("${ndi.client-id}")
	private String clientId;

	@Value("${ndi.client-secret}")
	private String clientSecret;

	@Value("${ndi.grant-type}")
	private String grantType;

	@Value("${ndi.create-proof-request-url}")
	private String createProofRequestUrl;

	@Value("${ndi.schema-name-url}")
	private String schemaNameUrl;

	@Value("${ndi.nats.url}")
	private String natsUrl;

	@Value("${ndi.nats.seed}")
	private String natsSeed;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final Map<String, Connection> activeNatsConnections = new ConcurrentHashMap<>();
	private final Map<String, Dispatcher> activeNatsDispatchers = new ConcurrentHashMap<>();
	private final Map<String, Object> natsAuthResults = new ConcurrentHashMap<>();

	// ==================== Token ====================

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

	// ==================== Proof request ====================

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
			if (payload == null || payload.isNull()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Empty NDI response"));
			}

			if (payload.isString()) {
				String text = payload.asString("");
				if (text.toLowerCase().contains("denied")) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body(Map.of("message", "User rejected proof request"));
				}
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Unexpected NDI response"));
			}

			JsonNode dataNode = payload.path("data");
			if (dataNode.isMissingNode() || dataNode.isNull()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Malformed NDI response"));
			}

			String type = dataNode.path("type").asString("");
			if ("present-proof/rejected".equals(type)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("message", "User rejected proof request"));
			}

			JsonNode requestedPresentation = dataNode.path("requested_presentation");
			JsonNode revealedAttrs = requestedPresentation.path("revealed_attrs");

			if (revealedAttrs.isMissingNode() || revealedAttrs.isNull()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("message", "Missing attributes in NDI response"));
			}

			String idNumber = safeRevealedValue(revealedAttrs, "ID Number");
			String fullName = safeRevealedValue(revealedAttrs, "Full Name");
			String gender = safeRevealedValue(revealedAttrs, "Gender");
			String dateOfBirth = safeRevealedValue(revealedAttrs, "Date of Birth");

			if (idNumber == null || idNumber.isBlank()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("message", "NDI response missing ID Number"));
			}

			String[] nameParts = fullName == null ? new String[0] : fullName.trim().split("\\s+");
			String firstName = nameParts.length > 0 ? nameParts[0] : "";
			String middleName = nameParts.length > 2
					? String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length - 1))
					: "";
			String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : middleName;

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
			log.error("Failed to process NATS response", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to process NATS response"));
		}
	}

	// ==================== Backend NATS auth processing ====================

	@Override
	public ResponseEntity<?> processAuthNatsResponse(JsonNode payload) {
		try {
			if (payload == null || payload.isNull()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Empty NDI response"));
			}

			if (payload.isString()) {
				String text = payload.asString("");
				if (text.toLowerCase().contains("denied")) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body(Map.of("message", "The user has declined the request to share proof."));
				}
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Unexpected NDI response"));
			}

			if (payload.has("message")) {
				String msg = payload.path("message").asString("");
				if (msg.toLowerCase().contains("denied")) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body(Map.of("message", "The user has declined the request to share proof."));
				}
			}

			JsonNode dataNode = payload.path("data");
			if (dataNode.isMissingNode() || dataNode.isNull()) {
				log.warn("NATS payload missing 'data' field: {}", payload);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Malformed NDI response"));
			}

			String type = dataNode.path("type").asString("");
			if ("present-proof/rejected".equals(type)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("message", "The user has declined the request to share proof."));
			}

			JsonNode requestedPresentation = dataNode.path("requested_presentation");
			JsonNode revealedAttrs = requestedPresentation.path("revealed_attrs");
			if (revealedAttrs.isMissingNode() || revealedAttrs.isNull()) {
				log.warn("Missing revealed_attrs in NATS payload: {}", payload);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("message", "Missing attributes in NDI response"));
			}

			String idNumber = safeRevealedValue(revealedAttrs, "ID Number");
			String fullName = safeRevealedValue(revealedAttrs, "Full Name");
			String dateOfBirth = safeRevealedValue(revealedAttrs, "Date of Birth");

			if (idNumber == null || idNumber.isBlank()) {
				log.warn("NDI response missing ID Number: {}", payload);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("message", "NDI response missing ID Number"));
			}

			String[] nameParts = fullName == null ? new String[0] : fullName.trim().split("\\s+");
			String firstName = nameParts.length > 0 ? nameParts[0] : "";
			String middleName = nameParts.length > 2
					? String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length - 1))
					: "";
			String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : middleName;

			List<Tuple> user = userRepository.findNDIByUserId(idNumber, 1);
			if (user != null && !user.isEmpty()) {
				AuthenticationRequest data = new AuthenticationRequest();
				data.setUsername(idNumber);
				return authenticationService.authByBhutanNDI(data);
			} else {
				Set<Integer> roles = new HashSet<>();
				roles.add(11);
				roles.add(22);
				roles.add(28);
				roles.add(23);
				roles.add(9);
				roles.add(7);
				UserRegisterRequest registerRequest = UserRegisterRequest.builder().userId(idNumber)
						.firstName(firstName).middleName(middleName).lastName(lastName).password("password")
						.locationId("14").statusId("1").role(roles).doB(dateOfBirth).currentRole(11).build();
				return authenticationService.register(registerRequest);
			}

		} catch (Exception e) {
			log.error("Failed to process NDI auth response", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to process NDI response"));
		}
	}

	private String safeRevealedValue(JsonNode revealedAttrs, String attributeName) {
		JsonNode attr = revealedAttrs.path(attributeName);
		if (attr.isMissingNode() || attr.isNull() || !attr.isArray() || attr.isEmpty()) {
			return null;
		}
		JsonNode first = attr.get(0);
		if (first == null || first.isNull()) {
			return null;
		}
		JsonNode value = first.path("value");
		if (value.isMissingNode() || value.isNull()) {
			return null;
		}
		return value.asString(null);
	}

	// ==================== NATS ====================

	@Override
	public void connectToNats(String threadId, String sessionKey) {

		if (threadId == null || threadId.isBlank()) {
			log.error("Invalid thread ID provided to connectToNats");
			throw new IllegalArgumentException("Invalid thread ID");
		}

		if (activeNatsConnections.containsKey(threadId)) {
			log.warn("Already subscribed to threadId: {}", threadId);
			return;
		}

		try {
			Connection nc = createNatsConnection();
			activeNatsConnections.put(threadId, nc);
			log.info("Connected to NATS at {}", natsUrl);

			Dispatcher dispatcher = nc.createDispatcher(msg -> {
				try {
					String jsonPayload = new String(msg.getData(), StandardCharsets.UTF_8);

					log.info("Received NATS message on subject [{}] replyTo [{}] thread [{}]: [{}]", msg.getSubject(),
							msg.getReplyTo(), threadId, jsonPayload);

					String trimmed = jsonPayload == null ? "" : jsonPayload.trim();

					if (trimmed.isEmpty()) {
						log.debug("Ignoring empty NATS payload on thread {}", threadId);
						return;
					}

					JsonNode node;
					if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
						node = objectMapper.readTree(trimmed);
					} else {
						node = objectMapper.getNodeFactory().stringNode(trimmed);
					}

					var response = processAuthNatsResponse(node);
					log.info("Auth processing status: {}", response.getStatusCode());

					if (response.getStatusCode().is2xxSuccessful()) {
						log.info("Authentication succeeded for threadId: {}", threadId);
						natsAuthResults.put(threadId, response.getBody());
					} else {
						HttpStatusCode code = response.getStatusCode();
						String errCode = (code.value() == 401) ? "AUTH_DENIED" : "AUTH_FAILED";
						Object body = response.getBody();
						String message = body instanceof Map<?, ?> m && m.get("message") != null
								? m.get("message").toString()
								: (body != null ? body.toString() : "Authentication failed");
						log.warn("Authentication not completed for threadId: {} — status {} — {}", threadId, code,
								message);
						natsAuthResults.put(threadId, Map.of("error", errCode, "message", message));
					}

					cleanupNats(threadId);

				} catch (Exception e) {
					log.error("Error processing NATS message for threadId: {}", threadId, e);
					natsAuthResults.put(threadId, Map.of("error", "PROCESSING_ERROR", "message",
							e.getMessage() != null ? e.getMessage() : "Processing error"));
					cleanupNats(threadId);
				}
			});

			// NATS `>` does not match the bare subject, so subscribe to both.
			dispatcher.subscribe(threadId);
			dispatcher.subscribe(threadId + ".>");
			log.info("SUBSCRIBED to subject: {} and {}.>", threadId, threadId);

			activeNatsDispatchers.put(threadId, dispatcher);

		} catch (Exception e) {
			log.error("Error connecting to NATS for threadId: {}", threadId, e);
			cleanupNats(threadId);
			throw new RuntimeException("Failed to connect to NATS: " + e.getMessage(), e);
		}
	}

	private Connection createNatsConnection() throws Exception {
		NKey nkey = NKey.fromSeed(natsSeed.toCharArray());

		AuthHandler authHandler = new AuthHandler() {
			@Override
			public char[] getID() {
				try {
					return nkey.getPublicKey();
				} catch (Exception e) {
					log.error("Failed to get public key from NKey", e);
					return null;
				}
			}

			@Override
			public byte[] sign(byte[] nonce) {
				try {
					return nkey.sign(nonce);
				} catch (Exception e) {
					log.error("Failed to sign nonce with NKey", e);
					return null;
				}
			}

			@Override
			public char[] getJWT() {
				return null;
			}
		};

		Options options = new Options.Builder().server(natsUrl).authHandler(authHandler).build();
		return Nats.connect(options);
	}

	private void cleanupNats(String threadId) {

		Dispatcher dispatcher = activeNatsDispatchers.remove(threadId);
		if (dispatcher != null) {
			try {
				dispatcher.unsubscribe(threadId);
			} catch (Exception e) {
				log.warn("Error unsubscribing dispatcher from root subject: {}", threadId, e);
			}
			try {
				dispatcher.unsubscribe(threadId + ".>");
			} catch (Exception e) {
				log.warn("Error unsubscribing dispatcher from wildcard subject: {}", threadId, e);
			}
		}

		Connection connection = activeNatsConnections.remove(threadId);
		if (connection != null) {
			try {
				connection.close();
				log.info("Closed NATS connection for threadId: {}", threadId);
			} catch (Exception e) {
				log.warn("Error closing NATS connection for threadId: {}", threadId, e);
			}
		}

		CompletableFuture.delayedExecutor(5, TimeUnit.MINUTES).execute(() -> natsAuthResults.remove(threadId));
	}

	@Override
	public Object getNatsAuthResult(String threadId) {
		return natsAuthResults.get(threadId);
	}

	@PreDestroy
	public void shutdownNats() {
		log.info("Shutting down all NATS connections...");
		activeNatsConnections.keySet().forEach(this::cleanupNats);
	}
}
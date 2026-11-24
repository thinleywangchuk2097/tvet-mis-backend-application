package com.moesd.tvet.mis.backend.application.serviceimpl;

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
import com.moesd.tvet.mis.backend.application.exception.NdiIntegrationException;
import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;
import com.moesd.tvet.mis.backend.application.repository.BhutanNDIRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import com.moesd.tvet.mis.backend.application.service.AuthenticationService;
import com.moesd.tvet.mis.backend.application.service.BhutanNDIService;
import io.nats.client.AuthHandler;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.ErrorListener;
import io.nats.client.Message;
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

	private static final String MESSAGE = "message";
	private static final String ERROR = "error";
	private static final String DENIED = "denied";
	private static final String PROOF_REJECTED_TYPE = "present-proof/rejected";
	private static final String PROOF_VALIDATED = "ProofValidated";
	private static final String USER_DECLINED = "The user has declined the request to share proof.";
	private static final String USER_REJECTED = "User rejected proof request";
	private static final String MISSING_ID_NUMBER = "NDI response missing ID Number";

	// NDI proof attribute names
	private static final String ATTR_ID_NUMBER = "ID Number";
	private static final String ATTR_FULL_NAME = "Full Name";
	private static final String ATTR_GENDER = "Gender";
	private static final String ATTR_DATE_OF_BIRTH = "Date of Birth";

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
	// Threads whose agent sent an empty NATS notice: value = last time the agent API was asked
	private final Map<String, Long> proofFetchTimes = new ConcurrentHashMap<>();
	private static final long PROOF_FETCH_INTERVAL_MS = 3000;

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
			throw new NdiIntegrationException("Failed to fetch NDI token");
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
		for (String attribute : List.of(ATTR_ID_NUMBER, ATTR_FULL_NAME, ATTR_GENDER, ATTR_DATE_OF_BIRTH)) {
			proofAttributes.add(Map.of("name", attribute, "restrictions", List.of(Map.of("schema_name", schemaNameUrl))));
		}

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
			ResponseEntity<?> invalid = payloadError(payload, USER_REJECTED, false);
			if (invalid != null) {
				return invalid;
			}

			JsonNode revealedAttrs = revealedAttributes(payload);
			String idNumber = safeRevealedValue(revealedAttrs, ATTR_ID_NUMBER);
			if (idNumber == null || idNumber.isBlank()) {
				return error(HttpStatus.BAD_REQUEST, MISSING_ID_NUMBER);
			}
			String fullName = safeRevealedValue(revealedAttrs, ATTR_FULL_NAME);
			NameParts name = splitName(fullName);

			Map<String, Object> response = new HashMap<>();
			response.put("idNumber", idNumber);
			response.put("fullName", fullName);
			response.put("firstName", name.first());
			response.put("middleName", name.middle());
			response.put("lastName", name.last());
			response.put("gender", safeRevealedValue(revealedAttrs, ATTR_GENDER));
			response.put("dateOfBirth", safeRevealedValue(revealedAttrs, ATTR_DATE_OF_BIRTH));

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Failed to process NATS response", e);
			return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process NATS response");
		}
	}

	// ==================== Backend NATS auth processing ====================

	@Override
	public ResponseEntity<?> processAuthNatsResponse(JsonNode payload) {
		try {
			ResponseEntity<?> invalid = payloadError(payload, USER_DECLINED, true);
			if (invalid != null) {
				return invalid;
			}

			JsonNode revealedAttrs = revealedAttributes(payload);
			String idNumber = safeRevealedValue(revealedAttrs, ATTR_ID_NUMBER);
			if (idNumber == null || idNumber.isBlank()) {
				log.warn("NDI response missing ID Number: {}", payload);
				return error(HttpStatus.BAD_REQUEST, MISSING_ID_NUMBER);
			}
			NameParts name = splitName(safeRevealedValue(revealedAttrs, ATTR_FULL_NAME));
			String dateOfBirth = safeRevealedValue(revealedAttrs, ATTR_DATE_OF_BIRTH);

			return loginOrRegister(idNumber, name, dateOfBirth);

		} catch (Exception e) {
			log.error("Failed to process NDI auth response", e);
			return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process NDI response");
		}
	}

	/** Logs in an existing NDI user, or registers a new one with the default roles. */
	private ResponseEntity<?> loginOrRegister(String idNumber, NameParts name, String dateOfBirth) {
		List<Tuple> user = userRepository.findNDIByUserId(idNumber, 1);
		if (user != null && !user.isEmpty()) {
			AuthenticationRequest data = new AuthenticationRequest();
			data.setUsername(idNumber);
			return authenticationService.authByBhutanNDI(data);
		}
		Set<Integer> roles = new HashSet<>(List.of(11, 22, 28, 23, 9, 7));
		UserRegisterRequest registerRequest = UserRegisterRequest.builder().userId(idNumber)
				.firstName(name.first()).middleName(name.middle()).lastName(name.last()).password("password")
				.locationId("14").statusId("1").role(roles).doB(dateOfBirth).currentRole(11).build();
		return authenticationService.register(registerRequest);
	}

	/**
	 * Error response for an empty, declined or malformed NDI payload; null when the payload
	 * carries revealed attributes.
	 */
	private static ResponseEntity<?> payloadError(JsonNode payload, String rejectedMessage, boolean checkMessageField) {
		if (payload == null || payload.isNull()) {
			return error(HttpStatus.BAD_REQUEST, "Empty NDI response");
		}
		if (payload.isString()) {
			if (isDenied(payload.asString(""))) {
				return error(HttpStatus.UNAUTHORIZED, rejectedMessage);
			}
			return error(HttpStatus.BAD_REQUEST, "Unexpected NDI response");
		}
		if (checkMessageField && payload.has(MESSAGE) && isDenied(payload.path(MESSAGE).asString(""))) {
			return error(HttpStatus.UNAUTHORIZED, rejectedMessage);
		}
		JsonNode proof = proofBody(payload);
		if (PROOF_REJECTED_TYPE.equals(proof.path("type").asString(""))) {
			return error(HttpStatus.UNAUTHORIZED, rejectedMessage);
		}
		String verification = proof.path("verification_result").asString("");
		if (!verification.isEmpty() && !PROOF_VALIDATED.equalsIgnoreCase(verification)) {
			log.warn("NDI proof not validated ({}): {}", verification, payload);
			return error(HttpStatus.UNAUTHORIZED, "Bhutan NDI could not validate the shared proof");
		}
		if (isAbsent(revealedAttributes(payload))) {
			log.warn("Missing revealed_attrs in NATS payload: {}", payload);
			return error(HttpStatus.BAD_REQUEST, "Missing attributes in NDI response");
		}
		return null;
	}

	private static JsonNode revealedAttributes(JsonNode payload) {
		return proofBody(payload).path("requested_presentation").path("revealed_attrs");
	}

	/**
	 * The proof itself. Documented NATS format has it at the top level
	 * ({"type":"present-proof/presentation-result", "requested_presentation":{...}, "thid":...});
	 * older payloads wrap it in "data".
	 */
	private static JsonNode proofBody(JsonNode payload) {
		JsonNode data = payload.path("data");
		return data.isObject() && !payload.has("requested_presentation") ? data : payload;
	}

	private static boolean isDenied(String text) {
		return text.toLowerCase().contains(DENIED);
	}

	private static boolean isAbsent(JsonNode node) {
		return node.isMissingNode() || node.isNull();
	}

	private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of(MESSAGE, message));
	}

	/** First word = first name, last word = last name, anything in between = middle name. */
	private record NameParts(String first, String middle, String last) {
	}

	private static NameParts splitName(String fullName) {
		String[] parts = fullName == null ? new String[0] : fullName.trim().split("\\s+");
		String first = parts.length > 0 ? parts[0] : "";
		String middle = parts.length > 2 ? String.join(" ", Arrays.copyOfRange(parts, 1, parts.length - 1)) : "";
		String last = parts.length > 1 ? parts[parts.length - 1] : middle;
		return new NameParts(first, middle, last);
	}

	private String safeRevealedValue(JsonNode revealedAttrs, String attributeName) {
		JsonNode attr = revealedAttrs.path(attributeName);
		if (isAbsent(attr) || !attr.isArray() || attr.isEmpty()) {
			return null;
		}
		JsonNode first = attr.get(0);
		if (first == null || first.isNull()) {
			return null;
		}
		JsonNode value = first.path("value");
		if (isAbsent(value)) {
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

			Dispatcher dispatcher = nc.createDispatcher(msg -> handleNatsMessage(threadId, msg));

			// NATS `>` does not match the bare subject, so subscribe to both.
			dispatcher.subscribe(threadId);
			dispatcher.subscribe(threadId + ".>");
			log.info("SUBSCRIBED to subject: {} and {}.>", threadId, threadId);

			activeNatsDispatchers.put(threadId, dispatcher);

		} catch (Exception e) {
			log.error("Error connecting to NATS for threadId: {}", threadId, e);
			cleanupNats(threadId);
			throw new NdiIntegrationException("Failed to connect to NATS: " + e.getMessage(), e);
		}
	}

	/** Processes one NDI proof message for a login thread and stores the result for polling. */
	private void handleNatsMessage(String threadId, Message msg) {
		try {
			String jsonPayload = new String(msg.getData(), StandardCharsets.UTF_8);
			log.info("Received NATS message on subject [{}] replyTo [{}] thread [{}]: [{}]", msg.getSubject(),
					msg.getReplyTo(), threadId, jsonPayload);
			String trimmed = jsonPayload.trim();
			if (trimmed.isEmpty()) {
				// This agent only sends an empty notice: fetch the proof from its API
				log.info("Empty NATS notice on thread {}: fetching the proof from the agent", threadId);
				proofFetchTimes.putIfAbsent(threadId, 0L);
				fetchProofFromAgent(threadId);
				return;
			}
			storeAuthResult(threadId, toJsonNode(trimmed));
		} catch (Exception e) {
			log.error("Error processing NATS message for threadId: {}", threadId, e);
			natsAuthResults.put(threadId, Map.of(ERROR, "PROCESSING_ERROR", MESSAGE,
					e.getMessage() != null ? e.getMessage() : "Processing error"));
			cleanupNats(threadId);
		}
	}

	/** Logs in / registers from an NDI proof payload, stores the result for polling and closes NATS. */
	private void storeAuthResult(String threadId, JsonNode payload) {
		var response = processAuthNatsResponse(payload);
		log.info("Auth processing status: {}", response.getStatusCode());
		if (response.getStatusCode().is2xxSuccessful()) {
			log.info("Authentication succeeded for threadId: {}", threadId);
			natsAuthResults.put(threadId, response.getBody());
		} else {
			HttpStatusCode code = response.getStatusCode();
			String errCode = (code.value() == 401) ? "AUTH_DENIED" : "AUTH_FAILED";
			String message = failureMessage(response.getBody());
			log.warn("Authentication not completed for threadId: {} — status {} — {}", threadId, code, message);
			natsAuthResults.put(threadId, Map.of(ERROR, errCode, MESSAGE, message));
		}
		proofFetchTimes.remove(threadId);
		cleanupNats(threadId);
	}

	/**
	 * Asks the agent for the proof by thread id (its NATS notice is empty). The raw reply is logged;
	 * when it carries revealed attributes the user is logged in. Throttled per thread.
	 */
	private void fetchProofFromAgent(String threadId) {
		long now = System.currentTimeMillis();
		Long last = proofFetchTimes.get(threadId);
		if (last == null || now - last < PROOF_FETCH_INTERVAL_MS || natsAuthResults.containsKey(threadId)) {
			return;
		}
		proofFetchTimes.put(threadId, now);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + getValidToken().getAccessToken());
			String url = createProofRequestUrl + "?threadId=" + threadId;
			ResponseEntity<String> reply = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
					String.class);
			String body = reply.getBody();
			log.info("Agent proof lookup [{}] -> {}: {}", url, reply.getStatusCode(), body);
			if (body != null && !body.isBlank()) {
				JsonNode payload = objectMapper.readTree(body);
				if (!isAbsent(revealedAttributes(payload))) {
					storeAuthResult(threadId, payload);
				} else if ("done".equalsIgnoreCase(payload.path("data").path("status").asString(""))) {
					// Proof finished on the agent but it gave us no attributes: stop the page waiting
					log.warn("NDI agent finished thread {} without returning the proof attributes", threadId);
					natsAuthResults.put(threadId, Map.of(ERROR, "AUTH_FAILED", MESSAGE,
							"Bhutan NDI did not return your details. Please try again later or use another login method."));
					proofFetchTimes.remove(threadId);
					cleanupNats(threadId);
				}
			}
		} catch (Exception e) {
			log.warn("Agent proof lookup failed for thread {}: {}", threadId, e.getMessage());
		}
	}

	/** JSON objects/arrays are parsed; anything else (e.g. "denied") becomes a text node. */
	private JsonNode toJsonNode(String payload) {
		if (payload.startsWith("{") || payload.startsWith("[")) {
			return objectMapper.readTree(payload);
		}
		return objectMapper.getNodeFactory().stringNode(payload);
	}

	private static String failureMessage(Object body) {
		if (body instanceof Map<?, ?> m && m.get(MESSAGE) != null) {
			return m.get(MESSAGE).toString();
		}
		return body != null ? body.toString() : "Authentication failed";
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

		// errorListener: log why a connect attempt failed (the client otherwise only says "Unable to connect")
		Options options = new Options.Builder().server(natsUrl).authHandler(authHandler)
				.errorListener(new ErrorListener() {
					@Override
					public void exceptionOccurred(Connection conn, Exception exp) {
						log.warn("NATS connection problem ({}): {}", natsUrl, exp.toString(), exp);
					}
				}).build();
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

		CompletableFuture.delayedExecutor(5, TimeUnit.MINUTES).execute(() -> {
			natsAuthResults.remove(threadId);
			proofFetchTimes.remove(threadId);
		});
	}

	@Override
	public Object getNatsAuthResult(String threadId) {
		if (!natsAuthResults.containsKey(threadId) && proofFetchTimes.containsKey(threadId)) {
			fetchProofFromAgent(threadId); // retried while the login page keeps polling
		}
		return natsAuthResults.get(threadId);
	}

	@PreDestroy
	public void shutdownNats() {
		log.info("Shutting down all NATS connections...");
		activeNatsConnections.keySet().forEach(this::cleanupNats);
	}
}
package com.moesd.tvet.mis.backend.application.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;
import com.moesd.tvet.mis.backend.application.service.BhutanNDIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/auth/ndi")
public class BhutanNDIController {

	private final BhutanNDIService bhutanNDIService;

	@PostMapping("/get-token")
	public ResponseEntity<?> getNDIToken() {
		return ResponseEntity.ok(bhutanNDIService.getValidToken());
	}

	@PostMapping("/create-proof-request")
	public ResponseEntity<?> createProofRequests() {

		BhutanNDIToken token = bhutanNDIService.getValidToken();

		if (token == null || token.getAccessToken() == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid token");
		}

		try {
			Map<String, Object> response = bhutanNDIService.createProofRequest(token);

			String threadId = extractThreadId(response);
			if (threadId != null) {
				log.info("Auto-subscribing to NATS threadId: {}", threadId);
				bhutanNDIService.connectToNats(threadId, null);
			}

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Failed to create proof request", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create proof request");
		}
	}

	@GetMapping("/nats-auth-result/{threadId}")
	public ResponseEntity<?> getNatsAuthResult(@PathVariable String threadId) {

		Object result = bhutanNDIService.getNatsAuthResult(threadId);

		if (result == null) {
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status", "PENDING"));
		}

		if (result instanceof Map<?, ?> map && map.containsKey("error")) {
			String errCode = String.valueOf(map.get("error"));
			if ("AUTH_DENIED".equals(errCode)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
		}

		return ResponseEntity.ok(result);
	}

	@PostMapping("/return-nats-response")
	public ResponseEntity<?> returnNDINatsResponse(@RequestBody JsonNode payload) {
		return bhutanNDIService.processNatsResponse(payload);
	}

	@PostMapping("/nats-response-auth")
	public ResponseEntity<?> bhutanNDIAuthNatsResponse(@RequestBody JsonNode payload) {
		return bhutanNDIService.processAuthNatsResponse(payload);
	}

	private String extractThreadId(Map<String, Object> response) {
		Object threadIdObj = response.get("proofRequestThreadId");
		if (threadIdObj == null) {
			Object data = response.get("data");
			if (data instanceof Map<?, ?> dataMap) {
				threadIdObj = dataMap.get("proofRequestThreadId");
			}
		}
		return threadIdObj != null ? threadIdObj.toString() : null;
	}
}
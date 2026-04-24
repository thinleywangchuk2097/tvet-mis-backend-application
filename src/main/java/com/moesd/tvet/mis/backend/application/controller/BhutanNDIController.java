package com.moesd.tvet.mis.backend.application.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;
import com.moesd.tvet.mis.backend.application.service.BhutanNDIService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/auth/ndi")
public class BhutanNDIController {

	private final BhutanNDIService bhutanNDIService;

	@PostMapping("/get-token")
	public ResponseEntity<?> getNDIToken() {
		BhutanNDIToken token = bhutanNDIService.getValidToken();
		return ResponseEntity.ok(token);
	}

	@PostMapping("/create-proof-request")
	public ResponseEntity<?> createProofRequests() {

		BhutanNDIToken token = bhutanNDIService.getValidToken();

		if (token == null || token.getAccessToken() == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid token");
		}

		try {
			Map<String, Object> response = bhutanNDIService.createProofRequest(token);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create proof request");
		}
	}

	@PostMapping("/nats-response-submit")
	public ResponseEntity<?> bhutanNDINatsResponse(@RequestBody JsonNode payload) {
		return bhutanNDIService.processNatsResponse(payload);
	}
	
	
	@PostMapping("/nats-response-auth")
	public ResponseEntity<?> bhutanNDIAuthNatsResponse(@RequestBody JsonNode payload) {
		return bhutanNDIService.processAuthNatsResponse(payload);
	}

}

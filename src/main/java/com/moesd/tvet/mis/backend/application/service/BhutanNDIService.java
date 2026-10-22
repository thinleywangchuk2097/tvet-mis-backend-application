package com.moesd.tvet.mis.backend.application.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;
import tools.jackson.databind.JsonNode;

public interface BhutanNDIService {

	BhutanNDIToken getValidToken();

	Map<String, Object> createProofRequest(BhutanNDIToken token);

	ResponseEntity<?> processNatsResponse(JsonNode payload);

	ResponseEntity<?> processAuthNatsResponse(JsonNode payload);

	void connectToNats(String threadId, String sessionKey);

	Object getNatsAuthResult(String threadId);
}
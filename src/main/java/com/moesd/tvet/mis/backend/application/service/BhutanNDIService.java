package com.moesd.tvet.mis.backend.application.service;

import tools.jackson.databind.JsonNode;
import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface BhutanNDIService {

	BhutanNDIToken getValidToken();

	Map<String, Object> createProofRequest(BhutanNDIToken token);
	
	ResponseEntity<?> processNatsResponse(JsonNode payload);
	
	ResponseEntity<?> processAuthNatsResponse(JsonNode payload);
	
}
package com.moesd.tvet.mis.backend.application.service;

import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;

import java.util.Map;

public interface BhutanNDIService {

	BhutanNDIToken getValidToken();

	Map<String, Object> createProofRequest(BhutanNDIToken token);
}
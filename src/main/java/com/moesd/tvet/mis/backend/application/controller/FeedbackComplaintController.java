package com.moesd.tvet.mis.backend.application.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.dto.ComplaintActiondto;
import com.moesd.tvet.mis.backend.application.dto.FeedbackComplaintdto;
import com.moesd.tvet.mis.backend.application.dto.FileDownloaddto;
import com.moesd.tvet.mis.backend.application.service.FeedbackComplaintService;
import lombok.RequiredArgsConstructor;

/**
 * Feedback / Complain.
 * Public:     POST /api/v1/public/feedback-complaint            (submit, no login)
 * Logged in:  GET  /api/v1/feedback-complaint/feedback          (all feedback, no email / phone)
 * TVET QC:    GET  /api/v1/tvetqc/complaints                    (all complaints, full details)
 *             PUT  /api/v1/tvetqc/complaints/{id}               (status + action taken)
 *             GET  /api/v1/tvetqc/complaints/{id}/files/{fileId} (download an attachment)
 */
@RestController
@RequiredArgsConstructor
public class FeedbackComplaintController {

	private final FeedbackComplaintService service;

	@PostMapping("/api/v1/public/feedback-complaint")
	public ResponseEntity<Map<String, Object>> submit(@RequestBody FeedbackComplaintdto request) {
		return ResponseEntity.ok(service.submit(request));
	}

	// any authenticated user (SecurityConfiguration: anyRequest().authenticated())
	@GetMapping("/api/v1/feedback-complaint/feedback")
	public ResponseEntity<List<Map<String, Object>>> feedback() {
		return ResponseEntity.ok(service.getFeedback());
	}

	@GetMapping("/api/v1/tvetqc/complaints")
	@PreAuthorize("hasAuthority('22')")
	public ResponseEntity<List<Map<String, Object>>> complaints() {
		return ResponseEntity.ok(service.getComplaints());
	}

	@PutMapping("/api/v1/tvetqc/complaints/{id}")
	@PreAuthorize("hasAuthority('22')")
	public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody ComplaintActiondto request,
			Authentication authentication) {
		return ResponseEntity.ok(service.updateComplaint(id, request, authentication.getName()));
	}

	@GetMapping("/api/v1/tvetqc/complaints/{id}/files/{fileId}")
	@PreAuthorize("hasAuthority('22')")
	public ResponseEntity<byte[]> download(@PathVariable Long id, @PathVariable Long fileId) {
		FileDownloaddto file = service.downloadAttachment(id, fileId);
		MediaType type;
		try {
			type = MediaType.parseMediaType(file.getContentType());
		} catch (Exception e) {
			type = MediaType.APPLICATION_OCTET_STREAM;
		}
		return ResponseEntity.ok()
				.contentType(type)
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(file.getFileName(), StandardCharsets.UTF_8).build().toString())
				.body(file.getFileContent());
	}
}

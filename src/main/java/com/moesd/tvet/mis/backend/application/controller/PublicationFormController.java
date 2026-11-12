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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.dto.FileDownloaddto;
import com.moesd.tvet.mis.backend.application.dto.PublicationFormdto;
import com.moesd.tvet.mis.backend.application.service.PublicationFormService;
import lombok.RequiredArgsConstructor;

/**
 * Publications and Forms.
 * Public:  GET /api/v1/public/publication-forms?category=PUBLICATION|FORM
 *          GET /api/v1/public/publication-forms/{id}/download
 * TVET QC: /api/v1/tvetqc/publication-forms (list, create, update, delete) - role 22 only.
 */
@RestController
@RequiredArgsConstructor
public class PublicationFormController {

	private final PublicationFormService service;

	// ── Public ──────────────────────────────────────────────────────────
	@GetMapping("/api/v1/public/publication-forms")
	public ResponseEntity<List<Map<String, Object>>> list(@RequestParam(required = false) String category) {
		return ResponseEntity.ok(service.getDocuments(category));
	}

	@GetMapping("/api/v1/public/publication-forms/{id}/download")
	public ResponseEntity<byte[]> download(@PathVariable Long id) {
		FileDownloaddto file = service.download(id);
		MediaType type;
		try {
			type = MediaType.parseMediaType(file.getContentType());
		} catch (Exception e) {
			type = MediaType.APPLICATION_OCTET_STREAM;
		}
		return ResponseEntity.ok()
				.contentType(type)
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(file.getFileName(), StandardCharsets.UTF_8)
								.build().toString())
				.body(file.getFileContent());
	}

	// ── TVET QC (role 22) ───────────────────────────────────────────────
	@GetMapping("/api/v1/tvetqc/publication-forms")
	@PreAuthorize("hasAuthority('22')")
	public ResponseEntity<List<Map<String, Object>>> manageList(@RequestParam(required = false) String category) {
		return ResponseEntity.ok(service.getDocuments(category));
	}

	@PostMapping("/api/v1/tvetqc/publication-forms")
	@PreAuthorize("hasAuthority('22')")
	public ResponseEntity<Map<String, Object>> create(@RequestBody PublicationFormdto request,
			Authentication authentication) {
		return ResponseEntity.ok(service.create(request, authentication.getName()));
	}

	@PutMapping("/api/v1/tvetqc/publication-forms/{id}")
	@PreAuthorize("hasAuthority('22')")
	public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody PublicationFormdto request,
			Authentication authentication) {
		return ResponseEntity.ok(service.update(id, request, authentication.getName()));
	}

	@DeleteMapping("/api/v1/tvetqc/publication-forms/{id}")
	@PreAuthorize("hasAuthority('22')")
	public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.ok(Map.of("message", "Deleted"));
	}
}

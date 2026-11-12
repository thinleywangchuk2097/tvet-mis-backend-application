package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.dto.PublicNoticedto;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.service.PublicNoticeService;
import lombok.RequiredArgsConstructor;

/**
 * Public notices for the home-page notice bar - TVET QC (role 22) only.
 * The public page reads them from GET /api/v1/public/notification/get-public.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tvetqc/public-notices")
@PreAuthorize("hasAuthority('22')")
public class PublicNoticeController {

	private final PublicNoticeService service;

	@GetMapping
	public ResponseEntity<List<Map<String, Object>>> list() {
		return ResponseEntity.ok(service.getNotices());
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> create(@RequestBody PublicNoticedto request, Authentication auth) {
		return ResponseEntity.ok(service.create(request, userId(auth)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody PublicNoticedto request,
			Authentication auth) {
		return ResponseEntity.ok(service.update(id, request, userId(auth)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.ok(Map.of("message", "Deleted"));
	}

	// numeric id of the logged-in user, for created_by / updated_by
	private static Integer userId(Authentication auth) {
		if (auth != null && auth.getPrincipal() instanceof User u && u.getId() != null) {
			return u.getId().intValue();
		}
		return null;
	}
}

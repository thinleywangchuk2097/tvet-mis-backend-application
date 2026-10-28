package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.dto.QualityStandardCheckListDto;
import com.moesd.tvet.mis.backend.application.service.QualityStandardCheckListService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/quality_standard_check_list")
public class QualityStandardCheckListController {

	private final QualityStandardCheckListService qualityStandardCheckListService;

	/** Services that have a checklist: [{ service_id, service_name }] */
	@GetMapping("/get-services")
	public ResponseEntity<List<ObjectNode>> getServices() {
		return ResponseEntity.ok(qualityStandardCheckListService.getServices());
	}

	/** All items (active and inactive) of a service, e.g. 55 = CMFG monitoring. */
	@GetMapping("/get-by-service/{serviceId}")
	public ResponseEntity<?> getByService(@PathVariable Integer serviceId) {
		return qualityStandardCheckListService.getByService(serviceId);
	}

	/** Create (id null) or update a parent / sub parent / checklist item. */
	@PostMapping("/save")
	public ResponseEntity<?> save(@RequestBody QualityStandardCheckListDto request) {
		return qualityStandardCheckListService.save(request);
	}

	@PostMapping("/delete/{id}")
	public ResponseEntity<?> delete(@PathVariable Integer id) {
		return qualityStandardCheckListService.delete(id);
	}
}

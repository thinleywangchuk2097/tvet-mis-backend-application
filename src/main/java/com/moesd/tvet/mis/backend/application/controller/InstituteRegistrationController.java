package com.moesd.tvet.mis.backend.application.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteChangeRequestDto;
import com.moesd.tvet.mis.backend.application.dto.InstituteRegistrationdto;
import com.moesd.tvet.mis.backend.application.service.InstituteRegistrationService;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/institute-registration")
public class InstituteRegistrationController {

	private final InstituteRegistrationService instituteRegistrationService;
	private static final String MESSAGE_KEY = "Your application is already submitted for Registration";
	
	@PostMapping("/submit")
	public ResponseEntity<?> registerInstitute(@RequestBody InstituteRegistrationdto request) {
		return (instituteRegistrationService.registerInstitute(request));
	}

	@GetMapping("/get-application-status/{application_no}/{service_id}")
	public ResponseEntity<?> applicationExistOrNot(@PathVariable("application_no") String applicationNo,
			@PathVariable("service_id") String serviceId) {
		List<Tuple> instituteDetails = instituteRegistrationService.applicationExistOrNot(applicationNo, serviceId);

		if (!instituteDetails.isEmpty()) {
			Tuple tuple = instituteDetails.get(0);
			Integer proposalStatusId = tuple.get("status_id", Integer.class);
			Integer registrationStatusId = tuple.get("registration_status_id", Integer.class);

			Map<String, Object> response = new HashMap<>();

			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("proposalStatusId", proposalStatusId);
			dataMap.put("registrationStatusId", registrationStatusId);

			StatusResult result = resolveStatus(proposalStatusId, registrationStatusId, applicationNo);

			response.put("data", dataMap);
			response.put("message", result.message());
			response.put("alreadySubmitted", result.alreadySubmitted());

			return ResponseEntity.ok(response);
		}

		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", "No application found for application number: " + applicationNo);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	private StatusResult resolveStatus(Integer proposalStatusId, Integer registrationStatusId, String applicationNo) {
		if (proposalStatusId == null) {
			return new StatusResult(MESSAGE_KEY + applicationNo, true);
		}

		return switch (proposalStatusId) {
		case 55 -> new StatusResult("Proposal not yet approved: " + applicationNo, false);
		case 58 -> new StatusResult("Proposal rejected: " + applicationNo, false);
		case 57 -> resolveRegistrationStatus(registrationStatusId, applicationNo);
		default -> new StatusResult(MESSAGE_KEY + applicationNo, true);
		};
	}

	private StatusResult resolveRegistrationStatus(Integer registrationStatusId, String applicationNo) {
		if (registrationStatusId == null) {
			return new StatusResult("Proposal approved. Ready for registration: " + applicationNo, false);
		}

		return switch (registrationStatusId) {
		case 55, 56, 59, 62 -> new StatusResult("Registration in process for application: " + applicationNo, true);
		case 57 -> new StatusResult("Institute already registered for application: " + applicationNo, true);
		case 58 -> new StatusResult("Registration is rejected, Resubmit Again " + applicationNo, false);
		default -> new StatusResult(MESSAGE_KEY + applicationNo, true);
		};
	}

	private record StatusResult(String message, boolean alreadySubmitted) {
	}

	@GetMapping("/get-institute-application-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getInstituteRegistrationDetails(
			@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> instituteRegistrationDetails = instituteRegistrationService.getInstituteRegistrationDetails(applicationNo);
		return ResponseEntity.ok(instituteRegistrationDetails);
	}

	@GetMapping("/get-institute-details/{registration_no}")
	public ResponseEntity<List<ObjectNode>> getInstituteDetails(
			@PathVariable("registration_no") String registrationNo) {
		List<ObjectNode> instituteDetails = instituteRegistrationService.getInstituteDetails(registrationNo);
		return ResponseEntity.ok(instituteDetails);
	}

	@PostMapping("/verify-institute-registration")
	public ResponseEntity<?> verifyInstituteRegistration(@RequestBody InstituteRegistrationdto request) {
		return (instituteRegistrationService.verifyInstituteRegistration(request));
	}

	@GetMapping("/get-renewal-details/{registration_no}")
	public ResponseEntity<List<ObjectNode>> getInstituteRenewalDetails(
			@PathVariable("registration_no") String registrationNo) {
		List<ObjectNode> instituteRenewalDetails = instituteRegistrationService.getInstituteRenewalDetails(registrationNo);
		return ResponseEntity.ok(instituteRenewalDetails);
	}

	@GetMapping("/get-institute-change-details/{registration_no}")
	public ResponseEntity<List<ObjectNode>> getInstituteChangeDetails(
			@PathVariable("registration_no") String registrationNo) {
		List<ObjectNode> instituteChangeDetails = instituteRegistrationService.getInstituteChangeDetails(registrationNo);
		return ResponseEntity.ok(instituteChangeDetails);
	}

	@PostMapping("/change-institute")
	public ResponseEntity<?> instituteChange(@RequestBody InstituteChangeRequestDto request) {
		return (instituteRegistrationService.instituteChange(request));
	}

	@GetMapping("/get-change-institute/{application_no}")
	public ResponseEntity<List<ObjectNode>> getInstituteChangeByApplicationNo(
			@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> instituteChangeByApplicationNoDetails = instituteRegistrationService
				.getInstituteChangeByApplicationNo(applicationNo);
		return ResponseEntity.ok(instituteChangeByApplicationNoDetails);
	}

}

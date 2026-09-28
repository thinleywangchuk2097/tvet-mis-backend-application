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
			String message = "";
			boolean alreadySubmitted = false;

			// Create data map using HashMap (allows null values)
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("proposalStatusId", proposalStatusId);
			dataMap.put("registrationStatusId", registrationStatusId);

			// Status logic with null checks
			if (proposalStatusId == 55) {
				message = "Proposal not yet approved: " + applicationNo;
				alreadySubmitted = false;
			} else if (proposalStatusId == 58) {
				message = "Proposal rejected: " + applicationNo;
				alreadySubmitted = false;
			} else if (proposalStatusId == 57 && registrationStatusId != null) {
				// Only check registration status if it's not null
				if (registrationStatusId == 55 || registrationStatusId == 56 || registrationStatusId == 59
						|| registrationStatusId == 62) {
					message = "Registration in process for application: " + applicationNo;
					alreadySubmitted = true;
				} else if (registrationStatusId == 57) {
					message = "Institute already registered for application: " + applicationNo;
					alreadySubmitted = true;
				} else if (registrationStatusId == 58) {
					message = "Registration is rejected, Resubmit Again " + applicationNo;
					alreadySubmitted = false;
					// alreadySubmitted = true;
				} else {
					message = "Your application is already submitted for Registration: " + applicationNo;
					alreadySubmitted = true;
				}
			} else if (proposalStatusId == 57 && registrationStatusId == null) {
				// Proposal approved but no registration yet
				message = "Proposal approved. Ready for registration: " + applicationNo;
				alreadySubmitted = false;
			} else {
				message = "Your application is already submitted for Registration: " + applicationNo;
				alreadySubmitted = true;
			}

			response.put("data", dataMap);
			response.put("message", message);
			response.put("alreadySubmitted", alreadySubmitted);

			return ResponseEntity.ok(response);
		}

		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", "No application found for application number: " + applicationNo);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@GetMapping("/get-institute-application-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getInstituteRegistrationDetails(
			@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> instituteDetails = instituteRegistrationService.getInstituteRegistrationDetails(applicationNo);
		return ResponseEntity.ok(instituteDetails);
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
		List<ObjectNode> instituteDetails = instituteRegistrationService.getInstituteRenewalDetails(registrationNo);
		return ResponseEntity.ok(instituteDetails);
	}

	@GetMapping("/get-institute-change-details/{registration_no}")
	public ResponseEntity<List<ObjectNode>> getInstituteChangeDetails(
			@PathVariable("registration_no") String registrationNo) {
		List<ObjectNode> instituteDetails = instituteRegistrationService.getInstituteChangeDetails(registrationNo);
		return ResponseEntity.ok(instituteDetails);
	}

	@PostMapping("/change-institute")
	public ResponseEntity<?> instituteChange(@RequestBody InstituteChangeRequestDto request) {
		return (instituteRegistrationService.instituteChange(request));
	}

	@GetMapping("/get-change-institute/{application_no}")
	public ResponseEntity<List<ObjectNode>> getInstituteChangeByApplicationNo(
			@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> instituteDetails = instituteRegistrationService
				.getInstituteChangeByApplicationNo(applicationNo);
		return ResponseEntity.ok(instituteDetails);
	}

}

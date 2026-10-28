package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramAnnouncementDto;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramTrainerAppliedDto;
import com.moesd.tvet.mis.backend.application.dto.TOTCertificateRequestDto;
import com.moesd.tvet.mis.backend.application.dto.TOTCompleteApplicationDto;
import com.moesd.tvet.mis.backend.application.dto.TOTModuleMarksDto;
import com.moesd.tvet.mis.backend.application.dto.TOTTrainerSelectionDto;
import com.moesd.tvet.mis.backend.application.dto.TotProgramDto;
import com.moesd.tvet.mis.backend.application.service.TotService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/tot")
public class TotController {
	
	private final TotService totService;
	
	@PostMapping("/submit-tot-program")
	public ResponseEntity<?> submitTOTProgram(@RequestBody TotProgramDto request) {
		return (totService.submitTOTProgram(request));
	}
	
	@GetMapping("/get-tot-programs")
	public ResponseEntity<List<ObjectNode>> getToTPrograms() {
		List<ObjectNode> totProgramsDetails = totService.getToTPrograms();
		return ResponseEntity.ok(totProgramsDetails);
	}
	
	@PostMapping("/delete-tot-program/{id}")
	public ResponseEntity<?> deleteToTPrograms(@PathVariable Long id) {
		return totService.deleteToTPrograms(id);
	}
	
	
	@PostMapping("/submit-tot-announcement")
	public ResponseEntity<?> submitTOTProgramAnnouncement(@RequestBody TOTProgramAnnouncementDto request) {
		return (totService.submitTOTProgramAnnouncement(request));
	}
	
	@GetMapping("/get-tot-announcements")
	public ResponseEntity<List<ObjectNode>> getToTProgramsAnnouncement() {
		List<ObjectNode> totProgramsAnnouncementDetails = totService.getToTProgramsAnnouncement();
		return ResponseEntity.ok(totProgramsAnnouncementDetails);
	}
	
	@PostMapping("/delete-tot-announcement/{id}")
	public ResponseEntity<?> deleteToTProgramsAnnouncement(@PathVariable Long id) {
		return totService.deleteToTProgramsAnnouncement(id);
	}
	
	@PostMapping("/apply-trainer-to-tot-program")
	public ResponseEntity<?> applyTrainerToTOTProgram( @RequestBody List<TOTProgramTrainerAppliedDto> request) {
		return (totService.applyTrainerToTOTProgram(request));
	}
	
	@GetMapping("/get-trainer-applied/{applicationNo}")
	public ResponseEntity<List<ObjectNode>> getTrainerApplied(@PathVariable String applicationNo) {
		List<ObjectNode> totProgramsDetails = totService.getTrainerApplied(applicationNo);
		return ResponseEntity.ok(totProgramsDetails);
	}

	@PostMapping("/save-trainer-selection")
	public ResponseEntity<?> saveTrainerSelection(@RequestBody TOTTrainerSelectionDto request) {
		return totService.saveTrainerSelection(request);
	}

	@PostMapping("/save-module-marks")
	public ResponseEntity<?> saveModuleMarks(@RequestBody TOTModuleMarksDto request) {
		return totService.saveModuleMarks(request);
	}

	@GetMapping("/get-trainer-marks/{applicationNo}")
	public ResponseEntity<?> getTrainerMarks(@PathVariable String applicationNo) {
		return totService.getTrainerMarks(applicationNo);
	}

	@PostMapping("/generate-certificates")
	public ResponseEntity<?> generateCertificates(@RequestBody TOTCertificateRequestDto request) {
		return totService.generateCertificates(request);
	}

	@PostMapping("/complete-application")
	public ResponseEntity<?> completeApplication(@RequestBody TOTCompleteApplicationDto request) {
		return totService.completeApplication(request);
	}

	@GetMapping("/get-institute-applied/{instituteId}")
	public ResponseEntity<?> getInstituteApplied(@PathVariable Integer instituteId) {
		return totService.getInstituteApplied(instituteId);
	}

	/** TOT programmes with issued mark sheets (instituteId optional = only that institute's trainers). */
	@GetMapping("/get-certified-programs")
	public ResponseEntity<List<ObjectNode>> getCertifiedPrograms(
			@RequestParam(required = false) Integer instituteId) {
		return ResponseEntity.ok(totService.getCertifiedPrograms(instituteId));
	}

	/** Read-only mark sheet data for every certified trainer of an application. */
	@GetMapping("/get-certificates/{applicationNo}")
	public ResponseEntity<?> getCertificates(@PathVariable String applicationNo,
			@RequestParam(required = false) Integer instituteId) {
		return totService.getCertificates(applicationNo, instituteId);
	}

}

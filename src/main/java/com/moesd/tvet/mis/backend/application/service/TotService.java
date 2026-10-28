package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramAnnouncementDto;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramTrainerAppliedDto;
import com.moesd.tvet.mis.backend.application.dto.TOTCertificateRequestDto;
import com.moesd.tvet.mis.backend.application.dto.TOTCompleteApplicationDto;
import com.moesd.tvet.mis.backend.application.dto.TOTModuleMarksDto;
import com.moesd.tvet.mis.backend.application.dto.TOTTrainerSelectionDto;
import com.moesd.tvet.mis.backend.application.dto.TotProgramDto;

public interface TotService {

	ResponseEntity<?> submitTOTProgram(TotProgramDto request);

	List<ObjectNode> getToTPrograms();

	ResponseEntity<?> deleteToTPrograms(Long id);

	ResponseEntity<?> submitTOTProgramAnnouncement(TOTProgramAnnouncementDto request);

	List<ObjectNode> getToTProgramsAnnouncement();

	ResponseEntity<?> deleteToTProgramsAnnouncement(Long id);

	ResponseEntity<?> applyTrainerToTOTProgram(@RequestBody List<TOTProgramTrainerAppliedDto> request);
	
	List<ObjectNode> getTrainerApplied(String applicationNo);

	ResponseEntity<?> saveTrainerSelection(TOTTrainerSelectionDto request);

	ResponseEntity<?> saveModuleMarks(TOTModuleMarksDto request);

	ResponseEntity<?> getTrainerMarks(String applicationNo);

	ResponseEntity<?> generateCertificates(TOTCertificateRequestDto request);

	ResponseEntity<?> completeApplication(TOTCompleteApplicationDto request);

	ResponseEntity<?> getInstituteApplied(Integer instituteId);

	List<ObjectNode> getCertifiedPrograms(Integer instituteId);

	ResponseEntity<?> getCertificates(String applicationNo, Integer instituteId);
}

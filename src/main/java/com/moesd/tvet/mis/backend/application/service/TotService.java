package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramAnnouncementDto;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramTrainerAppliedDto;
import com.moesd.tvet.mis.backend.application.dto.TotProgramDto;

public interface TotService {

	ResponseEntity<?> submitTOTProgram(TotProgramDto request);

	List<ObjectNode> getToTPrograms();

	ResponseEntity<?> deleteToTPrograms(Long id);

	ResponseEntity<?> submitTOTProgramAnnouncement(TOTProgramAnnouncementDto request);

	List<ObjectNode> getToTProgramsAnnouncement();

	ResponseEntity<?> deleteToTProgramsAnnouncement(Long id);

	ResponseEntity<?> applyTrainerToTOTProgram(@RequestBody List<TOTProgramTrainerAppliedDto> request);
}

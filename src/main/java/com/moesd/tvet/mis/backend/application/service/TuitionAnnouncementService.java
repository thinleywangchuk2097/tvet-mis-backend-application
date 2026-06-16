package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TuitionAnnouncementDto;


public interface TuitionAnnouncementService {
	
	ResponseEntity<?> submitTuitionAnnouncement(TuitionAnnouncementDto request);

	List<ObjectNode> getAllTuitionAnnouncement(Integer institute_id);

	ResponseEntity<?> updateTuitionAnnouncement(TuitionAnnouncementDto request);

	ResponseEntity<?> softDeleteTuitionAnnouncement(Long tuitionId);
}

package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Totdto;

public interface TotService {
	ResponseEntity<?> submitCourseAnnouncement(Totdto request);
	
	List<ObjectNode> getCourseDetailsAnnouncementByUserId();
}

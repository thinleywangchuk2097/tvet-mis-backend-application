package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CurriculumDevelopmentdto;
import com.moesd.tvet.mis.backend.application.model.CurriculumDevelopment;



public interface CurriculumDevelopmentService {
	
	ResponseEntity<?> submitCurriculum(CurriculumDevelopmentdto request);
	
	List<ObjectNode> getCurriculumDetails(String application_no);
	
	List<ObjectNode> getCurriculumDetailsByUserId(String user_id);
	
	List<ObjectNode> getApprovedCurriculumDataByUserId(String user_id,String curriculum_type);
	
	CurriculumDevelopment getCurriculumById(Long id);
	
	ResponseEntity<?> verifyCurriculumDevelopment(CurriculumDevelopmentdto request);
}

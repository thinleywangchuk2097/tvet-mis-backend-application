package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CurriculumDevelopmentdto;
import com.moesd.tvet.mis.backend.application.model.CurriculumDevelopment;



public interface CurriculumDevelopmentService {
	
	ResponseEntity<?> submitCurriculum(CurriculumDevelopmentdto request);
	
	List<ObjectNode> getCurriculumDetails(String applicationNo);
	
	List<ObjectNode> getCurriculumDetailsByUserId(String userId);
	
	List<ObjectNode> getApprovedCurriculumDataByUserId(String userId,String curriculumType);
	
	CurriculumDevelopment getCurriculumById(Long id);
	
	ResponseEntity<?> verifyCurriculumDevelopment(CurriculumDevelopmentdto request);
}

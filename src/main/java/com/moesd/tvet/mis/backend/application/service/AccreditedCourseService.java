package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AccreditedCoursedto;

public interface AccreditedCourseService {

	ResponseEntity<?> registerAccreditedCourse(AccreditedCoursedto request);

	List<ObjectNode> getAccreditedCourseByApplicationNo(String application_no);

	List<ObjectNode> getAccreditedCourseDetailsByUserId(String user_id);

	List<ObjectNode> getAccreditedCourseByInstituteId(String institute_id);

	List<ObjectNode> getAccreditedApprovedCourseByUserId(String user_id);

	ResponseEntity<?> verifyAccreditedCourse(AccreditedCoursedto request);

	List<ObjectNode> curriculumExist(Long curriculumId, String registration_no);

	List<ObjectNode> getListSelectedBQFTraineeForExcel(String application_no);

	List<ObjectNode> getListPassTraineeForCertificatePrinting(String applicationNo, Integer instituteId,
			Integer serviceId, Integer certificationLevelId, Integer programmeId);
	
	List<ObjectNode> getServicesAssessementResult();
	
	List<ObjectNode> getProgrammesCertification(Integer institute_id,Integer service_id,Integer certification_level_id);

}

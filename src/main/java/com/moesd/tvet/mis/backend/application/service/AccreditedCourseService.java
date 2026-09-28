package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AccreditedCoursedto;

public interface AccreditedCourseService {

	ResponseEntity<?> registerAccreditedCourse(AccreditedCoursedto request);

	List<ObjectNode> getAccreditedCourseByApplicationNo(String applicationNo);

	List<ObjectNode> getAccreditedCourseDetailsByUserId(String userId);

	List<ObjectNode> getAccreditedCourseByInstituteId(String instituteId);

	List<ObjectNode> getAccreditedApprovedCourseByUserId(String userId);

	ResponseEntity<?> verifyAccreditedCourse(AccreditedCoursedto request);

	List<ObjectNode> curriculumExist(Long curriculumId, String registrationNo);

	List<ObjectNode> getListSelectedBQFTraineeForExcel(String applicationNo);

	List<ObjectNode> getListPassTraineeForCertificatePrinting(String applicationNo, Integer instituteId,
			Integer serviceId, Integer certificationLevelId, Integer programmeId);

	List<ObjectNode> getServicesAssessementResult();

	List<ObjectNode> getProgrammesCertification(Integer instituteId, Integer serviceId, Integer certificationLevelId);

}

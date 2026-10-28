package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.SelfAssessmentReportdto;

public interface SelfAssessmentReportService {

	ResponseEntity<?> submitSelfAssessmentReport(SelfAssessmentReportdto request);

	ResponseEntity<?> saveDraftSelfAssessmentReport(SelfAssessmentReportdto request);

	ResponseEntity<?> verifySelfAssessmentReport(SelfAssessmentReportdto request);

	List<ObjectNode> getSelfAssessmentReportByApplicationNo(String applicationNo);

	List<ObjectNode> getSelfAssessmentReportsByUserId(String userId);

	Map<String, Object> getBasicInformationByUserId(String userId);

	Map<String, Object> getBasicInformationByInstituteId(Integer instituteId);

	List<ObjectNode> getActiveSelfAssessmentReport(Integer instituteId);
}

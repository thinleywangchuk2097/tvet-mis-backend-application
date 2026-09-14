package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import tools.jackson.databind.node.ObjectNode;

public interface CertificateService {
	List<ObjectNode> getAssessmentInstitute();

	List<ObjectNode> getAssessmentCourse(Integer sectorId);
}

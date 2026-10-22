package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.repository.CertificateRepository;
import com.moesd.tvet.mis.backend.application.service.CertificateService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
	private final CertificateRepository certificateRepository;
	private final ObjectToJson objectTojson;
	
	@Override
	public List<ObjectNode> getAssessmentInstitute() {
	    return objectTojson._toJson(certificateRepository.getAssessmentInstitute());
	}

	@Override
	public List<ObjectNode> getAssessmentCourse(Integer instituteId) {
	    return objectTojson._toJson(certificateRepository.getAssessmentCourse(instituteId));
	}

}

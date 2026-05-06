package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.repository.CertificateRepository;
import com.moesd.tvet.mis.backend.application.service.CertificateService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
	private final CertificateRepository certificateRepository;
	private final ObjectToJson objectTojson;
	@Override
	public List<ObjectNode> getAssessmentInstitute() {
		List<Tuple> resultList= certificateRepository.getAssessmentInstitute();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getAssessmentCourse(Integer instituteId) {
		List<Tuple> resultList= certificateRepository.getAssessmentCourse(instituteId);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

}

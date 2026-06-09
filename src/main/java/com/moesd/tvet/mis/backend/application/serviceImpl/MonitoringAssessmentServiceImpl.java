package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.repository.MonitoringAssessmentRepository;
import com.moesd.tvet.mis.backend.application.service.MonitoringAssessmentService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonitoringAssessmentServiceImpl implements MonitoringAssessmentService{
	
	private final MonitoringAssessmentRepository monitoringAssessmentRepository;
	private final ObjectToJson objectTojson;
	
	@Override
	public List<ObjectNode> getInstituteTypeDropdown() {
		List<Tuple> resultList = monitoringAssessmentRepository.getInstituteTypeDropdown();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getInstituteDropdown(String service_id) {
		List<Tuple> resultList = monitoringAssessmentRepository.getInstituteDropdown(service_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

}

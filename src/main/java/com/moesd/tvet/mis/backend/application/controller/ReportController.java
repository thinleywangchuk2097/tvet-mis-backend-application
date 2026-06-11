package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.service.ReportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/report")
public class ReportController {

	private final ReportService reportService;

	@GetMapping("/get-course-data")
	public List<Map<String, Object>> getcourseData() {
		return reportService.getcourseData();
	}
	
	@GetMapping("/get-course-service-type")
	public List<Map<String, Object>> courseServiceType() {
		return reportService.courseServiceType();
	}
	
	@GetMapping("/get-institutes")
	public List<Map<String, Object>> getAllInstitutes() {
		return reportService.getAllInstitutes();
	}
	
	@GetMapping("/get-institutes-proposal-type")
	public List<Map<String, Object>> getInstitutesProposalType() {
		return reportService.getInstitutesProposalType();
	}
	
	@GetMapping("/get-institutes-registration-type")
	public List<Map<String, Object>> getInstituteRegistrationType() {
		return reportService.getInstituteRegistrationType();
	}
	
	@GetMapping("/get-institutes-proposal-details")
	public List<Map<String, Object>> getInstitutesProposalDetails() {
		return reportService.getInstitutesProposalDetails();
	}
	
	@GetMapping("/get-institutes-registration-details")
	public List<Map<String, Object>> getInstitutesRegistrationDetails() {
		return reportService.getInstitutesRegistrationDetails();
	}
	
	@GetMapping("/get-institutes-trainees-details")
	public List<Map<String, Object>> getInstitutesTraineesDetails() {
		return reportService.getInstitutesTraineesDetails();
	}
}

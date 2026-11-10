package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@GetMapping("/get-registration-applicant-details/{registration_type}/{application_no}")
	public List<Map<String, Object>> getRegistrationDetailsForCertificate(
			@PathVariable("registration_type") String registrationType,
			@PathVariable("application_no") String applicationNo) {
		return reportService.getRegistrationDetailsForCertificate(registrationType, applicationNo);
	}
	
	@GetMapping("/get-trainee-applicant-details/{registration_no}/{application_no}")
	public List<Map<String, Object>> getTraineesDetailsForCertificate(
			@PathVariable("registration_no") String registrationNo,
			@PathVariable("application_no") String applicationNo) {
		return reportService.getTraineesDetailsForCertificate(registrationNo, applicationNo);
	}
	
	// Certificate printing: competent trainees of all institutes
	@GetMapping("/get-all-trainee-certificate-details")
	public List<Map<String, Object>> getAllTraineesDetailsForCertificate() {
		return reportService.getAllTraineesDetailsForCertificate();
	}

	@GetMapping("/get-accredited-programme-details/{registration_no}/{application_no}")
	public List<Map<String, Object>> getAccreditedProgrammeDetailsForCertificate(
			@PathVariable("registration_no") String registrationNo,
			@PathVariable("application_no") String applicationNo) {
		return reportService.getAccreditedProgrammeDetailsForCertificate(registrationNo, applicationNo);
	}


	// Assessment report (Excel) for the logged-in institute
	@GetMapping("/get-institute-assessment-report/{registration_no}")
	public List<Map<String, Object>> getInstituteAssessmentReport(
			@PathVariable("registration_no") String registrationNo) {
		return reportService.getInstituteAssessmentReport(registrationNo);
	}

	// Assessor / Accreditor / QMS Auditor registrations report (logged-in users only)
	@GetMapping("/get-assessor-accreditor-auditor-report")
	public List<Map<String, Object>> getAssessorAccreditorAuditorReport() {
		return reportService.getAssessorAccreditorAuditorReport();
	}

	// BQF / RPL / Non-BQF programmes report (AccreditedCourse + NonAccreditedCourse)
	@GetMapping("/get-programmes-report")
	public List<Map<String, Object>> getProgrammesReport() {
		return reportService.getProgrammesReport();
	}
}

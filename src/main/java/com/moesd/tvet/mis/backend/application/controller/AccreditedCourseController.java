package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AccreditedCoursedto;
import com.moesd.tvet.mis.backend.application.service.AccreditedCourseService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/accredited-course")
public class AccreditedCourseController {

	private final AccreditedCourseService instituteAccreditedCourseService;

	@PostMapping("/submit")
	public ResponseEntity<?> registerAccreditedCourse(@RequestBody AccreditedCoursedto request) {
		return (instituteAccreditedCourseService.registerAccreditedCourse(request));
	}

	@GetMapping("/get-course-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getAccreditedCourseByApplicationNo(
			@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> data = instituteAccreditedCourseService.getAccreditedCourseByApplicationNo(applicationNo);
		return ResponseEntity.ok(data);
	}

	@GetMapping("/get-application-details/{user_id}")
	public ResponseEntity<List<ObjectNode>> getAccreditedCourseDetailsByUserId(@PathVariable("user_id") String userId) {
		List<ObjectNode> getAccreditedCourseDetailsByUserId = instituteAccreditedCourseService.getAccreditedCourseDetailsByUserId(userId);
		return ResponseEntity.ok(getAccreditedCourseDetailsByUserId);
	}

	@GetMapping("/get-accredited-course/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getAccreditedCourseByInstituteId(
			@PathVariable("institute_id") String instituteId) {
		List<ObjectNode> getAccreditedCourseByInstituteDetails = instituteAccreditedCourseService.getAccreditedCourseByInstituteId(instituteId);
		return ResponseEntity.ok(getAccreditedCourseByInstituteDetails);
	}

	@GetMapping("/get-accredited-approved-course-details/{user_id}")
	public ResponseEntity<List<ObjectNode>> getAccreditedApprovedCourseByUserId(
			@PathVariable("user_id") String userId) {
		List<ObjectNode> getAccreditedApprovedCourseDetails = instituteAccreditedCourseService.getAccreditedApprovedCourseByUserId(userId);
		return ResponseEntity.ok(getAccreditedApprovedCourseDetails);
	}

	@PostMapping("/verify-accredited-course")
	public ResponseEntity<?> verifyAccreditedCourse(@RequestBody AccreditedCoursedto request) {
		return (instituteAccreditedCourseService.verifyAccreditedCourse(request));
	}

	@GetMapping("/get-curriculum-exist/{curriculumId}/{registration_no}")
	public ResponseEntity<List<ObjectNode>> curriculumExist(@PathVariable Long curriculumId,
			@PathVariable("registration_no") String registrationNo) {
		List<ObjectNode> getCurriculumExist = instituteAccreditedCourseService.curriculumExist(curriculumId, registrationNo);
		return ResponseEntity.ok(getCurriculumExist);
	}

	@GetMapping("/get-list-selected-Trainee-bqf/{application_no}")
	public ResponseEntity<List<ObjectNode>> getListSelectedBQFTraineeForExcel(
			@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> details = instituteAccreditedCourseService.getListSelectedBQFTraineeForExcel(applicationNo);
		return ResponseEntity.ok(details);
	}

	@GetMapping("/get-passed-trainee-certificate-printing")
	public ResponseEntity<List<ObjectNode>> getListPassTraineeForCertificatePrinting(
			@RequestParam(required = false) String applicationNo, @RequestParam(required = false) Integer instituteId,
			@RequestParam(required = false) Integer serviceId,
			@RequestParam(required = false) Integer certificationLevelId,
			@RequestParam(required = false) Integer programmeId

	) {

		List<ObjectNode> result = instituteAccreditedCourseService.getListPassTraineeForCertificatePrinting(
				applicationNo, instituteId, serviceId, certificationLevelId, programmeId

		);

		return ResponseEntity.ok(result);
	}

	@GetMapping("/get-services-assessement-result")
	public ResponseEntity<List<ObjectNode>> getServicesAssessementResult() {
		List<ObjectNode> getServicesAssessementResult = instituteAccreditedCourseService.getServicesAssessementResult();
		return ResponseEntity.ok(getServicesAssessementResult);
	}

	@GetMapping("/get-programmes-certification/{institute_id}/{service_id}/{certification_level_id}")
	public ResponseEntity<List<ObjectNode>> getProgrammesCertification(
			@PathVariable("institute_id") Integer instituteId, @PathVariable("service_id") Integer serviceId,
			@PathVariable("certification_level_id") Integer certificationLevelId) {
		List<ObjectNode> getProgrammesCertificationDetails = instituteAccreditedCourseService.getProgrammesCertification(instituteId, serviceId,
				certificationLevelId);
		return ResponseEntity.ok(getProgrammesCertificationDetails);
	}

}

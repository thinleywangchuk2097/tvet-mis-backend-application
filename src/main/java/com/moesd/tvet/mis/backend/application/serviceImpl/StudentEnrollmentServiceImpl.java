package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.StudentEnrollmentDto;
import com.moesd.tvet.mis.backend.application.model.StudentEnrollment;
import com.moesd.tvet.mis.backend.application.model.Subject;
import com.moesd.tvet.mis.backend.application.repository.StudentEnrollmentRepository;
import com.moesd.tvet.mis.backend.application.repository.SubjectRepository;
import com.moesd.tvet.mis.backend.application.service.StudentEnrollmentService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentEnrollmentServiceImpl implements StudentEnrollmentService {

	private static final int SUBJECT_APPROVED = 57;

	private final StudentEnrollmentRepository studentEnrollmentRepository;
	private final SubjectRepository subjectRepository;
	private final ObjectToJson objectTojson;

	@Override
	public ResponseEntity<?> submitEnrollment(StudentEnrollmentDto request) {
		try {
			String invalid = validate(request);
			if (invalid != null)
				return error(HttpStatus.BAD_REQUEST, invalid);

			StudentEnrollment saved = studentEnrollmentRepository.save(StudentEnrollment.builder()
					.name(request.getName().trim())
					.genderId(request.getGenderId())
					.schoolLevelId(request.getSchoolLevelId())
					.subjectId(request.getSubjectId())
					.enrollmentYear(request.getEnrollmentYear())
					.instituteId(request.getInstituteId())
					.statusId(1)
					.createdBy(request.getCreatedBy())
					.createdAt(new Date())
					.build());
			return success(HttpStatus.CREATED, "Student enrolled successfully", saved.getId());
		} catch (Exception e) {
			log.error("Error enrolling student", e);
			return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to enrol student");
		}
	}

	@Override
	public List<ObjectNode> getAllActiveEnrollments(Integer instituteId) {
		return objectTojson._toJson(studentEnrollmentRepository.getAllActiveEnrollments(instituteId));
	}

	@Override
	public ResponseEntity<?> updateEnrollment(StudentEnrollmentDto request) {
		try {
			if (request.getId() == null)
				return error(HttpStatus.BAD_REQUEST, "id is required");
			StudentEnrollment existing = studentEnrollmentRepository.findById(request.getId()).orElse(null);
			if (existing == null)
				return error(HttpStatus.NOT_FOUND, "Enrolment not found with ID: " + request.getId());

			// the subject may stay as it is even if it is no longer the latest approved one
			boolean subjectChanged = !request.getSubjectId().equals(existing.getSubjectId());
			String invalid = subjectChanged ? validate(request) : validateFields(request);
			if (invalid != null)
				return error(HttpStatus.BAD_REQUEST, invalid);

			existing.setName(request.getName().trim());
			existing.setGenderId(request.getGenderId());
			existing.setSchoolLevelId(request.getSchoolLevelId());
			existing.setSubjectId(request.getSubjectId());
			existing.setEnrollmentYear(request.getEnrollmentYear());
			existing.setUpdatedBy(request.getUpdatedBy());
			existing.setUpdatedAt(new Date());
			studentEnrollmentRepository.save(existing);
			return success(HttpStatus.OK, "Enrolment updated successfully", existing.getId());
		} catch (Exception e) {
			log.error("Error updating enrolment", e);
			return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update enrolment");
		}
	}

	@Override
	public ResponseEntity<?> softDeleteEnrollment(Long id) {
		try {
			StudentEnrollment existing = studentEnrollmentRepository.findById(id).orElse(null);
			if (existing == null)
				return error(HttpStatus.NOT_FOUND, "Enrolment not found with ID: " + id);
			existing.setStatusId(0);
			existing.setUpdatedAt(new Date());
			studentEnrollmentRepository.save(existing);
			return success(HttpStatus.OK, "Enrolment deleted successfully", id);
		} catch (Exception e) {
			log.error("Error deleting enrolment", e);
			return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete enrolment");
		}
	}

	/** Required fields + the subject must be an approved, active subject of the same SES centre. */
	private String validate(StudentEnrollmentDto request) {
		String invalid = validateFields(request);
		if (invalid != null)
			return invalid;
		Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
		if (subject == null || !Integer.valueOf(1).equals(subject.getStatusId()))
			return "Subject not found";
		if (request.getInstituteId() != null && subject.getInstituteId() != null
				&& !request.getInstituteId().equals(subject.getInstituteId()))
			return "Subject does not belong to this SES centre";
		// no status = added before the subject workflow
		if (subject.getApplicationStatusId() != null && subject.getApplicationStatusId() != SUBJECT_APPROVED)
			return "Only approved subjects can be selected";
		return null;
	}

	private String validateFields(StudentEnrollmentDto request) {
		if (request.getName() == null || request.getName().isBlank())
			return "Name is required";
		if (request.getGenderId() == null)
			return "Gender is required";
		if (request.getSchoolLevelId() == null)
			return "School Level is required";
		if (request.getSubjectId() == null)
			return "Subject is required";
		if (request.getEnrollmentYear() == null)
			return "Year is required";
		return null;
	}

	private ResponseEntity<?> success(HttpStatus status, String message, Long id) {
		Map<String, Object> body = new HashMap<>();
		body.put("status", "SUCCESS");
		body.put("message", message);
		body.put("id", id);
		return ResponseEntity.status(status).body(body);
	}

	private ResponseEntity<?> error(HttpStatus status, String message) {
		Map<String, Object> body = new HashMap<>();
		body.put("status", "ERROR");
		body.put("message", message);
		return ResponseEntity.status(status).body(body);
	}
}

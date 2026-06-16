package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.StudentAddDto;
import com.moesd.tvet.mis.backend.application.model.AddStudent;
import com.moesd.tvet.mis.backend.application.model.StudentSubject;
import com.moesd.tvet.mis.backend.application.repository.AddStudentRepository;
import com.moesd.tvet.mis.backend.application.repository.StudentSubjectRepository;
import com.moesd.tvet.mis.backend.application.service.AddStudentService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddStudentServiceImpl implements AddStudentService{
	
	private final AddStudentRepository addStudentRepository;
	private final StudentSubjectRepository studentSubjectRepository;
	private final ObjectToJson objectTojson;
	
	@Override
	@Transactional
	public ResponseEntity<?> submitStudent(StudentAddDto request) {
		try {
			log.info("Submitting new student: {}", request.getFirstName() + " " + request.getLastName());
			
			// Create new student entity
			AddStudent student = AddStudent.builder()
				.citizenId(request.getCitizenId())
				.studentCode(request.getStudentCode())
				.firstName(request.getFirstName())
				.middleName(request.getMiddleName())
				.lastName(request.getLastName())
				.email(request.getEmail())
				.mobileNo(request.getMobileNo())
				.genderId(request.getGenderId())
				.dzongkhagId(request.getDzongkhagId())
				.exactLocation(request.getExactLocation())
				.emergencyContactName(request.getEmergencyContactName())
				.emergencyContactNo(request.getEmergencyContactNo())
				.enrollmentDate(request.getEnrollmentDate())
				.currentClass(request.getCurrentClass())
				.schoolName(request.getSchoolName())
				.schoolExactLocation(request.getSchoolExactLocation())
				.statusId(request.getStatusId() != null ? request.getStatusId() : 1)
				.instituteId(request.getInstituteId())
				.createdBy(request.getCreatedBy())
				.createdAt(new Date())
				.build();
			
			// Save student to database
			AddStudent savedStudent = addStudentRepository.save(student);
			
			// Save subjects if present
			if (request.getSubjects() != null && !request.getSubjects().isEmpty()) {
				List<StudentSubject> studentSubjects = request.getSubjects().stream()
					.map(subjectDto -> StudentSubject.builder()
						.subjectId(subjectDto.getSubjectId())
						.tutorId(subjectDto.getTutorId())
						.student(savedStudent)
						.build())
					.collect(Collectors.toList());
				
				studentSubjectRepository.saveAll(studentSubjects);
			}
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Student submitted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", savedStudent);
			
			log.info("Student submitted successfully with ID: {}", savedStudent.getId());
			return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error submitting student: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to submit student: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public List<ObjectNode> getAllActiveStudents(Integer institute_id) {
		List<Tuple> resultList = addStudentRepository.getAllActiveStudents(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	@Transactional
	public ResponseEntity<?> updateStudent(StudentAddDto request) {
		try {
			log.info("Updating student with ID: {}", request.getId());
			
			// Check if student exists
			Optional<AddStudent> existingStudentOpt = addStudentRepository.findById(request.getId());
			if (existingStudentOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Student not found with ID: " + request.getId());
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			AddStudent existingStudent = existingStudentOpt.get();
			
			// Update student entity
			existingStudent.setCitizenId(request.getCitizenId());
			existingStudent.setFirstName(request.getFirstName());
			existingStudent.setMiddleName(request.getMiddleName());
			existingStudent.setStudentCode(request.getStudentCode());
			existingStudent.setDateOfBirth(request.getDateOfBirth());
			existingStudent.setLastName(request.getLastName());
			existingStudent.setEmail(request.getEmail());
			existingStudent.setMobileNo(request.getMobileNo());
			existingStudent.setGenderId(request.getGenderId());
			existingStudent.setDzongkhagId(request.getDzongkhagId());
			existingStudent.setExactLocation(request.getExactLocation());
			existingStudent.setEmergencyContactName(request.getEmergencyContactName());
			existingStudent.setEmergencyContactNo(request.getEmergencyContactNo());
			existingStudent.setEnrollmentDate(request.getEnrollmentDate());
			existingStudent.setCurrentClass(request.getCurrentClass());
			existingStudent.setSchoolName(request.getSchoolName());
			existingStudent.setSchoolExactLocation(request.getSchoolExactLocation());
			existingStudent.setStatusId(request.getStatusId());
			existingStudent.setInstituteId(request.getInstituteId());
			existingStudent.setUpdatedBy(request.getUpdatedBy());
			existingStudent.setUpdatedAt(new Date());
			
			// Save updated student
			AddStudent updatedStudent = addStudentRepository.save(existingStudent);
			
			// Update subjects - delete existing and add new ones
			if (request.getSubjects() != null) {
				// Delete existing subjects
				studentSubjectRepository.deleteByStudentId(updatedStudent.getId());
				
				// Save new subjects
				if (!request.getSubjects().isEmpty()) {
					List<StudentSubject> studentSubjects = request.getSubjects().stream()
						.map(subjectDto -> StudentSubject.builder()
							.subjectId(subjectDto.getSubjectId())
							.tutorId(subjectDto.getTutorId())
							.student(updatedStudent)
							.build())
						.collect(Collectors.toList());
					
					studentSubjectRepository.saveAll(studentSubjects);
				}
			}
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Student updated successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", updatedStudent);
			
			log.info("Student updated successfully with ID: {}", updatedStudent.getId());
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error updating student: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to update student: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> softDeleteStudent(Long studentId) {
		try {
			log.info("Soft deleting student with ID: {}", studentId);
			
			// Check if student exists
			Optional<AddStudent> existingStudentOpt = addStudentRepository.findById(studentId);
			if (existingStudentOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Student not found with ID: " + studentId);
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			// Soft delete by setting statusId to 2 (Inactive)
			AddStudent student = existingStudentOpt.get();
			student.setStatusId(2);
			student.setUpdatedAt(new Date());
			
			// Save to database
			AddStudent deletedStudent = addStudentRepository.save(student);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Student deleted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", deletedStudent);
			
			log.info("Student soft deleted successfully with ID: {}", studentId);
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error deleting student: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to delete student: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}
}
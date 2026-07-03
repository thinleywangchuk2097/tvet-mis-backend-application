package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.OJTAgrementDto;
import com.moesd.tvet.mis.backend.application.dto.OJTCompanyDto;
import com.moesd.tvet.mis.backend.application.dto.OJTTraineeDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.OJTCompany;
import com.moesd.tvet.mis.backend.application.model.OJTCompanyAgreement;
import com.moesd.tvet.mis.backend.application.model.OJTTraineeDetails;
import com.moesd.tvet.mis.backend.application.repository.OJTCompanyAgreementRepository;
import com.moesd.tvet.mis.backend.application.repository.OJTCompanyRepository;
import com.moesd.tvet.mis.backend.application.repository.OJTTraineeDetailsRepository;
import com.moesd.tvet.mis.backend.application.service.OJTTraineeService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OJTTraineeServiceImpl implements OJTTraineeService{
	
	private final OJTCompanyRepository oJTCompanyRepository;
	private final OJTCompanyAgreementRepository oJTCompanyAgreementRepository;
	private final OJTTraineeDetailsRepository oJTTraineeDetailsRepository;
	private final ObjectToJson objectTojson;
	private final DocumentFileUploadService documentFileUploadService;
	
	@Override
	public ResponseEntity<?> submitOJTCompany(OJTCompanyDto request) {

		if (oJTCompanyRepository.existsByRegistrationNo(request.getRegistrationNo())) {
		    return ResponseEntity.status(HttpStatus.CONFLICT)
		            .body(Map.of(
		                    "status", "CONFLICT",
		                    "message", "Company already exists with Registration No: "
		                            + request.getRegistrationNo()
		            ));
		}

	    OJTCompany company = OJTCompany.builder()
	            .registrationNo(request.getRegistrationNo())
	            .companyName(request.getCompanyName())
	            .dzongkhagId(request.getDzongkhagId())
	            .instituteId(request.getInstituteId())
	            .contactPersonName(request.getContactPersonName())
	            .contactPersonMobileNo(request.getContactPersonMobileNo())
	            .contactPersonEmail(request.getContactPersonEmail())
	            .address(request.getAddress())
	            .description(request.getDescription())
	            .createdBy(request.getCreatedBy())
	            .updatedBy(request.getUpdatedBy())
	            .build();

	    OJTCompany savedCompany = oJTCompanyRepository.save(company);

	    return ResponseEntity.ok(savedCompany);
	}
	
	@Override
	public List<ObjectNode> getCompanyByInstituteId(String institute_id) {
		List<Tuple> result = oJTCompanyRepository.getCompanyByInstituteId(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(result);
		return DtlsJson;
		
	}
	
	@Override
	public ResponseEntity<?> submitOJTAgrement(OJTAgrementDto request) {

		if (oJTCompanyAgreementRepository.existsByAgreementTitle(request.getAgreementTitle())) {
		    return ResponseEntity.status(HttpStatus.CONFLICT)
		            .body(Map.of(
		                    "status", "CONFLICT",
		                    "message", "An agreement with the title '" + request.getAgreementTitle() + "' already exists."
		            ));
		}
		
		OJTCompany company = oJTCompanyRepository.findById(request.getCompanyId())
		        .orElseThrow(() -> new RecordNotFoundException(
		                "OJT Company not found with id: " + request.getCompanyId()));

		OJTCompanyAgreement agreement = OJTCompanyAgreement.builder()
		        .agreementTitle(request.getAgreementTitle())
		        .agreementDate(request.getAgreementDate())
		        .ojtcompany(company)
		        .instituteId(request.getInstituteId())
		        .startDate(request.getStartDate())
		        .endDate(request.getEndDate())
		        .totalTraineeNo(request.getTotalTraineeNo())
		        .superVisorName(request.getSuperVisorName())
		        .supervisorContactNo(request.getSupervisorContactNo())
		        .description(request.getDescription())
		        .createdBy(request.getCreatedBy())
		        .updatedBy(request.getUpdatedBy())
		        .build();

		OJTCompanyAgreement savedAgreement = oJTCompanyAgreementRepository.save(agreement);
	 // Documents
	 if (request.getDocuments() != null && request.getDocuments().length > 0) {
	 				documentFileUploadService.saveDocument(request.getDocuments(), null, "ojt",
	 						null, null, null);
	 }
	    return ResponseEntity.ok(savedAgreement);
	}
	
	@Override
	public List<ObjectNode> getAgreementByInstituteId(String institute_id) {
		List<Tuple> result = oJTCompanyAgreementRepository.getAgreementByInstituteId(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(result);
		return DtlsJson;
	}
	
	@Override
	public ResponseEntity<?> submitOJTTrainee(OJTTraineeDto request) {
		OJTCompanyAgreement agreement = oJTCompanyAgreementRepository.findById(request.getOjtAgreementId())
		        .orElseThrow(() -> new RecordNotFoundException(
		                "OJT Company not found with id: " + request.getOjtAgreementId()));
		
	    OJTTraineeDetails trainee = OJTTraineeDetails.builder()
	            .traineeCid(request.getTraineeCid())
	            .traineeName(request.getTraineeName())
	            .courseId(request.getCourseId())
	            .position(request.getPosition())
	            .ojtcompanyagreement(agreement)
	            .instituteId(request.getInstituteId())
	            .salary(request.getSalary())
	            .remarks(request.getRemarks())
	            .employmentStatusId(request.getEmploymentStatusId())
	            .createdBy(request.getCreatedBy())
	            .createdAt(request.getCreatedAt())
	            .updatedBy(request.getUpdatedBy())
	            .updatedAt(request.getUpdatedAt())
	            .build();

	    OJTTraineeDetails savedTrainee = oJTTraineeDetailsRepository.save(trainee);

	    return ResponseEntity.ok(Map.of(
	            "status", "SUCCESS",
	            "message", "Trainee saved successfully",
	            "data", savedTrainee
	    ));
	}

	@Override
	public List<ObjectNode> getTraineeByInstituteId(String institute_id) {
		List<Tuple> result = oJTTraineeDetailsRepository.getTraineeByInstituteId(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(result);
		return DtlsJson;
	}

	
	
	
}

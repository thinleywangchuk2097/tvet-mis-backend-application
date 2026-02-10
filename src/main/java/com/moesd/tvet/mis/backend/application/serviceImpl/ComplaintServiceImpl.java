package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Complaintdto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.Complaint;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.ComplaintRepository;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.ComplaintService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class ComplaintServiceImpl implements ComplaintService{

	private final GenerateApplicationNumber applicationNumber;
	private final ComplaintRepository complaintRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final DocumentFileUploadService documentFileUploadService;
	private final ServiceMasterRepository serviceMasterRepository;
	private final RoleServiceRepository roleServiceRepository;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final ObjectToJson objectTojson;
	
	@Override
	public ResponseEntity<?> submit(Complaintdto request) {

		try {
			// Fetch next assigned role dynamically
			RoleService roleService = roleServiceRepository
					.getNextAssignedRole(request.getCurrentRoleId(), request.getServiceId(), request.getStatusId())
					.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

			// Generate unique application number
			String applicationNo = applicationNumber.generateApplicationNumber(request.getServiceId());
			request.setApplicationNo(applicationNo);

			// Validate and fetch ServiceMaster using dynamic serviceId
			Integer serviceId = serviceMasterRepository.findById(request.getServiceId())
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found")).getId();

			// Fetch initial status ("unclaimed") from drop down
			Integer initiatedStatusId = dropdownManagementRepository.findChildById(98)//52
					.orElseThrow(() -> new RecordNotFoundException("unclaimed status not found"));

			// Build and save Complaint entity
			Complaint complaint = Complaint.builder().applicationNo(request.getApplicationNo())
					.applicantName(request.getApplicantName()).emaildId(request.getEmaildId())
					.mobileNo(request.getMobileNo()).complaintType(request.getComplaintType())
					.priorityLevel(request.getPriorityLevel()).description(request.getDescription()).build();

			complaint = complaintRepository.save(complaint);

			// Create workflow record
			WorkFlowList workflow = workTaskFlowService.createWorkflow(complaint.getApplicationNo(),
					complaint.getApplicantName(), serviceId, roleService.getNextStatusId(), request.getCurrentRoleId(),
					request.getUserId(), request.getRemarks());

			// Create task flow
			workTaskFlowService.createTaskFlow(complaint.getApplicationNo(), initiatedStatusId,
					roleService.getNextRoleId(), workflow, request.getRemarks(), request.getLocationId());

			// Save documents if provided
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), complaint.getApplicationNo(),
						"complaint", request.getServiceId(), request.getUserId(), null);
			}

			// Return proper JSON response
			return ResponseEntity.status(201).body(Map.of("status", "SUCCESS", "applicationNo",
					complaint.getApplicationNo(), "complaintId", complaint.getId()));

		} catch (RecordNotFoundException e) {
			log.error("Record not found: {}", e.getMessage());
			return ResponseEntity.status(404).body(Map.of("status", "FAIL", "message", e.getMessage()));

		} catch (Exception e) {
			log.error("Error in saving complaint details", e);
			return ResponseEntity.status(500).body(Map.of("status", "FAIL", "message", "Internal Server Error"));
		}
	}

	@Override
	public List<ObjectNode> getComplaintDetails(String application_no) {
		// TODO Auto-generated method stub
		List<Tuple> resultList = complaintRepository.getComplaintDetails(application_no);
		List<ObjectNode> ComplaintDtlsJson = objectTojson._toJson(resultList);
		return ComplaintDtlsJson;
		
	}
}

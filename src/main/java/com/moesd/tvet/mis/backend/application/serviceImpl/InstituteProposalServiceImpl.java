package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteProposaldto;
import com.moesd.tvet.mis.backend.application.dto.PartnerDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.InstituteProposal;
import com.moesd.tvet.mis.backend.application.model.InstituteProposalPartnership;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.InstituteProposalRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.InstituteProposalService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstituteProposalServiceImpl implements InstituteProposalService {

	private final InstituteProposalRepository instituteProposalRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ServiceMasterRepository serviceMasterRepository;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final RoleServiceRepository roleServiceRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final DocumentFileUploadService documentFileUploadService;
	private final ObjectToJson objectTojson;

	@Override
	public ResponseEntity<?> submitInstituteProposal(InstituteProposaldto dto) {
		try {

			// 1. Validate required fields
			if (dto.getServiceId() == null)
				throw new RecordNotFoundException("serviceId is required");

			if (dto.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assigned RoleId is required");

			if (dto.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");

			Integer serviceId = dto.getServiceId();
			Integer assignedRoleId = dto.getAssignedRoleId();
			String userId = dto.getUserId();
			Integer locationId = dto.getDzongkhagId();
			String applicantName = dto.getProposedInstituteName();
			// 2. Validate service
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// 3. Get unclaimed statusId
			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
					.orElseThrow(() -> new RecordNotFoundException("Unclaimed status not found"));

			// 4. Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);

			// 5. Build entity
			InstituteProposal proposal = InstituteProposal.builder().applicationNo(applicationNo)
					.ownershipTypeId(dto.getOwnershipTypeId()).otherOwnershipTypeId(dto.getOtherOwnershipTypeId())
					.registrationNo(dto.getRegistrationNo()).companyName(dto.getCompanyName())
					.otherName(dto.getOtherName()).otherAddress(dto.getOtherAddress())
					.proposedInstituteName(dto.getProposedInstituteName())
					.dzongkhagId(dto.getDzongkhagId()).exactLocation(dto.getExactLocation())
					.telephoneNo(dto.getTelephoneNo()).mobileNo(dto.getMobileNo()).emailId(dto.getEmail())
					.promoterCitizenId(dto.getPromoterCitizenId()).promoterName(dto.getPromoterName())
					.sectorId(dto.getSectorId()).activityLevelId(dto.getActivityLevelId()).serviceId(serviceId)
					.statusId(dto.getStatusId()).createdAt(LocalDateTime.now()).build();

			// 6. Handle partners safely
			// handle partners safely
			PartnerDto[] partners = dto.getPartners();
			if (partners != null && partners.length > 0) {
				List<InstituteProposalPartnership> partnerEntities = new ArrayList<>();
				for (PartnerDto partnerDto : partners) {
					InstituteProposalPartnership partner = InstituteProposalPartnership.builder()
							.typeOfOwnerId(partnerDto.getTypeOfOwner()).partnerCidNo(partnerDto.getCitizenId())
							.partnerName(partnerDto.getPartnerName())
							.partnerCompanyRegistrationNo(partnerDto.getRegistrationNo())
							.partnerCompanyName(partnerDto.getCompanyName()).parent(proposal).build();

					partnerEntities.add(partner);
				}
				proposal.setInstituteProposalPartnership(partnerEntities);
			} else {
				proposal.setInstituteProposalPartnership(new ArrayList<>()); // safe empty list
			}

			// 7. Save proposal
			instituteProposalRepository.save(proposal);

			// 8. Create workflow
			WorkFlowList workflow = workTaskFlowService.createWorkflow(applicationNo, applicantName, serviceId,
					dto.getStatusId(), assignedRoleId, dto.getRemarks());

			// 9. Create task flow
			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, assignedRoleId, dto.getAssignedUserId(),
					workflow, dto.getRemarks(), locationId);

			// 10. Save documents
			if (dto.getDocuments() != null && dto.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(dto.getDocuments(), applicationNo, "institute_proposal",
						serviceId, userId, null);
			}

			// 11. Return response
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("applicationNo", applicationNo, "status", HttpStatus.CREATED.value()));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to submit proposal", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getInstituteDetails(String application_no) {
		List<Tuple> resultList = instituteProposalRepository.getInstituteDetails(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public ResponseEntity<?> verifyInstituteProposal(InstituteProposaldto dto) {
		try {
			// 1. Validate required fields
			if (dto.getServiceId() == null)
				throw new RecordNotFoundException("serviceId is required");

			if (dto.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assigned RoleId is required");

			if (dto.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");

			Integer serviceId = dto.getServiceId();
			Integer assignedRoleId = dto.getAssignedRoleId();
			Integer statusId = dto.getStatusId();// work flow status Id
			String userId = dto.getUserId();

			// 2. Validate service
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// 3. Fetch next role
			RoleService roleService = roleServiceRepository.getNextAssignedRole(assignedRoleId, serviceId, statusId)
					.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

			// Find existing proposal by applicationNo
			InstituteProposal existingProposal = instituteProposalRepository.findByApplicationNo(dto.getApplicationNo()) // Returns
																															// Optional
					.orElseThrow(() -> new RecordNotFoundException("Institute proposal not found with applicationNo"));
			// Update InstituteRegistration entity
			existingProposal.setStatusId(dto.getStatusId());
			existingProposal.setUpdatedAt(LocalDateTime.now());
			if (dto.getServiceId() == 58) {
				existingProposal.setProposedInstituteName(dto.getProposedInstituteName());
				existingProposal.setDzongkhagId(dto.getDzongkhagId());
				existingProposal.setExactLocation(dto.getExactLocation());
				existingProposal.setMobileNo(dto.getMobileNo());
				existingProposal.setTelephoneNo(dto.getTelephoneNo());
				existingProposal.setEmailId(dto.getEmail());
				existingProposal.setSectorId(dto.getSectorId());
				existingProposal.setActivityLevelId(dto.getActivityLevelId());
			}
			// Save the updated registration
			instituteProposalRepository.save(existingProposal);

			// 4. Get task_status_id
			Integer taskStatusId = dropdownManagementRepository.findChildById(20)// completed taskStatusId
					.orElseThrow(() -> new RecordNotFoundException("Task Status Id status not found"));

			// 5. update workflow
			workTaskFlowService.updateWorkflow(dto.getApplicationNo(), statusId, assignedRoleId, dto.getUserId(),
					dto.getRemarks(), serviceId, null);

			// 6. update task flow
			workTaskFlowService.updateTaskFlow(dto.getApplicationNo(), taskStatusId, roleService.getNextRoleId(),
					dto.getUserId(), dto.getRemarks());

			// 7. Save documents
			if (dto.getDocuments() != null && dto.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(dto.getDocuments(), dto.getApplicationNo(), "institute_proposal",
						serviceId, userId, null);
			}

			// For update operations - return 200 OK with updated data
			return ResponseEntity.ok().body(Map.of("applicationNo", dto.getApplicationNo(), "status",
					HttpStatus.OK.value(), "message", "Application Approved successfully"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to submit proposal", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

}

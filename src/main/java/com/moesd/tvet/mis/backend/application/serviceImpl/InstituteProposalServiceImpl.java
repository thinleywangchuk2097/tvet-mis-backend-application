package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstituteProposalServiceImpl implements InstituteProposalService {

	private final InstituteProposalRepository instituteProposalRepository;
	private final GenerateApplicationNumber applicationNumber;
	private final ServiceMasterRepository serviceMasterRepository;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final RoleServiceRepository roleServiceRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final DocumentFileUploadService documentFileUploadService;
	

	@Override
	public ResponseEntity<?> submitInstituteProposal(InstituteProposaldto dto) {
	    try {

	        // 1. Validate required fields
	        if (dto.getServiceId() == null)
	            throw new RecordNotFoundException("serviceId is required");

	        if (dto.getCurrentRoleId() == null)
	            throw new RecordNotFoundException("currentRoleId is required");

	        if (dto.getStatusId() == null)
	            throw new RecordNotFoundException("statusId is required");

	        if (dto.getUserId() == null)
	            throw new RecordNotFoundException("userId is required");

	        Integer serviceId = dto.getServiceId();
	        Integer currentRoleId = dto.getCurrentRoleId();
	        Integer statusId = dto.getStatusId();
	        String userId = dto.getUserId();
	        Integer locationId = dto.getDzongkhagId();

	        // 2. Validate service
	        serviceMasterRepository.findById(serviceId)
	                .orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

	        // 3. Fetch next role
	        RoleService roleService = roleServiceRepository
	                .getNextAssignedRole(currentRoleId, serviceId, statusId)
	                .orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

	        // 4. Get initiated status
	        Integer initiatedStatusId = dropdownManagementRepository.findChildById(98)
	                .orElseThrow(() -> new RecordNotFoundException("Unclaimed status not found"));

	        // 5. Generate application number
	        String applicationNo = applicationNumber.generateApplicationNumber(serviceId);

	        // 6. Build entity
	        InstituteProposal proposal = InstituteProposal.builder()
	                .applicationNo(applicationNo)
	                .ownershipType(dto.getOwnershipType())
	                .otherOwnershipType(dto.getOtherOwnershipType())
	                .registrationNo(dto.getRegistrationNo())
	                .companyName(dto.getCompanyName())
	                .otherName(dto.getOtherName())
	                .otherAddress(dto.getOtherAddress())
	                .proposedInstituteName(dto.getProposedInstituteName())
	                .dzongkhagId(String.valueOf(dto.getDzongkhagId()))
	                .exactLocation(dto.getExactLocation())
	                .telephoneNo(dto.getTelephoneNo())
	                .mobileNo(dto.getMobileNo())
	                .emailId(dto.getEmail())
	                .promoterCidNo(dto.getPromoterCitizenId())
	                .promoterName(dto.getPromoterName())
	                .fieldOfTrainingId(dto.getFieldOfTraining())
	                .activityLevelId(dto.getActivityLevel())
	                .statusId(String.valueOf(dto.getStatusId()))
	                .createdAt(LocalDateTime.now())
	                .build();

	        // 7. Handle partners safely
	     // handle partners safely
	        PartnerDto[] partners = dto.getPartners();
	        if (partners != null && partners.length > 0) {
	            List<InstituteProposalPartnership> partnerEntities = new ArrayList<>();
	            for (PartnerDto partnerDto : partners) {
	                InstituteProposalPartnership partner = InstituteProposalPartnership.builder()
	                        .typeOfOwner(partnerDto.getTypeOfOwner())
	                        .partnerCidNo(partnerDto.getCitizenId())
	                        .partnerName(partnerDto.getPartnerName())
	                        .partnerCompanyRegistrationNo(partnerDto.getRegistrationNo())
	                        .partnerCompanyName(partnerDto.getCompanyName())
	                        .parent(proposal)
	                        .build();

	                partnerEntities.add(partner);
	            }
	            proposal.setInstituteProposalPartnership(partnerEntities);
	        } else {
	            proposal.setInstituteProposalPartnership(new ArrayList<>()); // safe empty list
	        }

	        // 8. Save proposal
	        instituteProposalRepository.save(proposal);

	        // 9. Determine applicant name
	        String applicantName =
	                dto.getCompanyName() != null && !dto.getCompanyName().isBlank()
	                        ? dto.getCompanyName()
	                        : dto.getOtherName() != null && !dto.getOtherName().isBlank()
	                        ? dto.getOtherName()
	                        : dto.getPromoterName() != null && !dto.getPromoterName().isBlank()
	                        ? dto.getPromoterName()
	                        : "Unknown Applicant";

	        // 10. Create workflow
	        WorkFlowList workflow = workTaskFlowService.createWorkflow(
	                applicationNo,
	                applicantName,
	                serviceId,
	                roleService.getNextStatusId(),
	                currentRoleId,
	                userId,
	                dto.getRemarks()
	        );

	        // 11. Create task flow
	        workTaskFlowService.createTaskFlow(
	                applicationNo,
	                initiatedStatusId,
	                roleService.getNextRoleId(),
	                roleService.getAssignedUserId(),
	                workflow,
	                dto.getRemarks(),
	                locationId
	        );

	        // 12. Save documents
	        if (dto.getDocuments() != null && dto.getDocuments().length > 0) {
	            documentFileUploadService.saveDocument(
	                    dto.getDocuments(),
	                    applicationNo,
	                    "institute_proposal",
	                    serviceId,
	                    userId,
	                    null
	            );
	        }

	        // 13. Return response
	        return ResponseEntity.status(HttpStatus.CREATED).body(
	                Map.of(
	                        "applicationNo", applicationNo,
	                        "status", HttpStatus.CREATED.value()
	                )
	        );

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of(
	                        "message", "Failed to submit proposal",
	                        "error", e.getMessage(),
	                        "timestamp", LocalDateTime.now()
	                ));
	    }
	}

}

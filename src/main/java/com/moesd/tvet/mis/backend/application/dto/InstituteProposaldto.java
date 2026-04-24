package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstituteProposaldto {
	// Ownership Information 
	private String ownershipTypeId; 
	private String otherOwnershipTypeId; 
	private String registrationNo;
	private String companyName;
	private String otherName;
	private String otherAddress;
	private PartnerDto[] partners;
	private String applicationNo;
	private String promoterCitizenId;
	private String promoterName;
	// Training Provider Profile
	private String proposedInstituteName;
	private Integer dzongkhagId; 
	private String exactLocation;
	private String telephoneNo;
	private String mobileNo;
	private String email;
	private String sectorId; 
	private String courseId;
	private String activityLevelId;
	// System fields
	private Integer serviceId; 
	private Integer assignedRoleId;
	private String assignedUserId; 
	private String userId; 
	private Integer statusId; 
	private String remarks;
	// Documents
	private Documentdto[] documents;
}

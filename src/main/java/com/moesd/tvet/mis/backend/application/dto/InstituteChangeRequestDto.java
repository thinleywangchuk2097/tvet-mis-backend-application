package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstituteChangeRequestDto {
	private Integer instituteId;
	private String reasonForChange;
	private String changeType; // location, name, ownership
	// Location changes
	private Integer dzongkhagId;
	private String exactLocation;
	// Name changes
	private String instituteName;
	// Ownership changes
	private String ownershipTypeId;
	private String otherOwnershipTypeId;
	private String registrationNo;
	private String companyName;
	private String otherName;
	private String otherAddress;
	private PartnerDto[] partners;
	private String promoterCitizenId;
	private String promoterName;
	// System fields
	private Integer serviceId;
	private Integer assignedRoleId;
	private String assignedUserId;
	private Integer statusId;
	private String remarks;
	private Integer createdBy;
	private Integer updatedBy;

	
	private Documentdto[] documents;

}

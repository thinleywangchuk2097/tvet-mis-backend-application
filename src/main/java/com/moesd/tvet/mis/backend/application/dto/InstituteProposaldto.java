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

	private String ownershipType;
	private String otherOwnershipType;
	private String registrationNo;
	private String companyName;
	private String otherName;
	private String otherAddress;
	private PartnerDto[] partners;
	private String proposedInstituteName;
	private Integer dzongkhagId;
	private String exactLocation;
	private String telephoneNo;
	private String mobileNo;
	private String email;
	private String promoterCitizenId;
	private String promoterName;
	private String fieldOfTraining;
	private String activityLevel;
	private String remarks;
	private Integer serviceId;
	private Integer currentRoleId;
	private String userId;
	private Integer statusId;
	private Documentdto[] documents;
}

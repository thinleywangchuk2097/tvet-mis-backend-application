package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstituteRegistrationdto {
	private String applicationNo;
	private String instituteName;
	private String dzongkhagId;
	private String exactLocation;
	private String telephoneNo;
	private String mobileNo;
	private String emailId;
	private String website;
	private Long ownershipTypeId;
	private Integer bhutaneseEmployees;
	private Integer nonBhutaneseEmployees;
	private String businessLicenseNo;
	private String keyContactName;
	private String keyContactDesignation;
	private String keyContactMobileNo;
	private List<Trainerdto> trainers;
	private List<Coursedto> courses;
	private List<QualityStandardsdto> qualityStandards;
	private List<AssignedRecsDto> assignedRecs;
	private List<AssignedRecsDto>assignedAccreditors;
	private List<TuitionDetailsdto>tuitionDetails;
	// System fields
	private Integer serviceId; 
	private Integer assignedRoleId;
	private String assignedUserId; 
	private String userId; 
	private Integer statusId; 
	private String remarks;
	private String createdBy;
	private String updatedBy;
	// Documents
	private Documentdto[] documents;
}

package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_institute_registration_dtls")
public class InstituteRegistrationDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "institute_id")
	private Long instituteId;

	@Column(name = "application_no", nullable = false, unique = true, length = 50)
	private String applicationNo;
	
	private String proposedInstituteName;

	private String dzongkhagId;

	private String exactLocation;

	private String telephoneNo;

	private String mobileNo;

	private String emailId;

	private String sectorId;

	private Integer serviceId;
	
	@Column(name = "registration_no", unique = true)
	private String RegistrationNo;

	@Column(name = "website")
	private String website;

	@Column(name = "ownership_type_id")
	private Long ownershipTypeId;

	@Column(name = "bhutanese_employees")
	private Integer bhutaneseEmployees;

	@Column(name = "non_bhutanese_employees")
	private Integer nonBhutaneseEmployees;

	@Column(name = "business_license_no")
	private String businessLicenseNo;

	@Column(name = "key_contact_name")
	private String keyContactName;

	@Column(name = "key_contact_designation")
	private String keyContactDesignation;
	
	private String keyContactMobileNo;

	private Integer statusId;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@Column(name = "updated_by")
	private String updatedBy;


	
}

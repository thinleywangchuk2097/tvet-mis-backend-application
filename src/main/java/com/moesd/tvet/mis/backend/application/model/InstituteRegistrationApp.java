package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "tbl_institute_registration_app")
public class InstituteRegistrationApp {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "application_no", nullable = false, unique = true, length = 50)
	private String applicationNo;
	
	private String proposalApplicationNo;
	
	private String proposedInstituteName;

	private String dzongkhagId;

	private String exactLocation;

	private String telephoneNo;

	private String mobileNo;

	private String emailId;

	private Integer serviceId;
	
	private String website;

	private Long ownershipTypeId;

	private Integer bhutaneseEmployees;

	private Integer nonBhutaneseEmployees;

	private String businessLicenseNo;

	private String keyContactName;

	private String keyContactDesignation;
	
	private String keyContactMobileNo;

	private Integer statusId;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@Column(name = "updated_by")
	private String updatedBy;

	//Relationships - Using mappedBy to indicate the child owns the relationship
	@Builder.Default
	@OneToMany(mappedBy = "instituteRegistration", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InstituteRegistrationAppTrainer> trainers = new ArrayList<>();
	
	@Builder.Default
	@OneToMany(mappedBy = "instituteRegistration", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InstituteRegistrationAppCourse> courses = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "instituteRegistration", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InstituteRegistrationAppQualityStandardResponse> qualityStandardResponses = new ArrayList<>();
	
	@Builder.Default
	@OneToMany(mappedBy = "instituteRegistration", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InstituteRegistrationAppTuitionDetails> tuitionDetails = new ArrayList<>();
}

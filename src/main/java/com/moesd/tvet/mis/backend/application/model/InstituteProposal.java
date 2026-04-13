package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
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
@Table(name = "tbl_institute_proposal")
public class InstituteProposal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false, unique = true)
	private String applicationNo;
	
	private String ownershipTypeId;

	private String registrationNo;

	private String companyName;

	private String proposedInstituteName;

	private Integer dzongkhagId;
	
	@Column(columnDefinition = "TEXT")
	private String exactLocation;

	private String telephoneNo;

	private String mobileNo;

	private String emailId;

	private String sectorId;

	private String activityLevelId;

	private String otherOwnershipTypeId;

	private String otherName;

	private String otherAddress;
	
	private String promoterCitizenId;
	
	private String promoterName;

	private Integer statusId;
	
	private Integer serviceId;
	
	private Integer createdBy;

	private LocalDateTime createdAt;

	private Integer updatedBy;

	private LocalDateTime updatedAt;
	
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstituteProposalPartnership> instituteProposalPartnership;
}

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

/**
 * Snapshot of tbl_institute_proposal taken before an ownership change is applied.
 * One row per change (no unique keys).
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_institute_proposal_audit")
public class InstituteProposalAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** id of the tbl_institute_proposal row */
	private Integer proposalId;

	/** proposal application no (tbl_institute_proposal.application_no) */
	private String applicationNo;

	/** institute change request that replaced this data */
	private String changeApplicationNo;

	private String ownershipTypeId;
	private String otherOwnershipTypeId;
	private String registrationNo;
	private String companyName;
	private String otherName;

	@Column(columnDefinition = "TEXT")
	private String otherAddress;

	private String promoterCitizenId;
	private String promoterName;
	private String proposedInstituteName;
	private Integer statusId;
	private Integer serviceId;

	private Integer createdBy;
	private LocalDateTime createdAt;
	private Integer updatedBy;
	private LocalDateTime updatedAt;

	/** when the snapshot was taken */
	private LocalDateTime auditedAt;
}

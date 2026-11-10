package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
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
 * Snapshot of tbl_institute_proposal_partnership rows taken before an ownership change is applied.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_institute_proposal_partnership_audit")
public class InstituteProposalPartnershipAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** id of the tbl_institute_proposal_partnership row */
	private Integer partnershipId;

	/** proposal application no (tbl_institute_proposal.application_no) */
	private String applicationNo;

	/** institute change request that replaced this data */
	private String changeApplicationNo;

	private String typeOfOwnerId;
	private String partnerCidNo;
	private String partnerName;
	private String partnerCompanyRegistrationNo;
	private String partnerCompanyName;

	/** when the snapshot was taken */
	private LocalDateTime auditedAt;
}

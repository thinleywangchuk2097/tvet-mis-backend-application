package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Reviewer action on a CMFG monitoring application (e.g. verify). */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CMFGVerifyDto {
	private String applicationNo;
	private Integer statusId;       // e.g. 56 = verified
	private Integer serviceId;      // default 55
	private Integer assignedRoleId; // role of the reviewer acting now
	private String userId;          // reviewer user id
	private Integer updatedBy;
	private String remarks;
	/** Reviewer's (possibly edited) Yes/No + remarks per checklist item. */
	private List<QualityStandardsdto> qualityStandards;
}

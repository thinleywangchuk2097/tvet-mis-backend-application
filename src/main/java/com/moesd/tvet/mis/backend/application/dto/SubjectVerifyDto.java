package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Reviewer action on an Add Subject application (service 56). */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectVerifyDto {
	private String applicationNo;
	private Integer statusId;       // 56 verified, 58 rejected (role 7); 57 approved (role 22)
	private Integer assignedRoleId; // role of the reviewer acting now
	private String userId;          // reviewer user id
	private Integer updatedBy;
	private String remarks;
}

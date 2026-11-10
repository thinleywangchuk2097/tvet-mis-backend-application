package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Verify / reject an institute change request. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstituteChangeVerifyDto {
	private String applicationNo;
	private Integer statusId; // 56 = verified (role 7), 57 = approved (role 22), 58 = rejected
	private String remarks;
	private Integer updatedBy;
	private String userId;
	private Integer currentRoleId; // role acting on the request (7 or 22)
}

package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TOTCompleteApplicationDto {
	private String applicationNo;
	private String userId;      // actor (logged-in user)
	private Integer updatedBy;
	private String remarks;
}

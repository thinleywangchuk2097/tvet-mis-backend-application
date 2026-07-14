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
public class TotProgramDto {
	
	private Long id;
	private String programName;
	private String programCode;
	private Integer programTypeId;
	private String description;
	private Integer statusId;
	private Integer createdBy;
	private Integer updatedBy;
	private List<TotModuleDto> modules;
	// System fields
	private Integer serviceId;
	private Integer assignedRoleId;
	private String userId;
	private String remarks;
}

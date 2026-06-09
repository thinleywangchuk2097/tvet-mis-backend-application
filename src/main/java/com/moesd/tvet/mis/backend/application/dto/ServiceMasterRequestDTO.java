package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMasterRequestDTO {
	
	private Integer id;
	
	private String serviceName;

	private String departmentId;

	private String validityDate;

	private String route;

	private Integer lastApplicationNo;

	private Integer licenseLastSequence;

	private String hasCertificate;

	private String isActive;
}

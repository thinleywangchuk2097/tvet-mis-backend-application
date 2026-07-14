package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnCampusJobPlacementFirmDto {
	
	private String registrationNo;
	private String firmName;
	private String contactPerson;
	private String contactPhone;
	private String contactEmail;
	private Integer dzongkhagId;
	private String address;
	private Long sessionId;
	private String description;
	private Integer instituteId;
	private Integer createdBy;
	private Integer updatedBy;

}

package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OJTCompanyDto {
	
	private Long id;

	private String registrationNo;

	private String companyName;

	private Integer dzongkhagId;

	private String contactPersonName;

	private String contactPersonMobileNo;
	
    private Integer instituteId;
    
	private String contactPersonEmail;

	private String address;

	private String description;

	private Integer createdBy;

	private Integer updatedBy;

}

package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartnerDto {

	private String partnerName;
	private String typeOfOwner;
	private String citizenId;
	private String registrationNo;
	private String companyName;

}

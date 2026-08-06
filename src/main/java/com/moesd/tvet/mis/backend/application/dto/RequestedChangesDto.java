package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestedChangesDto {
	// Location changes
	private String dzongkhagId;
	private String exactLocation;

	// Name changes
	private String instituteName;

	// Ownership changes
	private String ownershipTypeId;
	private Integer otherOwnershipTypeId;
	private String registrationNo;
	private String companyName;
	private String otherName;
	private String otherAddress;
	private String promoterCitizenId;
	private String promoterName;
	private String partnerships; // JSON string of partners
}

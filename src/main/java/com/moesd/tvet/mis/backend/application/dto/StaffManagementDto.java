package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffManagementDto {
	private Long id;
	private String hasCitizenId;
	private String citizenId;
	private String name;
	private String email;
	private String mobileNo;
	private String referenceNo;
	private Integer genderId;
	private String dob;
	private Integer instituteId;
	private Integer createdBy;
	private Integer updatedBy;
	
	private List<StaffEmploymentHistoryDto> staffemploymenthistory;
	private List<StaffTrainingHistoryDto> stafftraininghistory;

}

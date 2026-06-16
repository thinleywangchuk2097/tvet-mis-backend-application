package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorDto {
	
	private Long id;

	private String citizenId;
	
	private String firstName;
	
	private String middleName;
	
	private String lastName;
	
	private String email;
	
	private String mobileNo;
	
	private Integer qualificationId;
	
	private Integer instituteId;
	
	private Integer experienceYears;
	
	private String hourlyRate;
	
	private Integer statusId;
	
	private String specialization;
	
	private LocalDate joiningDate;
	
	private String description;
	
	private Integer createdBy;

	private Integer updatedBy;

	
}

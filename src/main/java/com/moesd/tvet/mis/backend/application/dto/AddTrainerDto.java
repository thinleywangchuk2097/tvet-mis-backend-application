package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTrainerDto {

	private Long id;

	private String citizenId;

	private String workPermitNo;

	private String name;
	
    private String specialization;
    
	private Integer genderId;

	private Integer qualificationId;

	private String workExperience;

	private Integer employmentTypeId;

	private String email;

	private String mobileNo;

	private Integer instituteId;

	private LocalDate joiningDate;

	private Integer statusId;

	private String description;

	private Integer createdBy;

	private Integer updatedBy;
	// Documents
	//private Documentdto[] documents;

	private List<TrainerCourseDto> courses;
}

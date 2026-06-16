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
public class StudentAddDto {
	
	private Long id;
	
	private String citizenId;
	
	private String studentCode;
	
	private String firstName;
	
	private String middleName;
	
	private String lastName;
	
	private String email;
	
	private String mobileNo;
	
	private Integer qualificationId;
	
	private Integer instituteId;
	
	private Integer genderId;
	
	private Integer statusId;
	
	private Integer dzongkhagId;
	
	private String exactLocation;
	
	private String emergencyContactName;
	
	private String emergencyContactNo;
	
	private String dateOfBirth;
	
	private LocalDate enrollmentDate;
	
	private String schoolName;
	
	private String currentClass;
	
	private String schoolExactLocation;
	
	private Integer createdBy;

	private Integer updatedBy;
	
	private List<StudentSubjectDto>subjects;

}

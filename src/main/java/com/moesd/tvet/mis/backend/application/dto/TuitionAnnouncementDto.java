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
public class TuitionAnnouncementDto {
private Long id;
	
	private String title;
	
	private Integer subjectId;
	
	private Integer tutorId;
	
	private LocalDate startDate;
	
	private LocalDate endDate;
	
	private String description;
	
	private Integer instituteId;
	
	private String startTime;
	
	private String endTime;
	
	private String venue;
	
	private Integer maxStudents;
	
	private String fee;
	
	private String materials;
	
	private String requirements;
	
	private String contactPerson;
	
	private String contactPhone;
	
	private String statusId;
	
	private Integer createdBy;

	private Integer updatedBy;

	
}

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
public class StaffEmploymentHistoryDto {
	private Long id;
	private LocalDate appointmentDate;
	private Integer employmentTypeId;
	private Integer qualificationId;
	private String designation;
	private LocalDate resignationDate;
}

package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_tuition_announcement_dtls")
public class TuitionAnnouncement {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;
}

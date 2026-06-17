package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "tbl_trainer_dtls")
public class AddTrainer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String citizenId;

	private String workPermitNo;

	private String name;

	private Integer genderId;
	
	private Integer qualificationId;
	
	private String workExperience;
	
	private String specialization;
    
	private Integer employmentTypeId;
	
	private String email;
	
	private String mobileNo;
	
	private Integer instituteId;
	
	private LocalDate joiningDate;
	
	private Integer statusId;
	
	private String description;

	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

	@OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TrainerCourse> trainerCourse;
}

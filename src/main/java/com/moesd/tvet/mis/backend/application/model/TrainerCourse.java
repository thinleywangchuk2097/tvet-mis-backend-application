package com.moesd.tvet.mis.backend.application.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tbl_trainer_course_dtls")
public class TrainerCourse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Integer courseId;
	private Integer courseTypeId;
	@ManyToOne
	@JoinColumn(
	    name = "trainerId",           // FK name column in Student Subject table
	    referencedColumnName = "id" // column id in AddStudent
	)
	
	private AddTrainer trainer;
}

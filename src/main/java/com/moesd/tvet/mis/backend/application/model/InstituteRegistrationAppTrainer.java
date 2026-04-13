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

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_institute_registration_trainer_app")
public class InstituteRegistrationAppTrainer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long nationalityId;
	private String cid;
	private String workPermit;
	private String name;
	private Long genderId;
	private String qualification;
	private Integer experience;
	private Long typeId;
	// Many-to-One relationship with InstituteRegistration
	@ManyToOne
	@JoinColumn(name = "application_no", // FK column in trainer table
			referencedColumnName = "application_no" // column in InstituteRegistration
	)
	private InstituteRegistrationApp instituteRegistration;
}

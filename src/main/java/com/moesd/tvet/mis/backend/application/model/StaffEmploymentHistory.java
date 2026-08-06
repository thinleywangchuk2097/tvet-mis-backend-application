package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDate;

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
@Table(name = "tbl_staff_employment_history_dtls")
public class StaffEmploymentHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private LocalDate appointmentDate;
	private Integer employmentTypeId;
	private Integer qualificationId;
	private String designation;
	private LocalDate resignationDate;
	
	@ManyToOne
	@JoinColumn(
	    name = "staffId",          
	    referencedColumnName = "id" 
	)
	private StaffManagement Staff;
	
}

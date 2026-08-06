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
@Table(name = "tbl_staff_training_history_dtls")
public class StaffTrainingHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String trainingName;
	private LocalDate trainingStart;
	private LocalDate trainingEnd;
	private String providerName;
	private LocalDate resignationDate;
	private Integer fundingSourceId;
	private String trainingCost;
	
	@ManyToOne
	@JoinColumn(
	    name = "staffId",          
	    referencedColumnName = "id" 
	)
	private StaffManagement Staff;
	
}

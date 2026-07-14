package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDate;
import java.util.Date;

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
@Table(name = "tbl_tot_announcement_dtls")
public class TOTAnnouncement {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String applicationNo;
    
    private LocalDate applicationStartDate;
    
    private LocalDate applicationEndDate;
    
    private LocalDate programStartDate;
    
    private LocalDate programEndDate;
    
    private String maxParticipants;
    
    private Integer programTypeId;
    
    private String venue;
    
    private String eligibilityCriteria;
    
    private String remarks;
    
	private Integer createdBy;
	
	private Integer statusId;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;
	
	@ManyToOne
	@JoinColumn(name = "program_id")
	private TOTProgram totProgram;
}

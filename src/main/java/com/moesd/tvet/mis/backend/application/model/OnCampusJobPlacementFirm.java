package com.moesd.tvet.mis.backend.application.model;

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
@Table(name = "tbl_campus_job_placement_firm_dtls")
public class OnCampusJobPlacementFirm {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String registrationNo;
	private String firmName;
	private String contactPerson;
	private String contactPhone;
	private String contactEmail;
	private Integer dzongkhagId;
	private String address;
	private String description;
	private Integer instituteId;
	private Integer createdBy;
	private Date createdAt;
	private Integer updatedBy;
	private Date updatedAt;
	
	@ManyToOne
	@JoinColumn(name = "session_id")
	private OnCampusJobPlacementSession session;
	
	
}

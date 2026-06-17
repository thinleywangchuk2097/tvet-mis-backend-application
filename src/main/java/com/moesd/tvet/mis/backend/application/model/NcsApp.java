package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Table(name = "tbl_ncs_curriculum_publication")
public class NcsApp {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)  // For MySQL/PostgreSQL auto-increment
	@Column(name = "publication_id", nullable = false, unique = true)
	private Integer publicationId;
	private Integer occupationId;
	private Integer certificationId;
	private String courseTitle;
	private String ncsCode;
	private Date validityDate;
	private String publicationType;
	private Integer createdBy;
	private Integer updatedBy;

}

package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Table(name = "tbl_program_monitoring_dtls")
public class ProgramMonitoring {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "application_no", nullable = false, unique = true, length = 50)
	private String applicationNo;

	private Integer dzongkhagId;

	private String exactLocation;

	private String instituteName;

	private Integer statusId;

	private Integer instituteId;

	private Integer courseTypeId;

	private Integer courseId;

	private LocalDate monitoringDate;

	private Long registrationNo;

	private String description;

	private Integer serviceId;

	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

	@Builder.Default
	@OneToMany(mappedBy = "programMonitoring", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProgramMonitoringCheckList> checklists = new ArrayList<>();
}

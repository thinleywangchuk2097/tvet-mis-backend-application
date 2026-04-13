package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "tbl_registration_quality_standard")
public class InstituteRegistrationQualityStandard {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String dropdownName;

	private String description;

	@Column(nullable = false)
	private String displayOrder;

	@Column(nullable = false)
	private Integer isActive;

	private Integer parentId;

	private LocalDateTime createdAt;

	private Integer updatedBy;
}

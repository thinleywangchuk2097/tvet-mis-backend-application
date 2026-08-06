package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
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
@Table(name = "tbl_institute_change_dtls")
public class InstituteChangeDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true)
	private String applicationNo;
	private Integer instituteId;
	private String changeType;
	private String reasonForChange;

	private Integer dzongkhagId;
	private String exactLocation;
	private String instituteName;

	private String ownershipTypeId;
	private String promoterCitizenId;
	private String promoterName;
	private String otherOwnershipTypeId;
	private String registrationNo;
	private String companyName;
	private String otherName;
	private String otherAddress;

	private Integer statusId;
	private Integer serviceId;
	private Integer createdBy;
	private LocalDateTime createdAt;
	private Integer updatedBy;
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InstituteChangePartnership> instituteChangePartnership;
}

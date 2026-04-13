package com.moesd.tvet.mis.backend.application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_service_master")
public class ServiceMaster {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "service_name")
	private String serviceName;
	
	@Column(name = "department_id")
	private String departmentId;
	
	private String validityDate;
	
	@Column(name = "route",columnDefinition = "TEXT")
	private String route;
	
	@Column(name = "last_application_no",columnDefinition = "INT(7) UNSIGNED ZEROFILL")
	private Integer  lastApplicationNo; 
	
	@Column(name = "license_last_sequence",columnDefinition = "INT(4) UNSIGNED ZEROFILL")
	private Integer  licenseLastSequence; 
	
	@Column(name = "has_certificate",columnDefinition = "CHAR(1) DEFAULT 'N'")
    private char hasCertificate='Y'; 
	
    @Column(name = "is_active",columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private char isActive='Y'; 
}

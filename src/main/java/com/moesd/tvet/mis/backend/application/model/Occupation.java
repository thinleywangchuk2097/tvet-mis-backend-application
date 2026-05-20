package com.moesd.tvet.mis.backend.application.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tbl_occupation_master")
public class Occupation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "occupation_name")
	private String occupationName;

	@Column(name = "isco_code")
	private String iscoCode;

	@Column(name = "is_active", columnDefinition = "CHAR(1) DEFAULT 'Y'")
	private char isActive = 'Y';

	@JsonBackReference
	@ManyToOne
	@JoinColumn(name = "sector_id")
	private Sector sector;
}

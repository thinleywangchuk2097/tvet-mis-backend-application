package com.moesd.tvet.mis.backend.application.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "tbl_sector_master")
public class Sector {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "sector_name")
	private String sectorName;

	@Column(name = "is_active", columnDefinition = "CHAR(1) DEFAULT 'Y'")
	private char isActive = 'Y';

	@JsonManagedReference
	@OneToMany(mappedBy = "sector", cascade = CascadeType.ALL)
	private List<Occupation> child;
}

package com.moesd.tvet.mis.backend.application.model;

import java.util.ArrayList;
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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_sector_master")
public class Sector {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "sector_name")
	private String sectorName;

	@Column(name = "is_active")
	private char isActive;

	@JsonManagedReference
	@OneToMany(mappedBy = "sector", cascade = CascadeType.ALL, orphanRemoval = true)  // Add orphanRemoval = true
	@Builder.Default 
	private List<Occupation> child = new ArrayList<>();  // Initialize to avoid null
}

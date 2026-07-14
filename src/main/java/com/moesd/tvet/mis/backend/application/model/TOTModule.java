package com.moesd.tvet.mis.backend.application.model;

import jakarta.persistence.Column;
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
@Table(name = "tbl_tot_module_dtls")
public class TOTModule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String moduleName;

	private String moduleCode;

	private String description;

	private String prerequisites;

	private String duration;
	
	private Integer statusId;

	private String learningOutcomes;

	@Column(name = "module_order")
	private Integer moduleOrder;

	// Many-to-One relationship
	@ManyToOne
	@JoinColumn(name = "program_id", // FK column in TOTModule table
			referencedColumnName = "id" // column in TOTProgram id
	)
	
	private TOTProgram totProgram;

}

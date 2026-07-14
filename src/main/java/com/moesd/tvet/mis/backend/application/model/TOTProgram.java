package com.moesd.tvet.mis.backend.application.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
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
@Table(name = "tbl_tot_program_dtls")
public class TOTProgram {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String programName;

	private String programCode;

	private Integer programTypeId;

	private String description;
	
    private Integer statusId;
    
	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;
	
	@Builder.Default
	@OneToMany(mappedBy = "totProgram", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TOTModule> modules = new ArrayList<>();

}

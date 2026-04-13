package com.moesd.tvet.mis.backend.application.model;
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

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_institute_proposal_partnership")
public class InstituteProposalPartnership {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String typeOfOwnerId;

	private String partnerCidNo;

	private String partnerName;
	
	private String partnerCompanyRegistrationNo;

	private String partnerCompanyName;
	
	@ManyToOne
	@JoinColumn(
	    name = "application_no",           // FK column in partnership table
	    referencedColumnName = "applicationNo" // column in InstituteProposal
	)
	private InstituteProposal parent;
}

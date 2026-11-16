package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDate;
import java.util.Date;
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

/**
 * Public feedback and complaints (Feedback/Complain menu).
 * Feedback is visible to every logged-in user; complaints only to TVET QC (role 22),
 * who record the status and the action taken.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_feedback_complaint")
public class FeedbackComplaint {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "reference_no", length = 30, unique = true)
	private String referenceNo;

	/** FEEDBACK or COMPLAINT */
	@Column(name = "type", nullable = false, length = 20)
	private String type;

	// ── submitter (all empty for an anonymous complaint)
	@Column(name = "anonymous")
	private Boolean anonymous;

	@Column(name = "name", length = 200)
	private String name;

	@Column(name = "email", length = 200)
	private String email;

	@Column(name = "contact_no", length = 30)
	private String contactNo;

	@Column(name = "submitter_role", length = 50)
	private String submitterRole;

	@Column(name = "category", length = 50)
	private String category;

	// ── feedback
	@Column(name = "rating")
	private Integer rating;

	@Column(name = "experience", columnDefinition = "TEXT")
	private String experience;

	@Column(name = "suggestions", columnDefinition = "TEXT")
	private String suggestions;

	@Column(name = "recommend")
	private Boolean recommend;

	// ── complaint
	@Column(name = "incident_date")
	private LocalDate incidentDate;

	@Column(name = "against_person", length = 200)
	private String againstPerson;

	@Column(name = "institution", length = 300)
	private String institution;

	@Column(name = "urgency", length = 20)
	private String urgency;

	@Column(name = "incident_details", columnDefinition = "TEXT")
	private String incidentDetails;

	@Column(name = "desired_resolution", columnDefinition = "TEXT")
	private String desiredResolution;

	// ── handling (complaints, by TVET QC)
	/** SUBMITTED, UNDER_REVIEW, RESOLVED, REJECTED */
	@Column(name = "status", length = 20)
	private String status;

	@Column(name = "action_taken", columnDefinition = "TEXT")
	private String actionTaken;

	@Column(name = "action_by", length = 100)
	private String actionBy;

	@Column(name = "action_at")
	private Date actionAt;

	@Column(name = "created_at")
	private Date createdAt;
}

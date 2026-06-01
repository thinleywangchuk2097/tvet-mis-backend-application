package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_token")
public class Token {
	@Id
	@GeneratedValue
	public Integer id;

	@Column(unique = true, columnDefinition = "TEXT")
	public String token;

	@Column(name = "token_type")
	@Builder.Default
	private String tokenType = "Bearer";

	public Boolean revoked;

	public Boolean expired;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	public User user;

	@Column(name = "login_at")
	@CreationTimestamp
	private LocalDateTime createdAt;
}

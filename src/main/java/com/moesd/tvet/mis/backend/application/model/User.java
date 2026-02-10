package com.moesd.tvet.mis.backend.application.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "tbl_user")
public class User implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String userId;

	@Column(nullable = false)
	private String firstName;
	
	@Column(nullable = true)
	private String middleName;
	
	@Column(nullable = true)
	private String lastName;
	
	@Column(nullable = true)
	private String genderId;
	
	@Column(nullable = false)
	private String password;
	
	private String mobileNo;
	
	private String emailId;
	
	@Column(name = "profile_path", columnDefinition = "TEXT")
	private String profilePath;
	
	private String statusId;
	
	private String locationId;
	
	private Integer currentRole;
	
	private Integer createdBy;
	
	private Date createdAt;
	
	private Integer updatedBy;
	
	private Date updatedAt;
	
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<Token> tokens;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserRole> userRoles;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
	    if (userRoles != null) {
	    	for (UserRole r : userRoles) {
	    		//authorities.add(new SimpleGrantedAuthority(r.getRole().getRoleName()));
	    		
	    		// Use role ID instead of role name
	    		authorities.add(new SimpleGrantedAuthority(r.getRole().getId().toString()));// Convert Long ID to String
	    		
            }
	    }

	    return authorities;
	}

	@Override
	public String getUsername() {
		return userId;
	}

}

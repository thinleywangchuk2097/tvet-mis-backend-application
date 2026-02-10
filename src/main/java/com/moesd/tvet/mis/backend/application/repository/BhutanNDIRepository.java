package com.moesd.tvet.mis.backend.application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.BhutanNDIToken;

public interface BhutanNDIRepository extends JpaRepository<BhutanNDIToken, Long>{
	Optional<BhutanNDIToken> findTopByOrderByExpirationDateDesc();
}

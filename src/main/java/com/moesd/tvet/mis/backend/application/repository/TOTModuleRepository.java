package com.moesd.tvet.mis.backend.application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.TOTModule;


public interface TOTModuleRepository extends JpaRepository<TOTModule, Long>{
	
	 Optional<TOTModule> findByModuleCode(String moduleCode);
}

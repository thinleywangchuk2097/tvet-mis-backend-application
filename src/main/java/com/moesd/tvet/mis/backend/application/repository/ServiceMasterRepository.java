package com.moesd.tvet.mis.backend.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.ServiceMaster;


public interface ServiceMasterRepository extends JpaRepository<ServiceMaster, Integer>{
	Optional<ServiceMaster> findById(Integer id);
}

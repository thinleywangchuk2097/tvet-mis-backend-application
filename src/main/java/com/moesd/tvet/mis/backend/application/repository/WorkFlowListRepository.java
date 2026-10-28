package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;

public interface WorkFlowListRepository extends JpaRepository<WorkFlowList,Long>{
	WorkFlowList findByApplicationNo(String applicationNo);

	// TOT: one application no can have several workflow rows (one per applying institute)
	List<WorkFlowList> findAllByApplicationNo(String applicationNo);
}

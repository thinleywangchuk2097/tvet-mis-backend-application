package com.moesd.tvet.mis.backend.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;

public interface WorkFlowListRepository extends JpaRepository<WorkFlowList,Long>{
	WorkFlowList findByApplicationNo(String applicationNo);
}

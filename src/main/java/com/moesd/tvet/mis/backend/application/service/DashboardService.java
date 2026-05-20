package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {

	List<Map<String, Object>> getDashboardData(Long userId, Long currentRoleId);

}

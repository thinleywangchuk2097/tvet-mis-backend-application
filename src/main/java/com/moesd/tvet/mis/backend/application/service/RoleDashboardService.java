package com.moesd.tvet.mis.backend.application.service;

import java.time.LocalDate;
import java.util.Map;

public interface RoleDashboardService {

	/** roleId <= 0 = all services (system-wide). */
	Map<String, Object> getRoleOverview(int roleId, LocalDate from, LocalDate to);

	/** Users, roles, dropdowns, services and open tasks per role (admin). */
	Map<String, Object> getAdminOverview(LocalDate from, LocalDate to);
}

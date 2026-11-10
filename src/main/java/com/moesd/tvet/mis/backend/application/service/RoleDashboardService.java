package com.moesd.tvet.mis.backend.application.service;

import java.time.LocalDate;
import java.util.Map;

public interface RoleDashboardService {

	/** roleId <= 0 = all services (system-wide). */
	Map<String, Object> getRoleOverview(int roleId, LocalDate from, LocalDate to);

	/** Users, roles, dropdowns, services and open tasks per role (admin). */
	Map<String, Object> getAdminOverview(LocalDate from, LocalDate to);

	/** TTTRC (Verifier 14 / Head 15): TOT programmes and curriculum data plus the role's open tasks. */
	Map<String, Object> getTttrcOverview(int roleId);

	/** DWPS 1 (role 21): curriculum applications and tracer surveys plus the role's open tasks. */
	Map<String, Object> getDwps1Overview(int roleId);
}

package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.service.DashboardService;
import com.moesd.tvet.mis.backend.application.service.InstituteDashboardService;
import com.moesd.tvet.mis.backend.application.service.RoleDashboardService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;
	private final InstituteDashboardService instituteDashboardService;
	private final RoleDashboardService roleDashboardService;

	@GetMapping("/get-dashboard-data/{userId}/{currentRoleId}")
	public List<Map<String, Object>> getDashboardData(@PathVariable Long userId, @PathVariable Long currentRoleId) {

		return dashboardService.getDashboardData(userId, currentRoleId);
	}

	/** Live data for the institute dashboard (registrationNo = logged-in institute user id). */
	@GetMapping("/institute/{registrationNo}")
	public ResponseEntity<?> getInstituteDashboard(@PathVariable String registrationNo) {
		try {
			return ResponseEntity.ok(instituteDashboardService.getInstituteDashboard(registrationNo));
		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("status", 404, "message", e.getMessage()));
		}
	}

	/**
	 * Staff dashboard overview (e.g. QAS Level 1 = role 7): TVET statistics plus the
	 * applications/tasks for the services this role handles. Dates are yyyy-MM-dd; default last 12 months.
	 */
	@GetMapping("/role-overview/{roleId}")
	public Map<String, Object> getRoleOverview(@PathVariable int roleId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		return roleDashboardService.getRoleOverview(roleId, fromDate, toDate);
	}

	/** Admin: users, roles, dropdowns, services and open tasks per role. Dates are yyyy-MM-dd. */
	@GetMapping("/admin-overview")
	public Map<String, Object> getAdminOverview(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		return roleDashboardService.getAdminOverview(fromDate, toDate);
	}
}

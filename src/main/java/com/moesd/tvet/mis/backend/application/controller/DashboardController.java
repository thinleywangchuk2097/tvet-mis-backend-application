package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.service.DashboardService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping("/get-dashboard-data/{userId}/{currentRoleId}")
	public List<Map<String, Object>> getDashboardData(@PathVariable Long userId, @PathVariable Long currentRoleId) {

		return dashboardService.getDashboardData(userId, currentRoleId);
	}
}

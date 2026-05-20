package com.moesd.tvet.mis.backend.application.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {

	private final JdbcTemplate jdbcTemplate;

	public List<Map<String, Object>> getDashboardData(Long userId, Long currentRoleId) {

		String sql = """
				SELECT *
				FROM dashboard_data
				WHERE user_id = ?
				AND role_id = ?
				""";

		return jdbcTemplate.queryForList(sql, userId, currentRoleId);
	}
}
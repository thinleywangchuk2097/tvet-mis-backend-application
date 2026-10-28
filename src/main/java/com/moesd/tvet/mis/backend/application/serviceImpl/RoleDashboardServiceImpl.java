package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.repository.RoleDashboardRepository;
import com.moesd.tvet.mis.backend.application.service.RoleDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleDashboardServiceImpl implements RoleDashboardService {

	private final RoleDashboardRepository repo;

	private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH);
	private static final int MAX_MONTHS = 24;

	@Override
	public Map<String, Object> getRoleOverview(int roleId, LocalDate from, LocalDate to) {
		// Default range: last 12 months
		LocalDate end = to != null ? to : LocalDate.now();
		LocalDate start = from != null ? from : end.minusMonths(11).withDayOfMonth(1);
		if (start.isAfter(end)) {
			LocalDate tmp = start;
			start = end;
			end = tmp;
		}
		final LocalDate f = start, t = end;

		Map<String, Object> out = new LinkedHashMap<>();
		out.put("fromDate", f.toString());
		out.put("toDate", t.toString());

		// ---- System-wide TVET statistics ----
		List<Map<String, Object>> byOwnership = safe("institutesByOwnership", repo::institutesByOwnership, List.of());
		List<Map<String, Object>> enrolment = safe("enrolment", repo::enrolmentByProgrammeType, List.of());
		long institutes = byOwnership.stream().mapToLong(r -> toLong(r.get("value"))).sum();

		Map<String, Object> tvet = new LinkedHashMap<>();
		tvet.put("institutes", institutes);
		tvet.put("institutesByOwnership", byOwnership);
		tvet.put("accreditedCourses",
				safe("accredited", () -> repo.count("SELECT COUNT(*) FROM tbl_accredited_course_dtls"), 0L));
		tvet.put("nonAccreditedCourses",
				safe("nonAccredited", () -> repo.count("SELECT COUNT(*) FROM tbl_non_accredited_course_dtls"), 0L));
		tvet.put("trainers", safe("trainers",
				() -> repo.count("SELECT COUNT(*) FROM tbl_trainer_dtls WHERE status_id = 1"), 0L));
		tvet.put("enrolmentByProgrammeType", enrolment);
		tvet.put("traineesEnrolled", enrolment.stream().mapToLong(r -> toLong(r.get("value"))).sum());
		tvet.put("traineesCertified", enrolment.stream().mapToLong(r -> toLong(r.get("certified"))).sum());
		tvet.put("totCertified", safe("totCertified", () -> repo.count(
				"SELECT COUNT(*) FROM tbl_tot_program_trainer_applied_dtls WHERE certificate_no IS NOT NULL"), 0L));
		out.put("tvet", tvet);

		// ---- Applications handled by this role ----
		Map<String, Object> apps = new LinkedHashMap<>();
		apps.put("total", safe("appCount", () -> repo.applicationCount(roleId, f, t), 0L));
		apps.put("byStatus", safe("byStatus", () -> repo.applicationsByStatus(roleId, f, t), List.of()));
		apps.put("byService", safe("byService", () -> repo.applicationsByService(roleId, f, t), List.of()));
		apps.put("byMonth", fillMonths(f, t,
				safe("byMonth", () -> repo.applicationsByMonth(roleId, f, t), List.<Map<String, Object>>of())));

		List<Map<String, Object>> recent = safe("recent",
				() -> normalizeAll(repo.recentApplications(roleId, f, t)), List.of());
		Map<Object, Map<String, Object>> latest = new LinkedHashMap<>();
		for (Map<String, Object> r : recent) latest.putIfAbsent(r.get("application_no"), r);
		apps.put("recent", new ArrayList<>(latest.values()));
		out.put("applications", apps);

		out.put("pendingTasks", safe("pendingTasks", () -> repo.pendingTasks(roleId),
				Map.of("total", 0, "unclaimed", 0, "claimed", 0)));
		return out;
	}

	@Override
	public Map<String, Object> getAdminOverview(LocalDate from, LocalDate to) {
		LocalDate end = to != null ? to : LocalDate.now();
		LocalDate start = from != null ? from : end.minusMonths(11).withDayOfMonth(1);
		if (start.isAfter(end)) {
			LocalDate tmp = start;
			start = end;
			end = tmp;
		}
		final LocalDate f = start, t = end;

		Map<String, Object> out = new LinkedHashMap<>();
		out.put("users", safe("userTotals", repo::userTotals, Map.of()));
		out.put("usersByRole", safe("usersByRole", repo::usersByRole, List.of()));
		out.put("usersByMonth",
				fillMonths(f, t, safe("usersByMonth", () -> repo.usersByMonth(f, t), List.<Map<String, Object>>of())));
		out.put("roles", safe("roleTotals", repo::roleTotals, Map.of()));
		out.put("dropdowns", safe("dropdownTotals", repo::dropdownTotals, Map.of()));
		out.put("services", safe("serviceTotals", repo::serviceTotals, Map.of()));
		out.put("pendingTasksByRole", safe("pendingTasksByRole", repo::pendingTasksByRole, List.of()));
		return out;
	}

	// ---------------- helpers ----------------

	private <T> T safe(String section, Supplier<T> supplier, T fallback) {
		try {
			T v = supplier.get();
			return v == null ? fallback : v;
		} catch (Exception e) {
			log.warn("Role dashboard section '{}' failed: {}", section, e.getMessage());
			return fallback;
		}
	}

	private List<Map<String, Object>> fillMonths(LocalDate from, LocalDate to, List<Map<String, Object>> rows) {
		Map<String, Long> byMonth = new HashMap<>();
		for (Map<String, Object> r : rows) byMonth.put(String.valueOf(r.get("ym")), toLong(r.get("value")));
		List<Map<String, Object>> out = new ArrayList<>();
		YearMonth endYm = YearMonth.from(to);
		YearMonth ym = YearMonth.from(from);
		if (ym.plusMonths(MAX_MONTHS - 1).isBefore(endYm)) ym = endYm.minusMonths(MAX_MONTHS - 1);
		for (; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("month", ym.toString());
			m.put("label", ym.format(MONTH_LABEL));
			m.put("value", byMonth.getOrDefault(ym.toString(), 0L));
			out.add(m);
		}
		return out;
	}

	private long toLong(Object v) {
		if (v instanceof Number n) return n.longValue();
		try {
			return v == null ? 0 : Long.parseLong(String.valueOf(v));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private List<Map<String, Object>> normalizeAll(List<Map<String, Object>> rows) {
		List<Map<String, Object>> out = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			Map<String, Object> m = new LinkedHashMap<>();
			row.forEach((k, v) -> {
				if (v instanceof java.sql.Timestamp ts) m.put(k, ts.toLocalDateTime().toString());
				else if (v instanceof java.sql.Date d) m.put(k, d.toLocalDate().toString());
				else if (v instanceof java.util.Date d) m.put(k, d.toInstant().toString());
				else if (v instanceof TemporalAccessor ta) m.put(k, ta.toString());
				else m.put(k, v);
			});
			out.add(m);
		}
		return out;
	}
}

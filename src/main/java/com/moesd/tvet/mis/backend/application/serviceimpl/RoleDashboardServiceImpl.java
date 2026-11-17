package com.moesd.tvet.mis.backend.application.serviceimpl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

	// keys of the dashboard maps (query columns and JSON fields)
	private static final String VALUE = "value";
	private static final String TOTAL = "total";
	private static final String PENDING_TASKS = "pendingTasks";
	private static final String RESPONDED = "responded";
	private static final String RECENT = "recent";
	private static final String BY_STATUS = "byStatus";
	private static final String APPLICATION_NO = "application_no";
	private static final String SENT = "sent";
	private static final String UNCLAIMED = "unclaimed";
	private static final String STATUS_NAME = "status_name";
	private static final String LAST_SENT = "last_sent";
	private static final String CLAIMED = "claimed";
	private static final String BY_TYPE = "byType";
	private static final String BY_MONTH = "byMonth";

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
		final LocalDate f = start;
		final LocalDate t = end;

		Map<String, Object> out = new LinkedHashMap<>();
		out.put("fromDate", f.toString());
		out.put("toDate", t.toString());

		// ---- System-wide TVET statistics ----
		List<Map<String, Object>> byOwnership = safe("institutesByOwnership", repo::institutesByOwnership, List.of());
		List<Map<String, Object>> enrolment = safe("enrolment", repo::enrolmentByProgrammeType, List.of());
		long institutes = byOwnership.stream().mapToLong(r -> toLong(r.get(VALUE))).sum();

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
		tvet.put("traineesEnrolled", enrolment.stream().mapToLong(r -> toLong(r.get(VALUE))).sum());
		tvet.put("traineesCertified", enrolment.stream().mapToLong(r -> toLong(r.get("certified"))).sum());
		tvet.put("totCertified", safe("totCertified", () -> repo.count(
				"SELECT COUNT(*) FROM tbl_tot_program_trainer_applied_dtls WHERE certificate_no IS NOT NULL"), 0L));
		out.put("tvet", tvet);

		// ---- Applications handled by this role ----
		Map<String, Object> apps = new LinkedHashMap<>();
		apps.put(TOTAL, safe("appCount", () -> repo.applicationCount(roleId, f, t), 0L));
		apps.put(BY_STATUS, safe(BY_STATUS, () -> repo.applicationsByStatus(roleId, f, t), List.of()));
		apps.put("byService", safe("byService", () -> repo.applicationsByService(roleId, f, t), List.of()));
		apps.put(BY_MONTH, fillMonths(f, t,
				safe(BY_MONTH, () -> repo.applicationsByMonth(roleId, f, t), List.<Map<String, Object>>of())));

		List<Map<String, Object>> recent = safe(RECENT,
				() -> normalizeAll(repo.recentApplications(roleId, f, t)), List.of());
		Map<Object, Map<String, Object>> latest = new LinkedHashMap<>();
		for (Map<String, Object> r : recent) latest.putIfAbsent(r.get(APPLICATION_NO), r);
		apps.put(RECENT, new ArrayList<>(latest.values()));
		out.put("applications", apps);

		out.put(PENDING_TASKS, safe(PENDING_TASKS, () -> repo.pendingTasks(roleId),
				Map.of(TOTAL, 0, UNCLAIMED, 0, CLAIMED, 0)));
		return out;
	}

	@Override
	public Map<String, Object> getTttrcOverview(int roleId) {
		Map<String, Object> out = new LinkedHashMap<>();

		Map<String, Object> tot = new LinkedHashMap<>();
		tot.put("totals", safe("totTotals", repo::totTotals, Map.of()));
		tot.put("announcements", safe("totAnnouncements", () -> normalizeAll(repo.totAnnouncements()), List.of()));
		tot.put("byResult", safe("totResults", repo::totResults, List.of()));
		tot.put("byGrade", safe("totGrades", repo::totGrades, List.of()));
		tot.put("byInstitute", safe("totByInstitute", repo::totByInstitute, List.of()));
		out.put("tot", tot);

		Map<String, Object> curriculum = new LinkedHashMap<>();
		List<Map<String, Object>> byStatus = safe("curriculumByStatus", repo::curriculumByStatus, List.of());
		curriculum.put(TOTAL, byStatus.stream().mapToLong(r -> toLong(r.get(VALUE))).sum());
		curriculum.put(BY_STATUS, byStatus);
		curriculum.put(BY_TYPE, safe("curriculumByType", repo::curriculumByType, List.of()));
		curriculum.put("byLevel", safe("curriculumByLevel", repo::curriculumByLevel, List.of()));
		curriculum.put(RECENT, safe("recentCurricula", () -> normalizeAll(repo.recentCurricula()), List.of()));
		out.put("curriculum", curriculum);

		// CMFG monitoring: applications (with Yes/No answers) + workflow status, matched by application no
		Map<String, Object> cmfg = new LinkedHashMap<>();
		Map<String, Map<String, Object>> statusByApp = new LinkedHashMap<>();
		for (Map<String, Object> r : safe("cmfgStatuses", repo::cmfgStatuses, List.<Map<String, Object>>of())) {
			statusByApp.put(String.valueOf(r.get(APPLICATION_NO)), r);
		}
		List<Map<String, Object>> apps = safe("cmfgApplications",
				() -> normalizeAll(repo.cmfgApplications()), List.<Map<String, Object>>of());
		long items = 0;
		long yes = 0;
		Set<Object> institutes = new HashSet<>();
		for (Map<String, Object> a : apps) {
			Map<String, Object> st = statusByApp.get(String.valueOf(a.get(APPLICATION_NO)));
			a.put("status_id", st != null ? st.get("status_id") : null);
			a.put(STATUS_NAME, st != null && st.get(STATUS_NAME) != null ? st.get(STATUS_NAME) : "Pending");
			items += toLong(a.get("total_items"));
			yes += toLong(a.get("yes_count"));
			if (a.get("institute_name") != null) institutes.add(a.get("institute_name"));
		}
		cmfg.put(TOTAL, apps.size());
		cmfg.put("institutes", institutes.size());
		cmfg.put("compliancePct", items > 0 ? Math.round(yes * 100.0 / items) : 0);
		cmfg.put(BY_STATUS, safe("cmfgByStatus", repo::cmfgByStatus, List.of()));
		cmfg.put(RECENT, apps.size() > 8 ? new ArrayList<>(apps.subList(0, 8)) : apps);
		out.put("cmfg", cmfg);

		out.put(PENDING_TASKS, safe(PENDING_TASKS, () -> repo.pendingTasks(roleId),
				Map.of(TOTAL, 0, UNCLAIMED, 0, CLAIMED, 0)));
		return out;
	}

	@Override
	public Map<String, Object> getDwps1Overview(int roleId) {
		Map<String, Object> out = new LinkedHashMap<>();

		// ---- Curriculum ----
		Map<String, Object> curriculum = new LinkedHashMap<>();
		List<Map<String, Object>> byStatus = safe("curriculumByStatus", repo::curriculumByStatus, List.of());
		curriculum.put(TOTAL, byStatus.stream().mapToLong(r -> toLong(r.get(VALUE))).sum());
		curriculum.put(BY_STATUS, byStatus);
		curriculum.put(BY_TYPE, safe("curriculumByType", repo::curriculumByType, List.of()));
		curriculum.put("byLevel", safe("curriculumByLevel", repo::curriculumByLevel, List.of()));
		curriculum.put(RECENT, safe("recentCurricula", () -> normalizeAll(repo.recentCurricula()), List.of()));
		out.put("curriculum", curriculum);

		// ---- Tracer: surveys matched with their send / response counts ----
		Map<String, Map<String, Object>> sendsBySurvey = new LinkedHashMap<>();
		for (Map<String, Object> r : safe("tracerSendsBySurvey", repo::tracerSendsBySurvey,
				List.<Map<String, Object>>of())) {
			sendsBySurvey.put(String.valueOf(r.get(APPLICATION_NO)), r);
		}
		List<Map<String, Object>> surveys = safe("tracerSurveys", () -> normalizeAll(repo.tracerSurveys()),
				List.<Map<String, Object>>of());
		long sent = 0;
		long responded = 0;
		for (Map<String, Object> sv : surveys) {
			Map<String, Object> sd = sendsBySurvey.get(String.valueOf(sv.get(APPLICATION_NO)));
			long sn = sd != null ? toLong(sd.get(SENT)) : 0;
			long rp = sd != null ? toLong(sd.get(RESPONDED)) : 0;
			sv.put(SENT, sn);
			sv.put(RESPONDED, rp);
			sv.put(LAST_SENT, sd != null && sd.get(LAST_SENT) != null ? String.valueOf(sd.get(LAST_SENT)) : null);
		}
		// totals over all sends (also counts sends whose question set was removed)
		for (Map<String, Object> sd : sendsBySurvey.values()) {
			sent += toLong(sd.get(SENT));
			responded += toLong(sd.get(RESPONDED));
		}

		LocalDate end = LocalDate.now();
		LocalDate start = end.minusMonths(11).withDayOfMonth(1);
		Map<String, Object> tracer = new LinkedHashMap<>();
		tracer.put("surveys", surveys.size());
		tracer.put(SENT, sent);
		tracer.put(RESPONDED, responded);
		tracer.put("responseRate", sent > 0 ? Math.round(responded * 100.0 / sent) : 0);
		tracer.put("bySurvey", surveys);
		tracer.put(BY_TYPE, safe("tracerSendsByType", repo::tracerSendsByType, List.of()));
		tracer.put(BY_MONTH, fillMonths(start, end,
				safe("tracerSendsByMonth", () -> repo.tracerSendsByMonth(start, end),
						List.<Map<String, Object>>of())));
		tracer.put("respondedByMonth", fillMonths(start, end,
				safe("tracerRespondedByMonth", () -> repo.tracerSendsByMonth(start, end).stream()
						.map(r -> {
							Map<String, Object> m = new LinkedHashMap<>();
							m.put("ym", r.get("ym"));
							m.put(VALUE, r.get(RESPONDED));
							return m;
						}).toList(), List.<Map<String, Object>>of())));
		out.put("tracer", tracer);

		out.put(PENDING_TASKS, safe(PENDING_TASKS, () -> repo.pendingTasks(roleId),
				Map.of(TOTAL, 0, UNCLAIMED, 0, CLAIMED, 0)));
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
		final LocalDate f = start;
		final LocalDate t = end;

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
		for (Map<String, Object> r : rows) byMonth.put(String.valueOf(r.get("ym")), toLong(r.get(VALUE)));
		List<Map<String, Object>> out = new ArrayList<>();
		YearMonth endYm = YearMonth.from(to);
		YearMonth ym = YearMonth.from(from);
		if (ym.plusMonths(MAX_MONTHS - 1).isBefore(endYm)) ym = endYm.minusMonths(MAX_MONTHS - 1);
		for (; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("month", ym.toString());
			m.put("label", ym.format(MONTH_LABEL));
			m.put(VALUE, byMonth.getOrDefault(ym.toString(), 0L));
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
				else if (v instanceof Date d) m.put(k, d.toInstant().toString());
				else if (v instanceof TemporalAccessor ta) m.put(k, ta.toString());
				else m.put(k, v);
			});
			out.add(m);
		}
		return out;
	}
}

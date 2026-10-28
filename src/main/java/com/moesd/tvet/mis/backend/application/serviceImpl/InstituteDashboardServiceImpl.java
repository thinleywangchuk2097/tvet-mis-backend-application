package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.repository.InstituteDashboardRepository;
import com.moesd.tvet.mis.backend.application.service.InstituteDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstituteDashboardServiceImpl implements InstituteDashboardService {

	private final InstituteDashboardRepository repo;

	private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH);

	@Override
	public Map<String, Object> getInstituteDashboard(String registrationNo) {
		Map<String, Object> raw = repo.findInstitute(registrationNo);
		if (raw == null || raw.get("institute_id") == null) {
			throw new RecordNotFoundException("Institute not found for registration no: " + registrationNo);
		}
		Long instituteId = toLong(raw.get("institute_id"));
		Map<String, Object> institute = normalize(raw);

		Map<String, Object> out = new LinkedHashMap<>();
		out.put("institute", institute);

		// ---- Trainers / staff / courses ----
		List<Map<String, Object>> trainersByEmployment = safe("trainers",
				() -> normalizeAll(repo.trainersByEmployment(instituteId)), List.of());
		long trainers = trainersByEmployment.stream().mapToLong(r -> toLong(r.get("value"))).sum();

		// ---- Programmes ----
		List<Map<String, Object>> programmes = safe("programmes",
				() -> normalizeAll(repo.programmes(instituteId)), List.of());
		LocalDate today = LocalDate.now();
		int ongoing = 0, upcoming = 0, completed = 0;
		for (Map<String, Object> p : programmes) {
			String status = programmeStatus(p, today);
			p.put("status", status);
			switch (status) {
				case "ONGOING" -> ongoing++;
				case "UPCOMING" -> upcoming++;
				case "COMPLETED" -> completed++;
				default -> { }
			}
		}

		// ---- Trainees ----
		Map<String, Object> traineeTotals = safe("traineeTotals",
				() -> repo.traineeTotals(instituteId), Map.of("total", 0, "graduates", 0));

		Map<String, Object> metrics = new LinkedHashMap<>();
		metrics.put("trainers", trainers);
		metrics.put("staff", safe("staff", () -> repo.staffCount(instituteId), 0L));
		metrics.put("accreditedCourses", safe("accredited", () -> repo.accreditedCourseCount(instituteId), 0L));
		metrics.put("nonAccreditedCourses",
				safe("nonAccredited", () -> repo.nonAccreditedCourseCount(instituteId), 0L));
		metrics.put("programmes", programmes.size());
		metrics.put("ongoingProgrammes", ongoing);
		metrics.put("upcomingProgrammes", upcoming);
		metrics.put("completedProgrammes", completed);
		metrics.put("trainees", toLong(traineeTotals.get("total")));
		metrics.put("graduates", toLong(traineeTotals.get("graduates")));
		out.put("metrics", metrics);

		out.put("trainersByEmployment", trainersByEmployment);
		out.put("genderDistribution",
				safe("gender", () -> normalizeAll(repo.traineesByGender(instituteId)), List.of()));
		out.put("monthlyEnrollments",
				fillMonths(safe("monthlyEnrollments", () -> repo.monthlyEnrollments(instituteId), List.of())));
		out.put("programmesPerMonth",
				fillMonths(safe("programmesPerMonth", () -> repo.programmesPerMonth(instituteId), List.of())));
		out.put("programmeList", programmes);

		Map<String, Object> tot = safe("tot", () -> normalize(repo.totSummary(instituteId)), Map.of());
		out.put("tot", tot);

		// ---- Recent applications (one row per application no, latest first) ----
		List<String> appNos = new ArrayList<>(new LinkedHashSet<>(
				safe("applicationNos", () -> repo.applicationNos(instituteId), List.<String>of())));
		appNos.removeIf(a -> a == null || a.isBlank());
		List<Map<String, Object>> workflows = safe("workflows", () -> normalizeAll(repo.workflows(appNos)),
				List.of());
		Map<Object, Map<String, Object>> latest = new LinkedHashMap<>();
		for (Map<String, Object> w : workflows) {
			latest.putIfAbsent(w.get("application_no"), w);
		}
		out.put("recentApplications", new ArrayList<>(latest.values()));

		return out;
	}

	// ---------------- helpers ----------------

	/** Runs one dashboard section; a failure is logged and that section falls back to a default. */
	private <T> T safe(String section, Supplier<T> supplier, T fallback) {
		try {
			T value = supplier.get();
			return value == null ? fallback : value;
		} catch (Exception e) {
			log.warn("Institute dashboard section '{}' failed: {}", section, e.getMessage());
			return fallback;
		}
	}

	private String programmeStatus(Map<String, Object> p, LocalDate today) {
		LocalDate start = toDate(p.get("course_start_date"));
		LocalDate end = toDate(p.get("course_end_date"));
		if (start == null) return "UNKNOWN";
		if (today.isBefore(start)) return "UPCOMING";
		if (end != null && today.isAfter(end)) return "COMPLETED";
		return "ONGOING";
	}

	private LocalDate toDate(Object v) {
		if (v == null) return null;
		try {
			return LocalDate.parse(String.valueOf(v).substring(0, 10));
		} catch (Exception e) {
			return null;
		}
	}

	/** Turns [{ym:"2026-03", value:4}] into the last 12 months with zeros filled in. */
	private List<Map<String, Object>> fillMonths(List<Map<String, Object>> rows) {
		Map<String, Long> byMonth = new HashMap<>();
		for (Map<String, Object> r : rows) {
			byMonth.put(String.valueOf(r.get("ym")), toLong(r.get("value")));
		}
		List<Map<String, Object>> out = new ArrayList<>();
		YearMonth now = YearMonth.now();
		for (int i = 11; i >= 0; i--) {
			YearMonth ym = now.minusMonths(i);
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
		for (Map<String, Object> r : rows) out.add(normalize(r));
		return out;
	}

	/** Dates/timestamps -> ISO strings so the JSON is predictable. */
	private Map<String, Object> normalize(Map<String, Object> row) {
		if (row == null) return null;
		Map<String, Object> out = new LinkedHashMap<>();
		row.forEach((k, v) -> {
			if (v instanceof java.sql.Timestamp ts) out.put(k, ts.toLocalDateTime().toString());
			else if (v instanceof java.sql.Date d) out.put(k, d.toLocalDate().toString());
			else if (v instanceof java.util.Date d) out.put(k, d.toInstant().toString());
			else if (v instanceof TemporalAccessor t) out.put(k, t.toString());
			else out.put(k, v);
		});
		return out;
	}
}

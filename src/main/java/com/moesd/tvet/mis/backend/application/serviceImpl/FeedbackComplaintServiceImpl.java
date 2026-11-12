package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.moesd.tvet.mis.backend.application.dto.ComplaintActiondto;
import com.moesd.tvet.mis.backend.application.dto.Documentdto;
import com.moesd.tvet.mis.backend.application.dto.FeedbackComplaintdto;
import com.moesd.tvet.mis.backend.application.dto.FileDownloaddto;
import com.moesd.tvet.mis.backend.application.model.FeedbackComplaint;
import com.moesd.tvet.mis.backend.application.model.FeedbackComplaintFile;
import com.moesd.tvet.mis.backend.application.repository.FeedbackComplaintFileRepository;
import com.moesd.tvet.mis.backend.application.repository.FeedbackComplaintRepository;
import com.moesd.tvet.mis.backend.application.service.FeedbackComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackComplaintServiceImpl implements FeedbackComplaintService {

	public static final String FEEDBACK = "FEEDBACK";
	public static final String COMPLAINT = "COMPLAINT";
	private static final Set<String> STATUSES = Set.of("SUBMITTED", "UNDER_REVIEW", "RESOLVED", "REJECTED");
	private static final Set<String> URGENCIES = Set.of("low", "medium", "high", "critical");

	// complaint attachments
	// any number of files; 10 MB each and 50 MB together (public endpoint, no login)
	private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;
	private static final long MAX_TOTAL_BYTES = 50L * 1024 * 1024;
	private static final Set<String> ALLOWED_EXT = Set.of("pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png");

	private final FeedbackComplaintRepository repository;
	private final FeedbackComplaintFileRepository fileRepository;

	@Value("${file.store.path}")
	private String fileStorePath;

	// ── public submit ────────────────────────────────────────────────────
	@Override
	@Transactional
	public Map<String, Object> submit(FeedbackComplaintdto r) {
		if (r == null) {
			throw bad("Request body is required");
		}
		String type = r.getType() == null ? "" : r.getType().trim().toUpperCase(Locale.ROOT);
		if (!FEEDBACK.equals(type) && !COMPLAINT.equals(type)) {
			throw bad("Type must be FEEDBACK or COMPLAINT");
		}
		boolean complaint = COMPLAINT.equals(type);
		// only complaints can be anonymous; then no identity is stored at all
		boolean anonymous = complaint && Boolean.TRUE.equals(r.getAnonymous());

		FeedbackComplaint e = FeedbackComplaint.builder()
				.type(type)
				.anonymous(anonymous)
				.submitterRole(limit(required(r.getSubmitterRole(), "Role"), 50, "Role"))
				.category(limit(required(r.getCategory(), "Category"), 50, "Category"))
				.status(complaint ? "SUBMITTED" : null)
				.createdAt(new Date())
				.build();

		if (!anonymous) {
			e.setName(limit(required(r.getName(), "Name"), 200, "Name"));
			String email = limit(required(r.getEmail(), "Email"), 200, "Email");
			if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
				throw bad("Enter a valid email");
			}
			e.setEmail(email);
			e.setContactNo(limit(trim(r.getContactNo()), 30, "Contact number"));
		}

		List<Documentdto> attachments = complaint ? validAttachments(r.getAttachments()) : List.of();

		if (complaint) {
			String urgency = required(r.getUrgency(), "Urgency").toLowerCase(Locale.ROOT);
			if (!URGENCIES.contains(urgency)) {
				throw bad("Choose an urgency level");
			}
			e.setUrgency(urgency);
			e.setIncidentDetails(limit(required(r.getIncidentDetails(), "Incident details"), 5000, "Incident details"));
			e.setDesiredResolution(limit(trim(r.getDesiredResolution()), 3000, "Desired resolution"));
			e.setAgainstPerson(limit(trim(r.getAgainstPerson()), 200, "Against"));
			e.setInstitution(limit(trim(r.getInstitution()), 300, "Institution"));
			e.setIncidentDate(parseDate(r.getIncidentDate()));
		} else {
			if (r.getRating() == null || r.getRating() < 1 || r.getRating() > 5) {
				throw bad("Rating must be between 1 and 5");
			}
			if (r.getRecommend() == null) {
				throw bad("Please answer whether you would recommend us");
			}
			e.setRating(r.getRating());
			e.setRecommend(r.getRecommend());
			e.setExperience(limit(required(r.getExperience(), "Experience"), 5000, "Experience"));
			e.setSuggestions(limit(trim(r.getSuggestions()), 3000, "Suggestions"));
		}

		FeedbackComplaint saved = repository.save(e);
		// FB-2026-000012 / CP-2026-000045
		saved.setReferenceNo(String.format("%s-%d-%06d", complaint ? "CP" : "FB", Year.now().getValue(), saved.getId()));
		saved = repository.save(saved);
		storeAttachments(saved, attachments);

		Map<String, Object> out = new LinkedHashMap<>();
		out.put("referenceNo", saved.getReferenceNo());
		out.put("type", saved.getType());
		return out;
	}

	// ── lists ────────────────────────────────────────────────────────────
	@Override
	public List<Map<String, Object>> getFeedback() {
		return repository.findByTypeOrderByCreatedAtDesc(FEEDBACK).stream().map(this::feedbackMap).toList();
	}

	@Override
	public List<Map<String, Object>> getComplaints() {
		return repository.findByTypeOrderByCreatedAtDesc(COMPLAINT).stream().map(this::complaintMap).toList();
	}

	// ── TVET QC action ───────────────────────────────────────────────────
	@Override
	@Transactional
	public Map<String, Object> updateComplaint(Long id, ComplaintActiondto r, String actionBy) {
		FeedbackComplaint e = repository.findById(id)
				.filter(x -> COMPLAINT.equals(x.getType()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
		if (r == null) {
			throw bad("Request body is required");
		}
		String status = required(r.getStatus(), "Status").toUpperCase(Locale.ROOT);
		if (!STATUSES.contains(status)) {
			throw bad("Status must be SUBMITTED, UNDER_REVIEW, RESOLVED or REJECTED");
		}
		String action = limit(trim(r.getActionTaken()), 5000, "Action taken");
		if (("RESOLVED".equals(status) || "REJECTED".equals(status)) && action == null) {
			throw bad("Describe the action taken before closing the complaint");
		}
		e.setStatus(status);
		e.setActionTaken(action);
		e.setActionBy(actionBy);
		e.setActionAt(new Date());
		return complaintMap(repository.save(e));
	}

	@Override
	public FileDownloaddto downloadAttachment(Long complaintId, Long fileId) {
		FeedbackComplaintFile f = fileRepository.findByIdAndComplaintId(fileId, complaintId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
		Path path = Paths.get(f.getUploadUrl());
		if (!Files.isRegularFile(path)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
		}
		try {
			String type = f.getContentType() == null || f.getContentType().isBlank()
					? "application/octet-stream" : f.getContentType();
			return new FileDownloaddto(Files.readAllBytes(path), type, false, f.getFileName());
		} catch (IOException e) {
			log.error("Could not read complaint file {}", fileId, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read file");
		}
	}

	// ── attachments ──────────────────────────────────────────────────────
	private List<Documentdto> validAttachments(List<Documentdto> files) {
		if (files == null || files.isEmpty()) {
			return List.of();
		}
		List<Documentdto> list = files.stream().filter(f -> f != null && f.getContent() != null && f.getContent().length > 0).toList();
		long total = 0;
		for (Documentdto f : list) {
			String name = cleanName(f.getName());
			if (!ALLOWED_EXT.contains(ext(name))) {
				throw bad("File type of " + name + " is not allowed");
			}
			if (f.getContent().length > MAX_FILE_BYTES) {
				throw bad(name + " is larger than 10 MB");
			}
			total += f.getContent().length;
		}
		if (total > MAX_TOTAL_BYTES) {
			throw bad("Attachments together must be 50 MB or less");
		}
		return list;
	}

	private void storeAttachments(FeedbackComplaint complaint, List<Documentdto> files) {
		if (files.isEmpty()) {
			return;
		}
		File dir = Paths.get(fileStorePath, "feedback-complaint", complaint.getReferenceNo()).toFile();
		if (!dir.exists() && !dir.mkdirs()) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create upload folder");
		}
		for (Documentdto f : files) {
			String name = cleanName(f.getName());
			File target = new File(dir, UUID.randomUUID().toString().replace("-", "") + "_" + name);
			try {
				Files.write(target.toPath(), f.getContent());
			} catch (IOException e) {
				log.error("Could not save complaint file {}", name, e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save " + name);
			}
			fileRepository.save(FeedbackComplaintFile.builder()
					.complaintId(complaint.getId())
					.fileName(name)
					.contentType(f.getContentType())
					.fileSize((long) f.getContent().length)
					.uploadUrl(target.getPath())
					.createdAt(new Date())
					.build());
		}
	}

	private static String cleanName(String name) {
		String n = name == null ? "" : Paths.get(name.replace('\\', '/')).getFileName().toString();
		n = n.replaceAll("[\\r\\n\"<>:|?*]", "_").trim();
		if (n.isEmpty() || n.startsWith(".")) {
			throw bad("Invalid file name");
		}
		return n.length() > 200 ? n.substring(n.length() - 200) : n;
	}

	private static String ext(String name) {
		int dot = name.lastIndexOf('.');
		return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

	// ── helpers ──────────────────────────────────────────────────────────
	private static String trim(String s) {
		return s == null || s.isBlank() ? null : s.trim();
	}

	private static String required(String s, String field) {
		String v = trim(s);
		if (v == null) {
			throw bad(field + " is required");
		}
		return v;
	}

	private static String limit(String s, int max, String field) {
		if (s != null && s.length() > max) {
			throw bad(field + " must be " + max + " characters or fewer");
		}
		return s;
	}

	private static LocalDate parseDate(String s) {
		String v = trim(s);
		if (v == null) {
			return null;
		}
		try {
			LocalDate d = LocalDate.parse(v.substring(0, Math.min(10, v.length())));
			if (d.isAfter(LocalDate.now())) {
				throw bad("Incident date cannot be in the future");
			}
			return d;
		} catch (DateTimeParseException ex) {
			throw bad("Incident date must be yyyy-MM-dd");
		}
	}

	private static ResponseStatusException bad(String m) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
	}

	// visible to every logged-in user: no email or phone number
	private Map<String, Object> feedbackMap(FeedbackComplaint e) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", e.getId());
		m.put("referenceNo", e.getReferenceNo());
		m.put("name", e.getName());
		m.put("submitterRole", e.getSubmitterRole());
		m.put("category", e.getCategory());
		m.put("rating", e.getRating());
		m.put("recommend", e.getRecommend());
		m.put("experience", e.getExperience());
		m.put("suggestions", e.getSuggestions());
		m.put("createdAt", e.getCreatedAt());
		return m;
	}

	// TVET QC only: full details
	private Map<String, Object> complaintMap(FeedbackComplaint e) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", e.getId());
		m.put("referenceNo", e.getReferenceNo());
		m.put("anonymous", Boolean.TRUE.equals(e.getAnonymous()));
		m.put("name", e.getName());
		m.put("email", e.getEmail());
		m.put("contactNo", e.getContactNo());
		m.put("submitterRole", e.getSubmitterRole());
		m.put("category", e.getCategory());
		m.put("urgency", e.getUrgency());
		m.put("incidentDate", e.getIncidentDate() == null ? null : e.getIncidentDate().toString());
		m.put("againstPerson", e.getAgainstPerson());
		m.put("institution", e.getInstitution());
		m.put("incidentDetails", e.getIncidentDetails());
		m.put("desiredResolution", e.getDesiredResolution());
		m.put("attachments", fileRepository.findByComplaintIdOrderByIdAsc(e.getId()).stream().map(f -> {
			Map<String, Object> a = new LinkedHashMap<>();
			a.put("id", f.getId());
			a.put("fileName", f.getFileName());
			a.put("fileSize", f.getFileSize());
			return a;
		}).toList());
		m.put("status", e.getStatus());
		m.put("actionTaken", e.getActionTaken());
		m.put("actionBy", e.getActionBy());
		m.put("actionAt", e.getActionAt());
		m.put("createdAt", e.getCreatedAt());
		return m;
	}
}

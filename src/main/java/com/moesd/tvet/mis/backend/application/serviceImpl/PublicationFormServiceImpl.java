package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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
import com.moesd.tvet.mis.backend.application.dto.Documentdto;
import com.moesd.tvet.mis.backend.application.dto.FileDownloaddto;
import com.moesd.tvet.mis.backend.application.dto.PublicationFormdto;
import com.moesd.tvet.mis.backend.application.model.PublicationForm;
import com.moesd.tvet.mis.backend.application.repository.PublicationFormRepository;
import com.moesd.tvet.mis.backend.application.service.PublicationFormService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicationFormServiceImpl implements PublicationFormService {

	public static final Set<String> CATEGORIES = Set.of("PUBLICATION", "FORM");

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt",
			"pptx", "odt", "ods", "csv", "txt", "jpg", "jpeg", "png", "zip");

	private static final long MAX_FILE_BYTES = 20L * 1024 * 1024; // 20 MB

	private final PublicationFormRepository repository;

	@Value("${file.store.path}")
	private String fileStorePath;

	// ── Read ────────────────────────────────────────────────────────────
	@Override
	public List<Map<String, Object>> getDocuments(String category) {
		List<PublicationForm> rows = (category == null || category.isBlank())
				? repository.findAllByOrderByPublishedDateDescIdDesc()
				: repository.findByCategoryOrderByPublishedDateDescIdDesc(normaliseCategory(category));
		return rows.stream().map(this::toMap).toList();
	}

	@Override
	public FileDownloaddto download(Long id) {
		PublicationForm doc = find(id);
		if (doc.getUploadUrl() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No file attached");
		}
		Path path = Paths.get(doc.getUploadUrl());
		if (!Files.isRegularFile(path)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
		}
		try {
			String type = doc.getContentType();
			if (type == null || type.isBlank()) {
				type = Files.probeContentType(path);
			}
			return new FileDownloaddto(Files.readAllBytes(path),
					type == null ? "application/octet-stream" : type, false, doc.getFileName());
		} catch (IOException e) {
			log.error("Could not read document {}", id, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read file");
		}
	}

	// ── Write (TVET QC) ─────────────────────────────────────────────────
	@Override
	@Transactional
	public Map<String, Object> create(PublicationFormdto request, String userId) {
		validate(request, true);
		PublicationForm doc = PublicationForm.builder()
				.category(normaliseCategory(request.getCategory()))
				.title(request.getTitle().trim())
				.publishedDate(parseDate(request.getPublishedDate()))
				.remarks(trimToNull(request.getRemarks()))
				.createdBy(userId)
				.createdAt(new Date())
				.build();
		storeFile(doc, request.getFile());
		return toMap(repository.save(doc));
	}

	@Override
	@Transactional
	public Map<String, Object> update(Long id, PublicationFormdto request, String userId) {
		PublicationForm doc = find(id);
		validate(request, false);
		doc.setCategory(normaliseCategory(request.getCategory()));
		doc.setTitle(request.getTitle().trim());
		doc.setPublishedDate(parseDate(request.getPublishedDate()));
		doc.setRemarks(trimToNull(request.getRemarks()));
		doc.setUpdatedBy(userId);
		doc.setUpdatedAt(new Date());
		if (hasContent(request.getFile())) {
			String oldFile = doc.getUploadUrl();
			storeFile(doc, request.getFile());
			deleteQuietly(oldFile);
		}
		return toMap(repository.save(doc));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		PublicationForm doc = find(id);
		repository.delete(doc);
		deleteQuietly(doc.getUploadUrl());
	}

	// ── Helpers ─────────────────────────────────────────────────────────
	private PublicationForm find(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
	}

	private void validate(PublicationFormdto request, boolean fileRequired) {
		if (request == null) {
			throw bad("Request body is required");
		}
		normaliseCategory(request.getCategory());
		if (request.getTitle() == null || request.getTitle().isBlank()) {
			throw bad("Title is required");
		}
		if (request.getTitle().trim().length() > 500) {
			throw bad("Title must be 500 characters or fewer");
		}
		parseDate(request.getPublishedDate());
		if (fileRequired && !hasContent(request.getFile())) {
			throw bad("Attachment is required");
		}
		if (hasContent(request.getFile())) {
			Documentdto f = request.getFile();
			if (f.getContent().length > MAX_FILE_BYTES) {
				throw bad("Attachment must be 20 MB or smaller");
			}
			String ext = extension(cleanFileName(f.getName()));
			if (!ALLOWED_EXTENSIONS.contains(ext)) {
				throw bad("File type ." + ext + " is not allowed");
			}
		}
	}

	private void storeFile(PublicationForm doc, Documentdto file) {
		String name = cleanFileName(file.getName());
		String uuid = UUID.randomUUID().toString().replace("-", "");
		File dir = Paths.get(fileStorePath, "publication-forms", doc.getCategory().toLowerCase(Locale.ROOT)).toFile();
		if (!dir.exists() && !dir.mkdirs()) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create upload folder");
		}
		File target = new File(dir, uuid + "_" + name);
		try {
			Files.write(target.toPath(), file.getContent());
		} catch (IOException e) {
			log.error("Could not save document file {}", name, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save file");
		}
		doc.setFileName(name);
		doc.setContentType(file.getContentType());
		doc.setFileSize((long) file.getContent().length);
		doc.setUploadUrl(target.getPath());
	}

	private static void deleteQuietly(String path) {
		if (path == null) {
			return;
		}
		try {
			Files.deleteIfExists(Paths.get(path));
		} catch (IOException e) {
			log.warn("Could not delete old document file {}", path, e);
		}
	}

	private static boolean hasContent(Documentdto f) {
		return f != null && f.getContent() != null && f.getContent().length > 0;
	}

	private static String cleanFileName(String name) {
		String n = name == null ? "" : Paths.get(name.replace('\\', '/')).getFileName().toString();
		n = n.replaceAll("[\\r\\n\"<>:|?*]", "_").trim();
		if (n.isEmpty() || n.startsWith(".")) {
			throw bad("Invalid file name");
		}
		return n;
	}

	private static String extension(String name) {
		int dot = name.lastIndexOf('.');
		return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

	private static String normaliseCategory(String category) {
		String c = category == null ? "" : category.trim().toUpperCase(Locale.ROOT);
		if (!CATEGORIES.contains(c)) {
			throw bad("Category must be PUBLICATION or FORM");
		}
		return c;
	}

	private static LocalDate parseDate(String value) {
		if (value == null || value.isBlank()) {
			throw bad("Published date is required");
		}
		try {
			return LocalDate.parse(value.trim().substring(0, Math.min(10, value.trim().length())));
		} catch (DateTimeParseException e) {
			throw bad("Published date must be yyyy-MM-dd");
		}
	}

	private static String trimToNull(String s) {
		return s == null || s.isBlank() ? null : s.trim();
	}

	private static ResponseStatusException bad(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}

	private Map<String, Object> toMap(PublicationForm d) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", d.getId());
		m.put("category", d.getCategory());
		m.put("title", d.getTitle());
		m.put("publishedDate", d.getPublishedDate() == null ? null : d.getPublishedDate().toString());
		m.put("remarks", d.getRemarks());
		m.put("fileName", d.getFileName());
		m.put("contentType", d.getContentType());
		m.put("fileSize", d.getFileSize());
		m.put("hasFile", d.getUploadUrl() != null);
		return m;
	}
}

package com.oj.platform.service;

import com.oj.platform.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Smallest-safe local-disk storage for Assessment Host verification documents
 * (Task: "Host Assessment Verification" - Section 9, File Upload Security). The
 * project has no pre-existing file-storage system, so this is intentionally minimal:
 * plain files on local disk, outside the web root, addressed only by a server-generated
 * random filename.
 *
 * Security properties (all requirements from the task spec):
 *  - File type is validated against a small allow-list of content types/extensions
 *    (images + PDF) - nothing else is accepted, so an uploaded file can never be an
 *    executable or script.
 *  - File size is capped (default 5 MB, configurable).
 *  - The filename actually used on disk is always a fresh random UUID plus a
 *    normalized extension from the allow-list - the user-supplied original filename is
 *    kept only as display metadata (AssessmentHostVerification.documentOriginalName)
 *    and is NEVER used to build a filesystem path, which also rules out path
 *    traversal via "../" or similar in the original name.
 *  - Storage root is a plain directory outside frontend/backend static/public folders
 *    (default "./storage/host-verification-documents", configurable via
 *    app.storage.host-verification-dir) - files here are never served by a static
 *    resource handler and are only ever reachable through the authenticated,
 *    ownership-checked download endpoint in AdminAssessmentHostVerificationController.
 *  - Every resolved path is re-checked to still be inside the storage root before any
 *    read/write, as defense in depth against path traversal.
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "pdf");

    @Value("${app.storage.host-verification-dir:./storage/host-verification-documents}")
    private String storageDir;

    @Value("${app.storage.max-file-size-bytes:5242880}")
    private long maxFileSizeBytes;

    private Path storageRoot;

    private Path getStorageRoot() {
        if (storageRoot == null) {
            try {
                Path root = Paths.get(storageDir).toAbsolutePath().normalize();
                Files.createDirectories(root);
                storageRoot = root;
            } catch (IOException e) {
                throw new IllegalStateException("Unable to initialize document storage directory: " + storageDir, e);
            }
        }
        return storageRoot;
    }

    /** Validates and stores the uploaded document, returning the server-generated
     *  filename it was stored under. Never trusts the client-supplied filename for
     *  anything beyond display. */
    public String storeVerificationDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A verification document file is required.");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new BadRequestException("File is too large. Maximum allowed size is "
                    + (maxFileSizeBytes / (1024 * 1024)) + " MB.");
        }

        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase(Locale.ROOT) : "";
        String extension = extractExtension(file.getOriginalFilename());

        if (!ALLOWED_CONTENT_TYPES.contains(contentType) || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file type. Only JPG, PNG, WEBP or PDF documents are accepted.");
        }

        String safeFileName = UUID.randomUUID() + "." + extension;
        Path target = getStorageRoot().resolve(safeFileName).normalize();

        if (!target.getParent().equals(getStorageRoot())) {
            // Defense in depth: should be unreachable since safeFileName is always a
            // freshly generated UUID, but never trust a resolved path without checking.
            throw new BadRequestException("Invalid file reference.");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Failed to store verification document", e);
            throw new IllegalStateException("Unable to store the uploaded document. Please try again.");
        }

        return safeFileName;
    }

    /** Loads a previously stored document by its server-generated filename only -
     *  never by anything derived from user input at read time. */
    public byte[] loadVerificationDocument(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            throw new BadRequestException("No document is available for this request.");
        }
        Path target = getStorageRoot().resolve(storedFileName).normalize();
        if (!target.getParent().equals(getStorageRoot()) || !Files.exists(target)) {
            throw new BadRequestException("No document is available for this request.");
        }
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            logger.error("Failed to read verification document", e);
            throw new IllegalStateException("Unable to read the stored document.");
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        // Only the extension is ever derived from the original filename - the rest of
        // it (including any path separators or "..") is discarded entirely.
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        return ext;
    }
}

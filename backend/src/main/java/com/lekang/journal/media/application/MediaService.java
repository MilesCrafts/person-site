package com.lekang.journal.media.application;

import com.lekang.journal.admin.application.AdminAuditService;
import com.lekang.journal.common.api.PageResponse;
import com.lekang.journal.common.error.ResourceNotFoundException;
import com.lekang.journal.media.api.AdminMediaAssetResponse;
import com.lekang.journal.media.infrastructure.MediaAssetEntity;
import com.lekang.journal.media.infrastructure.MediaAssetRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService {
    private static final long MAX_BYTES = 8L * 1024 * 1024;
    private static final int MAX_DIMENSION = 8000;
    private static final long MAX_PIXELS = 40_000_000L;
    private static final Set<String> JPEG_EXTENSIONS = Set.of("jpg", "jpeg");

    private final MediaAssetRepository repository;
    private final AdminAuditService auditService;
    private final Path storageRoot;

    public MediaService(
        MediaAssetRepository repository,
        AdminAuditService auditService,
        @Value("${journal.media.storage-root:./data/media}") String storageRoot
    ) {
        this.repository = repository;
        this.auditService = auditService;
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminMediaAssetResponse> list(int page, int size) {
        var pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );
        var result = repository.findByStatus("READY", pageable);
        return PageResponse.from(result, result.stream().map(AdminMediaAssetResponse::from).toList());
    }

    @Transactional
    public AdminMediaAssetResponse upload(MultipartFile file, String altText, Authentication authentication) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("image file is required");
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("image must not exceed 8 MB");

        String originalName = safeOriginalName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        if (!JPEG_EXTENSIONS.contains(extension) && !"png".equals(extension)) {
            throw new IllegalArgumentException("only JPEG and PNG images are supported");
        }

        try {
            byte[] source = file.getBytes();
            DecodedImage decoded = decode(source);
            String expectedFormat = JPEG_EXTENSIONS.contains(extension) ? "jpeg" : "png";
            if (!expectedFormat.equals(decoded.format())) {
                throw new IllegalArgumentException("file extension does not match image content");
            }
            String declaredType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
            String contentType = "jpeg".equals(decoded.format()) ? "image/jpeg" : "image/png";
            if (!declaredType.isBlank() && !contentType.equals(declaredType)) {
                throw new IllegalArgumentException("declared MIME type does not match image content");
            }

            Files.createDirectories(storageRoot);
            String storageKey = UUID.randomUUID() + ("jpeg".equals(decoded.format()) ? ".jpg" : ".png");
            Path target = resolveStorageKey(storageKey);
            Path temporary = Files.createTempFile(storageRoot, "upload-", ".tmp");
            try {
                if (!ImageIO.write(decoded.image(), decoded.format(), temporary.toFile())) {
                    throw new IOException("no image writer available");
                }
                try {
                    Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException exception) {
                    Files.move(temporary, target);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }

            long storedSize = Files.size(target);
            var asset = new MediaAssetEntity(
                "LOCAL_UPLOAD",
                storageKey,
                "/api/v1/media/pending/content",
                originalName,
                contentType,
                storedSize,
                decoded.width(),
                decoded.height(),
                normalizeAltText(altText),
                "READY",
                OffsetDateTime.now(ZoneOffset.UTC)
            );
            try {
                repository.saveAndFlush(asset);
                asset.assignExternalUrl("/api/v1/media/" + asset.getId() + "/content");
                auditService.record(authentication.getName(), "MEDIA_UPLOADED", "MEDIA_ASSET", asset.getId().toString());
                return AdminMediaAssetResponse.from(asset);
            } catch (RuntimeException exception) {
                Files.deleteIfExists(target);
                throw exception;
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new MediaStorageException("the image could not be stored", exception);
        }
    }

    @Transactional(readOnly = true)
    public MediaContent content(Long id) {
        MediaAssetEntity asset = repository.findById(id)
            .filter(candidate -> "READY".equals(candidate.getStatus()))
            .orElseThrow(() -> new ResourceNotFoundException("media asset not found"));
        if (!"LOCAL_UPLOAD".equals(asset.getSourceType()) || asset.getStorageKey() == null) {
            throw new ResourceNotFoundException("media content is not stored locally");
        }
        Path path = resolveStorageKey(asset.getStorageKey());
        if (!Files.isRegularFile(path)) throw new ResourceNotFoundException("media file not found");
        return new MediaContent(
            new FileSystemResource(path),
            asset.getContentType(),
            asset.getOriginalName(),
            asset.getSizeBytes()
        );
    }

    private DecodedImage decode(byte[] source) throws IOException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(source))) {
            if (stream == null) throw new IllegalArgumentException("invalid image data");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IllegalArgumentException("invalid image data");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if ("jpg".equals(format)) format = "jpeg";
                if (!"jpeg".equals(format) && !"png".equals(format)) {
                    throw new IllegalArgumentException("only JPEG and PNG images are supported");
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_DIMENSION || height > MAX_DIMENSION
                    || (long) width * height > MAX_PIXELS) {
                    throw new IllegalArgumentException("image dimensions are too large");
                }
                BufferedImage image = reader.read(0);
                if (image == null) throw new IllegalArgumentException("invalid image data");
                return new DecodedImage(image, format, width, height);
            } finally {
                reader.dispose();
            }
        }
    }

    private Path resolveStorageKey(String storageKey) {
        Path resolved = storageRoot.resolve(storageKey).normalize();
        if (!resolved.startsWith(storageRoot)) throw new IllegalArgumentException("invalid storage key");
        return resolved;
    }

    private static String safeOriginalName(String value) {
        if (value == null || value.isBlank()) return "image";
        String normalized = value.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (name.length() > 255) name = name.substring(name.length() - 255);
        return name.isBlank() ? "image" : name;
    }

    private static String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String normalizeAltText(String value) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        if (trimmed.length() > 255) throw new IllegalArgumentException("alt text must not exceed 255 characters");
        return trimmed;
    }

    private record DecodedImage(BufferedImage image, String format, int width, int height) {
    }
}

package com.lekang.journal.media.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lekang.journal.admin.application.AdminAuditService;
import com.lekang.journal.media.infrastructure.MediaAssetEntity;
import com.lekang.journal.media.infrastructure.MediaAssetRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class MediaServiceTest {
    Path storageRoot;

    @BeforeEach
    void createStorageRoot() throws Exception {
        Path testRoot = Path.of("target", "media-service-test").toAbsolutePath().normalize();
        storageRoot = testRoot.resolve(UUID.randomUUID().toString()).normalize();
        if (!storageRoot.startsWith(testRoot)) throw new IllegalStateException("invalid test storage path");
        Files.createDirectories(storageRoot);
    }

    @AfterEach
    void removeStorageRoot() throws Exception {
        Path testRoot = Path.of("target", "media-service-test").toAbsolutePath().normalize();
        if (storageRoot == null || !storageRoot.startsWith(testRoot) || !Files.exists(storageRoot)) return;
        try (var paths = Files.walk(storageRoot)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }

    @Test
    void validPngIsReencodedStoredAndReturned() throws Exception {
        MediaAssetRepository repository = mock(MediaAssetRepository.class);
        AdminAuditService auditService = mock(AdminAuditService.class);
        when(repository.saveAndFlush(any(MediaAssetEntity.class))).thenAnswer(invocation -> {
            MediaAssetEntity entity = invocation.getArgument(0);
            Field id = MediaAssetEntity.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(entity, 41L);
            return entity;
        });
        MediaService service = new MediaService(repository, auditService, storageRoot.toString());

        BufferedImage image = new BufferedImage(32, 18, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        var file = new MockMultipartFile("file", "cover.png", "image/png", bytes.toByteArray());

        var response = service.upload(
            file,
            "Article cover",
            UsernamePasswordAuthenticationToken.authenticated("admin", "", java.util.List.of())
        );

        assertThat(response.id()).isEqualTo(41L);
        assertThat(response.url()).isEqualTo("/api/v1/media/41/content");
        assertThat(response.width()).isEqualTo(32);
        assertThat(response.height()).isEqualTo(18);
        try (var files = Files.list(storageRoot)) {
            assertThat(files.filter(Files::isRegularFile).count()).isEqualTo(1);
        }
    }

    @Test
    void rejectsUnsupportedExtensionBeforeWriting() {
        MediaService service = new MediaService(
            mock(MediaAssetRepository.class),
            mock(AdminAuditService.class),
            storageRoot.toString()
        );
        var file = new MockMultipartFile("file", "cover.svg", "image/svg+xml", "<svg/>".getBytes());

        assertThatThrownBy(() -> service.upload(
            file,
            null,
            UsernamePasswordAuthenticationToken.authenticated("admin", "", java.util.List.of())
        )).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("JPEG and PNG");
    }
}

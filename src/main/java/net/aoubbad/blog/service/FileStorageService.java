package net.aoubbad.blog.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024; // 5MB
    private final Path uploadRoot = Paths.get("uploads");

    public String storePostImage(MultipartFile file) {
        return storeImage(file, "post");
    }

    public String storeProfileImage(MultipartFile file) {
        return storeImage(file, "profile");
    }

    private String storeImage(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = getExtension(originalName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Format image non supporte. Utilisez: jpg, jpeg, png, gif, webp.");
        }

        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image trop volumineuse. Taille maximale: 5MB.");
        }

        try {
            Files.createDirectories(uploadRoot);
            String storedName = prefix + "-" + UUID.randomUUID() + "." + extension;
            Path target = uploadRoot.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload de l'image.", e);
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }
}

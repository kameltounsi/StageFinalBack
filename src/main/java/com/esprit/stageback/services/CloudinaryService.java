package com.esprit.stageback.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadAvatar(MultipartFile file, Long userId) {
        validateImage(file);
        try {
            Map<?, ?> res = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "avatars/" + (userId != null ? userId : "unknown"),
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", true
                    )
            );
            Object secure = res.get("secure_url");
            Object plain  = res.get("url");
            if (secure == null && plain == null) {
                throw new IllegalStateException("Upload succeeded but Cloudinary did not return a URL.");
            }
            return (secure != null ? secure : plain).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload avatar to Cloudinary", e);
        }
    }

    public String uploadFile(MultipartFile file) {
        // Compat: garde ta méthode existante si d'autres la consomment
        validateImage(file);
        try {
            Map<?, ?> res = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", true
                    )
            );
            Object secure = res.get("secure_url");
            Object plain  = res.get("url");
            if (secure == null && plain == null) {
                throw new IllegalStateException("Upload succeeded but Cloudinary did not return a URL.");
            }
            return (secure != null ? secure : plain).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload to Cloudinary", e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Max file size is 5MB.");
        }
        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ct.startsWith("image/") && !MediaType.IMAGE_JPEG_VALUE.equals(ct)
                && !MediaType.IMAGE_PNG_VALUE.equals(ct) && !MediaType.IMAGE_GIF_VALUE.equals(ct)
                && !ct.contains("webp")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }
    }
}

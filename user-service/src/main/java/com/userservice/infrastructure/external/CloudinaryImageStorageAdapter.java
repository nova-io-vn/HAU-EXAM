package com.userservice.infrastructure.external;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.userservice.application.exception.ImageUploadException;
import com.userservice.application.model.ImageUploadCommand;
import com.userservice.application.model.StoredImage;
import com.userservice.application.port.out.ImageStoragePort;
import com.userservice.infrastructure.config.CloudinaryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CloudinaryImageStorageAdapter implements ImageStoragePort {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryImageStorageAdapter.class);
    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    public CloudinaryImageStorageAdapter(Cloudinary cloudinary, CloudinaryProperties properties) { this.cloudinary = cloudinary; this.properties = properties; }

    @Override
    public StoredImage upload(ImageUploadCommand command, String folder) {
        if (!properties.configured()) throw new ImageUploadException("Image storage is not configured");
        try {
            Map<?, ?> result = cloudinary.uploader().upload(command.bytes(), ObjectUtils.asMap("folder", folder, "resource_type", "image", "use_filename", false, "unique_filename", true));
            return new StoredImage(value(result, "url"), value(result, "secure_url"), value(result, "public_id"), value(result, "format"), number(result, "width"), number(result, "height"), numberLong(result, "bytes"));
        } catch (Exception ex) {
            log.warn("Cloudinary image upload failed; type={}", ex.getClass().getSimpleName());
            throw new ImageUploadException("Could not upload image", ex);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank() || !properties.configured()) return;
        try { cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image")); }
        catch (Exception ex) { log.warn("Cloudinary image cleanup failed for publicId={} type={}", publicId, ex.getClass().getSimpleName()); }
    }

    private static String value(Map<?, ?> map, String key) { return map.get(key) == null ? null : String.valueOf(map.get(key)); }
    private static Integer number(Map<?, ?> map, String key) { return map.get(key) instanceof Number n ? n.intValue() : null; }
    private static Long numberLong(Map<?, ?> map, String key) { return map.get(key) instanceof Number n ? n.longValue() : null; }
}

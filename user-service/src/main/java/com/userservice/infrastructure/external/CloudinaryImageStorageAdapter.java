package com.userservice.infrastructure.external;

import com.cloudinary.utils.ObjectUtils;
import com.userservice.application.exception.ImageUploadException;
import com.userservice.application.model.ImageUploadCommand;
import com.userservice.application.model.StoredImage;
import com.userservice.application.port.out.ImageStoragePort;
import com.userservice.infrastructure.service.CloudinarySettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CloudinaryImageStorageAdapter implements ImageStoragePort {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryImageStorageAdapter.class);
    private final CloudinarySettingsService settings;

    public CloudinaryImageStorageAdapter(CloudinarySettingsService settings) { this.settings = settings; }

    @Override
    public StoredImage upload(ImageUploadCommand command, String folder) {
        var cloudinary = settings.client();
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
        if (publicId == null || publicId.isBlank()) return;
        try { settings.client().uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image")); }
        catch (Exception ex) { log.warn("Cloudinary image cleanup failed for publicId={} type={}", publicId, ex.getClass().getSimpleName()); }
    }

    private static String value(Map<?, ?> map, String key) { return map.get(key) == null ? null : String.valueOf(map.get(key)); }
    private static Integer number(Map<?, ?> map, String key) { return map.get(key) instanceof Number n ? n.intValue() : null; }
    private static Long numberLong(Map<?, ?> map, String key) { return map.get(key) instanceof Number n ? n.longValue() : null; }
}

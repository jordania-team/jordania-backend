package com.jordania.api.Storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ImageStorageService {

    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final Duration presignedUrlTtl;
    private final String publicBaseUrl;
    private final long maxSizeBytes;

    public ImageStorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${app.storage.images.bucket}") String bucket,
            @Value("${app.storage.images.presigned-url-ttl}") Duration presignedUrlTtl,
            @Value("${app.storage.images.public-base-url}") String publicBaseUrl,
            @Value("${app.storage.images.max-size-bytes}") long maxSizeBytes
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = normalizedOptional(bucket);
        this.presignedUrlTtl = presignedUrlTtl;
        this.publicBaseUrl = normalizedBaseUrl(publicBaseUrl);
        this.maxSizeBytes = maxSizeBytes;
    }

    public String uploadTutorProfileImage(UUID tutorId, MultipartFile file) {
        String contentType = validateImage(file);
        String extension = ALLOWED_IMAGE_TYPES.get(contentType);
        String key = "uploads/tutors/%s/profile/%s.%s".formatted(tutorId, UUID.randomUUID(), extension);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(requiredBucket())
                .key(key)
                .contentType(contentType)
                .contentLength(file.getSize())
                .cacheControl("private, max-age=31536000")
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return key;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "could not read image file", exception);
        } catch (S3Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "could not upload image to S3", exception);
        }
    }

    public String displayUrl(String storedValue) {
        String normalized = normalizedOptional(storedValue);
        if (normalized == null || isHttpUrl(normalized)) {
            return normalized;
        }

        if (publicBaseUrl != null) {
            return publicBaseUrl + "/" + encodeS3Key(normalized);
        }

        if (bucket == null) {
            return normalized;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(normalized)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(presignedUrlTtl)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public boolean isDisplayUrlForStoredObject(String candidate, String storedValue) {
        String normalizedCandidate = normalizedOptional(candidate);
        String normalizedStoredValue = normalizedOptional(storedValue);
        if (normalizedCandidate == null
                || normalizedStoredValue == null
                || !isHttpUrl(normalizedCandidate)
                || isHttpUrl(normalizedStoredValue)) {
            return false;
        }

        if (publicBaseUrl != null && normalizedCandidate.startsWith(publicBaseUrl + "/")) {
            String candidateKey = normalizedCandidate.substring(publicBaseUrl.length() + 1);
            return candidateKey.equals(encodeS3Key(normalizedStoredValue));
        }

        try {
            URI uri = URI.create(normalizedCandidate);
            String query = uri.getRawQuery();
            if (query == null || !query.contains("X-Amz-Signature")) {
                return false;
            }

            String path = uri.getPath();
            return path.equals("/" + normalizedStoredValue) || path.endsWith("/" + normalizedStoredValue);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public void deleteIfStoredObject(String storedValue) {
        String normalized = normalizedOptional(storedValue);
        if (normalized == null || isHttpUrl(normalized)) {
            return;
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(requiredBucket())
                .key(normalized)
                .build();

        try {
            s3Client.deleteObject(request);
        } catch (S3Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "could not delete image from S3", exception);
        }
    }

    private String validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image file is required");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image file is too large");
        }

        String contentType = normalizedContentType(file.getContentType());
        if (!ALLOWED_IMAGE_TYPES.containsKey(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported image type");
        }
        return contentType;
    }

    private String requiredBucket() {
        if (bucket == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "images bucket is not configured");
        }
        return bucket;
    }

    private boolean isHttpUrl(String value) {
        String lowerCase = value.toLowerCase(Locale.ROOT);
        return lowerCase.startsWith("http://") || lowerCase.startsWith("https://");
    }

    private String normalizedContentType(String contentType) {
        String normalized = normalizedOptional(contentType);
        if (normalized == null) {
            return "";
        }
        int parameterStart = normalized.indexOf(';');
        String withoutParameters = parameterStart >= 0 ? normalized.substring(0, parameterStart) : normalized;
        return withoutParameters.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizedOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizedBaseUrl(String value) {
        String normalized = normalizedOptional(value);
        if (normalized == null) {
            return null;
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String encodeS3Key(String key) {
        return Stream.of(key.split("/", -1))
                .map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
    }
}

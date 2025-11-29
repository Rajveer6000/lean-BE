package com.lean.lean.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.time.Duration;

@Service
public class S3Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3Service.class);
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int STRING_LENGTH = 100;
    @SuppressWarnings("unused")
    private static final int NUM_STRINGS_TO_GENERATE = 1_000_000;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    public static String generateRandomString() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(STRING_LENGTH);
        for (int i = 0; i < STRING_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    public String uploadFile(File file, String name) {
        try (FileInputStream input = new FileInputStream(file)) {
            String path;
            if (StringUtils.isEmpty(name)) {
                path = generateRandomString().concat(".pdf");
            } else {
                path = name;
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .contentLength(file.length())
                    .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(input, file.length()));

            if (putObjectResponse != null) {
                return path;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to upload file {} to S3", file.getName(), e);
        }
        return null;
    }

    public URL generateSignedUrl(String objectKey, Duration expiration, String customBucket) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(customBucket)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url();
        } catch (S3Exception e) {
            LOGGER.error("Failed to generate signed URL for key {} in bucket {}", objectKey, customBucket, e);
            return null;
        }
    }

    public ResponseInputStream<GetObjectResponse> fetchS3Object(String objectKey, String customBucket) {
        LOGGER.info("Fetching object {} from bucket {}", objectKey, customBucket);
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(customBucket)
                    .key(objectKey)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (S3Exception e) {
            LOGGER.error("Failed to fetch object {} from bucket {}", objectKey, customBucket, e);
            return null;
        }
    }

    public String getFilePath(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return "uploads/" + fileName;
    }

    public String generateFileUrl(String objectKey, long expiryMinutes) {
        if (StringUtils.isBlank(objectKey)) {
            return null;
        }
        Duration expiration = Duration.ofMinutes(Math.max(expiryMinutes, 1));
        URL url = generateSignedUrl(objectKey, expiration, bucketName);
        return url != null ? url.toString() : null;
    }

    public byte[] downloadObjectAsByteArray(String customBucket, String key) throws IOException {
        Duration expiration = Duration.ofMinutes(5);
        URL signedUrl = generateSignedUrl(key, expiration, customBucket);

        try (InputStream inputStream = signedUrl.openStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }
    }
}

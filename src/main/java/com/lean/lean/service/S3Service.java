package com.lean.lean.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Date;

@Service
public class S3Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3Service.class);
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int STRING_LENGTH = 100;
    @SuppressWarnings("unused")
    private static final int NUM_STRINGS_TO_GENERATE = 1_000_000;

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
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
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.length());
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path, input, objectMetadata);
            PutObjectResult putObjectResult = amazonS3.putObject(putObjectRequest);
            if (putObjectResult != null) {
                return path;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to upload file {} to S3", file.getName(), e);
        }
        return null;
    }

    public URL generateSignedUrl(String objectKey, Date expiration, String customBucket) {
        try {
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(customBucket, objectKey)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        } catch (SdkClientException e) {
            LOGGER.error("Failed to generate signed URL for key {} in bucket {}", objectKey, customBucket, e);
            return null;
        }
    }

    public S3Object fetchS3Object(String objectKey, String customBucket) {
        LOGGER.info("Fetching object {} from bucket {}", objectKey, customBucket);
        try {
            return amazonS3.getObject(new GetObjectRequest(customBucket, objectKey));
        } catch (SdkClientException e) {
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
        long expiryMillis = Math.max(expiryMinutes, 1) * 60_000L;
        Date expiration = new Date(System.currentTimeMillis() + expiryMillis);
        URL url = generateSignedUrl(objectKey, expiration, bucketName);
        return url != null ? url.toString() : null;
    }

    public byte[] downloadObjectAsByteArray(String customBucket, String key) throws IOException {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 5;
        expiration.setTime(expTimeMillis);
        URL signedUrl = amazonS3.generatePresignedUrl(customBucket, key, expiration, HttpMethod.GET);
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

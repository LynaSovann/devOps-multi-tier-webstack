package com.java.backend.service.ServiceImpl;

import com.java.backend.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSizeProperty;

    public MinioServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    private long getMaxFileSize() {
        if (maxFileSizeProperty.endsWith("MB")) {
            return Long.parseLong(maxFileSizeProperty.replace("MB", "")) * 1024 * 1024;
        } else if (maxFileSizeProperty.endsWith("KB")) {
            return Long.parseLong(maxFileSizeProperty.replace("KB", "")) * 1024;
        } else if (maxFileSizeProperty.endsWith("GB")) {
            return Long.parseLong(maxFileSizeProperty.replace("GB", "")) * 1024 * 1024 * 1024;
        } else {
            return 10 * 1024 * 1024; // Default 10MB
        }
    }

    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    @Override
    public String uploadProfileImage(Integer userId, MultipartFile file) {
        // Validate file
        validateFile(file);

        // Ensure bucket exists
        ensureBucketExists();

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String fileName = "profiles/" + userId + "_" + System.currentTimeMillis() + extension;

            // Upload to MinIO
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }


            return minioUrl + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            System.out.println("Failed to upload image: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteProfileImage(String fileName) {
        try {
            // Extract object name from URL if full URL is provided
            String objectName = fileName;
            if (fileName.contains(bucketName + "/")) {
                objectName = fileName.substring(fileName.indexOf(bucketName + "/") + bucketName.length() + 1);
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );


        } catch (Exception e) {
            System.out.println("Failed to delete file: {}" + e.getMessage());
            // Don't throw exception if file doesn't exist

        }
    }

    @Override
    public String getPresignedUrl(String fileName) {
        try {
            // Extract object name from URL if full URL is provided
            String objectName = fileName;
            if (fileName.contains(bucketName + "/")) {
                objectName = fileName.substring(fileName.indexOf(bucketName + "/") + bucketName.length() + 1);
            }

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );

        } catch (Exception e) {
            System.out.println("Failed to generate image URL");
            return null;
        }
    }

    @Override
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );

                // Set bucket policy to public read
                String policy = """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": {"AWS": "*"},
                                    "Action": ["s3:GetObject"],
                                    "Resource": ["arn:aws:s3:::%s/*"]
                                }
                            ]
                        }
                        """.formatted(bucketName);

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );

                System.out.println("Bucket created and configured: {}" + bucketName);
            }

        } catch (Exception e) {
            System.out.println("Failed to ensure bucket exists: " + e.getMessage());

        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            System.out.println("File is required");

        }

        // Check file size
        long maxSize = getMaxFileSize();
        if (file.getSize() > maxSize) {
            System.out.println("File is too large: " + file.getSize());

        }

        // Check content type
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            System.out.println("File is not valid: " + file.getOriginalFilename());

        }
    }

}

// src/main/java/com/esprit/stageback/storage/WasabiS3StorageService.java
package com.esprit.stageback.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;

@Slf4j
@Service
public class WasabiS3StorageService implements StorageService {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;

    public WasabiS3StorageService(
            @Value("${wasabi.access-key}") String accessKey,
            @Value("${wasabi.secret-key}") String secretKey,
            @Value("${wasabi.bucket}") String bucket,
            @Value("${wasabi.endpoint}") String endpoint,                  // e.g. https://s3.eu-west-2.wasabisys.com
            @Value("${wasabi.signing-region:us-east-1}") String signingRegion, // keep us-east-1 for Wasabi signing
            @Value("${wasabi.path-style:true}") boolean pathStyle
    ) {
        this.bucket = Objects.requireNonNull(bucket, "bucket");

        var creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));

        // S3 client for data ops
        this.s3 = S3Client.builder()
                .credentialsProvider(creds)
                .region(Region.of(signingRegion))
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyle)
                        .build())
                .build();

        // Presigner for pre-signed URLs (same endpoint/region/creds)
        this.presigner = S3Presigner.builder()
                .credentialsProvider(creds)
                .region(Region.of(signingRegion))
                .endpointOverride(URI.create(endpoint))
                .build();
    }

    // === StorageService implementation ===

    @Override
    public void upload(String key, String contentType, long contentLength, InputStream in) {
        try {
            if (key.startsWith("/")) key = key.substring(1);

            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            // Important: use known length to avoid chunked signing surprises
            s3.putObject(req, RequestBody.fromInputStream(in, contentLength));
        } catch (S3Exception e) {
            var d = e.awsErrorDetails();
            String code = d != null ? d.errorCode() : "unknown";
            String msg  = d != null ? d.errorMessage() : e.getMessage();
            log.error("Wasabi PutObject failed: code={}, msg={}, status={}, requestId={}, key={}",
                    code, msg, e.statusCode(), e.requestId(), key);
            throw new RuntimeException("Upload failed: " + code + " - " + msg, e);
        } catch (SdkClientException e) {
            throw new RuntimeException("S3 SDK client error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            if (key.startsWith("/")) key = key.substring(1);
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            var d = e.awsErrorDetails();
            String code = d != null ? d.errorCode() : "unknown";
            String msg  = d != null ? d.errorMessage() : e.getMessage();
            log.error("Wasabi DeleteObject failed: code={}, msg={}, status={}, requestId={}, key={}",
                    code, msg, e.statusCode(), e.requestId(), key);
            throw new RuntimeException("Delete failed: " + code + " - " + msg, e);
        } catch (SdkClientException e) {
            throw new RuntimeException("S3 SDK client error: " + e.getMessage(), e);
        }
    }

    @Override
    public java.net.URL presignGet(String key, Duration ttl) {
        try {
            if (key.startsWith("/")) key = key.substring(1);

            var get = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            var presign = GetObjectPresignRequest.builder()
                    .getObjectRequest(get)
                    .signatureDuration(ttl)
                    .build();

            PresignedGetObjectRequest pg = presigner.presignGetObject(presign);
            return pg.url();
        } catch (Exception e) {
            throw new RuntimeException("Presign failed: " + e.getMessage(), e);
        }
    }
}

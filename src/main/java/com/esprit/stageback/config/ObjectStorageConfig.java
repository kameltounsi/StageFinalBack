// src/main/java/com/esprit/stageback/config/ObjectStorageConfig.java
package com.esprit.stageback.config;

import com.esprit.stageback.storage.WasabiProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(WasabiProps.class)
@RequiredArgsConstructor
@Slf4j
public class ObjectStorageConfig {

    private final WasabiProps props;

    private StaticCredentialsProvider creds() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
        );
    }

    @Bean
    public S3Client s3Client() {
        log.info("Wasabi S3 -> endpoint={}, region={}, pathStyle={}",
                props.getEndpoint(), props.getRegion(), props.isPathStyle());

        S3Configuration s3cfg = S3Configuration.builder()
                .pathStyleAccessEnabled(props.isPathStyle()) // <-- v2 correct
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint()))   // ex: https://s3.eu-west-2.wasabisys.com
                .region(Region.of(props.getRegion()))                // ex: eu-west-2
                .credentialsProvider(creds())
                .serviceConfiguration(s3cfg)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        // Pas de serviceConfiguration côté presigner, mais region/endpoint/crédentials suffisent
        return S3Presigner.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .credentialsProvider(creds())
                .build();
    }
}

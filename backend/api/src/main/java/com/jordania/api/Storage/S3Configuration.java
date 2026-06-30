package com.jordania.api.Storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Configuration {

    @Bean
    S3Client s3Client(@Value("${app.storage.images.region}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    @Bean
    S3Presigner s3Presigner(@Value("${app.storage.images.region}") String region) {
        return S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }
}

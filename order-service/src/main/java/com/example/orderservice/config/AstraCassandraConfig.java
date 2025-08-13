package com.example.orderservice.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@EnableCassandraRepositories(basePackages = "com.example.orderservice.repository")
public class AstraCassandraConfig {

    @Value("classpath:secure-connect-ecom-db.zip")
    private Resource secureBundle;

    @Value("${ASTRA_CLIENT_ID}")
    private String clientId;

    @Value("${ASTRA_CLIENT_SECRET}")
    private String clientSecret;

    @Bean
    public CqlSession cqlSession() throws IOException {
        // 將 classpath 裡的 zip 複製到暫存檔
        Path tempFile = Files.createTempFile("secure-connect", ".zip");
        Files.copy(secureBundle.getInputStream(), tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return CqlSession.builder()
                .withCloudSecureConnectBundle(tempFile)
                .withAuthCredentials(clientId, clientSecret)
                .withKeyspace("orders")
                .build();
    }
}
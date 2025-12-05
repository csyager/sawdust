package com.sawdust.controller.configuration;

import com.sawdust.controller.clients.SSLConnectionManager;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Configuration
@Slf4j
public class ClientsConfiguration {
    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient(
            @Value("${amazon.dynamodb.endpoint}") String endpoint
    ) {
        log.info("Initializing ddb client with endpoint {}", endpoint);
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(
            @NonNull DynamoDbClient client
    ) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }

    @Bean
    public SSLConnectionManager sslConnectionManager() throws CertificateException, KeyStoreException, IOException,
            NoSuchAlgorithmException, KeyManagementException {
        return new SSLConnectionManager();
    }
}

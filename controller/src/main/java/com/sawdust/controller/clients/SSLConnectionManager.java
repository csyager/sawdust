package com.sawdust.controller.clients;

import lombok.Getter;
import lombok.NonNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SSLConnectionManager {
    @NonNull
    @Getter
    private final HttpClient httpClient;
    @NonNull
    private final KeyStore keyStore;
    @NonNull
    private final TrustManagerFactory trustManagerFactory;
    @NonNull
    private final CertificateFactory certificateFactory;
    @NonNull
    private final SSLContext sslContext;

    public SSLConnectionManager() throws CertificateException, KeyStoreException, IOException,
            NoSuchAlgorithmException, KeyManagementException {
        // initialize reusable certificate factory
        this.certificateFactory = CertificateFactory.getInstance("X.509");

        // Create and init keystore
        this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        this.keyStore.load(null, null);

        // init trust manager factory
        this.trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        this.trustManagerFactory.init(keyStore);

        // Setup SSLContext once
        this.sslContext = SSLContext.getInstance("TLS");
        this.sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        // Create a single HttpClient instance
        this.httpClient = HttpClient.newBuilder().sslContext(sslContext).build();
    }

    public void addCertificate(String computeId, String certString)
            throws CertificateException, KeyStoreException, KeyManagementException {
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(
                new ByteArrayInputStream(certString.getBytes()));

        keyStore.setCertificateEntry(computeId, cert);
        trustManagerFactory.init(keyStore); // Reinitialize trust manager factory to recognize new cert
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null); // Update SSLContext
    }
}

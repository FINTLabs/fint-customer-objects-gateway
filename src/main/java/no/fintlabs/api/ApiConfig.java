package no.fintlabs.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Configuration
public class ApiConfig {
    @Value("${fint.customer-objects-gateway.encryption-key:}")
    private String encryptionKey;

    @Bean
    public EncryptionKeyPair encryptionKeyPair() throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            return new EncryptionKeyPair();
        }
        return new EncryptionKeyPair(encryptionKey);
    }
}

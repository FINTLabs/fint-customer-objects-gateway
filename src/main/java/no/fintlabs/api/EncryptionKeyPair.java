package no.fintlabs.api;

import lombok.Getter;

import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class EncryptionKeyPair {
    @Getter
    private PrivateKey privateKey;
    @Getter
    private String publicKey;

    public EncryptionKeyPair(String privateKeyPEM) throws NoSuchAlgorithmException, InvalidKeySpecException {
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", ""); // Remove whitespace
        var privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);

        var keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

        var rsaPrivateKey = (RSAPrivateCrtKey) this.privateKey;
        var publicKeySpec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent());
        var publicKey = keyFactory.generatePublic(publicKeySpec);
        this.publicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public EncryptionKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();

        privateKey = pair.getPrivate();
        publicKey = Base64.getEncoder().encodeToString((pair.getPublic().getEncoded()));
    }
}

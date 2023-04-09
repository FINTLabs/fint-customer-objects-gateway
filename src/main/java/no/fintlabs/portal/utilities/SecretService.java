package no.fintlabs.portal.utilities;

import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class SecretService {

    public String encryptPassword(String password, String publicKeyString) {
        try {
            return encrypt(password, publicKeyString);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException |
                 BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateSecret() {
        String[] characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890-=!@#$%^&*()_+:<>{}[]".split("");
        StringBuilder secret = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            secret.append(characters[(int) Math.floor(Math.random() * characters.length)]);
        }
        return secret.toString();
    }

    private PublicKey generatePublicKeyFromString(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {

        return KeyFactory
                .getInstance("RSA")
                .generatePublic(
                        new X509EncodedKeySpec(
                                Base64
                                        .getDecoder()
                                        .decode(publicKeyString)
                        )
                );
    }

    private String encrypt(String password, String publicKeyString) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, generatePublicKeyFromString(publicKeyString));

        return Base64
                .getEncoder()
                .encodeToString(encryptCipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
    }
}

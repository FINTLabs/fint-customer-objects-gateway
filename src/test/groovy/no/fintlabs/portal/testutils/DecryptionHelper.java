package no.fintlabs.portal.testutils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Base64;

public class DecryptionHelper {

    public static String decrypt(PrivateKey privateKey, String encryptedPassword) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(
                decryptCipher.doFinal(
                        Base64.getDecoder().decode(encryptedPassword.getBytes(StandardCharsets.UTF_8))),
                StandardCharsets.UTF_8);
    }
}

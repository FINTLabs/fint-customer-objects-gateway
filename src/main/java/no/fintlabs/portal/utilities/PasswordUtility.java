package no.fintlabs.portal.utilities;

import java.util.UUID;

public enum PasswordUtility {
    ;

    public static String newPassword() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateSecret() {
        String[] cc = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890-=!@#$%^&*()_+:<>{}[]".split("");
        String key = "";
        for (int i = 0; i < 50; i++) key += cc[(int) Math.floor(Math.random() * cc.length)];
        return key;
    }
}

package vn.ptit.pttkm3.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {
    public static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DUMMY_HASH = createDummyHash();

    private PasswordHasher() { }

    public static String hash(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return "pbkdf2-sha256$" + ITERATIONS + "$" + encoder.encodeToString(salt)
                + "$" + encoder.encodeToString(derived);
    }

    public static boolean verify(char[] password, String encoded) {
        ParsedHash parsed = parse(encoded);
        if (parsed == null) {
            ParsedHash dummy = parse(DUMMY_HASH);
            derive(password, dummy.salt(), dummy.iterations());
            return false;
        }
        byte[] actual = derive(password, parsed.salt(), parsed.iterations());
        return MessageDigest.isEqual(parsed.key(), actual);
    }

    public static char[] randomPassword() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).toCharArray();
    }

    private static ParsedHash parse(String encoded) {
        if (encoded == null) return null;
        String[] parts = encoded.split("\\$", -1);
        if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) return null;
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
            byte[] key = Base64.getUrlDecoder().decode(parts[3]);
            if (iterations != ITERATIONS || salt.length != SALT_BYTES || key.length != KEY_BITS / 8) return null;
            return new ParsedHash(iterations, salt, key);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Password hashing is unavailable", exception);
        } finally {
            spec.clearPassword();
        }
    }

    private static String createDummyHash() {
        char[] random = new char[48];
        for (int i = 0; i < random.length; i++) random[i] = (char) ('a' + RANDOM.nextInt(26));
        try { return hash(random); }
        finally { java.util.Arrays.fill(random, '\0'); }
    }

    private record ParsedHash(int iterations, byte[] salt, byte[] key) { }
}

package com.assignment;



import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;



import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.math.BigInteger;
import java.security.MessageDigest;

public class EncryptionUtils {
// encryting thepassword and all using the algos
    private static final String ALGORITHM = "AES"; // AES encryption algorithm
    private static final byte[] SECRET_KEY = "MySecretKey12345".getBytes(); // 16 bytes for AES

    public static String encrypt(String plainText) {
        try {
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error during encryption", e);
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error during decryption", e);
        }
    }
    public static String generatePassword(String userName) {
        try {
            // Use SHA-256 or any strong hash function
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = messageDigest.digest(userName.getBytes());
            return new BigInteger(1, hashedBytes).toString(16); // Generate a hex string
        } catch (Exception e) {
            throw new RuntimeException("Error generating password", e);
        }
    }
}


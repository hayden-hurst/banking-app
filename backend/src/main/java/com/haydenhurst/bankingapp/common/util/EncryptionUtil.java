package com.haydenhurst.bankingapp.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;
import java.security.SecureRandom;

// Note to self:
// iv (initialization vector) is a random/pseudo-random, non-secret input used to add randomness to the start of an encryption process


public class EncryptionUtil {
    private final SecretKeySpec secretKey;
    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final int IV_LENGTH = 12; // in bytes

    public EncryptionUtil(String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(), AES);
    }

    public String encrypt(String rawText) {
        try {
            // generate a 12 byte random IV which is preferable by GCM
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Configure AES cipher to use GCM mode
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // perform encryption
            byte[] cipherText = cipher.doFinal(rawText.getBytes()); // returns encrypted bytes of the rawText with the auth tag appended at the end

            // concatenate the IV + cipherText into a one byte array, the IV is used for decryption
            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encrypted); // returns as Base64 string so it can be stored in the database in text form
        } catch (Exception ex){
            throw new RuntimeException("Encryption failed", ex);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            // extract first 12 bytes to get IV
            // then you are left with the rest of the cipherText which has the auth tag at the end
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherText = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
            System.arraycopy(decoded, IV_LENGTH, cipherText, 0, cipherText.length);

            // configure the AES cipher again, but this time we are using decrypt mode
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            return new String(cipher.doFinal(cipherText)); // decrypt the cipherText using the same key and IV
        } catch (Exception ex) {
            throw new RuntimeException("Decryption failed", ex);
        }
    }
}

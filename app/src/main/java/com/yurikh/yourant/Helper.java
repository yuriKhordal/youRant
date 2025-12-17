package com.yurikh.yourant;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;


public class Helper {
    private static final String CIPHER_ENCRYPTION = "AES/GCM/NoPadding";
    private static final int CIPHER_IV_LEN = 12;
    private static final int CIPHER_TAG_LEN = 128;

    public static void displayError(Context ctx, Exception ex) {
        new AlertDialog.Builder(ctx)
                .setTitle("An error has occurred!")
                .setMessage(ex.getMessage())
                .show();
        Log.e("Error", "An error has occurred!", ex);
    }

    public static String timestampToDateStr(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000L);
        return DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
    }

    public static String prettifyTime(long timestamp) {
        final long SECOND = 1000L;
        final long MINUTE = 60*SECOND;
        final long HOUR = 60*MINUTE;
        final long DAY = 24*HOUR;
        final long MONTH = 30*DAY;
        long now = System.currentTimeMillis();
        long diff = now - timestamp*1000L;

        if (diff < MINUTE)
            return (diff/SECOND) + "s";
        else if (diff < HOUR)
            return (diff/MINUTE) + "m";
        else if (diff < DAY)
            return (diff/HOUR) + "h";
        else if (diff < MONTH)
            return (diff/DAY) + "d";

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000L);
        return DateFormat.format("dd/MM/yyyy", cal).toString();
    }

    public static byte[] longToByteArray(long val) {
        return ByteBuffer.allocate(Long.BYTES).putLong(val).array();
    }

    public static long byteArrayToLong(byte[] val) {
        if (val.length != Long.BYTES) {
            throw new IllegalArgumentException(
                "Byte array size not matching the size of a long. " +
                "Expected: " + Long.BYTES + ", got: " + val.length + "."
            );
        }

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(val);
        buffer.flip();
        return buffer.getLong();
    }

    // ============================== Cryptography ==============================

    /**
     * Encrypts a binary message using AES-GCM and return the resulting encrypted binary
     * with the IV vector prepended.
     */
    public static byte[] aesEncrypt(byte[] plain, Key key)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
    IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        // This doesn't work for some fucking reason.
//        byte[] iv = new byte[CIPHER_IV_LEN];
//        new SecureRandom().nextBytes(iv);
//
//        GCMParameterSpec parameterSpec = new GCMParameterSpec(CIPHER_TAG_LEN, iv);
        Cipher cipher = Cipher.getInstance(CIPHER_ENCRYPTION);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(plain);
        return ByteBuffer.allocate(iv.length + encrypted.length)
                .put(iv).put(encrypted).array();
    }

    /**
     * Encrypts a string using AES-GCM and return the resulting encrypted binary as a
     * base64 string, with the IV vector prepended.
     */
    public static String aesEncryptString(String text, Key key)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
    IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] plain = text.getBytes(StandardCharsets.UTF_8);
        byte[] cipher = aesEncrypt(plain, key);
        return Base64.encodeToString(cipher, Base64.NO_WRAP);
    }

    public static byte[] aesEncryptLong(long value, Key key)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
    IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] plain = longToByteArray(value);
        return aesEncrypt(plain, key);
    }

    /**
     * Decrypts an encrypted binary message with an IV prepended using AES-GCM and returns the
     * resulting plain message.
     */
    public static byte[] aesDecrypt(byte[] cipher, Key key)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] iv = new byte[CIPHER_IV_LEN];
        byte[] bEncrypted = new byte[cipher.length - CIPHER_IV_LEN];

        ByteBuffer.wrap(cipher).get(iv).get(bEncrypted);

        GCMParameterSpec parameterSpec = new GCMParameterSpec(CIPHER_TAG_LEN, iv);
        Cipher decryptor = Cipher.getInstance(CIPHER_ENCRYPTION);
        decryptor.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        return decryptor.doFinal(bEncrypted);
    }

    /**
     * Decrypts a base64 string with an IV prepended using AES-GCM and returns the
     * resulting decrypted string.
     */
    public static String aesDecryptAsString(String encrypted, Key key)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] cipher = Base64.decode(encrypted, Base64.NO_WRAP);
        byte[] plain = aesDecrypt(cipher, key);
        return new String(plain, StandardCharsets.UTF_8);
    }


    public static long aesDecryptAsLong(byte[] cipher, Key key)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] plain = aesDecrypt(cipher, key);
        return byteArrayToLong(plain);
    }
}

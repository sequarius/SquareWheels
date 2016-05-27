package com.seuqarius.squarewheels.security;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * A simple util of DES
 * Created by Sequarius on 2016/5/27.
 */
public class DESUtil {

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static final String ENCODED_UTF8 = "UTF-8";

    public static final String ENCODED_GBK = "GBK";

    public static final String ENCODED_GB2312 = "GB2312";

    public static final String ENCODED_ISO88591 = "ISO8859-1";

    public static final String ENCODED_ASCII = "ASCII";

    public static final String ENCODED_UNICODE = "UNICODE";

    public static final String CIPHER_INSTANCE_CBC = "DES/CBC/PKCS5Padding";

    public static final String CIPHER_INSTANCE_ECB = "DES/ECB/PKCS5Padding";
    public static final String ALGORITHM_NAME = "DES";

    public static String KEY;



    /**
     * DES encrypt method with content and keyString.
     *
     * @param content
     * @param keyString
     * @throws Exception
     */
    public static String encrypt(String content, String keyString)
            throws Exception {
        try {
            byte[] keyStringBytes = getStringByte(keyString).substring(0, 8)
                    .toUpperCase().getBytes(ENCODED_ASCII);
            Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_CBC);
            DESKeySpec desKeySpec = new DESKeySpec(keyStringBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_NAME);
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(keyStringBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] theCph = cipher.doFinal(content.getBytes(ENCODED_GB2312));
            return toHexString(theCph);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * DES encrypt method with content and keyString.
     *
     * @param content
     * @param keyString
     * @param encode              the content String encode
     * @param contentStringEncode the encrypted String encode
     * @param cipherInstanceType
     * @return
     * @throws Exception
     */
    public static String encrypt(String content, String keyString,
                                 String encode, String contentStringEncode,
                                 String cipherInstanceType) throws Exception {
        try {
            byte[] keyStringBytes = getStringByte(keyString).substring(0, 8)
                    .toUpperCase().getBytes(encode);
            Cipher cipher = Cipher.getInstance(cipherInstanceType);
            DESKeySpec desKeySpec = new DESKeySpec(keyStringBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_NAME);
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(keyStringBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] theCph = cipher
                    .doFinal(content.getBytes(contentStringEncode));
            return toHexString(theCph);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * DES decrypt method
     *
     * @param message            the string witch to decrypt
     * @param key
     * @param keyEncode
     * @param hexStringEncode
     * @param cipherInstanceType
     * @return
     * @throws Exception
     */
    public static String decrypt(String message, String key, String keyEncode,
                                 String hexStringEncode, String cipherInstanceType) {
        try {
            byte[] byteSrc = convertHexString(message);
            String jqStr = getStringByte(key).substring(0, 8).toUpperCase();
            byte[] theKey = jqStr.getBytes(keyEncode);
            Cipher cipher = Cipher.getInstance(cipherInstanceType);
            DESKeySpec desKeySpec = new DESKeySpec(theKey);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(theKey);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] retByte = cipher.doFinal(byteSrc);
            return new String(retByte, hexStringEncode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * DES decrypt method with message and key.
     *
     * @param message
     * @param key
     * @return
     */
    public static String decrypt(String message, String key) {
        try {
            byte[] byteSrc = convertHexString(message);
            String jqStr = getStringByte(key).substring(0, 8).toUpperCase();
            byte[] theKey = jqStr.getBytes(ENCODED_ASCII);
            Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_CBC);
            DESKeySpec desKeySpec = new DESKeySpec(theKey);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(theKey);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            byte[] retByte = cipher.doFinal(byteSrc);
            return new String(retByte, ENCODED_GB2312);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] convertHexString(String ss) {
        byte digest[] = new byte[ss.length() / 2];
        for (int i = 0; i < digest.length; i++) {
            String byteString = ss.substring(2 * i, 2 * i + 2);
            int byteValue = Integer.parseInt(byteString, 16);
            digest[i] = (byte) byteValue;
        }

        return digest;
    }

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    private static String getStringByte(String str) {
        if (TextUtils.isEmpty(str)) {
            throw new IllegalArgumentException("string input is null!");
        }
        MessageDigest messageDigest = getMessageDigest();
        byte[] digest;
        try {
            digest = messageDigest.digest(str.getBytes("ASCII"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("ASCII not supported!");
        }
        return new String(encodeHex(digest));
    }

    private static String toHexString(byte b[]) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String plainText = Integer.toHexString(0xff & b[i]);
            if (plainText.length() < 2)
                plainText = "0" + plainText;
            hexString.append(plainText);
        }

        return hexString.toString();
    }

    protected static final MessageDigest getMessageDigest() {
        String algorithm = "MD5";
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm ["
                    + algorithm + "]");
        }
    }

}

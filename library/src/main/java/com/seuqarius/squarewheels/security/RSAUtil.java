package com.seuqarius.squarewheels.security;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by Sequarius on 2016/5/27.
 * This is a simple util of RSA to get encrypt and decrypt String with public key
 */
public class RSAUtil {
    private static final String TAG = "RSAUtil";
    public static String RSA_PUBLIC_KEY;
    private static final String ALGORITHM_NAME = "RSA";

    private static PublicKey getPublicKeyFromX509(String algorithm,
                                                  String bysKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedKey = Base64.decode(bysKey, Base64.DEFAULT);
        X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(x509);
    }

    /**
     * Call this method when use static method before.
     * @param publicKey public key of RSA
     */
    public void initWithPublicKey(String publicKey) {
        if (!TextUtils.isEmpty(RSA_PUBLIC_KEY)) {
            Log.w(TAG, "public key changed");
        }
        RSA_PUBLIC_KEY = publicKey;
    }

    /**
     * encrypt method
     * @param content String witch should be encrypt with public key
     * @return
     */
    public static String encryptByPublic(String content) throws IllegalAccessException {
        if (TextUtils.isEmpty(RSA_PUBLIC_KEY)) {
            throw new IllegalAccessException("cant find public key,check you call init method before!");
        }
        try {
            PublicKey pubkey = getPublicKeyFromX509(ALGORITHM_NAME, RSA_PUBLIC_KEY);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);

            byte plaintext[] = content.getBytes("UTF-8");
            byte[] output = cipher.doFinal(plaintext);

            String s = new String(Base64.encode(output, Base64.DEFAULT));

            return s;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * decrypt method
     *
     * @param content encrypt String of private key
     * @return
     */
    public static String decryptByPublic(String content) throws IllegalAccessException {
        if (TextUtils.isEmpty(RSA_PUBLIC_KEY)) {
            throw new IllegalAccessException("cant find public key,check you call init method before!");
        }
        try {
            PublicKey pubkey = getPublicKeyFromX509(ALGORITHM_NAME, RSA_PUBLIC_KEY);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, pubkey);
            byte[] origen = Base64.decode(content, Base64.DEFAULT);
            return new String(cipher.doFinal(origen), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

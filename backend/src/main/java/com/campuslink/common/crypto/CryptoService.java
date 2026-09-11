package com.campuslink.common.crypto;

import com.campuslink.config.AppProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 敏感信息加密与哈希（技术方案 7.1 / 4.3）：
 * <ul>
 *   <li>hash：HMAC-SHA256，用于邮箱 / 学号的等值查询（users.email_hash、student_roster.student_id_hash）。</li>
 *   <li>encrypt/decrypt：AES-256-GCM（随机 IV），用于 email / phone / 学号的静态加密存储。</li>
 * </ul>
 * 两把密钥独立（APP_HASH_KEY / APP_CRYPT_KEY），生产必须覆盖开发默认值（上线检查清单项）。
 */
@Service
public class CryptoService {

    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private final byte[] hashKey;
    private final SecretKey cryptKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoService(AppProperties props) {
        this.hashKey = requireKey(props.getCrypto().getHashKey(), "APP_HASH_KEY");
        // AES 密钥必须恰好 16/24/32 字节：对任意长度的密钥材料做 SHA-256 派生为定长 32 字节
        this.cryptKey = new SecretKeySpec(sha256(requireKey(props.getCrypto().getCryptKey(), "APP_CRYPT_KEY")), "AES");
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /** HMAC-SHA256 → 64 位小写十六进制 */
    public String hash(String plain) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hashKey, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC hash failed", e);
        }
    }

    /** AES-256-GCM：输出 base64(iv ‖ cipherText) */
    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, cryptKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM encrypt failed", e);
        }
    }

    public String decrypt(String encoded) {
        try {
            byte[] all = Base64.getDecoder().decode(encoded);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, cryptKey, new GCMParameterSpec(GCM_TAG_BITS, all, 0, GCM_IV_BYTES));
            byte[] plain = cipher.doFinal(all, GCM_IV_BYTES, all.length - GCM_IV_BYTES);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM decrypt failed", e);
        }
    }

    private static byte[] requireKey(String key, String envName) {
        byte[] bytes = key == null ? new byte[0] : key.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("密钥强度不足：" + envName + " 至少需要 32 字节");
        }
        return bytes;
    }
}
